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
package org.apache.airavata.monitor.compute;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.JobSubmissionInterface;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "airavata.services.monitor.compute", name = "enabled", havingValue = "true")
public class ClusterStatusMonitorJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJob.class);

    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;
    private final AiravataServerProperties properties;
    private final ComputeSubmissionTracker computeSubmissionTracker;

    public ClusterStatusMonitorJob(
            RegistryService registryService,
            CredentialStoreService credentialStoreService,
            AiravataServerProperties properties,
            ComputeSubmissionTracker computeSubmissionTracker) {
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
        this.properties = properties;
        this.computeSubmissionTracker = computeSubmissionTracker;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            // Super-tenant gateway is now derived from the default gateway config.
            String superTenantGatewayId = properties.defaultGateway();
            List<ComputeResourceProfile> computeResourceProfiles = new ArrayList<>();
            List<ComputeResourcePreference> computeResourcePreferences = null;
            try {
                computeResourcePreferences =
                        registryService.getAllGatewayComputeResourcePreferences(superTenantGatewayId);
            } catch (Exception ex) {
                logger.warn(
                        "Could not find super tenant compute resources preferences for cluster status monitoring...");
            }
            if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
                computeResourcePreferences.forEach(p -> {
                    try {
                        String computeResourceId = p.getComputeResourceId();

                        // Skip cluster monitoring if jobs were submitted recently
                        if (computeSubmissionTracker.hasRecentSubmissions(
                                computeResourceId,
                                properties.services().monitor().compute().clusterCheckTimeWindow())) {
                            logger.debug(
                                    "Skipping cluster status check for {} - jobs submitted recently",
                                    computeResourceId);
                            return;
                        }

                        String credentialStoreToken = p.getResourceSpecificCredentialStoreToken();
                        String loginUserName = p.getLoginUserName();
                        String hostName = null;
                        if (credentialStoreToken == null || credentialStoreToken.isEmpty()) {
                            credentialStoreToken = registryService
                                    .getGatewayResourceProfile(superTenantGatewayId)
                                    .getCredentialStoreToken();
                        }
                        int port = -1;
                        ArrayList<String> queueNames = new ArrayList<>();

                        ComputeResourceDescription computeResourceDescription =
                                registryService.getComputeResource(computeResourceId);
                        hostName = computeResourceDescription.getHostName();
                        // FIXME This should come from compute resource description
                        port = 22;
                        computeResourceDescription.getBatchQueues().forEach(q -> queueNames.add(q.getQueueName()));

                        List<JobSubmissionInterface> jobSubmissionInterfaces =
                                computeResourceDescription.getJobSubmissionInterfaces();
                        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                            if (jobSubmissionInterfaces
                                    .get(0)
                                    .getJobSubmissionProtocol()
                                    .equals(JobSubmissionProtocol.SSH)) {
                                String resourceManagerType = registryService
                                        .getSSHJobSubmission(
                                                jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId())
                                        .getResourceJobManager()
                                        .getResourceJobManagerType()
                                        .name();
                                ComputeResourceProfile computeResourceProfile = new ComputeResourceProfile(
                                        hostName,
                                        loginUserName,
                                        port,
                                        credentialStoreToken,
                                        queueNames,
                                        resourceManagerType);
                                computeResourceProfiles.add(computeResourceProfile);
                            }
                        }
                    } catch (RegistryException e) {
                        logger.error(e.getMessage());
                    }
                });
            }

            ArrayList<QueueStatusModel> queueStatuses = new ArrayList<>();

            for (ComputeResourceProfile computeResourceProfile : computeResourceProfiles) {

                String userName = computeResourceProfile.getUserName();
                String hostName = computeResourceProfile.getHostName();
                int port = computeResourceProfile.getPort();

                SSHClient client = null;
                try {
                    client = new SSHClient();
                    // Disable strict host key checking
                    client.addHostKeyVerifier(new PromiscuousVerifier());
                    client.connect(hostName, port);

                    SSHCredential sshCredential = credentialStoreService.getSSHCredential(
                            computeResourceProfile.getCredentialStoreToken(), superTenantGatewayId);

                    // Load private key
                    KeyProvider keyProvider = loadKeyProvider(sshCredential);
                    client.authPublickey(userName, keyProvider);

                    logger.debug("Connected to " + hostName);

                    for (String queue : computeResourceProfile.getQueueNames()) {
                        String command = "";
                        if (computeResourceProfile.getResourceManagerType().equals("SLURM"))
                            command = "sinfo -s -p " + queue + " -o \"%a %F\" | tail -1";
                        else if (computeResourceProfile.getResourceManagerType().equals("PBS"))
                            command = "qstat -Q " + queue + "| tail -1";

                        if (command.isEmpty()) {
                            logger.warn("No matching resource manager type found for "
                                    + computeResourceProfile.getResourceManagerType());
                            continue;
                        }

                        // Execute command using SSHJ
                        try (net.schmizz.sshj.connection.channel.direct.Session session = client.startSession()) {
                            net.schmizz.sshj.connection.channel.direct.Session.Command cmd = session.exec(command);

                            // Read stdout
                            StringBuilder resultBuilder = new StringBuilder();
                            try (InputStream stdout = cmd.getInputStream()) {
                                byte[] tmp = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = stdout.read(tmp)) != -1) {
                                    resultBuilder.append(new String(tmp, 0, bytesRead, StandardCharsets.UTF_8));
                                }
                            }

                            // Wait for command to complete
                            cmd.join(30, TimeUnit.SECONDS);
                            Integer exitStatus = cmd.getExitStatus();
                            logger.debug(hostName + " " + queue + " " + "exit-status: " + exitStatus);

                            String result = resultBuilder.toString();

                            if (result != null && result.length() > 0) {
                                QueueStatusModel queueStatus = null;
                                if (computeResourceProfile
                                        .getResourceManagerType()
                                        .equals("SLURM")) {
                                    String[] sparts = result.split(" ");
                                    boolean isUp = sparts[0].equalsIgnoreCase("up");
                                    String knts = sparts[1];
                                    sparts = knts.split("/");
                                    int running = Integer.parseInt(sparts[0].trim());
                                    int queued = Integer.parseInt(sparts[1].trim());
                                    queueStatus = new QueueStatusModel(
                                            hostName,
                                            queue,
                                            isUp,
                                            running,
                                            queued,
                                            org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp()
                                                    .getTime());

                                } else if (computeResourceProfile
                                        .getResourceManagerType()
                                        .equals("PBS")) {
                                    result = result.replaceAll("\\s+", " ");
                                    String[] sparts = result.split(" ");
                                    boolean isUp = sparts[3].equalsIgnoreCase("yes");
                                    int running = Integer.parseInt(sparts[6].trim());
                                    int queued = Integer.parseInt(sparts[5].trim());
                                    queueStatus = new QueueStatusModel(
                                            hostName,
                                            queue,
                                            isUp,
                                            running,
                                            queued,
                                            org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp()
                                                    .getTime());
                                }

                                if (queueStatus != null) queueStatuses.add(queueStatus);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Failed to get cluster status from " + computeResourceProfile.getHostName());
                    logger.error(ex.getMessage(), ex);
                } finally {
                    if (client != null && client.isConnected()) {
                        try {
                            client.disconnect();
                        } catch (Exception e) {
                            logger.warn("Error disconnecting SSH client", e);
                        }
                    }
                }
            }
            if (queueStatuses != null && !queueStatuses.isEmpty()) {
                registryService.registerQueueStatuses(queueStatuses);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private static class ComputeResourceProfile {

        private String hostName;
        private String userName;
        private int port;
        private String credentialStoreToken;
        private List<String> queueNames;
        private String resourceManagerType;

        public ComputeResourceProfile(
                String hostName,
                String userName,
                int port,
                String credentialStoreToken,
                List<String> queueNames,
                String resourceManagerType) {
            this.hostName = hostName;
            this.userName = userName;
            this.port = port;
            this.credentialStoreToken = credentialStoreToken;
            this.queueNames = queueNames;
            this.resourceManagerType = resourceManagerType;
        }

        public String getHostName() {
            return hostName;
        }

        // Unused setter methods - commented out
        /*
        public void setHostName(String hostName) {
            this.hostName = hostName;
        }
        */

        public String getUserName() {
            return userName;
        }

        // Unused setter method - commented out
        /*
        public void setUserName(String userName) {
            this.userName = userName;
        }
        */

        public int getPort() {
            return port;
        }

        // Unused setter method - commented out
        /*
        public void setPort(int port) {
            this.port = port;
        }
        */

        public String getCredentialStoreToken() {
            return credentialStoreToken;
        }

        // Unused setter method - commented out
        /*
        public void setCredentialStoreToken(String credentialStoreToken) {
            this.credentialStoreToken = credentialStoreToken;
        }
        */

        public List<String> getQueueNames() {
            return queueNames;
        }

        // Unused setter method - commented out
        /*
        public void setQueueNames(List<String> queueNames) {
            this.queueNames = queueNames;
        }
        */

        public String getResourceManagerType() {
            return resourceManagerType;
        }

        // Unused setter method - commented out
        /*
        public void setResourceManagerType(String resourceManagerType) {
            this.resourceManagerType = resourceManagerType;
        }
        */
    }

    /**
     * Load KeyProvider from SSHCredential bytes.
     */
    private static KeyProvider loadKeyProvider(SSHCredential sshCredential) throws java.io.IOException {
        String privateKeyStr = sshCredential.getPrivateKey();
        String passphrase = sshCredential.getPassphrase();

        // Use SSHClient.loadKeys() to load key from string
        SSHClient tempClient = new SSHClient();
        net.schmizz.sshj.userauth.password.PasswordFinder passwordFinder = null;
        if (passphrase != null && !passphrase.isEmpty()) {
            passwordFinder = net.schmizz.sshj.userauth.password.PasswordUtils.createOneOff(passphrase.toCharArray());
        }
        return tempClient.loadKeys(privateKeyStr, null, passwordFinder);
    }
}
