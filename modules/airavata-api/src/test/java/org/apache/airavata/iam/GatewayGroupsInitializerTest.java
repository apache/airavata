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
package org.apache.airavata.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.airavata.compute.resource.adapter.ResourceProfileAdapter;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.service.GatewayService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.GroupCardinality;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.iam.service.GatewayGroupsInitializer;
import org.apache.airavata.iam.service.SharingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfiguration.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            GatewayGroupsInitializerTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.lazy-initialization=true",
            "security.tls.enabled=true",
            "security.iam.enabled=false",
            "security.manager.enabled=false",
            "flyway.enabled=false",
            "services.scheduler.rescheduler.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        org.apache.airavata.config.ServerProperties.class)
public class GatewayGroupsInitializerTest {
    public static final String GATEWAY_ID = "test-gateway";
    public static final String TEST_ADMIN_USERNAME = "test-admin-username";
    public static final String ADMIN_OWNER_ID = TEST_ADMIN_USERNAME + "@" + GATEWAY_ID;

    @MockitoBean
    private GatewayService mockGatewayGroupsService;

    @MockitoBean
    private ResourceProfileAdapter mockResourceProfileAdapter;

    @MockitoBean
    private SharingService mockSharingService;

    @MockitoBean
    private CredentialStoreService mockCredentialStoreService;

    private GatewayGroupsInitializer gatewayGroupsInitializer;

    @BeforeEach
    public void setUp() {
        var superAdmin = org.mockito.Mockito.mock(
                org.apache.airavata.config.ServerProperties.Security.Iam.Super.class);
        org.mockito.Mockito.when(superAdmin.username()).thenReturn(TEST_ADMIN_USERNAME);

        var iam = org.mockito.Mockito.mock(
                org.apache.airavata.config.ServerProperties.Security.Iam.class);
        org.mockito.Mockito.when(iam.superAdmin()).thenReturn(superAdmin);

        var security = org.mockito.Mockito.mock(
                org.apache.airavata.config.ServerProperties.Security.class);
        org.mockito.Mockito.when(security.iam()).thenReturn(iam);

        var mockProperties = org.mockito.Mockito.mock(
                org.apache.airavata.config.ServerProperties.class);
        org.mockito.Mockito.when(mockProperties.security()).thenReturn(security);

        gatewayGroupsInitializer = new GatewayGroupsInitializer(
                mockGatewayGroupsService,
                mockSharingService,
                mockProperties);
    }

    @Test
    public void testWithoutAdminUser() {
        runTest(false);
    }

    @Test
    public void testWithAdminUser() {
        runTest(true);
    }

    private void runTest(boolean doesAdminUserExist) {
        try {
            when(mockSharingService.isUserExists(GATEWAY_ID, ADMIN_OWNER_ID))
                    .thenReturn(doesAdminUserExist);

            GatewayGroups gatewayGroups = gatewayGroupsInitializer.initialize(GATEWAY_ID);
            assertEquals(GATEWAY_ID, gatewayGroups.getGatewayId());

            if (!doesAdminUserExist) {
                verify(mockSharingService, atLeastOnce()).createUser(argThat(user -> {
                    assertEquals(ADMIN_OWNER_ID, user.getUserId());
                    assertEquals(TEST_ADMIN_USERNAME, user.getUserName());
                    assertEquals(GATEWAY_ID, user.getDomainId());
                    return true;
                }));
            }

            verify(mockSharingService, atLeastOnce()).createGroup(argThat(group -> {
                assertEquals(GATEWAY_ID, group.getDomainId());
                assertEquals(ADMIN_OWNER_ID, group.getOwnerId());
                assertEquals(GroupCardinality.MULTI_USER, group.getGroupCardinality());
                return true;
            }));
        } catch (SharingRegistryException | RegistryException e) {
            throw new RuntimeException("Failed to initialize gateway groups", e);
        }
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.iam",
                "org.apache.airavata.config",
                "org.apache.airavata.util",
                "org.apache.airavata.user"
            })
    static class TestConfiguration {}
}
