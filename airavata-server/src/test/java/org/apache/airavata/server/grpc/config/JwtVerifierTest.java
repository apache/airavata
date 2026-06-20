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
package org.apache.airavata.server.grpc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure identity-derivation logic of {@link JwtVerifier} — no signing or live Keycloak.
 * The signature/expiry/issuer verification path is exercised end-to-end by the real-Keycloak integration check.
 */
class JwtVerifierTest {

    @Test
    void realmFromIssuer_parsesRealmAsGatewayId() {
        assertEquals("default", JwtVerifier.realmFromIssuer("https://auth.airavata.host/realms/default"));
        assertEquals("default", JwtVerifier.realmFromIssuer("https://auth.airavata.host/realms/default/"));
        assertEquals("gw42", JwtVerifier.realmFromIssuer("https://kc.example.org/auth/realms/gw42"));
        assertNull(JwtVerifier.realmFromIssuer("https://auth.airavata.host/no-realm-here"));
        assertNull(JwtVerifier.realmFromIssuer(null));
    }

    @Test
    void toVerifiedToken_derivesUserGatewayAndRolesFromClaims() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://auth.airavata.host/realms/default")
                .claim("preferred_username", "default-admin")
                .claim("realm_access", Map.of("roles", List.of("admin-rw", "user")))
                .build();

        VerifiedToken v = JwtVerifier.toVerifiedToken(claims);

        assertEquals("default-admin", v.userName());
        assertEquals("default", v.gatewayId());
        assertTrue(v.roles().contains("admin-rw"));
        assertTrue(v.roles().contains("user"));
    }

    @Test
    void toVerifiedToken_handlesMissingUsernameAndRoles() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://auth.airavata.host/realms/default")
                .build();

        VerifiedToken v = JwtVerifier.toVerifiedToken(claims);

        assertNull(v.userName());
        assertEquals("default", v.gatewayId());
        assertTrue(v.roles().isEmpty());
    }
}
