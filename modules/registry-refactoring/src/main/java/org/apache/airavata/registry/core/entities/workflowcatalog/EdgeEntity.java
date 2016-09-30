package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the edge database table.
 */
@Entity
public class EdgeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private EdgePK id;

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

    public EdgeEntity() {
    }

    public EdgePK getId() {
        return id;
    }

    public void setId(EdgePK id) {
        this.id = id;
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