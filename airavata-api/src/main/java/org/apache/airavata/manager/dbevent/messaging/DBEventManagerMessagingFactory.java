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
package org.apache.airavata.manager.dbevent.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.manager.dbevent.messaging.impl.DBEventMessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Ajinkya on 3/1/17.
 */
@Component
public class DBEventManagerMessagingFactory {

    private static final Logger log = LoggerFactory.getLogger(DBEventManagerMessagingFactory.class);

    private Subscriber dbEventSubscriber;

    private Publisher dbEventPublisher;

    private final MessagingFactory messagingFactory;

    public DBEventManagerMessagingFactory(MessagingFactory messagingFactory) {
        this.messagingFactory = messagingFactory;
    }

    /**
     * Get DB Event subscriber
     * @return
     * @throws AiravataException
     */
    public Subscriber getDBEventSubscriber(AiravataServerProperties properties) throws AiravataException {
        if (null == dbEventSubscriber) {
            synchronized (this) {
                if (null == dbEventSubscriber) {
                    log.info("Creating DB Event subscriber.....");
                    dbEventSubscriber = messagingFactory.getDBEventSubscriber(
                            new DBEventMessageHandler(properties, this), DBEventService.DB_EVENT.toString());
                    log.info("DB Event subscriber created");
                }
            }
        }
        return dbEventSubscriber;
    }

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
}
