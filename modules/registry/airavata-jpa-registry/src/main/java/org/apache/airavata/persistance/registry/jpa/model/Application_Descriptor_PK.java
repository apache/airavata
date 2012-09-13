package org.apache.airavata.persistance.registry.jpa.model;


public class Application_Descriptor_PK {
    private String gateway_name;
    private String application_descriptor_ID;
    private String host_descriptor_ID;
    private String service_descriptor_ID;

    public Application_Descriptor_PK(String gateway_name, String application_descriptor_ID, String host_descriptor_ID, String service_descriptor_ID) {
        this.gateway_name = gateway_name;
        this.application_descriptor_ID = application_descriptor_ID;
        this.host_descriptor_ID = host_descriptor_ID;
        this.service_descriptor_ID = service_descriptor_ID;
    }

    public Application_Descriptor_PK() {
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

    public String getGateway_name() {
        return gateway_name;
    }

    public String getApplication_descriptor_ID() {
        return application_descriptor_ID;
    }

    public String getHost_descriptor_ID() {
        return host_descriptor_ID;
    }

    public String getService_descriptor_ID() {
        return service_descriptor_ID;
    }

    public void setGateway_name(String gateway_name) {
        this.gateway_name = gateway_name;
    }

    public void setApplication_descriptor_ID(String application_descriptor_ID) {
        this.application_descriptor_ID = application_descriptor_ID;
    }

    public void setHost_descriptor_ID(String host_descriptor_ID) {
        this.host_descriptor_ID = host_descriptor_ID;
    }

    public void setService_descriptor_ID(String service_descriptor_ID) {
        this.service_descriptor_ID = service_descriptor_ID;
    }
}
