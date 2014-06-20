package org.apache.airavata.persistence.appcatalog.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResource {


    @Id
    @Column(name = "RESOURCE_ID")
    private String applicationID;

    @Column(name = "HOST_NAME")
    private String hostname;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "PREFERRED_JOB_SUBMISSION_PROTOCOL ")
    private String preferredJobSubmissionProtocol;

    public String getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreferredJobSubmissionProtocol() {
        return preferredJobSubmissionProtocol;
    }

    public void setPreferredJobSubmissionProtocol(String preferredJobSubmissionProtocol) {
        this.preferredJobSubmissionProtocol = preferredJobSubmissionProtocol;
    }
}
