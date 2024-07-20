package org.apache.airavata.agent.connection.service;

import org.apache.airavata.model.security.AuthzToken;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }

    public static String username() {
        return AUTHZ_TOKEN.get().getClaimsMap().get("username");
    }

    public static String gatewayId() {
        return AUTHZ_TOKEN.get().getClaimsMap().get("gatewayId");
    }
}
