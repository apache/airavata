///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
//*/
//package org.apache.airavata.gfac.ssh.util;
//
//import org.apache.airavata.gfac.core.cluster.RemoteCluster;
//import org.apache.airavata.gfac.impl.HPCRemoteCluster;
//import org.apache.airavata.registry.cpi.AppCatalog;
//import org.apache.airavata.registry.cpi.AppCatalogException;
//import org.apache.airavata.common.exception.ApplicationSettingsException;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
//import org.apache.airavata.gfac.core.GFacConstants;
//import org.apache.airavata.gfac.core.GFacException;
//import org.apache.airavata.gfac.core.RequestData;
//import org.apache.airavata.gfac.core.JobDescriptor;
//import org.apache.airavata.gfac.core.JobManagerConfiguration;
//import org.apache.airavata.gfac.core.cluster.ServerInfo;
//import org.apache.airavata.gfac.core.context.JobExecutionContext;
//import org.apache.airavata.gfac.core.context.MessageContext;
//import org.apache.airavata.gfac.core.handler.GFacHandlerException;
//import org.apache.airavata.gfac.core.GFacUtils;
//import org.apache.airavata.gfac.gsi.ssh.impl.GSISSHAbstractCluster;
//import org.apache.airavata.gfac.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
//import org.apache.airavata.gfac.gsi.ssh.util.CommonUtils;
//import org.apache.airavata.gfac.ssh.context.SSHAuthWrapper;
//import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
//import org.apache.airavata.gfac.ssh.security.TokenizedSSHAuthInfo;
//import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
//import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
//import org.apache.airavata.model.appcatalog.appdeployment.ApplicationParallelismType;
//import org.apache.airavata.model.appcatalog.appinterface.DataType;
//import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
//import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
//import org.apache.airavata.model.appcatalog.computeresource.*;
//import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
//import org.apache.airavata.model.experiment.ComputationalResourceScheduling;
//import org.apache.airavata.model.experiment.CorrectiveAction;
//import org.apache.airavata.model.experiment.ErrorCategory;
//import org.apache.airavata.model.experiment.TaskDetails;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.util.*;
//
//public class GFACSSHUtils {
//    private final static Logger logger = LoggerFactory.getLogger(GFACSSHUtils.class);
//
//    public static Map<String, List<RemoteCluster>> clusters = new HashMap<String, List<RemoteCluster>>();
//
//    public static final String PBS_JOB_MANAGER = "pbs";
//    public static final String SLURM_JOB_MANAGER = "slurm";
//    public static final String SUN_GRID_ENGINE_JOB_MANAGER = "UGE";
//    public static final String LSF_JOB_MANAGER = "LSF";
//
//    public static int maxClusterCount = 5;
//
//    /**
//     * This method is to add computing resource specific authentication, if its a third party machine, use the other addSecurityContext
//     * @param jobExecutionContext
//     * @throws GFacException
//     * @throws ApplicationSettingsException
//     */
//    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
//        JobSubmissionProtocol preferredJobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
//        JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
//        if (preferredJobSubmissionProtocol == JobSubmissionProtocol.GLOBUS || preferredJobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
//            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
//        } else if (preferredJobSubmissionProtocol == JobSubmissionProtocol.SSH) {
//            try {
//                AppCatalog appCatalog = jobExecutionContext.getAppCatalog();
//                SSHJobSubmission sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(preferredJobSubmissionInterface.getJobSubmissionInterfaceId());
//                SecurityProtocol securityProtocol = sshJobSubmission.getSecurityProtocol();
//                if (securityProtocol == SecurityProtocol.GSI || securityProtocol == SecurityProtocol.SSH_KEYS) {
//                    SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
//                    String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
//                    RequestData requestData = new RequestData(jobExecutionContext.getGatewayID());
//                    requestData.setTokenId(credentialStoreToken);
//
//                    ServerInfo serverInfo = new ServerInfo(null, jobExecutionContext.getHostName());
//
//                    RemoteCluster pbsRemoteCluster = null;
//                    try {
//                        AuthenticationInfo tokenizedSSHAuthInfo = new TokenizedSSHAuthInfo(requestData);
//                        String installedParentPath = jobExecutionContext.getResourceJobManager().getJobManagerBinPath();
//                        if (installedParentPath == null) {
//                            installedParentPath = "/";
//                        }
//
//                        SSHCredential credentials =((TokenizedSSHAuthInfo)tokenizedSSHAuthInfo).getCredentials();// this is just a call to get and set credentials in to this object,data will be used
//                        if(credentials.getPrivateKey()==null || credentials.getPublicKey()==null){
//                            // now we fall back to username password authentication
//                            Properties configurationProperties = ServerSettings.getProperties();
//                            tokenizedSSHAuthInfo = new DefaultPasswordAuthenticationInfo(configurationProperties.getProperty(GFacConstants.SSH_PASSWORD));
//                        }
//                        // This should be the login user name from compute resource preference
//                        String loginUser = jobExecutionContext.getLoginUserName();
//                        if (loginUser == null) {
//                            loginUser = credentials.getPortalUserName();
//                        }
//                        serverInfo.setUserName(loginUser);
//                        jobExecutionContext.getExperiment().setUserName(loginUser);
//
//
//                        // inside the pbsCluser object
//
//                        String key = loginUser + jobExecutionContext.getHostName() + serverInfo.getPort();
//                        boolean recreate = false;
//                        synchronized (clusters) {
//                            if (clusters.containsKey(key) && clusters.get(key).size() < maxClusterCount) {
//                                recreate = true;
//                            } else if (clusters.containsKey(key)) {
//                                int i = new Random().nextInt(Integer.MAX_VALUE) % maxClusterCount;
//                                if (clusters.get(key).get(i).getSession().isConnected()) {
//                                    pbsRemoteCluster = clusters.get(key).get(i);
//                                } else {
//                                    clusters.get(key).remove(i);
//                                    recreate = true;
//                                }
//                                if (!recreate) {
//                                    try {
//                                        pbsRemoteCluster.listDirectory("~/"); // its hard to trust isConnected method, so we try to connect if it works we are good,else we recreate
//                                    } catch (Exception e) {
//                                        clusters.get(key).remove(i);
//                                        logger.info("Connection found the connection map is expired, so we create from the scratch");
//                                        maxClusterCount++;
//                                        recreate = true; // we make the pbsRemoteCluster to create again if there is any exception druing connection
//                                    }
//                                }
//                                logger.info("Re-using the same connection used with the connection string:" + key);
//                            } else {
//                                recreate = true;
//                            }
//                            if (recreate) {
//                            	 JobManagerConfiguration jConfig = null;
//                                 String jobManager = sshJobSubmission.getResourceJobManager().getResourceJobManagerType().toString();
//                                 if (jobManager == null) {
//                                     logger.error("No Job Manager is configured, so we are picking pbs as the default job manager");
//                                     jConfig = CommonUtils.getPBSJobManager(installedParentPath);
//                                 } else {
//                                     if (PBS_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//                                         jConfig = CommonUtils.getPBSJobManager(installedParentPath);
//                                     } else if (SLURM_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//                                         jConfig = CommonUtils.getSLURMJobManager(installedParentPath);
//                                     } else if (SUN_GRID_ENGINE_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//                                         jConfig = CommonUtils.getUGEJobManager(installedParentPath);
//                                     } else if (LSF_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//                                         jConfig = CommonUtils.getLSFJobManager(installedParentPath);
//                                     }
//                                 }
//
//                                pbsRemoteCluster = new HPCRemoteCluster(serverInfo, tokenizedSSHAuthInfo,jConfig);
//                                List<RemoteCluster> pbsRemoteClusters = null;
//                                if (!(clusters.containsKey(key))) {
//                                    pbsRemoteClusters = new ArrayList<RemoteCluster>();
//                                } else {
//                                    pbsRemoteClusters = clusters.get(key);
//                                }
//                                pbsRemoteClusters.add(pbsRemoteCluster);
//                                clusters.put(key, pbsRemoteClusters);
//                            }
//                        }
//                    } catch (Exception e) {
//                        throw new GFacException("Error occurred...", e);
//                    }
//                    sshSecurityContext.setRemoteCluster(pbsRemoteCluster);
//                    jobExecutionContext.addSecurityContext(jobExecutionContext.getHostName(), sshSecurityContext);
//                }
//            } catch (AppCatalogException e) {
//                throw new GFacException("Error while getting SSH Submission object from app catalog", e);
//            }
//        }
//    }
//
//    /**
//     * This method can be used to add third party resource security contexts
//     * @param jobExecutionContext
//     * @param sshAuth
//     * @throws GFacException
//     * @throws ApplicationSettingsException
//     */
//    public static void addSecurityContext(JobExecutionContext jobExecutionContext,SSHAuthWrapper sshAuth) throws GFacException, ApplicationSettingsException {
//        try {
//            if(sshAuth== null) {
//                throw new GFacException("Error adding security Context, because sshAuthWrapper is null");
//            }
//            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
//            AppCatalog appCatalog = jobExecutionContext.getAppCatalog();
//            JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
//            SSHJobSubmission sshJobSubmission = null;
//			try {
//				sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(preferredJobSubmissionInterface.getJobSubmissionInterfaceId());
//			} catch (Exception e1) {
//				 logger.error("Not able to get SSHJobSubmission from registry");
//			}
//
//            RemoteCluster pbsRemoteCluster = null;
//            String key=sshAuth.getKey();
//            boolean recreate = false;
//            synchronized (clusters) {
//                if (clusters.containsKey(key) && clusters.get(key).size() < maxClusterCount) {
//                    recreate = true;
//                } else if (clusters.containsKey(key)) {
//                    int i = new Random().nextInt(Integer.MAX_VALUE) % maxClusterCount;
//                    if (clusters.get(key).get(i).getSession().isConnected()) {
//                        pbsRemoteCluster = clusters.get(key).get(i);
//                    } else {
//                        clusters.get(key).remove(i);
//                        recreate = true;
//                    }
//                    if (!recreate) {
//                        try {
//                            pbsRemoteCluster.listDirectory("~/"); // its hard to trust isConnected method, so we try to connect if it works we are good,else we recreate
//                        } catch (Exception e) {
//                            clusters.get(key).remove(i);
//                            logger.info("Connection found the connection map is expired, so we create from the scratch");
//                            maxClusterCount++;
//                            recreate = true; // we make the pbsRemoteCluster to create again if there is any exception druing connection
//                        }
//                    }
//                    logger.info("Re-using the same connection used with the connection string:" + key);
//                } else {
//                    recreate = true;
//                }
//                if (recreate) {
//               	 JobManagerConfiguration jConfig = null;
//               	 String installedParentPath = null;
//               	 if(jobExecutionContext.getResourceJobManager()!= null){
//               		installedParentPath = jobExecutionContext.getResourceJobManager().getJobManagerBinPath();
//               	 }
//                 if (installedParentPath == null) {
//                     installedParentPath = "/";
//                 }
//					if (sshJobSubmission != null) {
//						String jobManager = sshJobSubmission.getResourceJobManager().getResourceJobManagerType().toString();
//						if (jobManager == null) {
//							logger.error("No Job Manager is configured, so we are picking pbs as the default job manager");
//							jConfig = CommonUtils.getPBSJobManager(installedParentPath);
//						} else {
//							if (PBS_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//								jConfig = CommonUtils.getPBSJobManager(installedParentPath);
//							} else if (SLURM_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//								jConfig = CommonUtils.getSLURMJobManager(installedParentPath);
//							} else if (SUN_GRID_ENGINE_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
//								jConfig = CommonUtils.getUGEJobManager(installedParentPath);
//							} else if (LSF_JOB_MANAGER.equals(jobManager)) {
//								jConfig = CommonUtils.getLSFJobManager(installedParentPath);
//							}
//						}
//					}
//                    pbsRemoteCluster = new HPCRemoteCluster(sshAuth.getServerInfo(), sshAuth.getAuthenticationInfo(),jConfig);
//                    key = sshAuth.getKey();
//                    List<RemoteCluster> pbsRemoteClusters = null;
//                    if (!(clusters.containsKey(key))) {
//                        pbsRemoteClusters = new ArrayList<RemoteCluster>();
//                    } else {
//                        pbsRemoteClusters = clusters.get(key);
//                    }
//                    pbsRemoteClusters.add(pbsRemoteCluster);
//                    clusters.put(key, pbsRemoteClusters);
//                }
//            }
//            sshSecurityContext.setRemoteCluster(pbsRemoteCluster);
//            jobExecutionContext.addSecurityContext(key, sshSecurityContext);
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new GFacException("Error adding security Context", e);
//        }
//    }
//
//
//    public static JobDescriptor createJobDescriptor(JobExecutionContext jobExecutionContext, RemoteCluster remoteCluster) throws AppCatalogException, ApplicationSettingsException {
//        JobDescriptor jobDescriptor = new JobDescriptor();
//        TaskDetails taskData = jobExecutionContext.getTaskData();
//
//
//        // set email based job monitoring email  address if monitor mode is JOB_EMAIL_NOTIFICATION_MONITOR
//        boolean addJobNotifMail = isEmailBasedJobMonitor(jobExecutionContext);
//        String emailIds = null;
//        if (addJobNotifMail) {
//            emailIds = ServerSettings.getEmailBasedMonitorAddress();
//        }
//        // add all configured job notification email addresses.
//        if (ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_ENABLE).equalsIgnoreCase("true")) {
//            String flags = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_FLAGS);
//            if (flags != null && jobExecutionContext.getApplicationContext().getComputeResourceDescription().getHostName().equals("stampede.tacc.xsede.org")) {
//                flags = "ALL";
//            }
//            jobDescriptor.setMailOptions(flags);
//
//            String userJobNotifEmailIds = ServerSettings.getSetting(ServerSettings.JOB_NOTIFICATION_EMAILIDS);
//            if (userJobNotifEmailIds != null && !userJobNotifEmailIds.isEmpty()) {
//                if (emailIds != null && !emailIds.isEmpty()) {
//                    emailIds += ("," + userJobNotifEmailIds);
//                } else {
//                    emailIds = userJobNotifEmailIds;
//                }
//            }
//
//            if (taskData.isEnableEmailNotification()) {
//                List<String> emailList = jobExecutionContext.getTaskData().getEmailAddresses();
//                String elist = GFacUtils.listToCsv(emailList, ',');
//                if (elist != null && !elist.isEmpty()) {
//                    if (emailIds != null && !emailIds.isEmpty()) {
//                        emailIds = emailIds + "," + elist;
//                    } else {
//                        emailIds = elist;
//                    }
//                }
//            }
//        }
//        if (emailIds != null && !emailIds.isEmpty()) {
//            logger.info("Email list: " + emailIds);
//            jobDescriptor.setMailAddress(emailIds);
//        }
//        // this is common for any application descriptor
//
//        jobDescriptor.setCallBackIp(ServerSettings.getIp());
//        jobDescriptor.setCallBackPort(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.GFAC_SERVER_PORT, "8950"));
//        jobDescriptor.setInputDirectory(jobExecutionContext.getInputDir());
//        jobDescriptor.setOutputDirectory(jobExecutionContext.getOutputDir());
//        jobDescriptor.setExecutablePath(jobExecutionContext.getApplicationContext()
//                .getApplicationDeploymentDescription().getExecutablePath());
//        jobDescriptor.setStandardOutFile(jobExecutionContext.getStandardOutput());
//        jobDescriptor.setStandardErrorFile(jobExecutionContext.getStandardError());
//        String computationalProjectAccount = taskData.getTaskScheduling().getComputationalProjectAccount();
//        if (computationalProjectAccount == null){
//            ComputeResourcePreference computeResourcePreference = jobExecutionContext.getApplicationContext().getComputeResourcePreference();
//            if (computeResourcePreference != null) {
//                computationalProjectAccount = computeResourcePreference.getAllocationProjectNumber();
//            }
//        }
//        if (computationalProjectAccount != null) {
//            jobDescriptor.setAcountString(computationalProjectAccount);
//        }
//        // To make job name alpha numeric
//        jobDescriptor.setJobName("A" + String.valueOf(generateJobName()));
//        jobDescriptor.setWorkingDirectory(jobExecutionContext.getWorkingDir());
//
//        List<String> inputValues = new ArrayList<String>();
//        MessageContext input = jobExecutionContext.getInMessageContext();
//
//        // sort the inputs first and then build the command ListR
//        Comparator<InputDataObjectType> inputOrderComparator = new Comparator<InputDataObjectType>() {
//            @Override
//            public int compare(InputDataObjectType inputDataObjectType, InputDataObjectType t1) {
//                return inputDataObjectType.getInputOrder() - t1.getInputOrder();
//            }
//        };
//        Set<InputDataObjectType> sortedInputSet = new TreeSet<InputDataObjectType>(inputOrderComparator);
//        for (Object object : input.getParameters().values()) {
//            if (object instanceof InputDataObjectType) {
//                InputDataObjectType inputDOT = (InputDataObjectType) object;
//                sortedInputSet.add(inputDOT);
//            }
//        }
//        for (InputDataObjectType inputDataObjectType : sortedInputSet) {
//            if (!inputDataObjectType.isRequiredToAddedToCommandLine()) {
//                continue;
//            }
//            if (inputDataObjectType.getApplicationArgument() != null
//                    && !inputDataObjectType.getApplicationArgument().equals("")) {
//                inputValues.add(inputDataObjectType.getApplicationArgument());
//            }
//
//            if (inputDataObjectType.getValue() != null
//                    && !inputDataObjectType.getValue().equals("")) {
//                if (inputDataObjectType.getType() == DataType.URI) {
//                    // set only the relative path
//                    String filePath = inputDataObjectType.getValue();
//                    filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
//                    inputValues.add(filePath);
//                }else {
//                    inputValues.add(inputDataObjectType.getValue());
//                }
//
//            }
//        }
//        Map<String, Object> outputParams = jobExecutionContext.getOutMessageContext().getParameters();
//        for (Object outputParam : outputParams.values()) {
//            if (outputParam instanceof OutputDataObjectType) {
//                OutputDataObjectType output = (OutputDataObjectType) outputParam;
//                if (output.getApplicationArgument() != null
//                        && !output.getApplicationArgument().equals("")) {
//                    inputValues.add(output.getApplicationArgument());
//                }
//                if (output.getValue() != null && !output.getValue().equals("") && output.isRequiredToAddedToCommandLine()) {
//                    if (output.getType() == DataType.URI){
//                        String filePath = output.getValue();
//                        filePath = filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1, filePath.length());
//                        inputValues.add(filePath);
//                    }
//                }
//            }
//        }
//
//        jobDescriptor.setInputValues(inputValues);
//        jobDescriptor.setUserName(((GSISSHAbstractCluster) remoteCluster).getServerInfo().getUserName());
//        jobDescriptor.setShellName("/bin/bash");
//        jobDescriptor.setAllEnvExport(true);
//        jobDescriptor.setOwner(((HPCRemoteCluster) remoteCluster).getServerInfo().getUserName());
//
//        ResourceJobManager resourceJobManager = jobExecutionContext.getResourceJobManager();
//
//
//        ComputationalResourceScheduling taskScheduling = taskData.getTaskScheduling();
//        if (taskScheduling != null) {
//            int totalNodeCount = taskScheduling.getNodeCount();
//            int totalCPUCount = taskScheduling.getTotalCPUCount();
//
//
//            if (taskScheduling.getComputationalProjectAccount() != null) {
//                jobDescriptor.setAcountString(taskScheduling.getComputationalProjectAccount());
//            }
//            if (taskScheduling.getQueueName() != null) {
//                jobDescriptor.setQueueName(taskScheduling.getQueueName());
//            }
//
//            if (totalNodeCount > 0) {
//                jobDescriptor.setNodes(totalNodeCount);
//            }
//            if (taskScheduling.getComputationalProjectAccount() != null) {
//                jobDescriptor.setAcountString(taskScheduling.getComputationalProjectAccount());
//            }
//            if (taskScheduling.getQueueName() != null) {
//                jobDescriptor.setQueueName(taskScheduling.getQueueName());
//            }
//            if (totalCPUCount > 0) {
//                int ppn = totalCPUCount / totalNodeCount;
//                jobDescriptor.setProcessesPerNode(ppn);
//                jobDescriptor.setCPUCount(totalCPUCount);
//            }
//            if (taskScheduling.getWallTimeLimit() > 0) {
//                jobDescriptor.setMaxWallTime(String.valueOf(taskScheduling.getWallTimeLimit()));
//                if(resourceJobManager.getResourceJobManagerType().equals(ResourceJobManagerType.LSF)){
//                    jobDescriptor.setMaxWallTimeForLSF(String.valueOf(taskScheduling.getWallTimeLimit()));
//                }
//            }
//            if (taskScheduling.getTotalPhysicalMemory() > 0) {
//                jobDescriptor.setUsedMemory(taskScheduling.getTotalPhysicalMemory() + "");
//            }
//        } else {
//            logger.error("Task scheduling cannot be null at this point..");
//        }
//        ApplicationDeploymentDescription appDepDescription = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription();
//        List<String> moduleCmds = appDepDescription.getModuleLoadCmds();
//        if (moduleCmds != null) {
//            for (String moduleCmd : moduleCmds) {
//                jobDescriptor.addModuleLoadCommands(moduleCmd);
//            }
//        }
//        List<String> preJobCommands = appDepDescription.getPreJobCommands();
//        if (preJobCommands != null) {
//            for (String preJobCommand : preJobCommands) {
//                jobDescriptor.addPreJobCommand(parseCommand(preJobCommand, jobExecutionContext));
//            }
//        }
//
//        List<String> postJobCommands = appDepDescription.getPostJobCommands();
//        if (postJobCommands != null) {
//            for (String postJobCommand : postJobCommands) {
//                jobDescriptor.addPostJobCommand(parseCommand(postJobCommand, jobExecutionContext));
//            }
//        }
//
//        ApplicationParallelismType parallelism = appDepDescription.getParallelism();
//        if (parallelism != null){
//            if (parallelism == ApplicationParallelismType.MPI || parallelism == ApplicationParallelismType.OPENMP || parallelism == ApplicationParallelismType.OPENMP_MPI){
//                Map<JobManagerCommand, String> jobManagerCommands = resourceJobManager.getJobManagerCommands();
//                if (jobManagerCommands != null && !jobManagerCommands.isEmpty()) {
//                    for (JobManagerCommand command : jobManagerCommands.keySet()) {
//                        if (command == JobManagerCommand.SUBMISSION) {
//                            String commandVal = jobManagerCommands.get(command);
//                            jobDescriptor.setJobSubmitter(commandVal);
//                        }
//                    }
//                }
//            }
//        }
//        return jobDescriptor;
//    }
//
//    public static boolean isEmailBasedJobMonitor(JobExecutionContext jobExecutionContext) throws AppCatalogException {
//        if (jobExecutionContext.getPreferredJobSubmissionProtocol() == JobSubmissionProtocol.SSH) {
//            String jobSubmissionInterfaceId = jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionInterfaceId();
//            SSHJobSubmission sshJobSubmission = jobExecutionContext.getAppCatalog().getComputeResource().getSSHJobSubmission(jobSubmissionInterfaceId);
//            MonitorMode monitorMode = sshJobSubmission.getMonitorMode();
//            return monitorMode != null && monitorMode == MonitorMode.JOB_EMAIL_NOTIFICATION_MONITOR;
//        } else {
//            return false;
//        }
//    }
//
//    private static int generateJobName() {
//        Random random = new Random();
//        int i = random.nextInt(Integer.MAX_VALUE);
//        i = i + 99999999;
//        if(i<0) {
//            i = i * (-1);
//        }
//        return i;
//    }
//
//    private static String parseCommand(String value, JobExecutionContext jobExecutionContext) {
//        String parsedValue = value.replaceAll("\\$workingDir", jobExecutionContext.getWorkingDir());
//        parsedValue = parsedValue.replaceAll("\\$inputDir", jobExecutionContext.getInputDir());
//        parsedValue = parsedValue.replaceAll("\\$outputDir", jobExecutionContext.getOutputDir());
//        return parsedValue;
//    }
//    /**
//     * This method can be used to set the Security Context if its not set and later use it in other places
//     * @param jobExecutionContext
//     * @param authenticationInfo
//     * @param userName
//     * @param hostName
//     * @param port
//     * @return
//     * @throws GFacException
//     */
//    public static String prepareSecurityContext(JobExecutionContext jobExecutionContext, AuthenticationInfo authenticationInfo
//            , String userName, String hostName, int port) throws GFacException {
//        ServerInfo serverInfo = new ServerInfo(userName, hostName);
//        String key = userName+hostName+port;
//        SSHAuthWrapper sshAuthWrapper = new SSHAuthWrapper(serverInfo, authenticationInfo, key);
//        if (jobExecutionContext.getSecurityContext(key) == null) {
//            try {
//                GFACSSHUtils.addSecurityContext(jobExecutionContext, sshAuthWrapper);
//            } catch (ApplicationSettingsException e) {
//                logger.error(e.getMessage());
//                try {
//                    StringWriter errors = new StringWriter();
//                    e.printStackTrace(new PrintWriter(errors));
//                    GFacUtils.saveErrorDetails(jobExecutionContext, errors.toString(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR);
//                } catch (GFacException e1) {
//                    logger.error(e1.getLocalizedMessage());
//                }
//                throw new GFacHandlerException("Error while creating SSHSecurityContext", e, e.getLocalizedMessage());
//            }
//        }
//        return key;
//    }
//}
