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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify that persistence configuration loads correctly.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class PersistenceConfigurationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testEntityManagerFactoryCanBeCreated() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory should be created");
    }

    @Test
    public void testEntityManagerFactoryIsOpen() {
        assertTrue(entityManagerFactory.isOpen(), "EntityManagerFactory should be open");
    }

    @Test
    public void testEntitiesAreLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        assertNotNull(entities, "Entities should be available");
        assertFalse(entities.isEmpty(), "Entities should be loaded");
    }
}
