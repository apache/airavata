/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.gfac.ssh.util;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.ssh.context.SSHAuthWrapper;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.security.TokenizedSSHAuthInfo;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.GSISSHAbstractCluster;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.CorrectiveAction;
import org.apache.airavata.model.workspace.experiment.ErrorCategory;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GFACSSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACSSHUtils.class);

    public static Map<String, List<Cluster>> clusters = new HashMap<String, List<Cluster>>();

    public static int maxClusterCount = 5;

    /**
     * This method is to add computing resource specific authentication, if its a third party machine, use the other addSecurityContext
     * @param jobExecutionContext
     * @throws GFacException
     * @throws ApplicationSettingsException
     */
    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        JobSubmissionProtocol preferredJobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
        JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
        if (preferredJobSubmissionProtocol == JobSubmissionProtocol.GLOBUS || preferredJobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if (preferredJobSubmissionProtocol == JobSubmissionProtocol.SSH) {
            try {
                AppCatalog appCatalog = jobExecutionContext.getAppCatalog();
                SSHJobSubmission sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(preferredJobSubmissionInterface.getJobSubmissionInterfaceId());
                SecurityProtocol securityProtocol = sshJobSubmission.getSecurityProtocol();
                if (securityProtocol == SecurityProtocol.GSI || securityProtocol == SecurityProtocol.SSH_KEYS) {
                    SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
                    String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
                    RequestData requestData = new RequestData(ServerSettings.getDefaultUserGateway());
                    requestData.setTokenId(credentialStoreToken);

                    ServerInfo serverInfo = new ServerInfo(null, jobExecutionContext.getHostName());

                    Cluster pbsCluster = null;
                    try {
                        TokenizedSSHAuthInfo tokenizedSSHAuthInfo = new TokenizedSSHAuthInfo(requestData);
                        String installedParentPath = jobExecutionContext.getResourceJobManager().getJobManagerBinPath();
                        if (installedParentPath == null) {
                            installedParentPath = "/";
                        }

                        SSHCredential credentials = tokenizedSSHAuthInfo.getCredentials();// this is just a call to get and set credentials in to this object,data will be used
                        serverInfo.setUserName(credentials.getPortalUserName());
                        jobExecutionContext.getExperiment().setUserName(credentials.getPortalUserName());
                        // inside the pbsCluser object

                        String key = credentials.getPortalUserName() + jobExecutionContext.getHostName() + serverInfo.getPort();
                        boolean recreate = false;
                        synchronized (clusters) {
                            if (clusters.containsKey(key) && clusters.get(key).size() < maxClusterCount) {
                                recreate = true;
                            } else if (clusters.containsKey(key)) {
                                int i = new Random().nextInt(Integer.MAX_VALUE) % maxClusterCount;
                                if (clusters.get(key).get(i).getSession().isConnected()) {
                                    pbsCluster = clusters.get(key).get(i);
                                } else {
                                    clusters.get(key).remove(i);
                                    recreate = true;
                                }
                                if (!recreate) {
                                    try {
                                        pbsCluster.listDirectory("~/"); // its hard to trust isConnected method, so we try to connect if it works we are good,else we recreate
                                    } catch (Exception e) {
                                        clusters.get(key).remove(i);
                                        logger.info("Connection found the connection map is expired, so we create from the scratch");
                                        maxClusterCount++;
                                        recreate = true; // we make the pbsCluster to create again if there is any exception druing connection
                                    }
                                }
                                logger.info("Re-using the same connection used with the connection string:" + key);
                            } else {
                                recreate = true;
                            }
                            if (recreate) {
                                pbsCluster = new PBSCluster(serverInfo, tokenizedSSHAuthInfo,
                                        CommonUtils.getPBSJobManager(installedParentPath));
                                List<Cluster> pbsClusters = null;
                                if (!(clusters.containsKey(key))) {
                                    pbsClusters = new ArrayList<Cluster>();
                                } else {
                                    pbsClusters = clusters.get(key);
                                }
                                pbsClusters.add(pbsCluster);
                                clusters.put(key, pbsClusters);
                            }
                        }
                    } catch (Exception e) {
                        throw new GFacException("Error occurred...", e);
                    }
                    sshSecurityContext.setPbsCluster(pbsCluster);
                    jobExecutionContext.addSecurityContext(jobExecutionContext.getHostName(), sshSecurityContext);
                }
            } catch (AppCatalogException e) {
                throw new GFacException("Error while getting SSH Submission object from app catalog", e);
            }
        }
    }

    /**
     * This method can be used to add third party resource security contexts
     * @param jobExecutionContext
     * @param sshAuth
     * @throws GFacException
     * @throws ApplicationSettingsException
     */
    public static void addSecurityContext(JobExecutionContext jobExecutionContext,SSHAuthWrapper sshAuth) throws GFacException, ApplicationSettingsException {
        try {
            if(sshAuth== null) {
                throw new GFacException("Error adding security Context, because sshAuthWrapper is null");
            }
            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
            Cluster pbsCluster = null;
            String key=sshAuth.getKey();
            boolean recreate = false;
            synchronized (clusters) {
                if (clusters.containsKey(key) && clusters.get(key).size() < maxClusterCount) {
                    recreate = true;
                } else if (clusters.containsKey(key)) {
                    int i = new Random().nextInt(Integer.MAX_VALUE) % maxClusterCount;
                    if (clusters.get(key).get(i).getSession().isConnected()) {
                        pbsCluster = clusters.get(key).get(i);
                    } else {
                        clusters.get(key).remove(i);
                        recreate = true;
                    }
                    if (!recreate) {
                        try {
                            pbsCluster.listDirectory("~/"); // its hard to trust isConnected method, so we try to connect if it works we are good,else we recreate
                        } catch (Exception e) {
                            clusters.get(key).remove(i);
                            logger.info("Connection found the connection map is expired, so we create from the scratch");
                            maxClusterCount++;
                            recreate = true; // we make the pbsCluster to create again if there is any exception druing connection
                        }
                    }
                    logger.info("Re-using the same connection used with the connection string:" + key);
                } else {
                    recreate = true;
                }
                if (recreate) {
                    pbsCluster = new PBSCluster(sshAuth.getServerInfo(), sshAuth.getAuthenticationInfo(),null);
                    key = sshAuth.getKey();
                    List<Cluster> pbsClusters = null;
                    if (!(clusters.containsKey(key))) {
                        pbsClusters = new ArrayList<Cluster>();
                    } else {
                        pbsClusters = clusters.get(key);
                    }
                    pbsClusters.add(pbsCluster);
                    clusters.put(key, pbsClusters);
                }
            }
            sshSecurityContext.setPbsCluster(pbsCluster);
            jobExecutionContext.addSecurityContext(key, sshSecurityContext);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public static JobDescriptor createJobDescriptor(JobExecutionContext jobExecutionContext, Cluster cluster) {
        JobDescriptor jobDescriptor = new JobDescriptor();
        TaskDetails taskData = jobExecutionContext.getTaskData();
        ResourceJobManager resourceJobManager = jobExecutionContext.getResourceJobManager();
        Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
        if (jobManagerCommands != null && !jobManagerCommands.isEmpty()) {
            for (JobManagerCommand command : jobManagerCommands.keySet()) {
                if (command == JobManagerCommand.SUBMISSION) {
                    String commandVal = jobManagerCommands.get(command);
                    jobDescriptor.setJobSubmitter(commandVal);
                }
            }
        }
        // this is common for any application descriptor
        jobDescriptor.setCallBackIp(ServerSettings.getIp());
        jobDescriptor.setCallBackPort(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.GFAC_SERVER_PORT, "8950"));
        jobDescriptor.setInputDirectory(jobExecutionContext.getInputDir());
        jobDescriptor.setOutputDirectory(jobExecutionContext.getOutputDir());
        jobDescriptor.setExecutablePath(jobExecutionContext.getApplicationContext()
                .getApplicationDeploymentDescription().getExecutablePath());
        jobDescriptor.setStandardOutFile(jobExecutionContext.getStandardOutput());
        jobDescriptor.setStandardErrorFile(jobExecutionContext.getStandardError());
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE);
        jobDescriptor.setJobName(String.valueOf(i + 99999999));
        jobDescriptor.setWorkingDirectory(jobExecutionContext.getWorkingDir());
        List<String> inputValues = new ArrayList<String>();
        MessageContext input = jobExecutionContext.getInMessageContext();
        Map<String, Object> inputs = input.getParameters();
        Set<String> keys = inputs.keySet();
        for (String paramName : keys) {
            InputDataObjectType inputDataObjectType = (InputDataObjectType) inputs.get(paramName);
            inputValues.add(inputDataObjectType.getValue());
        }
        jobDescriptor.setInputValues(inputValues);
        jobDescriptor.setUserName(((GSISSHAbstractCluster) cluster).getServerInfo().getUserName());
        jobDescriptor.setShellName("/bin/bash");
        jobDescriptor.setAllEnvExport(true);
        jobDescriptor.setMailOptions("n");
        jobDescriptor.setOwner(((PBSCluster) cluster).getServerInfo().getUserName());

        ComputationalResourceScheduling taskScheduling = taskData.getTaskScheduling();
        if (taskScheduling != null) {
            int totalNodeCount = taskScheduling.getNodeCount();
            int totalCPUCount = taskScheduling.getTotalCPUCount();


            if (taskScheduling.getComputationalProjectAccount() != null) {
                jobDescriptor.setAcountString(taskScheduling.getComputationalProjectAccount());
            }
            if (taskScheduling.getQueueName() != null) {
                jobDescriptor.setQueueName(taskScheduling.getQueueName());
            }

            if (totalNodeCount > 0) {
                jobDescriptor.setNodes(totalNodeCount);
            }
            if (taskScheduling.getComputationalProjectAccount() != null) {
                jobDescriptor.setAcountString(taskScheduling.getComputationalProjectAccount());
            }
            if (taskScheduling.getQueueName() != null) {
                jobDescriptor.setQueueName(taskScheduling.getQueueName());
            }
            if (totalCPUCount > 0) {
                int ppn = totalCPUCount / totalNodeCount;
                jobDescriptor.setProcessesPerNode(ppn);
                jobDescriptor.setCPUCount(totalCPUCount);
            }
            if (taskScheduling.getWallTimeLimit() > 0) {
                jobDescriptor.setMaxWallTime(String.valueOf(taskScheduling.getWallTimeLimit()));
            }
        } else {
            logger.error("Task scheduling cannot be null at this point..");
        }
        return jobDescriptor;
    }

    /**
     * This method can be used to set the Security Context if its not set and later use it in other places
     * @param jobExecutionContext
     * @param authenticationInfo
     * @param userName
     * @param hostName
     * @param port
     * @return
     * @throws GFacException
     */
    public static String prepareSecurityContext(JobExecutionContext jobExecutionContext, AuthenticationInfo authenticationInfo
            , String userName, String hostName, int port) throws GFacException {
        ServerInfo serverInfo = new ServerInfo(userName, hostName);
        String key = userName+hostName+port;
        SSHAuthWrapper sshAuthWrapper = new SSHAuthWrapper(serverInfo, authenticationInfo, key);
        if (jobExecutionContext.getSecurityContext(key) == null) {
            try {
                GFACSSHUtils.addSecurityContext(jobExecutionContext, sshAuthWrapper);
            } catch (ApplicationSettingsException e) {
                logger.error(e.getMessage());
                try {
                    GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
                } catch (GFacException e1) {
                    logger.error(e1.getLocalizedMessage());
                }
                throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
            }
        }
        return key;
    }

}
