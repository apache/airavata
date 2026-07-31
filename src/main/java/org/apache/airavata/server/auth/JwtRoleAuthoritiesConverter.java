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
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Derives Spring Security authorities for a validated JWT by looking up the
 * token's username against {@link UserRoleLookupService}, rather than trusting
 * roles embedded in the token itself.
 *
 * <p>Authorities are plain role names (e.g. {@code "ADMIN"}), matching
 * {@code @PreAuthorize("hasAuthority('ADMIN')")} rather than {@code hasRole(...)},
 * which expects a {@code ROLE_} prefix.
 */
@Component
public class JwtRoleAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserRoleLookupService userRoleLookupService;

    public JwtRoleAuthoritiesConverter(UserRoleLookupService userRoleLookupService) {
        this.userRoleLookupService = userRoleLookupService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            username = jwt.getSubject();
        }
        return userRoleLookupService.getRoles(username).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
