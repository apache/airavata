package org.apache.airavata.agent.connection.service;

import org.apache.airavata.model.security.AuthzToken;

import java.util.Map;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();

    public static AuthzToken authzToken() {
        return AUTHZ_TOKEN.get();
    }

    public static void setAuthzToken(AuthzToken token) {
        AUTHZ_TOKEN.set(token);
    }

    public static String username() {
        return getClaim("userName");
    }

    public static String gatewayId() {
        return getClaim("gatewayID");
    }

    private static String getClaim(String claimId) {
        return AUTHZ_TOKEN.get().getClaimsMap().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(claimId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing '" + claimId + "' claim in the authentication token"));
    }
}
