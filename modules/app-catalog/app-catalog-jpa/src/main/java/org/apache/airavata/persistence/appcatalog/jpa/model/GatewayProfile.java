package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GATEWAY_PROFILE")
public class GatewayProfile {

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayID;
    @Column(name = "GATEWAY_NAME")
    private String gatewayName;
    @Column(name = "GATEWAY_DESCRIPTION")
    private String gatewayDescription;

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getGatewayDescription() {
        return gatewayDescription;
    }

    public void setGatewayDescription(String gatewayDescription) {
        this.gatewayDescription = gatewayDescription;
    }
}
