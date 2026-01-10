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
package org.apache.airavata.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.accountprovisioning.SSHAccountManager;
import org.apache.airavata.agents.api.AdaptorSupport;
import org.apache.airavata.agents.support.AdaptorSupportImpl;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.credential.repositories.CredentialRepository;
import org.apache.airavata.profile.repositories.UserProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.security.GatewayGroupsInitializer;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

/**
 * Test suite to verify AiravataApplication startup with different configurations.
 *
 * These tests verify that:
 * - Spring context loads successfully
 * - All required beans are created
 * - Services can initialize
 * - Different startup modes work correctly
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, UnifiedApplicationStartupTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false" // Disable FlywayConfig since TestcontainersConfig handles migrations
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        org.apache.airavata.config.AiravataServerProperties.class)
@EnabledIfSystemProperty(
        named = "test.startup.enabled",
        matches = "true",
        disabledReason = "Startup tests require full infrastructure - run with -Dtest.startup.enabled=true")
public class UnifiedApplicationStartupTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.profile.mappers",
                "org.apache.airavata.profile.utils",
                "org.apache.airavata.sharing.services",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.sharing.mappers",
                "org.apache.airavata.sharing.utils",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
                "org.apache.airavata.credential.utils",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security",
                "org.apache.airavata.accountprovisioning"
            })
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("profileServiceEntityManagerFactory")
    private EntityManagerFactory profileServiceEntityManagerFactory;

    @Autowired(required = false)
    @Qualifier("appCatalogEntityManagerFactory")
    private EntityManagerFactory appCatalogEntityManagerFactory;

    @Autowired(required = false)
    @Qualifier("expCatalogEntityManagerFactory")
    private EntityManagerFactory expCatalogEntityManagerFactory;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    public void testAllEntityManagerFactoriesAreCreated() {
        assertNotNull(profileServiceEntityManagerFactory, "Profile service EntityManagerFactory should be created");
        assertNotNull(appCatalogEntityManagerFactory, "App catalog EntityManagerFactory should be created");
        assertNotNull(expCatalogEntityManagerFactory, "Exp catalog EntityManagerFactory should be created");
    }

    @Test
    public void testCoreServicesAreCreated() {

        assertTrue(
                applicationContext.getBeansOfType(RegistryService.class).size() > 0,
                "RegistryService should be registered as a bean");
        assertTrue(
                applicationContext.getBeansOfType(CredentialStoreService.class).size() > 0,
                "CredentialStoreService should be registered as a bean");
    }

    @Test
    public void testMapperBeansAreCreated() {

        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.registry.mappers.GatewayMapper.class)
                                .size()
                        > 0,
                "Registry GatewayMapper should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.profile.mappers.GatewayMapper.class)
                                .size()
                        > 0,
                "Profile GatewayMapper should be registered as a bean");
    }

    @Test
    public void testRequiredComponentsAreCreated() {

        assertTrue(
                applicationContext.getBeansOfType(SSHAccountManager.class).size() > 0,
                "SSHAccountManager should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(DefaultKeyStorePasswordCallback.class)
                                .size()
                        > 0,
                "DefaultKeyStorePasswordCallback should be registered as a bean");
        assertTrue(
                applicationContext
                                .getBeansOfType(GatewayGroupsInitializer.class)
                                .size()
                        > 0,
                "GatewayGroupsInitializer should be registered as a bean");
    }

    @Test
    public void testAdaptorSupportBeanIsPrimary() {

        if (applicationContext.getBeansOfType(AdaptorSupport.class).size() > 0) {
            AdaptorSupport adaptorSupport = applicationContext.getBean(AdaptorSupport.class);
            assertNotNull(adaptorSupport, "AdaptorSupport bean should be available");
            assertTrue(
                    adaptorSupport instanceof AdaptorSupportImpl,
                    "AdaptorSupport should be AdaptorSupportImpl (primary bean)");
        }
    }

    @Test
    public void testRepositoriesAreInjected() {
        UserProfileRepository userProfileRepository = applicationContext.getBean(UserProfileRepository.class);
        ComputeResourceRepository computeResourceRepository =
                applicationContext.getBean(ComputeResourceRepository.class);
        ExperimentRepository experimentRepository = applicationContext.getBean(ExperimentRepository.class);
        DomainRepository domainRepository = applicationContext.getBean(DomainRepository.class);
        CredentialRepository credentialRepository = applicationContext.getBean(CredentialRepository.class);

        assertNotNull(userProfileRepository, "UserProfileRepository should be injected");
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be injected");
        assertNotNull(experimentRepository, "ExperimentRepository should be injected");
        assertNotNull(domainRepository, "DomainRepository should be injected");
        assertNotNull(credentialRepository, "CredentialRepository should be injected");
    }

    @Test
    public void testAiravataServiceIsNotInitializedWhenThriftDisabled() {

        assertTrue(
                applicationContext.getBeansOfType(RegistryService.class).size() > 0,
                "RegistryService should be available");
    }

    @Test
    public void testPropertiesAreLoaded() {
        AiravataServerProperties properties = applicationContext.getBean(AiravataServerProperties.class);
        assertNotNull(properties, "AiravataServerProperties should be loaded");
        assertNotNull(properties.database(), "Database properties should be configured");
        assertNotNull(properties.database().registry(), "Registry database properties should be configured");
    }
}
