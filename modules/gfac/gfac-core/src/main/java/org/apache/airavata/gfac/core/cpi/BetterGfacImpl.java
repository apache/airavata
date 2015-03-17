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

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.Scheduler;
import org.apache.airavata.gfac.core.context.ApplicationContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.context.MessageContext;
import org.apache.airavata.gfac.core.handler.*;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.core.monitor.state.GfacExperimentStateChangeRequest;
import org.apache.airavata.gfac.core.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.core.notification.listeners.LoggingListener;
import org.apache.airavata.gfac.core.provider.GFacProvider;
import org.apache.airavata.gfac.core.provider.GFacProviderException;
import org.apache.airavata.gfac.core.provider.GFacRecoverableProvider;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.core.states.GfacPluginState;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.DataType;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.messaging.event.*;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This is the GFac CPI class for external usage, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public class BetterGfacImpl implements GFac,Watcher {
    private static final Logger log = LoggerFactory.getLogger(BetterGfacImpl.class);
    public static final String ERROR_SENT = "ErrorSent";

    private Registry registry;
    private AppCatalog appCatalog;

    // we are not storing zk instance in to jobExecution context
    private ZooKeeper zk;

    private static List<ThreadedHandler> daemonHandlers = new ArrayList<ThreadedHandler>();

    private static File gfacConfigFile;

    private static List<AbstractActivityListener> activityListeners = new ArrayList<AbstractActivityListener>();

    private static MonitorPublisher monitorPublisher;

    private boolean cancelled = false;

    /**
     * Constructor for GFac
     *
     * @param registry
     * @param zooKeeper
     */
    public BetterGfacImpl(Registry registry,  AppCatalog appCatalog, ZooKeeper zooKeeper,
                          MonitorPublisher publisher) {
        this.registry = registry;
        monitorPublisher = publisher;     // This is a EventBus common for gfac
        this.zk = zooKeeper;
        this.appCatalog = appCatalog;
    }

    public static void startStatusUpdators(Registry registry, ZooKeeper zk, MonitorPublisher publisher) {
        try {
            String[] listenerClassList = ServerSettings.getActivityListeners();
            Publisher rabbitMQPublisher = null;
            if (ServerSettings.isRabbitMqPublishEnabled()){
                rabbitMQPublisher = PublisherFactory.createPublisher();
            }
            for (String listenerClass : listenerClassList) {
                Class<? extends AbstractActivityListener> aClass = Class.forName(listenerClass).asSubclass(AbstractActivityListener.class);
                AbstractActivityListener abstractActivityListener = aClass.newInstance();
                activityListeners.add(abstractActivityListener);
                abstractActivityListener.setup(publisher, registry, zk, rabbitMQPublisher);
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
        } catch (AiravataException e) {
            log.error("Error loading the listener classes configured in airavata-server.properties", e);
        }
    }

    public static void startDaemonHandlers() {
        List<GFacHandlerConfig> daemonHandlerConfig = null;
        String className = null;
        try {
            URL resource = BetterGfacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
            if (resource != null) {
                gfacConfigFile = new File(resource.getPath());
            }
            daemonHandlerConfig = GFacConfiguration.getDaemonHandlers(gfacConfigFile);
            for (GFacHandlerConfig handlerConfig : daemonHandlerConfig) {
                className = handlerConfig.getClassName();
                Class<?> aClass = Class.forName(className).asSubclass(ThreadedHandler.class);
                ThreadedHandler threadedHandler = (ThreadedHandler) aClass.newInstance();
                threadedHandler.initProperties(handlerConfig.getProperties());
                daemonHandlers.add(threadedHandler);
            }
        } catch (ParserConfigurationException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (IOException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (SAXException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (XPathExpressionException e) {
            log.error("Error parsing gfac-config.xml, double check the xml configuration", e);
        } catch (ClassNotFoundException e) {
            log.error("Error initializing the handler: " + className);
            log.error(className + " class has to implement " + ThreadedHandler.class);
        } catch (InstantiationException e) {
            log.error("Error initializing the handler: " + className);
            log.error(className + " class has to implement " + ThreadedHandler.class);
        } catch (GFacHandlerException e) {
            log.error("Error initializing the handler: " + className);
            log.error(className + " class has to implement " + ThreadedHandler.class);
        } catch (IllegalAccessException e) {
            log.error("Error initializing the handler: " + className);
            log.error(className + " class has to implement " + ThreadedHandler.class);
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
    
    public BetterGfacImpl(Registry registry) {
    	this();
    	this.registry = registry;
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
            GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
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

        URL resource = BetterGfacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        Properties configurationProperties = ServerSettings.getProperties();
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), configurationProperties);

        // start constructing jobexecutioncontext
        jobExecutionContext = new JobExecutionContext(gFacConfiguration, applicationInterface.getApplicationName());

        // setting experiment/task/workflownode related information
        Experiment experiment = (Experiment) registry.get(RegistryModelType.EXPERIMENT, experimentID);
        jobExecutionContext.setExperiment(experiment);
        jobExecutionContext.setExperimentID(experimentID);
        jobExecutionContext.setWorkflowNodeDetails(experiment.getWorkflowNodeDetailsList().get(0));
        jobExecutionContext.setTaskData(taskData);
        jobExecutionContext.setGatewayID(gatewayID);
        jobExecutionContext.setAppCatalog(appCatalog);
        
      
        List<JobDetails> jobDetailsList = taskData.getJobDetailsList();
        //FIXME: Following for loop only set last jobDetails element to the jobExecutionContext
        for(JobDetails jDetails:jobDetailsList){
            jobExecutionContext.setJobDetails(jDetails);
        }
        // setting the registry
        jobExecutionContext.setRegistry(registry);

        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setComputeResourceDescription(computeResource);
        applicationContext.setApplicationDeploymentDescription(applicationDeployment);
        applicationContext.setApplicationInterfaceDescription(applicationInterface);
        applicationContext.setComputeResourcePreference(gatewayResourcePreferences);
        jobExecutionContext.setApplicationContext(applicationContext);


//        List<InputDataObjectType> experimentInputs = experiment.getExperimentInputs();
//        jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getInputParamMap(experimentInputs)));
        List<InputDataObjectType> taskInputs = taskData.getApplicationInputs();
        jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getInputParamMap(taskInputs)));

        jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
        jobExecutionContext.setGfac(this);
        jobExecutionContext.setZk(zk);
        jobExecutionContext.setCredentialStoreToken(AiravataZKUtils.getExpTokenId(zk, experimentID, taskID));

        // handle job submission protocol
        List<JobSubmissionInterface> jobSubmissionInterfaces = computeResource.getJobSubmissionInterfaces();
        if (jobSubmissionInterfaces != null && !jobSubmissionInterfaces.isEmpty()){
            Collections.sort(jobSubmissionInterfaces, new Comparator<JobSubmissionInterface>() {
                @Override
                public int compare(JobSubmissionInterface jobSubmissionInterface, JobSubmissionInterface jobSubmissionInterface2) {
                    return jobSubmissionInterface.getPriorityOrder() - jobSubmissionInterface2.getPriorityOrder();
                }
            });

            jobExecutionContext.setHostPrioritizedJobSubmissionInterfaces(jobSubmissionInterfaces);
        }else {
            throw new GFacException("Compute resource should have at least one job submission interface defined...");
        }
        // handle data movement protocol
        List<DataMovementInterface> dataMovementInterfaces = computeResource.getDataMovementInterfaces();
        if (dataMovementInterfaces != null && !dataMovementInterfaces.isEmpty()) {
            Collections.sort(dataMovementInterfaces, new Comparator<DataMovementInterface>() {
                @Override
                public int compare(DataMovementInterface dataMovementInterface, DataMovementInterface dataMovementInterface2) {
                    return dataMovementInterface.getPriorityOrder() - dataMovementInterface2.getPriorityOrder();
                }
            });
            jobExecutionContext.setHostPrioritizedDataMovementInterfaces(dataMovementInterfaces);
        }

        // set compute resource configuration as default preferred values, after that replace those with gateway user preferences.
        populateDefaultComputeResourceConfiguration(jobExecutionContext, applicationInterface, computeResource);
        populateResourceJobManager(jobExecutionContext);
        // if gateway resource preference is set
        if (gatewayResourcePreferences != null ) {
            if (gatewayResourcePreferences.getScratchLocation() == null) {
                gatewayResourcePreferences.setScratchLocation("/tmp");
            }
            setUpWorkingLocation(jobExecutionContext, applicationInterface, gatewayResourcePreferences.getScratchLocation());

            jobExecutionContext.setPreferredJobSubmissionProtocol(gatewayResourcePreferences.getPreferredJobSubmissionProtocol());
            if (gatewayResourcePreferences.getPreferredJobSubmissionProtocol() == null) {
                jobExecutionContext.setPreferredJobSubmissionInterface(jobExecutionContext.getHostPrioritizedJobSubmissionInterfaces().get(0));
                jobExecutionContext.setPreferredJobSubmissionProtocol(jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionProtocol());
            } else {
                for (JobSubmissionInterface jobSubmissionInterface : jobSubmissionInterfaces) {
                    if (gatewayResourcePreferences.getPreferredJobSubmissionProtocol() == jobSubmissionInterface.getJobSubmissionProtocol()) {
                        jobExecutionContext.setPreferredJobSubmissionInterface(jobSubmissionInterface);
                        break;
                    }
                }
            }

            if(gatewayResourcePreferences.getLoginUserName() != null){
                jobExecutionContext.setLoginUserName(gatewayResourcePreferences.getLoginUserName());
            }

            // set gatewayUserPreferred data movement protocol and interface
            jobExecutionContext.setPreferredDataMovementProtocol(gatewayResourcePreferences.getPreferredDataMovementProtocol());
            if (gatewayResourcePreferences.getPreferredJobSubmissionProtocol() == null) {
                jobExecutionContext.setPreferredDataMovementInterface(jobExecutionContext.getHostPrioritizedDataMovementInterfaces().get(0));
                jobExecutionContext.setPreferredDataMovementProtocol(jobExecutionContext.getPreferredDataMovementInterface().getDataMovementProtocol());
            } else {
            	// this check is to avoid NPE when job submission endpoints do 
            	// not contain any data movement interfaces. 
            	if((dataMovementInterfaces != null) && (!dataMovementInterfaces.isEmpty())) {
            		for (DataMovementInterface dataMovementInterface : dataMovementInterfaces) {
            			if (gatewayResourcePreferences.getPreferredDataMovementProtocol() == dataMovementInterface.getDataMovementProtocol()) {
            				jobExecutionContext.setPreferredDataMovementInterface(dataMovementInterface);
            				break;
                    	}
            		}
            	}
            }
        }  else {
            setUpWorkingLocation(jobExecutionContext, applicationInterface, "/tmp");
        }
        List<OutputDataObjectType> taskOutputs = taskData.getApplicationOutputs();
        if (taskOutputs == null || taskOutputs.isEmpty() ){
            taskOutputs = applicationInterface.getApplicationOutputs();
        }

        for (OutputDataObjectType objectType : taskOutputs){
            if (objectType.getType() == DataType.URI && objectType.getValue() != null){
                String filePath = objectType.getValue();
                // if output is not in working folder
                if (objectType.getLocation() != null && !objectType.getLocation().isEmpty()) {
                	if(objectType.getLocation().startsWith(File.separator)){
                		filePath = objectType.getLocation() + File.separator + filePath;
                    }else{
                    	filePath = jobExecutionContext.getOutputDir() + File.separator + objectType.getLocation() + File.separator + filePath;
                    }
                }else{
                	filePath = jobExecutionContext.getOutputDir() + File.separator + filePath;
                }
                objectType.setValue(filePath);
                
            }
            if (objectType.getType() == DataType.STDOUT){
                objectType.setValue(jobExecutionContext.getOutputDir() + File.separator + jobExecutionContext.getApplicationName() + ".stdout");
            }
            if (objectType.getType() == DataType.STDERR){
                objectType.setValue(jobExecutionContext.getOutputDir() + File.separator + jobExecutionContext.getApplicationName() + ".stderr");
            }
        }
        jobExecutionContext.setOutMessageContext(new MessageContext(GFacUtils.getOuputParamMap(taskOutputs)));
        return jobExecutionContext;
    }

    private void setUpWorkingLocation(JobExecutionContext jobExecutionContext, ApplicationInterfaceDescription applicationInterface, String scratchLocation) {
        /**
         * Scratch location
         */
        jobExecutionContext.setScratchLocation(scratchLocation);

        /**
         * Working dir
         */
        String workingDir = scratchLocation + File.separator + jobExecutionContext.getExperimentID();
        jobExecutionContext.setWorkingDir(workingDir);

            /*
            * Input and Output Directory
            */
//        jobExecutionContext.setInputDir(workingDir + File.separator + Constants.INPUT_DATA_DIR_VAR_NAME);
        jobExecutionContext.setInputDir(workingDir);
//        jobExecutionContext.setOutputDir(workingDir + File.separator + Constants.OUTPUT_DATA_DIR_VAR_NAME);
        jobExecutionContext.setOutputDir(workingDir);

            /*
            * Stdout and Stderr for Shell
            */
        jobExecutionContext.setStandardOutput(workingDir + File.separator + applicationInterface.getApplicationName().replaceAll("\\s+", "") + ".stdout");
        jobExecutionContext.setStandardError(workingDir + File.separator + applicationInterface.getApplicationName().replaceAll("\\s+", "") + ".stderr");
    }

    private void populateDefaultComputeResourceConfiguration(JobExecutionContext jobExecutionContext, ApplicationInterfaceDescription applicationInterface, ComputeResourceDescription computeResource) {
        Map<FileSystems, String> fileSystems = computeResource.getFileSystems();
        String scratchLocation = fileSystems.get(FileSystems.SCRATCH);
        if (scratchLocation != null) {
            setUpWorkingLocation(jobExecutionContext, applicationInterface, scratchLocation);
        }

        jobExecutionContext.setPreferredJobSubmissionInterface(jobExecutionContext.getHostPrioritizedJobSubmissionInterfaces().get(0));
        jobExecutionContext.setPreferredJobSubmissionProtocol(jobExecutionContext.getPreferredJobSubmissionInterface().getJobSubmissionProtocol());

        if (jobExecutionContext.getHostPrioritizedDataMovementInterfaces() != null) {
            jobExecutionContext.setPreferredDataMovementInterface(jobExecutionContext.getHostPrioritizedDataMovementInterfaces().get(0));
            jobExecutionContext.setPreferredDataMovementProtocol(jobExecutionContext.getPreferredDataMovementInterface().getDataMovementProtocol());
        }
    }

    private void populateResourceJobManager (JobExecutionContext jobExecutionContext) {
        try {
            JobSubmissionProtocol submissionProtocol = jobExecutionContext.getPreferredJobSubmissionProtocol();
            JobSubmissionInterface jobSubmissionInterface = jobExecutionContext.getPreferredJobSubmissionInterface();
            if (submissionProtocol == JobSubmissionProtocol.SSH) {
                SSHJobSubmission sshJobSubmission = GFacUtils.getSSHJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (sshJobSubmission != null){
                    jobExecutionContext.setResourceJobManager(sshJobSubmission.getResourceJobManager());
                }
            } else if (submissionProtocol == JobSubmissionProtocol.LOCAL){
                LOCALSubmission localJobSubmission = GFacUtils.getLocalJobSubmission(jobSubmissionInterface.getJobSubmissionInterfaceId());
                if (localJobSubmission != null){
                    jobExecutionContext.setResourceJobManager(localJobSubmission.getResourceJobManager());
                }
            }
        } catch (AppCatalogException e) {
           log.error("Error occured while retrieving job submission interface", e);
        }
    }

    private boolean submitJob(JobExecutionContext jobExecutionContext) throws GFacException {
        // We need to check whether this job is submitted as a part of a large workflow. If yes,
        // we need to setup workflow tracking listerner.
        try {
            String experimentEntry = GFacUtils.findExperimentEntry(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID(), zk);
            Stat exists = zk.exists(experimentEntry + File.separator + "operation", false);
            zk.getData(experimentEntry + File.separator + "operation", this, exists);
            int stateVal = GFacUtils.getZKExperimentStateValue(zk, jobExecutionContext);   // this is the original state came, if we query again it might be different,so we preserve this state in the environment
            monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext)
                    , GfacExperimentState.ACCEPTED));                  // immediately we get the request we update the status
            String workflowInstanceID = null;
            if ((workflowInstanceID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_INSTANCE_ID)) != null) {
                // This mean we need to register workflow tracking listener.
                //todo implement WorkflowTrackingListener properly
//                registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
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
            GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
            throw new GFacException("Error launching the Job",e);
        } catch (KeeperException e) {
            GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
            throw new GFacException("Error launching the Job",e);
        } catch (InterruptedException e) {
            GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
            throw new GFacException("Error launching the Job",e);
        }
    }

    public boolean cancel(String experimentID, String taskID, String gatewayID) throws GFacException {
        JobExecutionContext jobExecutionContext = null;
        try {
            jobExecutionContext = createJEC(experimentID, taskID, gatewayID);
            return cancel(jobExecutionContext);
        } catch (Exception e) {
            GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
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
//                registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
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
                        JobStatusChangeRequestEvent changeRequestEvent = new JobStatusChangeRequestEvent();
                        changeRequestEvent.setState(JobState.FAILED);
                        JobIdentifier jobIdentifier = new JobIdentifier(jobExecutionContext.getJobDetails().getJobID(),
                                                                        jobExecutionContext.getTaskData().getTaskID(),
                                                                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                                                                        jobExecutionContext.getExperimentID(),
                                                                        jobExecutionContext.getGatewayID());
                        changeRequestEvent.setJobIdentity(jobIdentifier);
                        monitorPublisher.publish(changeRequestEvent);
                    } catch (NullPointerException e1) {
                        log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
                                + "NullPointerException occurred because at this point there might not have Job Created", e1, e);
                        //monitorPublisher.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
                        // Updating the task status if there's any task associated
                        monitorPublisher.publish(new TaskStatusChangeRequestEvent(TaskState.FAILED,
                                                                                  new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                                                                                                     jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                                                                                                     jobExecutionContext.getExperimentID(),
                                                                                                     jobExecutionContext.getGatewayID())));

                    }
                    jobExecutionContext.setProperty(ERROR_SENT, "true");
                    jobExecutionContext.getNotifier().publish(new ExecutionFailEvent(e.getCause()));
                    throw new GFacException(e.getMessage(), e);
                }
            }
            return true;
        } catch (ApplicationSettingsException e) {
            log.error("Error occured while cancelling job for experiment : " + jobExecutionContext.getExperimentID(), e);
            throw new GFacException(e.getMessage(), e);
        } catch (KeeperException e) {
            log.error("Error occured while cancelling job for experiment : " + jobExecutionContext.getExperimentID(), e);
            throw new GFacException(e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error("Error occured while cancelling job for experiment : " + jobExecutionContext.getExperimentID(), e);
            throw new GFacException(e.getMessage(), e);
        }
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
                JobIdentifier jobIdentity = new JobIdentifier(
                        jobExecutionContext.getJobDetails().getJobID(), jobExecutionContext.getTaskData().getTaskID(),
                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getGatewayID());
				monitorPublisher.publish(new JobStatusChangeEvent(JobState.FAILED, jobIdentity));
                GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );
			} catch (NullPointerException e1) {
				log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
						+ "NullPointerException occurred because at this point there might not have Job Created", e1, e);
