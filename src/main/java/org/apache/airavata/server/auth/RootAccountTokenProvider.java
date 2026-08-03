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
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

/**
 * Generates and provides a random root account token on application startup.
 * The token is printed to the console and used for Super Admin authentication.
 * Enabled via airavata.security.root-account.enabled property (default: true).
 */
@Component
@ConditionalOnProperty(name = "airavata.security.root-account.enabled", havingValue = "true", matchIfMissing = true)
public class RootAccountTokenProvider {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String ROOT_USER = "root";

    private final String rootAccountToken;

    public RootAccountTokenProvider() {
        this.rootAccountToken = generateRandomToken();
        printToken();
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString();
    }

    private void printToken() {
        System.out.println("\n========================================");
        System.out.println("ROOT ACCOUNT TOKEN (Super Admin):");
        System.out.println(rootAccountToken);
        System.out.println("========================================\n");
    }

    public String getToken() {
        return rootAccountToken;
    }

    public boolean isRootToken(String token) {
        return token != null && token.equals(rootAccountToken);
    }

    OAuth2AuthenticatedPrincipal createRootPrincipal() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("sub", ROOT_USER);
        attributes.put("username", ROOT_USER);
        attributes.put("active", true);

        Collection<GrantedAuthority> authorities = Set.of(SUPER_ADMIN_ROLE).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();

        return new DefaultOAuth2AuthenticatedPrincipal(ROOT_USER, attributes, authorities);
    }
}
