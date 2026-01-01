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
package org.apache.airavata.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.airavata.common.model.GatewayGroups;
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.SharingRegistryService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.sharing.model.GroupCardinality;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            GatewayGroupsInitializerTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "security.tls.enabled=true",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class GatewayGroupsInitializerTest {
    public static final String GATEWAY_ID = "test-gateway";
    public static final String IDENTITY_SERVER_PWD_CRED_TOKEN = "identity-server-pwd-cred-token";
    public static final String TEST_ADMIN_USERNAME = "test-admin-username";
    public static final String ADMIN_OWNER_ID = TEST_ADMIN_USERNAME + "@" + GATEWAY_ID;

    @MockitoBean
    private RegistryService mockRegistryService;

    @MockitoBean
    private SharingRegistryService mockSharingRegistryService;

    @MockitoBean
    private CredentialStoreService mockCredentialStoreService;

    private GatewayGroupsInitializer gatewayGroupsInitializer;

    @BeforeEach
    public void setUp() {
        gatewayGroupsInitializer = new GatewayGroupsInitializer(
                mockRegistryService, mockSharingRegistryService, mockCredentialStoreService);
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
        GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
        gatewayResourceProfile.setGatewayID(GATEWAY_ID);
        gatewayResourceProfile.setIdentityServerPwdCredToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setLoginUserName(TEST_ADMIN_USERNAME);
        passwordCredential.setGatewayId(GATEWAY_ID);
        passwordCredential.setToken(IDENTITY_SERVER_PWD_CRED_TOKEN);

        try {
            when(mockRegistryService.getGatewayResourceProfile(GATEWAY_ID)).thenReturn(gatewayResourceProfile);
            when(mockCredentialStoreService.getPasswordCredential(IDENTITY_SERVER_PWD_CRED_TOKEN, GATEWAY_ID))
                    .thenReturn(passwordCredential);
            when(mockSharingRegistryService.isUserExists(GATEWAY_ID, ADMIN_OWNER_ID))
                    .thenReturn(doesAdminUserExist);

            GatewayGroups gatewayGroups = gatewayGroupsInitializer.initialize(GATEWAY_ID);
            assertEquals(GATEWAY_ID, gatewayGroups.getGatewayId());

            if (!doesAdminUserExist) {
                verify(mockSharingRegistryService, atLeastOnce()).createUser(argThat(user -> {
                    assertEquals(ADMIN_OWNER_ID, user.getUserId());
                    assertEquals(TEST_ADMIN_USERNAME, user.getUserName());
                    assertEquals(GATEWAY_ID, user.getDomainId());
                    return true;
                }));
            }

            verify(mockSharingRegistryService, atLeastOnce()).createGroup(argThat(group -> {
                assertEquals(GATEWAY_ID, group.getDomainId());
                assertEquals(ADMIN_OWNER_ID, group.getOwnerId());
                assertEquals(GroupCardinality.MULTI_USER, group.getGroupCardinality());
                return true;
            }));
        } catch (SharingRegistryException | RegistryServiceException | CredentialStoreException e) {
            throw new RuntimeException("Failed to initialize gateway groups", e);
        }
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.security",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
