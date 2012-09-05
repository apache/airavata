package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Application_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Service_Descriptor;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ServiceDescriptorResource extends AbstractResource {
    private String serviceDescName;
    private int gatewayID;
    private int userID;
    private String content;


    public ServiceDescriptorResource(String serviceDescName) {
        this.serviceDescName = serviceDescName;
    }

    public ServiceDescriptorResource() {

    }

    public String getServiceDescName() {
        return serviceDescName;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getGatewayID() {
        return gatewayID;
    }

    public String getContent() {
        return content;
    }

    public void setGatewayID(int gatewayID) {
        this.gatewayID = gatewayID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayID(gatewayID);
            applicationDescriptorResource.setHostDescName(serviceDescName);
            return applicationDescriptorResource;
        }
        return null;
    }

    public void remove(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("Delete p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.service_descriptor_ID = :service_desc_name and p.gateway_ID = :gate_ID");
            q.setParameter("app_desc_id", name);
            q.setParameter("service_desc_name", serviceDescName);
            q.setParameter("gate_ID", gatewayID);
            q.executeUpdate();
            end();
        }

    }

    public Resource get(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.service_descriptor_ID = :service_desc_name and p.gateway_ID =:gate_ID");
            q.setParameter("app_desc_id", name);
            q.setParameter("service_desc_name", serviceDescName);
            q.setParameter("gate_ID", gatewayID);
            Application_Descriptor eappDesc = (Application_Descriptor) q.getSingleResult();
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(eappDesc.getApplication_descriptor_ID());
            applicationDescriptorResource.setGatewayID(eappDesc.getGateway().getGateway_ID());
            applicationDescriptorResource.setContent(eappDesc.getApplication_descriptor_xml());
            applicationDescriptorResource.setHostDescName(eappDesc.getHost_descriptor().getHost_descriptor_ID());
            applicationDescriptorResource.setServiceDescName(serviceDescName);
            end();
            return applicationDescriptorResource;
        }
        return null;
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.gateway_ID =:gate_ID and p.service_descriptor_ID = :service_desc_name");
            q.setParameter("gate_ID", gatewayID);
            q.setParameter("service_desc_name", serviceDescName);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                    ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID());
                    applicationDescriptorResource.setGatewayID(gatewayID);
                    applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
                    applicationDescriptorResource.setHostDescName(applicationDescriptor.getHost_descriptor().getHost_descriptor_ID());
                    applicationDescriptorResource.setServiceDescName(serviceDescName);
                    resourceList.add(applicationDescriptorResource);
                }
            }
            end();
        }
        return resourceList;
    }

    public void save() {
        begin();
        Service_Descriptor serviceDescriptor = new Service_Descriptor();
        serviceDescriptor.setService_descriptor_ID(serviceDescName);
        Gateway gateway = new Gateway();
        gateway.setGateway_ID(gatewayID);
        serviceDescriptor.setGateway(gateway);
        serviceDescriptor.setService_descriptor_xml(content);
        em.persist(serviceDescriptor);
        end();

    }

    public boolean isExists(ResourceType type, Object name) {
        return false;
    }
}
