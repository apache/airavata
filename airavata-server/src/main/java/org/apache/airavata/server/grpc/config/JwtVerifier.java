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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies a Keycloak access token (RS256 signature against the realm JWKS, {@code exp}, and issuer) and
 * extracts {@code realm_access.roles}. The issuer is taken from the token's own {@code iss} claim, and a JWKS
 * processor is cached per issuer so multi-tenant (multi-realm) tokens are each verified against their own realm.
 *
 * <p>Verification is fail-closed but non-rejecting: a missing, malformed, expired, or unverifiable token simply
 * yields no roles (logged), so the caller resolves to a non-admin. Roles are therefore only ever trusted when
 * they come from a signature-verified token — a forged token cannot assert admin.
 */
public final class JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, ConfigurableJWTProcessor<SecurityContext>> PROCESSORS = new ConcurrentHashMap<>();

    private JwtVerifier() {}

    /** Verifies the token and returns its realm roles, or an empty list if verification fails. */
    public static List<String> verifyAndExtractRoles(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return List.of();
        }
        try {
            String issuer = unverifiedIssuer(accessToken);
            if (issuer == null) {
                log.warn("Access token has no issuer claim; no realm roles extracted");
                return List.of();
            }
            JWTClaimsSet claims = processorFor(issuer).process(accessToken, null);
            Map<String, Object> realmAccess = claims.getJSONObjectClaim("realm_access");
            if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
                return List.of();
            }
            List<String> result = roles.stream().map(String::valueOf).collect(Collectors.toList());
            log.debug("Verified realm roles from {}: {}", issuer, result);
            return result;
        } catch (Exception e) {
            log.warn("JWT verification failed; no realm roles extracted: {}", e.getMessage());
            return List.of();
        }
    }

    /** Reads the {@code iss} claim from the token payload without verifying the signature. */
    private static String unverifiedIssuer(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }
        Map<String, Object> payload =
                objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), new TypeReference<>() {});
        Object iss = payload.get("iss");
        return iss == null ? null : iss.toString();
    }

    private static ConfigurableJWTProcessor<SecurityContext> processorFor(String issuer) {
        return PROCESSORS.computeIfAbsent(issuer, iss -> {
            try {
                JWKSource<SecurityContext> jwkSource = JWKSourceBuilder.create(
                                URI.create(iss + "/protocol/openid-connect/certs")
                                        .toURL())
                        .build();
                ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
                processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
                processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                        new JWTClaimsSet.Builder().issuer(iss).build(), Set.of("exp")));
                return processor;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to build JWT processor for issuer " + iss, e);
            }
        });
    }
}
