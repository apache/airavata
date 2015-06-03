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
package org.apache.airavata.gfac.monitor.impl.push.amqp;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.core.MessageParser;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class BasicConsumer implements Consumer {
    private final static Logger logger = LoggerFactory.getLogger(AMQPMonitor.class);

    private MessageParser parser;

    private MonitorPublisher publisher;

    public BasicConsumer(MessageParser parser, MonitorPublisher publisher) {
        this.parser = parser;
        this.publisher = publisher;
    }

    public void handleCancel(String consumerTag) {
    }

    public void handleCancelOk(String consumerTag) {
    }

    public void handleConsumeOk(String consumerTag) {
    }

    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) {

        logger.debug("job update for: " + envelope.getRoutingKey());
        String message = new String(body);
        message = message.replaceAll("(?m)^", "    ");
        // Here we parse the message and get the job status and push it
        // to the Event bus, this will be picked by
//        AiravataJobStatusUpdator and store in to registry

        logger.debug("************************************************************");
        logger.debug("AMQP Message recieved \n" + message);
        logger.debug("************************************************************");
        try {
            String jobID = envelope.getRoutingKey().split("\\.")[0];
            MonitorID monitorID = new MonitorID(null, jobID, null, null, null, null,null);
            monitorID.setStatus(parser.parseMessage(message));
            publisher.publish(monitorID);
        } catch (AiravataMonitorException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void handleRecoverOk(String consumerTag) {
    }

    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    }

}
