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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify that persistence configuration loads correctly
 * and all EntityManagerFactory beans can be created without errors.
 */
@SpringBootTest(
        classes = {JpaConfig.class, AiravataServerProperties.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class PersistenceConfigurationTest {

    private final EntityManagerFactory profileServiceEntityManagerFactory;
    private final EntityManagerFactory appCatalogEntityManagerFactory;
    private final EntityManagerFactory expCatalogEntityManagerFactory;
    private final EntityManagerFactory replicaCatalogEntityManagerFactory;
    private final EntityManagerFactory workflowCatalogEntityManagerFactory;
    private final EntityManagerFactory sharingRegistryEntityManagerFactory;
    private final EntityManagerFactory credentialStoreEntityManagerFactory;

    public PersistenceConfigurationTest(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory profileServiceEntityManagerFactory,
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory appCatalogEntityManagerFactory,
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory expCatalogEntityManagerFactory,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory replicaCatalogEntityManagerFactory,
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory workflowCatalogEntityManagerFactory,
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory sharingRegistryEntityManagerFactory,
            @Qualifier("credentialStoreEntityManagerFactory")
                    EntityManagerFactory credentialStoreEntityManagerFactory) {
        this.profileServiceEntityManagerFactory = profileServiceEntityManagerFactory;
        this.appCatalogEntityManagerFactory = appCatalogEntityManagerFactory;
        this.expCatalogEntityManagerFactory = expCatalogEntityManagerFactory;
        this.replicaCatalogEntityManagerFactory = replicaCatalogEntityManagerFactory;
        this.workflowCatalogEntityManagerFactory = workflowCatalogEntityManagerFactory;
        this.sharingRegistryEntityManagerFactory = sharingRegistryEntityManagerFactory;
        this.credentialStoreEntityManagerFactory = credentialStoreEntityManagerFactory;
    }

    @Test
    public void testPersistenceUnitsCanBeCreated() {
        // This test verifies that all persistence units can be initialized
        // without class loading or persistence.xml errors
        assertNotNull(profileServiceEntityManagerFactory, "Profile service EntityManagerFactory should be created");
        assertNotNull(appCatalogEntityManagerFactory, "App catalog EntityManagerFactory should be created");
        assertNotNull(expCatalogEntityManagerFactory, "Exp catalog EntityManagerFactory should be created");
        assertNotNull(replicaCatalogEntityManagerFactory, "Replica catalog EntityManagerFactory should be created");
        assertNotNull(workflowCatalogEntityManagerFactory, "Workflow catalog EntityManagerFactory should be created");
        assertNotNull(sharingRegistryEntityManagerFactory, "Sharing registry EntityManagerFactory should be created");
        assertNotNull(credentialStoreEntityManagerFactory, "Credential store EntityManagerFactory should be created");
    }

    @Test
    public void testPersistenceUnitsAreOpen() {
        if (profileServiceEntityManagerFactory != null) {
            assertTrue(
                    profileServiceEntityManagerFactory.isOpen(), "Profile service EntityManagerFactory should be open");
        }
        if (appCatalogEntityManagerFactory != null) {
            assertTrue(appCatalogEntityManagerFactory.isOpen(), "App catalog EntityManagerFactory should be open");
        }
        if (expCatalogEntityManagerFactory != null) {
            assertTrue(expCatalogEntityManagerFactory.isOpen(), "Exp catalog EntityManagerFactory should be open");
        }
        if (replicaCatalogEntityManagerFactory != null) {
            assertTrue(
                    replicaCatalogEntityManagerFactory.isOpen(), "Replica catalog EntityManagerFactory should be open");
        }
        if (workflowCatalogEntityManagerFactory != null) {
            assertTrue(
                    workflowCatalogEntityManagerFactory.isOpen(),
                    "Workflow catalog EntityManagerFactory should be open");
        }
        if (sharingRegistryEntityManagerFactory != null) {
            assertTrue(
                    sharingRegistryEntityManagerFactory.isOpen(),
                    "Sharing registry EntityManagerFactory should be open");
        }
        if (credentialStoreEntityManagerFactory != null) {
            assertTrue(
                    credentialStoreEntityManagerFactory.isOpen(),
                    "Credential store EntityManagerFactory should be open");
        }
    }
}
