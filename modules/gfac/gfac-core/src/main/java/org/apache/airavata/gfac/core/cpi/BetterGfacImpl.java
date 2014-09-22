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
package org.apache.airavata.gfac.core.cpi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.Scheduler;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.handler.GFacHandler;
import org.apache.airavata.gfac.core.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.handler.GFacRecoverableHandler;
import org.apache.airavata.gfac.core.handler.ThreadedHandler;
import org.apache.airavata.gfac.core.monitor.JobIdentity;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.monitor.TaskIdentity;
//import org.apache.airavata.api.server.listener.ExperimentStatusChangedEvent;
import org.apache.airavata.gfac.core.monitor.state.GfacExperimentStateChangeRequest;
import org.apache.airavata.gfac.core.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangeRequest;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangedEvent;
import org.apache.airavata.gfac.core.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.core.notification.listeners.LoggingListener;
import org.apache.airavata.gfac.core.notification.listeners.WorkflowTrackingListener;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.provider.GFacRecoverableProvider;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.core.states.GfacPluginState;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobManagerCommand;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.schemas.gfac.SSHHostType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This is the GFac CPI class for external usage, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public class BetterGfacImpl implements GFac,Watcher {
    private static final Logger log = LoggerFactory.getLogger(BetterGfacImpl.class);
    public static final String ERROR_SENT = "ErrorSent";

    private Registry registry;

//    private AiravataAPI airavataAPI;

//    private AiravataRegistry2 airavataRegistry2;

    private ZooKeeper zk;                       // we are not storing zk instance in to jobExecution context

    private static Integer mutex = new Integer(-1);

    private static List<ThreadedHandler> daemonHandlers = new ArrayList<ThreadedHandler>();

    private static File gfacConfigFile;

    private static List<AbstractActivityListener> activityListeners = new ArrayList<AbstractActivityListener>();

    private static MonitorPublisher monitorPublisher;

    private boolean cancelled = false;

    private static ExecutorService cachedThreadPool;

    /**
     * Constructor for GFac
     *
     * @param registry
     * @param zooKeeper
     */
    public BetterGfacImpl(Registry registry, ZooKeeper zooKeeper,
                          MonitorPublisher publisher) {
        this.registry = registry;
//        this.airavataAPI = airavataAPI;
//        this.airavataRegistry2 = airavataRegistry2;
        monitorPublisher = publisher;     // This is a EventBus common for gfac
        this.zk = zooKeeper;
       this.cachedThreadPool = Executors.newCachedThreadPool();
    }

    public static void startStatusUpdators(Registry registry, ZooKeeper zk, MonitorPublisher publisher) {
        try {
            String[] listenerClassList = ServerSettings.getActivityListeners();
            for (String listenerClass : listenerClassList) {
                Class<? extends AbstractActivityListener> aClass = Class.forName(listenerClass).asSubclass(AbstractActivityListener.class);
                AbstractActivityListener abstractActivityListener = aClass.newInstance();
                activityListeners.add(abstractActivityListener);
                abstractActivityListener.setup(publisher, registry, zk);
                log.info("Registering listener: " + listenerClass);
                publisher.registerListener(abstractActivityListener);
            }
        } catch (ClassNotFoundException e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        } catch (InstantiationException e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        } catch (IllegalAccessException e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        }
    }

    public static void startDaemonHandlers() {
        List<GFacHandlerConfig> daemonHandlerConfig = null;
        URL resource = BetterGfacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        gfacConfigFile = new File(resource.getPath());
        try {
            daemonHandlerConfig = GFacConfiguration.getDaemonHandlers(gfacConfigFile);
        } catch (ParserConfigurationException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (IOException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (SAXException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (XPathExpressionException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        }

        for (GFacHandlerConfig handlerConfig : daemonHandlerConfig) {
            String className = handlerConfig.getClassName();
            try {
                Class<?> aClass = Class.forName(className).asSubclass(ThreadedHandler.class);
                ThreadedHandler threadedHandler = (ThreadedHandler) aClass.newInstance();
                threadedHandler.initProperties(handlerConfig.getProperties());
                daemonHandlers.add(threadedHandler);
            } catch (ClassNotFoundException e) {
                log.error("Error initializing the handler: " + className);
                log.error(className + " class has to implement " + ThreadedHandler.class);
            } catch (InstantiationException e) {
                log.error("Error initializing the handler: " + className);
                log.error(className + " class has to implement " + ThreadedHandler.class);
            } catch (IllegalAccessException e) {
                log.error("Error initializing the handler: " + className);
                log.error(className + " class has to implement " + ThreadedHandler.class);
            } catch (GFacHandlerException e) {
                log.error("Error initializing the handler " + className);
            } catch (GFacException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        for (ThreadedHandler tHandler : daemonHandlers) {
            (new Thread(tHandler)).start();
        }
    }

    /**
     * This can be used to submit jobs for testing purposes just by filling parameters by hand (JobExecutionContext)
     */
    public BetterGfacImpl() {
        daemonHandlers = new ArrayList<ThreadedHandler>();
        startDaemonHandlers();
    }

    /**
     * This is the job launching method outsiders of GFac can use, this will invoke the GFac handler chain and providers
     * And update the registry occordingly, so the users can query the database to retrieve status and output from Registry
     *
     * @param experimentID
     * @return
     * @throws GFacException
     */
    public boolean submitJob(String experimentID, String taskID, String gatewayID) throws GFacException {
        JobExecutionContext jobExecutionContext = null;
        try {
            jobExecutionContext = createJEC(experimentID, taskID, gatewayID);
            return submitJob(jobExecutionContext);
        } catch (Exception e) {
            log.error("Error inovoking the job with experiment ID: " + experimentID);
            throw new GFacException(e);
        }
    }

    private JobExecutionContext createJEC(String experimentID, String taskID, String gatewayID) throws Exception {

        JobExecutionContext jobExecutionContext;

        /** FIXME:
         * A temporary wrapper to co-relate the app catalog and experiment thrift models to old gfac schema documents.
         * The serviceName in ExperimentData and service name in ServiceDescriptor has to be same.
         * 1. Get the Task from the task ID and construct the Job object and save it in to registry
         * 2. Add properties of description documents to jobExecutionContext which will be used inside the providers.
         */

        //Fetch the Task details for the requested experimentID from the registry. Extract required pointers from the Task object.
        TaskDetails taskData = (TaskDetails) registry.get(RegistryModelType.TASK_DETAIL, taskID);

        String applicationInterfaceId = taskData.getApplicationId();
        String applicationDeploymentId = taskData.getApplicationDeploymentId();
        if (null == applicationInterfaceId) {
            throw new GFacException("Error executing the job. The required Application Id is missing");
        }
        if (null == applicationDeploymentId) {
            throw new GFacException("Error executing the job. The required Application deployment Id is missing");
        }

        AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();

        //fetch the compute resource, application interface and deployment information from app catalog
        ApplicationInterfaceDescription applicationInterface = appCatalog.
                getApplicationInterface().getApplicationInterface(applicationInterfaceId);
        ApplicationDeploymentDescription applicationDeployment = appCatalog.
                getApplicationDeployment().getApplicationDeployement(applicationDeploymentId);
        ComputeResourceDescription computeResource = appCatalog.getComputeResource().
                getComputeResource(applicationDeployment.getComputeHostId());
        ComputeResourcePreference gatewayResourcePreferences = appCatalog.getGatewayProfile().
                getComputeResourcePreference(gatewayID, applicationDeployment.getComputeHostId());
        if (gatewayResourcePreferences == null) {
            List<String> gatewayProfileIds = appCatalog.getGatewayProfile()
                    .getGatewayProfileIds(gatewayID);
            for (String profileId : gatewayProfileIds) {
                gatewayID = profileId;
                gatewayResourcePreferences = appCatalog.getGatewayProfile().
                        getComputeResourcePreference(gatewayID, applicationDeployment.getComputeHostId());
                if (gatewayResourcePreferences != null) {
                    break;
                }
            }
        }
        //Create the legacy schema docs to fill-in
        ServiceDescription legacyServiceDescription = new ServiceDescription();
        ServiceDescriptionType legacyServiceDescType = legacyServiceDescription.getType();
        ApplicationDescription legacyAppDescription = null;
        HostDescription legacyHostDescription = null;

        ///////////////SERVICE DESCRIPTOR///////////////////////////////
        //Fetch the application inputs and outputs from the app interface and create the legacy service description.
        legacyServiceDescType.setName(applicationInterface.getApplicationName());
        legacyServiceDescType.setDescription(applicationInterface.getApplicationName());
        List<InputParameterType> legacyInputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> legacyOutputParameters = new ArrayList<OutputParameterType>();
        List<InputDataObjectType> applicationInputs = applicationInterface.getApplicationInputs();
        for (InputDataObjectType dataObjectType : applicationInputs) {
            InputParameterType parameter = InputParameterType.Factory.newInstance();
            parameter.setParameterName(dataObjectType.getName());
            parameter.setParameterDescription(dataObjectType.getUserFriendlyDescription());
            ParameterType parameterType = parameter.addNewParameterType();
            switch (dataObjectType.getType()) {
                case FLOAT:
                    parameterType.setType(DataType.FLOAT);
                    break;
                case INTEGER:
                    parameterType.setType(DataType.INTEGER);
                    break;
                case STRING:
                    parameterType.setType(DataType.STRING);
                    break;
                case URI:
                    parameterType.setType(DataType.URI);
                    break;
            }
            parameterType.setName(parameterType.getType().toString());
            parameter.addParameterValue(dataObjectType.getValue());
            legacyInputParameters.add(parameter);
        }

        List<OutputDataObjectType> applicationOutputs = applicationInterface.getApplicationOutputs();
        for (OutputDataObjectType dataObjectType : applicationOutputs) {
            OutputParameterType parameter = OutputParameterType.Factory.newInstance();
            parameter.setParameterName(dataObjectType.getName());
            parameter.setParameterDescription(dataObjectType.getName());
            ParameterType parameterType = parameter.addNewParameterType();
            switch (dataObjectType.getType()) {
                case FLOAT:
                    parameterType.setType(DataType.FLOAT);
                    break;
                case INTEGER:
                    parameterType.setType(DataType.INTEGER);
                    break;
                case STRING:
                    parameterType.setType(DataType.STRING);
                    break;
                case URI:
                    parameterType.setType(DataType.URI);
                    break;
            }
            parameterType.setName(parameterType.getType().toString());
            legacyOutputParameters.add(parameter);
        }

        legacyServiceDescType.setInputParametersArray(legacyInputParameters.toArray(new InputParameterType[]{}));
        legacyServiceDescType.setOutputParametersArray(legacyOutputParameters.toArray(new OutputParameterType[]{}));

        ////////////////////-----------  HOST DESCRIPTOR  -----------------//////////////////////
        //Fetch the host description details and fill-in legacy doc
        ResourceJobManager resourceJobManager = null;
        for (JobSubmissionInterface jobSubmissionInterface : computeResource.getJobSubmissionInterfaces()) {
            switch (jobSubmissionInterface.getJobSubmissionProtocol()) {
                case LOCAL:
                    legacyHostDescription = new HostDescription();
                    LOCALSubmission localSubmission =
                            appCatalog.getComputeResource().getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                    resourceJobManager = localSubmission.getResourceJobManager();
                    break;
                case SSH:
                    SSHJobSubmission sshJobSubmission =
                            appCatalog.getComputeResource().getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                    resourceJobManager = sshJobSubmission.getResourceJobManager();
                    switch (sshJobSubmission.getSecurityProtocol()) {
                        case GSI:
                            legacyHostDescription = new HostDescription(GsisshHostType.type);
                            ((GsisshHostType) legacyHostDescription.getType()).setJobManager
                                    (resourceJobManager.getResourceJobManagerType().name());
                            ((GsisshHostType) legacyHostDescription.getType()).setInstalledPath(resourceJobManager.getJobManagerBinPath());
                            // applicationDescription.setInstalledParentPath(resourceJobManager.getJobManagerBinPath());
                            ((GsisshHostType) legacyHostDescription.getType()).setPort(sshJobSubmission.getSshPort());
                            break;
                        case SSH_KEYS:
                            legacyHostDescription = new HostDescription(SSHHostType.type);
                            ((SSHHostType) legacyHostDescription.getType()).setHpcResource(true);
                            break;
                        default:
                            legacyHostDescription = new HostDescription(SSHHostType.type);
                            ((SSHHostType) legacyHostDescription.getType()).setHpcResource(true);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        HostDescriptionType legacyHostDescType = legacyHostDescription.getType();
        legacyHostDescType.setHostName(computeResource.getHostName());
        String ipAddress = computeResource.getHostName();
        if (computeResource.getIpAddresses() != null && computeResource.getIpAddresses().size() > 0) {
            ipAddress = computeResource.getIpAddresses().iterator().next();
        } else if (computeResource.getHostAliases() != null && computeResource.getHostAliases().size() > 0) {
            ipAddress = computeResource.getHostAliases().iterator().next();
        }
        legacyHostDescType.setHostAddress(ipAddress);

        /////////////////////---------------- APPLICATION DESCRIPTOR ---------------------/////////////////////////
        //Fetch deployment information and fill-in legacy doc
        if ((legacyHostDescType instanceof GsisshHostType) || (legacyHostDescType instanceof SSHHostType)) {
            legacyAppDescription = new ApplicationDescription(HpcApplicationDeploymentType.type);
            HpcApplicationDeploymentType legacyHPCAppDescType = (HpcApplicationDeploymentType) legacyAppDescription.getType();
            switch (applicationDeployment.getParallelism()) {
                case SERIAL:
                    legacyHPCAppDescType.setJobType(JobTypeType.SERIAL);
                    break;
                case MPI:
                    legacyHPCAppDescType.setJobType(JobTypeType.MPI);
                    break;
                case OPENMP:
                    legacyHPCAppDescType.setJobType(JobTypeType.OPEN_MP);
                    break;
                default:
                    break;
            }
            //Fetch scheduling information from experiment request
            ComputationalResourceScheduling taskSchedule = taskData.getTaskScheduling();
            QueueType queueType = legacyHPCAppDescType.addNewQueue();
            queueType.setQueueName(taskSchedule.getQueueName());
            legacyHPCAppDescType.setCpuCount(taskSchedule.getTotalCPUCount());
            legacyHPCAppDescType.setNodeCount(taskSchedule.getNodeCount());
            legacyHPCAppDescType.setMaxWallTime(taskSchedule.getWallTimeLimit());
            if (resourceJobManager != null) {
                legacyHPCAppDescType.setInstalledParentPath(resourceJobManager.getJobManagerBinPath());
                if (resourceJobManager.getJobManagerCommands() != null) {
                    legacyHPCAppDescType.setJobSubmitterCommand(resourceJobManager.getJobManagerCommands().get(JobManagerCommand.SUBMISSION));
                }
            }
            ProjectAccountType projectAccountType = legacyHPCAppDescType.addNewProjectAccount();
            if (gatewayResourcePreferences != null) {
                projectAccountType.setProjectAccountNumber(gatewayResourcePreferences.getAllocationProjectNumber());
            }
        } else {
            legacyAppDescription = new ApplicationDescription();
        }
        ApplicationDeploymentDescriptionType legacyAppDescType = legacyAppDescription.getType();
        legacyAppDescType.addNewApplicationName().setStringValue(applicationInterface.getApplicationName().replaceAll(" ", "_"));
        legacyAppDescType.setExecutableLocation(applicationDeployment.getExecutablePath());
        if (gatewayResourcePreferences != null) {
            legacyAppDescType.setScratchWorkingDirectory(gatewayResourcePreferences.getScratchLocation());
        } else {
            legacyAppDescType.setScratchWorkingDirectory("/tmp");
            log.warn("Missing gateway resource profile for gateway id '" + gatewayID + "'.");
        }

        URL resource = BetterGfacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        Properties configurationProperties = ServerSettings.getProperties();
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), configurationProperties);

        // start constructing jobexecutioncontext
        jobExecutionContext = new JobExecutionContext(gFacConfiguration, applicationInterfaceId);

        // setting experiment/task/workflownode related information
        Experiment experiment = (Experiment) registry.get(RegistryModelType.EXPERIMENT, experimentID);
        jobExecutionContext.setExperiment(experiment);
        jobExecutionContext.setExperimentID(experimentID);
        jobExecutionContext.setWorkflowNodeDetails(experiment.getWorkflowNodeDetailsList().get(0));
        jobExecutionContext.setTaskData(taskData);
        jobExecutionContext.setGatewayID(gatewayID);


        List<JobDetails> jobDetailsList = taskData.getJobDetailsList();
        for(JobDetails jDetails:jobDetailsList){
            jobExecutionContext.setJobDetails(jDetails);
        }
        // setting the registry
        jobExecutionContext.setRegistry(registry);

        ApplicationContext applicationContext = new ApplicationContext();
//        applicationContext.setApplicationDeploymentDescription(applicationDescription);
        applicationContext.setHostDescription(legacyHostDescription);
        applicationContext.setServiceDescription(legacyServiceDescription);
        applicationContext.setApplicationDeploymentDescription(legacyAppDescription);
        jobExecutionContext.setApplicationContext(applicationContext);

        List<DataObjectType> experimentInputs = taskData.getApplicationInputs();
        jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getInMessageContext(experimentInputs,
                legacyServiceDescType.getInputParametersArray())));

        List<DataObjectType> outputData = taskData.getApplicationOutputs();
        jobExecutionContext.setOutMessageContext(new MessageContext(GFacUtils.getOutMessageContext(outputData,
                legacyServiceDescType.getOutputParametersArray())));

        jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
        jobExecutionContext.setGfac(this);
        jobExecutionContext.setZk(zk);
        jobExecutionContext.setCredentialStoreToken(AiravataZKUtils.getExpTokenId(zk, experimentID, taskID));
        return jobExecutionContext;
    }

    private boolean submitJob(JobExecutionContext jobExecutionContext) throws GFacException {
        // We need to check whether this job is submitted as a part of a large workflow. If yes,
        // we need to setup workflow tracking listerner.
        try {
            String experimentEntry = GFacUtils.findExperimentEntry(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), zk);
            Stat exists = zk.exists(experimentEntry + File.separator + "operation", false);
            zk.getData(experimentEntry + File.separator + "operation", this,exists);
            int stateVal = GFacUtils.getZKExperimentStateValue(zk, jobExecutionContext);   // this is the original state came, if we query again it might be different,so we preserve this state in the environment
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.ACCEPTED));                  // immediately we get the request we update the status
            String workflowInstanceID = null;
            if ((workflowInstanceID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_INSTANCE_ID)) != null) {
                // This mean we need to register workflow tracking listener.
                //todo implement WorkflowTrackingListener properly
                registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
            }
            // Register log event listener. This is required in all scenarios.
            jobExecutionContext.getNotificationService().registerListener(new LoggingListener());
            if (stateVal < 2) {
                // In this scenario We do everything from the beginning
                launch(jobExecutionContext);
            } else if (stateVal >= 8) {
                log.info("There is nothing to recover in this job so we do not re-submit");
                ZKUtil.deleteRecursive(zk,
                        AiravataZKUtils.getExpZnodePath(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID()));
            } else {
                // Now we know this is an old Job, so we have to handle things gracefully
                log.info("Re-launching the job in GFac because this is re-submitted to GFac");
                reLaunch(jobExecutionContext, stateVal);
            }
            return true;
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean cancel(String experimentID, String taskID, String gatewayID) throws GFacException {
        JobExecutionContext jobExecutionContext = null;
        try {
            jobExecutionContext = createJEC(experimentID, taskID, gatewayID);
            return cancel(jobExecutionContext);
        } catch (Exception e) {
            log.error("Error inovoking the job with experiment ID: " + experimentID);
            throw new GFacException(e);
        }
    }

    private boolean cancel(JobExecutionContext jobExecutionContext) throws GFacException {
        // We need to check whether this job is submitted as a part of a large workflow. If yes,
        // we need to setup workflow tracking listener.
        try {
            // we cannot call GFacUtils.getZKExperimentStateValue because experiment might be running in some other node
            String expPath = GFacUtils.findExperimentEntry(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), zk);
            int stateVal = GFacUtils.getZKExperimentStateValue(zk, expPath);   // this is the original state came, if we query again it might be different,so we preserve this state in the environment
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.ACCEPTED));                  // immediately we get the request we update the status
            String workflowInstanceID = null;
            if ((workflowInstanceID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_INSTANCE_ID)) != null) {
                // This mean we need to register workflow tracking listener.
                //todo implement WorkflowTrackingListener properly
                registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
            }
            // Register log event listener. This is required in all scenarios.
            jobExecutionContext.getNotificationService().registerListener(new LoggingListener());
            if (stateVal < 2) {
                // In this scenario We do everything from the beginning
                log.info("Job is not yet submitted, so nothing much to do except changing the registry entry " +
                        " and stop the execution chain");
            } else if (stateVal >= 8) {
                log.error("This experiment is almost finished, so cannot cancel this experiment");
                ZKUtil.deleteRecursive(zk,
                        AiravataZKUtils.getExpZnodePath(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID()));
            } else {
                log.info("Job is in a position to perform a proper cancellation");
                try {
                    Scheduler.schedule(jobExecutionContext);

                    invokeProviderCancel(jobExecutionContext);

                } catch (Exception e) {
                    try {
                        // we make the experiment as failed due to exception scenario
                        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.FAILED));
                        // monitorPublisher.publish(new
                        // ExperimentStatusChangedEvent(new
                        // ExperimentIdentity(jobExecutionContext.getExperimentID()),
                        // ExperimentState.FAILED));
                        // Updating the task status if there's any task associated
                        // monitorPublisher.publish(new TaskStatusChangeRequest(
                        // new TaskIdentity(jobExecutionContext.getExperimentID(),
                        // jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        // jobExecutionContext.getTaskData().getTaskID()),
                        // TaskState.FAILED
                        // ));
                        monitorPublisher.publish(new JobStatusChangeRequest(new MonitorID(jobExecutionContext), new JobIdentity(jobExecutionContext.getExperimentID(),
                                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext
                                .getJobDetails().getJobID()), JobState.FAILED));
                    } catch (NullPointerException e1) {
                        log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
                                + "NullPointerException occurred because at this point there might not have Job Created", e1, e);
                        //monitorPublisher.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
                        // Updating the task status if there's any task associated
                        monitorPublisher.publish(new TaskStatusChangeRequest(new TaskIdentity(jobExecutionContext.getExperimentID(), jobExecutionContext
                                .getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID()), TaskState.FAILED));

                    }
                    jobExecutionContext.setProperty(ERROR_SENT, "true");
                    jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(e.getCause()));
                    throw new GFacException(e.getMessage(), e);
                }
            }
            return true;
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

	private void reLaunch(JobExecutionContext jobExecutionContext, int stateVal) throws GFacException {
		// Scheduler will decide the execution flow of handlers and provider
		// which handles
		// the job.
		String experimentID = jobExecutionContext.getExperimentID();
		try {
			Scheduler.schedule(jobExecutionContext);

			// Executing in handlers in the order as they have configured in
			// GFac configuration
			// here we do not skip handler if some handler does not have to be
			// run again during re-run it can implement
			// that logic in to the handler
			reInvokeInFlowHandlers(jobExecutionContext);

			// After executing the in handlers provider instance should be set
			// to job execution context.
			// We get the provider instance and execute it.
			if (stateVal == 2 || stateVal == 3) {
				invokeProviderExecute(jobExecutionContext); // provider never ran in
														// previous invocation
			} else if (stateVal == 4) { // whether sync or async job have to
										// invoke the recovering because it
										// crashed in the Handler
				reInvokeProviderExecute(jobExecutionContext);
			} else if (stateVal >= 5 && GFacUtils.isSynchronousMode(jobExecutionContext)) {
				// In this case we do nothing because provider ran successfully,
				// no need to re-run the job
				log.info("Provider does not have to be recovered because it ran successfully for experiment: " + experimentID);
			} else if (stateVal == 5 && !GFacUtils.isSynchronousMode(jobExecutionContext)) {
				// this is async mode where monitoring of jobs is hapenning, we
				// have to recover
				reInvokeProviderExecute(jobExecutionContext);
			} else if (stateVal == 6) {
				reInvokeOutFlowHandlers(jobExecutionContext);
			} else {
				log.info("We skip invoking Handler, because the experiment:" + stateVal + " state is beyond the Provider Invocation !!!");
				log.info("ExperimentId: " + experimentID + " taskId: " + jobExecutionContext.getTaskData().getTaskID());
			}
		} catch (Exception e) {
			try {
				// we make the experiment as failed due to exception scenario
				monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.FAILED));
				// monitorPublisher.publish(new
				// ExperimentStatusChangedEvent(new
				// ExperimentIdentity(jobExecutionContext.getExperimentID()),
				// ExperimentState.FAILED));
				// Updating the task status if there's any task associated
				// monitorPublisher.publish(new TaskStatusChangedEvent(
				// new TaskIdentity(jobExecutionContext.getExperimentID(),
				// jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
				// jobExecutionContext.getTaskData().getTaskID()),
				// TaskState.FAILED
				// ));
				monitorPublisher.publish(new JobStatusChangeRequest(new MonitorID(jobExecutionContext), new JobIdentity(jobExecutionContext.getExperimentID(),
						jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext
								.getJobDetails().getJobID()), JobState.FAILED));
			} catch (NullPointerException e1) {
				log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
						+ "NullPointerException occurred because at this point there might not have Job Created", e1, e);
