package org.apache.aiaravata.application.catalog.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "UNICORE_SUBMISSION")
public class UnicoreJobSubmission {
	@Id
    @Column(name = "SUBMISSION_ID")
    private String submissionID;
    @Column(name = "SECURITY_PROTOCAL")
    private String securityProtocol;

    @Column(name = "UNICORE_ENDPOINT_URL")
    private String unicoreEndpointUrl;

    public String getUnicoreEndpointUrl() {
		return unicoreEndpointUrl;
	}

    public void setUnicoreEndpointUrl(String unicoreEndpointUrl) {
		this.unicoreEndpointUrl = unicoreEndpointUrl;
	}

	public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

}
