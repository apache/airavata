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
package org.apache.airavata.messaging.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.CrudType;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventMessageContext;
import org.apache.airavata.common.model.DBEventPublisher;
import org.apache.airavata.common.model.DBEventPublisherContext;
import org.apache.airavata.common.model.DBEventType;
import org.apache.airavata.common.model.EntityType;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessagingFactory;
import org.apache.airavata.messaging.core.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBEventPublisherUtils
 */
public class DBEventPublisherUtils {

    private static final Logger logger = LoggerFactory.getLogger(DBEventPublisherUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private Publisher dbEventPublisher = null;
    private DBEventService publisherService;
    private final MessagingFactory messagingFactory;
    private final ThriftToDomainMapperRegistry mapperRegistry;

    public DBEventPublisherUtils(
            DBEventService dbEventService,
            MessagingFactory messagingFactory,
            ThriftToDomainMapperRegistry mapperRegistry) {
        this.publisherService = dbEventService;
        this.messagingFactory = messagingFactory;
        this.mapperRegistry = mapperRegistry;
    }

    /**
     * Publish DB Event for given entity.
     * @param entityType
     * @param crudType
     * @param entityModel Can be either a domain model or Thrift model (detected via reflection)
     */
    public void publish(EntityType entityType, CrudType crudType, Object entityModel) throws AiravataException {

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
                    dbEventPublisher = messagingFactory.getDBEventPublisher();
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
     * @param entityModel Can be either a domain model or Thrift model (detected via reflection)
     * @return
     * @throws AiravataException
     */
    private MessageContext getDBEventMessageContext(EntityType entityType, CrudType crudType, Object entityModel)
            throws AiravataException {
        try {
            // Check if entityModel is a Thrift model (from thriftapi package) or domain model
            Object domainModel;
            if (isThriftModel(entityModel)) {
                // Convert Thrift model to domain model, then serialize to JSON
                domainModel = convertThriftEntityToDomain(entityType, entityModel);
            } else {
                // Already a domain model, use directly
                domainModel = entityModel;
            }
            byte[] jsonBytes = objectMapper.writeValueAsBytes(domainModel);

            // set the publisherContext
            DBEventMessage dbEventMessage = new DBEventMessage();
            DBEventPublisherContext publisherContext = new DBEventPublisherContext();
            publisherContext.setCrudType(crudType);
            publisherContext.setEntityDataModel(java.nio.ByteBuffer.wrap(jsonBytes));
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

    private boolean isThriftModel(Object model) {
        // Check if the model is from the thriftapi package (Thrift-generated class)
        return model != null && model.getClass().getName().startsWith("org.apache.airavata.thriftapi");
    }

    private Object convertThriftEntityToDomain(EntityType entityType, Object thriftModel) throws Exception {
        // Use mapper registry instead of reflection
        return mapperRegistry.convertToDomain(entityType, thriftModel);
    }
}
