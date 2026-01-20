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

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to validate JPA entity configuration.
 * Note: persistence.xml is no longer used - entities are discovered via package scanning.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class ValidatePersistenceXml {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testEntitiesAreDiscovered() {
        assertNotNull(entityManagerFactory, "EntityManagerFactory should be created");

        var entities = entityManagerFactory.getMetamodel().getEntities();
        assertNotNull(entities, "Entities should be available");
        assertFalse(entities.isEmpty(), "Entities should be discovered via package scanning");

        // Verify key entities from different packages are present
        boolean hasComputeResource =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("ComputeResourceEntity"));
        boolean hasExperiment =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("ExperimentEntity"));
        boolean hasDomain =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("DomainEntity"));
        boolean hasCredential =
                entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals("CredentialEntity"));

        assertTrue(hasComputeResource, "ComputeResourceEntity should be discovered");
        assertTrue(hasExperiment, "ExperimentEntity should be discovered");
        assertTrue(hasDomain, "DomainEntity should be discovered");
        assertTrue(hasCredential, "CredentialEntity should be discovered");
    }
}
