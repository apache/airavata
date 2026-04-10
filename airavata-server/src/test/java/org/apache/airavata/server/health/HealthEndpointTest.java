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
package org.apache.airavata.server.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

/**
 * Unit test for {@link InfrastructureHealthIndicator}.
 * <p>
 * The indicator checks TCP connectivity to RabbitMQ, Kafka, and ZooKeeper.
 * In a test environment without those services running, the health status
 * should be DOWN with detail entries for each service.
 */
class HealthEndpointTest {

    @Test
    void healthReportsDownWhenInfrastructureUnavailable() {
        var indicator = new InfrastructureHealthIndicator();
        Health health = indicator.health();

        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus());

        // All three services should have detail entries
        assertTrue(health.getDetails().containsKey("rabbitmq"));
        assertTrue(health.getDetails().containsKey("zookeeper"));
        assertTrue(health.getDetails().containsKey("kafka"));

        // Each unreachable service should say so
        assertTrue(health.getDetails().get("rabbitmq").toString().contains("unreachable"));
        assertTrue(health.getDetails().get("zookeeper").toString().contains("unreachable"));
    }

    @Test
    void healthDetailsContainServiceNames() {
        var indicator = new InfrastructureHealthIndicator();
        Health health = indicator.health();

        // Should have exactly 3 detail entries
        assertEquals(3, health.getDetails().size());
    }
}
