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
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.MappingFactory;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.RequestData;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.ssh.context.SSHAuthWrapper;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.ssh.security.TokenizedSSHAuthInfo;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.job.JobDescriptor;
import org.apache.airavata.gsi.ssh.impl.GSISSHAbstractCluster;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.util.CommonUtils;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.SecurityProtocol;
import org.apache.airavata.model.workspace.experiment.ComputationalResourceScheduling;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GFACSSHUtils {
    private final static Logger logger = LoggerFactory.getLogger(GFACSSHUtils.class);

    public static Map<String, List<Cluster>> clusters = new HashMap<String, List<Cluster>>();

    public static int maxClusterCount = 5;

    public static final String ADVANCED_SSH_AUTH = "advanced.ssh.auth";


    public static void addSecurityContext(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException {
        JobSubmissionProtocol preferredJobSubmissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
        JobSubmissionInterface preferredJobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
        if (preferredJobSubmissionProtocol == JobSubmissionProtocol.GLOBUS || preferredJobSubmissionProtocol == JobSubmissionProtocol.UNICORE) {
            logger.error("This is a wrong method to invoke to non ssh host types,please check your gfac-config.xml");
        } else if ( preferredJobSubmissionProtocol == JobSubmissionProtocol.SSH  ) {
            try {
                AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
                SSHJobSubmission sshJobSubmission = appCatalog.getComputeResource().getSSHJobSubmission(preferredJobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission.getSecurityProtocol() == SecurityProtocol.GSI) {
                    SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
                    String credentialStoreToken = jobExecutionContext.getCredentialStoreToken(); // this is set by the framework
                    RequestData requestData = new RequestData(ServerSettings.getDefaultUserGateway());
                    requestData.setTokenId(credentialStoreToken);

                    ServerInfo serverInfo = new ServerInfo(null, jobExecutionContext.getHostName());
                    SSHAuthWrapper sshAuth = (SSHAuthWrapper) jobExecutionContext.getProperty(ADVANCED_SSH_AUTH);

                    Cluster pbsCluster = null;
                    try {
                        TokenizedSSHAuthInfo tokenizedSSHAuthInfo = new TokenizedSSHAuthInfo(requestData);
                        String installedParentPath = jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getExecutablePath();
                        if (installedParentPath == null) {
                            installedParentPath = "/";
                        }

                        SSHCredential credentials = tokenizedSSHAuthInfo.getCredentials();// this is just a call to get and set credentials in to this object,data will be used
                        serverInfo.setUserName(credentials.getPortalUserName());
                        jobExecutionContext.getExperiment().setUserName(credentials.getPortalUserName());
                        // inside the pbsCluser object

                        String key = credentials.getPortalUserName() + jobExecutionContext.getHostName() + serverInfo.getPort();
                        if(sshAuth!=null){
                            key=sshAuth.getKey();
                        }
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
                                if (sshAuth != null) {
                                    pbsCluster = new PBSCluster(sshAuth.getServerInfo(), sshAuth.getAuthenticationInfo(),
                                            CommonUtils.getPBSJobManager(installedParentPath));
                                    jobExecutionContext.setProperty(ADVANCED_SSH_AUTH,null); // some other provider might fail
                                    key = sshAuth.getKey();
                                } else {
                                    pbsCluster = new PBSCluster(serverInfo, tokenizedSSHAuthInfo,
                                            CommonUtils.getPBSJobManager(installedParentPath));
                                }
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
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    sshSecurityContext.setPbsCluster(pbsCluster);
                    jobExecutionContext.addSecurityContext(Constants.SSH_SECURITY_CONTEXT, sshSecurityContext);
                }
            } catch (AppCatalogException e) {
                throw new GFacException("Error while getting SSH Submission object from app catalog", e);
            }
        }
    }

    public static JobDescriptor createJobDescriptor(JobExecutionContext jobExecutionContext, Cluster cluster) {
        JobDescriptor jobDescriptor = new JobDescriptor();
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
            ActualParameter actualParameter = (ActualParameter) inputs.get(paramName);
            if ("URIArray".equals(actualParameter.getType().getType().toString()) || "StringArray".equals(actualParameter.getType().getType().toString())
                    || "FileArray".equals(actualParameter.getType().getType().toString())) {
                String[] values = null;
                if (actualParameter.getType() instanceof URIArrayType) {
                    values = ((URIArrayType) actualParameter.getType()).getValueArray();
                } else if (actualParameter.getType() instanceof StringArrayType) {
                    values = ((StringArrayType) actualParameter.getType()).getValueArray();
                } else if (actualParameter.getType() instanceof FileArrayType) {
                    values = ((FileArrayType) actualParameter.getType()).getValueArray();
                }
                String value = StringUtil.createDelimiteredString(values, " ");
                inputValues.add(value);
            } else {
                String paramValue = MappingFactory.toString(actualParameter);
                inputValues.add(paramValue);
            }
        }
        jobDescriptor.setInputValues(inputValues);

        return jobDescriptor;
    }

}
