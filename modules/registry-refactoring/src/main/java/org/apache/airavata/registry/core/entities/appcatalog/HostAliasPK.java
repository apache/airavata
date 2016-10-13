package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the host_alias database table.
 * 
 */
@Embeddable
public class HostAliasPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="RESOURCE_ID", insertable=false, updatable=false)
	private String resourceId;

	@Column(name = "ALIAS")
	private String alias;

	public HostAliasPK() {
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof HostAliasPK)) {
			return false;
		}
		HostAliasPK castOther = (HostAliasPK)other;
		return 
			this.resourceId.equals(castOther.resourceId)
			&& this.alias.equals(castOther.alias);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.resourceId.hashCode();
		hash = hash * prime + this.alias.hashCode();
		
		return hash;
	}
}