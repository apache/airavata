package org.apache.airavata.registry.core.entities.expcatalog;

public class UserPK {
    private String gatewayId;
    private String userId;

    public UserPK() {

    }

    public UserPK(String gatewayId, String userId) {
        this.gatewayId = gatewayId;
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPK)) return false;

        UserPK userPK = (UserPK) o;

        if (!gatewayId.equals(userPK.gatewayId)) return false;
        return userId.equals(userPK.userId);
    }

    @Override
    public int hashCode() {
        int result = gatewayId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}
