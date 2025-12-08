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

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
// Entity classes are checked by name in tests, imports not needed
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to validate that all JPA entities are properly loaded and accessible
 * through their respective EntityManagerFactories.
 */
@SpringBootTest(classes = {JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class EntityLoadingTest {

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
    @Qualifier("replicaCatalogEntityManagerFactory")
    private EntityManagerFactory replicaCatalogEntityManagerFactory;

    @Autowired
    @Qualifier("workflowCatalogEntityManagerFactory")
    private EntityManagerFactory workflowCatalogEntityManagerFactory;

    @Autowired
    @Qualifier("sharingRegistryEntityManagerFactory")
    private EntityManagerFactory sharingRegistryEntityManagerFactory;

    @Autowired
    @Qualifier("credentialStoreEntityManagerFactory")
    private EntityManagerFactory credentialStoreEntityManagerFactory;

    @Test
    public void testProfileServiceEntitiesAreLoaded() {
        EntityManager em = profileServiceEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Profile service should have entities loaded");

            // Check for specific entities
            // Check that entities are loaded (OpenJPA may use different class names)
            assertFalse(entities.isEmpty(), "Profile service should have entities loaded");
            // Verify by checking entity names rather than exact class matches
            boolean hasUserProfile = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("UserProfileEntity"));
            assertTrue(hasUserProfile, "UserProfileEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testAppCatalogEntitiesAreLoaded() {
        EntityManager em = appCatalogEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "App catalog should have entities loaded");

            boolean hasComputeResource = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("ComputeResourceEntity"));

            assertTrue(hasComputeResource, "ComputeResourceEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testExpCatalogEntitiesAreLoaded() {
        EntityManager em = expCatalogEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Exp catalog should have entities loaded");

            boolean hasExperiment = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("ExperimentEntity"));

            assertTrue(hasExperiment, "ExperimentEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testReplicaCatalogEntitiesAreLoaded() {
        EntityManager em = replicaCatalogEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Replica catalog should have entities loaded");

            boolean hasDataProduct = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("DataProductEntity"));

            assertTrue(hasDataProduct, "DataProductEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testWorkflowCatalogEntitiesAreLoaded() {
        EntityManager em = workflowCatalogEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Workflow catalog should have entities loaded");

            boolean hasAiravataWorkflow = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("AiravataWorkflowEntity"));

            assertTrue(hasAiravataWorkflow, "AiravataWorkflowEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testSharingRegistryEntitiesAreLoaded() {
        EntityManager em = sharingRegistryEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Sharing registry should have entities loaded");

            boolean hasDomain = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("DomainEntity"));
            boolean hasEntity = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("EntityEntity"));
            boolean hasUser = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("UserEntity"));

            assertTrue(hasDomain, "DomainEntity should be loaded");
            assertTrue(hasEntity, "EntityEntity should be loaded");
            assertTrue(hasUser, "UserEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testCredentialStoreEntitiesAreLoaded() {
        EntityManager em = credentialStoreEntityManagerFactory.createEntityManager();
        try {
            Set<EntityType<?>> entities = em.getMetamodel().getEntities();
            assertFalse(entities.isEmpty(), "Credential store should have entities loaded");

            boolean hasCredential = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("CredentialEntity"));
            boolean hasCommunityUser = entities.stream()
                    .anyMatch(e -> e.getJavaType().getSimpleName().equals("CommunityUserEntity"));

            assertTrue(hasCredential, "CredentialEntity should be loaded");
            assertTrue(hasCommunityUser, "CommunityUserEntity should be loaded");
        } finally {
            em.close();
        }
    }

    @Test
    public void testAllPersistenceUnitsHaveEntities() {
        EntityManagerFactory[] factories = {
            profileServiceEntityManagerFactory,
            appCatalogEntityManagerFactory,
            expCatalogEntityManagerFactory,
            replicaCatalogEntityManagerFactory,
            workflowCatalogEntityManagerFactory,
            sharingRegistryEntityManagerFactory,
            credentialStoreEntityManagerFactory
        };

        for (EntityManagerFactory factory : factories) {
            EntityManager em = factory.createEntityManager();
            try {
                Set<EntityType<?>> entities = em.getMetamodel().getEntities();
                assertFalse(
                        entities.isEmpty(),
                        "Persistence unit " + factory + " should have entities loaded. Found: " + entities.size());
            } finally {
                em.close();
            }
        }
    }
}
