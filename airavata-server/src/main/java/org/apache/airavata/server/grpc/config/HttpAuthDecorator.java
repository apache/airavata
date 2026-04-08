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
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.DecoratingHttpServiceFunction;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import java.util.HashMap;
import java.util.Map;
import org.apache.airavata.config.Constants;
import org.apache.airavata.config.UserContext;
import org.apache.airavata.model.security.proto.AuthzToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpAuthDecorator implements DecoratingHttpServiceFunction {

    private static final Logger log = LoggerFactory.getLogger(HttpAuthDecorator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HttpResponse serve(HttpService delegate, ServiceRequestContext ctx, HttpRequest req) throws Exception {
        String authHeader = req.headers().get("authorization");
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        if (accessToken == null) {
            return HttpResponse.of(HttpStatus.UNAUTHORIZED);
        }

        Map<String, String> claimsMap = new HashMap<>();
        String claimsHeader = req.headers().get("x-claims");
        if (claimsHeader != null && !claimsHeader.isBlank()) {
            try {
                claimsMap = objectMapper.readValue(claimsHeader, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse x-claims header: {}", e.getMessage());
            }
        }

        // Fall back to individual headers
        if (!claimsMap.containsKey(Constants.USER_NAME)) {
            String userName = req.headers().get("x-user-name");
            if (userName != null) {
                claimsMap.put(Constants.USER_NAME, userName);
            }
        }
        if (!claimsMap.containsKey(Constants.GATEWAY_ID)) {
            String gatewayId = req.headers().get("x-gateway-id");
            if (gatewayId != null) {
                claimsMap.put(Constants.GATEWAY_ID, gatewayId);
            }
        }

        AuthzToken authzToken = AuthzToken.newBuilder()
                .setAccessToken(accessToken)
                .putAllClaimsMap(claimsMap)
                .build();
        UserContext.setAuthzToken(authzToken);

        try {
            return delegate.serve(ctx, req);
        } finally {
            UserContext.clear();
        }
    }
}
