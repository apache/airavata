package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.*;

@Entity
@Table(name = "GATEWAY_PROFILE")
@IdClass(ApplicationDeploymentPK.class)
public class ApplicationDeployment {

    @Id
    @Column(name = "DEPLOYMENT_ID")
    private String deploymentID;

    @Id
    @Column(name = "APPLICATION_ID")
    private String applicationID;

    @Column(name = "DEPLOYMENT_HOST_NAME")
    private String deploymentHostName;


    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "DEPLOYMENT_ID")
    private Deployment deployment;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "APPLICATION_ID")
    private Application application;

    public String getDeploymentID() {
        return deploymentID;
    }

    public void setDeploymentID(String deploymentID) {
        this.deploymentID = deploymentID;
    }

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getDeploymentHostName() {
        return deploymentHostName;
    }

    public void setDeploymentHostName(String deploymentHostName) {
        this.deploymentHostName = deploymentHostName;
    }

    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
