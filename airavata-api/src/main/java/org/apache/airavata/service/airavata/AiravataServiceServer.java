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
package org.apache.airavata.service.airavata;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.server.OrchestratorServerHandler;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.WorkflowCatalogDBInitConfig;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.handlers.*;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.user.core.utils.UserProfileCatalogDBInitConfig;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
// import org.apache.airavata.api.workflow.Workflow;
// import org.apache.airavata.api.workflow.WorkflowHandler;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AiravataServiceServer combines all Airavata services into a single multiplexed server.
 * This server hosts all 10 services on a single port using TMultiplexedProcessor.
 */
public class AiravataServiceServer implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServiceServer.class);

    private static final String SERVER_NAME = "Airavata Service Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;
    private TServer server;

    // Database initialization configs for all services
    private List<DBInitConfig> dbInitConfigs = Arrays.asList(
            new ExpCatalogDBInitConfig(),
            new AppCatalogDBInitConfig(),
            new ReplicaCatalogDBInitConfig(),
            new WorkflowCatalogDBInitConfig(),
            new UserProfileCatalogDBInitConfig(),
            new SharingRegistryDBInitConfig());

    public AiravataServiceServer() {
        setStatus(ServerStatus.STOPPED);
    }

    @Override
    public void start() throws Exception {
        try {
            setStatus(ServerStatus.STARTING);

            logger.info("Initializing Airavata service databases...");
            for (DBInitConfig dbInitConfig : dbInitConfigs) {
                DBInitializer.initializeDB(dbInitConfig);
            }
            logger.info("Airavata service databases initialized successfully");

            final int serverPort = Integer.parseInt(ServerSettings.getSetting("airavata.service.port", "9930"));
            final String serverHost = ServerSettings.getSetting("airavata.service.host", "0.0.0.0");

            // Create handler instances in dependency order
            var registryHandler = new RegistryServerHandler();
            var credentialStoreHandler = new CredentialStoreServerHandler();
            var sharingHandler = new SharingRegistryServerHandler();
            var orchestratorHandler = new OrchestratorServerHandler(registryHandler);
            var userProfileHandler = new UserProfileServiceHandler();
            var tenantProfileHandler = new TenantProfileServiceHandler(credentialStoreHandler);
            var iamAdminHandler = new IamAdminServicesHandler(registryHandler, credentialStoreHandler);
            var groupManagerHandler = new GroupManagerServiceHandler(sharingHandler);

            // Create AiravataServerHandler with injected dependencies
            var airavataHandler = new AiravataServerHandler(registryHandler, sharingHandler, credentialStoreHandler);

            // Create all service processors
            var airavataProcessor = new Airavata.Processor<>(airavataHandler);
            var registryProcessor = new RegistryService.Processor<>(registryHandler);
            var credentialStoreProcessor = new CredentialStoreService.Processor<>(credentialStoreHandler);
            var sharingRegistryProcessor = new SharingRegistryService.Processor<>(sharingHandler);
            var orchestratorProcessor = new OrchestratorService.Processor<>(orchestratorHandler);
            // TODO: Uncomment when WorkflowModel thrift stubs are generated
            // var workflowProcessor = new Workflow.Processor<>(new WorkflowHandler());
            var userProfileProcessor = new UserProfileService.Processor<>(userProfileHandler);
            var tenantProfileProcessor = new TenantProfileService.Processor<>(tenantProfileHandler);
            var iamAdminServicesProcessor = new IamAdminServices.Processor<>(iamAdminHandler);
            var groupManagerProcessor = new GroupManagerService.Processor<>(groupManagerHandler);

            // Create multiplexed processor
            TMultiplexedProcessor airavataServiceProcessor = new TMultiplexedProcessor();

            // Register all processors with their service names
            airavataServiceProcessor.registerProcessor("Airavata", airavataProcessor);
            airavataServiceProcessor.registerProcessor("RegistryService", registryProcessor);
            airavataServiceProcessor.registerProcessor("CredentialStoreService", credentialStoreProcessor);
            airavataServiceProcessor.registerProcessor("SharingRegistryService", sharingRegistryProcessor);
            airavataServiceProcessor.registerProcessor("OrchestratorService", orchestratorProcessor);
            // TODO: Uncomment when WorkflowModel thrift stubs are generated
            // airavataServiceProcessor.registerProcessor("Workflow", workflowProcessor);
            airavataServiceProcessor.registerProcessor("UserProfileService", userProfileProcessor);
            airavataServiceProcessor.registerProcessor("TenantProfileService", tenantProfileProcessor);
            airavataServiceProcessor.registerProcessor("IamAdminServices", iamAdminServicesProcessor);
            airavataServiceProcessor.registerProcessor("GroupManagerService", groupManagerProcessor);

            // Create server transport
            TServerTransport serverTransport;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
            serverTransport = new TServerSocket(inetSocketAddress);

            // Configure server
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads =
                    Integer.parseInt(ServerSettings.getSetting("airavata.service.min.threads", "30"));
            options.maxWorkerThreads =
                    Integer.parseInt(ServerSettings.getSetting("airavata.service.max.threads", "100"));
            server = new TThreadPoolServer(options.processor(airavataServiceProcessor));

            // Start server in background thread
            new Thread(() -> {
                        try {
                            server.serve();
                            setStatus(ServerStatus.STOPPED);
                            logger.info("Airavata Service Server Stopped.");
                        } catch (Exception e) {
                            logger.error("Error in Airavata Service Server", e);
                            setStatus(ServerStatus.FAILED);
                        }
                    })
                    .start();

            // Wait for server to start
            new Thread(() -> {
                        while (!server.isServing()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (server.isServing()) {
                            setStatus(ServerStatus.STARTED);
                            logger.info(
                                    "Started Airavata Service Server on {}:{} with 9 services (Workflow pending thrift generation)",
                                    serverHost,
                                    serverPort);
                        } else {
                            setStatus(ServerStatus.FAILED);
                            logger.error("Failed to start Airavata Service Server");
                        }
                    })
                    .start();

        } catch (Exception e) {
            logger.error("Failed to start Airavata Service Server", e);
            setStatus(ServerStatus.FAILED);
            throw e;
        }
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
        // Configuration is handled in start()
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

    public static void main(String[] args) {
        try {
            AiravataServiceServer server = new AiravataServiceServer();
            server.start();

            // Keep the main thread alive
            while (server.getStatus() == ServerStatus.STARTED) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Error while running Airavata Service Server", e);
        }
    }
}
