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
package org.apache.airavata.orchestration.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessageHandler;
import org.apache.airavata.model.messaging.event.proto.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.Message;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.apache.airavata.model.messaging.event.proto.ProcessStatusChangeEvent;
import org.apache.airavata.model.messaging.event.proto.ProcessSubmitEvent;
import org.apache.airavata.model.messaging.event.proto.ProcessTerminateEvent;
import org.apache.airavata.model.messaging.event.proto.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.proto.TaskStatusChangeEvent;
import org.apache.airavata.util.AiravataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageListener;

public class StatusConsumer implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(StatusConsumer.class);

    private final MessageHandler handler;

    public StatusConsumer(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(org.springframework.amqp.core.Message amqpMessage) {
        try {
            byte[] body = amqpMessage.getBody();
            String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
            boolean isRedeliver =
                    Boolean.TRUE.equals(amqpMessage.getMessageProperties().getRedelivered());

            Message message = Message.parseFrom(body);
            Object event = null;
            String gatewayId = null;

            if (message.getMessageType().equals(MessageType.EXPERIMENT)) {
                ExperimentStatusChangeEvent experimentStatusChangeEvent =
                        ExperimentStatusChangeEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status "
                        + experimentStatusChangeEvent.getState());
                event = experimentStatusChangeEvent;
                gatewayId = experimentStatusChangeEvent.getGatewayId();
            } else if (message.getMessageType().equals(MessageType.PROCESS)) {
                ProcessStatusChangeEvent processStatusChangeEvent =
                        ProcessStatusChangeEvent.parseFrom(message.getEvent());
                log.debug("Message Recieved with message id :" + message.getMessageId() + " and with " + "message type "
                        + message.getMessageType() + " with status " + processStatusChangeEvent.getState());
                event = processStatusChangeEvent;
                gatewayId = processStatusChangeEvent.getProcessIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.TASK)) {
                TaskStatusChangeEvent taskStatusChangeEvent = TaskStatusChangeEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status "
                        + taskStatusChangeEvent.getState());
                event = taskStatusChangeEvent;
                gatewayId = taskStatusChangeEvent.getTaskIdentity().getGatewayId();
            } else if (message.getMessageType() == MessageType.PROCESSOUTPUT) {
                TaskOutputChangeEvent taskOutputChangeEvent = TaskOutputChangeEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '"
                        + message.getMessageType());
                event = taskOutputChangeEvent;
                gatewayId = taskOutputChangeEvent.getTaskIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.JOB)) {
                JobStatusChangeEvent jobStatusChangeEvent = JobStatusChangeEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status "
                        + jobStatusChangeEvent.getState());
                event = jobStatusChangeEvent;
                gatewayId = jobStatusChangeEvent.getJobIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.LAUNCHPROCESS)) {
                ProcessSubmitEvent processSubmitEvent = ProcessSubmitEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId: "
                        + processSubmitEvent.getExperimentId()
                        + "and processId: " + processSubmitEvent.getProcessId());
                event = processSubmitEvent;
                gatewayId = processSubmitEvent.getGatewayId();
            } else if (message.getMessageType().equals(MessageType.TERMINATEPROCESS)) {
                ProcessTerminateEvent processTerminateEvent = ProcessTerminateEvent.parseFrom(message.getEvent());
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for processId: "
                        + processTerminateEvent.getProcessId());
                event = processTerminateEvent;
                gatewayId = null;
            }
            MessageContext messageContext =
                    new MessageContext(event, message.getMessageType(), message.getMessageId(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
            messageContext.setIsRedeliver(isRedeliver);
            handler.onMessage(messageContext);
        } catch (InvalidProtocolBufferException e) {
            String msg = "Failed to de-serialize the proto message";
            log.warn(msg, e);
        }
    }
}
