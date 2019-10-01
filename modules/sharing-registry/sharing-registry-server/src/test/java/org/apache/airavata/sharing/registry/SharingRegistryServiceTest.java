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
import org.apache.airavata.sharing.registry.server.SharingRegistryServer;
import org.apache.airavata.sharing.registry.service.cpi.SharingRegistryService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

public class SharingRegistryServiceTest {
    private final static Logger logger = LoggerFactory.getLogger(SharingRegistryServiceTest.class);

    @BeforeClass
    public static void setUp() throws Exception {
        SharingRegistryServer server = new SharingRegistryServer();
        server.setTestMode(true);
        server.start();
        Thread.sleep(1000 * 2);
    }


    @Test
    public void test() throws TException, InterruptedException, ApplicationSettingsException {
        String serverHost = "localhost";
        int serverPort = 7878;

        SharingRegistryService.Client sharingServiceClient;
        if (!ServerSettings.isSharingTLSEnabled()) {
            TTransport transport = new TSocket(serverHost, serverPort);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            sharingServiceClient = new SharingRegistryService.Client(protocol);
        }else{
            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setKeyStore(ServerSettings.getKeyStorePath(), ServerSettings.getKeyStorePassword());
            params.setTrustStore(ServerSettings.getTrustStorePath(), ServerSettings.getTrustStorePassword());

            TTransport transport = TSSLTransportFactory.getClientSocket(serverHost, serverPort, 10000, params);
            TProtocol protocol = new TBinaryProtocol(transport);
            sharingServiceClient = new SharingRegistryService.Client(protocol);
        }

        Domain domain = new Domain();
        //has to be one word
        domain.setName("test-domain" + Math.random());
        //optional
        domain.setDescription("test domain description");

        String domainId = sharingServiceClient.createDomain(domain);
        Assert.assertTrue(sharingServiceClient.isDomainExists(domainId));

        User user1 = new User();
        //required
        user1.setUserId("test-user-1");
        //required
        user1.setUserName("test-user-1");
        //required
        user1.setDomainId(domainId);
        //required
        user1.setFirstName("John");
        //required
        user1.setLastName("Doe");
        //required
        user1.setEmail("john.doe@abc.com");
        //optional - this should be bytes of the users image icon
        //byte[] icon1 = new byte[10];
        //user1.setIcon(icon1);

        sharingServiceClient.createUser(user1);
        Assert.assertTrue(sharingServiceClient.isUserExists(domainId, "test-user-1"));

        User user2 = new User();
        //required
        user2.setUserId("test-user-2");
        //required
        user2.setUserName("test-user-2");
        //required
        user2.setDomainId(domainId);
        //required
        user2.setFirstName("John");
        //required
        user2.setLastName("Doe");
        //required
        user2.setEmail("john.doe@abc.com");
        //optional - this should be bytes of the users image icon
        //byte[] icon2 = new byte[10];
        //user2.setIcon(icon2);

        sharingServiceClient.createUser(user2);

        User user3 = new User();
        //required
        user3.setUserId("test-user-3");
        //required
        user3.setUserName("test-user-3");
        //required
        user3.setDomainId(domainId);
        //required
        user3.setFirstName("John");
        //required
        user3.setLastName("Doe");
        //required
        user3.setEmail("john.doe@abc.com");
        //optional - this should be bytes of the users image icon
        //byte[] icon3 = new byte[10];
        //user3.setIcon(icon3);

        sharingServiceClient.createUser(user3);

        User user7 = new User();
        //required
        user7.setUserId("test-user-7");
        //required
        user7.setUserName("test-user-7");
        //required
        user7.setDomainId(domainId);
        //required
        user7.setFirstName("John");
        //required
        user7.setLastName("Doe");
        //required
        user7.setEmail("john.doe@abc.com");
        //optional - this should be bytes of the users image icon
        //byte[] icon3 = new byte[10];
        //user3.setIcon(icon3);

        sharingServiceClient.createUser(user7);

        // Test updates to user and how it affects the user's SINGLE_USER group
        UserGroup singleUserGroupUser7 = sharingServiceClient.getGroup(domainId, user7.getUserId());
        Assert.assertEquals(GroupCardinality.SINGLE_USER, singleUserGroupUser7.getGroupCardinality());
        user7.setFirstName("Johnny");
        sharingServiceClient.updatedUser(user7);
        User updatedUser7 = sharingServiceClient.getUser(domainId, user7.getUserId());
        Assert.assertEquals("Johnny", updatedUser7.getFirstName());
        singleUserGroupUser7 = sharingServiceClient.getGroup(domainId, user7.getUserId());
        Assert.assertEquals(GroupCardinality.SINGLE_USER, singleUserGroupUser7.getGroupCardinality());

        UserGroup userGroup1 = new UserGroup();
        //required
        userGroup1.setGroupId("test-group-1");
        //required
        userGroup1.setDomainId(domainId);
        //required
        userGroup1.setName("test-group-1");
        //optional
        userGroup1.setDescription("test group description");
        //required
        userGroup1.setOwnerId("test-user-1");
        //required
        userGroup1.setGroupType(GroupType.USER_LEVEL_GROUP);

        sharingServiceClient.createGroup(userGroup1);
        userGroup1.setDescription("updated description");
        sharingServiceClient.updateGroup(userGroup1);
        Assert.assertTrue(sharingServiceClient.getGroup(domainId, userGroup1.getGroupId()).getDescription().equals("updated description"));
        Assert.assertTrue(sharingServiceClient.isGroupExists(domainId, "test-group-1"));

        UserGroup userGroup2 = new UserGroup();
        //required
        userGroup2.setGroupId("test-group-2");
        //required
        userGroup2.setDomainId(domainId);
        //required
        userGroup2.setName("test-group-2");
        //optional
        userGroup2.setDescription("test group description");
        //required
        userGroup2.setOwnerId("test-user-2");
        //required
        userGroup2.setGroupType(GroupType.USER_LEVEL_GROUP);

        sharingServiceClient.createGroup(userGroup2);

        sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("test-user-3"), "test-group-2");

        sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("test-user-7"), "test-group-1");

        sharingServiceClient.addChildGroupsToParentGroup(domainId, Arrays.asList("test-group-2"), "test-group-1");

        //Group roles
        Assert.assertTrue(sharingServiceClient.hasOwnerAccess(domainId, "test-group-1", "test-user-1"));

        // user has admin access
        Assert.assertTrue(sharingServiceClient.addGroupAdmins(domainId, "test-group-1", Arrays.asList("test-user-7")));
        Assert.assertTrue(sharingServiceClient.hasAdminAccess(domainId, "test-group-1", "test-user-7"));

        UserGroup getGroup = sharingServiceClient.getGroup(domainId, "test-group-1");
        Assert.assertTrue(getGroup.getGroupAdmins().size() == 1);

        Assert.assertTrue(sharingServiceClient.removeGroupAdmins(domainId, "test-group-1", Arrays.asList("test-user-7")));
        Assert.assertFalse(sharingServiceClient.hasAdminAccess(domainId, "test-group-1", "test-user-7"));

        // transfer group ownership
        sharingServiceClient.addUsersToGroup(domainId, Arrays.asList("test-user-2"), "test-group-1");
        Assert.assertTrue(sharingServiceClient.transferGroupOwnership(domainId, "test-group-1", "test-user-2"));
        Assert.assertTrue(sharingServiceClient.hasOwnerAccess(domainId, "test-group-1", "test-user-2"));
        Assert.assertTrue(sharingServiceClient.transferGroupOwnership(domainId, "test-group-1", "test-user-1"));
        Assert.assertFalse(sharingServiceClient.hasOwnerAccess(domainId, "test-group-1", "test-user-2"));

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
        Assert.assertTrue(sharingServiceClient.isPermissionExists(domainId, "READ"));

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
        Assert.assertTrue(sharingServiceClient.isEntityTypeExists(domainId, "PROJECT"));

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


        //Creating entities
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
        Assert.assertTrue(sharingServiceClient.isEntityExists(domainId, "test-project-1"));

        Entity entity2 = new Entity();
        entity2.setEntityId("test-experiment-1");
        entity2.setDomainId(domainId);
        entity2.setEntityTypeId("EXPERIMENT");
        entity2.setOwnerId("test-user-1");
        entity2.setName("test-experiment-1");
        entity2.setDescription("test experiment 1 description");
        entity2.setParentEntityId("test-project-1");
        entity2.setFullText("test experiment 1 benzene");
        sharingServiceClient.createEntity(entity2);


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

        Assert.assertTrue(sharingServiceClient.getEntity(domainId, "test-project-1").getSharedCount() == 0);
        sharingServiceClient.shareEntityWithUsers(domainId, "test-project-1", Arrays.asList("test-user-2"), "WRITE", true);
        Assert.assertTrue(sharingServiceClient.getEntity(domainId, "test-project-1").getSharedCount() == 1);
        ArrayList<SearchCriteria> filters = new ArrayList<>();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setSearchField(EntitySearchField.SHARED_COUNT);
        searchCriteria.setValue("1");
        searchCriteria.setSearchCondition(SearchCondition.GTE);
        filters.add(searchCriteria);
        Assert.assertEquals(1, sharingServiceClient.searchEntities(domainId, "test-user-2", filters, 0, -1).size());


        sharingServiceClient.revokeEntitySharingFromUsers(domainId, "test-project-1", Arrays.asList("test-user-2"), "WRITE");
        Assert.assertTrue(sharingServiceClient.getEntity(domainId, "test-project-1").getSharedCount() == 0);
        sharingServiceClient.shareEntityWithUsers(domainId, "test-project-1", Arrays.asList("test-user-2"), "WRITE", true);

        sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"), "READ", true);
        sharingServiceClient.shareEntityWithGroups(domainId, "test-experiment-2", Arrays.asList("test-group-2"), "CLONE", false);

        //true
        Assert.assertTrue(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-project-1", "WRITE"));
        //true
        Assert.assertTrue(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-1", "WRITE"));
        //true
        Assert.assertTrue(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-2", "WRITE"));

        //false
        Assert.assertFalse(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-1", "READ"));
        //true
        Assert.assertTrue(sharingServiceClient.userHasAccess(domainId, "test-user-2", "test-experiment-2", "READ"));

        //false
        Assert.assertFalse(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-project-1", "READ"));
        //true
        Assert.assertTrue(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "READ"));
        //false
        Assert.assertFalse(sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "WRITE"));

        //true
        Assert.assertTrue((sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-experiment-2", "CLONE")));
        //false
        Assert.assertFalse((sharingServiceClient.userHasAccess(domainId, "test-user-3", "test-file-1", "CLONE")));

        filters = new ArrayList<>();
        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.FULL_TEXT);
        searchCriteria.setValue("experiment");
        searchCriteria.setSearchField(EntitySearchField.FULL_TEXT);
        filters.add(searchCriteria);

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue("EXPERIMENT");
        searchCriteria.setSearchField(EntitySearchField.ENTITY_TYPE_ID);
        filters.add(searchCriteria);

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.EQUAL);
        searchCriteria.setValue("READ");
        searchCriteria.setSearchField(EntitySearchField.PERMISSION_TYPE_ID);
        filters.add(searchCriteria);

        Assert.assertTrue(sharingServiceClient.searchEntities(domainId, "test-user-2", filters, 0, -1).size() == 1);
        Entity persistedEntity = sharingServiceClient.searchEntities(
                domainId, "test-user-2", filters, 0, -1).get(0);
        Assert.assertEquals(entity3.getName(), persistedEntity.getName());
        Assert.assertEquals(entity3.getDescription(), persistedEntity.getDescription());
        Assert.assertEquals(entity3.getFullText(), persistedEntity.getFullText());

        searchCriteria = new SearchCriteria();
        searchCriteria.setSearchCondition(SearchCondition.NOT);
        searchCriteria.setValue("test-user-1");
        searchCriteria.setSearchField(EntitySearchField.OWNER_ID);
        filters.add(searchCriteria);
        Assert.assertTrue(sharingServiceClient.searchEntities(domainId, "test-user-2", filters, 0, -1).size() == 0);
    }
}
