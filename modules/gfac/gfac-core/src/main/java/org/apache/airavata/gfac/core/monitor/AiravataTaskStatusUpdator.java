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
package org.apache.airavata.gfac.core.monitor;

import java.util.Calendar;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.core.monitor.state.JobStatusChangedEvent;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangeRequest;
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangedEvent;
import org.apache.airavata.model.workspace.experiment.TaskDetails;
import org.apache.airavata.model.workspace.experiment.TaskState;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AiravataTaskStatusUpdator implements AbstractActivityListener {
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
    public void setupTaskStatus(TaskStatusChangeRequest taskStatus){
    	try {
			updateTaskStatus(taskStatus.getIdentity().getTaskId(), taskStatus.getState());
			logger.debug("Publishing task status for "+taskStatus.getIdentity().getTaskId()+":"+taskStatus.getState().toString());
			monitorPublisher.publish(new TaskStatusChangedEvent(taskStatus.getIdentity(),taskStatus.getState()));
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
		}
    }

    @Subscribe
    public void setupTaskStatus(JobStatusChangedEvent jobStatus){
    	TaskState state=TaskState.UNKNOWN;
    	switch(jobStatus.getState()){
    	case ACTIVE:
    		state=TaskState.EXECUTING; break;
    	case CANCELED:
    		state=TaskState.CANCELED; break;
    	case COMPLETE:
    		state=TaskState.COMPLETED; break;
    	case FAILED:
    		state=TaskState.FAILED; break;
    	case HELD: case SUSPENDED: case QUEUED:
    		state=TaskState.WAITING; break;
    	case SETUP:
    		state=TaskState.PRE_PROCESSING; break;
    	case SUBMITTED:
    		state=TaskState.STARTED; break;
    	case UN_SUBMITTED:
    		state=TaskState.CANCELED; break;
    	case CANCELING:
    		state=TaskState.CANCELING; break;
		default:
			break;
    	}
    	try {
			state = updateTaskStatus(jobStatus.getIdentity().getTaskId(), state);
			logger.debug("Publishing task status for "+jobStatus.getIdentity().getTaskId()+":"+state.toString());
			monitorPublisher.publish(new TaskStatusChangedEvent(jobStatus.getIdentity(),state));
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
		}
    }
    
    public  TaskState updateTaskStatus(String taskId, TaskState state) throws Exception {
    	TaskDetails details = (TaskDetails)airavataRegistry.get(RegistryModelType.TASK_DETAIL, taskId);
        if(details == null) {
            details = new TaskDetails();
            details.setTaskID(taskId);
        }
        org.apache.airavata.model.workspace.experiment.TaskStatus status = new org.apache.airavata.model.workspace.experiment.TaskStatus();
        if(!TaskState.CANCELED.equals(details.getTaskStatus().getExecutionState())
                && !TaskState.CANCELING.equals(details.getTaskStatus().getExecutionState())){
            status.setExecutionState(state);
        }else{
            status.setExecutionState(details.getTaskStatus().getExecutionState());
        }
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setTaskStatus(status);
        logger.debug("Updating task status for "+taskId+":"+details.getTaskStatus().toString());

        airavataRegistry.update(RegistryModelType.TASK_DETAIL, details, taskId);
        return details.getTaskStatus().getExecutionState();
    }

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
