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

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to validate that Hibernate entities match the database schema.
 *
 * This test validates:
 * 1. EntityManagerFactory is created and initialized
 * 2. Entity mappings are syntactically correct
 * 3. Metamodel is accessible and contains all entities from all packages
 */
@SpringBootTest(
        classes = {JpaConfiguration.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "flyway.enabled=false",
        })
@ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(ServerProperties.class)
public class SchemaValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidationTest.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testEntityManagerFactoryIsCreated() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory should be created");
        logger.info("EntityManagerFactory created successfully");
    }

    @Test
    public void testMetamodelIsAccessible() {
        assertNotNull(entityManagerFactory.getMetamodel(), "Metamodel should be available");
        logger.info("Metamodel is accessible");
    }

    @Test
    public void testEntitiesAreLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        assertNotNull(entities, "Entities should be available in metamodel");
        assertFalse(entities.isEmpty(), "Metamodel should contain entities");

        logger.info("Schema validation passed - {} entities loaded:", entities.size());
        entities.forEach(entity -> logger.info(
                "  - {} ({})", entity.getName(), entity.getJavaType().getSimpleName()));
    }

    @Test
    public void testUserEntitiesLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        boolean hasUserEntities =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().contains("UserEntity"));
        assertFalse(!hasUserEntities, "User entities should be loaded");
        logger.info("User entities are loaded");
    }

    @Test
    public void testAppCatalogEntitiesLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        boolean hasAppEntities =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("ApplicationEntity"));
        assertFalse(!hasAppEntities, "Application entities should be loaded");
        logger.info("Application entities are loaded");
    }

    @Test
    public void testExpCatalogEntitiesLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        boolean hasExpEntities =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("ExperimentEntity"));
        assertFalse(!hasExpEntities, "Experiment entities should be loaded");
        logger.info("Experiment entities are loaded");
    }

    @Test
    public void testSharingEntitiesLoaded() {
        var entities = entityManagerFactory.getMetamodel().getEntities();
        boolean hasSharingEntities =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("UserGroupEntity"));
        assertFalse(!hasSharingEntities, "Sharing entities (UserGroupEntity) should be loaded");
        logger.info("Sharing entities are loaded");
    }
}
