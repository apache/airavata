package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Application_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Host_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Service_Descriptor;

import java.util.List;

public class ApplicationDescriptorResource extends AbstractResource{
    private String name;
    private int gatewayID;
    private int userID;
    private String content;
    private String hostDescName;
    private String serviceDescName;

    public ApplicationDescriptorResource(String name) {
        this.name = name;
    }

    public ApplicationDescriptorResource() {

    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public int getGatewayID() {
        return gatewayID;
    }

    public String getContent() {
        return content;
    }

    public String getHostDescName() {
        return hostDescName;
    }

    public String getServiceDescName() {
        return serviceDescName;
    }

    public void setGatewayID(int gatewayID) {
        this.gatewayID = gatewayID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setHostDescName(String hostDescName) {
        this.hostDescName = hostDescName;
    }

    public void setServiceDescName(String serviceDescName) {
        this.serviceDescName = serviceDescName;
    }

    public Resource create(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Application_Descriptor applicationDescriptor = new Application_Descriptor();
        applicationDescriptor.setApplication_descriptor_ID(name);
        Gateway gateway = new Gateway();
        gateway.setGateway_ID(gatewayID);
        applicationDescriptor.setGateway(gateway);
        Host_Descriptor hostDescriptor = new Host_Descriptor();
        hostDescriptor.setHost_descriptor_ID(hostDescName);
        Service_Descriptor serviceDescriptor = new Service_Descriptor();
        serviceDescriptor.setService_descriptor_ID(serviceDescName);
        applicationDescriptor.setApplication_descriptor_xml(content);
        em.persist(applicationDescriptor);
        end();

    }

    public boolean isExists(ResourceType type, Object name) {
        return false;
    }
}
