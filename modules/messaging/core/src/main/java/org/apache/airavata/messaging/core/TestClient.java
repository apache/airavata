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
package org.apache.airavata.messaging.core;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class TestClient {
    public static final String RABBITMQ_BROKER_URL = "rabbitmq.broker.url";
    public static final String RABBITMQ_EXCHANGE_NAME = "rabbitmq.exchange.name";
    private final static Logger logger = LoggerFactory.getLogger(TestClient.class);
    private final static String experimentId = "*";

    public static void main(String[] args) {
        try {
            List<String> routingKeys = new ArrayList<>();
            routingKeys.add(experimentId);
            routingKeys.add(experimentId + ".*");
            MessagingFactory.getSubscriber(getMessageHandler(),routingKeys,  Type.STATUS);
        } catch (ApplicationSettingsException e) {
            logger.error("Error reading airavata server properties", e);
        }catch (Exception e) {
           logger.error(e.getMessage(), e);
        }

    }


    private static MessageHandler getMessageHandler(){
        return message -> {
                if (message.getType().equals(MessageType.EXPERIMENT)){
                    try {
                        ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent();
                        TBase messageEvent = message.getEvent();
                        byte[] bytes = ThriftUtils.serializeThriftObject(messageEvent);
                        ThriftUtils.createThriftFromBytes(bytes, event);
                        System.out.println(" Message Received with message id '" + message.getMessageId()
                                + "' and with message type '" + message.getType() + "' and with state : '" + event.getState().toString());
                    } catch (TException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            };
    }
}
