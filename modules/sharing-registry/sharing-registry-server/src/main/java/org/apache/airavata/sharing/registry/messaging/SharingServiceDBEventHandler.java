/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import org.apache.airavata.common.utils.CustosUtils;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventMessageContext;
import org.apache.airavata.model.error.DuplicateEntryException;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.custos.sharing.management.client.SharingManagementClient;
import org.apache.custos.sharing.service.Entity;
import org.apache.custos.sharing.service.EntityType;
import org.apache.custos.sharing.service.PermissionType;
import org.apache.custos.sharing.service.Status;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Ajinkya on 3/28/17.
 */
public class SharingServiceDBEventHandler implements MessageHandler {

    private final static Logger log = LoggerFactory.getLogger(SharingServiceDBEventHandler.class);

    private final SharingManagementClient sharingManagementClient;

    SharingServiceDBEventHandler() throws ApplicationSettingsException, SharingRegistryException, IOException {
        log.info("Connecting to Custos Sharing Service.........");
        sharingManagementClient = CustosUtils.getCustosClientProvider().getSharingManagementClient();
    }

    @Override
    public void onMessage(MessageContext messageContext) {

        log.info("New DB Event message to sharing service.");

        try {

            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());

            DBEventMessage dbEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(bytes, dbEventMessage);

            log.info("DB Event message to sharing service from " + dbEventMessage.getPublisherService());

            DBEventMessageContext dBEventMessageContext = dbEventMessage.getMessageContext();
            try {
                switch (dBEventMessageContext.getPublisher().getPublisherContext().getEntityType()) {

                    case TENANT:

                        log.info("Tenant specific DB Event communicated by " + dbEventMessage.getPublisherService());

                        Gateway gateway = new Gateway();
                        ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), gateway);

                        String custosId = gateway.getOauthClientId();

                        switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()) {

                            case CREATE:
                            case UPDATE:

                                /*
                                Following set of DB operations should happen in a transaction
                                As these are thrift calls we cannot enforce this restriction
                                If something goes wrong, message would get queued again and try to create
                                 DB entities which are already present. We catch DuplicateEntryException and
                                 log as a warning to handle such scenarios and move ahead.
                                 */

                                if (custosId != null && ! custosId.trim().equals("")) {
                                    //Creating Entity Types for each domain
                                    log.info("Creating entity type. Id PROJECT");
                                    if (!isEntityTypeExists(custosId, "PROJECT")) {
                                        EntityType projectEntityType = EntityType.newBuilder()
                                                .setName("PROJECT")
                                                .setDescription("Project entity type")
                                                .setId("PROJECT")
                                                .build();
                                        sharingManagementClient.createEntityType(custosId, projectEntityType);
                                    }


                                    log.info("Creating entity type. Id EXPERIMENT");
                                    if (!isEntityTypeExists(custosId, "EXPERIMENT")) {
                                        EntityType experimentEntityType = EntityType.newBuilder()
                                                .setName("EXPERIMENT")
                                                .setDescription("Experiment entity type")
                                                .setId("EXPERIMENT")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, experimentEntityType);
                                    }

                                    log.info("Creating entity type. Id FILE");
                                    if (!isEntityTypeExists(custosId, "FILE")) {
                                        EntityType fileEntityType = EntityType.newBuilder()
                                                .setName("FILE")
                                                .setDescription("File entity type")
                                                .setId("FILE")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, fileEntityType);

                                    }


