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
package org.apache.airavata.sharing.registry.messaging;

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
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventMessagingFactory {

    private final static Logger log = LoggerFactory.getLogger(SharingServiceDBEventMessagingFactory.class);

    private static Publisher dbEventPublisher;

    private static Subscriber sharingServiceDBEventSubscriber;

    /**
     * Get publisher for DB Event queue
     * Change access specifier as required
     * @return
     * @throws AiravataException
     */
    private static Publisher getDBEventPublisher() throws AiravataException {
        if(null == dbEventPublisher){
            synchronized (SharingServiceDBEventMessagingFactory.class){
                if(null == dbEventPublisher){
                    log.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    log.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    public static Subscriber getDBEventSubscriber() throws AiravataException, SharingRegistryException {
        if(null == sharingServiceDBEventSubscriber){
            synchronized (SharingServiceDBEventMessagingFactory.class){
                if(null == sharingServiceDBEventSubscriber){
                    log.info("Creating DB Event publisher.....");
                    sharingServiceDBEventSubscriber = MessagingFactory.getDBEventSubscriber(new SharingServiceDBEventHandler(), DBEventService.SHARING.toString());
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

        for(String publisher : publishers){

            log.info("Sending service discovery message. Publisher : " + publisher + ", Subscriber : " + DBEventService.SHARING.toString());

            DBEventSubscriber dbEventSubscriber = new DBEventSubscriber(DBEventService.SHARING.toString());
            DBEventMessageContext dbEventMessageContext = new DBEventMessageContext();
            dbEventMessageContext.setSubscriber(dbEventSubscriber);

            DBEventMessage dbEventMessage = new DBEventMessage(DBEventType.SUBSCRIBER, dbEventMessageContext, publisher);

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            getDBEventPublisher().publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));

        }

        return true;
    }

}
