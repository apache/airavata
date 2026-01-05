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
import org.apache.airavata.profile.repositories.UserProfileRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify AiravataApplication startup with minimal configuration.
 *
 * Minimal configuration:
 * - Thrift disabled
 * - REST disabled
 * - Background services disabled
 * - Only core services enabled
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            TestcontainersConfig.class,
            AiravataServerProperties.class,
            MinimalStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",

            // Infrastructure components excluded via @ComponentScan excludeFilters - no property flags needed
            // Core services (RegistryService, CredentialStoreService) are always available via DI
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
public class MinimalStartupTest {

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

    @Autowired
    @Qualifier("profileServiceEntityManagerFactory")
    private EntityManagerFactory profileServiceEntityManagerFactory;

    @Autowired
    @Qualifier("appCatalogEntityManagerFactory")
    private EntityManagerFactory appCatalogEntityManagerFactory;

    @Autowired
    @Qualifier("expCatalogEntityManagerFactory")
    private EntityManagerFactory expCatalogEntityManagerFactory;

    @Autowired
    private RegistryService registryService;

    @Autowired
    private CredentialStoreService credentialStoreService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded in minimal mode");
    }

    @Test
    public void testEntityManagerFactoriesAreCreated() {
        assertNotNull(profileServiceEntityManagerFactory, "Profile service EntityManagerFactory should be created");
        assertNotNull(appCatalogEntityManagerFactory, "App catalog EntityManagerFactory should be created");
        assertNotNull(expCatalogEntityManagerFactory, "Exp catalog EntityManagerFactory should be created");
    }

    @Test
    public void testCoreServicesAreAvailable() {
        assertNotNull(registryService, "RegistryService should be available");
        assertNotNull(credentialStoreService, "CredentialStoreService should be available");
    }

    @Test
    public void testRepositoriesAreAvailable() {
        assertNotNull(userProfileRepository, "UserProfileRepository should be available");
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be available");
        assertNotNull(experimentRepository, "ExperimentRepository should be available");
    }

    @Test
    public void testPropertiesAreLoaded() {
        AiravataServerProperties properties = applicationContext.getBean(AiravataServerProperties.class);
        assertNotNull(properties, "AiravataServerProperties should be loaded");
        assertNotNull(properties.database, "Database properties should be configured");
    }

    @Test
    public void testJpaConfigIsLoaded() {
        // Verify JpaConfig is loaded by checking for EntityManagerFactory beans
        assertTrue(
                applicationContext.getBeansOfType(EntityManagerFactory.class).size() >= 7,
                "All EntityManagerFactory beans should be created (at least 7 catalogs)");
    }
}
