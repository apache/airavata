package org.apache.airavata.research.service.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${airavata.openid.url}")
    private String openIdConfigURL;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI researchServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Research Service API")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2-pkce"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("oauth2-pkce",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("https://auth.dev.cybershuttle.org/realms/default/protocol/openid-connect/auth")
                                                        .tokenUrl("https://auth.dev.cybershuttle.org/realms/default/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "openid")
                                                                .addString("email", "email"))))));


    }

}
