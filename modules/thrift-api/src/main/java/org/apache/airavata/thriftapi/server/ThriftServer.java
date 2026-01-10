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
import org.apache.airavata.thriftapi.credential.model.CredentialStoreService;
import org.apache.airavata.thriftapi.handler.AiravataServiceHandler;
import org.apache.airavata.thriftapi.handler.CredentialServiceHandler;
import org.apache.airavata.thriftapi.handler.GroupManagerServiceHandler;
import org.apache.airavata.thriftapi.handler.IamAdminServiceHandler;
import org.apache.airavata.thriftapi.handler.OrchestratorServiceHandler;
import org.apache.airavata.thriftapi.handler.RegistryServiceHandler;
import org.apache.airavata.thriftapi.handler.SharingRegistryServerHandler;
import org.apache.airavata.thriftapi.handler.TenantProfileServiceHandler;
import org.apache.airavata.thriftapi.handler.UserProfileServiceHandler;
import org.apache.airavata.thriftapi.orchestrator.model.OrchestratorService;
import org.apache.airavata.thriftapi.profile.model.GroupManagerService;
import org.apache.airavata.thriftapi.profile.model.IamAdminServices;
import org.apache.airavata.thriftapi.profile.model.TenantProfileService;
import org.apache.airavata.thriftapi.profile.model.UserProfileService;
import org.apache.airavata.thriftapi.profile.model.group_manager_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.iam_admin_services_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.profile_tenant_cpiConstants;
import org.apache.airavata.thriftapi.profile.model.profile_user_cpiConstants;
import org.apache.airavata.thriftapi.registry.model.RegistryService;
import org.apache.airavata.thriftapi.service.Airavata;
import org.apache.airavata.thriftapi.sharing.model.SharingRegistryService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "services.thrift.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ThriftServer extends ServerLifecycle {

    private static final String SERVER_NAME = "Thrift Server";
    private static final String SERVER_VERSION = "1.0";

    // Service name constants for TMultiplexedProcessor
    private static final String AIRAVATA_SERVICE_NAME = "Airavata";
    private static final String PROFILE_SERVICE_NAME = "ProfileService";
    private static final String ORCHESTRATOR_SERVICE_NAME = "OrchestratorService";
    private static final String REGISTRY_SERVICE_NAME = "RegistryService";
    private static final String CREDENTIAL_STORE_SERVICE_NAME = "CredentialStoreService";
    private static final String SHARING_REGISTRY_SERVICE_NAME = "SharingRegistryService";

    private TServer server, TLSServer;
    private Thread serverThread;
    private Thread serverMonitorThread;
    private Thread tlsServerThread;
    private Thread tlsServerMonitorThread;

    private final AiravataServerProperties properties;
    private final AiravataServiceHandler airavataHandler;
    private final OrchestratorServiceHandler orchestratorHandler;
    private final RegistryServiceHandler registryHandler;
    private final CredentialServiceHandler credentialHandler;
    private final SharingRegistryServerHandler sharingHandler;
    private final UserProfileServiceHandler userProfileHandler;
    private final TenantProfileServiceHandler tenantProfileHandler;
    private final IamAdminServiceHandler iamAdminHandler;
    private final GroupManagerServiceHandler groupManagerHandler;

    public ThriftServer(
            AiravataServerProperties properties,
            AiravataServiceHandler airavataHandler,
            OrchestratorServiceHandler orchestratorHandler,
            RegistryServiceHandler registryHandler,
            CredentialServiceHandler credentialHandler,
            SharingRegistryServerHandler sharingHandler,
            UserProfileServiceHandler userProfileHandler,
            TenantProfileServiceHandler tenantProfileHandler,
            IamAdminServiceHandler iamAdminHandler,
            GroupManagerServiceHandler groupManagerHandler) {
        this.properties = properties;
        this.airavataHandler = airavataHandler;
        this.orchestratorHandler = orchestratorHandler;
        this.registryHandler = registryHandler;
        this.credentialHandler = credentialHandler;
        this.sharingHandler = sharingHandler;
        this.userProfileHandler = userProfileHandler;
        this.tenantProfileHandler = tenantProfileHandler;
        this.iamAdminHandler = iamAdminHandler;
        this.groupManagerHandler = groupManagerHandler;
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
        // API Server starts after Registry, Credential, and Sharing
        return 50;
    }

    @Override
    public boolean isRunning() {
        if (!properties.security().tls().enabled()) {
            return super.isRunning() && server != null && server.isServing();
        } else {
            return super.isRunning() && TLSServer != null && TLSServer.isServing();
        }
    }

    public void startThriftServer(TMultiplexedProcessor multiplexedProcessor)
            throws org.apache.airavata.thriftapi.exception.AiravataSystemException {
        try {
            final int serverPort = properties.services().thrift().server().port();

            if (!properties.security().tls().enabled()) {
                TServerTransport serverTransport = new TServerSocket(serverPort);
                TThreadPoolServer.Args options = new TThreadPoolServer.Args(serverTransport);
                server = new TThreadPoolServer(options.processor(multiplexedProcessor));
                serverThread = new Thread(() -> {
                    server.serve();
                    logger.info("Thrift Server Stopped.");
                });
                serverThread.setName(getServerName() + "-Server");
                serverThread.setDaemon(true);
                serverThread.start();

                serverMonitorThread = new Thread(() -> {
                    while (!server.isServing()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (server.isServing()) {
                        logger.info("Starting Thrift Server on Port " + serverPort);
                        logger.info("Listening to Thrift Clients ....");
                    }
                });
                serverMonitorThread.setName(getServerName() + "-Monitor");
                serverMonitorThread.setDaemon(true);
                serverMonitorThread.start();
                logger.info("Started API Server ....");
            } else {
                var TLSParams = new TSSLTransportFactory.TSSLTransportParameters();
                String configDir =
                        org.apache.airavata.config.AiravataConfigUtils.getConfigDir(); // Will throw if not found
                if (properties.security() == null
                        || properties.security().tls() == null
                        || properties.security().tls().keystore() == null
                        || properties.security().tls().keystore().path() == null) {
                    throw new IllegalStateException(
                            "TLS keystore configuration is missing: security.tls.keystore.path is not set in airavata.properties");
                }
                // Keystore path is relative to configDir (e.g., "keystores/airavata.p12")
                java.io.File keystoreFile = new java.io.File(configDir, properties.security().tls().keystore().path());
                TLSParams.setKeyStore(keystoreFile.getAbsolutePath(), properties.security().tls().keystore().password());
                var TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort, properties.security().tls().clientTimeout(), null, TLSParams);
                TThreadPoolServer.Args settings = new TThreadPoolServer.Args(TLSServerTransport);
                TLSServer = new TThreadPoolServer(settings.processor(multiplexedProcessor));
                tlsServerThread = new Thread(() -> {
                    TLSServer.serve();
                    logger.info("Thrift Server over TLS Stopped.");
                });
                tlsServerThread.setName(getServerName() + "-TLSServer");
                tlsServerThread.setDaemon(true);
                tlsServerThread.start();

                tlsServerMonitorThread = new Thread(() -> {
                    while (!TLSServer.isServing()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (TLSServer.isServing()) {
                        logger.info("Thrift Server over TLS started on Port " + serverPort);
                    }
                });
                tlsServerMonitorThread.setName(getServerName() + "-TLSMonitor");
                tlsServerMonitorThread.setDaemon(true);
                tlsServerMonitorThread.start();
                logger.info("API server started over TLS on Port: " + serverPort + " ...");
            }

        } catch (TTransportException e) {
            logger.error("Failed to start API server ...", e);
            throw new RuntimeException("Failed to start API server: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doStart() throws Exception {
        // Create processors for each service
        Airavata.Processor<Airavata.Iface> airavataProcessor = new Airavata.Processor<>(airavataHandler);
        OrchestratorService.Processor<OrchestratorServiceHandler> orchestratorProcessor =
                new OrchestratorService.Processor<>(orchestratorHandler);
        RegistryService.Processor<RegistryServiceHandler> registryProcessor =
                new RegistryService.Processor<>(registryHandler);
        CredentialStoreService.Processor<CredentialServiceHandler> credentialProcessor =
                new CredentialStoreService.Processor<>(credentialHandler);
        SharingRegistryService.Processor<SharingRegistryServerHandler> sharingProcessor =
                new SharingRegistryService.Processor<>(sharingHandler);

        // Create Profile Service processors (flattened - no nested multiplexing)
        var userProfileProcessor = new UserProfileService.Processor<>(userProfileHandler);
        var tenantProfileProcessor = new TenantProfileService.Processor<>(tenantProfileHandler);
        var iamAdminServicesProcessor = new IamAdminServices.Processor<>(iamAdminHandler);
        var groupManagerProcessor = new GroupManagerService.Processor<>(groupManagerHandler);

        // Create main multiplexed processor and register all services
        TMultiplexedProcessor mainMultiplexedProcessor = new TMultiplexedProcessor();
        mainMultiplexedProcessor.registerProcessor(AIRAVATA_SERVICE_NAME, airavataProcessor);
        // Register Profile sub-services with prefixed names
        mainMultiplexedProcessor.registerProcessor(
                PROFILE_SERVICE_NAME + "." + profile_user_cpiConstants.USER_PROFILE_CPI_NAME, userProfileProcessor);
        mainMultiplexedProcessor.registerProcessor(
                PROFILE_SERVICE_NAME + "." + profile_tenant_cpiConstants.TENANT_PROFILE_CPI_NAME,
                tenantProfileProcessor);
        mainMultiplexedProcessor.registerProcessor(
                PROFILE_SERVICE_NAME + "." + iam_admin_services_cpiConstants.IAM_ADMIN_SERVICES_CPI_NAME,
                iamAdminServicesProcessor);
        mainMultiplexedProcessor.registerProcessor(
                PROFILE_SERVICE_NAME + "." + group_manager_cpiConstants.GROUP_MANAGER_CPI_NAME, groupManagerProcessor);
        mainMultiplexedProcessor.registerProcessor(ORCHESTRATOR_SERVICE_NAME, orchestratorProcessor);
        mainMultiplexedProcessor.registerProcessor(REGISTRY_SERVICE_NAME, registryProcessor);
        mainMultiplexedProcessor.registerProcessor(CREDENTIAL_STORE_SERVICE_NAME, credentialProcessor);
        mainMultiplexedProcessor.registerProcessor(SHARING_REGISTRY_SERVICE_NAME, sharingProcessor);

        logger.info("Registered services in unified Thrift API: Airavata, ProfileService, OrchestratorService, "
                + "RegistryService, CredentialStoreService, SharingRegistryService");

        startThriftServer(mainMultiplexedProcessor);
    }

    @Override
    protected void doStop() throws Exception {
        if (!properties.security().tls().enabled()) {
            if (server != null && server.isServing()) {
                server.stop();
            }
            if (serverMonitorThread != null) {
                serverMonitorThread.interrupt();
                try {
                    serverMonitorThread.join(1000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for server monitor thread to stop", e);
                    Thread.currentThread().interrupt();
                }
            }
            if (serverThread != null) {
                serverThread.interrupt();
                try {
                    serverThread.join(5000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for server thread to stop", e);
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            if (TLSServer != null && TLSServer.isServing()) {
                TLSServer.stop();
            }
            if (tlsServerMonitorThread != null) {
                tlsServerMonitorThread.interrupt();
                try {
                    tlsServerMonitorThread.join(1000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for TLS server monitor thread to stop", e);
                    Thread.currentThread().interrupt();
                }
            }
            if (tlsServerThread != null) {
                tlsServerThread.interrupt();
                try {
                    tlsServerThread.join(5000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for TLS server thread to stop", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
