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
package org.apache.airavata.cli.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initializeDatabase(
            String dbName,
            Flyway flyway,
            DataSource dataSource,
            String configKey,
            String version,
            String configValueColumn,
            boolean includeExpireDate,
            boolean clean) {
        try {
            logger.info("Initializing database: {}", dbName);

            // Step 1: Clean database if enabled
            if (clean) {
                logger.info("Cleaning database: {}", dbName);
                flyway.clean();
                logger.info("Database {} cleaned successfully", dbName);
            }

            // Step 2: Run Flyway migrations (creates tables)
            logger.info("Running Flyway migrations for: {}", dbName);
            flyway.migrate();
            logger.info("Flyway migrations completed for: {}", dbName);

            // Step 3: Insert version number
            logger.info("Inserting version number for: {}", dbName);
            insertVersionNumber(dataSource, configKey, version, configValueColumn, includeExpireDate);
            logger.info("Version number inserted successfully for: {}", dbName);

        } catch (Exception e) {
            logger.error("Failed to initialize database: {}", dbName, e);
            throw new RuntimeException("Database initialization failed for: " + dbName, e);
        }
    }

    private static void insertVersionNumber(
            DataSource dataSource,
            String configKey,
            String version,
            String configValueColumn,
            boolean includeExpireDate)
            throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            // Check if version already exists
            String selectSql = String.format("SELECT %s FROM CONFIGURATION WHERE CONFIG_KEY = ?", configValueColumn);
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, configKey);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        // Version exists, update it
                        String updateSql;
                        if (includeExpireDate) {
                            updateSql = String.format(
                                    "UPDATE CONFIGURATION SET %s = ?, EXPIRE_DATE = ?, CATEGORY_ID = ? WHERE CONFIG_KEY = ?",
                                    configValueColumn);
                        } else {
                            updateSql = String.format(
                                    "UPDATE CONFIGURATION SET %s = ? WHERE CONFIG_KEY = ?", configValueColumn);
                        }
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, version);
                            if (includeExpireDate) {
                                updateStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                updateStmt.setString(3, "SYSTEM");
                                updateStmt.setString(4, configKey);
                            } else {
                                updateStmt.setString(2, configKey);
                            }
                            updateStmt.executeUpdate();
                            logger.debug("Updated version {} = {} in CONFIGURATION", configKey, version);
                        }
                    } else {
                        // Version doesn't exist, insert it
                        String insertSql;
                        if (includeExpireDate) {
                            insertSql = String.format(
                                    "INSERT INTO CONFIGURATION (CONFIG_KEY, %s, EXPIRE_DATE, CATEGORY_ID) VALUES (?, ?, ?, ?)",
                                    configValueColumn);
                        } else {
                            insertSql = String.format(
                                    "INSERT INTO CONFIGURATION (CONFIG_KEY, %s) VALUES (?, ?)", configValueColumn);
                        }
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setString(1, configKey);
                            insertStmt.setString(2, version);
                            if (includeExpireDate) {
                                insertStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                                insertStmt.setString(4, "SYSTEM");
                            }
                            insertStmt.executeUpdate();
                            logger.debug("Inserted version {} = {} into CONFIGURATION", configKey, version);
                        }
                    }
                }
            }
        }
    }
}
