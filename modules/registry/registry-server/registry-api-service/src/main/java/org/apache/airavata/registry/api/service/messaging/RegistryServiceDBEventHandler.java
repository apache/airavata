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
package org.apache.airavata.registry.api.service.messaging;

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBEventService;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.utils.ThriftClientPool;
import org.apache.airavata.common.utils.ThriftUtils;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.MessageHandler;
import org.apache.airavata.messaging.core.util.DBEventPublisherUtils;
import org.apache.airavata.model.dbevent.CrudType;
import org.apache.airavata.model.dbevent.DBEventMessage;
import org.apache.airavata.model.dbevent.DBEventPublisherContext;
import org.apache.airavata.model.dbevent.EntityType;
import org.apache.airavata.model.error.DuplicateEntryException;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by goshenoy on 3/30/17.
 */
public class RegistryServiceDBEventHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceDBEventHandler.class);
    private final ThriftClientPool<RegistryService.Client> registryClientPool;
    private DBEventPublisherUtils dbEventPublisherUtils = new DBEventPublisherUtils(DBEventService.REGISTRY);

    public RegistryServiceDBEventHandler() throws ApplicationSettingsException, RegistryServiceException {

        GenericObjectPoolConfig<RegistryService.Client> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(5);
        poolConfig.setMinIdle(1);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        // must set timeBetweenEvictionRunsMillis since eviction doesn't run unless that is positive
        poolConfig.setTimeBetweenEvictionRunsMillis(5L * 60L * 1000L);
        poolConfig.setNumTestsPerEvictionRun(10);
        poolConfig.setMaxWaitMillis(3000);

        registryClientPool = new ThriftClientPool<>(
                tProtocol -> new RegistryService.Client(tProtocol), poolConfig, ServerSettings.getRegistryServerHost(),
                Integer.parseInt(ServerSettings.getRegistryServerPort()));
    }

    @Override
    public void onMessage(MessageContext messageContext) {
        logger.info("RegistryServiceDBEventHandler | Received a new message!");

        try {
            // construct dbeventmessage thrift datamodel
            byte[] bytes = ThriftUtils.serializeThriftObject(messageContext.getEvent());
            DBEventMessage dbEventMessage = new DBEventMessage();
            ThriftUtils.createThriftFromBytes(bytes, dbEventMessage);
            logger.info("RegistryService received db-event-message from publisher: " + dbEventMessage.getPublisherService());

            // get publisher context
            DBEventPublisherContext publisherContext = dbEventMessage.getMessageContext().getPublisher().getPublisherContext();
            logger.info("RegistryService, Replicated Entity: " + publisherContext.getEntityType());

            RegistryService.Client registryClient = registryClientPool.getResource();
            // this try-block is mainly for catching DuplicateEntryException
            try {
                // check type of entity-type
                switch (publisherContext.getEntityType()) {
                    // Gateway related operations
                    case TENANT: {
                        // construct gateway datamodel from message
                        Gateway gateway = new Gateway();
                        ThriftUtils.createThriftFromBytes(publisherContext.getEntityDataModel(), gateway);

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
                        }
                        // break entity: gateway
                        break;
                    }

                    // UserProfile related operations
                    case USER_PROFILE: {
                        // construct userprofile datamodel from message
                        UserProfile userProfile = new UserProfile();
                        ThriftUtils.createThriftFromBytes(publisherContext.getEntityDataModel(), userProfile);

                        // call service-methods based on CRUD type
                        switch (publisherContext.getCrudType()) {
                            case CREATE: {
                                logger.info("Replicating addUser in Registry.");
                                if (!registryClient.isUserExists(userProfile.getGatewayId(), userProfile.getUserId())) {
                                    registryClient.addUser(userProfile);
                                }
                                Project defaultProject = createDefaultProject(registryClient, userProfile);
                                if (defaultProject != null) {

                                    // Publish new PROJECT event (sharing service will listen for it and register this as a shared Entity)
                                    dbEventPublisherUtils.publish(EntityType.PROJECT, CrudType.CREATE, defaultProject);
                                }
                                logger.info("addUser Replication Success!");
                                break;
                            }
                            case UPDATE: {
                                logger.info("Replicating updateGateway in Registry.");
                                //TODO: find appropriate method
                                break;
                            }
                            case DELETE: {
                                logger.info("Replicating deleteGateway in Registry.");
                                //TODO: find appropriate method
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
                registryClientPool.returnResource(registryClient);
            } catch (DuplicateEntryException ex) {
                // log this exception and proceed (do nothing)
                // this exception is thrown mostly when messages are re-consumed, hence ignore
                logger.warn("DuplicateEntryException while consuming db-event message, ex: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                registryClientPool.returnBrokenResource(registryClient);
                throw ex;
            }
            // send ack for received message
            logger.info("RegistryServiceDBEventHandler | Sending ack. Message Delivery Tag: " + messageContext.getDeliveryTag());
            RegistryServiceDBEventMessagingFactory.getDBEventSubscriber().sendAck(messageContext.getDeliveryTag());
        } catch (TException ex) {
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

    private Project createDefaultProject(RegistryService.Client registryClient, UserProfile userProfile) throws TException {
        // Just retrieve the first project to see if the user has any projects
        List<Project> projects = registryClient.getUserProjects(userProfile.getGatewayId(), userProfile.getUserId(), 1, 0);
        if (projects.isEmpty()) {
            Project defaultProject = new Project();
            defaultProject.setOwner(userProfile.getUserId());
            defaultProject.setName("Default Project");
            defaultProject.setGatewayId(userProfile.getGatewayId());
            defaultProject.setDescription("This is the default project for user " + userProfile.getUserId());
            String defaultProjectId = registryClient.createProject(userProfile.getGatewayId(), defaultProject);
            logger.info("Default project created for user {}", userProfile.getUserId());
            defaultProject.setProjectID(defaultProjectId);
            return defaultProject;
        }
        return null;
    }
}
