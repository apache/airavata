package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the edge database table.
 */
@Embeddable
public class EdgePK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "EDGE_ID")
    private String edgeId;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    private String templateId;

    public EdgePK() {
    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
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
        if (!(other instanceof EdgePK)) {
            return false;
        }
        EdgePK castOther = (EdgePK) other;
        return
                this.edgeId.equals(castOther.edgeId)
                        && this.templateId.equals(castOther.templateId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.edgeId.hashCode();
        hash = hash * prime + this.templateId.hashCode();

        return hash;
    }
}