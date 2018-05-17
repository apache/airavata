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
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.sharing.registry.models.Domain;
import org.apache.airavata.sharing.registry.models.Entity;
import org.apache.airavata.sharing.registry.models.EntityType;
import org.apache.airavata.sharing.registry.models.GroupCardinality;
import org.apache.airavata.sharing.registry.models.GroupType;
import org.apache.airavata.sharing.registry.models.PermissionType;
import org.apache.airavata.sharing.registry.models.User;
import org.apache.airavata.sharing.registry.models.UserGroup;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.thrift.TException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AiravataDataMigrator {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, TException, ApplicationSettingsException {
        Connection expCatConnection = ConnectionFactory.getInstance().getExpCatConnection();

        SharingRegistryServerHandler sharingRegistryServerHandler = new SharingRegistryServerHandler();
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

        //Map to reuse the domain ID and owner for creating application-deployment entities
        Map<String, String> domainOwnerMap = new HashMap<>();
        Map<String, GatewayGroups> gatewayGroupsMap = new HashMap<>();

        //Creating the gateway groups
        List<Domain> domainList = sharingRegistryServerHandler.getDomains(0, -1);
        final RegistryService.Client registryServiceClient = getRegistryServiceClient();
        for (Domain domain : domainList) {
            String ownerId = getAdminOwnerUser(domain, sharingRegistryServerHandler, credentialStoreServiceClient, registryServiceClient);
            if (ownerId != null) {
                domainOwnerMap.put(domain.domainId, ownerId);
            } else {
                continue;
            }

            if (registryServiceClient.isGatewayGroupsExists(domain.domainId)) {
                GatewayGroups gatewayGroups = registryServiceClient.getGatewayGroups(domain.domainId);
                gatewayGroupsMap.put(domain.domainId, gatewayGroups);
            } else {

                GatewayGroups gatewayGroups = migrateRolesToGatewayGroups(domain, ownerId, sharingRegistryServerHandler, registryServiceClient);
                gatewayGroupsMap.put(domain.domainId, gatewayGroups);
            }
        }

        //Creating application deployment entries
        for (String domainID : domainOwnerMap.keySet()) {
            GatewayGroups gatewayGroups = gatewayGroupsMap.get(domainID);
            List<ApplicationDeploymentDescription> applicationDeploymentDescriptionList = registryServiceClient.getAllApplicationDeployments(domainID);
            for (ApplicationDeploymentDescription description : applicationDeploymentDescriptionList) {
                Entity entity = new Entity();
                entity.setEntityId(description.getAppDeploymentId());
                entity.setDomainId(domainID);
                entity.setEntityTypeId(entity.domainId + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entity.setOwnerId(domainOwnerMap.get(domainID) + "@" + entity.domainId);
                entity.setName(description.getAppDeploymentId());
                entity.setDescription(description.getAppDeploymentDescription());
                if (entity.getDescription() == null)
                    entity.setDescription(entity.getName());
                else
                    entity.setFullText(entity.getName() + " " + entity.getDescription());

                if (!sharingRegistryServerHandler.isEntityExists(entity.domainId, entity.entityId))
                    sharingRegistryServerHandler.createEntity(entity);
                // Give default Gateway Users group and Read Only Admins group READ access
                sharingRegistryServerHandler.shareEntityWithGroups(entity.domainId, entity.entityId,
                        Arrays.asList(gatewayGroups.getDefaultGatewayUsersGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                        entity.domainId + ":" + ResourcePermissionType.READ, true);
                // Give Admins group WRITE access
                sharingRegistryServerHandler.shareEntityWithGroups(entity.domainId, entity.entityId,
                        Arrays.asList(gatewayGroups.getAdminsGroupId()),
                        entity.domainId + ":" + ResourcePermissionType.WRITE, true);
            }
        }

        expCatConnection.close();
        System.out.println("Completed!");

    }

    private static GatewayGroups migrateRolesToGatewayGroups(Domain domain, String ownerId, SharingRegistryServerHandler sharingRegistryServerHandler, RegistryService.Client registryServiceClient) throws TException, ApplicationSettingsException {
        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(domain.domainId);

        // Migrate roles to groups
        List<String> usernames = sharingRegistryServerHandler.getUsers(domain.domainId, 0, -1)
                .stream()
                // Filter out bad ids that don't have an "@" in them
                .filter(user -> user.getUserId().lastIndexOf("@") > 0)
                .map(user -> user.getUserId().substring(0, user.getUserId().lastIndexOf("@")))
                .collect(Collectors.toList());
        Map<String, List<String>> roleMap = loadRolesForUsers(domain.domainId, usernames);

        if (roleMap.containsKey("gateway-user")) {
            UserGroup gatewayUsersGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                    "Gateway Users",
                    "Default group for users of the gateway.", roleMap.get("gateway-user"));
            gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.groupId);
        }
        if (roleMap.containsKey("admin")) {
            UserGroup adminUsersGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                    "Admin Users",
                    "Admin users group.", roleMap.get("admin"));
            gatewayGroups.setAdminsGroupId(adminUsersGroup.groupId);
        }
        if (roleMap.containsKey("admin-read-only")) {
            UserGroup readOnlyAdminsGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                    "Read Only Admin Users",
                    "Group of admin users with read-only access.", roleMap.get("admin-read-only"));
            gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.groupId);
        }
        registryServiceClient.createGatewayGroups(gatewayGroups);
        return gatewayGroups;
    }

    private static String getAdminOwnerUser(Domain domain, SharingRegistryServerHandler sharingRegistryServerHandler, CredentialStoreService.Client credentialStoreServiceClient, RegistryService.Client registryServiceClient) throws TException {
        GatewayResourceProfile gatewayResourceProfile = null;
        try {
            gatewayResourceProfile = registryServiceClient.getGatewayResourceProfile(domain.domainId);
        } catch (Exception e) {
            System.out.println("Skipping creating group based auth migration for " + domain.domainId + " because it doesn't have a GatewayResourceProfile");
            return null;
        }
        if (gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {
            System.out.println("Skipping creating group based auth migration for " + domain.domainId + " because it doesn't have an identity server pwd credential token");
            return null;
        }
        String groupOwner = null;
        try {
            PasswordCredential credential = credentialStoreServiceClient.getPasswordCredential(
                    gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
            groupOwner = credential.getLoginUserName();
        } catch (Exception e) {
            System.out.println("Skipping creating group based auth migration for " + domain.domainId + " because the identity server pwd credential could not be retrieved.");
            return null;
        }

        String ownerId = groupOwner + "@" + domain.domainId;
        if (!sharingRegistryServerHandler.isUserExists(domain.domainId, ownerId)) {
            System.out.println("Skipping creating group based auth migration for " + domain.domainId + " because admin user doesn't exist in sharing registry.");
            return null;
        }
        return ownerId;
    }

    private static Map<String,List<String>> loadRolesForUsers(String gatewayId, List<String> usernames) throws TException, ApplicationSettingsException {

        TenantManagementKeycloakImpl tenantManagementKeycloak = new TenantManagementKeycloakImpl();
        PasswordCredential tenantAdminPasswordCredential = getTenantAdminPasswordCredential(gatewayId);
        Map<String, List<String>> roleMap = new HashMap<>();
        for (String username : usernames) {
            try {
                List<String> roles = tenantManagementKeycloak.getUserRoles(tenantAdminPasswordCredential, gatewayId, username);
                if (roles != null) {
                    for (String role : roles) {
                        if (!roleMap.containsKey(role)) {
                            roleMap.put(role, new ArrayList<>());
                        }
                        roleMap.get(role).add(username);
                    }
                } else {
                    System.err.println("Warning: user [" + username + "] in tenant [" + gatewayId + "] has no roles.");
                }
            } catch (IamAdminServicesException e) {
                System.err.println("Error: unable to load roles for user [" + username + "] in tenant [" + gatewayId + "].");
                e.printStackTrace();
            }
        }
        return roleMap;
    }

    private static PasswordCredential getTenantAdminPasswordCredential(String tenantId) throws TException, ApplicationSettingsException {

        GatewayResourceProfile gwrp = getRegistryServiceClient().getGatewayResourceProfile(tenantId);

        CredentialStoreService.Client csClient = getCredentialStoreServiceClient();
        return csClient.getPasswordCredential(gwrp.getIdentityServerPwdCredToken(), gwrp.getGatewayID());
    }

    private static UserGroup createGroup(SharingRegistryServerHandler sharingRegistryServerHandler, Domain domain, String ownerId, String groupName, String groupDescription, List<String> usernames) throws TException {

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(AiravataUtils.getId(groupName));
        userGroup.setDomainId(domain.domainId);
        userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup.setCreatedTime(System.currentTimeMillis());
        userGroup.setUpdatedTime(System.currentTimeMillis());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
        sharingRegistryServerHandler.createGroup(userGroup);

        List<String> userIds = usernames.stream()
                .map(username -> username + "@" + domain.domainId)
                .collect(Collectors.toList());

        sharingRegistryServerHandler.addUsersToGroup(domain.domainId, userIds, userGroup.getGroupId());
        return userGroup;
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