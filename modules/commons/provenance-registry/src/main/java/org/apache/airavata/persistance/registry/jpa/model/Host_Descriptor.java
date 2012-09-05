package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;

@Entity
public class Host_Descriptor {
    @Id
    private String host_descriptor_ID;
    private String host_descriptor_xml;

    @ManyToOne
    @JoinColumn(name="gateway_ID")
    private Gateway gateway;

    @ManyToMany
    @JoinColumn(name="user_ID")
    private Users user;

    public String getHost_descriptor_ID() {
        return host_descriptor_ID;
    }

    public String getHost_descriptor_xml() {
        return host_descriptor_xml;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setHost_descriptor_ID(String host_descriptor_ID) {
        this.host_descriptor_ID = host_descriptor_ID;
    }

    public void setHost_descriptor_xml(String host_descriptor_xml) {
        this.host_descriptor_xml = host_descriptor_xml;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
