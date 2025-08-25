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
package org.apache.airavata;

import org.apache.airavata.api.AiravataAPIServer;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.db.event.manager.DBEventManagerRunner;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.metascheduler.metadata.analyzer.DataInterpreterService;
import org.apache.airavata.metascheduler.process.scheduling.engine.rescheduler.ProcessReschedulingService;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.monitor.cluster.ClusterStatusMonitorJobScheduler;
import org.apache.airavata.monitor.compute.ComputationalResourceMonitoringService;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.platform.MonitoringServer;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static {
        Thread.currentThread().setName("Main");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static String getLogo() {
        String logo = "";
        try (java.io.InputStream is = Main.class.getClassLoader().getResourceAsStream("logo.txt")) {
            if (is != null) {
                java.util.Scanner scanner = new java.util.Scanner(is, java.nio.charset.StandardCharsets.UTF_8.name());
                scanner.useDelimiter("\\A");
                logo = scanner.hasNext() ? scanner.next() : "";
                scanner.close();
            }
        } catch (Exception e) {
            logger.warn("Could not load logo.txt", e);
        }
        return logo;
    }

    public static void main(String[] args) throws Exception {

        String logo = getLogo();
        System.out.println(logo);
        Thread.sleep(1000);

        logger.info("Starting Airavata API Server .......");
        var airavataApiServer = new AiravataAPIServer();
        airavataApiServer.start();

        logger.info("Starting DB Event Manager Runner .......");
        var dbEventManagerRunner = new DBEventManagerRunner();
        dbEventManagerRunner.start();

        logger.info("Starting Helix Controller .......");
        var helixController = new HelixController();
        helixController.start();

        logger.info("Starting Helix Participant .......");
        var participant = new GlobalParticipant();
        participant.run();

        logger.info("Starting Pre Workflow Manager .......");
        var preWorkflowManager = new PreWorkflowManager();
        preWorkflowManager.run();

        logger.info("Starting Post Workflow Manager .......");
        var postWorkflowManager = new PostWorkflowManager();
        postWorkflowManager.run();

        if (ServerSettings.getBooleanSetting("data.interpreter.enabled")) {
            logger.info("Starting Data Interpreter .......");
            var dataInterpreter = new DataInterpreterService();
            dataInterpreter.start();
        }

        if (ServerSettings.getBooleanSetting("process.rescheduler.enabled")) {
            logger.info("Starting Process Rescheduler .......");
            var processRescheduler = new ProcessReschedulingService();
            processRescheduler.start();
        }

        if (ServerSettings.getBooleanSetting("monitor.email.enabled")) {
            logger.info("Starting Email Monitor .......");
            var emailMonitor = new EmailBasedMonitor();
            emailMonitor.run();
        }

        if (ServerSettings.getBooleanSetting("monitor.job.realtime.enabled")) {
            logger.info("Starting Realtime Monitor .......");
            var realTimeMonitor = new RealtimeMonitor();
            realTimeMonitor.run();
        }

        if (ServerSettings.getBooleanSetting("monitor.job.submission.enabled")) {
            logger.info("Starting Job Submission Monitor .......");
            var clusterMonitor = new ClusterStatusMonitorJobScheduler();
            clusterMonitor.scheduleClusterStatusMonitoring();
        }

        if (ServerSettings.getBooleanSetting("monitor.compute.resource.enabled")) {
            logger.info("Starting Cluster Resource Monitor .......");
            var resourceMonitor = new ComputationalResourceMonitoringService();
            resourceMonitor.start();
        }

        if (ServerSettings.getBooleanSetting("monitor.prometheus.enabled")) {
            logger.info("Starting Prometheus Monitor .......");
            var monitoringServer = new MonitoringServer(
                    ServerSettings.getSetting("monitor.prometheus.host"),
                    ServerSettings.getIntSetting("monitor.prometheus.port"));
            monitoringServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(monitoringServer::stop));
        }

        postInit();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            logger.info("Main thread is interrupted! reason: " + ex);
            ServerSettings.setStopAllThreads(true);
        }
    }

    public static void postInit() {
        try {
            var defaultGatewayId = ServerSettings.getDefaultUserGateway();
            var defaultUsername = ServerSettings.getDefaultUser();
            var defaultPassword = ServerSettings.getDefaultUserPassword();
            var defaultOauthClientId = ServerSettings.getSetting("default.registry.oauth.client.id");
            var defaultOauthClientSecret = ServerSettings.getSetting("default.registry.oauth.client.secret");
            var credStore = AiravataServiceFactory.getCredentialStore();
            var domainRepository = new org.apache.airavata.catalog.sharing.db.repositories.DomainRepository();
            var gatewayRepository = new org.apache.airavata.registry.core.repositories.expcatalog.GatewayRepository();
            var expUserRepository = new org.apache.airavata.registry.core.repositories.expcatalog.UserRepository();
            var sharingUserRepository = new org.apache.airavata.catalog.sharing.db.repositories.UserRepository();
            var entityTypeRepository = new org.apache.airavata.catalog.sharing.db.repositories.EntityTypeRepository();
            var permissionTypeRepository =
                    new org.apache.airavata.catalog.sharing.db.repositories.PermissionTypeRepository();
            var gwrpRepository =
                    new org.apache.airavata.registry.core.repositories.appcatalog.GwyResourceProfileRepository();

            // create default gateway if not exists
            if (!gatewayRepository.isGatewayExist(defaultGatewayId)) {
                var gateway = new Gateway();
                gateway.setGatewayId(defaultGatewayId);
                gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
                gateway.setOauthClientId(defaultOauthClientId);
                gateway.setOauthClientSecret(defaultOauthClientSecret);
                gatewayRepository.addGateway(gateway);
            }

            // create default user if not exists and add to default gateway
            if (!expUserRepository.isUserExists(defaultGatewayId, defaultUsername)) {
                var defaultUser = new UserProfile();
                defaultUser.setUserId(defaultUsername);
                defaultUser.setGatewayId(defaultGatewayId);
                expUserRepository.addUser(defaultUser);
            }

            // create default gateway resource profile if not exists
            if (!gwrpRepository.isGatewayResourceProfileExists(defaultGatewayId)) {
                var gatewayResourceProfile = new GatewayResourceProfile();
                gatewayResourceProfile.setGatewayID(defaultGatewayId);
                gwrpRepository.addGatewayResourceProfile(gatewayResourceProfile);
            }

            // create password credential for default gateway resource profile if not exists
            var gwrp = gwrpRepository.getGatewayProfile(defaultGatewayId);
            if (gwrp != null && gwrp.getIdentityServerPwdCredToken() == null) {
                logger.debug("no password credential found for the default gateway: {}", defaultGatewayId);

                var pw = new PasswordCredential();
                pw.setPortalUserName(defaultUsername);
                pw.setGatewayId(defaultGatewayId);
                pw.setLoginUserName(defaultUsername);
                pw.setPassword(defaultPassword);
                pw.setDescription("Credentials for default gateway");

                String token = null;
                try {
                    logger.info("adding new password credential for the default gateway: {}", defaultGatewayId);
                    token = credStore.addPasswordCredential(pw);
                } catch (Exception ex) {
                    logger.error(
                            "Failed to add the password credential for the default gateway: {}", defaultGatewayId, ex);
                }

                if (token != null) {
                    logger.debug(
                            "adding password credential token {} to the default gateway: {}", token, defaultGatewayId);
                    gwrp.setIdentityServerPwdCredToken(token);
                    gwrp.setIdentityServerTenant(defaultGatewayId);
                    gwrpRepository.updateGatewayResourceProfile(gwrp);
                }
            }

            if (domainRepository.get(defaultGatewayId) == null) {
                var domain = new org.apache.airavata.catalog.sharing.models.Domain();
                domain.setDomainId(defaultGatewayId);
                domain.setName(defaultGatewayId);
                domain.setDescription("Domain entry for " + domain.getName());
                domainRepository.create(domain);

                var user = new org.apache.airavata.catalog.sharing.models.User();
                user.setDomainId(domain.getDomainId());
                user.setUserId(defaultUsername + "@" + defaultGatewayId);
                user.setUserName(defaultUsername);
                sharingUserRepository.create(user);

                // Creating Entity Types for each domain
                String[][] entityTypes = {
                    {"PROJECT", "Project entity type"},
                    {"EXPERIMENT", "Experiment entity type"},
                    {"FILE", "File entity type"},
                    {ResourceType.APPLICATION_DEPLOYMENT.name(), "Application Deployment entity type"},
                    {ResourceType.GROUP_RESOURCE_PROFILE.name(), "Group Resource Profile entity type"},
                    {ResourceType.CREDENTIAL_TOKEN.name(), "Credential Store Token entity type"}
                };
                for (String[] et : entityTypes) {
                    var entityType = new org.apache.airavata.catalog.sharing.models.EntityType();
                    entityType.setEntityTypeId(domain.getDomainId() + ":" + et[0]);
                    entityType.setDomainId(domain.getDomainId());
                    entityType.setName(et[0]);
                    entityType.setDescription(et[1]);
                    entityTypeRepository.create(entityType);
                }

                // Creating Permission Types for each domain
                String[][] permissionTypes = {
                    {"READ", "Read permission type"},
                    {"WRITE", "Write permission type"},
                    {"MANAGE_SHARING", "Sharing permission type"}
                };
                for (String[] pt : permissionTypes) {
                    var permissionType = new org.apache.airavata.catalog.sharing.models.PermissionType();
                    permissionType.setPermissionTypeId(domain.getDomainId() + ":" + pt[0]);
                    permissionType.setDomainId(domain.getDomainId());
                    permissionType.setName(pt[0]);
                    permissionType.setDescription(pt[1]);
                    permissionTypeRepository.create(permissionType);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to initialize DB entries", e);
        }
    }
}
