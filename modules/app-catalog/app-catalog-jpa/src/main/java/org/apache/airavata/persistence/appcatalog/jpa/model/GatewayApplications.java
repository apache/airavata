package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "GATEWAY_PROFILE")
@IdClass(GatewayApplicationsPK.class)
public class GatewayApplications {

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayID;

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationID;

    @Column(name = "IS_TURNED_ON")
    private boolean turnedOn;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfile gatewayProfile;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;

    public GatewayProfile getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfile gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
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

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }
}
