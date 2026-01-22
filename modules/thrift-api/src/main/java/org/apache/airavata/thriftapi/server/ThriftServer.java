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

import java.io.File;
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
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

/**
 * Thrift Server - Thrift Endpoints for Airavata API functions.
 *
 * <p>This server provides Thrift endpoints for Airavata API functions, running on port 8930
 * (configurable via {@code airavata.services.thrift.server.port}). It uses a multiplexed
 * processor to expose multiple internal services through a single Thrift endpoint.
 *
 * <p><b>External API:</b> This is one of four external API layers in Airavata:
 * <ul>
 *   <li>Thrift Server (port 8930) - Thrift Endpoints for Airavata API functions (this server)</li>
 *   <li>HTTP Server (port 8080):
 *       <ul>
 *         <li>Airavata API - HTTP Endpoints for Airavata API functions</li>
 *         <li>File API - HTTP Endpoints for file upload/download</li>
 *         <li>Agent API - HTTP Endpoints for interactive job contexts</li>
 *         <li>Research API - HTTP Endpoints for use by research hub</li>
 *       </ul>
 *   </li>
 *   <li>gRPC Server (port 9090) - For airavata binaries to open persistent channels with airavata APIs</li>
 *   <li>Dapr gRPC (port 50001) - Sidecar for pub/sub, state, and workflow execution</li>
 * </ul>
 *
 * <p><b>Multiplexed Services:</b> The following services are exposed through this unified
 * Thrift Server endpoint using service name prefixes:
 * <ul>
 *   <li>{@code Airavata} - Main Airavata service for experiments, processes, and workflows</li>
 *   <li>{@code ProfileService.UserProfileService} - User profile management</li>
 *   <li>{@code ProfileService.TenantProfileService} - Tenant/gateway profile management</li>
 *   <li>{@code ProfileService.IamAdminServices} - IAM administration</li>
 *   <li>{@code ProfileService.GroupManagerService} - Group management</li>
 *   <li>{@code OrchestratorService} - Workflow orchestration (internal service)</li>
 *   <li>{@code RegistryService} - Application and metadata registry (internal service)</li>
 *   <li>{@code CredentialStoreService} - Secure credential storage (internal service)</li>
 *   <li>{@code SharingRegistryService} - Permissions and sharing (internal service)</li>
 * </ul>
 *
 * <p><b>Internal Services:</b> Orchestrator, Registry, Profile Service, Sharing Registry,
 * and Credential Store are internal components that are accessed via Thrift Server.
 * They are not separate servers but are multiplexed through this unified endpoint.
 *
 * <p><b>Thrift Interface Definitions:</b> The Thrift IDL files are located in
 * {@code thrift-interface-descriptions/} directory. See the main README.md for stub
 * generation instructions.
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>{@code airavata.services.thrift.enabled} - Enable/disable Thrift server (default: true)</li>
 *   <li>{@code airavata.services.thrift.server.port} - Server port (default: 8930)</li>
 *   <li>{@code airavata.security.tls.enabled} - Enable TLS encryption (default: false)</li>
 * </ul>
 *
 * <p><b>TLS Support:</b> When TLS is enabled via {@code airavata.security.tls.enabled=true},
 * the server uses the keystore configured in {@code airavata.security.tls.keystore.path}
 * (relative to the configuration directory).
 *
 * @see org.apache.airavata.config.AiravataServerProperties
 * @see org.apache.airavata.config.ServerLifecycle
 */
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
                var serverTransport = new TServerSocket(serverPort);
                var options = new TThreadPoolServer.Args(serverTransport);
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
                            "TLS keystore configuration is missing: security.tls.keystore.path is not set in application.properties");
                }
                // Keystore path is relative to configDir (e.g., "keystores/airavata.p12")
                var keystoreFile = new File(
                        configDir, properties.security().tls().keystore().path());
                TLSParams.setKeyStore(
                        keystoreFile.getAbsolutePath(),
                        properties.security().tls().keystore().password());
                var TLSServerTransport = TSSLTransportFactory.getServerSocket(
                        serverPort, properties.security().tls().clientTimeout(), null, TLSParams);
                var settings = new TThreadPoolServer.Args(TLSServerTransport);
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
        var airavataProcessor = new Airavata.Processor<>(airavataHandler);
        var orchestratorProcessor = new OrchestratorService.Processor<>(orchestratorHandler);
        var registryProcessor = new RegistryService.Processor<>(registryHandler);
        var credentialProcessor = new CredentialStoreService.Processor<>(credentialHandler);
        var sharingProcessor = new SharingRegistryService.Processor<>(sharingHandler);

        // Create Profile Service processors (flattened - no nested multiplexing)
        var userProfileProcessor = new UserProfileService.Processor<>(userProfileHandler);
        var tenantProfileProcessor = new TenantProfileService.Processor<>(tenantProfileHandler);
        var iamAdminServicesProcessor = new IamAdminServices.Processor<>(iamAdminHandler);
        var groupManagerProcessor = new GroupManagerService.Processor<>(groupManagerHandler);

        // Create main multiplexed processor and register all services
        var mainMultiplexedProcessor = new TMultiplexedProcessor();
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

        logger.info("Registered services in unified Thrift Server: Airavata, ProfileService, OrchestratorService, "
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
