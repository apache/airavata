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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.server.handler.AiravataServerHandler;
import org.apache.airavata.api.server.util.Constants;
import org.apache.airavata.common.config.ServerSettings;
import org.apache.airavata.common.db.DBInitConfig;
import org.apache.airavata.common.db.DBInitializer;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.server.IServer;
import org.apache.airavata.common.server.MonitoringServer;
import org.apache.airavata.common.server.ServiceRegistry;
import org.apache.airavata.credential.handler.CredentialStoreServerHandler;
import org.apache.airavata.credential.repository.util.CredentialStoreDBInitConfig;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.monitor.ComputationalResourceMonitoringService;
import org.apache.airavata.execution.monitor.EmailBasedMonitor;
import org.apache.airavata.execution.monitor.RealtimeMonitor;
import org.apache.airavata.execution.orchestrator.AbstractTask;
import org.apache.airavata.execution.orchestrator.GlobalParticipant;
import org.apache.airavata.execution.orchestrator.HelixController;
import org.apache.airavata.execution.orchestrator.ParserWorkflowManager;
import org.apache.airavata.execution.orchestrator.PostWorkflowManager;
import org.apache.airavata.execution.orchestrator.PreWorkflowManager;
import org.apache.airavata.execution.scheduler.DataInterpreterService;
import org.apache.airavata.execution.scheduler.ProcessReschedulingService;
import org.apache.airavata.execution.util.AppCatalogDBInitConfig;
import org.apache.airavata.execution.util.ExpCatalogDBInitConfig;
import org.apache.airavata.execution.util.ReplicaCatalogDBInitConfig;
import org.apache.airavata.execution.util.WorkflowCatalogDBInitConfig;
import org.apache.airavata.messaging.handler.DBEventManagerRunner;
import org.apache.airavata.model.error.AiravataErrorType;
import org.apache.airavata.model.error.AiravataSystemException;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.server.OrchestratorServerHandler;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.security.profile.user.core.utils.UserProfileCatalogDBInitConfig;
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
import org.apache.airavata.service.profile.user.cpi.UserProfileService;
import org.apache.airavata.service.profile.user.cpi.profile_user_cpiConstants;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.util.SharingRegistryDBInitConfig;
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
public class AiravataServer {

    private static final Logger logger = LoggerFactory.getLogger(AiravataServer.class);

    private TServer server;
    private final ServiceRegistry serviceRegistry = new ServiceRegistry();

    private final List<DBInitConfig> dbInitConfigs = Arrays.asList(
            new ExpCatalogDBInitConfig(),
            new AppCatalogDBInitConfig(),
            new ReplicaCatalogDBInitConfig(),
            new WorkflowCatalogDBInitConfig(),
            new CredentialStoreDBInitConfig(),
            new UserProfileCatalogDBInitConfig());

