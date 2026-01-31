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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import io.dapr.client.DaprClient;
import org.apache.airavata.orchestrator.internal.messaging.DaprMessagingImpl.DaprSubscriptionRegistry;
import org.apache.airavata.orchestrator.internal.messaging.MessagingContracts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

/**
 * Test configuration for Dapr that provides a mock DaprClient.
 * This enables messaging tests to run without a real Dapr sidecar.
 *
 * <p>The mock client routes published messages directly to registered
 * handlers via DaprSubscriptionRegistry, simulating pub/sub behavior.
 */
@Configuration
@Profile("test")
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
public class TestDaprConfig {

    private static final Logger log = LoggerFactory.getLogger(TestDaprConfig.class);

    /**
     * Creates a mock DaprClient for test profile.
     * Messages published via this client are routed directly to handlers
     * registered in DaprSubscriptionRegistry.
     */
    @Bean
    @Primary
    public DaprClient daprClient(DaprSubscriptionRegistry registry) {
        log.info("Creating mock DaprClient for test profile with in-memory message routing");
        
        DaprClient mockClient = mock(DaprClient.class);
        
        // Configure publishEvent to route messages to registered handlers
        doAnswer(invocation -> {
            String pubsubName = invocation.getArgument(0);
            String topicName = invocation.getArgument(1);
            Object data = invocation.getArgument(2);
            
            log.debug("MockDaprClient: publishing to pubsub={}, topic={}", pubsubName, topicName);
            
            MessagingContracts.MessageHandler handler = registry.get(topicName);
            if (handler != null && data instanceof MessagingContracts.MessageContext) {
                try {
                    handler.onMessage((MessagingContracts.MessageContext) data);
                    log.debug("MockDaprClient: message delivered to handler for topic={}", topicName);
                } catch (Exception e) {
                    log.warn("MockDaprClient: handler error for topic={}: {}", topicName, e.getMessage());
                }
            } else {
                log.debug("MockDaprClient: no handler registered for topic={} or data is not MessageContext", topicName);
            }
            
            return Mono.empty();
        }).when(mockClient).publishEvent(anyString(), anyString(), any());
        
        // Also handle the 4-arg version with metadata
        doAnswer(invocation -> {
            String pubsubName = invocation.getArgument(0);
            String topicName = invocation.getArgument(1);
            Object data = invocation.getArgument(2);
            // Map<String, String> metadata = invocation.getArgument(3); // ignored in test
            
            log.debug("MockDaprClient: publishing to pubsub={}, topic={} (with metadata)", pubsubName, topicName);
            
            MessagingContracts.MessageHandler handler = registry.get(topicName);
            if (handler != null && data instanceof MessagingContracts.MessageContext) {
                try {
                    handler.onMessage((MessagingContracts.MessageContext) data);
                    log.debug("MockDaprClient: message delivered to handler for topic={}", topicName);
                } catch (Exception e) {
                    log.warn("MockDaprClient: handler error for topic={}: {}", topicName, e.getMessage());
                }
            }
            
            return Mono.empty();
        }).when(mockClient).publishEvent(anyString(), anyString(), any(), anyMap());
        
        log.info("Mock DaprClient configured for test profile");
        return mockClient;
    }
}
