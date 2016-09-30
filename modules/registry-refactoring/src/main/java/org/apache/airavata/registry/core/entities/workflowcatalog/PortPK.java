package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the port database table.
 * 
 */
@Embeddable
public class PortPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="PORT_ID")
	private String portId;

	@Column(name="TEMPLATE_ID", insertable=false, updatable=false)
	private String templateId;

	public PortPK() {
	}

	public String getPortId() {
		return portId;
	}

	public void setPortId(String portId) {
		this.portId = portId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PortPK)) {
			return false;
		}
		PortPK castOther = (PortPK)other;
		return 
			this.portId.equals(castOther.portId)
			&& this.templateId.equals(castOther.templateId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.portId.hashCode();
		hash = hash * prime + this.templateId.hashCode();
		
		return hash;
	}
}