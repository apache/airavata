/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.orchestration.infrastructure;

import java.util.UUID;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Type;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.messaging.event.proto.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.proto.ProcessTerminateEvent;
import org.apache.airavata.orchestration.service.OrchestratorContext;
import org.apache.airavata.orchestration.service.OrchestratorException;
import org.apache.airavata.util.AiravataUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to do the communication between orchestrator and gfac to handle using a queue
 */
public class GFACPassiveJobSubmitter implements JobSubmitter, Watcher {
    private static final Logger logger = LoggerFactory.getLogger(GFACPassiveJobSubmitter.class);
    private static final Object mutex = new Object();
    private Publisher publisher;

    public void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException {
        if (orchestratorContext.getPublisher() != null) {
            this.publisher = orchestratorContext.getPublisher();
        } else {
            try {
                this.publisher = MessagingFactory.getPublisher(Type.PROCESS_LAUNCH);
            } catch (AiravataException e) {
                logger.error(e.getMessage(), e);
                throw new OrchestratorException("Cannot initialize " + GFACPassiveJobSubmitter.class
                        + " need to start Rabbitmq server to use " + GFACPassiveJobSubmitter.class);
            }
        }
    }

    /**
     * Submit the job to a shared launch.queue accross multiple gfac instances
     *
     * @param experimentId
     * @param processId
     * @param tokenId
     * @return
     * @throws OrchestratorException
     */
    public boolean submit(String experimentId, String processId, String tokenId) throws OrchestratorException {
        try {
            String gatewayId = resolveGatewayId();
            ProcessSubmitEvent processSubmitEvent = ProcessSubmitEvent.newBuilder()
                    .setProcessId(processId)
                    .setGatewayId(gatewayId)
                    .setExperimentId(experimentId)
                    .setTokenId(tokenId)
                    .build();
            MessageContext messageContext = new MessageContext(
                    processSubmitEvent,
                    MessageType.LAUNCHPROCESS,
                    "LAUNCH" + ".PROCESS-" + UUID.randomUUID().toString(),
                    gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new OrchestratorException(e);
        }
        return true;
    }

    /**
     * Submit the experiment the terminate.queue job queue and remove the experiment from shared launch.queue
     * @param experimentId
     * @param processId
     * @return
     * @throws OrchestratorException
     */
    public boolean terminate(String experimentId, String processId, String tokenId) throws OrchestratorException {
        try {
            String gatewayId = resolveGatewayId();
            ProcessTerminateEvent processTerminateEvent = ProcessTerminateEvent.newBuilder()
                    .setProcessId(processId)
                    .setGatewayId(gatewayId)
                    .setTokenId(tokenId)
                    .build();
            MessageContext messageContext = new MessageContext(
                    processTerminateEvent,
                    MessageType.TERMINATEPROCESS,
                    "LAUNCH.TERMINATE-" + UUID.randomUUID().toString(),
                    gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
            return true;
        } catch (Exception e) {
            throw new OrchestratorException(e);
        }
    }

    private static String resolveGatewayId() {
        try {
            return ServerSettings.getDefaultUserGateway();
        } catch (Exception e) {
            throw new RuntimeException("Unable to resolve gateway id", e);
        }
    }

    public synchronized void process(WatchedEvent event) {
        logger.info(getClass().getName() + event.getPath());
        logger.info(getClass().getName() + event.getType());
        synchronized (mutex) {
            switch (event.getState()) {
                case SyncConnected:
                    mutex.notify();
                    break;
                default:
                    break;
            }
            switch (event.getType()) {
                case NodeCreated:
                    mutex.notify();
                    break;
                default:
                    break;
            }
        }
    }
}
