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
package org.apache.airavata.research.service.config;

import io.grpc.netty.NettyServerBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.ServerBuilderCustomizer;

/**
 * Configuration for gRPC server with keepalive settings.
 * Programmatically sets all keepalive properties to prevent NullPointerException.
 *
 * This configuration is designed to load LAST, after all other services are initialized,
 * to ensure proper dependency ordering and avoid the Duration NPE in DefaultServerFactoryPropertyMapper.
 */
@Configuration
@ConditionalOnProperty(name = "services.research.enabled", havingValue = "true", matchIfMissing = false)
@Order(Ordered.LOWEST_PRECEDENCE) // Load last
@Lazy // Defer initialization
public class GrpcServerConfig {

    /**
     * Primary ServerBuilderCustomizer that sets all required gRPC properties.
     * Uses @DependsOn to ensure it loads after core infrastructure beans.
     *
     * @return customizer for NettyServerBuilder
     */
    @Bean
    @Primary
    @Order(Ordered.LOWEST_PRECEDENCE) // Configure last
    @DependsOn({"dataSource", "entityManagerFactory", "transactionManager", "quartzScheduler"})
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
