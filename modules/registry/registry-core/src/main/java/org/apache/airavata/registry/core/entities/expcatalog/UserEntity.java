package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
@IdClass(UserPK.class)
public class UserEntity {
    private String airavataInternalUserId;
    private String userId;
    private String password;
    private String gatewayId;
    private GatewayEntity gateway;

    @Id
    @Column(name = "USER_NAME")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @ManyToOne(targetEntity = GatewayEntity.class)
    @JoinColumn(name = "GATEWAY_ID")
    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }
}
