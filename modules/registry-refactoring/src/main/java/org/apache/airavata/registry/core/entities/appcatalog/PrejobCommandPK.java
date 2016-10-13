package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the prejob_command database table.
 * 
 */
@Embeddable
public class PrejobCommandPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="APPDEPLOYMENT_ID", insertable=false, updatable=false)
	private String appdeploymentId;

	@Column(name="COMMAND")
	private String command;

	public PrejobCommandPK() {
	}

	public String getAppdeploymentId() {
		return appdeploymentId;
	}

	public void setAppdeploymentId(String appdeploymentId) {
		this.appdeploymentId = appdeploymentId;
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
		if (!(other instanceof PrejobCommandPK)) {
			return false;
		}
		PrejobCommandPK castOther = (PrejobCommandPK)other;
		return 
			this.appdeploymentId.equals(castOther.appdeploymentId)
			&& this.command.equals(castOther.command);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.appdeploymentId.hashCode();
		hash = hash * prime + this.command.hashCode();
		
		return hash;
	}
}