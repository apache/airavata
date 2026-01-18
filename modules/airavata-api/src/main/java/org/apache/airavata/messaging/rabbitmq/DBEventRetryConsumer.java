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
package org.apache.airavata.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.CrudType;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventPublisherContext;
import org.apache.airavata.common.model.EntityType;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.config.JacksonConfig;
import org.apache.airavata.messaging.Dispatcher;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.messaging.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DBEventRetryConsumer consumes failed DB events from RabbitMQ retry queue.
 * This is the ONLY RabbitMQ consumer for DB events (database state synchronization).
 * All other messaging uses Kafka.
 *
 * All RabbitMQ messages are Jackson JSON-serialized domain models (never Thrift).
 * Messages are deserialized from JSON using Jackson ObjectMapper.
 */
@Component
@Profile("!test")
public class DBEventRetryConsumer implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DBEventRetryConsumer.class);

    private static ObjectMapper objectMapper() {
        return JacksonConfig.getGlobalMapper();
    }

    private final Dispatcher dispatcher;
    private final MessagingFactory messagingFactory;
    private Subscriber retryQueueSubscriber;

    public DBEventRetryConsumer(Dispatcher dispatcher, MessagingFactory messagingFactory) {
        this.dispatcher = dispatcher;
        this.messagingFactory = messagingFactory;
    }

    @PostConstruct
    private void initializeRetryConsumer() throws AiravataException {
        logger.info("Initializing DB Event retry consumer for RabbitMQ (DB state sync only)");
        try {
            String serviceName = DBEventService.DB_EVENT.toString() + ".retry";
            // Queue name will be: db.event.retry.queue
            retryQueueSubscriber = messagingFactory.getDBEventSubscriber(this, serviceName);
            logger.info("DB Event retry consumer initialized and listening on RabbitMQ queue: {}", serviceName);
        } catch (AiravataException e) {
            logger.error("Error initializing DB Event retry consumer", e);
            throw e;
        }
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        logger.info(
                "DBEventRetryConsumer received retry message (JSON-deserialized). Message Id: {}",
                messageContext.getMessageId());

        try {
            // MessageContext was already deserialized from JSON via MessageContext.Wrapper in MessageConsumer
            DBEventMessage dbEventMessage = (DBEventMessage) messageContext.getEvent();
            DBEventPublisherContext publisherContext =
                    dbEventMessage.getMessageContext().getPublisher().getPublisherContext();

            EntityType entityType = publisherContext.getEntityType();
            CrudType crudType = publisherContext.getCrudType();

            // Extract JSON bytes from entity data (domain model serialized as JSON, never Thrift)
            ByteBuffer entityJsonBuffer = publisherContext.getEntityDataModel();
            byte[] entityJsonBytes = new byte[entityJsonBuffer.remaining()];
            entityJsonBuffer.duplicate().get(entityJsonBytes);

            // Deserialize JSON bytes to domain model using Jackson (RabbitMQ uses JSON, never Thrift)
            Class<?> entityClass = getEntityClass(entityType);
            Object domainModel = objectMapper().readValue(entityJsonBytes, entityClass);

            // Retry dispatch with domain model (already deserialized from JSON)
            logger.info("Retrying DB event dispatch: entityType={}, crudType={}", entityType, crudType);
            dispatcher.dispatch(entityType, crudType, domainModel);

            logger.info("Retry successful. Sending ack. Message Delivery Tag: {}", messageContext.getDeliveryTag());
            retryQueueSubscriber.sendAck(messageContext.getDeliveryTag());

        } catch (Exception e) {
            logger.error(
                    "Error processing retry message. Message Delivery Tag: {}", messageContext.getDeliveryTag(), e);
            // Message will be requeued by RabbitMQ if not acknowledged
            // After max retries, it will go to dead letter queue
        }
    }

    private Class<?> getEntityClass(EntityType entityType) {
        return switch (entityType) {
            case TENANT -> org.apache.airavata.common.model.Gateway.class;
            case USER_PROFILE -> org.apache.airavata.common.model.UserProfile.class;
            case PROJECT -> org.apache.airavata.common.model.Project.class;
            default -> throw new IllegalArgumentException("Unknown entity type: " + entityType);
        };
    }
}
