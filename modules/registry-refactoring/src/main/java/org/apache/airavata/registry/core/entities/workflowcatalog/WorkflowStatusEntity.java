package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the workflow_status database table.
 */
@Entity
@Table(name = "workflow_status")
public class WorkflowStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private WorkflowStatusPK id;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "STATE")
    private String state;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    public WorkflowStatusEntity() {
    }

    public WorkflowStatusPK getId() {
        return id;
    }

    public void setId(WorkflowStatusPK id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}