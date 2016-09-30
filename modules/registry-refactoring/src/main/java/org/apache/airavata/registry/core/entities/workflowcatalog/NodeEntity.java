package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the node database table.
 */
@Entity
public class NodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private NodePK id;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    @Column(name = "COMPONENT_STATUS_ID")
    private String componentStatusId;

    @Column(name = "CREATED_TIME")
    private Timestamp createdTime;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TEMPLATE_ID")
    private String templateId;

    public NodeEntity() {
    }

    public NodePK getId() {
        return id;
    }

    public void setId(NodePK id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getComponentStatusId() {
        return componentStatusId;
    }

    public void setComponentStatusId(String componentStatusId) {
        this.componentStatusId = componentStatusId;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}