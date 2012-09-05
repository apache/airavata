package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;

@Entity
public class Project {
    @Id
    private int project_ID;
    private String project_name;

    @ManyToOne
    @JoinColumn(name="gateway_ID")
    private Gateway gateway;

    @ManyToMany
    @JoinColumn(name = "user_ID")
    private Users users;

    public int getProject_ID() {
        return project_ID;
    }

    public String getProject_name() {
        return project_name;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setProject_ID(int project_ID) {
        this.project_ID = project_ID;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}

