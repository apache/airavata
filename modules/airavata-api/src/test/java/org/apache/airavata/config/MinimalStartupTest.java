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
import org.apache.airavata.registry.repositories.UserRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify AiravataApplication startup with minimal configuration.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, MinimalStartupTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.aop.proxy-target-class=true",
            "spring.main.lazy-initialization=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class MinimalStartupTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.profile.mappers",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.sharing.mappers",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security",
                "org.apache.airavata.accountprovisioning"
            },
            excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*KeyCloak.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Lifecycle.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Monitor.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Scheduler.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Kafka.*")
            })
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private RegistryService registryService;

    @Autowired
    private CredentialStoreService credentialStoreService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded in minimal mode");
    }

    @Test
    public void testEntityManagerFactoryIsCreated() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory should be created");
        assertTrue(entityManagerFactory.isOpen(), "EntityManagerFactory should be open");
    }

    @Test
    public void testCoreServicesAreAvailable() {
        assertNotNull(registryService, "RegistryService should be available");
        assertNotNull(credentialStoreService, "CredentialStoreService should be available");
    }

    @Test
    public void testRepositoriesAreAvailable() {
        assertNotNull(userRepository, "UserRepository should be available");
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be available");
        assertNotNull(experimentRepository, "ExperimentRepository should be available");
    }

    @Test
    public void testPropertiesAreLoaded() {
        AiravataServerProperties properties = applicationContext.getBean(AiravataServerProperties.class);
        assertNotNull(properties, "AiravataServerProperties should be loaded");
    }

    @Test
    public void testJpaConfigIsLoaded() {
        assertTrue(
                applicationContext.getBeansOfType(EntityManagerFactory.class).size() >= 1,
                "EntityManagerFactory bean should be created");
    }
}
