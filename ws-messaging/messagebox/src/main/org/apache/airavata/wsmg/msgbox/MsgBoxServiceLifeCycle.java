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

package org.apache.airavata.wsmg.msgbox;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.airavata.wsmg.msgbox.Storage.dbpool.DatabaseStorageImpl;
import org.apache.airavata.wsmg.msgbox.Storage.dbpool.JdbcStorage;
import org.apache.airavata.wsmg.msgbox.Storage.memory.InMemoryImpl;
import org.apache.airavata.wsmg.msgbox.util.ConfigKeys;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxCommonConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.log4j.Logger;

/**
 * This class initialize the messageBox service by setting the messageStore based on the configuration done by the user
 * This is the LifeCycle class
 */
public class MsgBoxServiceLifeCycle implements org.apache.axis2.engine.ServiceLifeCycle {

    private static final String CONFIGURATION_FILE_NAME = "configuration.file.name";
    Logger logger = Logger.getLogger(MsgBoxServiceLifeCycle.class);
    JdbcStorage db;

    public void shutDown(ConfigurationContext configurationcontext, AxisService axisservice) {
        System.out.println("Message box shutting down");
        if (db != null)
            db.closeAllConnections();
    }

    public void startUp(ConfigurationContext configurationcontext, AxisService axisservice) {

        overrideAddressingPhaseHander(configurationcontext);

        // Load the configuration file from the classpath
        ConfigurationManager confmanager = new ConfigurationManager("conf" + File.separator + "msgBox.properties");
        configurationcontext.setProperty(MsgBoxCommonConstants.CONF_MANAGER, confmanager);
        initDatabase(configurationcontext, confmanager);
        configurationcontext.setProperty(MsgBoxCommonConstants.INIT_MSG_BOX_SKELETON_TRUE, false);
    }

    public void initDatabase(ConfigurationContext configurationcontext, ConfigurationManager confmanager) {

        boolean dbImplemented = true;
        if (confmanager.getConfig(ConfigKeys.USE_DATABSE_STORAGE).equalsIgnoreCase("true")) {
            if (!checkConnection(confmanager)) {
                logger.fatal("Database creation failure at MsgBoxServiceLifeCycle class. Cannot connect with the database");
                throw new RuntimeException("Database failure");
            }
            db = new JdbcStorage(true, confmanager);
            try {
                /* This fails if the table: msgBoxes is not there in the database */
                MsgBoxServiceSkeleton.setStorage(new DatabaseStorageImpl(db));
            } catch (SQLException e) {
                throw new RuntimeException("Database failure");
            }
        }
        if (confmanager.getConfig(ConfigKeys.USE_DATABSE_STORAGE).equalsIgnoreCase("false")) {
            ConcurrentHashMap<String, LinkedList<String>> map = new ConcurrentHashMap<String, LinkedList<String>>();
            InMemoryImpl tempStor = new InMemoryImpl();
            tempStor.setMap(map);
            MsgBoxServiceSkeleton.setStorage(tempStor);
            dbImplemented = false;
        }

        configurationcontext.setProperty(MsgBoxCommonConstants.DB_IMPLEMENTED_TRUE, dbImplemented);
    }

    public boolean checkConnection(ConfigurationManager confmanager) {
        boolean dbexists = true;
        Connection conn;
        try {
            Class.forName(confmanager.getConfig(ConfigKeys.JDBC_DRIVER)).newInstance();
            conn = DriverManager.getConnection(confmanager.getConfig(ConfigKeys.MSG_BOX_JDBC_URL));
            try {
                conn.close();
            } catch (SQLException e) {
                logger.fatal("Database connect is not closed at the test", e);
            }
        } catch (Exception e) {
            logger.fatal("Checked for database connection with provided info. Failed connection", e);
            dbexists = false;
        }
        return dbexists;
    }

    private void overrideAddressingPhaseHander(ConfigurationContext configContext) {
        List<Phase> inflowPhases = configContext.getAxisConfiguration().getPhasesInfo().getINPhases();
        boolean foundFlag = false;

        for (Phase p : inflowPhases) {

            if (p.getName().equalsIgnoreCase("Addressing")) {

                List<Handler> handlers = p.getHandlers();

                for (Iterator<Handler> ite = handlers.iterator(); ite.hasNext();) {
                    Handler h = ite.next();
                    if (h.getClass().isAssignableFrom(StoreMessageHandler.class)) {
                        p.removeHandler(h.getHandlerDesc());
                        break;
                    }
                }

                p.addHandler(new StoreMessageHandler(), 0);
                foundFlag = true;
                break;
            }

        }

        if (!foundFlag) {
            throw new RuntimeException("unable to find addressing phase - inside inflow phases");
        }

    }
}
