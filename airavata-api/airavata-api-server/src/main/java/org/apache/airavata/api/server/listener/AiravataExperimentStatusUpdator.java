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
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.*;
import org.apache.airavata.common.utils.listener.AbstractActivityListener;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.impl.RabbitMQTaskLaunchConsumer;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.WorkflowNodeStatusChangeEvent;
import org.apache.airavata.model.util.ExecutionType;
import org.apache.airavata.model.workspace.experiment.Experiment;
import org.apache.airavata.model.workspace.experiment.ExperimentState;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Calendar;

public class AiravataExperimentStatusUpdator implements AbstractActivityListener {
    private final static Logger logger = LoggerFactory.getLogger(AiravataExperimentStatusUpdator.class);

    private Registry airavataRegistry;

    private MonitorPublisher monitorPublisher;

    private Publisher publisher;

    private ZooKeeper zk;

    private RabbitMQTaskLaunchConsumer consumer;


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
			ExecutionType executionType = DataModelUtils.getExecutionType((Experiment) airavataRegistry.get(RegistryModelType.EXPERIMENT, nodeStatus.getWorkflowNodeIdentity().getExperimentId()));
            String experimentNode = ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
            String experimentPath = experimentNode + File.separator + ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME)
                    + File.separator + nodeStatus.getWorkflowNodeIdentity().getExperimentId();
	        ExperimentState state;
	        switch (nodeStatus.getState()) {
	            case CANCELED:
	                state = ExperimentState.CANCELED; updateExperimentStatus = true;
                    cleanup(nodeStatus, experimentNode, experimentPath);
	                break;
	            case COMPLETED:
	            	if(executionType.equals(ExecutionType.SINGLE_APP)){
	            		state = ExperimentState.COMPLETED; updateExperimentStatus = true;
	            	}else{
	                state = ExperimentState.EXECUTING; updateExperimentStatus = true;
	                }
                    cleanup(nodeStatus, experimentNode, experimentPath);
	                break;
	            case INVOKED:
	                state = ExperimentState.LAUNCHED; updateExperimentStatus = false;
	                break;
	            case FAILED:
	                state = ExperimentState.FAILED; updateExperimentStatus = true;
                    cleanup(nodeStatus,experimentNode,experimentPath);
	                break;
	            case EXECUTING:
	                state = ExperimentState.EXECUTING; updateExperimentStatus = true;
	                break;
	            case CANCELING:
	                state = ExperimentState.CANCELING; updateExperimentStatus = true;
                    cleanup(nodeStatus,experimentNode,experimentPath);
                    break;
	            default:
	                return;
	        }
	        if (!updateExperimentStatus){
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
            publisher.publish(msgCntxt);
		} catch (Exception e) {
            logger.error("Error persisting data" + e.getLocalizedMessage(), e);
            throw new Exception("Error persisting experiment status..", e);
		}
    }

    private void cleanup(WorkflowNodeStatusChangeEvent nodeStatus, String experimentNode, String experimentPath) throws KeeperException, InterruptedException, AiravataException {
        int count = 0;
        long deliveryTag = AiravataZKUtils.getDeliveryTag(nodeStatus.getWorkflowNodeIdentity().getExperimentId(), zk, experimentNode, ServerSettings.getSetting(Constants.ZOOKEEPER_GFAC_SERVER_NAME));
        if(deliveryTag>0) {
            if (ServerSettings.isGFacPassiveMode()) {
                while (!consumer.isOpen() && count < 3) {
                    try {
                        consumer.reconnect();
                    } catch (AiravataException e) {
                        count++;
                    }
                }
                try {
                    if (consumer.isOpen()) {
                        consumer.sendAck(deliveryTag);
                    }
                } catch (Exception e) {
                    logger.error("Error sending the Ack ! If the worker pick this again airavata should gracefully handle !");
                }
            }
        }
        if (zk.exists(experimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX, false) != null) {
            ZKUtil.deleteRecursive(zk, experimentPath + AiravataZKUtils.DELIVERY_TAG_POSTFIX);
        }
        if (zk.exists(experimentPath, false) != null) {
            ZKUtil.deleteRecursive(zk, experimentPath);
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
            }else if (configuration instanceof RabbitMQTaskLaunchConsumer) {
                this.consumer = (RabbitMQTaskLaunchConsumer) configuration;
            }else if (configuration instanceof ZooKeeper) {
                this.zk = (ZooKeeper) configuration;
            }

        }
	}
}
