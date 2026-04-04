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
package org.apache.airavata.orchestration.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.interfaces.CredentialProvider;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.apache.airavata.interfaces.SSHConnectionService.*;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.apache.airavata.model.status.proto.QueueStatusModel;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterStatusMonitorJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJob.class);

    private final CredentialProvider credentialProvider;
    private final SSHConnectionService sshConnectionService;

    public ClusterStatusMonitorJob(CredentialProvider credentialProvider, SSHConnectionService sshConnectionService) {
        this.credentialProvider = credentialProvider;
        this.sshConnectionService = sshConnectionService;
    }

    @Override
    public void run() {
        try {
            String superTenantGatewayId = ServerSettings.getSuperTenantGatewayId();
            RegistryHandler registryClient = getRegistryClient();
            List<ComputeResourceProfile> computeResourceProfiles = new ArrayList<>();
            List<ComputeResourcePreference> computeResourcePreferences = null;
            try {
                computeResourcePreferences =
                        registryClient.getAllGatewayComputeResourcePreferences(superTenantGatewayId);
            } catch (Exception ex) {
                logger.warn(
                        "Could not find super tenant compute resources preferences for cluster status monitoring...");
            }
            if (computeResourcePreferences != null && computeResourcePreferences.size() > 0) {
                computeResourcePreferences.stream().forEach(p -> {
                    try {
                        String computeResourceId = p.getComputeResourceId();
                        String credentialStoreToken = p.getResourceSpecificCredentialStoreToken();
                        String loginUserName = p.getLoginUserName();
                        String hostName = null;
                        if (credentialStoreToken == null || credentialStoreToken.equals("")) {
                            credentialStoreToken = registryClient
                                    .getGatewayResourceProfile(superTenantGatewayId)
                                    .getCredentialStoreToken();
                        }
                        int port = -1;
                        ArrayList queueNames = new ArrayList<>();

                        ComputeResourceDescription computeResourceDescription =
                                registryClient.getComputeResource(computeResourceId);
                        hostName = computeResourceDescription.getHostName();
                        // FIXME This should come from compute resource description
                        port = 22;
                        computeResourceDescription.getBatchQueuesList().stream().forEach(q -> {
                            queueNames.add(q.getQueueName());
                        });

                        List<JobSubmissionInterface> jobSubmissionInterfaces =
                                computeResourceDescription.getJobSubmissionInterfacesList();
                        if (jobSubmissionInterfaces != null && jobSubmissionInterfaces.size() > 0) {
                            if (jobSubmissionInterfaces
                                    .get(0)
                                    .getJobSubmissionProtocol()
                                    .equals(JobSubmissionProtocol.SSH)) {
                                String resourceManagerType = registryClient
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
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                });
            }

            ArrayList<QueueStatusModel> queueStatuses = new ArrayList<>();

            for (ComputeResourceProfile computeResourceProfile : computeResourceProfiles) {

                String userName = computeResourceProfile.getUserName();
                String hostName = computeResourceProfile.getHostName();
                int port = computeResourceProfile.getPort();

                SSHConnection sshConnection = null;
                try {
                    CredentialProvider credentialClient = credentialProvider;
                    SSHCredential sshCredential = credentialClient.getSSHCredential(
                            computeResourceProfile.getCredentialStoreToken(), superTenantGatewayId);

                    sshConnection = sshConnectionService.connectSimple(
                            hostName,
                            port,
                            userName,
                            sshCredential.getPublicKey(),
                            sshCredential.getPrivateKey(),
                            sshCredential.getPassphrase());

                    logger.debug("Connected to " + hostName);

                    for (String queue : computeResourceProfile.getQueueNames()) {
                        String command = "";
                        if (computeResourceProfile.getResourceManagerType().equals("SLURM"))
                            command = "sinfo -s -p " + queue + " -o \"%a %F\" | tail -1";
                        else if (computeResourceProfile.getResourceManagerType().equals("PBS"))
                            command = "qstat -Q " + queue + "| tail -1";

                        if (command.equals("")) {
                            logger.warn("No matching resource manager type found for "
                                    + computeResourceProfile.getResourceManagerType());
                            continue;
                        }

                        try (SSHSession session = sshConnection.startSession()) {
                            SSHCommandResult cmd = session.exec(command);
                            String result = IOUtils.toString(cmd.getInputStream(), "UTF-8");
                            cmd.join(30, TimeUnit.SECONDS);
                            int exitStatus = cmd.getExitStatus();
                            logger.debug(hostName + " " + queue + " " + "exit-status: " + exitStatus);

                            if (result != null && result.length() > 0) {
                                QueueStatusModel queueStatus = null;
                                if (computeResourceProfile
                                        .getResourceManagerType()
                                        .equals("SLURM")) {
                                    String[] sparts = result.trim().split(" ");
                                    boolean isUp = sparts[0].equalsIgnoreCase("up");
                                    String knts = sparts[1];
                                    sparts = knts.split("/");
                                    int running = Integer.parseInt(sparts[0].trim());
                                    int queued = Integer.parseInt(sparts[1].trim());
                                    queueStatus = QueueStatusModel.newBuilder()
                                            .setHostName(hostName)
                                            .setQueueName(queue)
                                            .setQueueUp(isUp)
                                            .setRunningJobs(running)
                                            .setQueuedJobs(queued)
                                            .setTime(System.currentTimeMillis())
                                            .build();

                                } else if (computeResourceProfile
                                        .getResourceManagerType()
                                        .equals("PBS")) {
                                    result = result.replaceAll("\\s+", " ");
                                    String[] sparts = result.split(" ");
                                    boolean isUp = sparts[3].equalsIgnoreCase("yes");
                                    int running = Integer.parseInt(sparts[6].trim());
                                    int queued = Integer.parseInt(sparts[5].trim());
                                    queueStatus = QueueStatusModel.newBuilder()
                                            .setHostName(hostName)
                                            .setQueueName(queue)
                                            .setQueueUp(isUp)
                                            .setRunningJobs(running)
                                            .setQueuedJobs(queued)
                                            .setTime(System.currentTimeMillis())
                                            .build();
                                }

                                if (queueStatus != null) queueStatuses.add(queueStatus);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Failed to get cluster status from " + computeResourceProfile.getHostName());
                    logger.error(ex.getMessage(), ex);
                } finally {
                    if (sshConnection != null) {
                        try {
                            sshConnection.disconnect();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
            if (queueStatuses != null && queueStatuses.size() > 0) {
                registryClient.registerQueueStatuses(queueStatuses);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cluster status monitoring failed", e);
        }
    }

    private static RegistryHandler getRegistryClient() {
        return SchedulerUtils.getRegistryHandler();
    }

    private CredentialProvider getCredentialProvider() {
        return credentialProvider;
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

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getCredentialStoreToken() {
            return credentialStoreToken;
        }

        public void setCredentialStoreToken(String credentialStoreToken) {
            this.credentialStoreToken = credentialStoreToken;
        }

        public List<String> getQueueNames() {
            return queueNames;
        }

        public void setQueueNames(List<String> queueNames) {
            this.queueNames = queueNames;
        }

        public String getResourceManagerType() {
            return resourceManagerType;
        }

        public void setResourceManagerType(String resourceManagerType) {
            this.resourceManagerType = resourceManagerType;
        }
    }
}