//				monitorPublisher
//						.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
				// Updating the task status if there's any task associated
				monitorPublisher.publish(new TaskStatusChangedEvent(new TaskIdentity(jobExecutionContext.getExperimentID(), jobExecutionContext
						.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID()), TaskState.FAILED));

			}
			jobExecutionContext.setProperty(ERROR_SENT, "true");
			jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(e.getCause()));
			throw new GFacException(e.getMessage(), e);
		}
    }

	private void launch(JobExecutionContext jobExecutionContext) throws GFacException {
		// Scheduler will decide the execution flow of handlers and provider
		// which handles
		// the job.
		try {
			Scheduler.schedule(jobExecutionContext);

			// Executing in handlers in the order as they have configured in
			// GFac configuration
			// here we do not skip handler if some handler does not have to be
			// run again during re-run it can implement
			// that logic in to the handler
            if (!isCancelled()) {
                invokeInFlowHandlers(jobExecutionContext); // to keep the
                // consistency we always
                // try to re-run to
                // avoid complexity
            }else{
                log.info("Experiment is cancelled, so launch operation is stopping immediately");
                return; // if the job is cancelled, status change is handled in cancel operation this thread simply has to be returned
            }
            // if (experimentID != null){
			// registry2.changeStatus(jobExecutionContext.getExperimentID(),AiravataJobState.State.INHANDLERSDONE);
			// }

			// After executing the in handlers provider instance should be set
			// to job execution context.
			// We get the provider instance and execute it.
            if (!isCancelled()) {
                invokeProviderExecute(jobExecutionContext);
            } else {
                log.info("Experiment is cancelled, so launch operation is stopping immediately");
                return;
            }
            } catch (Exception e) {
			try {
				// we make the experiment as failed due to exception scenario
				monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.FAILED));
				// monitorPublisher.publish(new
				// ExperimentStatusChangedEvent(new
				// ExperimentIdentity(jobExecutionContext.getExperimentID()),
				// ExperimentState.FAILED));
				// Updating the task status if there's any task associated
				// monitorPublisher.publish(new TaskStatusChangeRequest(
				// new TaskIdentity(jobExecutionContext.getExperimentID(),
				// jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
				// jobExecutionContext.getTaskData().getTaskID()),
				// TaskState.FAILED
				// ));
				monitorPublisher.publish(new JobStatusChangeRequest(new MonitorID(jobExecutionContext), new JobIdentity(jobExecutionContext.getExperimentID(),
						jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext
								.getJobDetails().getJobID()), JobState.FAILED));
			} catch (NullPointerException e1) {
				log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
						+ "NullPointerException occurred because at this point there might not have Job Created", e1, e);
				//monitorPublisher.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
				// Updating the task status if there's any task associated
				monitorPublisher.publish(new TaskStatusChangeRequest(new TaskIdentity(jobExecutionContext.getExperimentID(), jobExecutionContext
						.getWorkflowNodeDetails().getNodeInstanceId(), jobExecutionContext.getTaskData().getTaskID()), TaskState.FAILED));

			}
			jobExecutionContext.setProperty(ERROR_SENT, "true");
			jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(e.getCause()));
			throw new GFacException(e.getMessage(), e);
		}
    }

    private void invokeProviderExecute(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException, InterruptedException, KeeperException {
        GFacProvider provider = jobExecutionContext.getProvider();
        if (provider != null) {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKING));
            GFacUtils.createPluginZnode(zk, jobExecutionContext, provider.getClass().getName());
            initProvider(provider, jobExecutionContext);
            executeProvider(provider, jobExecutionContext);
            disposeProvider(provider, jobExecutionContext);
            GFacUtils.updatePluginState(zk, jobExecutionContext, provider.getClass().getName(), GfacPluginState.COMPLETED);
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKED));
        }
        if (GFacUtils.isSynchronousMode(jobExecutionContext)) {
            invokeOutFlowHandlers(jobExecutionContext);
        }
    }

    private void reInvokeProviderExecute(JobExecutionContext jobExecutionContext) throws GFacException, GFacProviderException, ApplicationSettingsException, InterruptedException, KeeperException {
        GFacProvider provider = jobExecutionContext.getProvider();
        if (provider != null) {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKING));
            String plState = GFacUtils.getPluginState(zk, jobExecutionContext, provider.getClass().getName());
            if (Integer.valueOf(plState) >= GfacPluginState.INVOKED.getValue()) {    // this will make sure if a plugin crashes it will not launch from the scratch, but plugins have to save their invoked state
                if (provider instanceof GFacRecoverableProvider) {
                    GFacUtils.createPluginZnode(zk, jobExecutionContext, provider.getClass().getName());
                    ((GFacRecoverableProvider) provider).recover(jobExecutionContext);
                    GFacUtils.updatePluginState(zk, jobExecutionContext, provider.getClass().getName(), GfacPluginState.COMPLETED);
                }
            } else {
                GFacUtils.createPluginZnode(zk, jobExecutionContext, provider.getClass().getName());
                initProvider(provider, jobExecutionContext);
                executeProvider(provider, jobExecutionContext);
                disposeProvider(provider, jobExecutionContext);
                GFacUtils.updatePluginState(zk, jobExecutionContext, provider.getClass().getName(), GfacPluginState.COMPLETED);
            }
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKED));
        }

        if (GFacUtils.isSynchronousMode(jobExecutionContext))

        {
            invokeOutFlowHandlers(jobExecutionContext);
        }

    }

    private void invokeProviderCancel(JobExecutionContext jobExecutionContext) throws GFacException, ApplicationSettingsException, InterruptedException, KeeperException {
        GFacProvider provider = jobExecutionContext.getProvider();
        if (provider != null) {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKING));
            initProvider(provider, jobExecutionContext);
            cancelProvider(provider, jobExecutionContext);
            disposeProvider(provider, jobExecutionContext);
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKED));
        }
        if (GFacUtils.isSynchronousMode(jobExecutionContext)) {
            invokeOutFlowHandlers(jobExecutionContext);
        }
    }

    private void reInvokeProviderCancel(JobExecutionContext jobExecutionContext) throws GFacException, GFacProviderException, ApplicationSettingsException, InterruptedException, KeeperException {
        GFacProvider provider = jobExecutionContext.getProvider();
        if (provider != null) {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKING));
            String plState = GFacUtils.getPluginState(zk, jobExecutionContext, provider.getClass().getName());
            if (Integer.valueOf(plState) >= GfacPluginState.INVOKED.getValue()) {    // this will make sure if a plugin crashes it will not launch from the scratch, but plugins have to save their invoked state
                if (provider instanceof GFacRecoverableProvider) {
                    GFacUtils.createPluginZnode(zk, jobExecutionContext, provider.getClass().getName());
                    ((GFacRecoverableProvider) provider).recover(jobExecutionContext);
                    GFacUtils.updatePluginState(zk, jobExecutionContext, provider.getClass().getName(), GfacPluginState.COMPLETED);
                }
            } else {
                GFacUtils.createPluginZnode(zk, jobExecutionContext, provider.getClass().getName());
                initProvider(provider, jobExecutionContext);
                cancelProvider(provider, jobExecutionContext);
                disposeProvider(provider, jobExecutionContext);
                GFacUtils.updatePluginState(zk, jobExecutionContext, provider.getClass().getName(), GfacPluginState.COMPLETED);
            }
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.PROVIDERINVOKED));
        }

        if (GFacUtils.isSynchronousMode(jobExecutionContext))

        {
            invokeOutFlowHandlers(jobExecutionContext);
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

    private void cancelProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.cancelJob(jobExecutionContext);
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
        try {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.INHANDLERSINVOKING));
            for (GFacHandlerConfig handlerClassName : handlers) {
                if(!isCancelled()) {
                    Class<? extends GFacHandler> handlerClass;
                    GFacHandler handler;
                    try {
                        GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName());
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
                        GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                        // if exception thrown before that we do not make it finished
                    } catch (GFacHandlerException e) {
                        throw new GFacException("Error Executing a InFlow Handler", e.getCause());
                    }
                }else{
                    log.info("Experiment execution is cancelled, so InHandler invocation is going to stop");
                    break;
                }
            }
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.INHANDLERSINVOKED));
        } catch (Exception e) {
            throw new GFacException("Error invoking ZK", e);
        }
    }

    public void invokeOutFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        GFacConfiguration gFacConfiguration = jobExecutionContext.getGFacConfiguration();
        List<GFacHandlerConfig> handlers = null;
        if (gFacConfiguration != null) {
            handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();
        } else {
            try {
                jobExecutionContext = createJEC(jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext.getGatewayID());
            } catch (Exception e) {
                log.error("Error constructing job execution context during outhandler invocation");
                throw new GFacException(e);
            }
        }
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.OUTHANDLERSINVOKING));
        for (GFacHandlerConfig handlerClassName : handlers) {
            if(!isCancelled()) {
                Class<? extends GFacHandler> handlerClass;
                GFacHandler handler;
                try {
                    GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName());
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
                } catch (Exception e) {
                    throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
                }
                try {
                    handler.invoke(jobExecutionContext);
                    GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                } catch (Exception e) {
                    // TODO: Better error reporting.
                    throw new GFacException("Error Executing a OutFlow Handler", e);
                }
            }else{
                log.info("Experiment execution is cancelled, so OutHandler invocation is going to stop");
                break;
            }
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.OUTHANDLERSINVOKED));
        }

        // At this point all the execution is finished so we update the task and experiment statuses.
        // Handler authors does not have to worry about updating experiment or task statuses.
