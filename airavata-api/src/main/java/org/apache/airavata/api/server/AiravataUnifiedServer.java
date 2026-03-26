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
import org.apache.airavata.api.server.util.Constants;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.DBInitializer;
import org.apache.airavata.common.utils.IServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.server.OrchestratorServerHandler;
import org.apache.airavata.credential.store.server.CredentialStoreServerHandler;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.credential.store.store.impl.util.CredentialStoreDBInitConfig;
import org.apache.airavata.registry.core.utils.AppCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogDBInitConfig;
import org.apache.airavata.registry.core.utils.WorkflowCatalogDBInitConfig;
import org.apache.airavata.service.profile.groupmanager.cpi.GroupManagerService;
import org.apache.airavata.service.profile.groupmanager.cpi.group_manager_cpiConstants;
import org.apache.airavata.service.profile.handlers.GroupManagerServiceHandler;
import org.apache.airavata.service.profile.handlers.IamAdminServicesHandler;
import org.apache.airavata.service.profile.handlers.TenantProfileServiceHandler;
import org.apache.airavata.service.profile.handlers.UserProfileServiceHandler;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.iam_admin_services_cpiConstants;
import org.apache.airavata.service.profile.tenant.cpi.TenantProfileService;
import org.apache.airavata.service.profile.tenant.cpi.profile_tenant_cpiConstants;
import org.apache.airavata.service.profile.user.core.utils.UserProfileCatalogDBInitConfig;
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.sharing.registry.db.utils.SharingRegistryDBInitConfig;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated Thrift server that hosts all Airavata services on a single multiplexed port.
 *
 * <p>Services registered on the TMultiplexedProcessor:
 * <ul>
 *   <li>"Airavata" → AiravataServerHandler (main API)</li>
 *   <li>"RegistryService" → RegistryServerHandler</li>
 *   <li>"SharingRegistry" → SharingRegistryServerHandler</li>
 *   <li>"CredentialStore" → CredentialStoreServerHandler</li>
 *   <li>Profile services (UserProfile, TenantProfile, IamAdminServices, GroupManager)</li>
 * </ul>
 *
 * <p>All services share the same port (default 8930, from {@code apiserver.port}).
 */
public class AiravataUnifiedServer implements IServer {

    private static final Logger logger = LoggerFactory.getLogger(AiravataUnifiedServer.class);
    private static final String SERVER_NAME = "Airavata Unified Server";
    private static final String SERVER_VERSION = "1.0";

    private ServerStatus status;
    private TServer server;

    private final List<DBInitConfig> dbInitConfigs = Arrays.asList(
            new ExpCatalogDBInitConfig(),
            new AppCatalogDBInitConfig(),
            new ReplicaCatalogDBInitConfig(),
            new WorkflowCatalogDBInitConfig(),
            new CredentialStoreDBInitConfig(),
            new UserProfileCatalogDBInitConfig());

    public AiravataUnifiedServer() {
        setStatus(ServerStatus.STOPPED);
    }

    @Override
    public void start() throws Exception {
        setStatus(ServerStatus.STARTING);

        logger.info("Initializing databases...");
        for (DBInitConfig dbInitConfig : dbInitConfigs) {
            DBInitializer.initializeDB(dbInitConfig);
        }
        logger.info("Databases initialized successfully");

        try {
            final String serverHost = ServerSettings.getSetting(Constants.API_SERVER_HOST, null);
            final int serverPort =
                    Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_PORT, "8930"));

            // --- Instantiate handlers ---
            CredentialStoreServerHandler credentialStoreServerHandler = new CredentialStoreServerHandler();
            RegistryServerHandler registryServerHandler = new RegistryServerHandler();
            SharingRegistryServerHandler sharingRegistryServerHandler =
                    new SharingRegistryServerHandler(new SharingRegistryDBInitConfig());

            // AiravataServerHandler constructor accepting in-process handler refs is added in Task 3.
            // Until then this file will not compile.
            AiravataServerHandler airavataServerHandler = new AiravataServerHandler(
                    registryServerHandler, sharingRegistryServerHandler, credentialStoreServerHandler);

            // --- Build processors ---
            TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();

            multiplexedProcessor.registerProcessor(
                    "Airavata", new Airavata.Processor<>(airavataServerHandler));

            multiplexedProcessor.registerProcessor(
                    "RegistryService", new RegistryService.Processor<>(registryServerHandler));

            multiplexedProcessor.registerProcessor(
                    "SharingRegistry", new SharingRegistryService.Processor<>(sharingRegistryServerHandler));

            multiplexedProcessor.registerProcessor(
                    "CredentialStore", new CredentialStoreService.Processor<>(credentialStoreServerHandler));

            // Profile services (multiplexed on the same port as the existing ProfileServiceServer)
            multiplexedProcessor.registerProcessor(
                    profile_user_cpiConstants.USER_PROFILE_CPI_NAME,
                    new UserProfileService.Processor<>(new UserProfileServiceHandler()));

            multiplexedProcessor.registerProcessor(
                    profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME,
                    new TenantProfileService.Processor<>(new TenantProfileServiceHandler()));

            multiplexedProcessor.registerProcessor(
                    iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME,
                    new IamAdminServices.Processor<>(new IamAdminServicesHandler()));

            multiplexedProcessor.registerProcessor(
                    group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME,
                    new GroupManagerService.Processor<>(new GroupManagerServiceHandler()));

            // Orchestrator service
            try {
                OrchestratorServerHandler orchestratorHandler = new OrchestratorServerHandler();
                multiplexedProcessor.registerProcessor(
                        "Orchestrator", new OrchestratorService.Processor<>(orchestratorHandler));
                logger.info("Orchestrator service registered");
            } catch (Exception e) {
                logger.warn("Orchestrator service failed to initialize (ZooKeeper/Helix may not be available): {}",
                        e.getMessage());
            }

            // --- Transport and server ---
            TServerTransport serverTransport;
            if (serverHost == null) {
                serverTransport = new TServerSocket(serverPort);
            } else {
                serverTransport = new TServerSocket(new InetSocketAddress(serverHost, serverPort));
            }

            TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
            options.minWorkerThreads =
                    Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_MIN_THREADS, "50"));
            server = new TThreadPoolServer(options.processor(multiplexedProcessor));

            new Thread(() -> {
                        server.serve();
                        setStatus(ServerStatus.STOPPED);
                        logger.info("Airavata Unified Server stopped.");
                    })
                    .start();

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
                            logger.info("Airavata Unified Server started on port {}", serverPort);
                            logger.info("Registered services: Airavata, RegistryService, SharingRegistry, "
                                    + "CredentialStore, UserProfile, TenantProfile, IamAdminServices, GroupManager");
                        }
                    })
                    .start();

        } catch (TTransportException | ApplicationSettingsException e) {
            logger.error("Failed to start Airavata Unified Server", e);
            setStatus(ServerStatus.FAILED);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
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
    public void configure() throws Exception {}

    @Override
    public ServerStatus getStatus() throws Exception {
        return status;
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

    @Override
    public String getVersion() {
        return SERVER_VERSION;
    }

    private void setStatus(ServerStatus stat) {
        status = stat;
        status.updateTime();
    }

    public static void main(String[] args) {
        try {
            new AiravataUnifiedServer().start();
        } catch (Exception e) {
            logger.error("Error starting Airavata Unified Server", e);
        }
    }
}
