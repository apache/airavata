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
package org.apache.airavata.sharing.registry;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.sharing.registry.models.*;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.Arrays;

public class CipresTest {
    public static void main(String[] args) throws InterruptedException, TException, ApplicationSettingsException {
        System.out.println("Hello World!");
        //should use the correct host name and port here
        String serverHost = "wb-airavata.scigap.org";

        int serverPort = 7878;
        TTransport transport = null;
        TProtocol protocol = null;
        try {

            SharingRegistryService.Client sharingServiceClient;

            //Non Secure Client
//            transport = new TSocket(serverHost, serverPort);
//            transport.open();
//            protocol = new TBinaryProtocol(transport);
//            sharingServiceClient= new SharingRegistryService.Client(protocol);

            //TLS enabled client
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setKeyStore(ServerSettings.getKeyStorePath(), ServerSettings.getKeyStorePassword());
            params.setTrustStore(ServerSettings.getTrustStorePath(), ServerSettings.getTrustStorePassword());
            transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
            protocol = new TBinaryProtocol(transport);
            sharingServiceClient = new SharingRegistryService.Client(protocol);


            try {
                sharingServiceClient.deleteDomain("test-domain");
            } catch (SharingRegistryException sre1) {
                System.out.println("deleteDomain failed" + sre1.getMessage() + "\n");
            }
            Domain domain = new Domain();
            //has to be one word
            domain.setName("test-domain");
            //optional
            domain.setDescription("test domain description");
            //domain id will be same as domain name
            String domainId = sharingServiceClient.createDomain(domain);
            System.out.println("After domain creation...\n");

            User user1 = new User();
            String userName1 = "test-user-1";
            String userId1 = "test-user-1";
            //required
            user1.setUserId(userId1);
            //required
            user1.setUserName(userName1);
            //required
            user1.setDomainId(domainId);
            //required
            user1.setFirstName("John");
            //required
            user1.setLastName("Doe");
            //required
            user1.setEmail("john.doe@abc.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[10];
            //user1.setIcon(icon);
            sharingServiceClient.createUser(user1);
            User user2 = new User();
            String userName2 = "test-user-2";
            String userId2 = "test-user-2";
            //required
            user2.setUserId(userId2);
            //required
            user2.setUserName(userName2);
            //required
            user2.setDomainId(domainId);
            //required
            user2.setFirstName("John");
            //required
            user2.setLastName("Doe");
            //required
            user2.setEmail("john.doe@abc.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[20];
            //user2.setIcon(icon);
            sharingServiceClient.createUser(user2);
            User user3 = new User();
            String userName3 = "test-user-3";
            String userId3 = "test-user-3";
            //required
            user3.setUserId(userId3);
            //required
            user3.setUserName(userName3);
            //required
            user3.setDomainId(domainId);
            //required
            user3.setFirstName("John");
            //required
            user3.setLastName("Doe");
            //required
            user3.setEmail("john.doe@abc.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[30];
            //user3.setIcon(icon);
            sharingServiceClient.createUser(user3);
            System.out.println("After user creation...\n");

            UserGroup userGroup1 = new UserGroup();
            //required
            userGroup1.setGroupId("test-group-1");
            //required
            userGroup1.setDomainId(domainId);
            //required
            userGroup1.setName("test-group-1");
            //optional
            //userGroup1.setDescription("test group description");
            //required
            userGroup1.setOwnerId("test-user-1");
            //required
            userGroup1.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingServiceClient.createGroup(userGroup1);
            //Similarly create another group "userGroup2" with the owner being "test-user-2".
            UserGroup userGroup2 = new UserGroup();
            //required
            userGroup2.setGroupId("test-group-2");
            //required
            userGroup2.setDomainId(domainId);
            //required
            userGroup2.setName("test-group-2");
            //optional
            //userGroup2.setDescription("test group description");
            //required
            userGroup2.setOwnerId("test-user-2");
            //required
            userGroup2.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingServiceClient.createGroup(userGroup2);
            System.out.println("After group creation...\n");

            sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("test-user-3"), "test-group-2");
            System.out.println("After adding user to group...\n");

            sharingServiceClient.addChildGroupsToParentGroup(domainId, Arrays.asList("test-group-2"), "test-group-1");


            PermissionType permissionType1 = new PermissionType();
            //required
            permissionType1.setPermissionTypeId("READ");
            //required
            permissionType1.setDomainId(domainId);
            //required
            permissionType1.setName("READ");
            //optional
            permissionType1.setDescription("READ description");
            sharingServiceClient.createPermissionType(permissionType1);
            PermissionType permissionType2 = new PermissionType();
            permissionType2.setPermissionTypeId("WRITE");
            permissionType2.setDomainId(domainId);
            permissionType2.setName("WRITE");
            permissionType2.setDescription("WRITE description");
            sharingServiceClient.createPermissionType(permissionType2);
            PermissionType permissionType3 = new PermissionType();
            permissionType3.setPermissionTypeId("CLONE");
            permissionType3.setDomainId(domainId);
            permissionType3.setName("CLONE");
            permissionType3.setDescription("CLONE description");
            sharingServiceClient.createPermissionType(permissionType3);
            System.out.println("After adding groups to parent group...\n");

            EntityType entityType1 = new EntityType();
            //required
            entityType1.setEntityTypeId("PROJECT");
            //required
            entityType1.setDomainId(domainId);
            //required
            entityType1.setName("PROJECT");
            //optional
            entityType1.setDescription("PROJECT entity type description");
            sharingServiceClient.createEntityType(entityType1);
            EntityType entityType2 = new EntityType();
            entityType2.setEntityTypeId("EXPERIMENT");
            entityType2.setDomainId(domainId);
            entityType2.setName("EXPERIMENT");
            entityType2.setDescription("EXPERIMENT entity type");
            sharingServiceClient.createEntityType(entityType2);
            EntityType entityType3 = new EntityType();
            entityType3.setEntityTypeId("FILE");
            entityType3.setDomainId(domainId);
            entityType3.setName("FILE");
            entityType3.setDescription("FILE entity type");
            sharingServiceClient.createEntityType(entityType3);
            System.out.println("After project entity creation...\n");

            Entity entity1 = new Entity();
            //required
            entity1.setEntityId("test-project-1");
            //required
            entity1.setDomainId(domainId);
            //required
            entity1.setEntityTypeId("PROJECT");
            //required
            entity1.setOwnerId("test-user-1");
            //required
            entity1.setName("test-project-1");
            //optional
            entity1.setDescription("test project 1 description");
            //optional
            entity1.setFullText("test project 1 stampede gaussian seagrid");
            //optional - If not set this will be default to current system time
            entity1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingServiceClient.createEntity(entity1);
            System.out.println("After currentTimeMillis()...\n");
            Entity entity2 = new Entity();
            entity2.setEntityId("test-experiment-1");
            entity2.setDomainId(domainId);
            entity2.setEntityTypeId("EXPERIMENT");
            entity2.setOwnerId("test-user-1");
            entity2.setName("test-experiment-1");
            entity2.setDescription("test experiment 1 description");
            entity2.setParentEntityId("test-project-1");
            entity2.setFullText("test experiment 1 benzene");
            System.out.println("Before sharingServiceClient.createEntity entity2...\n");
            sharingServiceClient.createEntity(entity2);
            System.out.println("After sharingServiceClient.createEntity entity2...\n");
            Entity entity3 = new Entity();
            entity3.setEntityId("test-experiment-2");
            entity3.setDomainId(domainId);
            entity3.setEntityTypeId("EXPERIMENT");
            entity3.setOwnerId("test-user-1");
            entity3.setName("test-experiment-2");
            entity3.setDescription("test experiment 2 description");
            entity3.setParentEntityId("test-project-1");
            entity3.setFullText("test experiment 1 3-methyl 1-butanol stampede");
            sharingServiceClient.createEntity(entity3);
            System.out.println("After sharingServiceClient.createEntity entity3...\n");
            Entity entity4 = new Entity();
            entity4.setEntityId("test-file-1");
            entity4.setDomainId(domainId);
            entity4.setEntityTypeId("FILE");
            entity4.setOwnerId("test-user-1");
            entity4.setName("test-file-1");
            entity4.setDescription("test file 1 description");
            entity4.setParentEntityId("test-experiment-2");
            entity4.setFullText("test input file 1 for experiment 2");
            sharingServiceClient.createEntity(entity4);
            System.out.println("After sharingServiceClient.createEntity entity4...\n");
            System.out.println("After test entity creation...\n");

            //shared with cascading permissions
            //System.out.println("Before shareEntityWithUsers WRITE...\n");
            //sharingServiceClient.shareEntityWithUsers(domainId, "test-project-1", Arrays.asList("test-user-2"), "WRITE", true);
            System.out.println("Before shareEntityWithGroups READ...\n");
            long time = System.currentTimeMillis();
            sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"), "READ", true);
            System.out.println("Time for sharing " + (System.currentTimeMillis() - time));
            //shared with non cascading permissions
            System.out.println("Before shareEntityWithGroups CLONE...\n");
            time = System.currentTimeMillis();
            sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"), "CLONE", false);
            System.out.println("Time for sharing " + (System.currentTimeMillis() - time));

            //test-project-1 is explicitly shared with test-user-2 with WRITE permission
            System.out.println("Before userHasAccess 1...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-project-1", "WRITE"));
            //test-user-2 has WRITE permission to test-experiment-1 and test-experiment-2 indirectly
            System.out.println("Before userHasAccess 2...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-1", "WRITE"));
            System.out.println("Before userHasAccess 3...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-2", "WRITE"));
            //test-user-2 does not have READ permission to test-experiment-1 and test-experiment-2
            System.out.println("Before userHasAccess 4...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-1", "READ"));
            System.out.println(domainId + " test-user-2 " + " test-experiment-2 " + " READ ");
            System.out.println("Before userHasAccess 5...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-2", "READ"));
            //test-user-3 does not have READ permission to test-project-1
            System.out.println("Before userHasAccess 6...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-project-1", "READ"));
            //test-experiment-2 is shared with test-group-2 with READ permission. Therefore test-user-3 has READ permission
            System.out.println("Before userHasAccess 7...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "READ"));
            //test-user-3 does not have WRITE permission to test-experiment-2
            System.out.println("Before userHasAccess 8...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "WRITE"));
            //test-user-3 has CLONE permission to test-experiment-2
            System.out.println("Before userHasAccess 9...\n");
            System.out.println((sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "CLONE")));
            //test-user-3 does not have CLONE permission to test-file-1
            System.out.println("Before userHasAccess 10...\n");
            System.out.println((sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-file-1", "CLONE")));
            System.out.println("After cascading permissions...\n");

            ArrayList<SearchCriteria> filters = new ArrayList<>();
            //ArrayList<SearchCriteria> filters = new List<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.LIKE);
            searchCriteria.setValue("experiment stampede methyl");
            //searchCriteria.setValue("stampede");
            //searchCriteria.setSearchField(EntitySearchField.NAME);
            searchCriteria.setSearchField(EntitySearchField.FULL_TEXT);
            filters.add(searchCriteria);
            searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue("READ");
            searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            filters.add(searchCriteria);
            System.out.println(sharingServiceClient.searchEntities(domainId, "test-user-2", filters, 0, -1).size());
            System.out.println(sharingServiceClient.searchEntities(domainId, "test-user-2", filters, 0, -1));
            System.out.println("After searchEntities...\n");


            //System.out.println("After searchEntities...\n");
            User userA = new User();
            String userNameA = "UserA";
            String userIdA = "UserA";
            //required
            userA.setUserId(userIdA);
            //required
            userA.setUserName(userNameA);
            //required
            userA.setDomainId(domainId);
            //required
            userA.setFirstName("User");
            //required
            userA.setLastName("A");
            //required
            userA.setEmail("user.a@example.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[10];
            //userA.setIcon(icon);
            sharingServiceClient.createUser(userA);
            User userB = new User();
            String userNameB = "UserB";
            String userIdB = "UserB";
            //required
            userB.setUserId(userIdB);
            //required
            userB.setUserName(userNameB);
            //required
            userB.setDomainId(domainId);
            //required
            userB.setFirstName("User");
            //required
            userB.setLastName("B");
            //required
            userB.setEmail("user.b@example.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[10];
            //userB.setIcon(icon);
            sharingServiceClient.createUser(userB);
            User userC = new User();
            String userNameC = "UserC";
            String userIdC = "UserC";
            //required
            userC.setUserId(userIdC);
            //required
            userC.setUserName(userNameC);
            //required
            userC.setDomainId(domainId);
            //required
            userC.setFirstName("User");
            //required
            userC.setLastName("C");
            //required
            userC.setEmail("user.c@example.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[10];
            //userC.setIcon(icon);
            sharingServiceClient.createUser(userC);
            User userD = new User();
            String userNameD = "UserD";
            String userIdD = "UserD";
            //required
            userD.setUserId(userIdD);
            //required
            userD.setUserName(userNameD);
            //required
            userD.setDomainId(domainId);
            //required
            userD.setFirstName("User");
            //required
            userD.setLastName("D");
            //required
            userD.setEmail("user.d@example.com");
            //optional - this should be bytes of the users image icon
            //byte[] icon = new byte[10];
            //userD.setIcon(icon);
            sharingServiceClient.createUser(userD);
            System.out.println("After user creation...\n");

            UserGroup Group1 = new UserGroup();
            //required
            Group1.setGroupId("Group1");
            //required
            Group1.setDomainId(domainId);
            //required
            Group1.setName("Group1");
            //optional
            //userGroup1.setDescription("test group description");
            //required
            Group1.setOwnerId("UserA");
            //required
            Group1.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingServiceClient.createGroup(Group1);
            System.out.println("After Group1 creation...\n");

            sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("UserB"), "Group1");
            sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("UserC"), "Group1");
            sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("UserD"), "Group1");

            System.out.println("After adding users to Group1 creation...\n");

            EntityType entityTypeFolder = new EntityType();
            //required
            entityTypeFolder.setEntityTypeId("FOLDER");
            //required
            entityTypeFolder.setDomainId(domainId);
            //required
            entityTypeFolder.setName("FOLDER");
            //optional
            //entityTypeFolder.setDescription("PROJECT entity type description");
            sharingServiceClient.createEntityType(entityTypeFolder);
            System.out.println("After creating FOLDER entity type...\n");

            EntityType entityTypeInputData = new EntityType();
            //required
            entityTypeInputData.setEntityTypeId("INPUTDATA");
            //required
            entityTypeInputData.setDomainId(domainId);
            //required
            entityTypeInputData.setName("INPUTDATA");
            //optional
            //entityTypeFolder.setDescription("PROJECT entity type description");
            sharingServiceClient.createEntityType(entityTypeInputData);
            System.out.println("After creating INPUTDATA entity type...\n");

            Entity entityB1 = new Entity();
            //required
            entityB1.setEntityId("UserBProject1");
            //required
            entityB1.setDomainId(domainId);
            //required
            entityB1.setEntityTypeId("PROJECT");
            //required
            entityB1.setOwnerId("UserB");
            //required
            entityB1.setName("UserBProject1");
            //optional
            entityB1.setDescription("User B's Project 1");
            //optional
            entityB1.setFullText("test project 1");
            //optional - If not set this will be default to current system time
            entityB1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingServiceClient.createEntity(entityB1);
            System.out.println("After creating UserBProject1 ...\n");

            Entity entityC1 = new Entity();
            //required
            entityC1.setEntityId("UserCProject2");
            //required
            entityC1.setDomainId(domainId);
            //required
            entityC1.setEntityTypeId("PROJECT");
            //required
            entityC1.setOwnerId("UserC");
            //required
            entityC1.setName("UserCProject2");
            //optional
            entityC1.setDescription("User C's Project 2");
            //optional
            entityC1.setFullText("test project 2");
            //optional - If not set this will be default to current system time
            entityC1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingServiceClient.createEntity(entityC1);
            System.out.println("After creating UserCProject2 ...\n");

            Entity entityF1 = new Entity();
            entityF1.setEntityId("Folder1");
            entityF1.setDomainId(domainId);
            entityF1.setEntityTypeId("FOLDER");
            entityF1.setOwnerId("UserB");
            entityF1.setName("UserBFolder1");
            entityF1.setDescription("UserB's Folder 1");
            entityF1.setParentEntityId("UserBProject1");
            entityF1.setFullText("test experiment 1 ethidium");
            sharingServiceClient.createEntity(entityF1);
            System.out.println("After creating Folder1 ...\n");

            Entity entityD1 = new Entity();
            entityD1.setEntityId("Data1");
            entityD1.setDomainId(domainId);
            entityD1.setEntityTypeId("INPUTDATA");
            entityD1.setOwnerId("UserB");
            entityD1.setName("UserBData1");
            entityD1.setDescription("UserB's Data 1");
            entityD1.setParentEntityId("Folder1");
            entityD1.setFullText("Data 1 for User B Folder 1");
            sharingServiceClient.createEntity(entityD1);
            System.out.println("After creating Data1 ...\n");

            Entity entityF2 = new Entity();
            entityF2.setEntityId("Folder2");
            entityF2.setDomainId(domainId);
            entityF2.setEntityTypeId("FOLDER");
            entityF2.setOwnerId("UserC");
            entityF2.setName("UserCFolder2");
            entityF2.setDescription("UserC's Folder 2");
            entityF2.setParentEntityId("UserCProject2");
            entityF2.setFullText("test experiment 2 ethidium");
            sharingServiceClient.createEntity(entityF2);
            System.out.println("After creating Folder2 ...\n");

            Entity entityD2 = new Entity();
            entityD2.setEntityId("Data2");
            entityD2.setDomainId(domainId);
            entityD2.setEntityTypeId("INPUTDATA");
            entityD2.setOwnerId("UserC");
            entityD2.setName("UserCData2");
            entityD2.setDescription("UserC's Data 2");
            entityD2.setParentEntityId("Folder2");
            entityD2.setFullText("Data 2 for User C Folder 1");
            sharingServiceClient.createEntity(entityD2);
            System.out.println("After creating Data2 ...\n");

            //sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"), "READ", true);
            time = System.currentTimeMillis();
            sharingServiceClient.shareEntityWithGroups(domainId, "Folder1", Arrays.asList("Group1"), "READ", true);
            System.out.println("Time for sharing " + (System.currentTimeMillis() - time));
            System.out.println("After READ sharing UserBFolder1 with Group1 ...\n");
            //sharingServiceClient.shareEntityWithGroups(domainId, "Folder2", Arrays.asList("Group1"), "READ", true);
            //System.out.println("After READ sharing UserCFolder2 with Group1 ...\n");

            Entity entityD3 = new Entity();
            entityD3.setEntityId("Data3");
            entityD3.setDomainId(domainId);
            entityD3.setEntityTypeId("INPUTDATA");
            entityD3.setOwnerId("UserC");
            entityD3.setName("UserCData3");
            entityD3.setDescription("UserC's Data 3");
            entityD3.setParentEntityId("Folder2");
            entityD3.setFullText("Data 3 for User C Folder 2");
            sharingServiceClient.createEntity(entityD3);
            System.out.println("After creating Data3 ...\n");

            System.out.println("Does UserC have READ access to Data1 ...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "UserC", "Data1", "READ"));
            System.out.println("Does UserC have READ access to Data2 ...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "UserC", "Data2", "READ"));
            System.out.println("Does UserC have READ access to Data3 ...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "UserC", "Data3", "READ"));
            System.out.println("Does UserB have READ access to Data3 ...\n");
            System.out.println(sharingServiceClient.userHasAccess(domainId, "UserB", "Data3", "READ"));

            ArrayList<SearchCriteria> sharedfilters = new ArrayList<>();
            searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue("READ");
            searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            sharedfilters.add(searchCriteria);
            System.out.println("items READable by UserC ...\n");
            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1).size());
            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            searchCriteria = new SearchCriteria();
            //searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setSearchCondition(SearchCondition.NOT);
            searchCriteria.setValue("UserC");
            searchCriteria.setSearchField(EntitySearchField.OWNER_ID);
            sharedfilters.add(searchCriteria);
            System.out.println("items READable, and not owned by UserC by UserC ...\n");
            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1).size());
            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            System.out.println("After searchEntities 2...\n");

            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1).size());
            System.out.println(sharingServiceClient.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            System.out.println("After searchEntities 2...\n");


            sharingServiceClient.removeUsersFromGroup(domainId, Arrays.asList("UserD"), "Group1");
            System.out.println("After removing UserD from Group1 ...\n");

            sharingServiceClient.deleteGroup(domainId, "Group1");
            System.out.println("After deleting Group1 ...\n");

            System.out.println("End of try clause...\n");
        } catch (TTransportException ex1) {
            System.out.println("TTransportException...\n");
            System.out.println(ex1);
            System.out.println(ex1.getCause());
            ex1.printStackTrace();
            System.out.println(ex1.getMessage());
        } catch (SharingRegistryException ex2) {
            System.out.println("SharingRegistryException...\n");
            System.out.println(ex2.getMessage());
        } catch (TException ex3) {
            System.out.println("TException...\n");
            System.out.println(ex3.getMessage());
        } finally {
            System.out.println("In finally...\n");
            transport.close();
        }
    }
}
