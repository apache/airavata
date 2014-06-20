package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "GATEWAY_PROFILE")
public class Application {

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationID;
    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    // the gateway that owns the application
    @Column(name = "GATEWAY_ID")
    private String gatewayID;

    //whether the appliation is publicly available or not
    @Column(name = "IS_PUBLIC")
    private boolean published;
    @Column(name = "APPLICATION_DESCRIPTION")
    private String applicationDescription;


    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "GATEWAY_ID")
    private GatewayProfile gatewayProfile;

    public GatewayProfile getGatewayProfile() {
        return gatewayProfile;
    }

    public void setGatewayProfile(GatewayProfile gatewayProfile) {
        this.gatewayProfile = gatewayProfile;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(String gatewayID) {
        this.gatewayID = gatewayID;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }
}