//				monitorPublisher
//						.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
				// Updating the task status if there's any task associated
                TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getGatewayID());
				monitorPublisher.publish(new TaskStatusChangeEvent(TaskState.FAILED, taskIdentity));
                GFacUtils.saveErrorDetails(jobExecutionContext, e.getLocalizedMessage(), CorrectiveAction.CONTACT_SUPPORT, ErrorCategory.AIRAVATA_INTERNAL_ERROR );

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
                JobIdentifier jobIdentity = new JobIdentifier(
                        jobExecutionContext.getJobDetails().getJobID(),jobExecutionContext.getTaskData().getTaskID(),jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getGatewayID());
				monitorPublisher.publish(new JobStatusChangeEvent(JobState.FAILED, jobIdentity));
			} catch (NullPointerException e1) {
				log.error("Error occured during updating the statuses of Experiments,tasks or Job statuses to failed, "
						+ "NullPointerException occurred because at this point there might not have Job Created", e1, e);
				//monitorPublisher.publish(new ExperimentStatusChangedEvent(new ExperimentIdentity(jobExecutionContext.getExperimentID()), ExperimentState.FAILED));
				// Updating the task status if there's any task associated
                TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                        jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                        jobExecutionContext.getExperimentID(),
                        jobExecutionContext.getGatewayID());
                monitorPublisher.publish(new TaskStatusChangeEvent(TaskState.FAILED, taskIdentity));

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

