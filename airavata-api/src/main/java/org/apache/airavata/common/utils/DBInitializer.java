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
package org.apache.airavata.common.utils;

import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);

    public static void initializeDB(List<DBInitConfig> dbInitConfigs) {
        for (var dbInitConfig : dbInitConfigs) {
            initializeDB(dbInitConfig);
        }
    }

    public static void initializeDB(DBInitConfig dbInitConfig) {
        var jdbcConfig = dbInitConfig.getJDBCConfig();
        var initScriptPrefix = dbInitConfig.getDBInitScriptPrefix();
        var checkTableName = dbInitConfig.getCheckTableName();

        // Create connection
        Connection conn = null;
        try {
            var dbUtil = new DBUtil(jdbcConfig);
            conn = dbUtil.getConnection();
            if (!DatabaseCreator.doesTableExist(checkTableName, conn)) {
                logger.info("Check failed, table: " + checkTableName + " not exists. Executing init script: "
                        + initScriptPrefix);
                DatabaseCreator.initializeTables(initScriptPrefix, conn);
                logger.info("Executed init script: " + initScriptPrefix);
            } else {
                logger.info("Check passed, table: " + checkTableName + " exists. Skipping init script: "
                        + initScriptPrefix);
            }
            conn.commit();

        } catch (Exception e) {
            String message = "Failed to initialize database for " + initScriptPrefix;
            logger.error(message, e);
            throw new RuntimeException(message, e);
        } finally {
            if (conn != null) {
                DBUtil.cleanup(conn);
            }
        }
    }
}
