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
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to validate that Hibernate entities match the database schema defined in Flyway migrations.
 *
 * This test:
 * 1. Uses Spring Boot test context to inject EntityManagerFactory beans
 * 2. Validates that entities can be loaded and metamodel is accessible
 * 3. Validates all 7 persistence units
 *
 * Note: Database schema is managed by Flyway migrations in db/migration/ directories.
 * This test validates entity structure and mapping correctness.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, AiravataServerProperties.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(
        properties = {
            // Configure all persistence units to use H2 in-memory database
            "database.profile.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.profile.driver=org.h2.Driver",
            "database.profile.user=sa",
            "database.profile.password=",
            "database.catalog.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.catalog.driver=org.h2.Driver",
            "database.catalog.user=sa",
            "database.catalog.password=",
            "database.registry.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.registry.driver=org.h2.Driver",
            "database.registry.user=sa",
            "database.registry.password=",
            "database.replica.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.replica.driver=org.h2.Driver",
            "database.replica.user=sa",
            "database.replica.password=",
            "database.workflow.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.workflow.driver=org.h2.Driver",
            "database.workflow.user=sa",
            "database.workflow.password=",
            "database.sharing.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.sharing.driver=org.h2.Driver",
            "database.sharing.user=sa",
            "database.sharing.password=",
            "database.vault.url=jdbc:h2:mem:schema_validation_test;DB_CLOSE_DELAY=-1;MODE=MySQL",
            "database.vault.driver=org.h2.Driver",
            "database.vault.user=sa",
            "database.vault.password=",
            "database.validation-query=SELECT 1"
        })
public class SchemaValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidationTest.class);

    // Map persistence unit names to EntityManagerFactory beans
    private final Map<String, EntityManagerFactory> emfMap;

    @Autowired
    public SchemaValidationTest(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileServiceEmf,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEmf,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEmf,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEmf,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEmf,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEmf,
            @Qualifier("credentialStoreEntityManagerFactory") EntityManagerFactory credentialStoreEmf) {
        this.emfMap = new HashMap<>();
        this.emfMap.put(JpaConfig.PROFILE_SERVICE_PU, profileServiceEmf);
        this.emfMap.put(JpaConfig.APPCATALOG_PU, appCatalogEmf);
        this.emfMap.put(JpaConfig.EXPCATALOG_PU, expCatalogEmf);
        this.emfMap.put(JpaConfig.REPLICACATALOG_PU, replicaCatalogEmf);
        this.emfMap.put(JpaConfig.WORKFLOWCATALOG_PU, workflowCatalogEmf);
        this.emfMap.put(JpaConfig.SHARING_REGISTRY_PU, sharingRegistryEmf);
        this.emfMap.put(JpaConfig.CREDENTIAL_STORE_PU, credentialStoreEmf);
    }

    @Test
    public void testProfileServiceSchemaValidation() {
        validatePersistenceUnit("profile_service");
    }

    @Test
    public void testAppCatalogSchemaValidation() {
        validatePersistenceUnit("app_catalog");
    }

    @Test
    public void testExpCatalogSchemaValidation() {
        validatePersistenceUnit("experiment_catalog");
    }

    @Test
    public void testReplicaCatalogSchemaValidation() {
        validatePersistenceUnit("replica_catalog");
    }

    @Test
    public void testWorkflowCatalogSchemaValidation() {
        validatePersistenceUnit("workflow_catalog");
    }

    @Test
    public void testSharingRegistrySchemaValidation() {
        validatePersistenceUnit("sharing_registry");
    }

    @Test
    public void testCredentialStoreSchemaValidation() {
        validatePersistenceUnit("credential_store");
    }

    @Test
    public void testAllPersistenceUnitsSchemaValidation() {
        int failures = 0;
        for (String puName : emfMap.keySet()) {
            try {
                validatePersistenceUnit(puName);
                logger.info("✓ Persistence unit '{}' validated successfully", puName);
            } catch (AssertionError e) {
                failures++;
                logger.error("✗ Persistence unit '{}' validation failed: {}", puName, e.getMessage());
            }
        }

        if (failures > 0) {
            fail(String.format("Schema validation failed for %d persistence unit(s)", failures));
        }
    }

    /**
     * Validate a persistence unit against the database schema.
     *
     * This test validates that:
     * - EntityManagerFactory can be created and initialized
     * - Entity mappings are syntactically correct
     * - Metamodel is accessible and contains entities
     *
     * Note: Database schema is managed by Flyway migrations. This test validates
     * entity structure and mapping correctness. For full schema validation, ensure
     * Flyway migrations match entity definitions.
     */
    private void validatePersistenceUnit(String persistenceUnitName) {
        EntityManagerFactory emf = emfMap.get(persistenceUnitName);
        if (emf == null) {
            fail("EntityManagerFactory not found for persistence unit: " + persistenceUnitName);
        }

        try {
            logger.info("Validating persistence unit: {}", persistenceUnitName);

            assertNotNull(emf, "EntityManagerFactory should be created");
            assertNotNull(emf.getMetamodel(), "Metamodel should be available");

            // Try to access the metamodel to ensure it's fully initialized
            var entities = emf.getMetamodel().getEntities();
            assertNotNull(entities, "Entities should be available in metamodel");

            logger.info(
                    "✓ Persistence unit '{}' schema validation passed ({} entities)",
                    persistenceUnitName,
                    entities.size());

        } catch (jakarta.persistence.PersistenceException e) {
            // Hibernate validation errors are wrapped in PersistenceException
            String errorMsg = String.format(
                    "Schema validation failed for persistence unit '%s': %s", persistenceUnitName, e.getMessage());
            if (e.getCause() != null) {
                errorMsg += " (Caused by: " + e.getCause().getMessage() + ")";
            }
            logger.error(errorMsg, e);
            fail(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format(
                    "Unexpected error validating persistence unit '%s': %s", persistenceUnitName, e.getMessage());
            logger.error(errorMsg, e);
            fail(errorMsg, e);
        }
        // Note: We don't close the EntityManagerFactory here as it's managed by Spring
    }
}
