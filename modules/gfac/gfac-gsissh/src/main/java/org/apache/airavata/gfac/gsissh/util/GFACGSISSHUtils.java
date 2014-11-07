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
package org.apache.airavata.gfac.gsissh.util;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.credential.store.credential.impl.certificate.CertificateCredential;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.gsissh.security.TokenizedMyProxyAuthInfo;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.api.job.JobManagerConfiguration;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SecurityProtocol;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class GFACGSISSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACGSISSHUtils.class);

    public static final String PBS_JOB_MANAGER = "pbs";
    public static final String SLURM_JOB_MANAGER = "slurm";
    public static final String SUN_GRID_ENGINE_JOB_MANAGER = "UGE";
    public static int maxClusterCount = 5;
    public static Map<String, List<Cluster>> clusters = new HashMap<String, List<Cluster>>();
    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        JobSubmissionInterface jobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
        JobSubmissionProtocol jobProtocol = jobSubmissionInterface.getJobSubmissionProtocol();
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            SSHJobSubmission sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
            if (jobProtocol == JobSubmissionProtocol.GLOBUS || jobProtocol == JobSubmissionProtocol.UNICORE
                    || jobProtocol == JobSubmissionProtocol.CLOUD || jobProtocol == JobSubmissionProtocol.LOCAL) {
                logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
            } else if (jobProtocol == JobSubmissionProtocol.SSH && sshJobSubmission.getSecurityProtocol() == SecurityProtocol.GSI) {
                String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
                RequestData requestData = new RequestData(ServerSettings.getDefaultUserGateway());
                requestData.setTokenId(credentialStoreToken);
                PBSCluster pbsCluster = null;
                GSISecurityContext context = null;

                TokenizedMyProxyAuthInfo tokenizedMyProxyAuthInfo = new TokenizedMyProxyAuthInfo(requestData);
                CredentialReader credentialReader = GFacUtils.getCredentialReader();
                if (credentialReader != null) {
                    CertificateCredential credential = null;
                    try {
                        credential = (CertificateCredential) credentialReader.getCredential(ServerSettings.getDefaultUserGateway(), credentialStoreToken);
                        requestData.setMyProxyUserName(credential.getCommunityUser().getUserName());
                    } catch (Exception e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }

                String key = requestData.getMyProxyUserName() + jobExecutionContext.getHostName()+
                        sshJobSubmission.getSshPort();
                boolean recreate = false;
                synchronized (clusters) {
                    if (clusters.containsKey(key) && clusters.get(key).size() < maxClusterCount) {
                        recreate = true;
                    } else if (clusters.containsKey(key)) {
                        int i = new Random().nextInt(Integer.MAX_VALUE) % maxClusterCount;
                        if (clusters.get(key).get(i).getSession().isConnected()) {
                            pbsCluster = (PBSCluster) clusters.get(key).get(i);
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
                            logger.info("Re-using the same connection used with the connection string:" + key);
                            context = new GSISecurityContext(tokenizedMyProxyAuthInfo.getCredentialReader(), requestData, pbsCluster);
                        }
                    } else {
                        recreate = true;
                    }

                    if (recreate) {
                        ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), jobExecutionContext.getHostName(),
                                sshJobSubmission.getSshPort());

                        JobManagerConfiguration jConfig = null;
                        String installedParentPath = sshJobSubmission.getResourceJobManager().getJobManagerBinPath();
                        String jobManager = sshJobSubmission.getResourceJobManager().getResourceJobManagerType().toString();
                        if (jobManager == null) {
                            logger.error("No Job Manager is configured, so we are picking pbs as the default job manager");
                            jConfig = CommonUtils.getPBSJobManager(installedParentPath);
                        } else {
                            if (PBS_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                                jConfig = CommonUtils.getPBSJobManager(installedParentPath);
                            } else if (SLURM_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                                jConfig = CommonUtils.getSLURMJobManager(installedParentPath);
                            } else if (SUN_GRID_ENGINE_JOB_MANAGER.equalsIgnoreCase(jobManager)) {
                                jConfig = CommonUtils.getSGEJobManager(installedParentPath);
                            }
                        }
                        pbsCluster = new PBSCluster(serverInfo, tokenizedMyProxyAuthInfo, jConfig);
                        context = new GSISecurityContext(tokenizedMyProxyAuthInfo.getCredentialReader(), requestData, pbsCluster);
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

                jobExecutionContext.addSecurityContext(Constants.GSI_SECURITY_CONTEXT, context);
            }
        } catch (Exception e) {
            throw new GFacException("An error occurred while creating GSI security context", e);
        }
    }

    public static JobDescriptor createJobDescriptor(JobExecutionContext jobExecutionContext, Cluster cluster) {
        JobDescriptor jobDescriptor = new JobDescriptor();
        ApplicationContext applicationContext = jobExecutionContext.getApplicationContext();
        ApplicationDeploymentDescription app = applicationContext.getApplicationDeploymentDescription();
        // this is common for any application descriptor
        jobDescriptor.setCallBackIp(ServerSettings.getIp());
        jobDescriptor.setCallBackPort(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.GFAC_SERVER_PORT, "8950"));
        jobDescriptor.setInputDirectory(jobExecutionContext.getInputDir());
        jobDescriptor.setOutputDirectory(jobExecutionContext.getOutputDir());
        jobDescriptor.setExecutablePath(jobExecutionContext.getExecutablePath());
        jobDescriptor.setStandardOutFile(jobExecutionContext.getStandardOutput());
        jobDescriptor.setStandardErrorFile(jobExecutionContext.getStandardError());
        Random random = new Random();
        int i = random.nextInt(Integer.MAX_VALUE); // We always set the job name
        jobDescriptor.setJobName("A" + String.valueOf(i+99999999));
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

        return jobDescriptor;
    }
}
