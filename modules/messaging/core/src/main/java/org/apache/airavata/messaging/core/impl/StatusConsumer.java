/**
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
 */
package org.apache.airavata.messaging.core.impl;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.JobStatusChangeEvent;
import org.apache.airavata.model.messaging.event.Message;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.messaging.event.ProcessStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.model.messaging.event.TaskStatusChangeEvent;
import org.apache.airavata.model.messaging.event.TaskSubmitEvent;
import org.apache.airavata.model.messaging.event.TaskTerminateEvent;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StatusConsumer extends DefaultConsumer {
    private static final Logger log = LoggerFactory.getLogger(StatusConsumer.class);

    private MessageHandler handler;
    private Connection connection;
    private Channel channel;

    public StatusConsumer(MessageHandler handler, Connection connection, Channel channel) {
        super(channel);
        this.handler = handler;
        this.connection = connection;
        this.channel = channel;
    }

    private StatusConsumer(Channel channel) {
        super(channel);
    }


    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        Message message = new Message();

        try {
            ThriftUtils.createThriftFromBytes(body, message);
            TBase event = null;
            String gatewayId = null;

            if (message.getMessageType().equals(MessageType.EXPERIMENT)) {
                ExperimentStatusChangeEvent experimentStatusChangeEvent = new ExperimentStatusChangeEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), experimentStatusChangeEvent);
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status " +
                        experimentStatusChangeEvent.getState());
                event = experimentStatusChangeEvent;
                gatewayId = experimentStatusChangeEvent.getGatewayId();
            } else if (message.getMessageType().equals(MessageType.PROCESS)) {
                ProcessStatusChangeEvent processStatusChangeEvent = new ProcessStatusChangeEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), processStatusChangeEvent);
                log.debug("Message Recieved with message id :" + message.getMessageId() + " and with " +
                        "message type " + message.getMessageType() + " with status " +
                        processStatusChangeEvent.getState());
                event = processStatusChangeEvent;
                gatewayId = processStatusChangeEvent.getProcessIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.TASK)) {
                TaskStatusChangeEvent taskStatusChangeEvent = new TaskStatusChangeEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), taskStatusChangeEvent);
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status " +
                        taskStatusChangeEvent.getState());
                event = taskStatusChangeEvent;
                gatewayId = taskStatusChangeEvent.getTaskIdentity().getGatewayId();
            } else if (message.getMessageType() == MessageType.PROCESSOUTPUT) {
                TaskOutputChangeEvent taskOutputChangeEvent = new TaskOutputChangeEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), taskOutputChangeEvent);
                log.debug(" Message Received with message id '" + message.getMessageId() + "' and with message type '" + message.getMessageType());
                event = taskOutputChangeEvent;
                gatewayId = taskOutputChangeEvent.getTaskIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.JOB)) {
                JobStatusChangeEvent jobStatusChangeEvent = new JobStatusChangeEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), jobStatusChangeEvent);
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  with status " +
                        jobStatusChangeEvent.getState());
                event = jobStatusChangeEvent;
                gatewayId = jobStatusChangeEvent.getJobIdentity().getGatewayId();
            } else if (message.getMessageType().equals(MessageType.LAUNCHPROCESS)) {
                TaskSubmitEvent taskSubmitEvent = new TaskSubmitEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), taskSubmitEvent);
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId: " +
                        taskSubmitEvent.getExperimentId() + "and taskId: " + taskSubmitEvent.getTaskId());
                event = taskSubmitEvent;
                gatewayId = taskSubmitEvent.getGatewayId();
            } else if (message.getMessageType().equals(MessageType.TERMINATEPROCESS)) {
                TaskTerminateEvent taskTerminateEvent = new TaskTerminateEvent();
                ThriftUtils.createThriftFromBytes(message.getEvent(), taskTerminateEvent);
                log.debug(" Message Received with message id '" + message.getMessageId()
                        + "' and with message type '" + message.getMessageType() + "'  for experimentId: " +
                        taskTerminateEvent.getExperimentId() + "and taskId: " + taskTerminateEvent.getTaskId());
                event = taskTerminateEvent;
                gatewayId = null;
            }
            MessageContext messageContext = new MessageContext(event, message.getMessageType(), message.getMessageId(), gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getTime(message.getUpdatedTime()));
            messageContext.setIsRedeliver(envelope.isRedeliver());
            handler.onMessage(messageContext);
        } catch (TException e) {
            String msg = "Failed to de-serialize the thrift message, from routing keys: " + envelope.getRoutingKey();
            log.warn(msg, e);
        }
    }
}
