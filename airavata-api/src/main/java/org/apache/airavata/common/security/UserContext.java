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
package org.apache.airavata.common.security;

import java.util.Map;
import org.apache.airavata.common.config.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.profile.commons.user.entities.UserProfileEntity;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<UserProfileEntity> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }

    public static UserProfileEntity user() {
        return CURRENT_USER.get();
    }

    public static void setUser(UserProfileEntity user) {
        CURRENT_USER.set(user);
    }

    public static String userId() {
        var token = authzToken();
        if (token == null) return null;
        Map<String, String> claims = token.getClaimsMap();
        return claims != null ? claims.get(Constants.USER_NAME) : null;
    }

    public static String gatewayId() {
        var token = authzToken();
        if (token == null) return null;
        Map<String, String> claims = token.getClaimsMap();
        return claims != null ? claims.get(Constants.GATEWAY_ID) : null;
    }

    public static boolean isAuthenticated() {
        return authzToken() != null;
    }

    public static void clear() {
        AUTHZ_TOKEN.remove();
        CURRENT_USER.remove();
    }
}
