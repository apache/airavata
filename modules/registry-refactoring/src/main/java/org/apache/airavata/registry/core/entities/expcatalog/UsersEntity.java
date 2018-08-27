package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
@IdClass(UserPK.class)
public class UsersEntity {
    private String airavataInternalUserId;
    private String userName;
    private String password;
    private String gatewayId;
    private GatewayEntity gateway;

    @Id
    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "AIRAVATA_INTERNAL_USER_ID")
    public String getAiravataInternalUserId() {
        return airavataInternalUserId;
    }

    public void setAiravataInternalUserId(String airavataInternalUserId) {
        this.airavataInternalUserId = airavataInternalUserId;
    }

    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID")
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST})
    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }
}
