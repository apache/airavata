package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.Column;
import javax.persistence.Id;

public class UserPK {
    private String gatewayId;
    private String userName;

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }


    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPK that = (UserPK) o;

        if (getGatewayId() != null ? !getGatewayId().equals(that.getGatewayId()) : that.getGatewayId() != null) return false;
        if (getUserName() != null ? !getUserName().equals(that.getUserName()) : that.getUserName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getGatewayId() != null ? getGatewayId().hashCode() : 0;
        result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
        return result;
    }
}
