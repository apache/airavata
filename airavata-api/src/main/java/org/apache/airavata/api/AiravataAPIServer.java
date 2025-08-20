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
package org.apache.airavata.api;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.apache.airavata.api.handler.AiravataAPIHandler;
import org.apache.airavata.catalog.sharing.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.catalog.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.catalog.sharing.service.cpi.SharingRegistryService;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.handler.CredentialStoreServerHandler;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.handler.OrchestratorServerHandler;
import org.apache.airavata.profile.handlers.GroupManagerServiceHandler;
import org.apache.airavata.profile.handlers.IamAdminServicesHandler;
import org.apache.airavata.profile.handlers.TenantProfileServiceHandler;
import org.apache.airavata.profile.handlers.UserProfileServiceHandler;
import org.apache.airavata.profile.user.core.utils.UserProfileCatalogDBInitConfig;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.registry.handler.RegistryServerHandler;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Airavata API multiplexes several services over a single Thrift server
 * - Airavata Service
 * - Registry Service
 * - Credential Store Service
 * - Sharing Registry Service
 * - Orchestrator Service
 * - User Profile Service
 * - Tenant Profile Service
 * - IAM Admin Service
 * - Group Manager Service
 */
public class AiravataAPIServer implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(AiravataAPIServer.class);
    private static final String SERVER_NAME = "Airavata Thrift API";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;
    private TServer server;

    public AiravataAPIServer() {
        setStatus(ServerStatus.STOPPED);
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    @Override
    public void start() throws Exception {
        try {
            setStatus(ServerStatus.STARTING);

            // Initialize databases
            logger.info("Initializing databases...");
            List<DBInitConfig> dbInitConfigs = Arrays.asList(
                    new ExpCatalogDBInitConfig(),
                    new AppCatalogDBInitConfig(),
                    new ReplicaCatalogDBInitConfig(),
                    new UserProfileCatalogDBInitConfig(),
                    new SharingRegistryDBInitConfig()
                    // new WorkflowCatalogDBInitConfig()
                    );

            for (DBInitConfig dbInitConfig : dbInitConfigs) {
                DBInitializer.initializeDB(dbInitConfig);
            }
            logger.info("Databases initialized successfully");

            // Use orchestrator's host and port configuration
            final int serverPort = Integer.parseInt(ServerSettings.getSetting("api.server.port", "8930"));
            final String serverHost = ServerSettings.getSetting("api.server.host", "localhost");

            // Create processors for each service
            var airavataAPIProcessor = new Airavata.Processor<>(new AiravataAPIHandler());
            var registryProcessor = new RegistryService.Processor<>(new RegistryServerHandler());
            var credentialStoreProcessor = new CredentialStoreService.Processor<>(new CredentialStoreServerHandler());
            var sharingRegistryProcessor = new SharingRegistryService.Processor<>(new SharingRegistryServerHandler());
            var orchestratorProcessor = new OrchestratorService.Processor<>(new OrchestratorServerHandler());
            var userProfileProcessor = new UserProfileService.Processor<>(new UserProfileServiceHandler());
            var tenantProfileProcessor = new TenantProfileService.Processor<>(new TenantProfileServiceHandler());
            var iamAdminServicesProcessor = new IamAdminServices.Processor<>(new IamAdminServicesHandler());
            var groupManagerProcessor = new GroupManagerService.Processor<>(new GroupManagerServiceHandler());

            // Create multiplexed processor
            TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();

            // Register all processors with their service names
            multiplexedProcessor.registerProcessor(ServiceName.AIRAVATA_API.toString(), airavataAPIProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.REGISTRY.toString(), registryProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.CREDENTIAL_STORE.toString(), credentialStoreProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.SHARING_REGISTRY.toString(), sharingRegistryProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.ORCHESTRATOR.toString(), orchestratorProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.USER_PROFILE.toString(), userProfileProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.TENANT_PROFILE.toString(), tenantProfileProcessor);
            multiplexedProcessor.registerProcessor(
                    ServiceName.IAM_ADMIN_SERVICES.toString(), iamAdminServicesProcessor);
            multiplexedProcessor.registerProcessor(ServiceName.GROUP_MANAGER.toString(), groupManagerProcessor);

            // Create server transport
            TServerTransport serverTransport;
            if (serverHost == null) {
                serverTransport = new TServerSocket(serverPort);
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(serverHost, serverPort);
                serverTransport = new TServerSocket(inetSocketAddress);
            }

            // Create and start server
            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting("api.server.min.threads", "30"));
            server = new TThreadPoolServer(options.processor(multiplexedProcessor));

            // Start server in background thread
            new Thread(
                            () -> {
                                server.serve();
                                setStatus(ServerStatus.STOPPED);
                                logger.info("Airavata Thrift API Stopped.");
                            },
                            this.getClass().getSimpleName())
                    .start();

            // Monitor server startup
            new Thread(
                            () -> {
                                while (!server.isServing()) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }
                                if (server.isServing()) {
                                    setStatus(ServerStatus.STARTED);
                                    logger.info("Started Airavata Thrift API on {}:{}", serverHost, serverPort);
                                    logger.info(
                                            "Registered services: {}, {}, {}, {}, {}, {}, {}, {}, {}",
                                            ServiceName.AIRAVATA_API.toString(),
                                            ServiceName.REGISTRY.toString(),
                                            ServiceName.CREDENTIAL_STORE.toString(),
                                            ServiceName.SHARING_REGISTRY.toString(),
                                            ServiceName.ORCHESTRATOR.toString(),
                                            ServiceName.USER_PROFILE.toString(),
                                            ServiceName.TENANT_PROFILE.toString(),
                                            ServiceName.IAM_ADMIN_SERVICES.toString(),
                                            ServiceName.GROUP_MANAGER.toString());
                                }
                            },
                            this.getClass().getSimpleName() + ".Monitor")
                    .start();

        } catch (TTransportException e) {
            logger.error("Failed to start Airavata Thrift API", e);
            setStatus(ServerStatus.FAILED);
            throw new Exception("Error while starting the Airavata Thrift API", e);
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
        // Configuration handled in start method
    }

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    private void setStatus(ServerStatus stat) {
        status = stat;
        status.updateTime();
    }
}
