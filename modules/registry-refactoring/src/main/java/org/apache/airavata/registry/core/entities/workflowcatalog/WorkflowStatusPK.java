package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the workflow_status database table.
 */
@Embeddable
public class WorkflowStatusPK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "STATUS_ID")
    private String statusId;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    private String templateId;

    public WorkflowStatusPK() {
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
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
        if (!(other instanceof WorkflowStatusPK)) {
            return false;
        }
        WorkflowStatusPK castOther = (WorkflowStatusPK) other;
        return
                this.statusId.equals(castOther.statusId)
                        && this.templateId.equals(castOther.templateId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.statusId.hashCode();
        hash = hash * prime + this.templateId.hashCode();

        return hash;
    }
}