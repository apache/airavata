package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the unicore_submission database table.
 * 
 */
@Entity
@Table(name="unicore_submission")
public class UnicoreSubmission implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="SUBMISSION_ID")
	private String submissionId;

	@Column(name="SECURITY_PROTOCAL")
	private String securityProtocal;

	@Column(name="UNICORE_ENDPOINT_URL")
	private String unicoreEndpointUrl;

	public UnicoreSubmission() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getSecurityProtocal() {
		return securityProtocal;
	}

	public void setSecurityProtocal(String securityProtocal) {
		this.securityProtocal = securityProtocal;
	}

	public String getUnicoreEndpointUrl() {
		return unicoreEndpointUrl;
	}

	public void setUnicoreEndpointUrl(String unicoreEndpointUrl) {
		this.unicoreEndpointUrl = unicoreEndpointUrl;
	}
}