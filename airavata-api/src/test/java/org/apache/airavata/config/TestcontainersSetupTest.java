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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test to verify Testcontainers setup and database schema creation.
 * Tests that containers start correctly and migrations are applied.
 *
 * Note: This test requires Docker or nerdctl to be running.
 * Testcontainers will automatically detect and use the available container runtime.
 */
@SpringBootTest(
        classes = {JpaConfig.class, AiravataPropertiesConfiguration.class, TestcontainersConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "flyway.enabled=false" // Disable FlywayConfig since TestcontainersConfig handles migrations
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@ActiveProfiles("test")
public class TestcontainersSetupTest {

    @Test
    public void testAppCatalogContainerAndSchema(@Qualifier("appCatalogDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "App catalog DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "COMPUTE_RESOURCE", null);
            assertTrue(tables.next(), "COMPUTE_RESOURCE table should exist");
            tables.close();
        }
    }

    @Test
    public void testExperimentCatalogContainerAndSchema(@Qualifier("registryDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Experiment catalog DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "EXPERIMENT", null);
            assertTrue(tables.next(), "EXPERIMENT table should exist");
            tables.close();
        }
    }

    @Test
    public void testProfileServiceContainerAndSchema(@Qualifier("profileServiceDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Profile service DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "USER_PROFILE", null);
            assertTrue(tables.next(), "USER_PROFILE table should exist");
            tables.close();
        }
    }

    @Test
    public void testReplicaCatalogContainerAndSchema(@Qualifier("replicaDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Replica catalog DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "DATA_PRODUCT", null);
            assertTrue(tables.next(), "DATA_PRODUCT table should exist");
            tables.close();
        }
    }

    @Test
    public void testWorkflowCatalogContainerAndSchema(@Qualifier("workflowDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Workflow catalog DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            // Check for any table to verify schema exists
            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
            assertTrue(tables.next(), "Workflow catalog should have at least one table");
            tables.close();
        }
    }

    @Test
    public void testSharingRegistryContainerAndSchema(@Qualifier("sharingDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Sharing registry DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "DOMAIN", null);
            assertTrue(tables.next(), "DOMAIN table should exist");
            tables.close();
        }
    }

    @Test
    public void testCredentialStoreContainerAndSchema(@Qualifier("credentialStoreDataSource") DataSource dataSource)
            throws Exception {
        assertNotNull(dataSource, "Credential store DataSource should be created");

        try (Connection conn = dataSource.getConnection()) {
            assertTrue(conn.isValid(5), "Connection should be valid");

            DatabaseMetaData metaData = conn.getMetaData();
            // Check for any table to verify schema exists
            ResultSet tables = metaData.getTables(null, null, null, new String[] {"TABLE"});
            assertTrue(tables.next(), "Credential store should have at least one table");
            tables.close();
        }
    }
}
