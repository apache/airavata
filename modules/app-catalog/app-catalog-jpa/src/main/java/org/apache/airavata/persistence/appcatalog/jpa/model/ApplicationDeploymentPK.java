package org.apache.airavata.persistence.appcatalog.jpa.model;

public class ApplicationDeploymentPK {
    private String deploymentID;
    private String applicationID;

    public ApplicationDeploymentPK(String deploymentID, String applicationID) {
        this.deploymentID = deploymentID;
        this.applicationID = applicationID;
    }

    public ApplicationDeploymentPK() {
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

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
}
