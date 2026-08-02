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
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;

/**
 * Validates bearer tokens issued by CILogon and populates the security context
 * with profile claims and authorities resolved via {@link UserRoleOpaqueTokenIntrospector}
 * (authorities looked up by username, not trusted from the token), so that
 * {@code @PreAuthorize(...)} works on service methods and controllers can read
 * the caller's email/name off the principal.
 *
 * <p>CILogon access tokens for standard (non "Full Service") OAuth clients are
 * opaque strings rather than self-contained JWTs, so tokens are validated via
 * CILogon's introspection endpoint (RFC 7662) rather than by decoding a JWT
 * locally against a JWK set; profile attributes come from a second call to
 * CILogon's userinfo endpoint, since introspection alone only returns a small,
 * fixed claim set.
 *
 * <p>The filter chain itself permits all requests — enforcement happens at the
 * method level via {@code @PreAuthorize}, so unannotated endpoints remain
 * reachable without a token during this bootstrap phase.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String introspectionUri;
    private final String userInfoUri;
    private final String clientId;
    private final String clientSecret;
    private final UserRoleLookupService userRoleLookupService;

    public SecurityConfig(
            @Value("${airavata.security.cilogon.introspection-uri:https://cilogon.org/oauth2/introspect}")
                    String introspectionUri,
            @Value("${airavata.security.cilogon.userinfo-uri:https://cilogon.org/oauth2/userinfo}")
                    String userInfoUri,
            @Value("${airavata.security.cilogon.client-id}") String clientId,
            @Value("${airavata.security.cilogon.client-secret}") String clientSecret,
            UserRoleLookupService userRoleLookupService) {
        this.introspectionUri = introspectionUri;
        this.userInfoUri = userInfoUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userRoleLookupService = userRoleLookupService;
    }

    @Bean
    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
        OpaqueTokenIntrospector delegate = SpringOpaqueTokenIntrospector.withIntrospectionUri(introspectionUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        CILogonUserInfoClient userInfoClient = new CILogonUserInfoClient(RestClient.create(), userInfoUri);
        return new UserRoleOpaqueTokenIntrospector(delegate, userRoleLookupService, userInfoClient);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OpaqueTokenIntrospector opaqueTokenIntrospector)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.opaqueToken(opaque -> opaque.introspector(opaqueTokenIntrospector)));

        return http.build();
    }
}
