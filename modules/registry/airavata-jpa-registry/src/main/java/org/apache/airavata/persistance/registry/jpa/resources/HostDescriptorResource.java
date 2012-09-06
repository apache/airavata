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
import org.apache.airavata.persistance.registry.jpa.model.Host_Descriptor;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class HostDescriptorResource extends AbstractResource {
    private String hostDescName;
    private String gatewayName;
    private int userID;
    private String content;

    public HostDescriptorResource(String hostDescName) {
        this.hostDescName = hostDescName;
    }

    public HostDescriptorResource() {
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getHostDescName() {
        return hostDescName;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getContent() {
        return content;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Resource create(ResourceType type) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayName(gatewayName);
            applicationDescriptorResource.setHostDescName(hostDescName);
            return applicationDescriptorResource;
        }
        return null;
    }

    public void remove(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("Delete p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.host_descriptor_ID = :host_desc_name and p.gateway_name =:gate_name");
            q.setParameter("app_desc_id", name);
            q.setParameter("host_desc_name", hostDescName);
            q.setParameter("gate_name", gatewayName);
            q.executeUpdate();
            end();
        }
    }

    public Resource get(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.host_descriptor_ID =:host_desc_name and p.gateway_name =:gate_name");
            q.setParameter("app_desc_id", name);
            q.setParameter("host_desc_name", hostDescName);
            q.setParameter("gate_name", gatewayName);
            Application_Descriptor eappDesc = (Application_Descriptor) q.getSingleResult();
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(eappDesc.getApplication_descriptor_ID());
            applicationDescriptorResource.setGatewayName(eappDesc.getGateway().getGateway_name());
            applicationDescriptorResource.setContent(eappDesc.getApplication_descriptor_xml());
            applicationDescriptorResource.setHostDescName(eappDesc.getHost_descriptor().getHost_descriptor_ID());
            applicationDescriptorResource.setServiceDescName(hostDescName);
            end();
            return applicationDescriptorResource;
        }
        return null;
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.gateway_name =:gate_name and p.host_descriptor_ID =:host_desc_id");
            q.setParameter("gate_name", gatewayName);
            q.setParameter("host_desc_id", hostDescName);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                    ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID());
                    applicationDescriptorResource.setGatewayName(gatewayName);
                    applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
                    applicationDescriptorResource.setHostDescName(hostDescName);
                    applicationDescriptorResource.setServiceDescName(applicationDescriptor.getService_descriptor().getService_descriptor_ID());
                    resourceList.add(applicationDescriptorResource);
                }
            }
            end();
        }
        return resourceList;
    }

    public void save() {
        begin();
        Host_Descriptor hostDescriptor = new Host_Descriptor();
        hostDescriptor.setHost_descriptor_ID(hostDescName);
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gatewayName);
        hostDescriptor.setGateway(gateway);
        hostDescriptor.setHost_descriptor_xml(content);
        em.persist(hostDescriptor);
        end();

    }

    public boolean isExists(ResourceType type, Object name) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            begin();
            Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.host_descriptor_ID =:host_desc_id and p.application_descriptor_ID =:app_dist_id");
            q.setParameter("host_desc_id", hostDescName);
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
