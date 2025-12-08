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
package org.apache.airavata.api.thrift.server;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.api.thrift.handler.RegistryServiceHandler;
import org.apache.airavata.api.thrift.util.RegistryServiceConstants;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.messaging.RegistryServiceDBEventMessagingFactory;
import org.apache.airavata.registry.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.utils.ReplicaCatalogDBInitConfig;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RegistryServiceServer implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceServer.class);

    private static final String SERVER_NAME = "Registry API Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;

    private TServer server;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    @Autowired
    private ExpCatalogDBInitConfig expCatalogDBInitConfig;

    @Autowired
    private AppCatalogDBInitConfig appCatalogDBInitConfig;

    @Autowired
    private ReplicaCatalogDBInitConfig replicaCatalogDBInitConfig;

    private List<DBInitConfig> dbInitConfigs;

    public RegistryServiceServer() {
        setStatus(ServerStatus.STOPPED);
    }

    @PostConstruct
    public void init() {
        dbInitConfigs = Arrays.asList(expCatalogDBInitConfig, appCatalogDBInitConfig, replicaCatalogDBInitConfig);
    }

    public void StartRegistryServer(
            RegistryService.Processor<RegistryServiceHandler> orchestratorServerHandlerProcessor) throws Exception {

        logger.info("Initializing databases...");
        for (DBInitConfig dbInitConfig : dbInitConfigs) {
            DBInitializer.initializeDB(dbInitConfig);
        }
        logger.info("Databases initialized successfully");

        final int serverPort = properties.services.registry.server.port;
        try {
            TServerTransport serverTransport = new TServerSocket(serverPort);

            // thrift server start
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = properties.services.registry.server.minThreads;
            server = new TThreadPoolServer(options.processor(orchestratorServerHandlerProcessor));
            new Thread() {
                public void run() {
                    server.serve();
                    setStatus(ServerStatus.STOPPED);
                    logger.info("Registry Server Stopped.");
                }
            }.start();
            new Thread() {
                public void run() {
                    while (!server.isServing()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (server.isServing()) {
                        setStatus(ServerStatus.STARTED);
                        logger.info("Started Registry Server on Port " + serverPort + " ...");

                        // start db event handlers
                        if (!startDatabaseEventHandlers()) {
                            logger.error("Stopping Registry Server as DB event handlers failed to start!");
                            server.stop();
                        }
                    }
                }
            }.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            setStatus(ServerStatus.FAILED);
            logger.error("Failed to start Registry server on port " + serverPort + " ...");
        }
    }

    private boolean startDatabaseEventHandlers() {
        try {
            // db-event handlers
            logger.info("Registring registry service with publishers for db-events.");
            RegistryServiceDBEventMessagingFactory.registerRegistryServiceWithPublishers(
                    RegistryServiceConstants.DB_EVENT_SUBSCRIBERS);

            logger.info("Starting registry service db-event-handler subscriber.");
            RegistryServiceDBEventMessagingFactory.getDBEventSubscriber();
        } catch (Exception ex) {
            logger.error("Failed to start database event handlers, reason: " + ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    @Override
    public void start() throws Exception {
        setStatus(ServerStatus.STARTING);
        RegistryServiceHandler handler = applicationContext.getBean(RegistryServiceHandler.class);
        RegistryService.Processor<RegistryServiceHandler> orchestratorService =
                new RegistryService.Processor<RegistryServiceHandler>(handler);
        StartRegistryServer(orchestratorService);
    }

    @Override
    public void stop() throws Exception {
        if (server != null && server.isServing()) {
            setStatus(ServerStatus.STOPING);
            server.stop();
        }
    }

    @Override
    public void restart() throws Exception {
        stop();
        start();
    }

    @Override
    public void configure() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(ServerStatus stat) {
        status = stat;
        status.updateTime();
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }
}
