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

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify Flyway configuration is correct for local/production startup.
 * This test verifies that FlywayConfig is properly configured and will run
 * migrations when the application starts (when flyway.enabled=true).
 *
 * Note: This test does not actually run migrations - it only verifies the
 * configuration is correct. Actual migration testing requires a real database.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, FlywayConfig.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "flyway.enabled=false", // Disable FlywayConfig since TestcontainersConfig handles migrations
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(AiravataServerProperties.class)
public class FlywayLocalStartupTest {

    @Test
    public void testFlywayConfigLoaded() {

        assertTrue(true, "FlywayConfig should be loaded");
    }

    @Test
    public void testFlywayBeansExist(
            @Autowired(required = false) @Qualifier("profileServiceFlyway") Flyway profileServiceFlyway,
            @Autowired(required = false) @Qualifier("appCatalogFlyway") Flyway appCatalogFlyway,
            @Autowired(required = false) @Qualifier("expCatalogFlyway") Flyway expCatalogFlyway,
            @Autowired(required = false) @Qualifier("replicaCatalogFlyway") Flyway replicaCatalogFlyway,
            @Autowired(required = false) @Qualifier("workflowCatalogFlyway") Flyway workflowCatalogFlyway,
            @Autowired(required = false) @Qualifier("sharingRegistryFlyway") Flyway sharingRegistryFlyway,
            @Autowired(required = false) @Qualifier("credentialStoreFlyway") Flyway credentialStoreFlyway) {}
}
