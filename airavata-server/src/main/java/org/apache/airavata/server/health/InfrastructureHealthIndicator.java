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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import org.apache.airavata.config.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Actuator health indicator that checks TCP connectivity to RabbitMQ, Kafka, and ZooKeeper.
 * Reports DOWN if any required infrastructure service is unreachable.
 */
@Component("infrastructure")
public class InfrastructureHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureHealthIndicator.class);
    private static final int CONNECT_TIMEOUT_MS = 3000;

    @Override
    public Health health() {
        var builder = Health.up();
        boolean allHealthy = true;

        // RabbitMQ host/port parsed from rabbitmq.broker.url (amqp://user:pass@host:port).
        try {
            URI amqp = URI.create(ServerSettings.getSetting("rabbitmq.broker.url", "amqp://localhost:5672"));
            int port = amqp.getPort() > 0 ? amqp.getPort() : 5672;
            allHealthy &= checkTcp(builder, "rabbitmq", amqp.getHost(), port);
        } catch (Exception e) {
            builder.withDetail("rabbitmq", "config error: " + e.getMessage());
            allHealthy = false;
        }

        allHealthy &= checkHostPort(builder, "zookeeper", "zookeeper.server.connection", "localhost:2181");
        allHealthy &= checkHostPort(builder, "kafka", "kafka.broker.url", "localhost:9092");

        return allHealthy ? builder.build() : builder.down().build();
    }

    /** Reads a "host:port" setting and checks TCP connectivity. */
    private boolean checkHostPort(Health.Builder builder, String name, String settingKey, String defaultValue) {
        try {
            String[] parts = ServerSettings.getSetting(settingKey, defaultValue).split(":");
            return checkTcp(builder, name, parts[0], Integer.parseInt(parts[1]));
        } catch (Exception e) {
            builder.withDetail(name, "config error: " + e.getMessage());
            return false;
        }
    }

    private boolean checkTcp(Health.Builder builder, String name, String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            builder.withDetail(name, "reachable");
            return true;
        } catch (Exception e) {
            logger.warn("{} health check failed: {}:{} - {}", name, host, port, e.getMessage());
            builder.withDetail(name, "unreachable: " + e.getMessage());
            return false;
        }
    }
}
