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

import com.google.common.eventbus.Subscribe;
import org.apache.airavata.api.server.util.DataModelUtils;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;
import org.apache.airavata.model.util.ExecutionType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

public class AiravataExperimentStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataExperimentStatusUpdator.class);

    private Registry airavataRegistry;
    private MonitorPublisher monitorPublisher;
    private Publisher publisher;

    public Registry getAiravataRegistry() {
        return airavataRegistry;
    }

    public void setAiravataRegistry(Registry airavataRegistry) {
        this.airavataRegistry = airavataRegistry;
    }
    
    @Subscribe
    public void setupExperimentStatus(WorkflowNodeStatusChangeEvent nodeStatus) throws Exception{
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
				ExecutionType executionType = DataModelUtils.getExecutionType((Experiment) airavataRegistry.get(RegistryModelType.EXPERIMENT, nodeStatus.getWorkflowNodeIdentity().getExperimentId()));
				updateExperimentStatus=(executionType==ExecutionType.SINGLE_APP);
	        }
			updateExperimentStatus(nodeStatus.getWorkflowNodeIdentity().getExperimentId(), state);
			logger.debug("Publishing experiment status for "+nodeStatus.getWorkflowNodeIdentity().getExperimentId()+":"+state.toString());
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(state,
                                                                                nodeStatus.getWorkflowNodeIdentity().getExperimentId(),
                                                                                nodeStatus.getWorkflowNodeIdentity().getGatewayId());
            monitorPublisher.publish(event);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext msgCntxt = new MessageContext(event, MessageType.EXPERIMENT, messageId, nodeStatus.getWorkflowNodeIdentity().getGatewayId());
            msgCntxt.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            if ( ServerSettings.isRabbitMqPublishEnabled()){
                publisher.publish(msgCntxt);
            }
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
            throw new Exception("Error persisting experiment status..", e);
		}
    }
    
    public  ExperimentState updateExperimentStatus(String experimentId, ExperimentState state) throws Exception {
    	Experiment details = (Experiment)airavataRegistry.get(RegistryModelType.EXPERIMENT, experimentId);
        if(details == null) {
            details = new Experiment();
            details.setExperimentID(experimentId);
        }
        org.apache.airavata.model.workspace.experiment.ExperimentStatus status = new org.apache.airavata.model.workspace.experiment.ExperimentStatus();
        status.setExperimentState(state);
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
			} else if (configuration instanceof Publisher){
                this.publisher=(Publisher) configuration;
            }
        }
	}
}
