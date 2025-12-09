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
package org.apache.airavata.sharing;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.config.JpaConfig;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.sharing.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class CipresTest implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CipresTest.class);

    private final SharingRegistryService sharingService;

    public CipresTest(SharingRegistryService sharingService) {
        this.sharingService = sharingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CipresTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Hello World!");

        try {

            try {
                sharingService.deleteDomain("test-domain");
            } catch (SharingRegistryException sre1) {
                logger.info("deleteDomain failed: {}", sre1.getMessage());
            }
            Domain domain = new Domain();
            // has to be one word
            domain.setName("test-domain");
            // optional
            domain.setDescription("test domain description");
            // domain id will be same as domain name
            String domainId = sharingService.createDomain(domain);
            logger.info("After domain creation...");

            User user1 = new User();
            String userName1 = "test-user-1";
            String userId1 = "test-user-1";
            // required
            user1.setUserId(userId1);
            // required
            user1.setUserName(userName1);
            // required
            user1.setDomainId(domainId);
            // required
            user1.setFirstName("John");
            // required
            user1.setLastName("Doe");
            // required
            user1.setEmail("john.doe@abc.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[10];
            // user1.setIcon(icon);
            sharingService.createUser(user1);
            User user2 = new User();
            String userName2 = "test-user-2";
            String userId2 = "test-user-2";
            // required
            user2.setUserId(userId2);
            // required
            user2.setUserName(userName2);
            // required
            user2.setDomainId(domainId);
            // required
            user2.setFirstName("John");
            // required
            user2.setLastName("Doe");
            // required
            user2.setEmail("john.doe@abc.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[20];
            // user2.setIcon(icon);
            sharingService.createUser(user2);
            User user3 = new User();
            String userName3 = "test-user-3";
            String userId3 = "test-user-3";
            // required
            user3.setUserId(userId3);
            // required
            user3.setUserName(userName3);
            // required
            user3.setDomainId(domainId);
            // required
            user3.setFirstName("John");
            // required
            user3.setLastName("Doe");
            // required
            user3.setEmail("john.doe@abc.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[30];
            // user3.setIcon(icon);
            sharingService.createUser(user3);
            logger.info("After user creation...");

            UserGroup userGroup1 = new UserGroup();
            // required
            userGroup1.setGroupId("test-group-1");
            // required
            userGroup1.setDomainId(domainId);
            // required
            userGroup1.setName("test-group-1");
            // optional
            // userGroup1.setDescription("test group description");
            // required
            userGroup1.setOwnerId("test-user-1");
            // required
            userGroup1.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(userGroup1);
            // Similarly create another group "userGroup2" with the owner being "test-user-2".
            UserGroup userGroup2 = new UserGroup();
            // required
            userGroup2.setGroupId("test-group-2");
            // required
            userGroup2.setDomainId(domainId);
            // required
            userGroup2.setName("test-group-2");
            // optional
            // userGroup2.setDescription("test group description");
            // required
            userGroup2.setOwnerId("test-user-2");
            // required
            userGroup2.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(userGroup2);
            logger.info("After group creation...");

            sharingService.addUsersToGroup(domainId, List.of("test-user-3"), "test-group-2");
            logger.info("After adding user to group...");

            sharingService.addChildGroupsToParentGroup(domainId, List.of("test-group-2"), "test-group-1");

            PermissionType permissionType1 = new PermissionType();
            // required
            permissionType1.setPermissionTypeId("READ");
            // required
            permissionType1.setDomainId(domainId);
            // required
            permissionType1.setName("READ");
            // optional
            permissionType1.setDescription("READ description");
            sharingService.createPermissionType(permissionType1);
            PermissionType permissionType2 = new PermissionType();
            permissionType2.setPermissionTypeId("WRITE");
            permissionType2.setDomainId(domainId);
            permissionType2.setName("WRITE");
            permissionType2.setDescription("WRITE description");
            sharingService.createPermissionType(permissionType2);
            PermissionType permissionType3 = new PermissionType();
            permissionType3.setPermissionTypeId("CLONE");
            permissionType3.setDomainId(domainId);
            permissionType3.setName("CLONE");
            permissionType3.setDescription("CLONE description");
            sharingService.createPermissionType(permissionType3);
            logger.info("After adding groups to parent group...");

            EntityType entityType1 = new EntityType();
            // required
            entityType1.setEntityTypeId("PROJECT");
            // required
            entityType1.setDomainId(domainId);
            // required
            entityType1.setName("PROJECT");
            // optional
            entityType1.setDescription("PROJECT entity type description");
            sharingService.createEntityType(entityType1);
            EntityType entityType2 = new EntityType();
            entityType2.setEntityTypeId("EXPERIMENT");
            entityType2.setDomainId(domainId);
            entityType2.setName("EXPERIMENT");
            entityType2.setDescription("EXPERIMENT entity type");
            sharingService.createEntityType(entityType2);
            EntityType entityType3 = new EntityType();
            entityType3.setEntityTypeId("FILE");
            entityType3.setDomainId(domainId);
            entityType3.setName("FILE");
            entityType3.setDescription("FILE entity type");
            sharingService.createEntityType(entityType3);
            logger.info("After project entity creation...");

            Entity entity1 = new Entity();
            // required
            entity1.setEntityId("test-project-1");
            // required
            entity1.setDomainId(domainId);
            // required
            entity1.setEntityTypeId("PROJECT");
            // required
            entity1.setOwnerId("test-user-1");
            // required
            entity1.setName("test-project-1");
            // optional
            entity1.setDescription("test project 1 description");
            // optional
            entity1.setFullText("test project 1 stampede gaussian seagrid");
            // optional - If not set this will be default to current system time
            entity1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingService.createEntity(entity1);
            logger.info("After currentTimeMillis()...");
            Entity entity2 = new Entity();
            entity2.setEntityId("test-experiment-1");
            entity2.setDomainId(domainId);
            entity2.setEntityTypeId("EXPERIMENT");
            entity2.setOwnerId("test-user-1");
            entity2.setName("test-experiment-1");
            entity2.setDescription("test experiment 1 description");
            entity2.setParentEntityId("test-project-1");
            entity2.setFullText("test experiment 1 benzene");
            logger.info("Before sharingServiceClient.createEntity entity2...");
            sharingService.createEntity(entity2);
            logger.info("After sharingServiceClient.createEntity entity2...");
            Entity entity3 = new Entity();
            entity3.setEntityId("test-experiment-2");
            entity3.setDomainId(domainId);
            entity3.setEntityTypeId("EXPERIMENT");
            entity3.setOwnerId("test-user-1");
            entity3.setName("test-experiment-2");
            entity3.setDescription("test experiment 2 description");
            entity3.setParentEntityId("test-project-1");
            entity3.setFullText("test experiment 1 3-methyl 1-butanol stampede");
            sharingService.createEntity(entity3);
            logger.info("After sharingServiceClient.createEntity entity3...");
            Entity entity4 = new Entity();
            entity4.setEntityId("test-file-1");
            entity4.setDomainId(domainId);
            entity4.setEntityTypeId("FILE");
            entity4.setOwnerId("test-user-1");
            entity4.setName("test-file-1");
            entity4.setDescription("test file 1 description");
            entity4.setParentEntityId("test-experiment-2");
            entity4.setFullText("test input file 1 for experiment 2");
            sharingService.createEntity(entity4);
            logger.info("After sharingServiceClient.createEntity entity4...");
            logger.info("After test entity creation...");

            // shared with cascading permissions
            // logger.info("Before shareEntityWithUsers WRITE...");
            // sharingServiceClient.shareEntityWithUsers(domainId, "test-project-1", Arrays.asList("test-user-2"),
            // "WRITE", true);
            logger.info("Before shareEntityWithGroups READ...");
            long time = System.currentTimeMillis();
            sharingService.shareEntityWithGroups(domainId, "test-experiment-2", List.of("test-group-2"), "READ", true);
            logger.info("Time for sharing " + (System.currentTimeMillis() - time));
            // shared with non cascading permissions
            logger.info("Before shareEntityWithGroups CLONE...");
            time = System.currentTimeMillis();
            sharingService.shareEntityWithGroups(
                    domainId, "test-experiment-2", List.of("test-group-2"), "CLONE", false);
            logger.info("Time for sharing " + (System.currentTimeMillis() - time));

            // test-project-1 is explicitly shared with test-user-2 with WRITE permission
            logger.info("Before userHasAccess 1...");
            logger.info(
                    "userHasAccess 1: {}",
                    sharingService.userHasAccess(domainId, "test-user-2", "test-project-1", "WRITE"));
            // test-user-2 has WRITE permission to test-experiment-1 and test-experiment-2 indirectly
            logger.info("Before userHasAccess 2...");
            logger.info(
                    "userHasAccess 2: {}",
                    sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-1", "WRITE"));
            logger.info("Before userHasAccess 3...");
            logger.info(
                    "userHasAccess 3: {}",
                    sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-2", "WRITE"));
            // test-user-2 does not have READ permission to test-experiment-1 and test-experiment-2
            logger.info("Before userHasAccess 4...");
            logger.info(
                    "userHasAccess 4: {}",
                    sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-1", "READ"));
            logger.info(
                    "userHasAccess 5: {}",
                    sharingService.userHasAccess(domainId, "test-user-2", "test-experiment-2", "READ"));
            // test-user-3 does not have READ permission to test-project-1
            logger.info("Before userHasAccess 6...");
            logger.info(
                    "userHasAccess 6: {}",
                    sharingService.userHasAccess(domainId, "test-user-3", "test-project-1", "READ"));
            // test-experiment-2 is shared with test-group-2 with READ permission. Therefore test-user-3 has READ
            // permission
            logger.info("Before userHasAccess 7...");
            logger.info(
                    "userHasAccess 7: {}",
                    sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "READ"));
            // test-user-3 does not have WRITE permission to test-experiment-2
            logger.info("Before userHasAccess 8...");
            logger.info(
                    "userHasAccess 8: {}",
                    sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "WRITE"));
            // test-user-3 has CLONE permission to test-experiment-2
            logger.info("Before userHasAccess 9...");
            logger.info(
                    "userHasAccess 9: {}",
                    sharingService.userHasAccess(domainId, "test-user-3", "test-experiment-2", "CLONE"));
            // test-user-3 does not have CLONE permission to test-file-1
            logger.info("Before userHasAccess 10...");
            logger.info(
                    "userHasAccess 10: {}",
                    sharingService.userHasAccess(domainId, "test-user-3", "test-file-1", "CLONE"));
            logger.info("After cascading permissions...");

            ArrayList<SearchCriteria> filters = new ArrayList<>();
            // ArrayList<SearchCriteria> filters = new List<>();
            SearchCriteria searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.LIKE);
            searchCriteria.setValue("experiment stampede methyl");
            // searchCriteria.setValue("stampede");
            // searchCriteria.setSearchField(EntitySearchField.NAME);
            searchCriteria.setSearchField(EntitySearchField.FULL_TEXT);
            filters.add(searchCriteria);
            searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue("READ");
            searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            filters.add(searchCriteria);
            logger.info(
                    "items READable by User2: {}",
                    sharingService
                            .searchEntities(domainId, "test-user-2", filters, 0, -1)
                            .size());
            logger.info(
                    "items READable by User2: {}",
                    sharingService.searchEntities(domainId, "test-user-2", filters, 0, -1));
            logger.info("After searchEntities...");

            User userA = new User();
            String userNameA = "UserA";
            String userIdA = "UserA";
            // required
            userA.setUserId(userIdA);
            // required
            userA.setUserName(userNameA);
            // required
            userA.setDomainId(domainId);
            // required
            userA.setFirstName("User");
            // required
            userA.setLastName("A");
            // required
            userA.setEmail("user.a@example.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[10];
            // userA.setIcon(icon);
            sharingService.createUser(userA);
            User userB = new User();
            String userNameB = "UserB";
            String userIdB = "UserB";
            // required
            userB.setUserId(userIdB);
            // required
            userB.setUserName(userNameB);
            // required
            userB.setDomainId(domainId);
            // required
            userB.setFirstName("User");
            // required
            userB.setLastName("B");
            // required
            userB.setEmail("user.b@example.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[10];
            // userB.setIcon(icon);
            sharingService.createUser(userB);
            User userC = new User();
            String userNameC = "UserC";
            String userIdC = "UserC";
            // required
            userC.setUserId(userIdC);
            // required
            userC.setUserName(userNameC);
            // required
            userC.setDomainId(domainId);
            // required
            userC.setFirstName("User");
            // required
            userC.setLastName("C");
            // required
            userC.setEmail("user.c@example.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[10];
            // userC.setIcon(icon);
            sharingService.createUser(userC);
            User userD = new User();
            String userNameD = "UserD";
            String userIdD = "UserD";
            // required
            userD.setUserId(userIdD);
            // required
            userD.setUserName(userNameD);
            // required
            userD.setDomainId(domainId);
            // required
            userD.setFirstName("User");
            // required
            userD.setLastName("D");
            // required
            userD.setEmail("user.d@example.com");
            // optional - this should be bytes of the users image icon
            // byte[] icon = new byte[10];
            // userD.setIcon(icon);
            sharingService.createUser(userD);
            logger.info("After user creation...");

            UserGroup Group1 = new UserGroup();
            // required
            Group1.setGroupId("Group1");
            // required
            Group1.setDomainId(domainId);
            // required
            Group1.setName("Group1");
            // optional
            // userGroup1.setDescription("test group description");
            // required
            Group1.setOwnerId("UserA");
            // required
            Group1.setGroupType(GroupType.USER_LEVEL_GROUP);
            sharingService.createGroup(Group1);
            logger.info("After Group1 creation...");

            sharingService.addUsersToGroup(domainId, List.of("UserB"), "Group1");
            sharingService.addUsersToGroup(domainId, List.of("UserC"), "Group1");
            sharingService.addUsersToGroup(domainId, List.of("UserD"), "Group1");

            logger.info("After adding users to Group1 creation...");

            EntityType entityTypeFolder = new EntityType();
            // required
            entityTypeFolder.setEntityTypeId("FOLDER");
            // required
            entityTypeFolder.setDomainId(domainId);
            // required
            entityTypeFolder.setName("FOLDER");
            // optional
            // entityTypeFolder.setDescription("PROJECT entity type description");
            sharingService.createEntityType(entityTypeFolder);
            logger.info("After creating FOLDER entity type...");

            EntityType entityTypeInputData = new EntityType();
            // required
            entityTypeInputData.setEntityTypeId("INPUTDATA");
            // required
            entityTypeInputData.setDomainId(domainId);
            // required
            entityTypeInputData.setName("INPUTDATA");
            // optional
            // entityTypeFolder.setDescription("PROJECT entity type description");
            sharingService.createEntityType(entityTypeInputData);
            logger.info("After creating INPUTDATA entity type...");

            Entity entityB1 = new Entity();
            // required
            entityB1.setEntityId("UserBProject1");
            // required
            entityB1.setDomainId(domainId);
            // required
            entityB1.setEntityTypeId("PROJECT");
            // required
            entityB1.setOwnerId("UserB");
            // required
            entityB1.setName("UserBProject1");
            // optional
            entityB1.setDescription("User B's Project 1");
            // optional
            entityB1.setFullText("test project 1");
            // optional - If not set this will be default to current system time
            entityB1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingService.createEntity(entityB1);
            logger.info("After creating UserBProject1 ...");

            Entity entityC1 = new Entity();
            // required
            entityC1.setEntityId("UserCProject2");
            // required
            entityC1.setDomainId(domainId);
            // required
            entityC1.setEntityTypeId("PROJECT");
            // required
            entityC1.setOwnerId("UserC");
            // required
            entityC1.setName("UserCProject2");
            // optional
            entityC1.setDescription("User C's Project 2");
            // optional
            entityC1.setFullText("test project 2");
            // optional - If not set this will be default to current system time
            entityC1.setOriginalEntityCreationTime(System.currentTimeMillis());
            sharingService.createEntity(entityC1);
            logger.info("After creating UserCProject2 ...");

            Entity entityF1 = new Entity();
            entityF1.setEntityId("Folder1");
            entityF1.setDomainId(domainId);
            entityF1.setEntityTypeId("FOLDER");
            entityF1.setOwnerId("UserB");
            entityF1.setName("UserBFolder1");
            entityF1.setDescription("UserB's Folder 1");
            entityF1.setParentEntityId("UserBProject1");
            entityF1.setFullText("test experiment 1 ethidium");
            sharingService.createEntity(entityF1);
            logger.info("After creating Folder1 ...");

            Entity entityD1 = new Entity();
            entityD1.setEntityId("Data1");
            entityD1.setDomainId(domainId);
            entityD1.setEntityTypeId("INPUTDATA");
            entityD1.setOwnerId("UserB");
            entityD1.setName("UserBData1");
            entityD1.setDescription("UserB's Data 1");
            entityD1.setParentEntityId("Folder1");
            entityD1.setFullText("Data 1 for User B Folder 1");
            sharingService.createEntity(entityD1);
            logger.info("After creating Data1 ...");

            Entity entityF2 = new Entity();
            entityF2.setEntityId("Folder2");
            entityF2.setDomainId(domainId);
            entityF2.setEntityTypeId("FOLDER");
            entityF2.setOwnerId("UserC");
            entityF2.setName("UserCFolder2");
            entityF2.setDescription("UserC's Folder 2");
            entityF2.setParentEntityId("UserCProject2");
            entityF2.setFullText("test experiment 2 ethidium");
            sharingService.createEntity(entityF2);
            logger.info("After creating Folder2 ...");

            Entity entityD2 = new Entity();
            entityD2.setEntityId("Data2");
            entityD2.setDomainId(domainId);
            entityD2.setEntityTypeId("INPUTDATA");
            entityD2.setOwnerId("UserC");
            entityD2.setName("UserCData2");
            entityD2.setDescription("UserC's Data 2");
            entityD2.setParentEntityId("Folder2");
            entityD2.setFullText("Data 2 for User C Folder 1");
            sharingService.createEntity(entityD2);
            logger.info("After creating Data2 ...");

            // sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"),
            // "READ", true);
            time = System.currentTimeMillis();
            sharingService.shareEntityWithGroups(domainId, "Folder1", List.of("Group1"), "READ", true);
            logger.info("Time for sharing " + (System.currentTimeMillis() - time));
            logger.info("After READ sharing UserBFolder1 with Group1 ...");
            // sharingServiceClient.shareEntityWithGroups(domainId, "Folder2", Arrays.asList("Group1"), "READ", true);
            // logger.info("After READ sharing UserCFolder2 with Group1 ...");

            Entity entityD3 = new Entity();
            entityD3.setEntityId("Data3");
            entityD3.setDomainId(domainId);
            entityD3.setEntityTypeId("INPUTDATA");
            entityD3.setOwnerId("UserC");
            entityD3.setName("UserCData3");
            entityD3.setDescription("UserC's Data 3");
            entityD3.setParentEntityId("Folder2");
            entityD3.setFullText("Data 3 for User C Folder 2");
            sharingService.createEntity(entityD3);
            logger.info("After creating Data3 ...");

            logger.info("Does UserC have READ access to Data1 ...");
            logger.info("userHasAccess 11: {}", sharingService.userHasAccess(domainId, "UserC", "Data1", "READ"));
            logger.info("Does UserC have READ access to Data2 ...");
            logger.info("userHasAccess 12: {}", sharingService.userHasAccess(domainId, "UserC", "Data2", "READ"));
            logger.info("Does UserC have READ access to Data3 ...");
            logger.info("userHasAccess 13: {}", sharingService.userHasAccess(domainId, "UserC", "Data3", "READ"));
            logger.info("Does UserB have READ access to Data3 ...");
            logger.info("userHasAccess 14: {}", sharingService.userHasAccess(domainId, "UserB", "Data3", "READ"));

            ArrayList<SearchCriteria> sharedfilters = new ArrayList<>();
            searchCriteria = new SearchCriteria();
            searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setValue("READ");
            searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
            sharedfilters.add(searchCriteria);
            logger.info(
                    "size of items READable by UserC: {}",
                    sharingService
                            .searchEntities(domainId, "UserC", sharedfilters, 0, -1)
                            .size());
            logger.info(
                    "items READable by UserC: {}",
                    sharingService.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            searchCriteria = new SearchCriteria();
            // searchCriteria.setSearchCondition(SearchCondition.EQUAL);
            searchCriteria.setSearchCondition(SearchCondition.NOT);
            searchCriteria.setValue("UserC");
            searchCriteria.setSearchField(EntitySearchField.OWNER_ID);
            sharedfilters.add(searchCriteria);
            logger.info(
                    "size of items READable, and not owned by UserC: {}",
                    sharingService
                            .searchEntities(domainId, "UserC", sharedfilters, 0, -1)
                            .size());
            logger.info(
                    "items READable by UserC: {}",
                    sharingService.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            logger.info("After searchEntities 2...");

            logger.info(
                    "items READable by UserC: {}",
                    sharingService
                            .searchEntities(domainId, "UserC", sharedfilters, 0, -1)
                            .size());
            logger.info(
                    "items READable by UserC: {}",
                    sharingService.searchEntities(domainId, "UserC", sharedfilters, 0, -1));
            logger.info("After searchEntities 2...");

            sharingService.removeUsersFromGroup(domainId, List.of("UserD"), "Group1");
            logger.info("After removing UserD from Group1 ...");

            sharingService.deleteGroup(domainId, "Group1");
            logger.info("After deleting Group1 ...");

            logger.info("End of try clause...");
        } catch (SharingRegistryException ex2) {
            logger.error("SharingRegistryException in CipresTest", ex2);
        }
    }
}
