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
package org.apache.airavata.orchestration.util;

import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.model.messaging.event.proto.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.task.SchedulerUtils;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrchestratorServerUtils {
    private static final Logger log = LoggerFactory.getLogger(OrchestratorUtils.class);

    public static void updateAndPublishExperimentStatus(
            String experimentId, ExperimentStatus status, Publisher publisher, String gatewayId) throws Exception {
        try {
            RegistryHandler registryHandler = SchedulerUtils.getRegistryHandler();
            registryHandler.updateExperimentStatus(status, experimentId);
            ExperimentStatusChangeEvent event = ExperimentStatusChangeEvent.newBuilder()
                    .setState(status.getState())
                    .setExperimentId(experimentId)
                    .setGatewayId(gatewayId)
                    .build();
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            publisher.publish(messageContext);
        } catch (AiravataException e) {
            log.error(
                    "expId : " + experimentId + " Error while publishing experiment status to " + status.toString(), e);
        }
    }

    public static ExperimentStatus getExperimentStatus(String experimentId) throws Exception {
        return SchedulerUtils.getRegistryHandler().getExperimentStatus(experimentId);
    }

    public static ProcessModel getProcess(String processId) throws Exception {
        return SchedulerUtils.getRegistryHandler().getProcess(processId);
    }
}
