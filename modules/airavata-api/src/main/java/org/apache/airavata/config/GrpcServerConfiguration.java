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
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.ServerBuilderCustomizer;

/**
 * gRPC keepalive configuration for long-lived bidirectional streams (Agent, Research).
 *
 * <p>Port is configured via standard {@code spring.grpc.server.port} in application.properties.
 * This class only customizes keepalive settings for persistent connections.
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
@Lazy
public class GrpcServerConfiguration {

    @Bean
    @Primary
    @Order(Ordered.LOWEST_PRECEDENCE)
    @DependsOn({"dataSource", "entityManagerFactory", "transactionManager"})
    public ServerBuilderCustomizer<NettyServerBuilder> keepAliveServerConfigurer() {
        return serverBuilder -> {
            serverBuilder
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(5, TimeUnit.SECONDS)
                    .permitKeepAliveWithoutCalls(true)
                    .permitKeepAliveTime(5, TimeUnit.MINUTES)
                    .maxConnectionIdle(Long.MAX_VALUE, TimeUnit.DAYS)
                    .maxConnectionAge(Long.MAX_VALUE, TimeUnit.DAYS)
                    .maxConnectionAgeGrace(Long.MAX_VALUE, TimeUnit.DAYS);
        };
    }
}
