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
package org.apache.airavata.sharing.event;

import java.util.List;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.messaging.service.Subscriber;
import org.apache.airavata.messaging.util.DBEventManagerConstants;
import org.apache.airavata.messaging.util.DBEventService;
import org.apache.airavata.model.dbevent.proto.DBEventMessage;
import org.apache.airavata.model.dbevent.proto.DBEventMessageContext;
import org.apache.airavata.model.dbevent.proto.DBEventSubscriber;
import org.apache.airavata.model.dbevent.proto.DBEventType;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventMessagingFactory {

    private static final Logger log = LoggerFactory.getLogger(SharingServiceDBEventMessagingFactory.class);

    private static Publisher dbEventPublisher;

    private static Subscriber sharingServiceDBEventSubscriber;

    /**
     * Get publisher for DB Event queue
     * Change access specifier as required
     * @return
     * @throws AiravataException
     */
    private static Publisher getDBEventPublisher() throws AiravataException {
        if (null == dbEventPublisher) {
            synchronized (SharingServiceDBEventMessagingFactory.class) {
                if (null == dbEventPublisher) {
                    log.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    log.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    public static Subscriber getDBEventSubscriber() throws AiravataException {
        if (null == sharingServiceDBEventSubscriber) {
            synchronized (SharingServiceDBEventMessagingFactory.class) {
                if (null == sharingServiceDBEventSubscriber) {
                    log.info("Creating DB Event publisher.....");
                    try {
                        sharingServiceDBEventSubscriber = MessagingFactory.getDBEventSubscriber(
                                new SharingServiceDBEventHandler(), DBEventService.SHARING.toString());
                    } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
                        throw new AiravataException("Failed to create SharingServiceDBEventHandler", e);
                    }
                    log.info("DB Event publisher created");
                }
            }
        }
        return sharingServiceDBEventSubscriber;
    }

    /**
     * Register sharing service with stated publishers
     * @param publishers
     * @return
     * @throws AiravataException
     */
    public static boolean registerSharingServiceWithPublishers(List<String> publishers) throws AiravataException {

        for (String publisher : publishers) {

            log.info("Sending service discovery message. Publisher : " + publisher + ", Subscriber : "
                    + DBEventService.SHARING.toString());

            DBEventSubscriber dbEventSubscriber = DBEventSubscriber.newBuilder()
                    .setSubscriberService(DBEventService.SHARING.toString())
                    .build();
            DBEventMessageContext dbEventMessageContext = DBEventMessageContext.newBuilder()
                    .setSubscriber(dbEventSubscriber)
                    .build();

            DBEventMessage dbEventMessage = DBEventMessage.newBuilder()
                    .setDbEventType(DBEventType.SUBSCRIBER)
                    .setMessageContext(dbEventMessageContext)
                    .setPublisherService(publisher)
                    .build();

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            getDBEventPublisher()
                    .publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));
        }

        return true;
    }
}
