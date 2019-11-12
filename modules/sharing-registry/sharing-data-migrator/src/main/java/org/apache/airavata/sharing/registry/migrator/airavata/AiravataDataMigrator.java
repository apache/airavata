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
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.credential.store.CredentialSummary;
import org.apache.airavata.model.credential.store.PasswordCredential;
import org.apache.airavata.model.credential.store.SummaryType;
import org.apache.airavata.model.group.ResourcePermissionType;
import org.apache.airavata.model.group.ResourceType;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.user.Status;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.security.AiravataSecurityException;
import org.apache.airavata.service.profile.client.ProfileServiceClientFactory;
import org.apache.airavata.service.profile.iam.admin.services.core.impl.TenantManagementKeycloakImpl;
import org.apache.airavata.service.profile.iam.admin.services.cpi.IamAdminServices;
import org.apache.airavata.service.profile.iam.admin.services.cpi.exception.IamAdminServicesException;
import org.apache.airavata.service.security.AiravataSecurityManager;
import org.apache.airavata.service.security.SecurityManagerFactory;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.apache.airavata.sharing.registry.utils.ThriftDataModelConversion;
import org.apache.thrift.TException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class AiravataDataMigrator {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, TException, ApplicationSettingsException {
        String gatewayId = null;
        if (args.length > 0) {
            gatewayId = args[0];
        }
        String gatewayWhereClause = "";
        if (gatewayId != null) {
            System.out.println("Running sharing data migration for " + gatewayId);
            gatewayWhereClause = " WHERE GATEWAY_ID = '" + gatewayId + "'";
        } else {
            System.out.println("Running sharing data migration for all gateways");
        }

        Connection expCatConnection = ConnectionFactory.getInstance().getExpCatConnection();

        SharingRegistryServerHandler sharingRegistryServerHandler = new SharingRegistryServerHandler();
        CredentialStoreService.Client credentialStoreServiceClient = getCredentialStoreServiceClient();
        IamAdminServices.Client iamAdminServiceClient = getIamAdminServiceClient();

        String query = "SELECT * FROM GATEWAY" + gatewayWhereClause;
        Statement statement = expCatConnection.createStatement();
        ResultSet rs = statement.executeQuery(query);

        while (rs.next()) {
            try{
                //Creating domain entries
                Domain domain = new Domain();
                domain.setDomainId(rs.getString("GATEWAY_ID"));
                domain.setName(rs.getString("GATEWAY_ID"));
                domain.setDescription("Domain entry for " + domain.getName());

                if (!sharingRegistryServerHandler.isDomainExists(domain.getDomainId()))
                    sharingRegistryServerHandler.createDomain(domain);

                //Creating Entity Types for each domain
                EntityType entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":PROJECT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("PROJECT");
                entityType.setDescription("Project entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":EXPERIMENT");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("EXPERIMENT");
                entityType.setDescription("Experiment entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":FILE");
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("FILE");
                entityType.setDescription("File entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":"+ ResourceType.APPLICATION_DEPLOYMENT.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName("APPLICATION-DEPLOYMENT");
                entityType.setDescription("Application Deployment entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":"+ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.GROUP_RESOURCE_PROFILE.name());
                entityType.setDescription("Group Resource Profile entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                entityType = new EntityType();
                entityType.setEntityTypeId(domain.getDomainId()+":"+ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDomainId(domain.getDomainId());
                entityType.setName(ResourceType.CREDENTIAL_TOKEN.name());
                entityType.setDescription("Credential Store Token entity type");
                if (!sharingRegistryServerHandler.isEntityTypeExists(entityType.getDomainId(), entityType.getEntityTypeId()))
                    sharingRegistryServerHandler.createEntityType(entityType);

                //Creating Permission Types for each domain
                PermissionType permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId()+":READ");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("READ");
                permissionType.setDescription("Read permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.getDomainId(), permissionType.getPermissionTypeId()))
                    sharingRegistryServerHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId()+":WRITE");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("WRITE");
                permissionType.setDescription("Write permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.getDomainId(), permissionType.getPermissionTypeId()))
                    sharingRegistryServerHandler.createPermissionType(permissionType);

                permissionType = new PermissionType();
                permissionType.setPermissionTypeId(domain.getDomainId()+":MANAGE_SHARING");
                permissionType.setDomainId(domain.getDomainId());
                permissionType.setName("MANAGE_SHARING");
                permissionType.setDescription("Sharing permission type");
                if (!sharingRegistryServerHandler.isPermissionExists(permissionType.getDomainId(), permissionType.getPermissionTypeId()))
                    sharingRegistryServerHandler.createPermissionType(permissionType);

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        //Creating user entries
        query = "SELECT * FROM USERS" + gatewayWhereClause;
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        while(rs.next()){
            try{
                User user = new User();
                user.setUserId(rs.getString("AIRAVATA_INTERNAL_USER_ID"));
                user.setDomainId(rs.getString("GATEWAY_ID"));
                user.setUserName(rs.getString("USER_NAME"));

                if (!sharingRegistryServerHandler.isUserExists(user.getDomainId(), user.getUserId()))
                    sharingRegistryServerHandler.createUser(user);
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
            // If we're only running migration for gatewayId, then skip other gateways
            if (gatewayId != null && !gatewayId.equals(domain.getDomainId())) {
                continue;
            }
            String ownerId = getAdminOwnerUser(domain, sharingRegistryServerHandler, credentialStoreServiceClient, registryServiceClient);
            if (ownerId != null) {
                domainOwnerMap.put(domain.getDomainId(), ownerId);
            } else {
                continue;
            }
            if (registryServiceClient.isGatewayGroupsExists(domain.getDomainId())) {
                GatewayGroups gatewayGroups = registryServiceClient.getGatewayGroups(domain.getDomainId());
                gatewayGroupsMap.put(domain.getDomainId(), gatewayGroups);
            } else {
                GatewayGroups gatewayGroups = migrateRolesToGatewayGroups(domain, ownerId, sharingRegistryServerHandler, registryServiceClient);
                gatewayGroupsMap.put(domain.getDomainId(), gatewayGroups);
            }
            //find all the active users in keycloak that do not exist in sharing registry service and migrate them to the database
            AuthzToken authzToken_of_management_user = getManagementUsersAccessToken(domain.getDomainId());
            List<UserProfile> missingUsers = getUsersToMigrate(sharingRegistryServerHandler, iamAdminServiceClient, authzToken_of_management_user, null, domain.getDomainId());
            migrateKeycloakUsersToGateway(iamAdminServiceClient, authzToken_of_management_user, missingUsers);
            addUsersToGroups(sharingRegistryServerHandler, missingUsers, gatewayGroupsMap.get(domain.getDomainId()), domain.getDomainId());
        }
        //Creating project entries
        query = "SELECT * FROM PROJECT" + gatewayWhereClause;
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        List<Entity> projectEntities = new ArrayList<>();
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
//                Map<String, String> metadata = new HashMap<>();
//                metadata.put("CREATION_TIME", rs.getDate("CREATION_TIME").toString());
                projectEntities.add(entity);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //Creating experiment entries
        query = "SELECT * FROM EXPERIMENT" + gatewayWhereClause;
        statement = expCatConnection.createStatement();
        rs = statement.executeQuery(query);
        List<Entity> experimentEntities = new ArrayList<>();
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
//                Map<String, String> metadata = new HashMap<>();
//                metadata.put("CREATION_TIME", rs.getDate("CREATION_TIME").toString());
//                metadata.put("EXPERIMENT_TYPE", rs.getString("EXPERIMENT_TYPE"));
//                metadata.put("EXECUTION_ID", rs.getString("EXECUTION_ID"));
//                metadata.put("GATEWAY_EXECUTION_ID", rs.getString("GATEWAY_EXECUTION_ID"));
//                metadata.put("ENABLE_EMAIL_NOTIFICATION", rs.getString("ENABLE_EMAIL_NOTIFICATION"));
//                metadata.put("EMAIL_ADDRESSES", rs.getString("EMAIL_ADDRESSES"));
//                metadata.put("GATEWAY_INSTANCE_ID", rs.getString("GATEWAY_INSTANCE_ID"));
//                metadata.put("ARCHIVE", rs.getString("ARCHIVE"));
                experimentEntities.add(entity);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        for (Entity entity : projectEntities) {
            if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId())) {
                sharingRegistryServerHandler.createEntity(entity);
            }
        }

        for (Entity entity : experimentEntities) {
            if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId())) {
                if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getParentEntityId())) {
                    System.out.println("Warning: project entity does exist for experiment entity " + entity.getEntityId() + " in gateway " + entity.getDomainId());
                    continue;
                } else {
                    sharingRegistryServerHandler.createEntity(entity);
                }
            }
            if (gatewayGroupsMap.containsKey(entity.getDomainId())) {
                shareEntityWithAdminGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroupsMap.get(entity.getDomainId()), false);
            } else {
                System.out.println("Warning: no Admin gateway groups to share experiment entity " + entity.getEntityId() + " in gateway " + entity.getDomainId());
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
                entity.setEntityTypeId(entity.getDomainId() + ":" + ResourceType.APPLICATION_DEPLOYMENT.name());
                entity.setOwnerId(domainOwnerMap.get(domainID));
                entity.setName(description.getAppDeploymentId());
                entity.setDescription(description.getAppDeploymentDescription());
                if (entity.getDescription() == null)
                    entity.setDescription(entity.getName());
                else
                    entity.setFullText(entity.getName() + " " + entity.getDescription());

                if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId()))
                    sharingRegistryServerHandler.createEntity(entity);
                shareEntityWithGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroups, false);
            }
        }

        // Migrating from GatewayResourceProfile to GroupResourceProfile
        for (String domainID : domainOwnerMap.keySet()) {
            GatewayGroups gatewayGroups = gatewayGroupsMap.get(domainID);
            if (needsGroupResourceProfileMigration(domainID, domainOwnerMap.get(domainID), registryServiceClient, sharingRegistryServerHandler)) {

                GroupResourceProfile groupResourceProfile = migrateGatewayResourceProfileToGroupResourceProfile(domainID, registryServiceClient);

                // create GroupResourceProfile entity in sharing registry
                Entity entity = new Entity();
                entity.setEntityId(groupResourceProfile.getGroupResourceProfileId());
                entity.setDomainId(domainID);
                entity.setEntityTypeId(entity.getDomainId() + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
                entity.setOwnerId(domainOwnerMap.get(domainID));
                entity.setName(groupResourceProfile.getGroupResourceProfileName());
                entity.setDescription(groupResourceProfile.getGroupResourceProfileName() + " Group Resource Profile");
                if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId()))
                    sharingRegistryServerHandler.createEntity(entity);
                shareEntityWithGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroups, false);

            }
        }

        // Creating credential store token entries (GATEWAY SSH tokens)
        for (String domainID : domainOwnerMap.keySet()) {
            List<CredentialSummary> gatewayCredentialSummaries = credentialStoreServiceClient.getAllCredentialSummaryForGateway(SummaryType.SSH, domainID);
            for (CredentialSummary credentialSummary : gatewayCredentialSummaries) {
                Entity entity = new Entity();
                entity.setEntityId(credentialSummary.getToken());
                entity.setDomainId(domainID);
                entity.setEntityTypeId(entity.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                entity.setOwnerId(domainOwnerMap.get(domainID));
                entity.setName(credentialSummary.getToken());
                entity.setDescription(maxLengthString(credentialSummary.getDescription(), 255));
                if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId()))
                    sharingRegistryServerHandler.createEntity(entity);
                if (gatewayGroupsMap.containsKey(entity.getDomainId())) {
                    shareEntityWithAdminGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroupsMap.get(entity.getDomainId()), false);
                }
            }
        }

        // Creating credential store token entries (USER SSH tokens)
        for (String domainID : domainOwnerMap.keySet()) {
            List<User> sharingUsers = sharingRegistryServerHandler.getUsers(domainID, 0, Integer.MAX_VALUE);
            for (User sharingUser : sharingUsers) {

                String userId = sharingUser.getUserId();
                if (!userId.endsWith("@" + domainID)) {
                    System.out.println("Skipping credentials for user " + userId + " since sharing user id is improperly formed");
                    continue;
                }
                String username = userId.substring(0, userId.lastIndexOf("@"));
                List<CredentialSummary> gatewayCredentialSummaries = credentialStoreServiceClient.getAllCredentialSummaryForUserInGateway(SummaryType.SSH, domainID, username);
                for (CredentialSummary credentialSummary : gatewayCredentialSummaries) {
                    Entity entity = new Entity();
                    entity.setEntityId(credentialSummary.getToken());
                    entity.setDomainId(domainID);
                    entity.setEntityTypeId(entity.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                    entity.setOwnerId(userId);
                    entity.setName(credentialSummary.getToken());
                    // Cap description length at max 255 characters
                    entity.setDescription(maxLengthString(credentialSummary.getDescription(), 255));
                    if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId()))
                        sharingRegistryServerHandler.createEntity(entity);
                    // Don't need to share USER SSH tokens with any group
                }
            }
        }
        // Creating credential store token entries (GATEWAY PWD tokens)
        for (String domainID : domainOwnerMap.keySet()) {
            Map<String, String> gatewayPasswords = credentialStoreServiceClient.getAllPWDCredentialsForGateway(domainID);
            for (Map.Entry<String, String> gatewayPasswordEntry : gatewayPasswords.entrySet()) {
                Entity entity = new Entity();
                entity.setEntityId(gatewayPasswordEntry.getKey());
                entity.setDomainId(domainID);
                entity.setEntityTypeId(entity.getDomainId() + ":" + ResourceType.CREDENTIAL_TOKEN.name());
                entity.setOwnerId(domainOwnerMap.get(domainID));
                entity.setName(gatewayPasswordEntry.getKey());
                entity.setDescription(maxLengthString(gatewayPasswordEntry.getValue(), 255));
                if (!sharingRegistryServerHandler.isEntityExists(entity.getDomainId(), entity.getEntityId()))
                    sharingRegistryServerHandler.createEntity(entity);
                if (gatewayGroupsMap.containsKey(entity.getDomainId())) {
                    shareEntityWithAdminGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroupsMap.get(entity.getDomainId()), false);
                }
            }
        }

        expCatConnection.close();
        System.out.println("Completed!");

        System.exit(0);
    }

    private static void shareEntityWithGatewayGroups(SharingRegistryServerHandler sharingRegistryServerHandler, Entity entity, GatewayGroups gatewayGroups, boolean cascadePermission) throws TException {
        // Give default Gateway Users group READ access
        sharingRegistryServerHandler.shareEntityWithGroups(entity.getDomainId(), entity.getEntityId(),
                Arrays.asList(gatewayGroups.getDefaultGatewayUsersGroupId()),
                entity.getDomainId() + ":" + ResourcePermissionType.READ, cascadePermission);
        shareEntityWithAdminGatewayGroups(sharingRegistryServerHandler, entity, gatewayGroups, cascadePermission);
    }

    private static void shareEntityWithAdminGatewayGroups(SharingRegistryServerHandler sharingRegistryServerHandler, Entity entity, GatewayGroups gatewayGroups, boolean cascadePermission) throws TException {
        // Give Admins group and Read Only Admins group READ access
        sharingRegistryServerHandler.shareEntityWithGroups(entity.getDomainId(), entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId(), gatewayGroups.getReadOnlyAdminsGroupId()),
                entity.getDomainId() + ":" + ResourcePermissionType.READ, cascadePermission);
        // Give Admins group WRITE access
        sharingRegistryServerHandler.shareEntityWithGroups(entity.getDomainId(), entity.getEntityId(),
                Arrays.asList(gatewayGroups.getAdminsGroupId()),
                entity.getDomainId() + ":" + ResourcePermissionType.WRITE, cascadePermission);
    }

    private static List<UserProfile> getUsersToMigrate(SharingRegistryServerHandler sharingRegistryServerHandler, IamAdminServices.Client adminServiceClient, AuthzToken authzToken, String search, String domainId) throws TException {

        List<UserProfile> missingUsers = new ArrayList<>();
        List<UserProfile> keycloakUsers = adminServiceClient.getUsers(authzToken, 0, -1, search);

        for (UserProfile profile : keycloakUsers) {
            if (profile.getState().equals(Status.ACTIVE) && !sharingRegistryServerHandler.isUserExists(domainId, profile.getAiravataInternalUserId())) {
                missingUsers.add(profile);
            }
        }
        return missingUsers;
    }

    private static boolean migrateKeycloakUsersToGateway(IamAdminServices.Client adminServiceClient,AuthzToken authzToken, List<UserProfile> missingUsers) throws TException{

        boolean allUsersUpdated = true;
        for(UserProfile profile: missingUsers) {
                allUsersUpdated &= adminServiceClient.enableUser(authzToken, profile.getUserId());
            }
        return allUsersUpdated;
    }
    private static void checkUsersInSharingRegistryService(SharingRegistryServerHandler sharingRegistryServerHandler, List<UserProfile> missingUsers, String domainId) throws TException{
        System.out.println("Waiting for " + missingUsers.size() + " missing users to be propogated to sharing db");
        int waitCount = 0;
        // Wait up to 10 seconds for event based replication to complete, then
        // add missing users to sharing registry
        while (waitCount < 10) {
            boolean missingInSharing = false;
            for (UserProfile users : missingUsers) {
                if (!sharingRegistryServerHandler.isUserExists(domainId, users.getAiravataInternalUserId())) {
                    missingInSharing = true;
                    break;
                }
            }
            if (!missingInSharing) {
                break;
            }
            try {
                System.out.print(".");
                // wait for 1 second
                Thread.sleep(1000);
                waitCount++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (UserProfile users : missingUsers) {
            if (!sharingRegistryServerHandler.isUserExists(domainId, users.getAiravataInternalUserId())) {
                sharingRegistryServerHandler.createUser(ThriftDataModelConversion.getUser(users));
            }
        }
    }
    private static boolean addUsersToGroups(SharingRegistryServerHandler sharingRegistryServerHandler, List<UserProfile> missingUsers, GatewayGroups gatewayGroups, String domainId) throws TException, ApplicationSettingsException{
        //before adding to groups make sure sharing registry has the user otherwise add it
        checkUsersInSharingRegistryService(sharingRegistryServerHandler, missingUsers, domainId);
        boolean updatedAllUsers = true;
        Map<String, List<String>> roleMap = loadRolesForUsers(domainId, missingUsers
                .stream()
                .map(user -> user.getAiravataInternalUserId().substring(0, user.getAiravataInternalUserId().lastIndexOf("@")))
                .collect(Collectors.toList()));
        if(roleMap.containsKey("gateway-user")){
            List<String> userIds = roleMap.get("gateway-user").stream()
                    .map(username -> username + "@" + domainId)
                    .collect(Collectors.toList());
            updatedAllUsers &= sharingRegistryServerHandler.addUsersToGroup(domainId, userIds, gatewayGroups.getDefaultGatewayUsersGroupId());
        }
        if(roleMap.containsKey("admin")){
            List<String> userIds = roleMap.get("admin").stream()
                    .map(username -> username + "@" + domainId)
                    .collect(Collectors.toList());
            updatedAllUsers &= sharingRegistryServerHandler.addUsersToGroup(domainId, userIds, gatewayGroups.getAdminsGroupId());
        }
        if(roleMap.containsKey("admin-read-only")){
            List<String> userIds = roleMap.get("admin-read-only").stream()
                    .map(username -> username + "@" + domainId)
                    .collect(Collectors.toList());
            updatedAllUsers &= sharingRegistryServerHandler.addUsersToGroup(domainId, userIds, gatewayGroups.getReadOnlyAdminsGroupId());
        }

        return updatedAllUsers;
    }

    private static GatewayGroups migrateRolesToGatewayGroups(Domain domain, String ownerId, SharingRegistryServerHandler sharingRegistryServerHandler, RegistryService.Client registryServiceClient) throws TException, ApplicationSettingsException {
        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(domain.getDomainId());

        // Migrate roles to groups
        List<String> usernames = sharingRegistryServerHandler.getUsers(domain.getDomainId(), 0, -1)
                .stream()
                // Filter out bad user ids that don't end in "@" + domainId
                .filter(user -> user.getUserId().endsWith("@" + domain.getDomainId()))
                .map(user -> user.getUserId().substring(0, user.getUserId().lastIndexOf("@")))
                .collect(Collectors.toList());
        Map<String, List<String>> roleMap = loadRolesForUsers(domain.getDomainId(), usernames);

        UserGroup gatewayUsersGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                "Gateway Users",
                "Default group for users of the gateway.",
                roleMap.containsKey("gateway-user") ? roleMap.get("gateway-user") : Collections.emptyList());
        gatewayGroups.setDefaultGatewayUsersGroupId(gatewayUsersGroup.getGroupId());

        UserGroup adminUsersGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                "Admin Users",
                "Admin users group.",
                roleMap.containsKey("admin") ? roleMap.get("admin") : Collections.emptyList());
        gatewayGroups.setAdminsGroupId(adminUsersGroup.getGroupId());

        UserGroup readOnlyAdminsGroup = createGroup(sharingRegistryServerHandler, domain, ownerId,
                "Read Only Admin Users",
                "Group of admin users with read-only access.",
                roleMap.containsKey("admin-read-only") ? roleMap.get("admin-read-only") : Collections.emptyList());
        gatewayGroups.setReadOnlyAdminsGroupId(readOnlyAdminsGroup.getGroupId());

        registryServiceClient.createGatewayGroups(gatewayGroups);
        return gatewayGroups;
    }

    private static String getAdminOwnerUser(Domain domain, SharingRegistryServerHandler sharingRegistryServerHandler, CredentialStoreService.Client credentialStoreServiceClient, RegistryService.Client registryServiceClient) throws TException {
        GatewayResourceProfile gatewayResourceProfile = null;
        try {
            gatewayResourceProfile = registryServiceClient.getGatewayResourceProfile(domain.getDomainId());
        } catch (Exception e) {
            System.out.println("Skipping creating group based auth migration for " + domain.getDomainId() + " because it doesn't have a GatewayResourceProfile");
            return null;
        }
        if (gatewayResourceProfile.getIdentityServerPwdCredToken() == null) {
            System.out.println("Skipping creating group based auth migration for " + domain.getDomainId() + " because it doesn't have an identity server pwd credential token");
            return null;
        }
        String groupOwner = null;
        try {
            PasswordCredential credential = credentialStoreServiceClient.getPasswordCredential(
                    gatewayResourceProfile.getIdentityServerPwdCredToken(), gatewayResourceProfile.getGatewayID());
            groupOwner = credential.getLoginUserName();
        } catch (Exception e) {
            System.out.println("Skipping creating group based auth migration for " + domain.getDomainId() + " because the identity server pwd credential could not be retrieved.");
            return null;
        }

        String ownerId = groupOwner + "@" + domain.getDomainId();
        if (!sharingRegistryServerHandler.isUserExists(domain.getDomainId(), ownerId)) {
            System.out.println("Skipping creating group based auth migration for " + domain.getDomainId() + " because admin user doesn't exist in sharing registry.");
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
            } catch (Exception e) {
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
        userGroup.setDomainId(domain.getDomainId());
        userGroup.setGroupCardinality(GroupCardinality.MULTI_USER);
        userGroup.setCreatedTime(System.currentTimeMillis());
        userGroup.setUpdatedTime(System.currentTimeMillis());
        userGroup.setName(groupName);
        userGroup.setDescription(groupDescription);
        userGroup.setOwnerId(ownerId);
        userGroup.setGroupType(GroupType.DOMAIN_LEVEL_GROUP);
        sharingRegistryServerHandler.createGroup(userGroup);

        List<String> userIds = usernames.stream()
                .map(username -> username + "@" + domain.getDomainId())
                .collect(Collectors.toList());

        sharingRegistryServerHandler.addUsersToGroup(domain.getDomainId(), userIds, userGroup.getGroupId());
        return userGroup;
    }

    private static boolean needsGroupResourceProfileMigration(String gatewayId, String domainOwnerId, RegistryService.Client registryServiceClient, SharingRegistryServerHandler sharingRegistryServerHandler)
            throws TException {
        // Return true if GatewayResourceProfile has at least one ComputeResourcePreference and there is no GroupResourceProfile
        List<ComputeResourcePreference> computeResourcePreferences = registryServiceClient.getAllGatewayComputeResourcePreferences(gatewayId);
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue(gatewayId + ":" + ResourceType.GROUP_RESOURCE_PROFILE.name());
        List<String> accessibleGRPIds = sharingRegistryServerHandler.searchEntities(gatewayId, domainOwnerId, Collections.singletonList(searchCriteria), 0, -1)
                .stream()
                .map(p -> p.getEntityId())
                .collect(Collectors.toList());
        List<GroupResourceProfile> groupResourceProfiles = registryServiceClient.getGroupResourceList(gatewayId, accessibleGRPIds);
        return !computeResourcePreferences.isEmpty() && groupResourceProfiles.isEmpty();
    }

    private static GroupResourceProfile migrateGatewayResourceProfileToGroupResourceProfile(String gatewayId, RegistryService.Client registryServiceClient) throws TException {

        GroupResourceProfile groupResourceProfile = new GroupResourceProfile();
        groupResourceProfile.setGatewayId(gatewayId);
        groupResourceProfile.setGroupResourceProfileName("Default");
        GatewayResourceProfile gatewayResourceProfile = registryServiceClient.getGatewayResourceProfile(gatewayId);
        if (isValid(gatewayResourceProfile.getCredentialStoreToken())) {
            groupResourceProfile.setDefaultCredentialStoreToken(gatewayResourceProfile.getCredentialStoreToken());
        }
        List<GroupComputeResourcePreference> groupComputeResourcePreferences = new ArrayList<>();
        List<ComputeResourcePolicy> computeResourcePolicies = new ArrayList<>();
        List<ComputeResourcePreference> computeResourcePreferences = registryServiceClient.getAllGatewayComputeResourcePreferences(gatewayId);
        Map<String, String> allComputeResourceNames = registryServiceClient.getAllComputeResourceNames();
        for (ComputeResourcePreference computeResourcePreference : computeResourcePreferences) {
            if (!allComputeResourceNames.containsKey(computeResourcePreference.getComputeResourceId())) {
                System.out.println("Warning: compute resource " + computeResourcePreference.getComputeResourceId() + " does not exist, skipping converting its ComputeResourcePreference for " + gatewayId);
                continue;
            }
            GroupComputeResourcePreference groupComputeResourcePreference = convertComputeResourcePreferenceToGroupComputeResourcePreference(groupResourceProfile.getGroupResourceProfileId(), computeResourcePreference);
            ComputeResourcePolicy computeResourcePolicy = createDefaultComputeResourcePolicy(groupResourceProfile.getGroupResourceProfileId(), computeResourcePreference.getComputeResourceId(), registryServiceClient);
            groupComputeResourcePreferences.add(groupComputeResourcePreference);
            computeResourcePolicies.add(computeResourcePolicy);
        }
        groupResourceProfile.setComputePreferences(groupComputeResourcePreferences);
        groupResourceProfile.setComputeResourcePolicies(computeResourcePolicies);
        String groupResourceProfileId = registryServiceClient.createGroupResourceProfile(groupResourceProfile);
        groupResourceProfile.setGroupResourceProfileId(groupResourceProfileId);
        return groupResourceProfile;
    }

    private static GroupComputeResourcePreference convertComputeResourcePreferenceToGroupComputeResourcePreference(String groupResourceProfileId, ComputeResourcePreference computeResourcePreference) {
        GroupComputeResourcePreference groupComputeResourcePreference = new GroupComputeResourcePreference();
        groupComputeResourcePreference.setGroupResourceProfileId(groupResourceProfileId);
        groupComputeResourcePreference.setComputeResourceId(computeResourcePreference.getComputeResourceId());
        groupComputeResourcePreference.setOverridebyAiravata(computeResourcePreference.isOverridebyAiravata());
        if (isValid(computeResourcePreference.getLoginUserName())) {
            groupComputeResourcePreference.setLoginUserName(computeResourcePreference.getLoginUserName());
        }
        groupComputeResourcePreference.setPreferredJobSubmissionProtocol(computeResourcePreference.getPreferredJobSubmissionProtocol());
        groupComputeResourcePreference.setPreferredDataMovementProtocol(computeResourcePreference.getPreferredDataMovementProtocol());
        if (isValid(computeResourcePreference.getPreferredBatchQueue())) {
            groupComputeResourcePreference.setPreferredBatchQueue(computeResourcePreference.getPreferredBatchQueue());
        }
        if (isValid(computeResourcePreference.getScratchLocation())) {
            groupComputeResourcePreference.setScratchLocation(computeResourcePreference.getScratchLocation());
        }
        if (isValid(computeResourcePreference.getAllocationProjectNumber())) {
            groupComputeResourcePreference.setAllocationProjectNumber(computeResourcePreference.getAllocationProjectNumber());
        }
        if (isValid(computeResourcePreference.getResourceSpecificCredentialStoreToken())) {
            groupComputeResourcePreference.setResourceSpecificCredentialStoreToken(computeResourcePreference.getResourceSpecificCredentialStoreToken());
        }
        if (isValid(computeResourcePreference.getUsageReportingGatewayId())) {
            groupComputeResourcePreference.setUsageReportingGatewayId(computeResourcePreference.getUsageReportingGatewayId());
        }
        if (isValid(groupComputeResourcePreference.getQualityOfService())) {
            groupComputeResourcePreference.setQualityOfService(computeResourcePreference.getQualityOfService());
        }
        // Note: skipping copying of reservation time and ssh account provisioner configuration for now
        return groupComputeResourcePreference;
    }

    private static ComputeResourcePolicy createDefaultComputeResourcePolicy(String groupResourceProfileId, String computeResourceId, RegistryService.Client registryServiceClient) throws TException {
        ComputeResourcePolicy computeResourcePolicy = new ComputeResourcePolicy();
        computeResourcePolicy.setComputeResourceId(computeResourceId);
        computeResourcePolicy.setGroupResourceProfileId(groupResourceProfileId);
        ComputeResourceDescription computeResourceDescription = registryServiceClient.getComputeResource(computeResourceId);
        List<String> batchQueueNames = computeResourceDescription.getBatchQueues().stream().map(bq -> bq.getQueueName()).collect(Collectors.toList());
        computeResourcePolicy.setAllowedBatchQueues(batchQueueNames);
        return computeResourcePolicy;
    }

    private static boolean isValid(String s) {
        return s != null && !"".equals(s.trim());
    }

    private static String maxLengthString(String s, int maxLength) {

        if (s != null) {
            return s.substring(0, Math.min(maxLength, s.length()));
        } else {
            return null;
        }
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

    private static IamAdminServices.Client getIamAdminServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getProfileServiceServerPort());
        final String serverHost = ServerSettings.getProfileServiceServerHost();
        try {
            return ProfileServiceClientFactory.createIamAdminServiceClient(serverHost, serverPort);
        } catch (IamAdminServicesException e) {
            throw new TException("Unable to create i am admin service client...", e);
        }
    }

    private static AuthzToken getManagementUsersAccessToken(String tenantId) throws TException{
        try {
            AiravataSecurityManager securityManager = SecurityManagerFactory.getSecurityManager();
            AuthzToken authzToken = securityManager.getUserManagementServiceAccountAuthzToken(tenantId);
            return authzToken;
        } catch (AiravataSecurityException e){
            throw new TException("Unable to fetch access token for management user for tenant: " + tenantId, e);
        }

    }
}
