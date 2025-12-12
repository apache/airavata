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
package org.apache.airavata.registry.messaging;

import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.dbevent.DBEventSubscriber;
import org.apache.airavata.model.dbevent.DBEventType;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Created by goshenoy on 3/30/17.
 */
@Component
@ConditionalOnProperty(name = "services.registryService.enabled", havingValue = "true", matchIfMissing = true)
public class RegistryServiceDBEventMessagingFactory {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceDBEventMessagingFactory.class);

    private static Publisher dbEventPublisher;

    private static Subscriber registryServiceDBEventSubscriber;
    private static RegistryServiceDBEventMessagingFactory instance;

    private final RegistryService registryService;

    public RegistryServiceDBEventMessagingFactory(RegistryService registryService) {
        this.registryService = registryService;
        instance = this;
    }

    private static Publisher getDBEventPublisher() throws AiravataException {
        if (null == dbEventPublisher) {
            synchronized (RegistryServiceDBEventMessagingFactory.class) {
                if (null == dbEventPublisher) {
                    logger.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    logger.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    public static Subscriber getDBEventSubscriber() throws AiravataException, RegistryServiceException {
        if (null == registryServiceDBEventSubscriber) {
            synchronized (RegistryServiceDBEventMessagingFactory.class) {
                if (null == registryServiceDBEventSubscriber) {
                    logger.info("Creating DB Event publisher.....");
                    RegistryServiceDBEventHandler handler;
                    String serviceName = DBEventService.REGISTRY.toString();
                    try {
                        RegistryService service = instance != null ? instance.registryService : null;
                        if (service == null) {
                            throw new IllegalStateException(
                                    "RegistryServiceDBEventMessagingFactory not initialized via Spring");
                        }
                        handler = new RegistryServiceDBEventHandler(service);
                    } catch (Exception e) {
                        throw new AiravataException(
                                "Failed to create registry service DB event handler for service: " + serviceName, e);
                    }
                    registryServiceDBEventSubscriber = MessagingFactory.getDBEventSubscriber(handler, serviceName);
                    logger.info("DB Event subscriber created for service: " + serviceName);
                }
            }
        }
        return registryServiceDBEventSubscriber;
    }

    public static boolean registerRegistryServiceWithPublishers(List<String> publisherList) throws AiravataException {
        for (String publisher : publisherList) {
            logger.info("Sending service discovery message. Publisher: " + publisher + ", Subscriber: "
                    + DBEventService.REGISTRY.toString());

            DBEventSubscriber dbEventSubscriber = new DBEventSubscriber(DBEventService.REGISTRY.toString());
            DBEventMessageContext dbEventMessageContext = new DBEventMessageContext();
            dbEventMessageContext.setSubscriber(dbEventSubscriber);

            DBEventMessage dbEventMessage =
                    new DBEventMessage(DBEventType.SUBSCRIBER, dbEventMessageContext, publisher);

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            getDBEventPublisher()
                    .publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));
        }
        return true;
    }
}
