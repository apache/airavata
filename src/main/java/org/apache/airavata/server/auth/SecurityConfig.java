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
package org.apache.airavata.server.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Validates bearer JWTs issued by Keycloak and populates the security context with
 * authorities resolved via {@link JwtRoleAuthoritiesConverter} (looked up by
 * username, not trusted from the token), so that {@code @PreAuthorize(...)} works
 * on service methods.
 *
 * <p>The JWK set is fetched lazily on first token verification (rather than via
 * Spring Boot's {@code issuer-uri} auto-configuration, which eagerly calls the
 * OIDC discovery endpoint at startup) so the server still starts when Keycloak
 * isn't running locally; only requests bearing a token fail until it is.
 *
 * <p>The filter chain itself permits all requests — enforcement happens at the
 * method level via {@code @PreAuthorize}, so unannotated endpoints remain
 * reachable without a token during this bootstrap phase.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String jwkSetUri;
    private final JwtRoleAuthoritiesConverter jwtRoleAuthoritiesConverter;

    public SecurityConfig(
            @Value("${airavata.security.openid-url}") String openIdUrl,
            JwtRoleAuthoritiesConverter jwtRoleAuthoritiesConverter) {
        this.jwkSetUri = openIdUrl + "/protocol/openid-connect/certs";
        this.jwtRoleAuthoritiesConverter = jwtRoleAuthoritiesConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtRoleAuthoritiesConverter);
        authenticationConverter.setPrincipalClaimName("preferred_username");

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));

        return http.build();
    }
}