//        monitorPublisher.publish(new
//                ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()),
//                ExperimentState.COMPLETED));
        // Updating the task status if there's any task associated
        monitorPublisher.publish(new TaskStatusChangeRequest(
                new TaskIdentity(jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getTaskData().getTaskID()), TaskState.COMPLETED
        ));
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.COMPLETED));
    }

    /**
     * If handlers ran successfully we re-run only recoverable handlers
     * If handler never ran we run the normal invoke method
     *
     * @param jobExecutionContext
     * @throws GFacException
     */
    private void reInvokeInFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<GFacHandlerConfig> handlers = jobExecutionContext.getGFacConfiguration().getInHandlers();
        try {
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.INHANDLERSINVOKING));
            for (GFacHandlerConfig handlerClassName : handlers) {
                Class<? extends GFacHandler> handlerClass;
                GFacHandler handler;
                try {
                    handlerClass = Class.forName(handlerClassName.getClassName().trim()).asSubclass(GFacHandler.class);
                    handler = handlerClass.newInstance();
                    String plState = GFacUtils.getPluginState(zk, jobExecutionContext, handlerClassName.getClassName());
                    int state = 0;
                    try {
                        state = Integer.valueOf(plState);
                    } catch (NumberFormatException e) {

                    }
                    if (state >= GfacPluginState.INVOKED.getValue()) {
                        if (handler instanceof GFacRecoverableHandler) {
                            // if these already ran we re-run only recoverable handlers
                            log.info(handlerClassName.getClassName() + " is a recoverable handler so we recover the handler");
                            GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.INVOKING);
                            ((GFacRecoverableHandler) handler).recover(jobExecutionContext);
                            GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                        } else {
                            log.info(handlerClassName.getClassName() + " is not a recoverable handler so we do not run because it already ran in last-run");
                        }
                    } else {
                        log.info(handlerClassName.getClassName() + " never ran so we run this is normal mode");
                        GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.INVOKING);
                        handler.initProperties(handlerClassName.getProperties());
                        handler.invoke(jobExecutionContext);
                        GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                    }
                } catch (GFacHandlerException e) {
                    throw new GFacException("Error Executing a InFlow Handler", e.getCause());
                } catch (ClassNotFoundException e) {
                    throw new GFacException("Cannot load handler class " + handlerClassName, e);
                } catch (InstantiationException e) {
                    throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
                } catch (IllegalAccessException e) {
                    throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
                }
            }
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.INHANDLERSINVOKED));
        } catch (Exception e) {
            throw new GFacException("Error invoking ZK", e);
        }
    }

    public void reInvokeOutFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        GFacConfiguration gFacConfiguration = jobExecutionContext.getGFacConfiguration();
        List<GFacHandlerConfig> handlers = null;
        if (gFacConfiguration != null) {
            handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();
        } else {
            try {
                jobExecutionContext = createJEC(jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getTaskData().getTaskID(), jobExecutionContext.getGatewayID());
            } catch (Exception e) {
                log.error("Error constructing job execution context during outhandler invocation");
                throw new GFacException(e);
            }
            launch(jobExecutionContext);
        }
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.OUTHANDLERSINVOKING));
        for (GFacHandlerConfig handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.getClassName().trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
                String plState = GFacUtils.getPluginState(zk, jobExecutionContext, handlerClassName.getClassName());
                if (Integer.valueOf(plState) >= GfacPluginState.INVOKED.getValue()) {
                    if (handler instanceof GFacRecoverableHandler) {
                        // if these already ran we re-run only recoverable handlers
                        log.info(handlerClassName.getClassName() + " is a recoverable handler so we recover the handler");
                        GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.INVOKING);
                        ((GFacRecoverableHandler) handler).recover(jobExecutionContext);
                        GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                    } else {
                        log.info(handlerClassName.getClassName() + " is not a recoverable handler so we do not run because it already ran in last-run");
                    }
                } else {
                    log.info(handlerClassName.getClassName() + " never ran so we run this is normal mode");
                    GFacUtils.createPluginZnode(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.INVOKING);
                    handler.initProperties(handlerClassName.getProperties());
                    handler.invoke(jobExecutionContext);
                    GFacUtils.updatePluginState(zk, jobExecutionContext, handlerClassName.getClassName(), GfacPluginState.COMPLETED);
                }
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (Exception e) {
                // TODO: Better error reporting.
                throw new GFacException("Error Executing a OutFlow Handler", e);
            }
        }
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.OUTHANDLERSINVOKED));

        // At this point all the execution is finished so we update the task and experiment statuses.
        // Handler authors does not have to worry about updating experiment or task statuses.
