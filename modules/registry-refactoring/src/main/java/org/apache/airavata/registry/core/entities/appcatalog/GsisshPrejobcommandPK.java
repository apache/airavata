package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the gsissh_prejobcommand database table.
 * 
 */
@Embeddable
public class GsisshPrejobcommandPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="SUBMISSION_ID", insertable=false, updatable=false)
	private String submissionId;

	@Column(name="COMMAND")
	private String command;

	public GsisshPrejobcommandPK() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GsisshPrejobcommandPK)) {
			return false;
		}
		GsisshPrejobcommandPK castOther = (GsisshPrejobcommandPK)other;
		return 
			this.submissionId.equals(castOther.submissionId)
			&& this.command.equals(castOther.command);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.submissionId.hashCode();
		hash = hash * prime + this.command.hashCode();
		
		return hash;
	}
}