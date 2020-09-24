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
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.error.DuplicateEntryException;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.server.SharingRegistryServer;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.sharing.registry.utils.ThriftDataModelConversion;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventHandler implements MessageHandler {

    private final static Logger log = LoggerFactory.getLogger(SharingServiceDBEventHandler.class);

    private final SharingRegistryService.Client sharingRegistryClient;

    SharingServiceDBEventHandler() throws ApplicationSettingsException, SharingRegistryException {
        log.info("Starting sharing registry client.....");
        sharingRegistryClient = SharingRegistryServiceClientFactory.createSharingRegistryClient(ServerSettings.getSetting(SharingRegistryServer.SHARING_REG_SERVER_HOST), Integer.parseInt(ServerSettings.getSetting(SharingRegistryServer.SHARING_REG_SERVER_PORT)));
    }

    @Override
    public void onMessage(MessageContext messageContext) {

        log.info("New DB Event message to sharing service.");

        try{

            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());

            DBEventMessage dbEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(bytes, dbEventMessage);

            log.info("DB Event message to sharing service from " + dbEventMessage.getPublisherService());

            DBEventMessageContext dBEventMessageContext = dbEventMessage.getMessageContext();
            try{
                switch (dBEventMessageContext.getPublisher().getPublisherContext().getEntityType()){

                    case USER_PROFILE :

                        log.info("User profile specific DB Event communicated by " + dbEventMessage.getPublisherService());

                        UserProfile  userProfile = new UserProfile();
                        ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), userProfile);
                        User user = ThriftDataModelConversion.getUser(userProfile);

                        switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()){

                            case CREATE:
                            case UPDATE:

                                if (!sharingRegistryClient.isUserExists(user.getDomainId(), user.getUserId())) {
                                    log.info("Creating user. User Id : " + user.getUserId());
                                    sharingRegistryClient.createUser(user);
                                    log.debug("User created. User Id : " + user.getUserId());
                                } else {
                                    log.info("Updating user. User Id : " + user.getUserId());
                                    sharingRegistryClient.updatedUser(user);
                                    log.debug("User updated. User Id : " + user.getUserId());
                                }

                                break;

                            case READ:
                                //FIXME: Remove if not required
                                break;

                            case DELETE:
                                log.info("Deleting user. User Id : " + user.getUserId());

                                sharingRegistryClient.deleteUser(user.getDomainId(), user.getUserId());
                                log.debug("User deleted. User Id : " + user.getUserId());

                                break;
                        }
                        break;

                    case TENANT :

                        log.info("Tenant specific DB Event communicated by " + dbEventMessage.getPublisherService());

                        Gateway gateway = new Gateway();
                        ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), gateway);

                        switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()){

                            case CREATE:
                            case UPDATE:

                                // Only create the domain is it doesn't already exist
                                if (sharingRegistryClient.isDomainExists(gateway.getGatewayId())){
                                    break;
                                }
                                /*
                                Following set of DB operations should happen in a transaction
                                As these are thrift calls we cannot enforce this restriction
                                If something goes wrong, message would get queued again and try to create
                                 DB entities which are already present. We catch DuplicateEntryException and
                                 log as a warning to handle such scenarios and move ahead.
                                 */

                                log.info("Creating domain. Id : " + gateway.getGatewayId());

                                Domain domain = new Domain();
                                domain.setDomainId(gateway.getGatewayId());
                                domain.setName(gateway.getGatewayName());
                                domain.setDescription("Domain entry for " + domain.getName());
                                try{
                                    sharingRegistryClient.createDomain(domain);
                                    log.debug("Domain created. Id : " + gateway.getGatewayId());
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Domain Id : " + gateway.getGatewayId(), ex);
                                }

                                //Creating Entity Types for each domain
                                log.info("Creating entity type. Id : " + domain.getDomainId()+":PROJECT");
                                org.apache.airavata.sharing.registry.models.EntityType entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":PROJECT");
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName("PROJECT");
                                entityType.setDescription("Project entity type");
                                try {
                                    sharingRegistryClient.createEntityType(entityType);
                                    log.debug("Entity type created. Id : " + domain.getDomainId()+":PROJECT");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Entity Id : " + domain.getDomainId()+":PROJECT", ex);
                                }

                                log.info("Creating entity type. Id : " + domain.getDomainId()+":EXPERIMENT");
                                entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":EXPERIMENT");
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName("EXPERIMENT");
                                entityType.setDescription("Experiment entity type");
                                try{
                                    sharingRegistryClient.createEntityType(entityType);
                                    log.debug("Entity type created. Id : " + domain.getDomainId()+":EXPERIMENT");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Entity Id : " + domain.getDomainId()+":EXPERIMENT", ex);
                                }

                                log.info("Creating entity type. Id : " + domain.getDomainId()+":FILE");
                                entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":FILE");
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName("FILE");
                                entityType.setDescription("File entity type");
                                try {
                                    sharingRegistryClient.createEntityType(entityType);
                                    log.debug("Entity type created. Id : " + domain.getDomainId() + ":FILE");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Entity Id : " + domain.getDomainId()+":FILE", ex);
                                }

                                log.info("Creating entity type. Id : " + domain.getDomainId()+":"+ResourceType.APPLICATION_DEPLOYMENT);
                                entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":"+ ResourceType.APPLICATION_DEPLOYMENT.name());
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName("APPLICATION-DEPLOYMENT");
                                entityType.setDescription("Application Deployment entity type");
                                sharingRegistryClient.createEntityType(entityType);

                                log.info("Creating entity type. Id : " + domain.getDomainId()+":"+ResourceType.GROUP_RESOURCE_PROFILE);
                                entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":"+ResourceType.GROUP_RESOURCE_PROFILE.name());
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                                entityType.setDescription("Group Resource Profile entity type");
                                sharingRegistryClient.createEntityType(entityType);

                                log.info("Creating entity type. Id : " + domain.getDomainId()+":"+ResourceType.CREDENTIAL_TOKEN);
                                entityType = new org.apache.airavata.sharing.registry.models.EntityType();
                                entityType.setEntityTypeId(domain.getDomainId()+":"+ResourceType.CREDENTIAL_TOKEN.name());
                                entityType.setDomainId(domain.getDomainId());
                                entityType.setName(ResourceType.CREDENTIAL_TOKEN.name());
                                entityType.setDescription("Credential Store Token entity type");
                                sharingRegistryClient.createEntityType(entityType);

                                //Creating Permission Types for each domain
                                log.info("Creating Permission Type. Id : " + domain.getDomainId()+":READ");
                                PermissionType permissionType = new PermissionType();
                                permissionType.setPermissionTypeId(domain.getDomainId()+":READ");
                                permissionType.setDomainId(domain.getDomainId());
                                permissionType.setName("READ");
                                permissionType.setDescription("Read permission type");
                                try {
                                    sharingRegistryClient.createPermissionType(permissionType);
                                    log.debug("Permission Type created. Id : " + domain.getDomainId() + ":READ");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Permission Id : " + domain.getDomainId()+":READ", ex);
                                }

                                log.info("Creating Permission Type. Id : " + domain.getDomainId()+":WRITE");
                                permissionType = new PermissionType();
                                permissionType.setPermissionTypeId(domain.getDomainId()+":WRITE");
                                permissionType.setDomainId(domain.getDomainId());
                                permissionType.setName("WRITE");
                                permissionType.setDescription("Write permission type");
                                try {
                                    sharingRegistryClient.createPermissionType(permissionType);
                                    log.debug("Permission Type created. Id : " + domain.getDomainId() + ":WRITE");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Permission Id : " + domain.getDomainId() + ":WRITE", ex);
                                }

                                log.info("Creating Permission Type. Id : " + domain.getDomainId()+":MANAGE_SHARING");
                                permissionType = new PermissionType();
                                permissionType.setPermissionTypeId(domain.getDomainId()+":MANAGE_SHARING");
                                permissionType.setDomainId(domain.getDomainId());
                                permissionType.setName("MANAGE_SHARING");
                                permissionType.setDescription("Manage sharing permission type");
                                try {
                                    sharingRegistryClient.createPermissionType(permissionType);
                                    log.debug("Permission Type created. Id : " + domain.getDomainId() + ":MANAGE_SHARING");
                                } catch (DuplicateEntryException ex) {
                                    log.warn("DuplicateEntryException while consuming TENANT create message, ex: " + ex.getMessage() + ", Permission Id : " + domain.getDomainId() + ":MANAGE_SHARING", ex);
                                }

                                break;
                        }


                        break;
                    
                    case PROJECT :
                        log.info("Project specific DB Event communicated by " + dbEventMessage.getPublisherService());

                        Project project = new Project();
                        ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), project);
                        final String domainId = project.getGatewayId();
                        final String entityId = project.getProjectID();

                        switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()){

                            case CREATE:
                            case UPDATE:

                                Entity entity = new Entity();
                                entity.setEntityId(entityId);
                                entity.setDomainId(domainId);
                                entity.setEntityTypeId(domainId + ":" + ResourceType.PROJECT.name());
                                entity.setOwnerId(project.getOwner() + "@" + domainId);
                                entity.setName(project.getName());
                                entity.setDescription(project.getDescription());

                                if (!sharingRegistryClient.isEntityExists(domainId, entityId)) {
                                    log.info("Creating project entity. Entity Id : " + entityId);
                                    sharingRegistryClient.createEntity(entity);
                                    log.info("Project entity created. Entity Id : " + entityId);
                                } else {
                                    log.info("Updating project entity. Entity Id : " + entityId);
                                    sharingRegistryClient.updateEntity(entity);
                                    log.info("Project entity updated. Entity Id : " + entityId);
                                }

                                break;

                            case READ:
                                log.info("Ignoring READ crud operation for entity type PROJECT");
                                break;

                            case DELETE:
                                log.info("Deleting project entity. Entity Id : " + entityId);
                                sharingRegistryClient.deleteEntity(domainId, entityId);
                                log.info("Project entity deleted. Entity Id : " + entityId);

                                break;
                        }
                        break;

                    default: log.error("Handler not defined for " + dBEventMessageContext.getPublisher().getPublisherContext().getEntityType());
                }
            } catch (DuplicateEntryException ex) {
                // log this exception and proceed (do nothing)
                // this exception is thrown mostly when messages are re-consumed in case of some exception, hence ignore
                log.warn("DuplicateEntryException while consuming db-event message, ex: " + ex.getMessage(), ex);
            }

            log.info("Sending ack. Message Delivery Tag : " + messageContext.getDeliveryTag());
            SharingServiceDBEventMessagingFactory.getDBEventSubscriber().sendAck(messageContext.getDeliveryTag());

        } catch (TException e) {
            log.error("Error processing message.", e);
        } catch (ApplicationSettingsException e) {
            log.error("Error fetching application settings.", e);
        } catch (AiravataException e) {
            log.error("Error sending ack. Message Delivery Tag : " + messageContext.getDeliveryTag(), e);
        }
    }
}
