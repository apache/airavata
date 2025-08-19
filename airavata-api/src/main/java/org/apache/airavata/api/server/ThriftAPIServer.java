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
package org.apache.airavata.api.server;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.server.OrchestratorServerHandler;
import org.apache.airavata.profile.handlers.GroupManagerServiceHandler;
import org.apache.airavata.profile.handlers.IamAdminServicesHandler;
import org.apache.airavata.profile.handlers.TenantProfileServiceHandler;
import org.apache.airavata.profile.handlers.UserProfileServiceHandler;
import org.apache.airavata.profile.user.core.utils.UserProfileCatalogDBInitConfig;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Airavata Thrift API Server that combines all Airavata services into a single server
 * using the orchestrator's host and port configuration.
 */
public class ThriftAPIServer implements IServer {
    private static final Logger logger = LoggerFactory.getLogger(ThriftAPIServer.class);
    private static final String SERVER_NAME = "Airavata Thrift API";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;
    private TServer server;

    // Service names for multiplexing
    public static final String AIRAVATA_API_SERVICE = "AiravataAPI";
    public static final String REGISTRY_SERVICE = "RegistryAPI";
    public static final String CREDENTIAL_STORE_SERVICE = "CredentialStore";
    public static final String SHARING_REGISTRY_SERVICE = "SharingRegistry";
    public static final String ORCHESTRATOR_SERVICE = "Orchestrator";
    public static final String USER_PROFILE_SERVICE = "UserProfile";
    public static final String TENANT_PROFILE_SERVICE = "TenantProfile";
    public static final String IAM_ADMIN_SERVICES = "IamAdminServices";
    public static final String GROUP_MANAGER_SERVICE = "GroupManager";

    public ThriftAPIServer() {
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
            );
            
            for (DBInitConfig dbInitConfig : dbInitConfigs) {
                DBInitializer.initializeDB(dbInitConfig);
            }
            logger.info("Databases initialized successfully");

            // Use orchestrator's host and port configuration
            final int serverPort = Integer.parseInt(ServerSettings.getSetting("orchestrator.server.port", "8940"));
            final String serverHost = ServerSettings.getSetting("orchestrator.server.host", "localhost");

            // Create processors for each service
            var airavataAPIProcessor = new Airavata.Processor<>(new AiravataServerHandler());
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
            multiplexedProcessor.registerProcessor(AIRAVATA_API_SERVICE, airavataAPIProcessor);
            multiplexedProcessor.registerProcessor(REGISTRY_SERVICE, registryProcessor);
            multiplexedProcessor.registerProcessor(CREDENTIAL_STORE_SERVICE, credentialStoreProcessor);
            multiplexedProcessor.registerProcessor(SHARING_REGISTRY_SERVICE, sharingRegistryProcessor);
            multiplexedProcessor.registerProcessor(ORCHESTRATOR_SERVICE, orchestratorProcessor);
            multiplexedProcessor.registerProcessor(USER_PROFILE_SERVICE, userProfileProcessor);
            multiplexedProcessor.registerProcessor(TENANT_PROFILE_SERVICE, tenantProfileProcessor);
            multiplexedProcessor.registerProcessor(IAM_ADMIN_SERVICES, iamAdminServicesProcessor);
            multiplexedProcessor.registerProcessor(GROUP_MANAGER_SERVICE, groupManagerProcessor);

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
            options.minWorkerThreads = Integer.parseInt(ServerSettings.getSetting("orchestrator.server.min.threads", "30"));
            server = new TThreadPoolServer(options.processor(multiplexedProcessor));

            // Start server in background thread
            new Thread(() -> {
                server.serve();
                setStatus(ServerStatus.STOPPED);
                logger.info("Airavata Thrift API Stopped.");
            }).start();

            // Monitor server startup
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
                    logger.info("Started Airavata Thrift API on {}:{}", serverHost, serverPort);
                    logger.info("Registered services: {}, {}, {}, {}, {}, {}, {}, {}, {}", 
                        AIRAVATA_API_SERVICE, REGISTRY_SERVICE, CREDENTIAL_STORE_SERVICE, 
                        SHARING_REGISTRY_SERVICE, ORCHESTRATOR_SERVICE, USER_PROFILE_SERVICE,
                        TENANT_PROFILE_SERVICE, IAM_ADMIN_SERVICES, GROUP_MANAGER_SERVICE);
                }
            }).start();

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
