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

import org.apache.airavata.config.IntegrationTestConfiguration;
import org.apache.airavata.credential.entity.CredentialEntity;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for repository integration tests using Spring Boot.
 *
 * <p>This base class provides:
 * <ul>
 *   <li>Automatic property loading via {@link TestPropertyInitializer}</li>
 *   <li>JPA configuration with Testcontainers MariaDB</li>
 *   <li>Transaction rollback after each test via {@link Transactional}</li>
 *   <li>Disabled non-essential services (security, monitoring, etc.)</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
 * public class MyRepositoryTest extends TestBase {
 *
 *     private final MyRepository repository;
 *
 *     public MyRepositoryTest(MyRepository repository) {
 *         this.repository = repository;
 *     }
 *
 *     @Test
 *     void testSomething() {
 *         // test code
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>Property Override</h3>
 * <p>Properties from airavata.properties are loaded with low priority.
 * To override for specific tests, use:
 * <pre>
 * {@code @TestPropertySource(properties = "my.property=value")}
 * </pre>
 *
 * @see IntegrationTestConfiguration
 * @see TestPropertyInitializer
 */
@SpringBootTest(
        classes = IntegrationTestConfiguration.class,
        properties = {
            // Allow bean overriding for test configurations
            "spring.main.allow-bean-definition-overriding=true",

            // Disable Flyway (Testcontainers handles schema via Hibernate DDL)
            "airavata.flyway.enabled=false",

            // Disable all optional services
            "airavata.services.scheduler.rescheduler.enabled=false",
            "airavata.services.scheduler.interpreter.enabled=false",
            "airavata.services.controller.enabled=false",
            "airavata.services.participant.enabled=false",
            "airavata.services.monitor.compute.enabled=false",
            "airavata.services.monitor.email.enabled=false",
            "airavata.services.monitor.realtime.enabled=false",

            // Disable security components
            "airavata.security.iam.enabled=false",
        })
@ActiveProfiles("test")
@Transactional
public abstract class TestBase {

    @org.springframework.beans.factory.annotation.Autowired
    protected jakarta.persistence.EntityManager entityManager;

    /**
     * Flush pending changes to the database and clear the JPA first-level cache.
     * Use this before fetching entities that were modified via child entity saves
     * to ensure fresh data is loaded from the database.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Persist a minimal CREDENTIALS row so RESOURCE_PROFILE / RESOURCE_ACCESS / RESOURCE_ACCESS_GRANT
     * foreign keys to (GATEWAY_ID, TOKEN_ID) are satisfied. Idempotent: safe to call multiple times
     * for the same (gatewayId, tokenId) within the same transaction.
     */
    protected void ensureCredentialExists(String gatewayId, String tokenId) {
        var existing = entityManager
                .createQuery(
                        "SELECT c FROM CredentialEntity c WHERE c.gatewayId = :gw AND c.credentialId = :id",
                        CredentialEntity.class)
                .setParameter("gw", gatewayId)
                .setParameter("id", tokenId)
                .getResultList();
        if (!existing.isEmpty()) {
            return;
        }
        CredentialEntity cred = new CredentialEntity();
        cred.setGatewayId(gatewayId);
        cred.setCredentialId(tokenId);
        cred.setUserId("test-user");
        cred.setType("SSH");
        cred.setCredentialData(new byte[] {0});
        entityManager.persist(cred);
        entityManager.flush();
    }

    /**
     * Default constructor for tests using @TestConstructor autowiring.
     */
    protected TestBase() {
        // No initialization needed - Spring handles everything
    }
}
