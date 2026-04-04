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
package org.apache.airavata.orchestration.event;

import java.util.List;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.messaging.service.MessageContext;
import org.apache.airavata.messaging.service.MessageHandler;
import org.apache.airavata.messaging.util.DBEventPublisherUtils;
import org.apache.airavata.messaging.util.DBEventService;
import org.apache.airavata.model.dbevent.proto.CrudType;
import org.apache.airavata.model.dbevent.proto.DBEventMessage;
import org.apache.airavata.model.dbevent.proto.DBEventPublisherContext;
import org.apache.airavata.model.dbevent.proto.EntityType;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by goshenoy on 3/30/17.
 */
public class RegistryServiceDBEventHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceDBEventHandler.class);
    private final RegistryHandler registryHandler;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.REGISTRY);

    public RegistryServiceDBEventHandler() {
        this.registryHandler = SchedulerUtils.getRegistryHandler();
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        logger.info("RegistryServiceDBEventHandler | Received a new message!");

        try {
            // parse proto dbeventmessage from event
            DBEventMessage dbEventMessage = (DBEventMessage) messageContext.getEvent();
            logger.info("RegistryService received db-event-message from publisher: "
                    + dbEventMessage.getPublisherService());

            // get publisher context
            DBEventPublisherContext publisherContext =
                    dbEventMessage.getMessageContext().getPublisher().getPublisherContext();
            logger.info("RegistryService, Replicated Entity: " + publisherContext.getEntityType());

            RegistryHandler registryClient = registryHandler;
            // this try-block is mainly for catching DuplicateEntryException
            try {
                // check type of entity-type
                switch (publisherContext.getEntityType()) {
                    // Gateway related operations
                    case TENANT: {
                        // construct gateway datamodel from message
                        Gateway gateway = Gateway.parseFrom(publisherContext.getEntityDataModel());

                        // call service-methods based on CRUD type
                        switch (publisherContext.getCrudType()) {
                            case CREATE: {
                                logger.info("Replicating addGateway in Registry.");
                                registryClient.addGateway(gateway);
                                logger.info("addGateway Replication Success!");
                                break;
                            }
                            case UPDATE: {
                                logger.info("Replicating updateGateway in Registry.");
                                if (!registryClient.isGatewayExist(gateway.getGatewayId())) {
                                    logger.info("Gateway doesn't exist so adding instead of updating.");
                                    registryClient.addGateway(gateway);
                                } else {
                                    registryClient.updateGateway(gateway.getGatewayId(), gateway);
                                }
                                logger.info("updateGateway Replication Success!");
                                break;
                            }
                            case DELETE: {
                                logger.info("Replicating deleteGateway in Registry.");
                                registryClient.deleteGateway(gateway.getGatewayId());
                                logger.info("deleteGateway Replication Success!");
                                break;
                            }
                            case READ: {
                                logger.info("Replicating readGateway in Registry: " + publisherContext.getCrudType());
                                // TODO: find appropriate method
                                break;
                            }
                        }
                        // break entity: gateway
                        break;
                    }

                    // UserProfile related operations
                    case USER_PROFILE: {
                        // construct userprofile datamodel from message
                        UserProfile userProfile = UserProfile.parseFrom(publisherContext.getEntityDataModel());

                        // call service-methods based on CRUD type
                        switch (publisherContext.getCrudType()) {
                            case CREATE: {
                                logger.info("Replicating addUser in Registry.");
                                if (!registryClient.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                                    registryClient.addUser(userProfile);
                                }
                                Project defaultProject = createDefaultProject(registryClient, userProfile);
                                if (defaultProject != null) {

                                    // Publish new PROJECT event (sharing service will listen for it and register this
                                    // as a shared Entity)
                                    dbEventPublisherUtils.publish(EntityType.PROJECT, CrudType.CREATE, defaultProject);
                                }
                                logger.info("addUser Replication Success!");
                                break;
                            }
                            case UPDATE: {
                                logger.info(
                                        "Replicating updateGateway in Registry.",
                                        publisherContext.getEntityDataModel());
                                // TODO: find appropriate method
                                break;
                            }
                            case DELETE: {
                                logger.info(
                                        "Replicating deleteGateway in Registry.",
                                        publisherContext.getEntityDataModel());
                                // TODO: find appropriate method
                                break;
                            }
                            case READ: {
                                logger.info("Replicating readGateway in Registry: " + publisherContext.getCrudType());
                                // TODO: find appropriate method
                                break;
                            }
                        }
                        // break entity: userprofile
                        break;
                    }

                    // no handler for entity
                    default: {
                        logger.error("Handler not defined for Entity: " + publisherContext.getEntityType());
                    }
                }

            } catch (Exception ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("DuplicateEntry")) {
                    // log this exception and proceed (do nothing)
                    // this exception is thrown mostly when messages are re-consumed, hence ignore
                    logger.warn("DuplicateEntryException while consuming db-event message, ex: " + ex.getMessage(), ex);
                    return;
                }
                throw ex;
            }
            // send ack for received message
            logger.info("RegistryServiceDBEventHandler | Sending ack. Message Delivery Tag: "
                    + messageContext.getDeliveryTag());
            RegistryServiceDBEventMessagingFactory.getDBEventSubscriber().sendAck(messageContext.getDeliveryTag());
        } catch (Exception ex) {
            logger.error("Error processing message: " + ex, ex);
        } catch (Throwable t) {
            // Catch all exceptions types otherwise RabbitMQ's DefaultExceptionHandler will close the channel
            logger.error("Failed to handle message: " + t, t);
        }
    }

    private Project createDefaultProject(RegistryHandler registryClient, UserProfile userProfile) throws Exception {
        // Just retrieve the first project to see if the user has any projects
        List<Project> projects =
                registryClient.getUserProjects(userProfile.getGatewayId(), userProfile.getUserId(), 1, 0);
        if (projects.isEmpty()) {
            Project defaultProject = Project.newBuilder()
                    .setOwner(userProfile.getUserId())
                    .setName("Default Project")
                    .setGatewayId(userProfile.getGatewayId())
                    .setDescription("This is the default project for user " + userProfile.getUserId())
                    .build();
            String defaultProjectId = registryClient.createProject(userProfile.getGatewayId(), defaultProject);
            logger.info("Default project created for user {}", userProfile.getUserId());
            defaultProject =
                    defaultProject.toBuilder().setProjectId(defaultProjectId).build();
            return defaultProject;
        }
        return null;
    }
}
