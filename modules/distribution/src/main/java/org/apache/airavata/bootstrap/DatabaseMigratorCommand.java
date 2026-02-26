/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.bootstrap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Database migration command.
 */
@Component
@Order(1) // Run before other CommandLineRunners
@ConditionalOnProperty(name = "migrate.enabled", havingValue = "true")
public class DatabaseMigratorCommand implements CommandLineRunner {

    @Value("${migrate.url:}")
    private String migrateUrl;

    @Value("${migrate.user:}")
    private String migrateUser;

    @Value("${migrate.password:}")
    private String migratePassword;

    @Value("${migrate.version:}")
    private String migrateVersion;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigratorCommand.class);
    private static final String delimiter = ";";
    private static final String MIGRATE_SQL_MYSQL = "migrate_mysql.sql";
    private static final String REGISTRY_VERSION = "registry.version";
    private static final String AIRAVATA_VERSION = "0.5";
    private static final String MIGRATION_DIR = "dev-tools/migrations";

    @Override
    public void run(String... args) throws Exception {
        String jdbcURL = migrateUrl;
        String jdbcUser = migrateUser;
        String jdbcPwd = migratePassword;
        String currentAiravataVersion = migrateVersion;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--url=")) {
                jdbcURL = arg.substring(6);
            } else if (arg.startsWith("--user=")) {
                jdbcUser = arg.substring(7);
            } else if (arg.startsWith("--pwd=")) {
                jdbcPwd = arg.substring(6);
            } else if (arg.startsWith("--version=") || arg.startsWith("--v=")) {
                currentAiravataVersion = arg.contains("=") ? arg.substring(arg.indexOf('=') + 1) : null;
            } else if (arg.equals("--url") && i + 1 < args.length) {
                jdbcURL = args[++i];
            } else if (arg.equals("--user") && i + 1 < args.length) {
                jdbcUser = args[++i];
            } else if (arg.equals("--pwd") && i + 1 < args.length) {
                jdbcPwd = args[++i];
            } else if ((arg.equals("--version") || arg.equals("--v")) && i + 1 < args.length) {
                currentAiravataVersion = args[++i];
            }
        }

        if (jdbcURL == null
                || jdbcURL.isEmpty()
                || jdbcUser == null
                || jdbcUser.isEmpty()
                || jdbcPwd == null
                || jdbcPwd.isEmpty()
                || currentAiravataVersion == null
                || currentAiravataVersion.isEmpty()) {
            logger.error("Missing required parameters. Usage: --migrate.enabled=true --migrate.url=... "
                    + "--migrate.user=... --migrate.password=... --migrate.version=...");
            logger.error("Or via command args: --url=... --user=... --pwd=... --version=...");
            System.exit(1);
        }

        logger.info(
                "Starting database migration from version {} to {}",
                currentAiravataVersion,
                getIncrementedVersion(currentAiravataVersion));
        updateDB(jdbcURL, jdbcUser, jdbcPwd, currentAiravataVersion);
        logger.info("Database migration completed successfully");
    }

    private void updateDB(String jdbcUrl, String jdbcUser, String jdbcPwd, String currentAiravataVersion) {
        String dbType = getDBType(jdbcUrl);

        Connection connection = null;
        InputStream sqlStream = null;
        try {
            // Only support MariaDB/MySQL
            String migrationScriptName;
            if (dbType != null && (dbType.contains("mysql") || dbType.contains("mariadb"))) {
                migrationScriptName = MIGRATE_SQL_MYSQL;
            } else {
                logger.error("Unsupported database type: {}. Only MariaDB/MySQL is supported.", dbType);
                throw new RuntimeException(
                        "Unsupported database type: " + dbType + ". Only MariaDB/MySQL is supported.");
            }

            // Find migration script in dev-tools/migrations
            String versionDir = getIncrementedVersion(currentAiravataVersion);
            Path migrationScriptPath = findMigrationScript(versionDir, migrationScriptName);

            if (migrationScriptPath == null || !Files.exists(migrationScriptPath)) {
                logger.error("Migration script not found: {}/{}/{}", MIGRATION_DIR, versionDir, migrationScriptName);
                throw new RuntimeException(
                        "Migration script not found: " + MIGRATION_DIR + "/" + versionDir + "/" + migrationScriptName);
            }

            logger.info("Using migration script: {}", migrationScriptPath);
            sqlStream = new FileInputStream(migrationScriptPath.toFile());

            // JDBC 4.0+ automatically loads drivers via ServiceLoader mechanism
            // when DriverManager.getConnection() is called, so no manual loading needed.
            connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);

            if (canUpdate(connection, currentAiravataVersion)) {
                executeSQLScript(connection, sqlStream);
            } else {
                logger.info("Database is already at the target version or migration not needed");
            }
        } catch (Exception e) {
            logger.error("Error while updating the database", e);
            throw new RuntimeException("Error while updating the database", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Error closing database connection", e);
                }
            }
            if (sqlStream != null) {
                try {
                    sqlStream.close();
                } catch (Exception e) {
                    logger.error("Error closing SQL stream", e);
                }
            }
        }
    }

    private Path findMigrationScript(String versionDir, String scriptName) {
        String[] basePaths = {
            System.getProperty("airavata.home"),
            System.getProperty("user.dir"), // Current working directory
            System.getenv("AIRAVATA_HOME")
        };

        for (String basePath : basePaths) {
            if (basePath == null || basePath.isEmpty()) {
                continue;
            }

            var projectRoot = Paths.get(basePath);
            var migrationPath =
                    projectRoot.resolve(MIGRATION_DIR).resolve(versionDir).resolve(scriptName);
            if (Files.exists(migrationPath)) {
                return migrationPath;
            }

            var apiPath = projectRoot.resolve("modules/airavata-api");
            if (Files.exists(apiPath)) {
                var migrationPathFromApi =
                        projectRoot.resolve(MIGRATION_DIR).resolve(versionDir).resolve(scriptName);
                if (Files.exists(migrationPathFromApi)) {
                    return migrationPathFromApi;
                }
            }

            var current = Paths.get(basePath);
            for (int i = 0; i < 5; i++) {
                var candidate =
                        current.resolve(MIGRATION_DIR).resolve(versionDir).resolve(scriptName);
                if (Files.exists(candidate)) {
                    return candidate;
                }
                Path parent = current.getParent();
                if (parent == null || parent.equals(current)) {
                    break;
                }
                current = parent;
            }
        }

        var currentDir = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < 5; i++) {
            var candidate =
                    currentDir.resolve(MIGRATION_DIR).resolve(versionDir).resolve(scriptName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            Path parent = currentDir.getParent();
            if (parent == null || parent.equals(currentDir)) {
                break;
            }
            currentDir = parent;
        }

        return null;
    }

    private boolean canUpdate(Connection conn, String currentAiravataVersion) {
        // CONFIGURATION table removed; version is tracked by Flyway flyway_schema_history.
        // Run script when migrate.enabled and version is set.
        return true;
    }

    private String getIncrementedVersion(String currentVersion) {
        var decimalFormat = new DecimalFormat("#,##0.0");
        Double currentVer = Double.parseDouble(currentVersion);
        double v = currentVer + .1;
        return decimalFormat.format(v);
    }

    private String executeSelectQuery(Connection conn, String query) {
        try (var statement = conn.createStatement();
             var rs = statement.executeQuery(query)) {
            if (rs != null && rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void executeQuery(Connection conn, String query) {
        try (var statement = conn.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void executeSQLScript(Connection conn, InputStream inputStream) throws Exception {
        var sql = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                var st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }
                sql.append(" ").append(line);

                if (line.indexOf("--") >= 0) {
                    sql.append("\n");
                }
                if ((checkStringBufferEndsWith(sql, delimiter))) {
                    String sqlString = sql.substring(0, sql.length() - delimiter.length());
                    executeSQL(sqlString, conn);
                    sql.replace(0, sql.length(), "");
                }
            }
            logger.info(sql.toString());
            if (sql.length() > 0) {
                executeSQL(sql.toString(), conn);
            }
        } catch (Exception e) {
            logger.error("Error occurred while executing SQL script for creating Airavata database", e);
            throw new Exception("Error occurred while executing SQL script for creating Airavata database", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.error("Error closing reader", e);
                }
            }
        }
    }

    private String getDBType(String jdbcURL) {
        try {
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getScheme();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }

    private void executeSQL(String sql, Connection conn) throws Exception {
        if ("".equals(sql.trim())) {
            return;
        }
        Statement statement = null;
        try {
            logger.debug("SQL : " + sql);

            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            statement = conn.createStatement();
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            do {
                if (!ret) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                }
            } while (ret);

            logger.debug(sql + " : " + updateCountTotal + " rows affected");

            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                logger.warn(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().equals("X0Y32")) {
                logger.info("Table Already Exists", e);
            } else {
                throw new Exception("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Error occurred while closing statement.", e);
                }
            }
        }
    }
}
