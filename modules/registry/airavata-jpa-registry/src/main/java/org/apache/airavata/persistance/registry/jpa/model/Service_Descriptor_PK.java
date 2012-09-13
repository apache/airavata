package org.apache.airavata.persistance.registry.jpa.model;


public class Service_Descriptor_PK {
    private String gateway_name;
    private String service_descriptor_ID;

    public Service_Descriptor_PK() {
        ;
    }

    public Service_Descriptor_PK(String gateway_name, String service_descriptor_ID) {
        this.gateway_name = gateway_name;
        this.service_descriptor_ID = service_descriptor_ID;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getGateway_name() {
        return gateway_name;
    }

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }
}
