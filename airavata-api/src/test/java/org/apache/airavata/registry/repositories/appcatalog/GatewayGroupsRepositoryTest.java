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
package org.apache.airavata.registry.repositories.appcatalog;

import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.GatewayGroupsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestConstructor;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, GatewayGroupsRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true"
        ,
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false",
            "services.airavata.enabled=false",
            "services.registryService.enabled=false",
            "services.userprofile.enabled=false",
            "services.groupmanager.enabled=false",
            "services.iam.enabled=false",
            "services.orchestrator.enabled=false",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GatewayGroupsRepositoryTest extends TestBase {

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

    private static final String GATEWAY_ID = "gateway-id";
    private static final String ADMIN_GROUPS_ID = "admin-groups-id";
    private static final String READ_ONLY_ADMINS_GROUP_ID = "read-only-admins-group-id";
    private static final String DEFAULT_GATEWAY_USERS_GROUP_ID = "default-gateway-users-group-id";

    private final GatewayGroupsService gatewayGroupsService;

    public GatewayGroupsRepositoryTest(GatewayGroupsService gatewayGroupsService) {
        super(Database.APP_CATALOG);
        this.gatewayGroupsService = gatewayGroupsService;
    }

    @Test
    public void testCreateAndRetrieveGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayGroupsService.create(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayGroupsService.get(GATEWAY_ID);

        Assertions.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assertions.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assertions.assertEquals(DEFAULT_GATEWAY_USERS_GROUP_ID, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assertions.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayGroupsService.delete(GATEWAY_ID);
    }

    @Test
    public void testUpdateGatewayGroups() throws Exception {

        GatewayGroups gatewayGroups = new GatewayGroups();
        gatewayGroups.setGatewayId(GATEWAY_ID);
        gatewayGroups.setAdminsGroupId(ADMIN_GROUPS_ID);
        gatewayGroups.setReadOnlyAdminsGroupId(READ_ONLY_ADMINS_GROUP_ID);
        gatewayGroups.setDefaultGatewayUsersGroupId(DEFAULT_GATEWAY_USERS_GROUP_ID);

        gatewayGroupsService.create(gatewayGroups);

        final String defaultGatewayUsersGroupId = "some-other-group-id";
        gatewayGroups.setDefaultGatewayUsersGroupId(defaultGatewayUsersGroupId);

        gatewayGroupsService.update(gatewayGroups);

        GatewayGroups retrievedGatewayGroups = gatewayGroupsService.get(GATEWAY_ID);

        Assertions.assertEquals(ADMIN_GROUPS_ID, retrievedGatewayGroups.getAdminsGroupId());
        Assertions.assertEquals(READ_ONLY_ADMINS_GROUP_ID, retrievedGatewayGroups.getReadOnlyAdminsGroupId());
        Assertions.assertEquals(defaultGatewayUsersGroupId, retrievedGatewayGroups.getDefaultGatewayUsersGroupId());
        Assertions.assertEquals(gatewayGroups, retrievedGatewayGroups);

        gatewayGroupsService.delete(GATEWAY_ID);
    }
}