    public void start() throws Exception {
        logger.info("Initializing databases...");
        for (DBInitConfig dbInitConfig : dbInitConfigs) {
            DBInitializer.initializeDB(dbInitConfig);
        }
        logger.info("Databases initialized successfully");

        try {
            final String serverHost = ServerSettings.getSetting(Constants.API_SERVER_HOST, null);
            final int serverPort = Integer.parseInt(ServerSettings.getSetting(Constants.API_SERVER_PORT, "8930"));

            // --- Instantiate handlers ---
            CredentialStoreServerHandler credentialStoreServerHandler = new CredentialStoreServerHandler();
            RegistryServerHandler registryServerHandler = new RegistryServerHandler();
            SharingRegistryServerHandler sharingRegistryServerHandler =
                    new SharingRegistryServerHandler(new SharingRegistryDBInitConfig());

            // AiravataServerHandler constructor accepting in-process handler refs is added in Task 3.
            // Until then this file will not compile.
            AiravataServerHandler airavataServerHandler = new AiravataServerHandler(
                    registryServerHandler, sharingRegistryServerHandler, credentialStoreServerHandler);
            airavataServerHandler.initialize();

            // --- Build processors ---
            TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();

            multiplexedProcessor.registerProcessor("Airavata", new Airavata.Processor<>(airavataServerHandler));

            multiplexedProcessor.registerProcessor(
                    "RegistryService", new RegistryService.Processor<>(registryServerHandler));

            multiplexedProcessor.registerProcessor(
                    "SharingRegistry", new SharingRegistryService.Processor<>(sharingRegistryServerHandler));

            multiplexedProcessor.registerProcessor(
                    "CredentialStore", new CredentialStoreService.Processor<>(credentialStoreServerHandler));

            // Profile services
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
                logger.warn(
                        "Orchestrator service failed to initialize (ZooKeeper/Helix may not be available): {}",
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

            Thread serveThread = new Thread(
                    () -> {
                        server.serve();
                        logger.info("Airavata Server stopped.");
                    },
                    "airavata-thrift-server");
            serveThread.setDaemon(true);
            serveThread.start();

            new Thread(() -> {
                        while (!server.isServing()) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (server.isServing()) {
                            logger.info("Airavata Server started on port {}", serverPort);
                            logger.info("Registered services: Airavata, RegistryService, SharingRegistry, "
                                    + "CredentialStore, UserProfile, TenantProfile, IamAdminServices, GroupManager");
                            startBackgroundServices();
                        }
                    })
                    .start();

        } catch (TTransportException | ApplicationSettingsException e) {
            logger.error("Failed to start Airavata Server", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Starts background services after the Thrift server is ready.
     * Each service has an independent lifecycle — failures are non-fatal
     * and do not affect other services.
     */
    private void startBackgroundServices() {
        logger.info("Starting background services...");

        // DB Event Manager — processes async DB events
        registerAndStart(new DBEventManagerRunner(), "db_event_manager");

        // Monitoring Server — Prometheus metrics endpoint
        try {
            if (ServerSettings.getBooleanSetting("api.server.monitoring.enabled")) {
                String monHost = ServerSettings.getSetting("api.server.monitoring.host", "localhost");
                int monPort = Integer.parseInt(ServerSettings.getSetting("api.server.monitoring.port", "9097"));
                MonitoringServer monitoringServer = new MonitoringServer(monHost, monPort);
                monitoringServer.setServiceRegistry(serviceRegistry);
                registerAndStart(monitoringServer, "monitoring_server");
            }
        } catch (Exception e) {
            logger.warn("  monitoring_server: config error — {}", e.getMessage());
            serviceRegistry.recordError("monitoring_server", e.getMessage());
        }

        // Cluster status monitoring — polls compute resource queue status
        try {
            if (ServerSettings.enableClusterStatusMonitoring()) {
                registerAndStart(new ComputationalResourceMonitoringService(), "cluster_status_monitor");
            }
        } catch (Exception e) {
            logger.warn("  cluster_status_monitor: config error — {}", e.getMessage());
            serviceRegistry.recordError("cluster_status_monitor", e.getMessage());
        }

        // Data interpreter — metadata analysis for submitted jobs
        try {
            if (ServerSettings.enableDataAnalyzerJobScanning()) {
                registerAndStart(new DataInterpreterService(), "data_interpreter");
            }
        } catch (Exception e) {
            logger.warn("  data_interpreter: config error — {}", e.getMessage());
            serviceRegistry.recordError("data_interpreter", e.getMessage());
        }

        // Process rescheduler — retries/reschedules failed processes
        try {
            if (ServerSettings.enableMetaschedulerJobScanning()) {
                registerAndStart(new ProcessReschedulingService(), "process_rescheduler");
            }
        } catch (Exception e) {
            logger.warn("  process_rescheduler: config error — {}", e.getMessage());
            serviceRegistry.recordError("process_rescheduler", e.getMessage());
        }

        // Execution engine services — controller must initialize the cluster
        // in ZooKeeper before participants and workflow managers can join
        try {
            registerAndStart(new HelixController(), "helix_controller");
            waitForHelixCluster();
        } catch (Exception e) {
            logger.warn("  helix_controller: config error — {}", e.getMessage());
            serviceRegistry.recordError("helix_controller", e.getMessage());
        }
        try {
            ArrayList<Class<? extends AbstractTask>> taskClasses = new ArrayList<>();
            for (String taskName : GlobalParticipant.TASK_CLASS_NAMES) {
                taskClasses.add(Class.forName(taskName).asSubclass(AbstractTask.class));
            }
            registerAndStart(new GlobalParticipant(taskClasses, null), "helix_participant");
        } catch (Exception e) {
            logger.warn("  helix_participant: config error — {}", e.getMessage());
            serviceRegistry.recordError("helix_participant", e.getMessage());
        }
        try {
            registerAndStart(new PreWorkflowManager(), "pre_workflow_manager");
        } catch (Exception e) {
            logger.warn("  pre_workflow_manager: config error — {}", e.getMessage());
            serviceRegistry.recordError("pre_workflow_manager", e.getMessage());
        }
        try {
            registerAndStart(new PostWorkflowManager(), "post_workflow_manager");
        } catch (Exception e) {
            logger.warn("  post_workflow_manager: config error — {}", e.getMessage());
            serviceRegistry.recordError("post_workflow_manager", e.getMessage());
        }
        try {
            registerAndStart(new ParserWorkflowManager(), "parser_workflow_manager");
        } catch (Exception e) {
            logger.warn("  parser_workflow_manager: config error — {}", e.getMessage());
            serviceRegistry.recordError("parser_workflow_manager", e.getMessage());
        }

        // Job monitors
        try {
            if (Boolean.parseBoolean(ServerSettings.getSetting("email.based.monitoring.enabled", "false"))) {
                registerAndStart(new EmailBasedMonitor(), "email_monitor");
            }
        } catch (Exception e) {
            logger.warn("  email_monitor: config error — {}", e.getMessage());
            serviceRegistry.recordError("email_monitor", e.getMessage());
        }
        try {
            registerAndStart(new RealtimeMonitor(), "realtime_monitor");
        } catch (Exception e) {
            logger.warn("  realtime_monitor: config error — {}", e.getMessage());
            serviceRegistry.recordError("realtime_monitor", e.getMessage());
        }

        logger.info("Background services initialization complete");
    }

    private void waitForHelixCluster() {
        String clusterName = ServerSettings.getSetting("helix.cluster.name", "AiravataCluster");
        String zkUrl = ServerSettings.getSetting("zookeeper.server.connection", "localhost:2181");
        logger.info("  Waiting for Helix cluster '{}' in ZooKeeper at {}...", clusterName, zkUrl);
        for (int i = 0; i < 30; i++) {
            try {
                org.apache.helix.manager.zk.ZKHelixAdmin admin = new org.apache.helix.manager.zk.ZKHelixAdmin(zkUrl);
                List<String> clusters = admin.getClusters();
                admin.close();
                if (clusters.contains(clusterName)) {
                    logger.info("  Helix cluster '{}' is ready", clusterName);
                    return;
                }
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
        logger.warn("  Helix cluster '{}' not found after 30s — proceeding anyway", clusterName);
    }

    private void registerAndStart(IServer service, String label) {
        registerAndStart(service, label, null);
    }

    private void registerAndStart(IServer service, String label, Supplier<IServer> factory) {
        Thread t = new Thread(service, "airavata-" + label);
        t.setDaemon(true);
        t.start();
        serviceRegistry.register(label, service, t, factory);
        logger.info("  {}: started", label);
    }

    public void stop() throws Exception {
        if (server != null && server.isServing()) {
            server.stop();
        }
        serviceRegistry.stopAll();
    }

    public static void main(String[] args) {
        try {
            new AiravataServer().start();
        } catch (Exception e) {
            logger.error("Error starting Airavata Server", e);
        }
    }
}
