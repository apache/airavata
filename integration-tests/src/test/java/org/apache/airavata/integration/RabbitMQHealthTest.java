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
package org.apache.airavata.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies AMQP connectivity to RabbitMQ and that the core Airavata exchanges
 * are already declared (passive declare — does not create them).
 */
@Tag("integration")
class RabbitMQHealthTest {

    private static final String HOST = System.getProperty("airavata.rabbitmq.host", "localhost");
    private static final int PORT = Integer.parseInt(System.getProperty("airavata.rabbitmq.port", "5672"));
    private static final String USERNAME = System.getProperty("airavata.rabbitmq.username", "airavata");
    private static final String PASSWORD = System.getProperty("airavata.rabbitmq.password", "airavata");
    private static final String VHOST = System.getProperty("airavata.rabbitmq.vhost", "/");

    static Stream<String> exchanges() {
        return Stream.of("experiment_exchange", "process_exchange", "status_exchange");
    }

    @ParameterizedTest(name = "exchange {0} is declared")
    @MethodSource("exchanges")
    void exchangeShouldBeDeclared(String exchangeName) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setVirtualHost(VHOST);

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            assertDoesNotThrow(
                    () -> channel.exchangeDeclarePassive(exchangeName),
                    "Exchange '" + exchangeName + "' is not declared in RabbitMQ");
        }
    }
}
