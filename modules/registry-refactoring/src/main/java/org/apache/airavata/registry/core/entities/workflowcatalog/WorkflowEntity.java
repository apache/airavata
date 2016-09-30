package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the workflow database table.
 */
@Entity
public class WorkflowEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TEMPLATE_ID")
    private String templateId;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "GRAPH")
    private String graph;

    @Column(name = "IMAGE")
    @Lob
    private byte[] image;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "WORKFLOW_NAME")
    private String workflowName;


    public WorkflowEntity() {
    }

    public String getTemplateId() {

        return this.templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    public String getCreatedUser() {

        return this.createdUser;
    }

    public void setCreatedUser(String createdUser) {

        this.createdUser = createdUser;
    }

    public Timestamp getCreationTime() {

        return this.creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {

        this.creationTime = creationTime;
    }

    public String getGatewayId() {

        return this.gatewayId;
    }

    public void setGatewayId(String gatewayId) {

        this.gatewayId = gatewayId;
    }

    public String getGraph() {

        return this.graph;
    }

    public void setGraph(String graph) {

        this.graph = graph;
    }

    public byte[] getImage() {

        return this.image;
    }

    public void setImage(byte[] image) {

        this.image = image;
    }

    public Timestamp getUpdateTime() {

        return this.updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {

        this.updateTime = updateTime;
    }

    public String getWorkflowName() {

        return this.workflowName;
    }

    public void setWorkflowName(String workflowName) {

        this.workflowName = workflowName;
    }


}