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

package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.core.entities.expcatalog.UserPK;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

    GatewayRepository gatewayRepository;
    UserRepository userRepository;
    private String gatewayId;
    private String gatewayId2;


    public UserRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        userRepository = new UserRepository();
    }

    @Before
    public void createTestData() throws RegistryException {

        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gatewayId = gatewayRepository.addGateway(gateway);

        Gateway gateway2 = new Gateway();
        gateway2.setGatewayId("gateway2");
        gatewayId2 = gatewayRepository.addGateway(gateway2);
    }

    @After
    public void deleteTestData() throws RegistryException {

        gatewayRepository.removeGateway(gatewayId);
        gatewayRepository.removeGateway(gatewayId2);
    }

    @Test
    public void test() throws RegistryException {

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("username");
        userProfile.setAiravataInternalUserId("username@" + gatewayId);
        userProfile.setGatewayId(gatewayId);

        userRepository.addUser(userProfile);
        UserProfile retrievedUserProfile = userRepository.get(new UserPK(gatewayId, "username"));
        assertEquals("username", retrievedUserProfile.getUserId());
        assertEquals("username@" + gatewayId, retrievedUserProfile.getAiravataInternalUserId());
        assertEquals(gatewayId, retrievedUserProfile.getGatewayId());

        userRepository.delete(new UserPK(gatewayId, "username"));
    }

    @Test
    public void testGetAllUsernamesInGateway() throws RegistryException {

        // Two users in first gateway, only one in the second gateway
        String username1 = "username1";
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(username1);
        userProfile.setAiravataInternalUserId(username1 + "@" + gatewayId);
        userProfile.setGatewayId(gatewayId);
        userRepository.addUser(userProfile);

        String username2 = "username2";
        UserProfile userProfile2 = new UserProfile();
        userProfile2.setUserId(username2);
        userProfile2.setAiravataInternalUserId(username2 + "@" + gatewayId);
        userProfile2.setGatewayId(gatewayId);
        userRepository.addUser(userProfile2);

        String username3 = "username3";
        UserProfile userProfile3 = new UserProfile();
        userProfile3.setUserId(username3);
        userProfile3.setAiravataInternalUserId(username3 + "@" + gatewayId2);
        userProfile3.setGatewayId(gatewayId2);
        userRepository.addUser(userProfile3);

        List<String> gateway1Usernames = userRepository.getAllUsernamesInGateway(gatewayId);
        assertEquals(2, gateway1Usernames.size());
        assertEquals(new HashSet<>(Arrays.asList(username1, username2)), new HashSet<>(gateway1Usernames));

        List<String> gateway2Usernames = userRepository.getAllUsernamesInGateway(gatewayId2);
        assertEquals(1, gateway2Usernames.size());
        assertEquals(Collections.singleton(username3), new HashSet<>(gateway2Usernames));


        userRepository.delete(new UserPK(gatewayId, username1));
        userRepository.delete(new UserPK(gatewayId, username2));
        userRepository.delete(new UserPK(gatewayId2, username3));
    }


}
