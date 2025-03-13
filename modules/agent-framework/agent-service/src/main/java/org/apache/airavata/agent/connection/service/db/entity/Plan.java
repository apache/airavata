package org.apache.airavata.agent.connection.service.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "PLAN")
public class Plan {

    @Id
    @Column(name = "PLAN_ID", nullable = false)
    private String id;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "GATEWAY_ID", nullable = false)
    private String gatewayId;

    @Column(name = "DATA", columnDefinition = "TEXT")
    private String data;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
