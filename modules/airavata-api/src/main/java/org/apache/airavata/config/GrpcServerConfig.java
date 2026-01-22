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

import io.grpc.netty.NettyServerBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.grpc.server.ServerBuilderCustomizer;

/**
 * Unified gRPC Server Configuration - External API for Agent and Research services.
 *
 * <p>This configuration class sets up the unified gRPC server that runs on port 9090
 * (configurable via {@code airavata.services.grpc.server.port}). The gRPC server hosts
 * multiple services through a single endpoint.
 *
 * <p><b>External API:</b> This is one of four external API layers in Airavata:
 * <ul>
 *   <li>Thrift Server (port 8930) - Thrift Endpoints for Airavata API functions</li>
 *   <li>HTTP Server (port 8080):
 *       <ul>
 *         <li>Airavata API - HTTP Endpoints for Airavata API functions</li>
 *         <li>File API - HTTP Endpoints for file upload/download</li>
 *         <li>Agent API - HTTP Endpoints for interactive job contexts</li>
 *         <li>Research API - HTTP Endpoints for use by research hub</li>
 *       </ul>
 *   </li>
 *   <li>gRPC Server (port 9090) - For airavata binaries to open persistent channels with airavata APIs (this server)</li>
 *   <li>Dapr gRPC (port 50001) - Sidecar for pub/sub, state, and workflow execution</li>
 * </ul>
 *
 * <p><b>Persistent Connectivity:</b> The unified gRPC server enables airavata binaries to open
 * persistent channels with airavata APIs. This includes:
 * <ul>
 *   <li><b>Agent API</b> - Bidirectional streaming for agent-server communication.
 *       Handles environment setup, command execution, Jupyter/Python execution, and TCP tunneling.
 *       Defined in {@code modules/agent-framework/proto/agent-communication.proto}.
 *       Note: Agent API also has HTTP endpoints on the HTTP Server (port 8080).</li>
 *   <li><b>Research API</b> - Persistent connectivity for research hub services.
 *       Provides gRPC persistent connections for research workflows and data management.
 *       Note: Research API also has HTTP endpoints on the HTTP Server (port 8080).</li>
 * </ul>
 *
 * <p><b>Configuration:</b>
 * <ul>
 *   <li>Maps {@code airavata.services.grpc.server.port} to {@code spring.grpc.server.port}</li>
 *   <li>Sets keepalive properties programmatically to prevent NullPointerException</li>
 *   <li>Configures connection timeouts and keepalive settings for long-lived connections</li>
 *   <li>Loads LAST, after all other services are initialized</li>
 * </ul>
 *
 * <p><b>Keepalive Settings:</b> The server is configured with aggressive keepalive settings
 * to support long-lived bidirectional streams required by the Agent Service:
 * <ul>
 *   <li>Keepalive time: 30 seconds</li>
 *   <li>Keepalive timeout: 5 seconds</li>
 *   <li>Permit keepalive without calls: true</li>
 *   <li>Permit keepalive time: 5 minutes</li>
 *   <li>Connection idle/age limits: effectively infinite</li>
 * </ul>
 *
 * @see org.apache.airavata.agent.connection.service.handlers.AgentConnectionHandler
 * @see org.apache.airavata.config.AiravataServerProperties
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE) // Load last
@Lazy // Defer initialization
public class GrpcServerConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final AiravataServerProperties properties;

    public GrpcServerConfig(AiravataServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        mapGrpcPortProperty(environment);
    }

    private void mapGrpcPortProperty(ConfigurableEnvironment environment) {
        if (properties.services() != null && properties.services().grpc() != null) {
            var grpcPort = properties.services().grpc().server().port();
            var mappedProperties = new HashMap<String, Object>();
            mappedProperties.put("spring.grpc.server.port", grpcPort);
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource("grpcServerMappedProperties", mappedProperties));
        }
    }

    /**
     * Primary ServerBuilderCustomizer that sets all required gRPC properties.
     * Uses @DependsOn to ensure it loads after core infrastructure beans.
     *
     * @return customizer for NettyServerBuilder
     */
    @Bean
    @Primary
    @Order(Ordered.LOWEST_PRECEDENCE) // Configure last
    @DependsOn({"dataSource", "entityManagerFactory", "transactionManager"})
    public ServerBuilderCustomizer<NettyServerBuilder> keepAliveServerConfigurer() {
        return serverBuilder -> {
            serverBuilder
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .permitKeepAliveWithoutCalls(true)
                    .permitKeepAliveTime(5, TimeUnit.MINUTES)
                    .maxConnectionIdle(Long.MAX_VALUE, TimeUnit.DAYS) // effectively infinite
                    .maxConnectionAge(Long.MAX_VALUE, TimeUnit.DAYS) // effectively infinite
                    .maxConnectionAgeGrace(Long.MAX_VALUE, TimeUnit.DAYS); // effectively infinite
        };
    }
}
