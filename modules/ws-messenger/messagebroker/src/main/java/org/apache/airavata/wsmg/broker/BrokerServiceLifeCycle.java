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

package org.apache.airavata.wsmg.broker;

import java.io.File;

import org.apache.airavata.wsmg.broker.handler.PublishedMessageHandler;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionManager;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.commons.storage.WsmgInMemoryStorage;
import org.apache.airavata.wsmg.commons.storage.WsmgPersistantStorage;
import org.apache.airavata.wsmg.commons.util.Axis2Utils;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.messenger.ConsumerUrlManager;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.airavata.wsmg.messenger.strategy.impl.FixedParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.ParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.SerialSender;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerServiceLifeCycle implements ServiceLifeCycle {

    private static final Logger log = LoggerFactory.getLogger(BrokerServiceLifeCycle.class);
    private SendingStrategy method = null;

    public void shutDown(ConfigurationContext arg, AxisService service) {
        log.info("broker shutting down");
        if (method != null) {
            method.shutdown();
        }
    }

    public void startUp(ConfigurationContext configContext, AxisService axisService) {

        Boolean inited = (Boolean) configContext.getProperty(WsmgCommonConstants.BROKER_INITED);

        if (inited == null || inited == false) {
            log.info("starting broker");
            Axis2Utils.overrideAddressingPhaseHander(configContext, new PublishedMessageHandler());
            WsmgConfigurationContext brokerConext = initConfigurations(configContext, axisService);
            initQueue(brokerConext);
            initDeliveryMethod(brokerConext.getConfigurationManager());

            inited = true;
            configContext.setProperty(WsmgCommonConstants.BROKER_INITED, inited);
        } else {
            log.info("init was already done by another webservice");
        }
    }

    private WsmgConfigurationContext initConfigurations(ConfigurationContext configContext, AxisService axisService) {

        WsmgConfigurationContext wsmgConfig = new WsmgConfigurationContext();
        configContext.setProperty(WsmgCommonConstants.BROKER_WSMGCONFIG, wsmgConfig);

        ConfigurationManager configMan = new ConfigurationManager("conf" + File.separator
                + WsmgCommonConstants.BROKER_CONFIGURATION_FILE_NAME);

        wsmgConfig.setConfigurationManager(configMan);

        String type = configMan.getConfig(WsmgCommonConstants.CONFIG_STORAGE_TYPE,
                WsmgCommonConstants.STORAGE_TYPE_PERSISTANT);

        /*
         * Determine Storage
         */
        if (WsmgCommonConstants.STORAGE_TYPE_IN_MEMORY.equalsIgnoreCase(type)) {
            WsmgInMemoryStorage inmem = new WsmgInMemoryStorage();

            wsmgConfig.setStorage(inmem);
            wsmgConfig.setQueue(inmem);
            wsmgConfig.setSubscriptionManager(new SubscriptionManager(wsmgConfig, inmem));
            
        } else {
            String jdbcUrl = configMan.getConfig(WsmgCommonConstants.CONFIG_JDBC_URL);
            String jdbcDriver = configMan.getConfig(WsmgCommonConstants.CONFIG_JDBC_DRIVER);
            WsmgPersistantStorage persis = new WsmgPersistantStorage(jdbcUrl, jdbcDriver);
            
            wsmgConfig.setStorage(persis);
            wsmgConfig.setQueue(persis);
            wsmgConfig.setSubscriptionManager(new SubscriptionManager(wsmgConfig, persis));
        }                

        NotificationProcessor notificatonProcessor = new NotificationProcessor(wsmgConfig);
        wsmgConfig.setNotificationProcessor(notificatonProcessor);
        
        return wsmgConfig;
    }

    private void initQueue(WsmgConfigurationContext context) {

        log.info("setting up queue");

        WSMGParameter.OUT_GOING_QUEUE = context.getQueue();

        if (WSMGParameter.cleanQueueonStartUp) {
            log.debug("cleaning up persistant queue");
            WSMGParameter.OUT_GOING_QUEUE.cleanup();
            log.debug("cleaned up persistant queue");
        }

        RunTimeStatistics.setStartUpTime();

    }

    private void initDeliveryMethod(ConfigurationManager configMan) {

        String shouldStart = configMan.getConfig(WsmgCommonConstants.CONFIG_START_DELIVERY_THREADS);

        if (!Boolean.parseBoolean(shouldStart)) {

            if (configMan.getConfig(WsmgCommonConstants.CONFIG_STORAGE_TYPE,
                    WsmgCommonConstants.STORAGE_TYPE_PERSISTANT).equalsIgnoreCase(
                    WsmgCommonConstants.STORAGE_TYPE_IN_MEMORY)) {

                /*
                 *  user has asked to use in memory queue but without starting the delivery thread. this will accumulate message in memory.
                 */
                log.error("conflicting configuration detected, using in memory queue without starting delivery thread will result memory growth.");

            }
            return;
        }

        String deliveryMethod = configMan.getConfig(WsmgCommonConstants.CONFIG_DELIVERY_METHOD,
                WsmgCommonConstants.DELIVERY_METHOD_SERIAL);

        ConsumerUrlManager urlManager = new ConsumerUrlManager(configMan);

        String initedmethod = null;

        if (WsmgCommonConstants.DELIVERY_METHOD_PARALLEL.equalsIgnoreCase(deliveryMethod)) {

            method = new ParallelSender(configMan, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_PARALLEL;

        } else if (WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW.equalsIgnoreCase(deliveryMethod)) {
            method = new FixedParallelSender(configMan, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW;
        } else {
            method = new SerialSender(configMan, urlManager);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_SERIAL;
        }

        method.start();
        log.info(initedmethod + " sending method inited");
    }
}
