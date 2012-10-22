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
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.common.utils.ServiceUtils;
import org.apache.airavata.registry.api.AbstractRegistryUpdaterThread;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.util.RegistryUtils;
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
import org.apache.airavata.wsmg.messenger.Deliverable;
import org.apache.airavata.wsmg.messenger.DeliveryProcessor;
import org.apache.airavata.wsmg.messenger.SenderUtils;
import org.apache.airavata.wsmg.messenger.protocol.DeliveryProtocol;
import org.apache.airavata.wsmg.messenger.protocol.impl.Axis2Protocol;
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
    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

    public static final int JCR_AVAIALABILITY_WAIT_INTERVAL = 1000 * 10;
    public static final String JCR_CLASS = "jcr.class";
    public static final String JCR_USER = "jcr.user";
    public static final String JCR_PASS = "jcr.pass";
    public static final String ORG_APACHE_JACKRABBIT_REPOSITORY_URI = "org.apache.jackrabbit.repository.uri";
    private static final String MESSAGE_BROKER_SERVICE_NAME = "EventingService";
    private static final String SERVICE_URL = "message_broker_service_url";
    private static final String JCR_REGISTRY = "registry";
    private Thread thread;

    private static final long DEFAULT_SOCKET_TIME_OUT = 20000l;

    private DeliveryProcessor proc;
    private ConsumerUrlManager urlManager;

    private static Boolean initialized = false;

    public void shutDown(ConfigurationContext configurationcontext, AxisService service) {
        log.info("broker shutting down");
        if (proc != null) {
            proc.stop();
            proc = null;
        }
        if (urlManager != null) {
            urlManager.stop();
            urlManager = null;
        }

        synchronized (initialized) {
            if (initialized) {
                initialized = false;
                AiravataRegistry2 registry = (AiravataRegistry2) configurationcontext
                        .getProperty(JCR_REGISTRY);
                registry.unsetEventingURI();
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    log.info("Message box url update thread is interrupted");
                }

            }
        }
        log.info("broker shut down");
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

        final ConfigurationContext context = configContext;
        synchronized (initialized) {
            if (!initialized) {
                initialized = true;
                new Thread() {
                    @Override
                    public void run() {
                        Properties properties = new Properties();
                        try {
                            URL url = this.getClass().getClassLoader()
                                    .getResource(REPOSITORY_PROPERTIES);
                            properties.load(url.openStream());
                            Map<String, String> map = new HashMap<String, String>(
                                    (Map) properties);
                            try {
                                Thread.sleep(JCR_AVAIALABILITY_WAIT_INTERVAL);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            AiravataRegistry2 registry = RegistryUtils.getRegistryFromConfig(url);
                            String localAddress = ServiceUtils
                                    .generateServiceURLFromConfigurationContext(
                                            context,
                                            MESSAGE_BROKER_SERVICE_NAME);
                            log.debug("MESSAGE BOX SERVICE_ADDRESS:"
                                    + localAddress);
                            context.setProperty(SERVICE_URL, new URI(
                                    localAddress));
                            context.setProperty(JCR_REGISTRY, registry);
                            /*
                                    * Heart beat message to registry
                                    */
                            thread = new MsgBrokerURLRegisterThread(registry, context);
                            thread.start();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }.start();
            }
        }
    }

    private WsmgConfigurationContext initConfigurations(ConfigurationContext configContext, AxisService axisService) {

        WsmgConfigurationContext wsmgConfig = new WsmgConfigurationContext();
        configContext.setProperty(WsmgCommonConstants.BROKER_WSMGCONFIG, wsmgConfig);

        ConfigurationManager configMan = new ConfigurationManager(
                    WsmgCommonConstants.BROKER_CONFIGURATION_FILE_NAME);

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
                 * user has asked to use in memory queue but without starting the delivery thread. this will accumulate
                 * message in memory.
                 */
                log.error("conflicting configuration detected, using in memory queue without starting delivery thread will result memory growth.");

            }
            return;
        }

        /*
         * Create Protocol
         */
        DeliveryProtocol protocol;
        String protocolClass = configMan
                .getConfig(WsmgCommonConstants.DELIVERY_PROTOCOL, Axis2Protocol.class.getName());
        try {
            Class cl = Class.forName(protocolClass);
            Constructor<DeliveryProtocol> co = cl.getConstructor(null);
            protocol = co.newInstance((Object[]) null);

        } catch (Exception e) {
            log.error("Cannot initial protocol sender", e);
            return;
        }
        protocol.setTimeout(configMan.getConfig(WsmgCommonConstants.CONFIG_SOCKET_TIME_OUT, DEFAULT_SOCKET_TIME_OUT));

        /*
         * Create delivery method
         */
        SendingStrategy method = null;
        String initedmethod = null;
        String deliveryMethod = configMan.getConfig(WsmgCommonConstants.CONFIG_DELIVERY_METHOD,
                WsmgCommonConstants.DELIVERY_METHOD_SERIAL);
        if (WsmgCommonConstants.DELIVERY_METHOD_PARALLEL.equalsIgnoreCase(deliveryMethod)) {
            method = new ParallelSender();
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_PARALLEL;

        } else if (WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW.equalsIgnoreCase(deliveryMethod)) {
            int poolsize = configMan.getConfig(WsmgCommonConstants.CONFIG_SENDING_THREAD_POOL_SIZE,
                    WsmgCommonConstants.DEFAULT_SENDING_THREAD_POOL_SIZE);
            int batchsize = configMan.getConfig(WsmgCommonConstants.CONFIG_SENDING_BATCH_SIZE,
                    WsmgCommonConstants.DEFAULT_SENDING_BATCH_SIZE);
            method = new FixedParallelSender(poolsize, batchsize);
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_THREAD_CREW;

        } else {
            method = new SerialSender();
            initedmethod = WsmgCommonConstants.DELIVERY_METHOD_SERIAL;
        }

        /*
         * Create Deliverable
         */
        urlManager = new ConsumerUrlManager(configMan);
        Deliverable senderUtils = new SenderUtils(urlManager);
        senderUtils.setProtocol(protocol);

        proc = new DeliveryProcessor(senderUtils, method);
        proc.start();
        log.info(initedmethod + " sending method inited");
    }

    class MsgBrokerURLRegisterThread extends AbstractRegistryUpdaterThread {

        private ConfigurationContext context = null;

        public MsgBrokerURLRegisterThread(AiravataRegistry2 registry, ConfigurationContext context) {
            super(registry);
            this.context = context;
        }


        protected void updateRegistry(AiravataRegistry2 registry) {
            URI localAddress = (URI) this.context.getProperty(SERVICE_URL);
            registry.setEventingURI(localAddress);
            log.info("Updated Workflow Interpreter service URL in to Repository");
        }

    }
}
