package org.apache.airavata.persistance.registry.jpa.model;

public class Host_Descriptor_PK {
    private String gateway_name;
    private String host_descriptor_ID;

    public Host_Descriptor_PK() {
        ;
    }

    public Host_Descriptor_PK(String gateway_name, String host_descriptor_ID) {
        this.gateway_name = gateway_name;
        this.host_descriptor_ID = host_descriptor_ID;
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

    public String getHost_descriptor_ID() {
        return host_descriptor_ID;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public void setHost_descriptor_ID(String host_descriptor_ID) {
        this.host_descriptor_ID = host_descriptor_ID;
    }
}
