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
package org.apache.airavata.db.event.manager.messaging.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.db.event.manager.messaging.DBEventManagerException;
import org.apache.airavata.db.event.manager.messaging.DBEventManagerMessagingFactory;
import org.apache.airavata.db.event.manager.utils.DbEventManagerZkUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.dbevent.DBEventType;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Ajinkya on 3/14/17.
 */
public class DBEventMessageHandler implements MessageHandler {

    private final static Logger log = LoggerFactory.getLogger(DBEventMessageHandler.class);
    private CuratorFramework curatorClient;

    public DBEventMessageHandler() throws ApplicationSettingsException {
        startCuratorClient();
    }

    private void startCuratorClient() throws ApplicationSettingsException {
        curatorClient = DbEventManagerZkUtils.getCuratorClient();
        curatorClient.start();
    }

    @Override
    public void onMessage(MessageContext messageContext) {

        log.info("Incoming DB event message. Message Id : " + messageContext.getMessageId());
        try {

            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());

            DBEventMessage dbEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(bytes, dbEventMessage);

            DBEventMessageContext dBEventMessageContext = dbEventMessage.getMessageContext();

            switch (dbEventMessage.getDbEventType()){

                case SUBSCRIBER:
                    log.info("Registering " + dBEventMessageContext.getSubscriber().getSubscriberService() + " subscriber for " + dbEventMessage.getPublisherService());
                    DbEventManagerZkUtils.createDBEventMgrZkNode(curatorClient, dbEventMessage.getPublisherService(), dBEventMessageContext.getSubscriber().getSubscriberService());
                    break;
                case PUBLISHER:
                    List<String> subscribers = DbEventManagerZkUtils.getSubscribersForPublisher(curatorClient, dbEventMessage.getPublisherService());
                    if(subscribers.isEmpty()){
                        log.error("No Subscribers registered for the service");
                        throw new DBEventManagerException("No Subscribers registered for the service");
                    }
                    String routingKey = getRoutingKeyFromList(subscribers);
                    log.info("Publishing " + dbEventMessage.getPublisherService() + " db event to " + subscribers.toString());
                    MessageContext messageCtx = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");
                    messageCtx.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                    DBEventManagerMessagingFactory.getDBEventPublisher().publish(messageCtx, routingKey);
                    break;
            }

            log.info("Sending ack. Message Delivery Tag : " + messageContext.getDeliveryTag());
            DBEventManagerMessagingFactory.getDBEventSubscriber().sendAck(messageContext.getDeliveryTag());

        } catch (Exception e) {
            log.error("Error processing message.", e);
        }
    }

    private String getRoutingKeyFromList(final List<String> subscribers){
        StringBuilder sb = new StringBuilder();
        Collections.sort(subscribers);
        for(String subscriber : subscribers){
            sb.append(subscriber).append(DBEventManagerConstants.ROUTING_KEY_SEPARATOR);
        }
        return sb.substring(0, sb.length() - 1);
    }
}
