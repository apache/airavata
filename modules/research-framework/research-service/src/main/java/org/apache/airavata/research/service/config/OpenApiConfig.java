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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${airavata.openid.url}")
    private String openIdConfigURL;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(globalHeaderCustomizer())
                .build();
    }

    @Bean
    public OpenAPI researchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Research Service API").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2-pkce"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(
                                "oauth2-pkce",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl(
                                                                openIdConfigURL + "/protocol/openid-connect/auth")
                                                        .tokenUrl(openIdConfigURL + "/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "openid")
                                                                .addString("email", "email"))))));
    }

    @Bean
    public OpenApiCustomizer globalHeaderCustomizer() {
        System.out.println("Applying global header customizer...");
        return openApi -> {
            Parameter claimsHeader = new Parameter()
                    .in("header")
                    .schema(new StringSchema())
                    .name("X-Claims")
                    .description("{userName: ..., gatewayID: ...}")
                    .required(false);

            openApi.getPaths().values().forEach(pathItem -> {
                pathItem.readOperations().forEach(operation -> {
                    operation.addParametersItem(claimsHeader);
                });
            });
        };
    }
}
