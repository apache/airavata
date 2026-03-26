package org.apache.airavata.service.context;

import java.util.Collections;
import java.util.Map;

public class RequestContext {

    private final String userId;
    private final String gatewayId;
    private final String accessToken;
    private final Map<String, String> claims;

    public RequestContext(String userId, String gatewayId, String accessToken, Map<String, String> claims) {
        this.userId = userId;
        this.gatewayId = gatewayId;
        this.accessToken = accessToken;
        this.claims = Collections.unmodifiableMap(claims);
    }

    public String getUserId() { return userId; }
    public String getGatewayId() { return gatewayId; }
    public String getAccessToken() { return accessToken; }
    public Map<String, String> getClaims() { return claims; }
}
