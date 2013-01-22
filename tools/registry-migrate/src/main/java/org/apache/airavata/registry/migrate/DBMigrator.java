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

package org.apache.airavata.registry.migrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DBMigrator {
    private static final Logger logger = LoggerFactory.getLogger(DBMigrator.class);
    private static final String delimiter = ";";
    private static final String MIGRATE_SQL_DERBY = "migrate_derby.sql";
    private static final String MIGRATE_SQL_MYSQL = "migrate_mysql.sql";
    private static final String MIGRATED_AIRAVATA_VERSION = "migrated.airavata.version";
    private static String currentAiravataVersion = "0.5";
    private static final String SELECT_QUERY = "SELECT config_val FROM CONFIGURATION WHERE config_key=' " + MIGRATED_AIRAVATA_VERSION + "'";
    private static final String INSERT_QUERY = "INSERT INTO CONFIGURATION (config_key, config_val, expire_date, category_id) VALUES('" +
            MIGRATED_AIRAVATA_VERSION + "', '" + getIncrementedVersion(currentAiravataVersion) + "', '" + getCurrentDate() +
            "','SYSTEM')";


    public static void main(String[] args) {
         updateDB("jdbc:mysql://localhost:3306/persistent_data",
                 "airavata",
                 "airavata");
    }

    //we assume given database is up and running
    public static void updateDB (String jdbcUrl, String jdbcUser, String jdbcPwd){
        Scanner in = new Scanner(System.in);
        if (jdbcPwd == null){
            System.out.println("Enter JDBC password : ");
            jdbcPwd = in.next();
        }
        File sqlFile = null;
        String dbType = getDBType(jdbcUrl);
        String jdbcDriver = null;

        Connection connection;
        try {
            if (dbType.contains("derby")){
                jdbcDriver = "org.apache.derby.jdbc.ClientDriver";
                URL url = DBMigrator.class.getClassLoader()
                        .getResource(MIGRATE_SQL_DERBY);
                sqlFile = new File(url.toURI());

            } else if (dbType.contains("mysql")){
                jdbcDriver = "com.mysql.jdbc.Driver";
                URL url = DBMigrator.class.getClassLoader()
                        .getResource(MIGRATE_SQL_MYSQL);
                sqlFile = new File(url.toURI());
            }
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);
            InputStream sqlStream = new FileInputStream(sqlFile);
            if (canUpdated(connection)){
                executeSQLScript(connection, sqlStream);
                //update configuration table with airavata version
                updateConfigTable(connection);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean canUpdated (Connection conn){
        String config = executeSelectQuery(conn);
        if (config != null) {
            return false;
        } else {
            return true;
        }
    }

    private static void updateConfigTable (Connection connection){
        executeInsertQuery(connection);
    }

    private static Timestamp getCurrentDate (){
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Timestamp d = new Timestamp(date.getTime());
        return d;
    }

    private static String getIncrementedVersion (String currentVersion){
        Double currentVer = Double.valueOf(currentVersion);
        return String.valueOf(currentVer + 0.1);
    }

    private static String executeSelectQuery (Connection conn){
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(SELECT_QUERY);
            if (rs != null){
                while (rs.next()) {
                    currentAiravataVersion = rs.getString(1);
                    return currentAiravataVersion;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void executeInsertQuery (Connection conn){
        try {
            Statement statement = conn.createStatement();
            System.out.println(INSERT_QUERY);
            statement.execute(INSERT_QUERY) ;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void executeSQLScript(Connection conn, InputStream inputStream) throws Exception {
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;
        try{
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
                    String sqlString = sql.substring(0, sql.length() - delimiter.length());
                    System.out.println("##########sql string = " + sqlString);
                    executeSQL(sqlString, conn);
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
                System.out.println("sql string = " + sql.toString());
                executeSQL(sql.toString(), conn);
            }
        }catch (IOException e){
            logger.error("Error occurred while executing SQL script for creating Airavata database", e);
            throw new Exception("Error occurred while executing SQL script for creating Airavata database", e);
        }finally {
            if (reader != null) {
                reader.close();
            }

        }
    }

    private static String getDBType(String jdbcURL){
        try{
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getScheme();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

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

    private static void executeSQL(String sql, Connection conn) throws Exception {
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
            if (e.getSQLState().equals("X0Y32")) {
                logger.info("Table Already Exists", e);
            } else {
                throw new Exception("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Error occurred while closing result set.", e);
                }
            }
        }
    }
}
