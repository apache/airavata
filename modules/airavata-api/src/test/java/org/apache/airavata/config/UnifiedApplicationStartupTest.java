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
import org.apache.airavata.credential.repositories.CredentialRepository;
import org.apache.airavata.registry.repositories.appcatalog.ComputeResourceRepository;
import org.apache.airavata.registry.repositories.expcatalog.ExperimentRepository;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.sharing.repositories.DomainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify application startup with unified configuration.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, UnifiedApplicationStartupTest.TestConfiguration.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "airavata.flyway.enabled=false"})
@ActiveProfiles("test")
public class UnifiedApplicationStartupTest {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.sharing.services",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
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
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded");
    }

    @Test
    public void testEntityManagerFactoryIsCreated() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory should be created");
        assertTrue(entityManagerFactory.isOpen(), "EntityManagerFactory should be open");
    }

    @Test
    public void testCoreServicesAreCreated() {
        assertTrue(
                applicationContext.getBeansOfType(RegistryService.class).size() > 0,
                "RegistryService should be registered");
    }

    @Test
    public void testRepositoriesAreInjected() {
        assertNotNull(applicationContext.getBean(ComputeResourceRepository.class));
        assertNotNull(applicationContext.getBean(ExperimentRepository.class));
        assertNotNull(applicationContext.getBean(DomainRepository.class));
        assertNotNull(applicationContext.getBean(CredentialRepository.class));
    }
}
