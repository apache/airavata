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
 * <p>Verification is fail-closed: any missing, malformed, expired, or unverifiable token throws
 * {@link TokenVerificationException}, and the caller rejects the request. Identity (user + gateway) and roles
 * are taken solely from the verified token; client-asserted headers are never trusted.
 */
public final class JwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(JwtVerifier.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, ConfigurableJWTProcessor<SecurityContext>> PROCESSORS = new ConcurrentHashMap<>();

    private JwtVerifier() {}

    /**
     * Verifies the access token (RS256 signature against the realm JWKS, {@code exp}, and issuer) and returns
     * the caller's identity and roles. Fail-closed: a missing, malformed, expired, or unverifiable token throws
     * {@link TokenVerificationException}.
     */
    public static VerifiedToken verify(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new TokenVerificationException("Missing access token");
        }
        String issuer;
        try {
            issuer = unverifiedIssuer(accessToken);
        } catch (Exception e) {
            throw new TokenVerificationException("Malformed access token", e);
        }
        if (issuer == null) {
            throw new TokenVerificationException("Access token has no issuer claim");
        }
        try {
            JWTClaimsSet claims = processorFor(issuer).process(accessToken, null);
            return toVerifiedToken(claims);
        } catch (Exception e) {
            throw new TokenVerificationException("Access token verification failed: " + e.getMessage(), e);
        }
    }

    /** Pure mapping of a signature-verified claims set to identity + roles (no signature work). */
    static VerifiedToken toVerifiedToken(JWTClaimsSet claims) throws Exception {
        String userName = claims.getStringClaim("preferred_username");
        String gatewayId = realmFromIssuer(claims.getIssuer());
        List<String> roles = List.of();
        Map<String, Object> realmAccess = claims.getJSONObjectClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> r) {
            roles = r.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return new VerifiedToken(userName, gatewayId, roles);
    }

    /** Parses the realm (gateway id) from a Keycloak issuer URL: {@code .../realms/<realm>} -> {@code <realm>}. */
    static String realmFromIssuer(String issuer) {
        if (issuer == null) {
            return null;
        }
        int idx = issuer.indexOf("/realms/");
        if (idx < 0) {
            return null;
        }
        String realm = issuer.substring(idx + "/realms/".length());
        int slash = realm.indexOf('/');
        return slash >= 0 ? realm.substring(0, slash) : realm;
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
