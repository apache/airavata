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
package org.apache.airavata.monitor.cluster;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ClusterStatusMonitorJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJob.class);
    private static ApplicationContext applicationContext;
    
    @Autowired
    private RegistryService registryService;
    
    @Autowired
    private CredentialStoreService credentialStoreService;
    
    @org.springframework.beans.factory.annotation.Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        ClusterStatusMonitorJob.applicationContext = applicationContext;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            String superTenantGatewayId = ServerSettings.getSuperTenantGatewayId();
            // Use injected service, fallback to ApplicationContext if not injected (for Quartz instantiation)
            final RegistryService registryService = (this.registryService != null) 
                ? this.registryService 
                : (applicationContext != null ? applicationContext.getBean(RegistryService.class) : null);
            if (registryService == null) {
                throw new JobExecutionException("RegistryService not available.");
            }
            List<ComputeResourceProfile> computeResourceProfiles = new ArrayList<>();
            List<ComputeResourcePreference> computeResourcePreferences = null;
            try {
                computeResourcePreferences =
                        registryService.getAllGatewayComputeResourcePreferences(superTenantGatewayId);
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
                        computeResourceDescription.getBatchQueues().stream().forEach(q -> {
                            queueNames.add(q.getQueueName());
                        });

                        List<JobSubmissionInterface> jobSubmissionInterfaces =
                                computeResourceDescription.getJobSubmissionInterfaces();
                        if (jobSubmissionInterfaces != null && jobSubmissionInterfaces.size() > 0) {
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
                    } catch (RegistryServiceException e) {
                        logger.error(e.getMessage());
                    }
                });
            }

            ArrayList<QueueStatusModel> queueStatuses = new ArrayList<>();

            for (ComputeResourceProfile computeResourceProfile : computeResourceProfiles) {

                String userName = computeResourceProfile.getUserName();
                String hostName = computeResourceProfile.getHostName();
                int port = computeResourceProfile.getPort();

                try {
                    JSch jsch = new JSch();
                    // Use injected service, fallback to ApplicationContext if not injected
                    CredentialStoreService credentialService = this.credentialStoreService;
                    if (credentialService == null && applicationContext != null) {
                        credentialService = applicationContext.getBean(CredentialStoreService.class);
                    }
                    if (credentialService == null) {
                        throw new JobExecutionException("CredentialStoreService not available.");
                    }
                    SSHCredential sshCredential = credentialService.getSSHCredential(
                            computeResourceProfile.getCredentialStoreToken(), superTenantGatewayId);
                    jsch.addIdentity(
                            hostName,
                            sshCredential.getPrivateKey().getBytes(),
                            sshCredential.getPublicKey().getBytes(),
                            sshCredential.getPassphrase().getBytes());

                    Session session = jsch.getSession(userName, hostName, port);
                    java.util.Properties config = new java.util.Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);

                    logger.debug("Connected to " + hostName);

                    session.connect();
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

                        Channel channel = session.openChannel("exec");
                        ((ChannelExec) channel).setCommand(command);
                        channel.setInputStream(null);
                        ((ChannelExec) channel).setErrStream(System.err);
                        InputStream in = channel.getInputStream();
                        channel.connect();
                        byte[] tmp = new byte[1024];
                        String result = "";
                        while (true) {
                            while (in.available() > 0) {
                                int i = in.read(tmp, 0, 1024);
                                if (i < 0) break;
                                result += new String(tmp, 0, i);
                            }
                            if (channel.isClosed()) {
                                if (in.available() > 0) continue;
                                logger.debug(hostName + " " + queue + " " + "exit-status: " + channel.getExitStatus());
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ee) {
                            }
                        }
                        channel.disconnect();

                        if (result != null && result.length() > 0) {
                            QueueStatusModel queueStatus = null;
                            if (computeResourceProfile.getResourceManagerType().equals("SLURM")) {
                                String[] sparts = result.split(" ");
                                boolean isUp = sparts[0].equalsIgnoreCase("up");
                                String knts = sparts[1];
                                sparts = knts.split("/");
                                int running = Integer.parseInt(sparts[0].trim());
                                int queued = Integer.parseInt(sparts[1].trim());
                                queueStatus = new QueueStatusModel(
                                        hostName, queue, isUp, running, queued, System.currentTimeMillis());

                            } else if (computeResourceProfile
                                    .getResourceManagerType()
                                    .equals("PBS")) {
                                result = result.replaceAll("\\s+", " ");
                                String[] sparts = result.split(" ");
                                boolean isUp = sparts[3].equalsIgnoreCase("yes");
                                int running = Integer.parseInt(sparts[6].trim());
                                int queued = Integer.parseInt(sparts[5].trim());
                                queueStatus = new QueueStatusModel(
                                        hostName, queue, isUp, running, queued, System.currentTimeMillis());
                            }

                            if (queueStatus != null) queueStatuses.add(queueStatus);
                        }
                    }
                    session.disconnect();
                } catch (Exception ex) {
                    logger.error("Failed to get cluster status from " + computeResourceProfile.getHostName());
                    logger.error(ex.getMessage(), ex);
                }
            }
            if (queueStatuses != null && queueStatuses.size() > 0) {
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
}
