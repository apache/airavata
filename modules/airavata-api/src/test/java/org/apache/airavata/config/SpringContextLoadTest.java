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
import org.apache.airavata.registry.repositories.GatewayRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to validate that Spring application context loads correctly.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class SpringContextLoadTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ComputeResourceRepository computeResourceRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    @Autowired
    private GatewayRepository gatewayRepository;

    @Autowired
    private CredentialRepository credentialRepository;

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
    public void testRepositoriesAreInjected() {
        assertNotNull(computeResourceRepository, "ComputeResourceRepository should be injected");
        assertNotNull(experimentRepository, "ExperimentRepository should be injected");
        assertNotNull(gatewayRepository, "GatewayRepository should be injected");
        assertNotNull(credentialRepository, "CredentialRepository should be injected");
    }
}
