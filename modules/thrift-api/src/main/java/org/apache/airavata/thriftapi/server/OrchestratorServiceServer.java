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

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.config.ServerLifecycle;
import org.apache.airavata.thriftapi.handler.OrchestratorServiceHandler;
import org.apache.airavata.thriftapi.orchestrator.model.OrchestratorService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "services.thrift.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class OrchestratorServiceServer extends ServerLifecycle {

    private static final String SERVER_NAME = "Orchestrator Server";
    private static final String SERVER_VERSION = "1.0";
    private TServer server;

    private final ApplicationContext applicationContext;
    private final AiravataServerProperties properties;

    // private ClusterStatusMonitorJobScheduler clusterStatusMonitorJobScheduler;

    public OrchestratorServiceServer(ApplicationContext applicationContext, AiravataServerProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
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
        // Orchestrator starts after API Server
        return 60;
    }

    @Override
    public boolean isRunning() {
        return server != null && server.isServing();
    }

    public void StartOrchestratorServer(
            OrchestratorService.Processor<OrchestratorServiceHandler> orchestratorServerHandlerProcessor)
            throws Exception {
        final int serverPort = properties.services.orchestrator.serverPort;
        try {
            TServerTransport serverTransport = new TServerSocket(serverPort);
            // server = new TSimpleServer(
            // new
            // TServer.Args(serverTransport).processor(orchestratorServerHandlerProcessor));
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = properties.services.orchestrator.serverMinThreads;
            server = new TThreadPoolServer(options.processor(orchestratorServerHandlerProcessor));
            new Thread() {
                public void run() {
                    server.serve();
                    logger.info("Starting Orchestrator Server ... ");
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
                        logger.info("Started Orchestrator Server on Port " + serverPort + " ...");
                    }
                }
            }.start();
        } catch (TTransportException e) {
            logger.error(e.getMessage());
            logger.error("Failed to start Orchestrator server on port " + serverPort + " ...");
            throw e;
        }
    }

    @Override
    protected void doStart() throws Exception {
        // Note: monitoringService, metaschedulerService, and dataInterpreterService
        // are now SmartLifecycle components and will be started automatically by Spring
        // based on their phase values. We only need to start the orchestrator server
        // itself.
        OrchestratorServiceHandler handler = applicationContext.getBean(OrchestratorServiceHandler.class);
        OrchestratorService.Processor<OrchestratorServiceHandler> orchestratorService =
                new OrchestratorService.Processor<OrchestratorServiceHandler>(handler);
        StartOrchestratorServer(orchestratorService);
    }

    @Override
    protected void doStop() throws Exception {
        if (server != null && server.isServing()) {
            server.stop();
        }
        // Note: monitoringService, metaschedulerService, and dataInterpreterService
        // will be stopped automatically by Spring's lifecycle management
    }
}
