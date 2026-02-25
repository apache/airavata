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
package org.apache.airavata.iam.model;

import org.apache.airavata.core.util.Constants;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static String userId() {
        return authzToken().getClaimsMap().get("userId");
    }

    public static String gatewayId() {
        return authzToken().getClaimsMap().get(Constants.GATEWAY_ID);
    }

    public static boolean isAuthenticated() {
        return AUTHZ_TOKEN.get() != null;
    }

    public static void clear() {
        AUTHZ_TOKEN.remove();
    }
}
