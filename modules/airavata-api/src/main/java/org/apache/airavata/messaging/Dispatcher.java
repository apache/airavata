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
package org.apache.airavata.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.model.CrudType;
import org.apache.airavata.common.model.DBEventMessage;
import org.apache.airavata.common.model.DBEventMessageContext;
import org.apache.airavata.common.model.DBEventPublisher;
import org.apache.airavata.common.model.DBEventPublisherContext;
import org.apache.airavata.common.model.DBEventType;
import org.apache.airavata.common.model.EntityType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.MessageType;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.SharingResourceType;
import org.apache.airavata.common.model.UserProfile;
import org.apache.airavata.common.utils.DBEventManagerConstants;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.messaging.rabbitmq.MessagingFactory;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.sharing.model.Domain;
import org.apache.airavata.sharing.model.DuplicateEntryException;
import org.apache.airavata.sharing.model.Entity;
import org.apache.airavata.sharing.model.PermissionType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DirectDBEventDispatcher replaces DBEventPublisherUtils with direct service calls.
 * Makes synchronous calls to RegistryService and SharingRegistryService.
 * On failure, publishes to RabbitMQ retry queue for database state synchronization.
 */
@Component
public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RegistryService registryService;
    private final SharingRegistryService sharingRegistryService;
    private final MessagingFactory messagingFactory;
    private Publisher retryQueuePublisher = null;

    public Dispatcher(
            RegistryService registryService,
            SharingRegistryService sharingRegistryService,
            MessagingFactory messagingFactory) {
        this.registryService = registryService;
        this.sharingRegistryService = sharingRegistryService;
        this.messagingFactory = messagingFactory;
    }

    /**
     * Dispatch DB event by making direct service calls.
     * On failure, publishes to RabbitMQ retry queue for database state synchronization.
     * All RabbitMQ messages use Jackson JSON serialization of domain models (never Thrift).
     *
     * @param entityType The type of entity
     * @param crudType The CRUD operation type
     * @param domainModel Domain model
     * @throws AiravataException
     */
    public void dispatch(EntityType entityType, CrudType crudType, Object domainModel) throws AiravataException {
        try {
            // Make direct service calls based on entity type
            switch (entityType) {
                case TENANT -> dispatchTenantEvent(crudType, domainModel);
                case USER_PROFILE -> dispatchUserProfileEvent(crudType, domainModel);
                case PROJECT -> dispatchProjectEvent(crudType, domainModel);
                default -> {
                    logger.warn("No handler for entity type: {}", entityType);
                }
            }
        } catch (Exception e) {
            logger.error("Error dispatching DB event: entityType={}, crudType={}", entityType, crudType, e);
            // Publish to RabbitMQ retry queue for database state synchronization
            publishToRetryQueue(entityType, crudType, domainModel);
            throw new AiravataException("Failed to dispatch DB event, published to retry queue", e);
        }
    }

    private void dispatchTenantEvent(CrudType crudType, Object domainModel) throws Exception {
        Gateway gateway = (Gateway) domainModel;

        switch (crudType) {
            case CREATE -> {
                // RegistryService: addGateway
                try {
                    if (!registryService.isGatewayExist(gateway.getGatewayId())) {
                        registryService.addGateway(gateway);
                        logger.info("Replicated addGateway in Registry for gateway: {}", gateway.getGatewayId());
                    }
                } catch (RegistryServiceException e) {
                    logger.error("Error adding gateway to RegistryService", e);
                    throw e;
                }

                // SharingService: createDomain and related entities
                try {
                    if (!sharingRegistryService.isDomainExists(gateway.getGatewayId())) {
                        Domain domain = new Domain();
                        domain.setDomainId(gateway.getGatewayId());
                        domain.setName(gateway.getGatewayName());
                        domain.setDescription("Domain entry for " + domain.getName());
                        sharingRegistryService.createDomain(domain);
                        logger.info("Created domain in SharingService: {}", gateway.getGatewayId());

                        // Create entity types
                        createEntityTypesForDomain(gateway.getGatewayId());
                        // Create permission types
                        createPermissionTypesForDomain(gateway.getGatewayId());
                    }
                } catch (SharingRegistryException e) {
                    logger.error("Error creating domain in SharingService", e);
                    throw e;
                }
            }
            case UPDATE -> {
                // RegistryService: updateGateway
                try {
                    if (!registryService.isGatewayExist(gateway.getGatewayId())) {
                        registryService.addGateway(gateway);
                        logger.info("Gateway doesn't exist, added instead of updating: {}", gateway.getGatewayId());
                    } else {
                        registryService.updateGateway(gateway.getGatewayId(), gateway);
                        logger.info("Replicated updateGateway in Registry for gateway: {}", gateway.getGatewayId());
                    }
                } catch (RegistryServiceException e) {
                    logger.error("Error updating gateway in RegistryService", e);
                    throw e;
                }
            }
            case DELETE -> {
                // RegistryService: deleteGateway
                try {
                    registryService.deleteGateway(gateway.getGatewayId());
                    logger.info("Replicated deleteGateway in Registry for gateway: {}", gateway.getGatewayId());
                } catch (RegistryServiceException e) {
                    logger.error("Error deleting gateway in RegistryService", e);
                    throw e;
                }
            }
        }
    }

    private void dispatchUserProfileEvent(CrudType crudType, Object domainModel) throws Exception {
        UserProfile userProfile = (UserProfile) domainModel;

        switch (crudType) {
            case CREATE -> {
                // RegistryService: addUser and createDefaultProject
                try {
                    if (!registryService.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                        registryService.addUser(userProfile);
                        logger.info("Replicated addUser in Registry for user: {}", userProfile.getUserId());
                    }

                    // Create default project if needed
                    List<Project> projects =
                            registryService.getUserProjects(userProfile.getGatewayId(), userProfile.getUserId(), 1, 0);
                    if (projects.isEmpty()) {
                        Project defaultProject = new Project();
                        defaultProject.setOwner(userProfile.getUserId());
                        defaultProject.setName("Default Project");
                        defaultProject.setGatewayId(userProfile.getGatewayId());
                        defaultProject.setDescription(
                                "This is the default project for user " + userProfile.getUserId());
                        String defaultProjectId =
                                registryService.createProject(userProfile.getGatewayId(), defaultProject);
                        logger.info("Default project created for user: {}", userProfile.getUserId());
                        defaultProject.setProjectID(defaultProjectId);

                        // Dispatch PROJECT event for the default project
                        dispatch(EntityType.PROJECT, CrudType.CREATE, defaultProject);
                    }
                } catch (RegistryServiceException e) {
                    logger.error("Error adding user in RegistryService", e);
                    throw e;
                }

                // SharingService: createUser
                try {
                    User user = convertUserProfileToUser(userProfile);
                    if (!sharingRegistryService.isUserExists(user.getDomainId(), user.getUserId())) {
                        sharingRegistryService.createUser(user);
                        logger.info("Created user in SharingService: {}", user.getUserId());
                    } else {
                        sharingRegistryService.updatedUser(user);
                        logger.info("Updated user in SharingService: {}", user.getUserId());
                    }
                } catch (SharingRegistryException e) {
                    logger.error("Error creating user in SharingService", e);
                    throw e;
                }
            }
            case UPDATE -> {
                // SharingService: updatedUser
                try {
                    User user = convertUserProfileToUser(userProfile);
                    if (sharingRegistryService.isUserExists(user.getDomainId(), user.getUserId())) {
                        sharingRegistryService.updatedUser(user);
                        logger.info("Updated user in SharingService: {}", user.getUserId());
                    }
                } catch (SharingRegistryException e) {
                    logger.error("Error updating user in SharingService", e);
                    throw e;
                }
            }
            case DELETE -> {
                // SharingService: deleteUser
                try {
                    sharingRegistryService.deleteUser(userProfile.getGatewayId(), userProfile.getUserId());
                    logger.info("Deleted user in SharingService: {}", userProfile.getUserId());
                } catch (SharingRegistryException e) {
                    logger.error("Error deleting user in SharingService", e);
                    throw e;
                }
            }
        }
    }

    private void dispatchProjectEvent(CrudType crudType, Object domainModel) throws Exception {
        Project project = (Project) domainModel;
        String domainId = project.getGatewayId();
        String entityId = project.getProjectID();

        switch (crudType) {
            case CREATE, UPDATE -> {
                // SharingService: createEntity or updateEntity
                try {
                    Entity entity = new Entity();
                    entity.setEntityId(entityId);
                    entity.setDomainId(domainId);
                    entity.setEntityTypeId(domainId + ":" + SharingResourceType.PROJECT.name());
                    entity.setOwnerId(project.getOwner() + "@" + domainId);
                    entity.setName(project.getName());
                    entity.setDescription(project.getDescription());

                    if (!sharingRegistryService.isEntityExists(domainId, entityId)) {
                        sharingRegistryService.createEntity(entity);
                        logger.info("Created project entity in SharingService: {}", entityId);
                    } else {
                        sharingRegistryService.updateEntity(entity);
                        logger.info("Updated project entity in SharingService: {}", entityId);
                    }
                } catch (SharingRegistryException e) {
                    logger.error("Error creating/updating project entity in SharingService", e);
                    throw e;
                }
            }
            case DELETE -> {
                // SharingService: deleteEntity
                try {
                    sharingRegistryService.deleteEntity(domainId, entityId);
                    logger.info("Deleted project entity in SharingService: {}", entityId);
                } catch (SharingRegistryException e) {
                    logger.error("Error deleting project entity in SharingService", e);
                    throw e;
                }
            }
        }
    }

    private void createEntityTypesForDomain(String domainId) {
        String[] entityTypeNames = {
            "PROJECT",
            "EXPERIMENT",
            "FILE",
            SharingResourceType.APPLICATION_DEPLOYMENT.name(),
            SharingResourceType.GROUP_RESOURCE_PROFILE.name(),
            SharingResourceType.CREDENTIAL_TOKEN.name()
        };

        for (String entityTypeName : entityTypeNames) {
            try {
                String entityTypeId = domainId + ":" + entityTypeName;
                if (!sharingRegistryService.isEntityTypeExists(domainId, entityTypeId)) {
                    org.apache.airavata.sharing.model.EntityType entityType =
                            new org.apache.airavata.sharing.model.EntityType();
                    entityType.setEntityTypeId(entityTypeId);
                    entityType.setDomainId(domainId);
                    entityType.setName(entityTypeName);
                    entityType.setDescription(entityTypeName + " entity type");
                    sharingRegistryService.createEntityType(entityType);
                    logger.debug("Created entity type: {}", entityTypeId);
                }
            } catch (SharingRegistryException | DuplicateEntryException e) {
                // Log but continue - entity type might already exist
                logger.warn("Error creating entity type {}: {}", entityTypeName, e.getMessage());
            }
        }
    }

    private void createPermissionTypesForDomain(String domainId) {
        String[] permissionNames = {"READ", "WRITE", "MANAGE_SHARING"};

        for (String permissionName : permissionNames) {
            try {
                String permissionTypeId = domainId + ":" + permissionName;
                if (!sharingRegistryService.isPermissionExists(domainId, permissionTypeId)) {
                    PermissionType permissionType = new PermissionType();
                    permissionType.setPermissionTypeId(permissionTypeId);
                    permissionType.setDomainId(domainId);
                    permissionType.setName(permissionName);
                    permissionType.setDescription(permissionName + " permission type");
                    sharingRegistryService.createPermissionType(permissionType);
                    logger.debug("Created permission type: {}", permissionTypeId);
                }
            } catch (SharingRegistryException | DuplicateEntryException e) {
                // Log but continue - permission type might already exist
                logger.warn("Error creating permission type {}: {}", permissionName, e.getMessage());
            }
        }
    }

    private User convertUserProfileToUser(UserProfile userProfile) {
        User user = new User();
        user.setUserId(userProfile.getUserId());
        user.setDomainId(userProfile.getGatewayId());
        user.setUserName(userProfile.getUserId());
        if (userProfile.getEmails() != null && !userProfile.getEmails().isEmpty()) {
            user.setEmail(userProfile.getEmails().get(0));
        }
        user.setFirstName(userProfile.getFirstName());
        user.setLastName(userProfile.getLastName());
        return user;
    }

    private void publishToRetryQueue(EntityType entityType, CrudType crudType, Object entityModel)
            throws AiravataException {
        try {
            // entityModel is always a domain model (RabbitMQ only uses domain models, never Thrift)
            Object domainModel = entityModel;

            // Serialize domain model to JSON using Jackson (RabbitMQ uses JSON, never Thrift)
            byte[] entityJsonBytes = objectMapper.writeValueAsBytes(domainModel);

            // Create DBEventMessage for retry queue (will be Jackson-serialized via MessageContext.Wrapper)
            DBEventMessage dbEventMessage = new DBEventMessage();
            DBEventPublisherContext publisherContext = new DBEventPublisherContext();
            publisherContext.setCrudType(crudType);
            publisherContext.setEntityDataModel(ByteBuffer.wrap(entityJsonBytes));
            publisherContext.setEntityType(entityType);

            DBEventPublisher dbEventPublisher = new DBEventPublisher();
            dbEventPublisher.setPublisherContext(publisherContext);

            DBEventMessageContext dbMessageContext = DBEventMessageContext.publisher(dbEventPublisher);
            dbEventMessage.setDbEventType(DBEventType.PUBLISHER);
            dbEventMessage.setPublisherService(DBEventService.DB_EVENT.toString());
            dbEventMessage.setMessageContext(dbMessageContext);

            MessageContext messageContext = new MessageContext(dbEventMessage, MessageType.DB_EVENT, "", "");

            // Publish to RabbitMQ retry queue (DB state sync only)
            // MessageContext will be Jackson-serialized to JSON via MessageContext.Wrapper in RabbitMQPublisher
            getRetryQueuePublisher()
                    .publish(messageContext, DBEventManagerConstants.getRoutingKey(DBEventService.DB_EVENT.toString()));

            logger.info(
                    "Published failed DB event to RabbitMQ retry queue (JSON-serialized): entityType={}, crudType={}",
                    entityType,
                    crudType);
        } catch (Exception e) {
            logger.error("Error publishing to retry queue", e);
            throw new AiravataException("Failed to publish to retry queue", e);
        }
    }

    private Publisher getRetryQueuePublisher() throws AiravataException {
        if (retryQueuePublisher == null) {
            synchronized (this) {
                if (retryQueuePublisher == null) {
                    logger.info("Creating RabbitMQ retry queue publisher for DB state sync");
                    retryQueuePublisher = messagingFactory.getDBEventPublisher();
                    logger.info("RabbitMQ retry queue publisher created");
                }
            }
        }
        return retryQueuePublisher;
    }
}
