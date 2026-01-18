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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.model.CrudType;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventPublisherContext;
import org.apache.airavata.common.model.EntityType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.config.JacksonConfig;
import org.apache.airavata.messaging.Dispatcher;
import org.apache.airavata.messaging.MessageContext;
import org.apache.airavata.messaging.MessageHandler;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by goshenoy on 3/30/17.
 */
@Component
public class RegistryServiceDBEventHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceDBEventHandler.class);

    private static ObjectMapper objectMapper() {
        return JacksonConfig.getGlobalMapper();
    }

    private final RegistryService registryService;
    private final RegistryServiceDBEventMessagingFactory messagingFactory;
    private final Dispatcher dbEventDispatcher;

    public RegistryServiceDBEventHandler(
            RegistryService registryService,
            RegistryServiceDBEventMessagingFactory messagingFactory,
            Dispatcher dbEventDispatcher) {
        this.registryService = registryService;
        this.messagingFactory = messagingFactory;
        this.dbEventDispatcher = dbEventDispatcher;
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        logger.info("RegistryServiceDBEventHandler | Received a new message!");

        try {
            // DBEventMessage now extends MessagingEvent, so we can cast directly
            DBEventMessage dbEventMessage = (DBEventMessage) messageContext.getEvent();
            logger.info("RegistryService received db-event-message from publisher: "
                    + dbEventMessage.getPublisherService());

            // get publisher context
            DBEventPublisherContext publisherContext =
                    dbEventMessage.getMessageContext().getPublisher().getPublisherContext();
            logger.info("RegistryService, Replicated Entity: " + publisherContext.getEntityType());

            // check type of entity-type
            switch (publisherContext.getEntityType()) {
                // Gateway related operations
                case TENANT -> {
                    // Deserialize JSON to domain model
                    var entityDataBuffer = publisherContext.getEntityDataModel();
                    var entityDataBytes = new byte[entityDataBuffer.remaining()];
                    entityDataBuffer.duplicate().get(entityDataBytes);

                    var gateway = objectMapper().readValue(entityDataBytes, Gateway.class);

                    // call service-methods based on CRUD type
                    switch (publisherContext.getCrudType()) {
                        case CREATE -> {
                            logger.info("Replicating addGateway in Registry.");
                            registryService.addGateway(gateway);
                            logger.info("addGateway Replication Success!");
                        }
                        case UPDATE -> {
                            logger.info("Replicating updateGateway in Registry.");
                            if (!registryService.isGatewayExist(gateway.getGatewayId())) {
                                logger.info("Gateway doesn't exist so adding instead of updating.");
                                registryService.addGateway(gateway);
                            } else {
                                registryService.updateGateway(gateway.getGatewayId(), gateway);
                            }
                            logger.info("updateGateway Replication Success!");
                        }
                        case DELETE -> {
                            logger.info("Replicating deleteGateway in Registry.");
                            registryService.deleteGateway(gateway.getGatewayId());
                            logger.info("deleteGateway Replication Success!");
                        }
                    }
                }

                // UserProfile related operations
                case USER_PROFILE -> {
                    // Deserialize JSON to domain model
                    var entityDataBuffer = publisherContext.getEntityDataModel();
                    var entityDataBytes = new byte[entityDataBuffer.remaining()];
                    entityDataBuffer.duplicate().get(entityDataBytes);

                    var userProfile = objectMapper().readValue(entityDataBytes, UserProfile.class);

                    // call service-methods based on CRUD type
                    switch (publisherContext.getCrudType()) {
                        case CREATE -> {
                            logger.info("Replicating addUser in Registry.");
                            if (!registryService.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                                registryService.addUser(userProfile);
                            }
                            var defaultProject = createDefaultProject(registryService, userProfile);
                            if (defaultProject != null) {

                                // Dispatch new PROJECT event (sharing service will handle it)
                                try {
                                    dbEventDispatcher.dispatch(EntityType.PROJECT, CrudType.CREATE, defaultProject);
                                } catch (AiravataException e) {
                                    logger.error("Error dispatching PROJECT create event for default project", e);
                                }
                            }
                            logger.info("addUser Replication Success!");
                        }
                        case UPDATE -> {
                            logger.info(
                                    "Replicating updateGateway in Registry.", publisherContext.getEntityDataModel());
                            // TODO: find appropriate method
                        }
                        case DELETE -> {
                            logger.info(
                                    "Replicating deleteGateway in Registry.", publisherContext.getEntityDataModel());
                            // TODO: find appropriate method
                        }
                    }
                }

                // no handler for entity
                default -> logger.error("Handler not defined for Entity: " + publisherContext.getEntityType());
            }
            // send ack for received message
            logger.info("RegistryServiceDBEventHandler | Sending ack. Message Delivery Tag: "
                    + messageContext.getDeliveryTag());
            messagingFactory.getDBEventSubscriber().sendAck(messageContext.getDeliveryTag());
        } catch (RegistryServiceException ex) {
            logger.error("Error processing message: " + ex, ex);
        } catch (ApplicationSettingsException ex) {
            logger.error("Error fetching application settings: " + ex, ex);
        } catch (AiravataException ex) {
            logger.error("Error sending ack. Message Delivery Tag: " + messageContext.getDeliveryTag(), ex);
        } catch (Throwable t) {
            // Catch all exceptions types otherwise RabbitMQ's DefaultExceptionHandler will close the channel
            logger.error("Failed to handle message: " + t, t);
        }
    }

    private Project createDefaultProject(RegistryService registryService, UserProfile userProfile)
            throws RegistryServiceException {
        // Just retrieve the first project to see if the user has any projects
        List<Project> projects =
                registryService.getUserProjects(userProfile.getGatewayId(), userProfile.getUserId(), 1, 0);
        if (projects.isEmpty()) {
            Project defaultProject = new Project();
            defaultProject.setOwner(userProfile.getUserId());
            defaultProject.setName("Default Project");
            defaultProject.setGatewayId(userProfile.getGatewayId());
            defaultProject.setDescription("This is the default project for user " + userProfile.getUserId());
            String defaultProjectId = registryService.createProject(userProfile.getGatewayId(), defaultProject);
            logger.info("Default project created for user {}", userProfile.getUserId());
            defaultProject.setProjectID(defaultProjectId);
            return defaultProject;
        }
        return null;
    }
}
