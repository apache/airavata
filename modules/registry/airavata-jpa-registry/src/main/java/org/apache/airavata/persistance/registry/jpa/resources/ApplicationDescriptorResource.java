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
import org.apache.airavata.persistance.registry.jpa.model.*;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDescriptorResource extends AbstractResource {
    private String name;
    private String gatewayName;
    private String updatedUser;
    private String content;
    private String hostDescName;
    private String serviceDescName;
    
    public ApplicationDescriptorResource(String name) {
        this.setName(name);
    }
    
    public ApplicationDescriptorResource(String name, String gatewayName, String hostDescName, String serviceDescName) {
        this.setName(name);
        this.gatewayName = gatewayName;
        this.hostDescName = hostDescName;
        this.serviceDescName = serviceDescName;
    }

    public ApplicationDescriptorResource() {
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public String getUpdatedUser() {
        return updatedUser;
    }

    public void setUpdatedUser(String updatedUser) {
        this.updatedUser = updatedUser;
    }

    public String getName() {
        return name;
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

    /**
     * key should be gateway_name, application_descriptor_ID, host_descriptor_ID, service_descriptor_ID
     * @param keys
     */
    public void removeMe(Object[] keys) {
        begin();
        Query q = em.createQuery("Delete p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_id and p.gateway_name=:gate_name and p.host_descriptor_ID =:host_id " +
                "and p.service_descriptor_ID =: service_desc");
        q.setParameter("gate_name", keys[0]);
        q.setParameter("app_desc_id", keys[1]);
        q.setParameter("host_id", keys[2]);
        q.setParameter("service_desc", keys[3]);
        q.executeUpdate();
        end();
    }

    public Resource get(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

    /**
     * keys should contain gateway_name, application_descriptor_ID, host_descriptor_ID, service_descriptor_ID
     * @param keys
     * @return
     */
    public List<Resource> getMe(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        begin();
        Query q = em.createQuery("SELECT p FROM Application_Descriptor p WHERE p.application_descriptor_ID = :app_desc_name and p.gateway_name=:gate_name and p.host_descriptor_ID =:host_id " +
                "and p.service_descriptor_ID =: service_desc");
        q.setParameter("gate_name", keys[0]);
        q.setParameter("app_desc_id", keys[1]);
        q.setParameter("host_id", keys[2]);
        q.setParameter("service_desc", keys[3]);
        Application_Descriptor applicationDescriptor = (Application_Descriptor)q.getSingleResult();
        ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource(applicationDescriptor.getApplication_descriptor_ID(),
                applicationDescriptor.getGateway().getGateway_name(), applicationDescriptor.getHost_descriptor_ID(), applicationDescriptor.getService_descriptor_ID());
        applicationDescriptorResource.setUpdatedUser(applicationDescriptor.getUser().getUser_name());
        applicationDescriptorResource.setContent(applicationDescriptor.getApplication_descriptor_xml());
        end();
        list.add(applicationDescriptorResource);
        return list;
    }

    public List<Resource> get(ResourceType type) {
        throw new UnsupportedOperationException();
    }

    public void save() {
        begin();
        Application_Descriptor applicationDescriptor = new Application_Descriptor();
        applicationDescriptor.setApplication_descriptor_ID(getName());
        Gateway gateway = new Gateway();
        gateway.setGateway_name(gatewayName);
        applicationDescriptor.setGateway(gateway);
        Users user = new Users();
        user.setUser_name(updatedUser);
        applicationDescriptor.setUser(user);
        applicationDescriptor.setApplication_descriptor_xml(content);
        applicationDescriptor.setService_descriptor_ID(serviceDescName);
        applicationDescriptor.setHost_descriptor_ID(hostDescName);
        em.persist(applicationDescriptor);
        end();

    }

    public void save(boolean isAppendable) {

    }

    public boolean isExists(ResourceType type, Object name) {
        throw new UnsupportedOperationException();
    }

	public void setName(String name) {
		this.name = name;
	}
}