//        monitorPublisher.publish(new
//                ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()),
//                ExperimentState.COMPLETED));
        // Updating the task status if there's any task associated
        
        monitorPublisher.publish(new TaskStatusChangedEvent(
                new TaskIdentity(jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getTaskData().getTaskID()), TaskState.COMPLETED
        ));
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.COMPLETED));
    }


    public static void setMonitorPublisher(MonitorPublisher monitorPublisher) {
        BetterGfacImpl.monitorPublisher = monitorPublisher;
    }

//    public AiravataAPI getAiravataAPI() {
//        return airavataAPI;
//    }

//    public AiravataRegistry2 getAiravataRegistry2() {
//        return airavataRegistry2;
//    }

    public static List<ThreadedHandler> getDaemonHandlers() {
        return daemonHandlers;
    }

    public static String getErrorSent() {
        return ERROR_SENT;
    }

    public File getGfacConfigFile() {
        return gfacConfigFile;
    }

    public static MonitorPublisher getMonitorPublisher() {
        return monitorPublisher;
    }

    public Registry getRegistry() {
        return registry;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void process(WatchedEvent watchedEvent) {
        if(Event.EventType.NodeDataChanged.equals(watchedEvent.getType())){
            // node data is changed, this means node is cancelled.
            log.info("Experiment is cancelled with this path:"+watchedEvent.getPath());
            this.cancelled = true;
        }
    }

    public static ExecutorService getCachedThreadPool(){
        return cachedThreadPool;
    }
}
