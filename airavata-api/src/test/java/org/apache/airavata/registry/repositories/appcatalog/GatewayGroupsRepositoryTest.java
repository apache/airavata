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
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class GatewayGroupsRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
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
