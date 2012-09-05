package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;

@Entity
public class Users {

    @ManyToMany
    @JoinColumn(name="gateway_ID")
    private Gateway gateway;

    @Id
    private int user_ID;
    private String password;
    private String user_name;
    private int gateway_ID;

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public int getUser_ID() {
        return user_ID;
    }

    public String getUser_name() {
        return user_name;
    }

    public int getGateway_ID() {
        return gateway_ID;
    }

    public void setUser_ID(int user_ID) {
        this.user_ID = user_ID;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setGateway_ID(int gateway_ID) {
        this.gateway_ID = gateway_ID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
