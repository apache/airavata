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
import org.apache.airavata.gfac.core.monitor.state.TaskStatusChangedEvent;
import org.apache.airavata.gfac.core.monitor.state.WorkflowNodeStatusChangedEvent;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeDetails;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeState;
import org.apache.airavata.model.workspace.experiment.WorkflowNodeStatus;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AiravataWorkflowNodeStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataWorkflowNodeStatusUpdator.class);

    private Registry airavataRegistry;

    private MonitorPublisher monitorPublisher;

    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }

    @Subscribe
    public void setupWorkflowNodeStatus(TaskStatusChangedEvent taskStatus){
    	WorkflowNodeState state=WorkflowNodeState.UNKNOWN;
    	switch(taskStatus.getState()){
    	case CANCELED:
    		state=WorkflowNodeState.CANCELED; break;
    	case COMPLETED:
    		state=WorkflowNodeState.COMPLETED; break;
    	case CONFIGURING_WORKSPACE:
    		state=WorkflowNodeState.INVOKED; break;
    	case FAILED:
    		state=WorkflowNodeState.FAILED; break;
    	case EXECUTING: case WAITING: case PRE_PROCESSING: case POST_PROCESSING: case OUTPUT_DATA_STAGING: case INPUT_DATA_STAGING:
    		state=WorkflowNodeState.EXECUTING; break;
    	case STARTED:
    		state=WorkflowNodeState.INVOKED; break;
    	case CANCELING:
    		state=WorkflowNodeState.CANCELING; break;
		default:
			return;
    	}
    	try {
			updateWorkflowNodeStatus(taskStatus.getIdentity().getWorkflowNodeID(), state);
			logger.debug("Publishing workflow node status for "+taskStatus.getIdentity().getWorkflowNodeID()+":"+state.toString());
			monitorPublisher.publish(new WorkflowNodeStatusChangedEvent(taskStatus.getIdentity(),state));
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
		}
    }

    public  void updateWorkflowNodeStatus(String workflowNodeId, WorkflowNodeState state) throws Exception {
		logger.debug("Updating workflow node status for "+workflowNodeId+":"+state.toString());
    	WorkflowNodeDetails details = (WorkflowNodeDetails)airavataRegistry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeId);
        if(details == null) {
            details = new WorkflowNodeDetails();
            details.setNodeInstanceId(workflowNodeId);
        }
        WorkflowNodeStatus status = new WorkflowNodeStatus();
        status.setWorkflowNodeState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setWorkflowNodeStatus(status);
        airavataRegistry.update(RegistryModelType.WORKFLOW_NODE_STATUS, status, workflowNodeId);
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
