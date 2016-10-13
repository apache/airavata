package org.apache.airavata.registry.core.entities.appcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the application_input database table.
 * 
 */
@Embeddable
public class ApplicationInputPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="INTERFACE_ID", insertable=false, updatable=false)
	private String interfaceId;

	@Column(name="INPUT_KEY")
	private String inputKey;

	public ApplicationInputPK() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getInputKey() {
		return inputKey;
	}

	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ApplicationInputPK)) {
			return false;
		}
		ApplicationInputPK castOther = (ApplicationInputPK)other;
		return 
			this.interfaceId.equals(castOther.interfaceId)
			&& this.inputKey.equals(castOther.inputKey);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.interfaceId.hashCode();
		hash = hash * prime + this.inputKey.hashCode();
		
		return hash;
	}
}