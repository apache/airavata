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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.registry.entities.expcatalog.UserPK;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, UserRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class UserRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            useDefaultFilters = false,
            includeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.(monitor|helix|sharing\\.migrator|credential|profile|security|accountprovisioning)\\..*"),
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.REGEX,
                        pattern = "org\\.apache\\.airavata\\.service\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({org.apache.airavata.config.AiravataPropertiesConfiguration.class, org.apache.airavata.config.DozerMapperConfig.class})
    static class TestConfiguration {}

    private final GatewayService gatewayService;
    private final UserService userService;

    private String gatewayId;
    private String gatewayId2;

    public UserRepositoryTest(GatewayService gatewayService, UserService userService) {
        super(Database.EXP_CATALOG);
        this.gatewayService = gatewayService;
        this.userService = userService;
    }

    @BeforeEach
    public void createTestData() throws RegistryException {

        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gatewayId = gatewayService.addGateway(gateway);

        Gateway gateway2 = new Gateway();
        gateway2.setGatewayId("gateway2");
        gatewayId2 = gatewayService.addGateway(gateway2);
    }

    @AfterEach
    public void deleteTestData() throws RegistryException {

        gatewayService.removeGateway(gatewayId);
        gatewayService.removeGateway(gatewayId2);
    }

    @Test
    public void test() throws RegistryException {

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId("username");
        userProfile.setAiravataInternalUserId("username@" + gatewayId);
        userProfile.setGatewayId(gatewayId);

        userService.addUser(userProfile);
        UserProfile retrievedUserProfile = userService.get(new UserPK(gatewayId, "username"));
        assertEquals("username", retrievedUserProfile.getUserId());
        assertEquals("username@" + gatewayId, retrievedUserProfile.getAiravataInternalUserId());
        assertEquals(gatewayId, retrievedUserProfile.getGatewayId());

        userService.delete(new UserPK(gatewayId, "username"));
    }

    @Test
    public void testGetAllUsernamesInGateway() throws RegistryException {

        // Two users in first gateway, only one in the second gateway
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

        userService.delete(new UserPK(gatewayId, username1));
        userService.delete(new UserPK(gatewayId, username2));
        userService.delete(new UserPK(gatewayId2, username3));
    }
}
