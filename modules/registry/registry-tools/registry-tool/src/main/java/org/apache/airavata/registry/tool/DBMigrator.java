/**
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
 */
package org.apache.airavata.registry.tool;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Date;

public class DBMigrator {
    private static final Logger logger = LoggerFactory.getLogger(DBMigrator.class);
    private static final String delimiter = ";";
    private static final String MIGRATE_SQL_DERBY = "migrate_derby.sql";
    private static final String MIGRATE_SQL_MYSQL = "migrate_mysql.sql";
    private static final String REGISTRY_VERSION = "registry.version";
    private static final String AIRAVATA_VERSION = "0.5";
    private static String currentAiravataVersion;
    private static String relativePath;
    private static String SELECT_QUERY;
    private static String INSERT_QUERY;
    private static String UPDATE_QUERY;
    private static String jdbcURL;
    private static String jdbcUser;
    private static String jdbcPwd;

    public static void main(String[] args) {
        parseArguments(args);
        generateConfigTableQueries();
        updateDB(jdbcURL, jdbcUser, jdbcPwd);
    }

    public static void generateConfigTableQueries(){
        SELECT_QUERY = "SELECT * FROM CONFIGURATION WHERE config_key='" + REGISTRY_VERSION + "' and category_id='SYSTEM'";
        INSERT_QUERY = "INSERT INTO CONFIGURATION (config_key, config_val, expire_date, category_id) VALUES('" +
                REGISTRY_VERSION + "', '" + getIncrementedVersion(currentAiravataVersion) + "', '" + getCurrentDate() +
                "','SYSTEM')";
        UPDATE_QUERY = "UPDATE CONFIGURATION SET config_val='" + getIncrementedVersion(currentAiravataVersion) + "', expire_date='" + getCurrentDate() +
                        "' WHERE config_key='" + REGISTRY_VERSION + "' and category_id='SYSTEM'";
    }

    //we assume given database is up and running
    public static void updateDB (String jdbcUrl, String jdbcUser, String jdbcPwd){
        relativePath = "db-scripts/" + getIncrementedVersion(currentAiravataVersion) + "/";
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
            File file = null;
            if (dbType.contains("derby")){
                jdbcDriver = "org.apache.derby.jdbc.ClientDriver";
                sqlStream = DBMigrator.class.getClassLoader().getResourceAsStream(relativePath + MIGRATE_SQL_DERBY);
            } else if (dbType.contains("mysql")){
                jdbcDriver = "com.mysql.jdbc.Driver";
                sqlStream = DBMigrator.class.getClassLoader().getResourceAsStream(relativePath + MIGRATE_SQL_MYSQL);
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
        if (!currentAiravataVersion.equals(AIRAVATA_VERSION)){
            String config = executeSelectQuery(conn);
            if (config != null){
                if (config.equals(getIncrementedVersion(currentAiravataVersion))) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if (currentAiravataVersion.equals(AIRAVATA_VERSION)){
            return true;
        }
        return false;
    }

    private static void updateConfigTable (Connection connection){
        // if existing need to update, otherwise insert
        if (executeSelectQuery(connection) != null){
            executeQuery(connection, UPDATE_QUERY);
        } else {
            executeQuery(connection, INSERT_QUERY);
        }
    }

    private static Timestamp getCurrentDate (){
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        Timestamp d = new Timestamp(date.getTime());
        return d;
    }

    private static String getIncrementedVersion (String currentVersion){

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        Double currentVer = Double.parseDouble(currentVersion);
        double v = currentVer + .1;
        String formattedVal = decimalFormat.format(v);
        return formattedVal;
    }

    private static String executeSelectQuery (Connection conn){
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(SELECT_QUERY);
            if (rs != null){
                while (rs.next()) {
                    currentAiravataVersion = rs.getString(2);
                    return currentAiravataVersion;
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage() , e);
        }
        return null;
    }

    private static void executeQuery (Connection conn, String query){
        try {
            Statement statement = conn.createStatement();
            statement.execute(query) ;
        } catch (SQLException e) {
            logger.error(e.getMessage() , e);
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
            System.out.println(sql.toString());
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
            options.addOption("v", true, "Airavata Current Version");
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
            currentAiravataVersion = cmd.getOptionValue("v");
            if (currentAiravataVersion == null){
                logger.info("You should enter current Airavata version you are using...");
            }
        } catch (ParseException e) {
            logger.error("Error while reading command line parameters" , e);
        }
    }

    protected static InputStream readFile(File file) {
        StringBuilder fileContentsBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            char[] buffer = new char[32767];
            bufferedReader = new BufferedReader(new FileReader(file));
            int read = 0;

            do {
                read = bufferedReader.read(buffer);
                if (read > 0) {
                    fileContentsBuilder.append(buffer, 0, read);
                }
            } while (read > 0);
        } catch (Exception e) {
            logger.error("Failed to read file " + file.getPath(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    logger.error("Unable to close BufferedReader for " + file.getPath(), e);
                }
            }
        }
        System.out.println(fileContentsBuilder.toString());
        InputStream is = new ByteArrayInputStream(fileContentsBuilder.toString().getBytes());

        return is;
    }
}
