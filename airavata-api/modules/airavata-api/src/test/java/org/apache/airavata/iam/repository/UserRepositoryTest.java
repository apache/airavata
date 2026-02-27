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
package org.apache.airavata.iam.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.airavata.config.ServiceIntegrationTestBase;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.iam.model.UserProfile;
import org.apache.airavata.iam.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for UserRepository.
 * Uses ServiceIntegrationTestBase for the broader context that includes UserService.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UserRepositoryTest extends ServiceIntegrationTestBase {

    private final GatewayService gatewayService;
    private final UserService userService;

    // Gateway IDs
    private String gatewayId;
    private String gatewayId2;

    public UserRepositoryTest(GatewayService gatewayService, UserService userService) {
        this.gatewayService = gatewayService;
        this.userService = userService;
    }

    @BeforeEach
    public void createTestData() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gatewayId = gatewayService.createGateway(gateway);

        Gateway gateway2 = new Gateway();
        gateway2.setGatewayId("gateway2");
        gatewayId2 = gatewayService.createGateway(gateway2);
    }

    @AfterEach
    public void deleteTestData() throws Exception {
        gatewayService.deleteGateway(gatewayId);
        gatewayService.deleteGateway(gatewayId2);
    }

    @Test
    public void testUserCrudOperations() throws Exception {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("username");
        userProfile.setAiravataInternalUserId("username@" + gatewayId);
        userProfile.setGatewayId(gatewayId);

        userService.addUser(userProfile);
        UserProfile retrievedUserProfile = userService.get("username", gatewayId);
        assertEquals("username", retrievedUserProfile.getUserId());
        assertEquals("username@" + gatewayId, retrievedUserProfile.getAiravataInternalUserId());
        assertEquals(gatewayId, retrievedUserProfile.getGatewayId());

        userService.delete("username", gatewayId);
    }

    @Test
    public void testGetAllUsernamesInGateway() throws Exception {
        String username1 = "username1";
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(username1);
        userProfile.setAiravataInternalUserId(username1 + "@" + gatewayId);
        userProfile.setGatewayId(gatewayId);
        userService.addUser(userProfile);

        String username2 = "username2";
        UserProfile userProfile2 = new UserProfile();
        userProfile2.setUserId(username2);
        userProfile2.setAiravataInternalUserId(username2 + "@" + gatewayId);
        userProfile2.setGatewayId(gatewayId);
        userService.addUser(userProfile2);

        String username3 = "username3";
        UserProfile userProfile3 = new UserProfile();
        userProfile3.setUserId(username3);
        userProfile3.setAiravataInternalUserId(username3 + "@" + gatewayId2);
        userProfile3.setGatewayId(gatewayId2);
        userService.addUser(userProfile3);

        List<String> gateway1Usernames = userService.getAllUsernamesInGateway(gatewayId);
        assertEquals(2, gateway1Usernames.size());
        assertEquals(new HashSet<>(Arrays.asList(username1, username2)), new HashSet<>(gateway1Usernames));

        List<String> gateway2Usernames = userService.getAllUsernamesInGateway(gatewayId2);
        assertEquals(1, gateway2Usernames.size());
        assertEquals(Collections.singleton(username3), new HashSet<>(gateway2Usernames));

        userService.delete(username1, gatewayId);
        userService.delete(username2, gatewayId);
        userService.delete(username3, gatewayId2);
    }
}
