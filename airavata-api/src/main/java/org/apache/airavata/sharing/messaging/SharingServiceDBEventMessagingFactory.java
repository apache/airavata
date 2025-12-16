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
package org.apache.airavata.sharing.messaging;

import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventMessageContext;
import org.apache.airavata.common.model.DBEventSubscriber;
import org.apache.airavata.common.model.DBEventType;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Ajinkya on 3/28/17.
 */
@Component
public class SharingServiceDBEventMessagingFactory {

    private static final Logger log = LoggerFactory.getLogger(SharingServiceDBEventMessagingFactory.class);

    private Publisher dbEventPublisher;

    private Subscriber sharingServiceDBEventSubscriber;

    private final SharingRegistryService sharingRegistryService;
    private final MessagingFactory messagingFactory;

    public SharingServiceDBEventMessagingFactory(
            SharingRegistryService sharingRegistryService, MessagingFactory messagingFactory) {
        this.sharingRegistryService = sharingRegistryService;
        this.messagingFactory = messagingFactory;
    }

    /**
     * Get publisher for DB Event queue
     * Change access specifier as required
     * @return
     * @throws AiravataException
     */
    public Publisher getDBEventPublisher() throws AiravataException {
        if (null == dbEventPublisher) {
            synchronized (this) {
                if (null == dbEventPublisher) {
                    log.info("Creating DB Event publisher.....");
                    dbEventPublisher = messagingFactory.getDBEventPublisher();
                    log.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    public Subscriber getDBEventSubscriber() throws AiravataException, SharingRegistryException {
        if (null == sharingServiceDBEventSubscriber) {
            synchronized (this) {
                if (null == sharingServiceDBEventSubscriber) {
                    SharingServiceDBEventHandler handler;
                    String serviceName = DBEventService.SHARING.toString();
                    log.info("Creating DB Event subscriber for service: " + serviceName);
                    try {
                        handler = new SharingServiceDBEventHandler(sharingRegistryService, this);
                    } catch (Exception e) {
                        throw new AiravataException(
                                "Failed to create sharing service DB event handler for service: " + serviceName, e);
                    }
                    sharingServiceDBEventSubscriber = messagingFactory.getDBEventSubscriber(handler, serviceName);
                    log.info("DB Event subscriber created for service: " + serviceName);
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
    public boolean registerSharingServiceWithPublishers(List<String> publishers) throws AiravataException {

        for (String publisher : publishers) {

            log.info("Sending service discovery message. Publisher : " + publisher + ", Subscriber : "
                    + DBEventService.SHARING.toString());

            DBEventSubscriber dbEventSubscriber = new DBEventSubscriber();
            dbEventSubscriber.setSubscriberService(DBEventService.SHARING.toString());
            DBEventMessageContext dbEventMessageContext = new DBEventMessageContext();
            dbEventMessageContext.setSubscriber(dbEventSubscriber);

            DBEventMessage dbEventMessage = new DBEventMessage();
            dbEventMessage.setDbEventType(DBEventType.SUBSCRIBER);
            dbEventMessage.setMessageContext(dbEventMessageContext);
            dbEventMessage.setPublisherService(publisher);

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            getDBEventPublisher()
                    .publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));
        }

        return true;
    }
}
