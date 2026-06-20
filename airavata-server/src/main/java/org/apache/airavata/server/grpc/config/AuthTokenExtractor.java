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

import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.config.Constants;
import org.apache.airavata.model.security.proto.AuthzToken;

/**
 * Shared Bearer-token handling for the gRPC ({@link GrpcAuthInterceptor}) and HTTP ({@link HttpAuthDecorator})
 * authentication paths. Identity and roles come solely from the signature-verified token ({@link VerifiedToken});
 * client-asserted headers (e.g. {@code x-claims}) are never consulted.
 */
public final class AuthTokenExtractor {

    private AuthTokenExtractor() {}

    /** Returns the bearer access token, or {@code null} if the header is absent or not a Bearer token. */
    public static String stripBearer(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Builds an {@link AuthzToken} whose claims map (user, gateway, realm roles) is populated solely from the
     * verified token. Downstream {@code UserContext.userId()/gatewayId()/roles()} read these verified values.
     */
    public static AuthzToken buildAuthzToken(String accessToken, VerifiedToken verified) {
        Map<String, String> claims = new HashMap<>();
        if (verified.userName() != null) {
            claims.put(Constants.USER_NAME, verified.userName());
        }
        if (verified.gatewayId() != null) {
            claims.put(Constants.GATEWAY_ID, verified.gatewayId());
        }
        claims.put(Constants.REALM_ROLES, String.join(",", verified.roles()));
        return AuthzToken.newBuilder()
                .setAccessToken(accessToken)
                .putAllClaimsMap(claims)
                .build();
    }
}
