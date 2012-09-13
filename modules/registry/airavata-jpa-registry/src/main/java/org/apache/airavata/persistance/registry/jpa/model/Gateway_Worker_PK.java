package org.apache.airavata.persistance.registry.jpa.model;

public class Gateway_Worker_PK {
    private String gateway_name;
    private String user_name;

    public Gateway_Worker_PK(String gateway_name, String user_name) {
        this.gateway_name = gateway_name;
        this.user_name = user_name;
    }

    public Gateway_Worker_PK() {
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

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }
}
