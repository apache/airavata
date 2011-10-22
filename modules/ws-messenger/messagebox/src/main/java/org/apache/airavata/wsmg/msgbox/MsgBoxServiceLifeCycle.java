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

import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.commons.util.Axis2Utils;
import org.apache.airavata.wsmg.msgbox.Storage.MsgBoxStorage;
import org.apache.airavata.wsmg.msgbox.Storage.dbpool.DatabaseStorageImpl;
import org.apache.airavata.wsmg.msgbox.Storage.memory.InMemoryImpl;
import org.apache.airavata.wsmg.msgbox.util.ConfigKeys;
import org.apache.airavata.wsmg.msgbox.util.MsgBoxCommonConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initialize the messageBox service by setting the messageStore based on the configuration done by the user
 * This is the LifeCycle class
 */
public class MsgBoxServiceLifeCycle implements ServiceLifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(MsgBoxServiceLifeCycle.class);
    private static final String CONFIGURATION_FILE_NAME = "msgBox.properties";
    private static final String TRUE = Boolean.toString(true);

    public void shutDown(ConfigurationContext configurationcontext, AxisService axisservice) {
        logger.info("Message box shutting down");
        if (configurationcontext.getProperty(MsgBoxCommonConstants.MSGBOX_STORAGE) != null) {
            MsgBoxStorage msgBoxStorage = (MsgBoxStorage) configurationcontext
                    .getProperty(MsgBoxCommonConstants.MSGBOX_STORAGE);
            msgBoxStorage.dispose();
        }
    }

    public void startUp(ConfigurationContext configurationcontext, AxisService axisservice) {

        Axis2Utils.overrideAddressingPhaseHander(configurationcontext, new StoreMessageHandler());

        // Load the configuration file from the classpath
        ConfigurationManager confmanager = new ConfigurationManager("conf" + File.separator + CONFIGURATION_FILE_NAME);
        initDatabase(configurationcontext, confmanager);
    }

    public void initDatabase(ConfigurationContext configurationcontext, ConfigurationManager confmanager) {
        /*
         * Determine Storage
         */
        String useDatabase = confmanager.getConfig(ConfigKeys.USE_DATABASE_STORAGE, TRUE);
        MsgBoxStorage msgBoxStorage = null;
        long time = getInterval(confmanager);
        if (useDatabase.equalsIgnoreCase(TRUE)) {
            String jdbcUrl = confmanager.getConfig(ConfigKeys.MSG_BOX_JDBC_URL);
            String jdbcDriver = confmanager.getConfig(ConfigKeys.MSG_BOX_JDBC_DRIVER);
            msgBoxStorage = new DatabaseStorageImpl(jdbcUrl, jdbcDriver, time);
        } else {
            msgBoxStorage = new InMemoryImpl(time);
        }
        configurationcontext.setProperty(MsgBoxCommonConstants.MSGBOX_STORAGE, msgBoxStorage);

    }

    private long getInterval(ConfigurationManager configs) {
        int messagePreservationDays = configs.getConfig(ConfigKeys.MSG_PRESV_DAYS, 2);
        int messagePreservationHours = configs.getConfig(ConfigKeys.MSG_PRESV_HRS, 0);
        int messagePreservationMinutes = configs.getConfig(ConfigKeys.MSG_PRESV_MINS, 0);

        long interval = messagePreservationDays * 24l;
        interval = (interval + messagePreservationHours) * 60;
        interval = (interval + messagePreservationMinutes) * 60;
        interval = interval * 1000;
        return interval;
    }

}
