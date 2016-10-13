package appcatlog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the job_submission_interface database table.
 * 
 */
@Embeddable
public class JobSubmissionInterfacePK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="COMPUTE_RESOURCE_ID", insertable=false, updatable=false)
	private String computeResourceId;

	@Column(name="JOB_SUBMISSION_INTERFACE_ID")
	private String jobSubmissionInterfaceId;

	public JobSubmissionInterfacePK() {
	}
	public String getComputeResourceId() {
		return this.computeResourceId;
	}
	public void setComputeResourceId(String computeResourceId) {
		this.computeResourceId = computeResourceId;
	}
	public String getJobSubmissionInterfaceId() {
		return this.jobSubmissionInterfaceId;
	}
	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof JobSubmissionInterfacePK)) {
			return false;
		}
		JobSubmissionInterfacePK castOther = (JobSubmissionInterfacePK)other;
		return 
			this.computeResourceId.equals(castOther.computeResourceId)
			&& this.jobSubmissionInterfaceId.equals(castOther.jobSubmissionInterfaceId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.computeResourceId.hashCode();
		hash = hash * prime + this.jobSubmissionInterfaceId.hashCode();
		
		return hash;
	}
}