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
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to validate that all JPA entities are properly loaded.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "airavata.flyway.enabled=false",
        })
@ActiveProfiles("test")
public class EntityLoadingTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void testEntitiesAreLoaded() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Entities should be loaded");

            // Verify key entities from different packages are loaded
            assertTrue(hasEntity(entities, "ComputeResourceEntity"), "ComputeResourceEntity should be loaded");
            assertTrue(hasEntity(entities, "ExperimentEntity"), "ExperimentEntity should be loaded");
            assertTrue(hasEntity(entities, "StatusEntity"), "StatusEntity should be loaded");
            assertTrue(hasEntity(entities, "GatewayEntity"), "GatewayEntity should be loaded");
            assertTrue(hasEntity(entities, "CredentialEntity"), "CredentialEntity should be loaded");
        } finally {
            em.close();
        }
    }

    private boolean hasEntity(Set<EntityType<?>> entities, String simpleName) {
        return entities.stream().anyMatch(e -> e.getJavaType().getSimpleName().equals(simpleName));
    }
}
