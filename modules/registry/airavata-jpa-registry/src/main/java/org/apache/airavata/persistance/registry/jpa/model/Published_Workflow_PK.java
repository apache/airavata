package org.apache.airavata.persistance.registry.jpa.model;

public class Published_Workflow_PK {
    private String gateway_name;
    private String publish_workflow_name;

    public Published_Workflow_PK(String gateway_name, String publish_workflow_name) {
        this.gateway_name = gateway_name;
        this.publish_workflow_name = publish_workflow_name;
    }

    public Published_Workflow_PK() {
        ;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getPublish_workflow_name() {
        return publish_workflow_name;
    }

    public void setPublish_workflow_name(String publish_workflow_name) {
        this.publish_workflow_name = publish_workflow_name;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
