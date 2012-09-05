package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Published_Workflow;

import java.sql.Date;
import java.util.List;

public class PublishWorkflowResource extends AbstractResource {
    private String name;
    private String version;
    private Date publishedDate;
    private String content;
    private int gatewayID;

    public PublishWorkflowResource() {
    }

    public PublishWorkflowResource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public String getContent() {
        return content;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGatewayID() {
        return gatewayID;
    }

    public void setGatewayID(int gatewayID) {
        this.gatewayID = gatewayID;
    }

    public Resource create(ResourceType type) {
        return null;
    }

    public void remove(ResourceType type, Object name) {

    }

    public Resource get(ResourceType type, Object name) {
        return null;
    }

    public List<Resource> get(ResourceType type) {
        return null;
    }

    public void save() {
        begin();
        Published_Workflow publishedWorkflow = new Published_Workflow();
        publishedWorkflow.setPublish_workflow_name(name);
        publishedWorkflow.setPublished_date(publishedDate);
        publishedWorkflow.setVersion(version);
        publishedWorkflow.setWorkflow_content(content);
        Gateway gateway = new Gateway();
        gateway.setGateway_ID(gatewayID);
        publishedWorkflow.setGateway(gateway);
        em.persist(gateway);
        end();
    }

    public boolean isExists(ResourceType type, Object name) {
        return false;
    }
}
