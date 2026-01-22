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
package org.apache.airavata.activities.monitoring.cluster;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.monitor.compute.ComputeSubmissionTracker;
import org.apache.airavata.orchestrator.WorkflowRuntimeHolder;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activity for cluster status monitoring.
 */
public class ClusterStatusMonitorActivity implements WorkflowActivity {

    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorActivity.class);

    /** Input for ClusterStatusMonitorActivity - just a marker record */
    public record ClusterStatusMonitorInput() implements Serializable {}

    @Override
    public String run(WorkflowActivityContext ctx) {
        ctx.getInput(ClusterStatusMonitorInput.class);
        logger.debug("ClusterStatusMonitorActivity");

        var registryService = WorkflowRuntimeHolder.getBean(RegistryService.class);
        var credentialStoreService = WorkflowRuntimeHolder.getBean(CredentialStoreService.class);
        var properties = WorkflowRuntimeHolder.getBean(AiravataServerProperties.class);
        var computeSubmissionTracker = WorkflowRuntimeHolder.getBean(ComputeSubmissionTracker.class);

        try {
            // Super-tenant gateway is now derived from the default gateway config.
            var superTenantGatewayId = properties.defaultGateway();
            var computeResourceProfiles = new ArrayList<ComputeResourceProfile>();
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
                        var computeResourceId = p.getComputeResourceId();

                        // Skip cluster monitoring if jobs were submitted recently
                        if (computeSubmissionTracker.hasRecentSubmissions(
                                computeResourceId,
                                properties.services().monitor().compute().clusterCheckTimeWindow())) {
                            logger.debug(
                                    "Skipping cluster status check for {} - jobs submitted recently",
                                    computeResourceId);
                            return;
                        }

                        var credentialStoreToken = p.getResourceSpecificCredentialStoreToken();
                        var loginUserName = p.getLoginUserName();
                        String hostName = null;
                        if (credentialStoreToken == null || credentialStoreToken.isEmpty()) {
                            credentialStoreToken = registryService
                                    .getGatewayResourceProfile(superTenantGatewayId)
                                    .getCredentialStoreToken();
                        }
                        int port = -1;
                        var queueNames = new ArrayList<String>();

                        var computeResourceDescription = registryService.getComputeResource(computeResourceId);
                        hostName = computeResourceDescription.getHostName();
                        // FIXME This should come from compute resource description
                        port = 22;
                        computeResourceDescription.getBatchQueues().forEach(q -> queueNames.add(q.getQueueName()));

                        var jobSubmissionInterfaces = computeResourceDescription.getJobSubmissionInterfaces();
                        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                            if (jobSubmissionInterfaces
                                    .get(0)
                                    .getJobSubmissionProtocol()
                                    .equals(JobSubmissionProtocol.SSH)) {
                                var resourceManagerType = registryService
                                        .getSSHJobSubmission(
                                                jobSubmissionInterfaces.get(0).getJobSubmissionInterfaceId())
                                        .getResourceJobManager()
                                        .getResourceJobManagerType()
                                        .name();
                                var computeResourceProfile = new ComputeResourceProfile(
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

            var queueStatuses = new ArrayList<QueueStatusModel>();

            for (var computeResourceProfile : computeResourceProfiles) {

                var userName = computeResourceProfile.getUserName();
                var hostName = computeResourceProfile.getHostName();
                var port = computeResourceProfile.getPort();

                SSHClient client = null;
                try {
                    client = new SSHClient();
                    // Disable strict host key checking
                    client.addHostKeyVerifier(new PromiscuousVerifier());
                    client.connect(hostName, port);

                    var sshCredential = credentialStoreService.getSSHCredential(
                            computeResourceProfile.getCredentialStoreToken(), superTenantGatewayId);

                    // Load private key
                    var keyProvider = loadKeyProvider(sshCredential);
                    client.authPublickey(userName, keyProvider);

                    logger.debug("Connected to " + hostName);

                    for (var queue : computeResourceProfile.getQueueNames()) {
                        var command = "";
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
                        try (var session = client.startSession()) {
                            var cmd = session.exec(command);

                            // Read stdout
                            var resultBuilder = new StringBuilder();
                            try (var stdout = cmd.getInputStream()) {
                                byte[] tmp = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = stdout.read(tmp)) != -1) {
                                    resultBuilder.append(new String(tmp, 0, bytesRead, StandardCharsets.UTF_8));
                                }
                            }

                            // Wait for command to complete
                            cmd.join(30, TimeUnit.SECONDS);
                            var exitStatus = cmd.getExitStatus();
                            logger.debug(hostName + " " + queue + " " + "exit-status: " + exitStatus);

                            var result = resultBuilder.toString();

                            if (result != null && result.length() > 0) {
                                QueueStatusModel queueStatus = null;
                                if (computeResourceProfile
                                        .getResourceManagerType()
                                        .equals("SLURM")) {
                                    var sparts = result.split(" ");
                                    var isUp = sparts[0].equalsIgnoreCase("up");
                                    var knts = sparts[1];
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
                                    var sparts = result.split(" ");
                                    var isUp = sparts[3].equalsIgnoreCase("yes");
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

            logger.debug("ClusterStatusMonitorActivity completed");
            return "ClusterStatusMonitorActivity completed successfully";
        } catch (Exception e) {
            logger.error("Error in ClusterStatusMonitorActivity: {}", e.getMessage(), e);
            throw new RuntimeException("ClusterStatusMonitorActivity failed", e);
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

        public String getUserName() {
            return userName;
        }

        public int getPort() {
            return port;
        }

        public String getCredentialStoreToken() {
            return credentialStoreToken;
        }

        public List<String> getQueueNames() {
            return queueNames;
        }

        public String getResourceManagerType() {
            return resourceManagerType;
        }
    }

    /**
     * Load KeyProvider from SSHCredential bytes.
     */
    private static KeyProvider loadKeyProvider(SSHCredential sshCredential) throws java.io.IOException {
        var privateKeyStr = sshCredential.getPrivateKey();
        var passphrase = sshCredential.getPassphrase();

        // Use SSHClient.loadKeys() to load key from string
        try (var tempClient = new SSHClient()) {
            net.schmizz.sshj.userauth.password.PasswordFinder passwordFinder = null;
            if (passphrase != null && !passphrase.isEmpty()) {
                passwordFinder =
                        net.schmizz.sshj.userauth.password.PasswordUtils.createOneOff(passphrase.toCharArray());
            }
            return tempClient.loadKeys(privateKeyStr, null, passwordFinder);
        }
    }
}
