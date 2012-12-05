package org.apache.airavata.common.context;

/**
 * The request context class. This will be local to a thread.
 * User data that needs to propagate relevant to a request will be stored here.
 * We use thread local globals to store request data.
 * Currently we only store user identity.
 */
public class RequestContext {

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    /**
     * User associated with current request.
     */
    private String userIdentity;

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * The gateway id.
     */
    private String gatewayId;


}
