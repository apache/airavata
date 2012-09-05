package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.sql.Date;

@Entity
@IdClass(Published_Workflow_PK.class)
public class Published_Workflow {

    @Id
    private String publish_workflow_name;
    private String version;
    private Date published_date;
    private String workflow_content;

    @Id
    @OneToMany()
    @JoinColumn(name = "gateway_ID")
    private Gateway gateway;

    public String getPublish_workflow_name() {
        return publish_workflow_name;
    }

    public String getVersion() {
        return version;
    }

    public Date getPublished_date() {
        return published_date;
    }

    public String getWorkflow_content() {
        return workflow_content;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setPublish_workflow_name(String publish_workflow_name) {
        this.publish_workflow_name = publish_workflow_name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPublished_date(Date published_date) {
        this.published_date = published_date;
    }

    public void setWorkflow_content(String workflow_content) {
        this.workflow_content = workflow_content;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }
}

class Published_Workflow_PK {
    private int gateway_ID;
    private String publish_workflow_name;

    private Published_Workflow_PK() {

    }

    public int getGateway_ID() {
        return gateway_ID;
    }

    public String getPublish_workflow_name() {
        return publish_workflow_name;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public void setPublish_workflow_name(String publish_workflow_name) {
        this.publish_workflow_name = publish_workflow_name;
    }

}

