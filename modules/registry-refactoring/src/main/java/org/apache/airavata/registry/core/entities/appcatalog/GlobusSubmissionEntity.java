package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the globus_submission database table.
 * 
 */
@Entity
@Table(name = "globus_submission")
public class GlobusSubmissionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SUBMISSION_ID")
	private String submissionId;

	@Column(name = "RESOURCE_JOB_MANAGER")
	private String resourceJobManager;

	@Column(name = "SECURITY_PROTOCAL")
	private String securityProtocal;

	public GlobusSubmissionEntity() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getResourceJobManager() {
		return resourceJobManager;
	}

	public void setResourceJobManager(String resourceJobManager) {
		this.resourceJobManager = resourceJobManager;
	}

	public String getSecurityProtocal() {
		return securityProtocal;
	}

	public void setSecurityProtocal(String securityProtocal) {
		this.securityProtocal = securityProtocal;
	}
}