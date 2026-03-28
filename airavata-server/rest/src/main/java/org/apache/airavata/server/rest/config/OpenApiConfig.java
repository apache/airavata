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
package org.apache.airavata.server.rest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${airavata.security.openid-url:}")
    private String openIdUrl;

    @Bean
    public OpenAPI airavataOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Airavata REST API")
                        .version("0.21-SNAPSHOT")
                        .description("Unified Airavata REST API"))
                .components(new Components()
                        .addSecuritySchemes(
                                "oauth2-pkce",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl(openIdUrl + "/protocol/openid-connect/auth")
                                                        .tokenUrl(openIdUrl + "/protocol/openid-connect/token")))))
                .addSecurityItem(new SecurityRequirement().addList("oauth2-pkce"));
    }

    @Bean
    public GroupedOpenApi researchGroup() {
        return GroupedOpenApi.builder()
                .group("research")
                .pathsToMatch("/api/v1/rf/**")
                .build();
    }

    @Bean
    public GroupedOpenApi agentGroup() {
        return GroupedOpenApi.builder()
                .group("agent")
                .pathsToMatch("/api/v1/agent/**", "/api/v1/plan/**", "/api/v1/exp/**")
                .build();
    }

    @Bean
    public GroupedOpenApi filesGroup() {
        return GroupedOpenApi.builder()
                .group("files")
                .pathsToMatch("/api/v1/files/**")
                .build();
    }

    @Bean
    public GroupedOpenApi kafkaGroup() {
        return GroupedOpenApi.builder()
                .group("kafka")
                .pathsToMatch("/api/v1/kafka/**")
                .build();
    }
}
