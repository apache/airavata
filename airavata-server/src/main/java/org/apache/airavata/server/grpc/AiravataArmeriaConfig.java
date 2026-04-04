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
package org.apache.airavata.server.grpc;

import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.server.cors.CorsService;
import com.linecorp.armeria.server.docs.DocService;
import com.linecorp.armeria.server.grpc.GrpcService;
import com.linecorp.armeria.spring.ArmeriaServerConfigurator;
import io.grpc.BindableService;
import java.util.List;
import org.apache.airavata.agent.config.AgentServiceConfig;
import org.apache.airavata.config.ConditionalOnServer;
import org.apache.airavata.research.config.ResearchServiceConfig;
import org.apache.airavata.server.file.FileController;
import org.apache.airavata.server.grpc.config.GrpcAuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnServer("grpc")
@Import({AgentServiceConfig.class, ResearchServiceConfig.class})
@ComponentScan(
        basePackages = {
            "org.apache.airavata.storage.service",
            "org.apache.airavata.storage.config",
            "org.apache.airavata.orchestration.service",
            "org.apache.airavata.orchestration.workflow",
            "org.apache.airavata.orchestration.infrastructure",
            "org.apache.airavata.orchestration.validation",
            "org.apache.airavata.orchestration.scheduling",
            "org.apache.airavata.orchestration.event",
            "org.apache.airavata.orchestration.util",
            "org.apache.airavata.iam.service"
        })
public class AiravataArmeriaConfig {

    @Bean
    public ArmeriaServerConfigurator grpcServerConfigurator(
            List<BindableService> grpcServices, GrpcAuthInterceptor authInterceptor) {
        return builder -> {
            GrpcService grpcService = GrpcService.builder()
                    .addServices(grpcServices)
                    .intercept(authInterceptor)
                    .enableHttpJsonTranscoding(true)
                    .build();

            builder.service(grpcService);

            builder.serviceUnder("/docs", DocService.builder().build());

            builder.decorator(CorsService.builderForAnyOrigin()
                    .allowRequestMethods(
                            HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)
                    .allowRequestHeaders("Authorization", "Content-Type", "X-Claims")
                    .newDecorator());
        };
    }

    @Bean
    public ArmeriaServerConfigurator fileServerConfigurator(FileController fileController) {
        return builder -> builder.annotatedService("/api/v1/files", fileController);
    }
}
