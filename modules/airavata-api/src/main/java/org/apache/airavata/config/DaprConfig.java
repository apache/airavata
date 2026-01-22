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

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dapr configuration for Pub/Sub (Redis) and State Store (Redis).
 *
 * <p>Configure via application.properties:
 *
 * <ul>
 *   <li>airavata.dapr.enabled=true
 *   <li>airavata.dapr.grpc-port=50001
 *   <li>airavata.dapr.pubsub.name=redis-pubsub
 *   <li>airavata.dapr.state.name=redis-state
 * </ul>
 *
 * <p>Dapr component YAMLs (redis-pubsub.yaml, redis-state.yaml) must be
 * available to the Dapr sidecar via --resources-path.
 *
 * <p>DaprClient uses gRPC-only mode to communicate with the Dapr sidecar.
 * Pub/Sub subscription callbacks are delivered via HTTP to the application
 * server (port 8080) at /api/v1/dapr/pubsub/{topic}.
 *
 * <p>When airavata.dapr.enabled=false (e.g. in tests without a Dapr sidecar),
 * this config is not loaded. Tests that need Dapr can use a separate
 * profile and provide their own DaprClient bean.
 */
@Configuration
@ConditionalOnProperty(prefix = "airavata.dapr", name = "enabled", havingValue = "true")
public class DaprConfig {

    /**
     * DaprClient for Pub/Sub and State Store. Connects to the Dapr sidecar via gRPC.
     *
     * <p>Uses airavata.dapr.grpc-port to set dapr.grpc.port system property
     * when it differs from the default (50001), so the SDK can connect to a
     * non-default sidecar. Otherwise set DAPR_GRPC_PORT in the environment
     * before starting the JVM.
     *
     * <p>The Dapr Java SDK defaults to gRPC mode, so no explicit protocol
     * configuration is needed.
     *
     * <p>Not created in profile "test"; use airavata.dapr.enabled=false or
     * a test-specific profile with a test DaprClient if needed.
     */
    @Bean
    @Profile("!test")
    public DaprClient daprClient(@Value("${airavata.dapr.grpc-port:50001}") int grpcPort) {
        if (grpcPort != 50001) {
            System.setProperty("dapr.grpc.port", String.valueOf(grpcPort));
        }
        return new DaprClientBuilder().build();
    }
}
