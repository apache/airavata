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
package org.apache.airavata.orchestrator.cpi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.gfac.monitor.AbstractActivityListener;
import org.apache.airavata.gfac.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.MonitorManager;
import org.apache.airavata.gfac.monitor.command.ExperimentCancelRequest;
import org.apache.airavata.gfac.monitor.core.Monitor;
import org.apache.airavata.gfac.monitor.core.PullMonitor;
import org.apache.airavata.gfac.monitor.core.PushMonitor;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.apache.airavata.gfac.monitor.impl.LocalJobMonitor;
import org.apache.airavata.gfac.monitor.impl.pull.qstat.QstatMonitor;
import org.apache.airavata.gfac.monitor.impl.push.amqp.AMQPMonitor;
import org.apache.airavata.gfac.monitor.state.JobStatusChangeRequest;
import org.apache.airavata.model.util.ExperimentModelUtil;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.job.JobSubmitter;
import org.apache.airavata.orchestrator.core.utils.OrchestratorUtils;
import org.apache.airavata.orchestrator.core.validator.JobMetadataValidator;
import org.apache.airavata.persistance.registry.jpa.model.WorkflowNodeDetail;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.schemas.gfac.GsisshHostType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class SimpleOrchestratorImpl extends AbstractOrchestrator implements AbstractActivityListener{
    private final static Logger logger = LoggerFactory.getLogger(SimpleOrchestratorImpl.class);
    private ExecutorService executor;
    
    // this is going to be null unless the thread count is 0
    private JobSubmitter jobSubmitter = null;

    private JobMetadataValidator jobMetadataValidator = null;

    private MonitorManager monitorManager = null;

    private AuthenticationInfo authenticationInfo = null;

    public SimpleOrchestratorImpl() throws OrchestratorException {
        try {
            try {
                String submitterClass = this.orchestratorContext.getOrchestratorConfiguration().getNewJobSubmitterClass();
                Class<? extends JobSubmitter> aClass = Class.forName(submitterClass.trim()).asSubclass(JobSubmitter.class);
                jobSubmitter = aClass.newInstance();
                jobSubmitter.initialize(this.orchestratorContext);
                monitorManager = new MonitorManager();
                String validatorClzz = this.orchestratorContext.getOrchestratorConfiguration().getValidatorClass();
                if (this.orchestratorConfiguration.isEnableValidation()) {
                    if (validatorClzz == null) {
                        logger.error("Job validation class is not properly set, so Validation will be turned off !");
                    }
                    Class<? extends JobMetadataValidator> vClass = Class.forName(validatorClzz.trim()).asSubclass(JobMetadataValidator.class);
                    jobMetadataValidator = vClass.newInstance();
                }
            } catch (Exception e) {
                String error = "Error creating JobSubmitter in non threaded mode ";
                logger.error(error);
                throw new OrchestratorException(error, e);
            }
        } catch (OrchestratorException e) {
            logger.error("Error Constructing the Orchestrator");
            throw e;
        }
    }

    public String launchExperiment(Experiment experiment, WorkflowNodeDetails workflowNode, TaskDetails task) throws OrchestratorException {
        // we give higher priority to userExperimentID
        //todo support multiple validators
        String jobID = null;
        String experimentId = experiment.getExperimentID();
        String taskId = task.getTaskID();
        String workflowNodeId = workflowNode.getNodeInstanceId();
        String userName = experiment.getUserName();
        // creating monitorID to register with monitoring queue
        // this is a special case because amqp has to be in place before submitting the job
        HostDescription hostDescription = OrchestratorUtils.getHostDescription(this, task);

        // creating monitorID to register with monitoring queue
        // this is a special case because amqp has to be in place before submitting the job
        try {
            if ((hostDescription instanceof GsisshHostType) &&
                    Constants.PUSH.equals(((GsisshHostType) hostDescription).getMonitorMode())) {
                MonitorID monitorID = new MonitorID(hostDescription, null, taskId, workflowNodeId, experimentId, userName);
                monitorManager.addAJobToMonitor(monitorID);
                jobSubmitter.submit(experimentId, taskId);
                if ("none".equals(jobID)) {
                    logger.error("Job submission Failed, so we remove the job from monitoring");

                } else {
                    logger.info("Job Launched to the resource by GFAC and jobID returned : " + jobID);
                }
            } else {
                // Launching job for each task
                // if the monitoring is pull mode then we add the monitorID for each task after submitting
                // the job with the jobID, otherwise we don't need the jobID
                jobSubmitter.submit(experimentId, taskId);
                logger.info("Job Launched to the resource by GFAC and jobID returned : " + jobID);
                MonitorID monitorID = new MonitorID(hostDescription, jobID, taskId, workflowNodeId, experimentId, userName, authenticationInfo);
                if ("none".equals(jobID)) {
                    logger.error("Job submission Failed, so we remove the job from monitoring");

                } else {
                    monitorManager.addAJobToMonitor(monitorID);
                }
            }
        } catch (Exception e) {
            throw new OrchestratorException("Error launching the job", e);
        }

        return jobID;
    }

    /**
     * This method will parse the ExperimentConfiguration and based on the configuration
     * we create a single or multiple tasks for the experiment.
     *
     * @param experimentId
     * @return
     * @throws OrchestratorException
     */
    public List<TaskDetails> createTasks(String experimentId) throws OrchestratorException {
        Experiment experiment = null;
        List<TaskDetails> tasks = new ArrayList<TaskDetails>();
        try {
            Registry newRegistry = orchestratorContext.getNewRegistry();
            experiment = (Experiment) newRegistry.get(DataType.EXPERIMENT, experimentId);


            WorkflowNodeDetails iDontNeedaNode = ExperimentModelUtil.createWorkflowNode("IDontNeedaNode", null);
            String nodeID = (String) newRegistry.add(ChildDataType.WORKFLOW_NODE_DETAIL, iDontNeedaNode, experimentId);

            TaskDetails taskDetails = ExperimentModelUtil.cloneTaskFromExperiment(experiment);
            taskDetails.setTaskID((String) newRegistry.add(ChildDataType.TASK_DETAIL, taskDetails, nodeID));
            tasks.add(taskDetails);
        } catch (Exception e) {
            throw new OrchestratorException("Error during creating a task");
        }
        return tasks;
    }

	@Override
	public void cancelExperiment(String experimentID)
			throws OrchestratorException {
		orchestratorContext.getMonitorManager().getMonitorPublisher().publish(new ExperimentCancelRequest(experimentID));
	}

    @Subscribe
    public void handlePostExperimentTask(JobStatusChangeRequest status) throws OrchestratorException {
        if(status.getState() == JobState.COMPLETE){
            MonitorID monitorID = status.getMonitorID();
            jobSubmitter.runAfterJobTask(monitorID.getExperimentID(), monitorID.getTaskID());
        }
    }
    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public JobMetadataValidator getJobMetadataValidator() {
        return jobMetadataValidator;
    }

    public void setJobMetadataValidator(JobMetadataValidator jobMetadataValidator) {
        this.jobMetadataValidator = jobMetadataValidator;
    }

    public JobSubmitter getJobSubmitter() {
        return jobSubmitter;
    }

    public void setJobSubmitter(JobSubmitter jobSubmitter) {
        this.jobSubmitter = jobSubmitter;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }

    @Override
	public void setup(Object... configurations) {
		for (Object config : configurations) {
			if (config instanceof MonitorManager){
				orchestratorContext.setMonitorManager((MonitorManager)config);
				try {
					getJobSubmitter().initialize(orchestratorContext);
				} catch (OrchestratorException e) {
					logger.error("Error reinitializing the job submitter!!!",e);
				}
			}
		}
	}

    public void initialize() throws OrchestratorException {
        // Filling monitorManager properties
            // we can keep a single user to do all the monitoring authentication for required machine..
        try{
            String myProxyUser = ServerSettings.getSetting("myproxy.username");
            String myProxyPass = ServerSettings.getSetting("myproxy.password");
            String certPath = ServerSettings.getSetting("trusted.cert.location");
            String myProxyServer = ServerSettings.getSetting("myproxy.server");
            setAuthenticationInfo(new MyProxyAuthenticationInfo(myProxyUser, myProxyPass, myProxyServer,
                    7512, 17280000, certPath));

            // loading Monitor configuration
            String monitors = ServerSettings.getSetting("monitors");
            if(monitors == null) {
                logger.error("No Monitor is configured, so job monitoring will not monitor any job");
                return;
            }
            List<String> monitorList = Arrays.asList(monitors.split(","));
            List<String> list = Arrays.asList(ServerSettings.getSetting("amqp.hosts").split(","));
            String proxyPath = ServerSettings.getSetting("proxy.file.path");
            String connectionName = ServerSettings.getSetting("connection.name");

            for (String monitorClass : monitorList) {
                Class<? extends Monitor> aClass = Class.forName(monitorClass).asSubclass(Monitor.class);
                Monitor monitor = aClass.newInstance();
                if (monitor instanceof PullMonitor) {
                    if (monitor instanceof QstatMonitor) {
                        monitorManager.addQstatMonitor((QstatMonitor) monitor);
                    }
                } else if (monitor instanceof PushMonitor) {
                    if (monitor instanceof AMQPMonitor) {
                        ((AMQPMonitor) monitor).initialize(proxyPath, connectionName, list);
                        monitorManager.addAMQPMonitor((AMQPMonitor) monitor);
                    }
                } else if(monitor instanceof LocalJobMonitor){
                    monitorManager.addLocalMonitor((LocalJobMonitor)monitor);
                } else {
                    logger.error("Wrong class is given to primary Monitor");
                }
            }

            monitorManager.registerListener(this);
            // Now Monitor Manager is properly configured, now we have to start the monitoring system.
            // This will initialize all the required threads and required queues
            monitorManager.launchMonitor();
        }  catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (AiravataMonitorException e) {
            e.printStackTrace();
        } catch (ApplicationSettingsException e) {
			e.printStackTrace();
		}
    }
}
