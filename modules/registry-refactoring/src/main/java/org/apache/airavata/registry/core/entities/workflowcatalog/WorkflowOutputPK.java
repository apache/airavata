package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the workflow_output database table.
 * 
 */
@Embeddable
public class WorkflowOutputPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="TEMPLATE_ID", insertable=false, updatable=false)
	private String templateId;

	@Column(name="OUTPUT_KEY")
	private String outputKey;

	public WorkflowOutputPK() {
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof WorkflowOutputPK)) {
			return false;
		}
		WorkflowOutputPK castOther = (WorkflowOutputPK)other;
		return 
			this.templateId.equals(castOther.templateId)
			&& this.outputKey.equals(castOther.outputKey);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.templateId.hashCode();
		hash = hash * prime + this.outputKey.hashCode();
		
		return hash;
	}
}