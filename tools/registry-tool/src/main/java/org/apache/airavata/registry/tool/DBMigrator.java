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

package org.apache.airavata.registry.tool;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DBMigrator {
    private static final Logger logger = LoggerFactory.getLogger(DBMigrator.class);
    private static final String delimiter = ";";
    private static final String MIGRATE_SQL_DERBY = "migrate_derby.sql";
    private static final String MIGRATE_SQL_MYSQL = "migrate_mysql.sql";
    private static final String REGISTRY_VERSION = "registry.version";
    private static String currentAiravataVersion = "0.5";
    private static final String RELATIVE_PATH = "db-scripts/0.6/";
    private static final String SELECT_QUERY = "SELECT config_val FROM CONFIGURATION WHERE config_key=' " + REGISTRY_VERSION + "'";
    private static final String INSERT_QUERY = "INSERT INTO CONFIGURATION (config_key, config_val, expire_date, category_id) VALUES('" +
            REGISTRY_VERSION + "', '" + getIncrementedVersion(currentAiravataVersion) + "', '" + getCurrentDate() +
            "','SYSTEM')";
    private static String jdbcURL;
    private static String jdbcUser;
    private static String jdbcPwd;

    public static void main(String[] args) {
        parseArguments(args);
        updateDB(jdbcURL, jdbcUser, jdbcPwd);
    }

    //we assume given database is up and running
    public static void updateDB (String jdbcUrl, String jdbcUser, String jdbcPwd){
        InputStream sqlStream = null;
        Scanner in = new Scanner(System.in);
        if (jdbcUrl == null || jdbcUrl.equals("")){
            System.out.println("Enter JDBC URL : ");
            jdbcUrl = in.next();
        }
        if (jdbcUser == null || jdbcUser.equals("")){
            System.out.println("Enter JDBC Username : ");
            jdbcUser = in.next();
        }
        if (jdbcPwd == null || jdbcPwd.equals("")){
            System.out.println("Enter JDBC password : ");
            jdbcPwd = in.next();
        }

        String dbType = getDBType(jdbcUrl);
        String jdbcDriver = null;

        Connection connection;
        try {
            if (dbType.contains("derby")){
                jdbcDriver = "org.apache.derby.jdbc.ClientDriver";
                sqlStream = DBMigrator.class.getClassLoader().getResourceAsStream(RELATIVE_PATH + MIGRATE_SQL_DERBY);
            } else if (dbType.contains("mysql")){
                jdbcDriver = "com.mysql.jdbc.Driver";
                sqlStream = DBMigrator.class.getClassLoader().getResourceAsStream(RELATIVE_PATH + MIGRATE_SQL_MYSQL);
            }
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPwd);
            if (canUpdated(connection)){
                executeSQLScript(connection, sqlStream);
                //update configuration table with airavata version
                updateConfigTable(connection);
            }
        } catch (ClassNotFoundException e) {
           logger.error("Unable to find SQL scripts..." , e);
        } catch (InstantiationException e) {
            logger.error("Error while updating the database..." , e);
        } catch (IllegalAccessException e) {
            logger.error("Error while updating the database..." , e);
        } catch (SQLException e) {
            logger.error("Error while updating the database..." , e);
        } catch (Exception e) {
            logger.error("Error while updating the database..." , e);
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
                    executeSQL(sqlString, conn);
                    sql.replace(0, sql.length(), "");
                }
            }
            // Catch any statements not followed by ;
            if (sql.length() > 0) {
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

    public static void parseArguments(String[] args){
        try{
            Options options = new Options();
            options.addOption("url", true , "JDBC URL");
            options.addOption("user", true, "JDBC Username");
            options.addOption("pwd", true, "JDBC Password");
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
            jdbcURL = cmd.getOptionValue("url");
            if (jdbcURL == null){
                logger.info("You should enter JDBC URL and JDBC Credentials as parameters...");
            }
            jdbcUser = cmd.getOptionValue("user");
            if (jdbcUser ==  null){
                logger.info("You should enter JDBC URL and JDBC Credentials as parameters...");
            }
            jdbcPwd = cmd.getOptionValue("pwd");
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters" , e);
        }
    }
}