//    private void registerWorkflowTrackingListener(String workflowInstanceID, JobExecutionContext jobExecutionContext) {
//        String workflowNodeID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_NODE_ID);
//        String topic = (String) jobExecutionContext.getProperty(Constants.PROP_TOPIC);
//        String brokerUrl = (String) jobExecutionContext.getProperty(Constants.PROP_BROKER_URL);
//        jobExecutionContext.getNotificationService().registerListener(
//                new WorkflowTrackingListener(workflowInstanceID, workflowNodeID, brokerUrl, topic));
//
//    }

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
                    TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                            jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                            jobExecutionContext.getExperimentID(),
                            jobExecutionContext.getGatewayID());
                    monitorPublisher.publish(new TaskStatusChangeRequestEvent(TaskState.FAILED, taskIdentity));
                    throw new GFacException(e);
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
        TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                jobExecutionContext.getExperimentID(),
                jobExecutionContext.getGatewayID());
        monitorPublisher.publish(new TaskStatusChangeEvent(TaskState.COMPLETED, taskIdentity));
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

        TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                jobExecutionContext.getExperimentID(),
                jobExecutionContext.getGatewayID());
        monitorPublisher.publish(new TaskStatusChangeEvent(TaskState.COMPLETED, taskIdentity));
        monitorPublisher.publish(new GfacExperimentStateChangeRequest(new MonitorID(jobExecutionContext), GfacExperimentState.COMPLETED));
    }


    public static void setMonitorPublisher(MonitorPublisher monitorPublisher) {
        BetterGfacImpl.monitorPublisher = monitorPublisher;
    }

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
}
