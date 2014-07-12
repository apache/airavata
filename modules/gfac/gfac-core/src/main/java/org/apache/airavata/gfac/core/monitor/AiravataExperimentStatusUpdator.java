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

import com.google.common.eventbus.Subscribe;

import org.apache.airavata.gfac.core.monitor.state.ExperimentStatusChangedEvent;
import org.apache.airavata.gfac.core.monitor.state.WorkflowNodeStatusChangedEvent;
import org.apache.airavata.gfac.core.notification.MonitorPublisher;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

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
    public void updateRegistry(ExperimentStatusChangedEvent experimentStatus) {
        ExperimentState state = experimentStatus.getState();
        if (state != null) {
            try {
                String experimentID = experimentStatus.getIdentity().getExperimentID();
                updateExperimentStatus(experimentID, state);
            } catch (Exception e) {
                logger.error("Error persisting data" + e.getLocalizedMessage(), e);
            }
        }
    }

    
    @Subscribe
    public void setupExperimentStatus(WorkflowNodeStatusChangedEvent nodeStatus) {
        ExperimentState state = ExperimentState.UNKNOWN;
        switch (nodeStatus.getState()) {
            case CANCELED:
                state = ExperimentState.CANCELED;
                break;
            case COMPLETED:
                state = ExperimentState.COMPLETED;
                break;
            case INVOKED:
                state = ExperimentState.LAUNCHED;
                break;
            case FAILED:
                state = ExperimentState.FAILED;
                break;
            case EXECUTING:
                state = ExperimentState.EXECUTING;
                break;
            case CANCELING:
                state = ExperimentState.CANCELING;
                break;
            default:
                break;
        }
		try {
			updateExperimentStatus(nodeStatus.getIdentity().getExperimentID(), state);
			logger.debug("Publishing experiment status for "+nodeStatus.getIdentity().getExperimentID()+":"+state.toString());
			monitorPublisher.publish(new ExperimentStatusChangedEvent(nodeStatus.getIdentity(), state));
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
		}
    }
    
    public  void updateExperimentStatus(String experimentId, ExperimentState state) throws Exception {
        logger.info("Updating the experiment status of experiment: " + experimentId + " to " + state.toString());
    	Experiment details = (Experiment)airavataRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
        if(details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        org.apache.airavata.model.workspace.experiment.ExperimentStatus status = new org.apache.airavata.model.workspace.experiment.ExperimentStatus();
        status.setExperimentState(state);
        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
        details.setExperimentStatus(status);
        airavataRegistry.update(RegistryModelType.EXPERIMENT_STATUS, status, experimentId);

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
