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
package org.apache.airavata.messaging.util;

import com.google.protobuf.MessageLite;
import org.apache.airavata.exception.AiravataException;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessagingFactory;
import org.apache.airavata.messaging.service.Publisher;
import org.apache.airavata.model.dbevent.proto.CrudType;
import org.apache.airavata.model.dbevent.proto.DBEventMessage;
import org.apache.airavata.model.dbevent.proto.DBEventMessageContext;
import org.apache.airavata.model.dbevent.proto.DBEventPublisher;
import org.apache.airavata.model.dbevent.proto.DBEventPublisherContext;
import org.apache.airavata.model.dbevent.proto.DBEventType;
import org.apache.airavata.model.dbevent.proto.EntityType;
import org.apache.airavata.model.messaging.event.proto.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBEventPublisherUtils
 */
public class DBEventPublisherUtils {

    private static final Logger logger = LoggerFactory.getLogger(DBEventPublisherUtils.class);
    private Publisher dbEventPublisher = null;
    private DBEventService publisherService;

    public DBEventPublisherUtils(DBEventService dbEventService) {
        this.publisherService = dbEventService;
    }

    /**
     * Publish DB Event for given entity.
     * @param entityType
     * @param crudType
     * @param entityModel
     */
    public void publish(EntityType entityType, CrudType crudType, MessageLite entityModel) throws AiravataException {

        getDbEventPublisher()
                .publish(
                        getDBEventMessageContext(entityType, crudType, entityModel),
                        DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));
    }

    /**
     * Returns singleton instance of dbEventPublisher
     * @return
     * @throws AiravataException
     */
    private Publisher getDbEventPublisher() throws AiravataException {
        if (null == dbEventPublisher) {
            synchronized (this) {
                if (null == dbEventPublisher) {
                    logger.info("Creating DB Event publisher.....");
                    dbEventPublisher = MessagingFactory.getDBEventPublisher();
                    logger.info("DB Event publisher created");
                }
            }
        }
        return dbEventPublisher;
    }

    /**
     * Constructs the dbEventMessageContext
     * @param entityType
     * @param crudType
     * @param entityModel
     * @return
     * @throws AiravataException
     */
    private MessageContext getDBEventMessageContext(EntityType entityType, CrudType crudType, MessageLite entityModel)
            throws AiravataException {
        try {
            // set the publisherContext
            DBEventPublisherContext publisherContext = DBEventPublisherContext.newBuilder()
                    .setCrudType(crudType)
                    .setEntityDataModel(entityModel.toByteString())
                    .setEntityType(entityType)
                    .build();

            // create dbEventPublisher with publisherContext
            DBEventPublisher dbEventPublisher = DBEventPublisher.newBuilder()
                    .setPublisherContext(publisherContext)
                    .build();

            // set messageContext to dbEventPublisher
            DBEventMessageContext dbMessageContext = DBEventMessageContext.newBuilder()
                    .setPublisher(dbEventPublisher)
                    .build();

            // set dbEventMessage with messageContext
            DBEventMessage dbEventMessage = DBEventMessage.newBuilder()
                    .setDbEventType(DBEventType.PUBLISHER)
                    .setPublisherService(this.publisherService.toString())
                    .setMessageContext(dbMessageContext)
                    .build();

            // construct and return messageContext
            return new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");
        } catch (Exception ex) {
            throw new AiravataException(ex.getMessage(), ex);
        }
    }
}
