package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the ssh_job_submission database table.
 * 
 */
@Entity
@Table(name="ssh_job_submission")
public class SshJobSubmissionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="JOB_SUBMISSION_INTERFACE_ID")
	private String jobSubmissionInterfaceId;

	@Column(name="ALTERNATIVE_SSH_HOSTNAME")
	private String alternativeSshHostname;

	@Column(name="CREATION_TIME")
	private Timestamp creationTime;

	@Column(name="MONITOR_MODE")
	private String monitorMode;

	@Column(name="SECURITY_PROTOCOL")
	private String securityProtocol;

	@Column(name="SSH_PORT")
	private int sshPort;

	@Column(name="UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name="RESOURCE_JOB_MANAGER_ID")
	private String resourceJobManagerId;

	public SshJobSubmissionEntity() {
	}

	public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}

	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
	}

	public String getAlternativeSshHostname() {
		return alternativeSshHostname;
	}

	public void setAlternativeSshHostname(String alternativeSshHostname) {
		this.alternativeSshHostname = alternativeSshHostname;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public String getMonitorMode() {
		return monitorMode;
	}

	public void setMonitorMode(String monitorMode) {
		this.monitorMode = monitorMode;
	}

	public String getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}

	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId = resourceJobManagerId;
	}
}