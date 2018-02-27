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
package org.apache.airavata.sharing.registry.migrator.airavata;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.sharing.registry.client.SharingRegistryServiceClientFactory;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.thrift.TException;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.api.RegistryService;

import java.util.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AiravataDataMigrator {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, TException, ApplicationSettingsException {
        Connection expCatConnection = ConnectionFactory.getInstance().getExpCatConnection();

        SharingRegistryServerHandler sharingRegistryServerHandler = new SharingRegistryServerHandler();
        SharingRegistryService.Client sharingClient = SharingRegistryServiceClientFactory.createSharingRegistryClient("149.165.169.138", 7878);
        CredentialStoreService.Client credentialStoreServiceClient = getCredentialStoreServiceClient();

        String query = "SELECT * FROM GATEWAY";
        Statement statement = expCatConnection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            try{
                //Creating domain entries
                Domain domain = new Domain();
                domain.setDomainId(rs.getString("GATEWAY_ID"));
                domain.setName(rs.getString("GATEWAY_ID"));
                domain.setDescription("Domain entry for " + domain.name);

                if (!sharingRegistryServerHandler.isDomainExists(domain.domainId))
                    sharingRegistryServerHandler.createDomain(domain);

                //Creating Entity Types for each domain
                EntityType entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId+":PROJECT");
                entityType.setDomainId(domain.domainId);
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.domainId, entityType.entityTypeId))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId+":EXPERIMENT");
                entityType.setDomainId(domain.domainId);
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.domainId, entityType.entityTypeId))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId+":FILE");
                entityType.setDomainId(domain.domainId);
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.domainId, entityType.entityTypeId))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId+":"+ ResourceType.APPLICATION_DEPLOYMENT.name());
                entityType.setDomainId(domain.domainId);
                entityType.setName("APPLICATION-DEPLOYMENT");
                entityType.setDescription("Application Deployment entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.domainId, entityType.entityTypeId))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.domainId+":"+ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDomainId(domain.domainId);
                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDescription("Group Resource Profile entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.domainId, entityType.entityTypeId))
                    sharingRegistryServerHandler.createEntityType(entityType);

                //Creating Permission Types for each domain
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.domainId+":READ");
                permissionType.setDomainId(domain.domainId);
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.domainId, permissionType.permissionTypeId))
                    sharingRegistryServerHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.domainId+":WRITE");
                permissionType.setDomainId(domain.domainId);
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.domainId, permissionType.permissionTypeId))
                    sharingRegistryServerHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.domainId+":EXEC");
                permissionType.setDomainId(domain.domainId);
                permissionType.setName("EXEC");
                permissionType.setDescription("Execute permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.domainId, permissionType.permissionTypeId))
                    sharingRegistryServerHandler.createPermissionType(permissionType);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        //Creating user entries
        query = "SELECT * FROM USERS";
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        while(rs.next()){
            try{
                User user = new User();
                user.setUserId(rs.getString("AIRAVATA_INTERNAL_USER_ID"));
                user.setDomainId(rs.getString("GATEWAY_ID"));
                user.setUserName(rs.getString("USER_NAME"));

                if (!sharingRegistryServerHandler.isUserExists(user.domainId, user.userId))
                    sharingRegistryServerHandler.createUser(user);
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        //Creating project entries
        query = "SELECT * FROM PROJECT";
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        while(rs.next()){
            try{
                Entity entity = new Entity();
                entity.setEntityId(rs.getString("PROJECT_ID"));
                entity.setDomainId(rs.getString("GATEWAY_ID"));
                entity.setEntityTypeId(rs.getString("GATEWAY_ID") + ":PROJECT");
                entity.setOwnerId(rs.getString("USER_NAME") + "@" + rs.getString("GATEWAY_ID"));
                entity.setName(rs.getString("PROJECT_NAME"));
                entity.setDescription(rs.getString("DESCRIPTION"));
                if(entity.getDescription() == null)
                    entity.setFullText(entity.getName());
                else
                    entity.setFullText(entity.getName() + " " + entity.getDescription());
                Map<String, String> metadata = new HashMap<>();
                metadata.put("CREATION_TIME", rs.getDate("CREATION_TIME").toString());

                if (!sharingRegistryServerHandler.isEntityExists(entity.domainId, entity.entityId))
                    sharingRegistryServerHandler.createEntity(entity);
            }catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        //Creating experiment entries
        query = "SELECT * FROM EXPERIMENT";
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        while(rs.next()){
            try {
                Entity entity = new Entity();
                entity.setEntityId(rs.getString("EXPERIMENT_ID"));
                entity.setDomainId(rs.getString("GATEWAY_ID"));
                entity.setEntityTypeId(rs.getString("GATEWAY_ID") + ":EXPERIMENT");
                entity.setOwnerId(rs.getString("USER_NAME") + "@" + rs.getString("GATEWAY_ID"));
                entity.setParentEntityId(rs.getString("PROJECT_ID"));
                entity.setName(rs.getString("EXPERIMENT_NAME"));
                entity.setDescription(rs.getString("DESCRIPTION"));
                if(entity.getDescription() == null)
                    entity.setFullText(entity.getName());
                else
                    entity.setFullText(entity.getName() + " " + entity.getDescription());
                Map<String, String> metadata = new HashMap<>();
                metadata.put("CREATION_TIME", rs.getDate("CREATION_TIME").toString());
                metadata.put("EXPERIMENT_TYPE", rs.getString("EXPERIMENT_TYPE"));
                metadata.put("EXECUTION_ID", rs.getString("EXECUTION_ID"));
                metadata.put("GATEWAY_EXECUTION_ID", rs.getString("GATEWAY_EXECUTION_ID"));
                metadata.put("ENABLE_EMAIL_NOTIFICATION", rs.getString("ENABLE_EMAIL_NOTIFICATION"));
                metadata.put("EMAIL_ADDRESSES", rs.getString("EMAIL_ADDRESSES"));
                metadata.put("GATEWAY_INSTANCE_ID", rs.getString("GATEWAY_INSTANCE_ID"));
                metadata.put("ARCHIVE", rs.getString("ARCHIVE"));

                if (!sharingRegistryServerHandler.isEntityExists(entity.domainId, entity.entityId))
                    sharingRegistryServerHandler.createEntity(entity);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        //Creating the everyone group
        List<Domain> domainList = sharingClient.getDomains(-1, 0);
        for (Domain domain : domainList) {
            GatewayResourceProfile gatewayResourceProfile = getRegistryServiceClient().getGatewayResourceProfile(domain.domainId);
            PasswordCredential credential = credentialStoreServiceClient.getPasswordCredential(
                    gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
            String groupOwner = credential.getLoginUserName();

            String groupId = "everyone@" + domain.domainId;
            if (!sharingClient.isGroupExists(domain.domainId, groupId)) {
                UserGroup userGroup = new UserGroup();
                userGroup.setGroupId(groupId);
                userGroup.setDomainId(domain.domainId);
                userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
                userGroup.setCreatedTime(System.currentTimeMillis());
                userGroup.setUpdatedTime(System.currentTimeMillis());
                userGroup.setName("everyone");
                userGroup.setDescription("Default Group");
                userGroup.setOwnerId(groupOwner + "@" + domain.domainId);
                userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
                sharingClient.createGroup(userGroup);

                List<User> userList = sharingClient.getUsers(domain.domainId, -1, 0);
                List<String> users = new ArrayList<>();
                for (User user : userList) {
                    users.add(user.getUserId());
                }
                sharingClient.addUsersToGroup(domain.domainId, users, groupId);
            }
        }

        //Creating application deployment entries
        query = "SELECT * FROM APPLICATION_DEPLOYMENT";
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        while(rs.next()){
            try {
                GatewayResourceProfile gatewayResourceProfile = getRegistryServiceClient().
                        getGatewayResourceProfile(rs.getString("GATEWAY_ID"));
                PasswordCredential credential = credentialStoreServiceClient.getPasswordCredential(
                        gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
                String applicationDeploymentOwner = credential.getLoginUserName();

                Entity entity = new Entity();
                entity.setEntityId(rs.getString("DEPLOYMENT_ID"));
                entity.setDomainId(rs.getString("GATEWAY_ID"));
                entity.setEntityTypeId(rs.getString("GATEWAY_ID") + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entity.setOwnerId(applicationDeploymentOwner + "@" + rs.getString("GATEWAY_ID"));
                entity.setName(rs.getString("DEPLOYMENT_ID"));
                entity.setDescription(rs.getString("APPLICATION_DESC"));
                if(entity.getDescription() == null)
                    entity.setFullText(entity.getName());
                else
                    entity.setFullText(entity.getName() + " " + entity.getDescription());
                Map<String, String> metadata = new HashMap<>();
                metadata.put("CREATION_TIME", rs.getDate("CREATION_TIME").toString());

                if (!sharingRegistryServerHandler.isEntityExists(entity.domainId, entity.entityId))
                    sharingRegistryServerHandler.createEntity(entity);
                String groupId = "everyone@" + entity.domainId;
                sharingClient.shareEntityWithGroups(entity.domainId, entity.entityId, Arrays.asList(groupId), entity.domainId+":"+ ResourcePermissionType.EXEC, true);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        expCatConnection.close();

    }

    private static CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }

    private static RegistryService.Client getRegistryServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
        final String serverHost = ServerSettings.getRegistryServerHost();
        try {
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException e) {
            throw new TException("Unable to create registry client...", e);
        }
    }

}