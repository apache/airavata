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
package org.apache.airavata.messaging.core;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.impl.ProcessConsumer;
import org.apache.airavata.messaging.core.impl.RabbitMQSubscriber;
import org.apache.airavata.messaging.core.impl.StatusConsumer;

import java.util.ArrayList;
import java.util.List;

public class MessagingFactory {

    public static Subscriber getSubscriber(final MessageHandler messageHandler,List<String> routingKeys, Subscriber.Type type) throws AiravataException {
        Subscriber subscriber = null;
        SubscriberProperties sp = getSubscriberProperties();

        switch (type) {
            case EXPERIMENT_LAUNCH:
                break;
            case PROCESS_LAUNCH:
                subscriber = getProcessSubscriber(sp);
                subscriber.listen((connection ,channel) -> new ProcessConsumer(messageHandler, connection, channel),
                        null,
                        routingKeys);
                break;
            case STATUS:
                subscriber = getStatusSubscriber(sp);
                subscriber.listen((connection, channel) -> new StatusConsumer(messageHandler, connection, channel),
                        null,
                        routingKeys);
                break;
            default:
                break;
        }

        return subscriber;
    }

    private static SubscriberProperties getSubscriberProperties() {
        return new SubscriberProperties()
                .setBrokerUrl(ServerSettings.RABBITMQ_BROKER_URL)
                .setDurable(ServerSettings.getRabbitmqDurableQueue())
                .setPrefetchCount(ServerSettings.getRabbitmqPrefetchCount())
                .setAutoRecoveryEnable(true)
                .setConsumerTag("default")
                .setExchangeType(SubscriberProperties.EXCHANGE_TYPE.TOPIC);
    }

    private static RabbitMQSubscriber getStatusSubscriber(SubscriberProperties sp) throws AiravataException {
        sp.setExchangeName(ServerSettings.getRabbitmqStatusExchangeName())
                .setAutoAck(true);
        return new RabbitMQSubscriber(sp);
    }


    private static RabbitMQSubscriber getProcessSubscriber(SubscriberProperties sp) throws AiravataException {
        sp.setExchangeName(ServerSettings.getRabbitmqProcessExchangeName())
                .setQueueName(ServerSettings.getRabbitmqProcessLaunchQueueName())
                .setAutoAck(false);
        return new RabbitMQSubscriber(sp);
    }





}
