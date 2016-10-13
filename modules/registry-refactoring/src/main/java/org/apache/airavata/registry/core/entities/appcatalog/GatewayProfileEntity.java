package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the gateway_profile database table.
 */
@Entity
@Table(name = "gateway_profile")
public class GatewayProfileEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "CS_TOKEN")
    private String csToken;

    @Column(name = "IDENTITY_SERVER_PWD_CRED_TOKEN")
    private String identityServerPwdCredToken;

    @Column(name = "IDENTITY_SERVER_TENANT")
    private String identityServerTenant;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;


    public GatewayProfileEntity() {
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getCsToken() {
        return csToken;
    }

    public void setCsToken(String csToken) {
        this.csToken = csToken;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}