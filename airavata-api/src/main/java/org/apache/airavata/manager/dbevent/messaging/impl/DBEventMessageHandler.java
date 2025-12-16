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
package org.apache.airavata.manager.dbevent.messaging.impl;

import java.util.Collections;
import java.util.List;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventMessageContext;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.manager.dbevent.messaging.DBEventManagerException;
import org.apache.airavata.manager.dbevent.messaging.DBEventManagerMessagingFactory;
import org.apache.airavata.manager.dbevent.utils.DbEventManagerZkUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/14/17.
 */
public class DBEventMessageHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(DBEventMessageHandler.class);
    private CuratorFramework curatorClient;
    private AiravataServerProperties properties;
    private DBEventManagerMessagingFactory messagingFactory;

    public DBEventMessageHandler() {
        // Properties should be set via setter
    }

    public DBEventMessageHandler(AiravataServerProperties properties) {
        this.properties = properties;
        startCuratorClient();
    }

    public DBEventMessageHandler(AiravataServerProperties properties, DBEventManagerMessagingFactory messagingFactory) {
        this.properties = properties;
        this.messagingFactory = messagingFactory;
        startCuratorClient();
    }

    public void setProperties(AiravataServerProperties properties) {
        this.properties = properties;
        if (curatorClient == null) {
            startCuratorClient();
        }
    }

    private void startCuratorClient() {
        if (properties == null) {
            throw new IllegalStateException("Properties must be set before starting curator client");
        }
        curatorClient = DbEventManagerZkUtils.getCuratorClient(properties);
        curatorClient.start();
    }

    @Override
    public void onMessage(MessageContext messageContext) {

        log.info("Incoming DB event message. Message Id : " + messageContext.getMessageId());
        try {

            var event = messageContext.getEvent();
            var type = messageContext.getType();
            var messageId = messageContext.getMessageId();
            var gatewayId = messageContext.getGatewayId();

            // TODO: the code won't work until dbevent and message models are properly mapped or merged
            var dbEventMessage = new DBEventMessage(messageContext);

            DBEventMessageContext dBEventMessageContext = dbEventMessage.getMessageContext();

            switch (dbEventMessage.getDbEventType()) {
                case SUBSCRIBER:
                    log.info("Registering "
                            + dBEventMessageContext.getSubscriber().getSubscriberService() + " subscriber for "
                            + dbEventMessage.getPublisherService());
                    DbEventManagerZkUtils.createDBEventMgrZkNode(
                            curatorClient,
                            dbEventMessage.getPublisherService(),
                            dBEventMessageContext.getSubscriber().getSubscriberService());
                    break;
                case PUBLISHER:
                    List<String> subscribers = DbEventManagerZkUtils.getSubscribersForPublisher(
                            curatorClient, dbEventMessage.getPublisherService());
                    if (subscribers.isEmpty()) {
                        log.error("No Subscribers registered for the service");
                        throw new DBEventManagerException("No Subscribers registered for the service");
                    }
                    String routingKey = getRoutingKeyFromList(subscribers);
                    log.info("Publishing " + dbEventMessage.getPublisherService() + " db event to "
                            + subscribers.toString());
                    MessageContext messageCtx = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");
                    messageCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                    if (messagingFactory == null) {
                        throw new IllegalStateException(
                                "DBEventManagerMessagingFactory must be set on DBEventMessageHandler");
                    }
                    messagingFactory.getDBEventPublisher().publish(messageCtx, routingKey);
                    break;
            }

            log.info("Sending ack. Message Delivery Tag : " + messageContext.getDeliveryTag());
            if (properties != null && messagingFactory != null) {
                messagingFactory.getDBEventSubscriber(properties).sendAck(messageContext.getDeliveryTag());
            }

        } catch (Exception e) {
            log.error("Error processing message.", e);
        }
    }

    private String getRoutingKeyFromList(final List<String> subscribers) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(subscribers);
        for (String subscriber : subscribers) {
            sb.append(subscriber).append(DBEventManagerConstants.ROUTING_KEY_SEPARATOR);
        }
        return sb.substring(0, sb.length() - 1);
    }
}
