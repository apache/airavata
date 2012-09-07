/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
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
    private String gatewayName;
    private String userName;
    private String content;


    public ServiceDescriptorResource(String serviceDescName) {
        this.serviceDescName = serviceDescName;
    }

    public ServiceDescriptorResource() {

    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getUserName() {
        return userName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServiceDescName() {
        return serviceDescName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayName(gatewayName);
            applicationDescriptorResource.setHostDescName(serviceDescName);
            return applicationDescriptorResource;
        }
        return null;
    }

    public void remove(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("Delete p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.service_descriptor_ID = :service_desc_name and p.gateway_name =:gate_name");
            q.setParameter("app_desc_id", name);
            q.setParameter("service_desc_name", serviceDescName);
            q.setParameter("gate_name", gatewayName);
            q.executeUpdate();
            end();
        }

    }

    public void removeMe(Object[] keys) {

    }

    public Resource get(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.service_descriptor_ID = :service_desc_name and p.gateway_name =:gate_name");
            q.setParameter("app_desc_id", name);
            q.setParameter("service_desc_name", serviceDescName);
            q.setParameter("gate_name", gatewayName);
            Application_Descriptor eappDesc = (Application_Descriptor) q.getSingleResult();
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(eappDesc.getApplication_descriptor_ID(),
                    eappDesc.getGateway().getGateway_name(), eappDesc.getHost_descriptor_ID(), eappDesc.getService_descriptor_ID());
            applicationDescriptorResource.setContent(eappDesc.getApplication_descriptor_xml());
            applicationDescriptorResource.setUpdatedUser(eappDesc.getUser().getUser_name());
            end();
            return applicationDescriptorResource;
        }
        return null;
    }

    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Service_Descriptor p WHERE p.service_descriptor_ID = :service_desc_name");
        q.setParameter("service_desc_name", keys[0]);
        Service_Descriptor serviceDescriptor = (Service_Descriptor)q.getSingleResult();
        ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource(serviceDescriptor.getService_descriptor_ID());
        serviceDescriptorResource.setGatewayName(serviceDescriptor.getGateway().getGateway_name());
        serviceDescriptorResource.setUserName(serviceDescriptor.getUser().getUser_name());
        serviceDescriptorResource.setContent(serviceDescriptor.getService_descriptor_xml());
        end();
        list.add(serviceDescriptorResource);
        return list;
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.gateway_name =:gate_name and p.service_descriptor_ID = :service_desc_name");
            q.setParameter("gate_name", gatewayName);
            q.setParameter("service_desc_name", serviceDescName);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                    ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID(),
                            applicationDescriptor.getGateway().getGateway_name(),
                            applicationDescriptor.getHost_descriptor_ID(),
                            applicationDescriptor.getService_descriptor_ID());
                    applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
                    applicationDescriptorResource.setUpdatedUser(applicationDescriptor.getUser().getUser_name());
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
        gateway.setGateway_name(gatewayName);
        serviceDescriptor.setGateway(gateway);
        serviceDescriptor.setService_descriptor_xml(content);
        em.persist(serviceDescriptor);
        end();

    }

    public boolean isExists(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.service_descriptor_ID =:service_desc_id and p.application_descriptor_ID =:app_dist_id");
            q.setParameter("service_desc_id", serviceDescName);
            q.setParameter("app_dist_id", name);
            Application_Descriptor applicationDescriptor = (Application_Descriptor) q.getSingleResult();
            if (applicationDescriptor != null) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
