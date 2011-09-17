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
import java.util.Iterator;
import java.util.List;

import org.apache.airavata.wsmg.broker.handler.PublishedMessageHandler;
import org.apache.airavata.wsmg.broker.subscription.SubscriptionManager;
import org.apache.airavata.wsmg.commons.WsmgCommonConstants;
import org.apache.airavata.wsmg.commons.config.ConfigurationManager;
import org.apache.airavata.wsmg.commons.storage.WsmgInMemoryStorage;
import org.apache.airavata.wsmg.commons.storage.WsmgPersistantStorage;
import org.apache.airavata.wsmg.commons.storage.WsmgStorage;
import org.apache.airavata.wsmg.config.WSMGParameter;
import org.apache.airavata.wsmg.config.WsmgConfigurationContext;
import org.apache.airavata.wsmg.messenger.ConsumerUrlManager;
import org.apache.airavata.wsmg.messenger.strategy.SendingStrategy;
import org.apache.airavata.wsmg.messenger.strategy.impl.FixedParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.ParallelSender;
import org.apache.airavata.wsmg.messenger.strategy.impl.SerialSender;
import org.apache.airavata.wsmg.util.RunTimeStatistics;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrokerServiceLifeCycle implements org.apache.axis2.engine.ServiceLifeCycle {

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
            overrideAddressingPhaseHander(configContext);
            initConfigurations(configContext, axisService);

            WsmgConfigurationContext brokerConext = (WsmgConfigurationContext) configContext
                    .getProperty(WsmgCommonConstants.BROKER_WSMGCONFIG);

            initQueue(brokerConext.getStorage());
            initDeliveryMethod(brokerConext.getConfigurationManager());

            inited = true;
            configContext.setProperty(WsmgCommonConstants.BROKER_INITED, inited);
        } else {
            log.info("init was already done by another webservice");
        }
    }

    private void initConfigurations(ConfigurationContext configContext, AxisService axisService) {

        WsmgConfigurationContext wsmgConfig = new WsmgConfigurationContext();
        configContext.setProperty(WsmgCommonConstants.BROKER_WSMGCONFIG, wsmgConfig);

        ConfigurationManager configMan = new ConfigurationManager("conf" + File.separator + WsmgCommonConstants.BROKER_CONFIGURATION_FILE_NAME);

        wsmgConfig.setConfigurationManager(configMan);

        String type = configMan.getConfig(WsmgCommonConstants.CONFIG_STORAGE_TYPE,
                WsmgCommonConstants.STORAGE_TYPE_PERSISTANT);

        WsmgStorage storage = null;

        if (WsmgCommonConstants.STORAGE_TYPE_IN_MEMORY.equalsIgnoreCase(type)) {
            storage = new WsmgInMemoryStorage();
        } else {
            try {
                storage = new WsmgPersistantStorage(WsmgCommonConstants.TABLE_NAME_EXPIRABLE_SUBCRIPTIONS,
                        WsmgCommonConstants.TABLE_NAME_NON_EXPIRABLE_SUBCRIPTIONS, configMan);

            } catch (AxisFault e) {
                throw new RuntimeException("Unable to init Broker persistant storage", e);
            }
        }

        wsmgConfig.setStorage(storage);

        SubscriptionManager subManager = new SubscriptionManager(wsmgConfig, storage);
        wsmgConfig.setSubscriptionManager(subManager);

        NotificationProcessor notificatonProcessor = new NotificationProcessor(wsmgConfig);
        wsmgConfig.setNotificationProcessor(notificatonProcessor);

    }

    private void initQueue(WsmgStorage storage) {

        log.info("setting up queue");

        WSMGParameter.OUT_GOING_QUEUE = storage;

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

                // user has asked to use in memory queue
                // but with out starting the delivery thread.
                // this will accumulate message in memory.

                log.error("conflicting configuration ditected, " + "using in memory queue with out starting delivery "
                        + "thread will result memory growth.");

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

    /**
     * @param configContext
     */
    private void overrideAddressingPhaseHander(ConfigurationContext configContext) {

        List<Phase> inflowPhases = configContext.getAxisConfiguration().getPhasesInfo().getINPhases();
        boolean foundFlag = false;

        for (Phase p : inflowPhases) {

            if (p.getName().equalsIgnoreCase("Addressing")) {

                List<Handler> handlers = p.getHandlers();

                for (Iterator<Handler> ite = handlers.iterator(); ite.hasNext();) {
                    Handler h = ite.next();
                    if (h.getClass().isAssignableFrom(PublishedMessageHandler.class)) {
                        p.removeHandler(h.getHandlerDesc());
                        break;
                    }
                }

                p.addHandler(new PublishedMessageHandler(), 0);
                foundFlag = true;
                break;
            }

        }

        if (!foundFlag) {
            throw new RuntimeException("unable to find addressing phase - inside inflow phases");
        }

    }

}
