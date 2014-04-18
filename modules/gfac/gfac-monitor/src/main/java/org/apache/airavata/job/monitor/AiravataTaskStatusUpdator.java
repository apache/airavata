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
package org.apache.airavata.job.monitor;

import java.util.Calendar;

import org.apache.airavata.job.monitor.event.MonitorPublisher;
import org.apache.airavata.job.monitor.state.ExperimentStatus;
import org.apache.airavata.job.monitor.state.TaskStatus;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.registry.cpi.DataType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AiravataTaskStatusUpdator implements AbstractActivityListener{
    private final static Logger logger = LoggerFactory.getLogger(AiravataTaskStatusUpdator.class);

    private Registry airavataRegistry;

    private MonitorPublisher monitorPublisher;
    
    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }

    @Subscribe
    public void updateRegistry(TaskStatus taskStatus) {
        TaskState state = taskStatus.getState();
        if (state != null) {
            try {
                String taskID = taskStatus.getMonitorID().getTaskID();
                updateTaskStatus(taskID, state);
            } catch (Exception e) {
                logger.error("Error persisting data" + e.getLocalizedMessage(), e);
            }
        }
    }
    
    @Subscribe
    public void setupExperimentStatus(TaskStatus taskStatus){
    	ExperimentState state=ExperimentState.UNKNOWN;
    	switch(taskStatus.getState()){
    	case CANCELED:
    		state=ExperimentState.CANCELED; break;
    	case COMPLETED:
    		state=ExperimentState.COMPLETED; break;
    	case CONFIGURING_WORKSPACE:
    		state=ExperimentState.LAUNCHED; break;
    	case FAILED:
    		state=ExperimentState.FAILED; break;
    	case EXECUTING: case WAITING: case PRE_PROCESSING: case POST_PROCESSING: case OUTPUT_DATA_STAGING: case INPUT_DATA_STAGING:
    		state=ExperimentState.EXECUTING; break;
    	case STARTED:
    		state=ExperimentState.LAUNCHED; break;
		default:
			break;
    	}
    	logger.debug("Publishing Experiment Status "+state.toString());
    	monitorPublisher.publish(new ExperimentStatus(taskStatus.getMonitorID(),state));
    }
    
    public  void updateTaskStatus(String taskId, TaskState state) throws Exception {
    	TaskDetails details = (TaskDetails)airavataRegistry.get(DataType.TASK_DETAIL, taskId);
        if(details == null) {
            details = new TaskDetails();
            details.setTaskID(taskId);
        }
        org.apache.airavata.model.workspace.experiment.TaskStatus status = new org.apache.airavata.model.workspace.experiment.TaskStatus();
        status.setExecutionState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setTaskStatus(status);
        airavataRegistry.update(org.apache.airavata.registry.cpi.DataType.TASK_DETAIL, details, taskId);
    }

	@Override
	public void setup(Object... configurations) {
		for (Object configuration : configurations) {
			if (configuration instanceof Registry){
				this.airavataRegistry=(Registry)configuration;
			} else if (configuration instanceof MonitorPublisher){
				this.monitorPublisher=(MonitorPublisher) configuration;
			} 
		}
	}
}
