package org.apache.airavata.common.security;

import org.apache.airavata.common.config.Constants;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.security.profile.commons.user.entities.UserProfileEntity;

import java.util.Map;

public class UserContext {

    private static final ThreadLocal<AuthzToken> AUTHZ_TOKEN = new ThreadLocal<>();
    private static final ThreadLocal<UserProfileEntity> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static AuthzToken authzToken() { return AUTHZ_TOKEN.get(); }
    public static void setAuthzToken(AuthzToken token) { AUTHZ_TOKEN.set(token); }
    public static UserProfileEntity user() { return CURRENT_USER.get(); }
    public static void setUser(UserProfileEntity user) { CURRENT_USER.set(user); }

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

    public static boolean isAuthenticated() { return authzToken() != null; }

    public static void clear() {
        AUTHZ_TOKEN.remove();
        CURRENT_USER.remove();
    }
}
