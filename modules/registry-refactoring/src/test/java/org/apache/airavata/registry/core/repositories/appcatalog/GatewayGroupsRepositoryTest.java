/*
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
 *
 */

package org.apache.airavata.registry.core.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayGroupsRepositoryTest extends TestBase {

    private static final String GATEWAY_ID = "gateway-id";
    private static final String ADMIN_GROUPS_ID = "admin-groups-id";
    private static final String READ_ONLY_ADMINS_GROUP_ID = "read-only-admins-group-id";
    private static final String DEFAULT_GATEWAY_USERS_GROUP_ID = "default-gateway-users-group-id";

    private GatewayGroupsRepository gatewayGroupsRepository;
    private static final Logger logger = LoggerFactory.getLogger(GatewayProfileRepositoryTest.class);

    public GatewayGroupsRepositoryTest() {
        super(Database.APP_CATALOG);
        gatewayGroupsRepository = new GatewayGroupsRepository();
    }

    @Test
    public void testCreateAndRetrieveGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayGroupsRepository.create(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayGroupsRepository.get(GATEWAY_ID);

        Assert.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assert.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assert.assertEquals(DEFAULT_GATEWAY_USERS_GROUP_ID, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assert.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayGroupsRepository.delete(GATEWAY_ID);
    }

    @Test
    public void testUpdateGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayGroupsRepository.create(gatewayGroups);

        final String defaultGatewayUsersGroupId = "some-other-group-id";
        gatewayGroups.setDefaultGatewayUsersGroupId(defaultGatewayUsersGroupId);

        gatewayGroupsRepository.update(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayGroupsRepository.get(GATEWAY_ID);

        Assert.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assert.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assert.assertEquals(defaultGatewayUsersGroupId, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assert.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayGroupsRepository.delete(GATEWAY_ID);
    }


}
