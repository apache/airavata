package org.apache.airavata.persistance.registry.jpa.model;


public class User_Workflow_PK {
    private String template_name;
    private String gateway_name;
    private String owner;

    public User_Workflow_PK(String template_name, String gateway_name) {
        this.template_name = template_name;
        this.gateway_name = gateway_name;
    }

    public User_Workflow_PK() {
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

    public String getTemplate_name() {
        return template_name;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
