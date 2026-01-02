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
package org.apache.airavata.thriftapi.server;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.registry.messaging.RegistryServiceDBEventMessagingFactory;
import org.apache.airavata.registry.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.thriftapi.handler.RegistryServiceHandler;
import org.apache.airavata.thriftapi.registry.model.RegistryService;
import org.apache.airavata.thriftapi.util.RegistryServiceConstants;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "services.thrift.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class RegistryServiceServer extends ServerLifecycle {
    private static final String SERVER_NAME = "Registry API Server";
    private static final String SERVER_VERSION = "1.0";

    private TServer server;

    private final AiravataServerProperties properties;
    private final ExpCatalogDBInitConfig expCatalogDBInitConfig;
    private final AppCatalogDBInitConfig appCatalogDBInitConfig;
    private final ReplicaCatalogDBInitConfig replicaCatalogDBInitConfig;
    private final RegistryServiceDBEventMessagingFactory messagingFactory;
    private final RegistryServiceHandler handler;

    private List<DBInitConfig> dbInitConfigs;

    public RegistryServiceServer(
            AiravataServerProperties properties,
            ExpCatalogDBInitConfig expCatalogDBInitConfig,
            AppCatalogDBInitConfig appCatalogDBInitConfig,
            ReplicaCatalogDBInitConfig replicaCatalogDBInitConfig,
            RegistryServiceDBEventMessagingFactory messagingFactory,
            RegistryServiceHandler handler) {
        this.properties = properties;
        this.expCatalogDBInitConfig = expCatalogDBInitConfig;
        this.appCatalogDBInitConfig = appCatalogDBInitConfig;
        this.replicaCatalogDBInitConfig = replicaCatalogDBInitConfig;
        this.messagingFactory = messagingFactory;
        this.handler = handler;
    }

    @Override
    public String getServerName() {
        return SERVER_NAME;
    }

    @Override
    public String getServerVersion() {
        return SERVER_VERSION;
    }

    @Override
    public int getPhase() {
        // Registry Server starts after DB Event Manager
        return 20;
    }

    @Override
    public boolean isRunning() {
        return server != null && server.isServing();
    }

    @PostConstruct
    public void init() {
        dbInitConfigs = Arrays.asList(expCatalogDBInitConfig, appCatalogDBInitConfig, replicaCatalogDBInitConfig);
    }

    public void StartRegistryServer(
            RegistryService.Processor<RegistryServiceHandler> orchestratorServerHandlerProcessor) throws Exception {

        // Database migrations are handled automatically by Flyway on application startup
        // See FlywayConfig for migration configuration

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
            logger.error("Failed to start Registry server on port " + serverPort + " ...");
            throw e;
        }
    }

    private boolean startDatabaseEventHandlers() {
        try {
            // db-event handlers
            logger.info("Registring registry service with publishers for db-events.");
            messagingFactory.registerRegistryServiceWithPublishers(RegistryServiceConstants.DB_EVENT_SUBSCRIBERS);

            logger.info("Starting registry service db-event-handler subscriber.");
            messagingFactory.getDBEventSubscriber();
        } catch (Exception ex) {
            logger.error("Failed to start database event handlers, reason: " + ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        RegistryService.Processor<RegistryServiceHandler> orchestratorService =
                new RegistryService.Processor<RegistryServiceHandler>(handler);
        StartRegistryServer(orchestratorService);
    }

    @Override
    protected void doStop() throws Exception {
        if (server != null && server.isServing()) {
            server.stop();
        }
    }
}
