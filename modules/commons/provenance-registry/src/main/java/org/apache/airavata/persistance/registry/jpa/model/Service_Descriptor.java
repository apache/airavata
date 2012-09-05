package org.apache.airavata.persistance.registry.jpa.model;

import javax.persistence.*;

@Entity
public class Service_Descriptor {
    @Id
    private String service_descriptor_ID;
    private String service_descriptor_xml;

    @ManyToOne
    @JoinColumn(name="gateway_ID")
    private Gateway gateway;

    @ManyToMany
    @JoinColumn(name="user_ID")
    private Users user;

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public String getService_descriptor_xml() {
        return service_descriptor_xml;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }

    public void setService_descriptor_xml(String service_descriptor_xml) {
        this.service_descriptor_xml = service_descriptor_xml;
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