                                    log.info("Creating entity type. Id " + ResourceType.APPLICATION_DEPLOYMENT.name());
                                    if (!isEntityTypeExists(custosId, ResourceType.APPLICATION_DEPLOYMENT.name())) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName(ResourceType.APPLICATION_DEPLOYMENT.name())
                                                .setDescription("Application Deployment entity type")
                                                .setId(ResourceType.APPLICATION_DEPLOYMENT.name())
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }

                                    log.info("Creating entity type. Id " + ResourceType.GROUP_RESOURCE_PROFILE.name());

                                    if (!isEntityTypeExists(custosId, ResourceType.GROUP_RESOURCE_PROFILE.name())) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName(ResourceType.GROUP_RESOURCE_PROFILE.name())
                                                .setDescription("Group Resource Profile entity type")
                                                .setId(ResourceType.GROUP_RESOURCE_PROFILE.name())
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }

                                    log.info("Creating entity type. Id " + ResourceType.CREDENTIAL_TOKEN.name());
                                    if (!isEntityTypeExists(custosId, ResourceType.CREDENTIAL_TOKEN.name())) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName(ResourceType.CREDENTIAL_TOKEN.name())
                                                .setDescription("Credential Store Token entity type")
                                                .setId(ResourceType.CREDENTIAL_TOKEN.name())
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }

                                    //Creating Permission Types for each domain
                                    log.info("Creating Permission Type. Id  READ");
                                    if (!isPermissionTypeExists(custosId, "READ")) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName("READ")
                                                .setDescription("Read permission type")
                                                .setId("READ")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }


                                    log.info("Creating Permission Type. Id : WRITE");

                                    if (!isPermissionTypeExists(custosId, "WRITE")) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName("WRITE")
                                                .setDescription("Write permission type")
                                                .setId("WRITE")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }

                                    log.info("Creating Permission Type. Id : OWNER");

                                    if (!isPermissionTypeExists(custosId, "OWNER")) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName("OWNER")
                                                .setDescription("Owner permission type")
                                                .setId("OWNER")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }

                                    log.info("Creating Permission Type. Id : MANAGE_SHARING");

                                    if (!isPermissionTypeExists(custosId, "MANAGE_SHARING")) {
                                        EntityType applicationEntityType = EntityType.newBuilder()
                                                .setName("MANAGE_SHARING")
                                                .setDescription("Manage sharing permission type")
                                                .setId("MANAGE_SHARING")
                                                .build();

                                        sharingManagementClient.createEntityType(custosId, applicationEntityType);
                                    }
                                }

                                break;
                        }


                        break;

                    case PROJECT:
                        log.info("Project specific DB Event communicated by " + dbEventMessage.getPublisherService());

                        Project project = new Project();
                        ThriftUtils.createThriftFromBytes(dBEventMessageContext.getPublisher().getPublisherContext().getEntityDataModel(), project);
                        final String custos = project.getGatewayId();
                        final String entityId = project.getProjectID();

                        log.info("Custos "+ custos);
                        log.info("Entity id "+ entityId);

                        Entity entity = Entity.newBuilder()
                                .setId(entityId)
                                .setDescription(project.getDescription())
                                .setName(project.getName())
                                .setOwnerId(project.getOwner())
                                .setType(ResourceType.PROJECT.name())
                                .build();

                        switch (dBEventMessageContext.getPublisher().getPublisherContext().getCrudType()) {

                            case CREATE:
                            case UPDATE:
                                log.info("Checking existence ########");
                                Status status = sharingManagementClient.isEntityExists(custos, entity);
                                log.info("Status ############" + status.getStatus());
                                if (!status.getStatus()) {
                                    log.info("Creating project entity. Entity Id : " + entityId);
                                    sharingManagementClient.createEntity(custos, entity);
                                    log.info("Project entity created. Entity Id : " + entityId);
                                } else {
                                    log.info("Updating project entity. Entity Id : " + entityId);
                                    sharingManagementClient.updateEntity(custos, entity);
                                    log.info("Project entity updated. Entity Id : " + entityId);
                                }

                                break;

                            case READ:
                                log.info("Ignoring READ crud operation for entity type PROJECT");
                                break;

                            case DELETE:
                                log.info("Deleting project entity. Entity Id : " + entityId);
                                sharingManagementClient.deleteEntity(custos, entity);
                                log.info("Project entity deleted. Entity Id : " + entityId);

                                break;
                        }
                        break;

                    default:
                        log.error("Handler not defined for " + dBEventMessageContext.getPublisher().getPublisherContext().getEntityType());
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
        } catch (IOException e) {
            log.error("Error sending ack. Message Delivery Tag : " + messageContext.getDeliveryTag(), e);
        }
    }


    private boolean isEntityTypeExists(String custosId, String entityType) {
        EntityType type = EntityType.newBuilder().setId(entityType).build();
        EntityType request = sharingManagementClient.getEntityType(custosId, type);
        if (request != null && request.getId() != null && request.getId().trim().equals(entityType)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPermissionTypeExists(String custosId, String entityType) {
        PermissionType type = PermissionType.newBuilder().setId(entityType).build();
        PermissionType request = sharingManagementClient.getPermissionType(custosId, type);
        if (request != null && request.getId() != null && request.getId().trim().equals(entityType)) {
            return true;
        } else {
            return false;
        }
    }
}
