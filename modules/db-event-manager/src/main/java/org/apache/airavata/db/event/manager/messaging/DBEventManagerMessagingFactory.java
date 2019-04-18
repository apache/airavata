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
package org.apache.airavata.db.event.manager.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.db.event.manager.messaging.impl.DBEventMessageHandler;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/1/17.
 */
public class DBEventManagerMessagingFactory {

    private final static Logger log = LoggerFactory.getLogger(DBEventManagerMessagingFactory.class);

    private static Subscriber dbEventSubscriber;

    private static Publisher dbEventPublisher;

    /**
     * Get DB Event subscriber
     * @return
     * @throws AiravataException
     */
    public static Subscriber getDBEventSubscriber() throws AiravataException {
        if(null == dbEventSubscriber){
            synchronized (DBEventManagerMessagingFactory.class){
                if(null == dbEventSubscriber){
                    log.info("Creating DB Event subscriber.....");
                    dbEventSubscriber = MessagingFactory.getDBEventSubscriber(new DBEventMessageHandler(), DBEventService.DB_EVENT.toString());
                    log.info("DB Event subscriber created");
                }
            }
        }
        return dbEventSubscriber;
    }


    public static Publisher getDBEventPublisher() throws AiravataException {
        if(null == dbEventPublisher){
            synchronized (DBEventManagerMessagingFactory.class){
                if(null == dbEventPublisher){
                    log.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    log.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

}
