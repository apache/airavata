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
package org.apache.airavata.registry.repositories.common;

import org.apache.airavata.config.IntegrationTestConfiguration;
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

            // Disable JPA auto-configuration (we configure manually)
            "spring.autoconfigure.exclude="
                    + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",

            // Disable Flyway (Testcontainers handles schema)
            "flyway.enabled=false",

            // Enable minimal API service for JpaConfig conditional
            "services.thrift.enabled=true",
            "services.rest.enabled=false",

            // Disable all optional services
            "services.scheduler.enabled=false",
            "services.scheduler.rescheduler.enabled=false",
            "services.scheduler.interpreter.enabled=false",
            "services.controller.enabled=false",
            "services.participant.enabled=false",
            "services.monitor.compute.enabled=false",
            "services.monitor.email.enabled=false",
            "services.monitor.realtime.enabled=false",

            // Disable security components
            "security.iam.enabled=false",
            "security.authzCache.enabled=false",

            // Disable messaging
            "kafka.enabled=false",
            "rabbitmq.enabled=false"
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
     * Default constructor for tests using @TestConstructor autowiring.
     */
    protected TestBase() {
        // No initialization needed - Spring handles everything
    }
}
