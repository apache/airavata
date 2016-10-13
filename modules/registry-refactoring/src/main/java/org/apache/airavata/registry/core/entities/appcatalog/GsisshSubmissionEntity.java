package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

/**
 * The persistent class for the gsissh_submission database table.
 * 
 */
@Entity
@Table(name = "gsissh_submission")
public class GsisshSubmission implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SUBMISSION_ID")
	private String submissionId;

	@Column(name = "INSTALLED_PATH")
	private String installedPath;

	@Column(name = "MONITOR_MODE")
	private String monitorMode;

	@Column(name = "RESOURCE_JOB_MANAGER")
	private String resourceJobManager;

	@Column(name = "SSH_PORT")
	private int sshPort;

	public GsisshSubmission() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getInstalledPath() {
		return installedPath;
	}

	public void setInstalledPath(String installedPath) {
		this.installedPath = installedPath;
	}

	public String getMonitorMode() {
		return monitorMode;
	}

	public void setMonitorMode(String monitorMode) {
		this.monitorMode = monitorMode;
	}

	public String getResourceJobManager() {
		return resourceJobManager;
	}

	public void setResourceJobManager(String resourceJobManager) {
		this.resourceJobManager = resourceJobManager;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
}