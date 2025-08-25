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
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.factory.AiravataServiceFactory;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.model.status.QueueStatusModel;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.thrift.TException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterStatusMonitorJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ClusterStatusMonitorJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            String superTenantGatewayId = ServerSettings.getSuperTenantGatewayId();
            RegistryService.Iface registry = AiravataServiceFactory.getRegistry();
            List<ComputeResourceProfile> computeResourceProfiles = new ArrayList<>();
            List<ComputeResourcePreference> computeResourcePreferences = null;
            try {
                computeResourcePreferences = registry.getAllGatewayComputeResourcePreferences(superTenantGatewayId);
            } catch (Exception ex) {
                logger.warn(
                        "Could not find super tenant compute resources preferences for cluster status monitoring...");
            }
            if (computeResourcePreferences != null && !computeResourcePreferences.isEmpty()) {
                computeResourcePreferences.forEach(p -> {
                    try {
                        String computeResourceId = p.getComputeResourceId();
                        String credentialStoreToken = p.getResourceSpecificCredentialStoreToken();
                        String loginUserName = p.getLoginUserName();
                        String hostName = null;
                        if (credentialStoreToken == null || credentialStoreToken.isEmpty()) {
                            credentialStoreToken = registry.getGatewayResourceProfile(superTenantGatewayId)
                                    .getCredentialStoreToken();
                        }
                        int port = -1;
                        var queueNames = new ArrayList<String>();

                        ComputeResourceDescription computeResourceDescription =
                                registry.getComputeResource(computeResourceId);
                        hostName = computeResourceDescription.getHostName();
                        // FIXME This should come from compute resource description
                        port = 22;
                        computeResourceDescription.getBatchQueues().forEach(q -> {
                            queueNames.add(q.getQueueName());
                        });

                        List<JobSubmissionInterface> jobSubmissionInterfaces =
                                computeResourceDescription.getJobSubmissionInterfaces();
                        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()) {
                            if (jobSubmissionInterfaces
                                    .get(0)
                                    .getJobSubmissionProtocol()
                                    .equals(JobSubmissionProtocol.SSH)) {
                                String resourceManagerType = registry.getSSHJobSubmission(
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
                    } catch (TException e) {
                        logger.error(e.getMessage());
                    }
                });
            }

            ArrayList<QueueStatusModel> queueStatuses = new ArrayList<>();

            for (ComputeResourceProfile computeResourceProfile : computeResourceProfiles) {

                String userName = computeResourceProfile.userName();
                String hostName = computeResourceProfile.hostName();
                int port = computeResourceProfile.port();

                try {
                    JSch jsch = new JSch();
                    CredentialStoreService.Iface credentialStore = AiravataServiceFactory.getCredentialStore();
                    SSHCredential sshCredential = credentialStore.getSSHCredential(
                            computeResourceProfile.credentialStoreToken(), superTenantGatewayId);
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
                    for (String queue : computeResourceProfile.queueNames()) {
                        String command = "";
                        if (computeResourceProfile.resourceManagerType().equals("SLURM"))
                            command = "sinfo -s -p " + queue + " -o \"%a %F\" | tail -1";
                        else if (computeResourceProfile.resourceManagerType().equals("PBS"))
                            command = "qstat -Q " + queue + "| tail -1";

                        if (command.isEmpty()) {
                            logger.warn("No matching resource manager type found for "
                                    + computeResourceProfile.resourceManagerType());
                            continue;
                        }

                        Channel channel = session.openChannel("exec");
                        ((ChannelExec) channel).setCommand(command);
                        channel.setInputStream(null);
                        ((ChannelExec) channel).setErrStream(System.err);
                        InputStream in = channel.getInputStream();
                        channel.connect();
                        byte[] tmp = new byte[1024];
                        StringBuilder result = new StringBuilder();
                        while (true) {
                            while (in.available() > 0) {
                                int i = in.read(tmp, 0, 1024);
                                if (i < 0) break;
                                result.append(new String(tmp, 0, i));
                            }
                            if (channel.isClosed()) {
                                if (in.available() > 0) continue;
                                logger.debug(hostName + " " + queue + " " + "exit-status: " + channel.getExitStatus());
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignored) {
                            }
                        }
                        channel.disconnect();

                        if (!result.isEmpty()) {
                            QueueStatusModel queueStatus = null;
                            switch (computeResourceProfile.resourceManagerType()) {
                                case "SLURM" -> {
                                    String[] sparts = result.toString().split(" ");
                                    boolean isUp = sparts[0].equalsIgnoreCase("up");
                                    String knts = sparts[1];
                                    sparts = knts.split("/");
                                    int running = Integer.parseInt(sparts[0].trim());
                                    int queued = Integer.parseInt(sparts[1].trim());
                                    queueStatus = new QueueStatusModel(
                                            hostName, queue, isUp, running, queued, System.currentTimeMillis());
                                }
                                case "PBS" -> {
                                    result = new StringBuilder(result.toString().replaceAll("\\s+", " "));
                                    String[] sparts = result.toString().split(" ");
                                    boolean isUp = sparts[3].equalsIgnoreCase("yes");
                                    int running = Integer.parseInt(sparts[6].trim());
                                    int queued = Integer.parseInt(sparts[5].trim());
                                    queueStatus = new QueueStatusModel(
                                            hostName, queue, isUp, running, queued, System.currentTimeMillis());
                                }
                            }
                            if (queueStatus != null) {
                                queueStatuses.add(queueStatus);
                            }
                        }
                    }
                    session.disconnect();
                } catch (Exception ex) {
                    logger.error("Failed to get cluster status from " + computeResourceProfile.hostName());
                    logger.error(ex.getMessage(), ex);
                }
            }
            if (!queueStatuses.isEmpty()) {
                registry.registerQueueStatuses(queueStatuses);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private record ComputeResourceProfile(
            String hostName,
            String userName,
            int port,
            String credentialStoreToken,
            List<String> queueNames,
            String resourceManagerType) {}
}
