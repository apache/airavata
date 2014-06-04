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
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.cpi.RegistryException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class ServiceDescriptorResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ServiceDescriptorResource.class);
    private String serviceDescName;
    private String gatewayName;
    private String userName;
    private String content;

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

    public Resource create(ResourceType type) throws RegistryException{
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayName(gatewayName);
            applicationDescriptorResource.setHostDescName(getServiceDescName());
            return applicationDescriptorResource;
        }
        logger.error("Unsupported resource type for service descriptor resource.", new IllegalArgumentException());
        throw new IllegalArgumentException("Unsupported resource type for service descriptor resource.");
    }

    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for service descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for service descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }


    public List<Resource> get(ResourceType type) throws RegistryException{
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            if (type == ResourceType.APPLICATION_DESCRIPTOR) {
                em = ResourceUtils.getEntityManager();
                em.getTransaction().begin();
                QueryGenerator queryGenerator = new QueryGenerator(APPLICATION_DESCRIPTOR);
                queryGenerator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
                queryGenerator.setParameter(ApplicationDescriptorConstants.SERVICE_DESC_ID, serviceDescName);
                Query q = queryGenerator.selectQuery(em);
                List results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                        ApplicationDescriptorResource applicationDescriptorResource = (ApplicationDescriptorResource) Utils.getResource(ResourceType.APPLICATION_DESCRIPTOR, applicationDescriptor);
                        resourceList.add(applicationDescriptorResource);
                    }
                }
                em.getTransaction().commit();
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return resourceList;
    }

    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            Service_Descriptor existingServiceDesc = em.find(Service_Descriptor.class, new Service_Descriptor_PK(gatewayName, serviceDescName));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Service_Descriptor serviceDescriptor = new Service_Descriptor();
            serviceDescriptor.setService_descriptor_ID(getServiceDescName());
            Gateway gateway = em.find(Gateway.class, gatewayName);
            serviceDescriptor.setGateway(gateway);
            serviceDescriptor.setGateway_name(gateway.getGateway_name());
            byte[] bytes = content.getBytes();
            serviceDescriptor.setService_descriptor_xml(bytes);
            Users user = em.find(Users.class, userName);
            serviceDescriptor.setUser(user);
            if (existingServiceDesc != null) {
                existingServiceDesc.setUser(user);
                existingServiceDesc.setService_descriptor_xml(bytes);
                existingServiceDesc.setGateway(gateway);
                existingServiceDesc.setGateway_name(gateway.getGateway_name());
                serviceDescriptor = em.merge(existingServiceDesc);
            } else {
                em.persist(serviceDescriptor);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

    }

    public boolean isExists(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for service descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

	public void setServiceDescName(String serviceDescName) {
		this.serviceDescName = serviceDescName;
	}
}
