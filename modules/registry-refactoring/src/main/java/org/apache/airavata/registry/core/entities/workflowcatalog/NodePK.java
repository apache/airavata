package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;

/**
 * The primary key class for the node database table.
 */
@Embeddable
public class NodePK implements Serializable {
    //default serial version id, required for serializable classes.
    private static final long serialVersionUID = 1L;

    @Column(name = "NODE_ID")
    private String nodeId;

    @Column(name = "TEMPLATE_ID", insertable = false, updatable = false)
    private String templateId;

    public NodePK() {
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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
        if (!(other instanceof NodePK)) {
            return false;
        }
        NodePK castOther = (NodePK) other;
        return
                this.nodeId.equals(castOther.nodeId)
                        && this.templateId.equals(castOther.templateId);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.nodeId.hashCode();
        hash = hash * prime + this.templateId.hashCode();

        return hash;
    }
}