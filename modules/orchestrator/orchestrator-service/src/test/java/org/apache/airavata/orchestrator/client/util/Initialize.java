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
package org.apache.airavata.orchestrator.client.util;

import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.StringTokenizer;

public class Initialize {
    private static final Logger logger = LoggerFactory.getLogger(Initialize.class);
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    public  String scriptName = "expcatalog-derby.sql";
    private NetworkServerControl server;
    private static final String delimiter = ";";
    public static final String PERSISTANT_DATA = "Configuration";

    public Initialize(String scriptName) {
        this.scriptName = scriptName;
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

    private static boolean isServerStarted(NetworkServerControl server, int ntries)
    {
        for (int i = 1; i <= ntries; i ++)
        {
            try {
                Thread.sleep(500);
                server.ping();
                return true;
            }
            catch (Exception e) {
                if (i == ntries)
                    return false;
            }
        }
        return false;
    }

    private void executeSQLScript(Connection conn) throws Exception {
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;
        try{

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(scriptName);
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
                executeSQL(sql.substring(0, sql.length() - delimiter.length()), conn);
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

    private static void executeSQL(String sql, Connection conn) throws Exception {
        // Check and ignore empty statements
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
                // eliminating the table already exception for the derby
                // database
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

    public void stopDerbyServer() throws SQLException{
        try {
            server.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SQLException("Error while stopping derby server", e);
        }
    }
}
