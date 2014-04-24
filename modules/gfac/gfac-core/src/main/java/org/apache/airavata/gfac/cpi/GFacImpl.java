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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.Scheduler;
import org.apache.airavata.gfac.context.ApplicationContext;
import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.context.MessageContext;
import org.apache.airavata.gfac.handler.GFacHandler;
import org.apache.airavata.gfac.handler.GFacHandlerConfig;
import org.apache.airavata.gfac.handler.GFacHandlerException;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.notification.listeners.LoggingListener;
import org.apache.airavata.gfac.notification.listeners.WorkflowTrackingListener;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.scheduler.HostScheduler;
import org.apache.airavata.gfac.utils.GFacUtils;
import org.apache.airavata.gfac.monitor.AbstractActivityListener;
import org.apache.airavata.gfac.monitor.MonitorManager;
import org.apache.airavata.gfac.monitor.command.ExperimentCancelRequest;
import org.apache.airavata.gfac.monitor.command.TaskCancelRequest;
import org.apache.airavata.model.workspace.experiment.DataObjectType;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.persistance.registry.jpa.resources.AbstractResource.TaskDetailConstants;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

/**
 * This is the GFac CPI class for external usage, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public class GFacImpl implements GFac, AbstractActivityListener {
    private static final Logger log = LoggerFactory.getLogger(GFacImpl.class);
    public static final String ERROR_SENT = "ErrorSent";
    public static final String PBS_JOB_MANAGER = "pbs";
    public static final String SLURM_JOB_MANAGER = "slurm";
    public static final String SUN_GRID_ENGINE_JOB_MANAGER = "sge";

    private Registry registry;

    private AiravataAPI airavataAPI;

    private AiravataRegistry2 airavataRegistry2;
    
    private MonitorManager monitorManager;

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
            jobExecutionContext = createJEC(experimentID, taskID);

            return  submitJob(jobExecutionContext);
        } catch (Exception e) {
            log.error("Error inovoking the job with experiment ID: " + experimentID);
            throw new GFacException(e);
        }
    }

    private JobExecutionContext createJEC(String experimentID, String taskID) throws Exception {
        JobExecutionContext jobExecutionContext;
        TaskDetails taskData = (TaskDetails) registry.get(DataType.TASK_DETAIL, taskID);
        // this is wear our new model and old model is mapping (so serviceName in ExperimentData and service name in ServiceDescriptor
        // has to be same.

        // 1. Get the Task from the task ID and construct the Job object and save it in to registry
        // 2. Add another property to jobExecutionContext and read them inside the provider and use it.
        String serviceName = taskData.getApplicationId();
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
        String hostName; 
        if(taskData.getTaskScheduling().getResourceHostId() != null){
        	hostName = taskData.getTaskScheduling().getResourceHostId();
        }else{
        	hostName = hostDescription.getType().getHostName();
        }
      
		ApplicationDescription applicationDescription = airavataRegistry2.getApplicationDescriptors(serviceName, hostName);
        URL resource = GFacImpl.class.getClassLoader().getResource(org.apache.airavata.common.utils.Constants.GFAC_CONFIG_XML);
        Properties configurationProperties = ServerSettings.getProperties();
        GFacConfiguration gFacConfiguration = GFacConfiguration.create(new File(resource.getPath()), airavataAPI, configurationProperties);

        jobExecutionContext = new JobExecutionContext(gFacConfiguration, serviceName);
        jobExecutionContext.setRegistry(registry);
        jobExecutionContext.setTaskData(taskData);

        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.setApplicationDeploymentDescription(applicationDescription);
        applicationContext.setHostDescription(hostDescription);
        applicationContext.setServiceDescription(serviceDescription);
        jobExecutionContext.setApplicationContext(applicationContext);

        List<DataObjectType> experimentInputs = taskData.getApplicationInputs();
        jobExecutionContext.setInMessageContext(new MessageContext(GFacUtils.getMessageContext(experimentInputs,
                serviceDescription.getType().getInputParametersArray())));

        List<DataObjectType> outputData = taskData.getApplicationOutputs();
        jobExecutionContext.setOutMessageContext(new MessageContext(GFacUtils.getMessageContext(outputData,
                serviceDescription.getType().getOutputParametersArray())));

        jobExecutionContext.setProperty(Constants.PROP_TOPIC, experimentID);
        jobExecutionContext.setExperimentID(experimentID);

        return jobExecutionContext;
    }

    public JobExecutionContext submitJob(JobExecutionContext jobExecutionContext) throws GFacException {
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
        return jobExecutionContext;
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
        GFacConfiguration gFacConfiguration = jobExecutionContext.getGFacConfiguration();
        List<GFacHandlerConfig> handlers = null;
        if(gFacConfiguration != null){
         handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();
        }else {
            try {
                jobExecutionContext = createJEC(jobExecutionContext.getExperimentID(), jobExecutionContext.getTaskData().getTaskID());
            } catch (Exception e) {
                log.error("Error constructing job execution context during outhandler invocation");
                throw new GFacException(e);
            }
            schedule(jobExecutionContext);
        }
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



	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof MonitorManager){
				monitorManager=(MonitorManager) configuration;
			} 	
		}
			
	}
	
	@Subscribe
	public void experimentCancelRequested(ExperimentCancelRequest request){
		try {
			List<String> nodeIds = registry.getIds(DataType.WORKFLOW_NODE_DETAIL, WorkflowNodeConstants.EXPERIMENT_ID, request.getExperimentId());
			for (String nodeId : nodeIds) {
				List<String> taskIds = registry.getIds(DataType.TASK_DETAIL, TaskDetailConstants.NODE_INSTANCE_ID, nodeId);
				for (String taskId : taskIds) {
					monitorManager.getMonitorPublisher().publish(new TaskCancelRequest(request.getExperimentId(),nodeId, taskId));
				}
			}
		} catch (RegistryException e) {
			log.error("Error while attempting to publish task cancel requests!!!",e);
		}
	}

}
