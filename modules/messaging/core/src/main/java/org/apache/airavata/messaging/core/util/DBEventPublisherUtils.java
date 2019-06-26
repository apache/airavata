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
package org.apache.airavata.messaging.core.util;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.dbevent.DBEventPublisher;
import org.apache.airavata.model.dbevent.DBEventPublisherContext;
import org.apache.airavata.model.dbevent.DBEventType;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBEventPublisherUtils
 */
public class DBEventPublisherUtils {

    private final static Logger logger = LoggerFactory.getLogger(DBEventPublisherUtils.class);
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
    public void publish(EntityType entityType, CrudType crudType, TBase entityModel) throws AiravataException {

        getDbEventPublisher().publish(getDBEventMessageContext(entityType, crudType, entityModel),
                DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));
    }

    /**
     * Returns singleton instance of dbEventPublisher
     * @return
     * @throws AiravataException
     */
    private Publisher getDbEventPublisher() throws AiravataException {
        if(null == dbEventPublisher){
            synchronized (this){
                if(null == dbEventPublisher){
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
    private MessageContext getDBEventMessageContext(EntityType entityType, CrudType crudType, TBase entityModel) throws AiravataException {
        try {
            // set the publisherContext
            DBEventMessage dbEventMessage = new DBEventMessage();
            DBEventPublisherContext publisherContext = new DBEventPublisherContext();
            publisherContext.setCrudType(crudType);
            publisherContext.setEntityDataModel(ThriftUtils.serializeThriftObject(entityModel));
            publisherContext.setEntityType(entityType);

            // create dbEventPublisher with publisherContext
            DBEventPublisher dbEventPublisher = new DBEventPublisher();
            dbEventPublisher.setPublisherContext(publisherContext);

            // set messageContext to dbEventPublisher
            DBEventMessageContext dbMessageContext = DBEventMessageContext.publisher(dbEventPublisher);

            // set dbEventMessage with messageContext
            dbEventMessage.setDbEventType(DBEventType.PUBLISHER);
            dbEventMessage.setPublisherService(this.publisherService.toString());
            dbEventMessage.setMessageContext(dbMessageContext);

            // construct and return messageContext
            return new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");
        } catch (Exception ex) {
            throw new AiravataException(ex.getMessage(), ex);
        }
    }
}
