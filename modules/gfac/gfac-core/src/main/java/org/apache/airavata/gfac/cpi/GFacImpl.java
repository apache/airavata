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
package org.apache.airavata.gfac.cpi;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialReaderFactory;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.*;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.context.security.GSISecurityContext;
import org.apache.airavata.gfac.context.security.SSHSecurityContext;
import org.apache.airavata.gfac.handler.GFacHandler;
import org.apache.airavata.gfac.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.handler.GFacHandlerException;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.notification.listeners.LoggingListener;
import org.apache.airavata.gfac.notification.listeners.WorkflowTrackingListener;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gsi.ssh.api.Cluster;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.api.authentication.GSIAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPasswordAuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.DefaultPublicKeyFileAuthentication;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.model.experiment.ConfigurationData;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.schemas.gfac.*;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.schemas.wec.SecurityContextDocument;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * This is the GFac CPI class for external usage, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public class GFacImpl implements GFac {
    private static final Logger log = LoggerFactory.getLogger(GFacImpl.class);
    public static final String ERROR_SENT = "ErrorSent";

    private Registry registry;

    private AiravataAPI airavataAPI;

    private AiravataRegistry2 airavataRegistry2;

    /**
     * Constructor for GFac
     *
     * @param registry
     * @param airavataAPI
     * @param airavataRegistry2
     */
    public GFacImpl(Registry registry, AiravataAPI airavataAPI, AiravataRegistry2 airavataRegistry2) {
        this.registry = registry;
        this.airavataAPI = airavataAPI;
        this.airavataRegistry2 = airavataRegistry2;
    }

    /**
     * This can be used to submit jobs for testing purposes just by filling parameters by hand (JobExecutionContext)
     */
    public GFacImpl() {

    }

    /**
     * This is the job launching method outsiders of GFac can use, this will invoke the GFac handler chain and providers
     * And update the registry occordingly, so the users can query the database to retrieve status and output from Registry
     *
     * @param experimentID
     * @return
     * @throws GFacException
     */
    public JobExecutionContext submitJob(String experimentID,String taskID) throws GFacException {
        JobExecutionContext jobExecutionContext = null;
        try {
            ConfigurationData configurationData = (ConfigurationData) registry.get(DataType.TASK_DETAIL, taskID);
            // this is wear our new model and old model is mapping (so serviceName in ExperimentData and service name in ServiceDescriptor
            // has to be same.

            // 1. Get the Task from the task ID and construct the Job object and save it in to registry
            // 2. Add another property to jobExecutionContext and read them inside the provider and use it.
            String serviceName = configurationData.getApplicationId();
        if (serviceName == null) {
            throw new GFacException("Error executing the job because there is not Application Name in this Experiment");
        }
            List<HostDescription> registeredHosts = new ArrayList<HostDescription>();
            Map<String, ApplicationDescription> applicationDescriptors = airavataRegistry2.getApplicationDescriptors(serviceName);
            for (String hostDescName : applicationDescriptors.keySet()) {
                registeredHosts.add(airavataRegistry2.getHostDescriptor(hostDescName));
            }
            Class<? extends HostScheduler> aClass = Class.forName(ServerSettings.getHostScheduler()).asSubclass(HostScheduler.class);
            HostScheduler hostScheduler = aClass.newInstance();
            HostDescription hostDescription = hostScheduler.schedule(registeredHosts);

            ServiceDescription serviceDescription = airavataRegistry2.getServiceDescriptor(serviceName);

            ApplicationDescription applicationDescription = airavataRegistry2.getApplicationDescriptors(serviceName, hostDescription.getType().getHostName());
            URL resource = GFacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
            Properties configurationProperties = ServerSettings.getProperties();
            GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

            jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);
            jobExecutionContext.setConfigurationData(configurationData);
            
            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setApplicationDeploymentDescription(applicationDescription);
            applicationContext.setHostDescription(hostDescription);
            applicationContext.setServiceDescription(serviceDescription);
            jobExecutionContext.setApplicationContext(applicationContext);


            Map<String, String> experimentInputs = configurationData.getExperimentInputs();
            jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getMessageContext(experimentInputs,
                    serviceDescription.getType().getInputParametersArray())));

            HashMap<String, Object> outputData = new HashMap<String, Object>();
            jobExecutionContext.setOutMessageContext(new MessageContext(outputData));

            jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
            jobExecutionContext.setExperimentID(experimentID);

            addSecurityContext(hostDescription, configurationProperties, jobExecutionContext);


            submitJob(jobExecutionContext);
        } catch (Exception e) {
            log.error("Error inovoking the job with experiment ID: " + experimentID);
            throw new GFacException(e);
        }
        return jobExecutionContext;
    }

    public void submitJob(JobExecutionContext jobExecutionContext) throws GFacException {
        // We need to check whether this job is submitted as a part of a large workflow. If yes,
        // we need to setup workflow tracking listerner.
        String workflowInstanceID = null;
        if ((workflowInstanceID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_INSTANCE_ID)) != null) {
            // This mean we need to register workflow tracking listener.
            //todo implement WorkflowTrackingListener properly
            registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
        }
        // Register log event listener. This is required in all scenarios.
        jobExecutionContext.getNotificationService().registerListener(new LoggingListener());
        schedule(jobExecutionContext);
    }

    private void schedule(JobExecutionContext jobExecutionContext) throws GFacException {
        // Scheduler will decide the execution flow of handlers and provider which handles
        // the job.
        String experimentID = jobExecutionContext.getExperimentID();
        try {
            Scheduler.schedule(jobExecutionContext);

            // Executing in handlers in the order as they have configured in GFac configuration
            invokeInFlowHandlers(jobExecutionContext);
//            if (experimentID != null){
//                registry2.changeStatus(jobExecutionContext.getExperimentID(),AiravataJobState.State.INHANDLERSDONE);
//            }

            // After executing the in handlers provider instance should be set to job execution context.
            // We get the provider instance and execute it.
            GFacProvider provider = jobExecutionContext.getProvider();
            if (provider != null) {
                initProvider(provider, jobExecutionContext);
                executeProvider(provider, jobExecutionContext);
                disposeProvider(provider, jobExecutionContext);
            }
//            if (experimentID != null){
//                registry2.changeStatus(jobExecutionContext.getExperimentID(),AiravataJobState.State.OUTHANDLERSDONE);
//            }
        } catch (Exception e) {
            jobExecutionContext.setProperty(ERROR_SENT, "true");
            jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(e.getCause()));
            throw new GFacException(e.getMessage(), e);
        }
    }

    private void initProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.initialize(jobExecutionContext);
        } catch (Exception e) {
            throw new GFacException("Error while initializing provider " + provider.getClass().getName() + ".", e);
        }
    }

    private void executeProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.execute(jobExecutionContext);
        } catch (Exception e) {
            throw new GFacException("Error while executing provider " + provider.getClass().getName() + " functionality.", e);
        }
    }

    private void disposeProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.dispose(jobExecutionContext);
        } catch (Exception e) {
            throw new GFacException("Error while invoking provider " + provider.getClass().getName() + " dispose method.", e);
        }
    }

    private void registerWorkflowTrackingListener(String workflowInstanceID, JobExecutionContext jobExecutionContext) {
        String workflowNodeID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_NODE_ID);
        String topic = (String) jobExecutionContext.getProperty(Constants.PROP_TOPIC);
        String brokerUrl = (String) jobExecutionContext.getProperty(Constants.PROP_BROKER_URL);
        jobExecutionContext.getNotificationService().registerListener(
                new WorkflowTrackingListener(workflowInstanceID, workflowNodeID, brokerUrl, topic));

    }

    private void invokeInFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<GFacHandlerConfig> handlers = jobExecutionContext.getGFacConfiguration().getInHandlers();
        for (GFacHandlerConfig handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.getClassName().trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
                handler.initProperties(handlerClassName.getProperties());
            } catch (ClassNotFoundException e) {
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            }
            try {
                handler.invoke(jobExecutionContext);
            } catch (GFacHandlerException e) {
                throw new GFacException("Error Executing a InFlow Handler", e.getCause());
            }
        }
    }

    public void invokeOutFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<GFacHandlerConfig> handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();

        for (GFacHandlerConfig handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.getClassName().trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
                handler.initProperties(handlerClassName.getProperties());
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            }
            try {
                handler.invoke(jobExecutionContext);
            } catch (Exception e) {
                // TODO: Better error reporting.
                throw new GFacException("Error Executing a OutFlow Handler", e);
            }
        }
    }

    private void addSecurityContext(HostDescription registeredHost, Properties configurationProperties,
                                    JobExecutionContext jobExecutionContext) throws GFacException {
        RequestData requestData;
        if (registeredHost.getType() instanceof GlobusHostType || registeredHost.getType() instanceof UnicoreHostType
                || registeredHost.getType() instanceof GsisshHostType) {

            //todo implement a way to get credential management service from configurationData
            SecurityContextDocument.SecurityContext.CredentialManagementService credentialManagementService = null;
            GSISecurityContext context = null;

            /*
            if (credentialManagementService != null) {
                String gatewayId = credentialManagementService.getGatewayId();
                String tokenId
                        = credentialManagementService.getTokenId();
                String portalUser = credentialManagementService.getPortalUser();

                requestData = new RequestData(tokenId, portalUser, gatewayId);
            } else {
                requestData = new RequestData("default");
            }

            try {
                context = new GSISecurityContext(CredentialReaderFactory.createCredentialStoreReader(), requestData);
            } catch (Exception e) {
                   throw new WorkflowException("An error occurred while creating GSI security context", e);
            }

            if (registeredHost.getType() instanceof GsisshHostType) {
                GSIAuthenticationInfo authenticationInfo
                        = new MyProxyAuthenticationInfo(requestData.getMyProxyUserName(), requestData.getMyProxyPassword(), requestData.getMyProxyServerUrl(),
                        requestData.getMyProxyPort(), requestData.getMyProxyLifeTime(), System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY));
                ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.setPbsCluster(pbsCluster);
            }        */

            requestData = new RequestData("default");
            try {
                context = new GSISecurityContext(CredentialReaderFactory.createCredentialStoreReader(), requestData);
            } catch (Exception e) {
                throw new GFacException("An error occurred while creating GSI security context", e);
            }
            if (registeredHost.getType() instanceof GsisshHostType) {
                GSIAuthenticationInfo authenticationInfo
                        = new MyProxyAuthenticationInfo(requestData.getMyProxyUserName(), requestData.getMyProxyPassword(), requestData.getMyProxyServerUrl(),
                        requestData.getMyProxyPort(), requestData.getMyProxyLifeTime(), System.getProperty(Constants.TRUSTED_CERTIFICATE_SYSTEM_PROPERTY));
                ServerInfo serverInfo = new ServerInfo(requestData.getMyProxyUserName(), registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

                context.setPbsCluster(pbsCluster);
            }
            jobExecutionContext.addSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT, context);
        } else if (registeredHost.getType() instanceof Ec2HostType) {
            //todo fixthis amazon securitycontext
//               if (this.configuration.getAmazonSecurityContext() != null) {
//                   jobExecutionContext.addSecurityContext(AmazonSecurityContext.AMAZON_SECURITY_CONTEXT,
//                           this.configuration.getAmazonSecurityContext());
        } else if (registeredHost.getType() instanceof SSHHostType) {
            String sshUserName = configurationProperties.getProperty(Constants.SSH_USER_NAME);
            String sshPrivateKey = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY);
            String sshPrivateKeyPass = configurationProperties.getProperty(Constants.SSH_PRIVATE_KEY_PASS);
            String sshPassword = configurationProperties.getProperty(Constants.SSH_PASSWORD);
            String sshPublicKey = configurationProperties.getProperty(Constants.SSH_PUBLIC_KEY);
            SSHSecurityContext sshSecurityContext = new SSHSecurityContext();
            if (((SSHHostType) registeredHost.getType()).getHpcResource()) {
                AuthenticationInfo authenticationInfo = null;
                // we give higher preference to the password over keypair ssh authentication
                if (sshPassword != null) {
                    authenticationInfo = new DefaultPasswordAuthenticationInfo(sshPassword);
                } else {
                    authenticationInfo = new DefaultPublicKeyFileAuthentication(sshPublicKey, sshPrivateKey, sshPrivateKeyPass);
                }
                ServerInfo serverInfo = new ServerInfo(sshUserName, registeredHost.getType().getHostAddress());

                Cluster pbsCluster = null;
                try {
                    pbsCluster = new PBSCluster(serverInfo, authenticationInfo,
                            (((HpcApplicationDeploymentType) jobExecutionContext.getApplicationContext().getApplicationDeploymentDescription().getType()).getInstalledParentPath()));
                } catch (SSHApiException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                sshSecurityContext.setPbsCluster(pbsCluster);
                sshSecurityContext.setUsername(sshUserName);
            } else {
                sshSecurityContext = new SSHSecurityContext();
                sshSecurityContext.setUsername(sshUserName);
                sshSecurityContext.setPrivateKeyLoc(sshPrivateKey);
                sshSecurityContext.setKeyPass(sshPrivateKeyPass);
            }
            jobExecutionContext.addSecurityContext(SSHSecurityContext.SSH_SECURITY_CONTEXT, sshSecurityContext);
        }
    }

}
