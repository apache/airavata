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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * Wraps CILogon's token introspection response, enriches it with profile claims
 * (email, name, etc.) from CILogon's userinfo endpoint, and populates
 * authorities
 * resolved via {@link UserRoleLookupService} (looked up by username, not
 * trusted
 * from the token), so that {@code @PreAuthorize(...)} works on service methods.
 *
 * <p>
 * CILogon access tokens for standard (non "Full Service") OAuth clients are
 * opaque, not self-contained JWTs, so tokens are validated by calling CILogon's
 * introspection endpoint rather than by decoding a JWT locally against a JWK
 * set.
 * Introspection alone only returns a small, fixed claim set — profile
 * attributes
 * come from a second call to userinfo, using the same bearer token.
 *
 * <p>
 * Not a Spring bean itself (it implements the same interface as the delegate
 * it wraps, which would make component-scanning it ambiguous/circular) —
 * {@link SecurityConfig} constructs it explicitly around the real introspector.
 */
public class UserRoleOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;
    private final UserRoleLookupService userRoleLookupService;
    private final CILogonUserInfoClient userInfoClient;
    private final Optional<RootAccountTokenProvider> rootAccountTokenProvider;

    public UserRoleOpaqueTokenIntrospector(
            OpaqueTokenIntrospector delegate,
            UserRoleLookupService userRoleLookupService,
            CILogonUserInfoClient userInfoClient,
            Optional<RootAccountTokenProvider> rootAccountTokenProvider) {
        this.delegate = delegate;
        this.userRoleLookupService = userRoleLookupService;
        this.userInfoClient = userInfoClient;
        this.rootAccountTokenProvider = rootAccountTokenProvider;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        if (rootAccountTokenProvider.isPresent() && rootAccountTokenProvider.get().isRootToken(token)) {
            return rootAccountTokenProvider.get().createRootPrincipal();
        }

        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);

        Map<String, Object> attributes = new LinkedHashMap<>(principal.getAttributes());
        // Introspection claims (active, scope, exp, sub, ...) win on conflict; userinfo
        // only fills in gaps.
        userInfoClient.fetchUserInfo(token).forEach(attributes::putIfAbsent);

        String username = (String) attributes.get("username");
        if (username == null) {
            username = (String) attributes.get("preferred_username");
        }
        if (username == null) {
            username = principal.getName();
        }

        Collection<GrantedAuthority> authorities = userRoleLookupService.getRoles(username).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new DefaultOAuth2AuthenticatedPrincipal(principal.getName(), attributes, authorities);
    }
}
