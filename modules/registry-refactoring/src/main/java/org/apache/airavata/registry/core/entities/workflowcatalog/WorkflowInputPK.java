package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the workflow_input database table.
 */
@Embeddable
public class WorkflowInputPK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    private String templateId;

    @Column(name = "INPUT_KEY")
    private String inputKey;

    public WorkflowInputPK() {
    }

    public String getTemplateId() {

        return this.templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public String getInputKey() {

        return this.inputKey;
    }

    public void setInputKey(String inputKey) {

        this.inputKey = inputKey;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkflowInputPK)) {
            return false;
        }
        WorkflowInputPK castOther = (WorkflowInputPK) other;
        return
                this.templateId.equals(castOther.templateId)
                        && this.inputKey.equals(castOther.inputKey);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.templateId.hashCode();
        hash = hash * prime + this.inputKey.hashCode();

        return hash;
    }
}