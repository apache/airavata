/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.airavata.wsmg.commons.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates the database tables required for messagebox and
 * messagebroker with default configuration this class creates embedded derby
 * database in local file system. User can specify required database in
 * appropriate properties files.
 */
public class DatabaseCreator {

    public enum DatabaseType {
        derby("(?i).*derby.*"), 
        mysql("(?i).*mysql.*"), 
        other("");

        private String pattern;

        private DatabaseType(String matchingPattern) {
            this.pattern = matchingPattern;
        }

        public String getMatchingPattern() {
            return this.pattern;
        }
    }

    private static DatabaseType[] supportedDatabase = new DatabaseType[] { DatabaseType.derby, DatabaseType.mysql };

    private static Logger log = LoggerFactory.getLogger(DatabaseCreator.class);
    private ConnectionPool connectionPool;
    private String delimiter = ";";
    Connection conn = null;
    Statement statement;

    public DatabaseCreator(ConnectionPool dataSource) {
        this.connectionPool = dataSource;
    }

    /**
     * Creates database
     * 
     * @throws Exception
     */
    public void createMsgBoxDatabase() throws Exception {
        createDatabase("msgBox");
    }

    /**
     * Creates database
     * 
     * @throws Exception
     */
    public void createMsgBrokerDatabase() throws Exception {
        createDatabase("msgBroker");
    }

    /**
     * Checks whether database tables are created.
     * 
     * @param checkSQL
     *            SQL execute during check.
     * @return <code>true</core> if checkSQL is success, else <code>false</code>
     *         .
     */
    public boolean isDatabaseStructureCreated(String checkSQL) {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Running a query to test the database tables existence.");
            }
            // check whether the tables are already created with a query
            Connection conn = connectionPool.getConnection();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(checkSQL);
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    return false;
                }
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    /**
     * executes given sql
     * 
     * @param sql
     * @throws Exception
     */
    private void executeSQL(String sql) throws Exception {
        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        ResultSet resultSet = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("SQL : " + sql);
            }

            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!ret) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (ret);

            if (log.isDebugEnabled()) {
                log.debug(sql + " : " + updateCountTotal + " rows affected");
            }
            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                log.info(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32")) {
                // eliminating the table already exception for the derby
                // database
                log.info("Table Already Exists", e);
            } else {
                throw new Exception("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing result set.", e);
                }
            }
        }
    }

    /**
     * computes relatational database type using database name
     * 
     * @return DatabaseType
     * @throws Exception
     * 
     */
    public static DatabaseType getDatabaseType(Connection conn) throws Exception {
        try {
            if (conn != null && (!conn.isClosed())) {
                DatabaseMetaData metaData = conn.getMetaData();
                String databaseProductName = metaData.getDatabaseProductName();
                return checkType(databaseProductName);
            }
        } catch (SQLException e) {
            String msg = "Failed to create Airavatadatabase." + e.getMessage();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
        return DatabaseType.other;
    }

    /**
     * Overloaded method with String input
     * 
     * @return DatabaseType
     * @throws Exception
     * 
     */
    public static DatabaseType getDatabaseType(String dbUrl) throws Exception {
        return checkType(dbUrl);
    }

    private static DatabaseType checkType(String text) throws Exception {
        try {
            if (text != null) {
                for (DatabaseType type : supportedDatabase) {
                    if (text.matches(type.getMatchingPattern()))
                        return type;
                }
            }
            String msg = "Unsupported database: " + text
                    + ". Database will not be created automatically by the Airavata. "
                    + "Please create the database using appropriate database scripts for " + "the database.";
            throw new Exception(msg);

        } catch (SQLException e) {
            String msg = "Failed to create Airavatadatabase." + e.getMessage();
            log.error(msg, e);
            throw new Exception(msg, e);
        }
    }

    /**
     * Get scripts location which is prefix + "-" + databaseType + ".sql"
     * 
     * @param prefix
     * @param databaseType
     * @return script location
     */
    private String getScriptLocation(String prefix, DatabaseType databaseType) {
        String scriptName = prefix + "-" + databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        return "database_scripts" + File.separator + scriptName;
    }

    private void createDatabase(String prefix) throws Exception {
        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            executeSQLScript(getScriptLocation(prefix, DatabaseCreator.getDatabaseType(this.conn)));
            conn.commit();
            if (log.isTraceEnabled()) {
                log.trace("Airavatatables are created successfully.");
            }
        } catch (SQLException e) {
            String msg = "Failed to create database tables for Airavataresource store. " + e.getMessage();
            log.error(msg, e);
            throw new Exception(msg, e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close database connection.", e);
            }
        }
    }

    private void executeSQLScript(String dbscriptName) throws Exception {
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(dbscriptName);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("--")) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line);
                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if ("REM".equalsIgnoreCase(token)) {
                        continue;
                    }
                }
                sql.append(" ").append(line);

                // SQL defines "--" as a comment to EOL
                // and in Oracle it may contain a hint
                // so we cannot just remove it, instead we must end it
                if (line.indexOf("--") >= 0) {
                    sql.append("\n");
                }
                if ((checkStringBufferEndsWith(sql, delimiter))) {
                    executeSQL(sql.substring(0, sql.length() - delimiter.length()));
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                executeSQL(sql.toString());
            }
        } catch (IOException e) {
            log.error("Error occurred while executing SQL script for creating Airavatadatabase", e);
            throw new Exception("Error occurred while executing SQL script for creating Airavatadatabase", e);

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Checks that a string buffer ends up with a given string. It may sound
     * trivial with the existing JDK API but the various implementation among
     * JDKs can make those methods extremely resource intensive and perform
     * poorly due to massive memory allocation and copying. See
     * 
     * @param buffer
     *            the buffer to perform the check on
     * @param suffix
     *            the suffix
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a suffix of the character sequence represented by the
     *         StringBuffer object; <code>false</code> otherwise. Note that the
     *         result will be <code>true</code> if the argument is the empty
     *         string.
     */
    public static boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        // this loop is done on purpose to avoid memory allocation performance
        // problems on various JDKs
        // StringBuffer.lastIndexOf() was introduced in jdk 1.4 and
        // implementation is ok though does allocation/copying
        // StringBuffer.toString().endsWith() does massive memory
        // allocation/copying on JDK 1.5
        // See http://issues.apache.org/bugzilla/show_bug.cgi?id=37169
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
}
