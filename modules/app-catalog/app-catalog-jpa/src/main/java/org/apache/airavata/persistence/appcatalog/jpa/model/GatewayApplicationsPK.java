package org.apache.airavata.persistence.appcatalog.jpa.model;

public class GatewayApplicationsPK {

    private String gatewayID;
    private String applicationID;

    public GatewayApplicationsPK(String gatewayID, String applicationID) {
        this.gatewayID = gatewayID;
        this.applicationID = applicationID;
    }

    public GatewayApplicationsPK() {
    }
    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }
}
