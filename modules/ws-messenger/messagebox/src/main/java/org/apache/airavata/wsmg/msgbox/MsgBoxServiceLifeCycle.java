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

import java.net.URI;

import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.PeriodicExecutorThread;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ServiceUtils;
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
//    private static final String CONFIGURATION_FILE_NAME = "airavata-server.properties";
    private static final String TRUE = Boolean.toString(true);
//    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;
    public static final String JCR_CLASS = "jcr.class";
    public static final String JCR_USER = "jcr.user";
    public static final String JCR_PASS = "jcr.pass";
    public static final String ORG_APACHE_JACKRABBIT_REPOSITORY_URI = "org.apache.jackrabbit.repository.uri";
	private static final String MESSAGE_BOX_SERVICE_NAME = "MsgBoxService";
	private static final String SERVICE_URL = "message_box_service_url";
	private static final String JCR_REGISTRY = "registry";
	private Thread thread;
    
    public void shutDown(ConfigurationContext configurationcontext, AxisService axisservice) {
        logger.info("Message box shutting down");
        AiravataAPI registry = (AiravataAPI) configurationcontext.getProperty(JCR_REGISTRY);
        if (registry != null && thread != null) {
            try {
                registry.getAiravataManager().unsetMessageBoxURI();
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.info("Message box url update thread is interrupted");
            }
        }
        if (configurationcontext.getProperty(MsgBoxCommonConstants.MSGBOX_STORAGE) != null) {
            MsgBoxStorage msgBoxStorage = (MsgBoxStorage) configurationcontext
                    .getProperty(MsgBoxCommonConstants.MSGBOX_STORAGE);
            msgBoxStorage.dispose();
        }
    }

    public void startUp(ConfigurationContext configurationcontext, AxisService axisservice) {
    	AiravataUtils.setExecutionAsServer();
        Axis2Utils.overrideAddressingPhaseHander(configurationcontext, new StoreMessageHandler());

        // Load the configuration file from the classpath
        ConfigurationManager confmanager = new ConfigurationManager();
        initDatabase(configurationcontext, confmanager);
        configurationcontext.setProperty(ConfigKeys.MSG_PRESV_INTERVAL,getIntervaltoExecuteDelete(confmanager));
        final ConfigurationContext context=configurationcontext;
        new Thread(){
    		@Override
    		public void run() {
//    	        Properties properties = new Properties();
    	        try {
//    	            URL url = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
//    	            properties.load(url.openStream());
//    	            Map<String, String> map = new HashMap<String, String>((Map) properties);
	            	try {
						Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

                    String userName = ServerSettings.getSystemUser();
                    String gateway = ServerSettings.getSystemUserGateway();

                    AiravataAPI airavataAPI = AiravataAPIFactory.getAPI(gateway, userName);
					String localAddress = ServiceUtils.generateServiceURLFromConfigurationContext(context, MESSAGE_BOX_SERVICE_NAME);
					logger.debug("MESSAGE BOX SERVICE_ADDRESS:" + localAddress);
                    context.setProperty(SERVICE_URL,new URI(localAddress));
					context.setProperty(JCR_REGISTRY,airavataAPI);
					/*
					 * Heart beat message to registry
					 */
					thread = new MsgBoxURLRegisterThread(airavataAPI, context);
					thread.start();
    	        } catch (Exception e) {
    	            logger.error(e.getMessage(), e);
    	        }
    		}
    	}.start();
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

    private long getIntervaltoExecuteDelete(ConfigurationManager configs) {
        int messagePreservationDays = configs.getConfig(ConfigKeys.MSG_PRESV_INTERVAL_DAYS, 0);
        int messagePreservationHours = configs.getConfig(ConfigKeys.MSG_PRESV_INTERVAL_HRS, 0);
        int messagePreservationMinutes = configs.getConfig(ConfigKeys.MSG_PRESV_INTERVAL_MINS, 5);

        long interval = messagePreservationDays * 24l;
        interval = (interval + messagePreservationHours) * 60;
        interval = (interval + messagePreservationMinutes) * 60;
        interval = interval * 1000;
        return interval;
    }
    
    class MsgBoxURLRegisterThread extends PeriodicExecutorThread {
        private ConfigurationContext context = null;

        MsgBoxURLRegisterThread(AiravataAPI registry, ConfigurationContext context) {
            super(registry);
            this.context = context;
        }

        @Override
        protected void updateRegistry(AiravataAPI registry) throws Exception {
            URI localAddress = (URI) this.context.getProperty(SERVICE_URL);
            registry.getAiravataManager().setMessageBoxURI(localAddress);
            logger.debug("Updated Message box service URL in to Repository");
        }
    }

}
