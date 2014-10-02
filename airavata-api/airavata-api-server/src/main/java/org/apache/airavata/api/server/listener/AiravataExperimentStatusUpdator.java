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
package org.apache.airavata.api.server.listener;

import java.util.Calendar;

import org.apache.airavata.api.server.util.DataModelUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.gfac.core.monitor.state.WorkflowNodeStatusChangedEvent;
import org.apache.airavata.model.util.ExecutionType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class AiravataExperimentStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataExperimentStatusUpdator.class);

    private Registry airavataRegistry;
    private MonitorPublisher monitorPublisher;

    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }
    
    @Subscribe
    public void setupExperimentStatus(WorkflowNodeStatusChangedEvent nodeStatus) {
		try {
			boolean updateExperimentStatus=true;
	        ExperimentState state = ExperimentState.UNKNOWN;
	        switch (nodeStatus.getState()) {
	            case CANCELED:
	                state = ExperimentState.CANCELED; updateExperimentStatus = true;
	                break;
	            case COMPLETED:
	                state = ExperimentState.COMPLETED; updateExperimentStatus = false;
	                break;
	            case INVOKED:
	                state = ExperimentState.LAUNCHED; updateExperimentStatus = false;
	                break;
	            case FAILED:
	                state = ExperimentState.FAILED; updateExperimentStatus = true;
	                break;
	            case EXECUTING:
	                state = ExperimentState.EXECUTING; updateExperimentStatus = true;
	                break;
	            case CANCELING:
	                state = ExperimentState.CANCELING; updateExperimentStatus = true;
	                break;
	            default:
	                return;
	        }
	        if (!updateExperimentStatus){
				ExecutionType executionType = DataModelUtils.getExecutionType((Experiment) airavataRegistry.get(RegistryModelType.EXPERIMENT, nodeStatus.getIdentity().getExperimentID()));
				updateExperimentStatus=(executionType==ExecutionType.SINGLE_APP);
	        }
            state = updateExperimentStatus(nodeStatus.getIdentity().getExperimentID(), state);
            logger.debug("Publishing experiment status for "+nodeStatus.getIdentity().getExperimentID()+":"+state.toString());
			monitorPublisher.publish(new ExperimentStatusChangedEvent(nodeStatus.getIdentity(), state));
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
		}
    }
    
    public  ExperimentState updateExperimentStatus(String experimentId, ExperimentState state) throws Exception {
    	Experiment details = (Experiment)airavataRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
        if(details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        org.apache.airavata.model.workspace.experiment.ExperimentStatus status = new org.apache.airavata.model.workspace.experiment.ExperimentStatus();
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        if(!ExperimentState.CANCELED.equals(details.getExperimentStatus().getExperimentState())&&
                !ExperimentState.CANCELING.equals(details.getExperimentStatus().getExperimentState())) {
            status.setExperimentState(state);
        }else{
            status.setExperimentState(details.getExperimentStatus().getExperimentState());
        }
        details.setExperimentStatus(status);
        logger.info("Updating the experiment status of experiment: " + experimentId + " to " + status.getExperimentState().toString());
        airavataRegistry.update(RegistryModelType.EXPERIMENT_STATUS, status, experimentId);
        return details.getExperimentStatus().getExperimentState();

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
