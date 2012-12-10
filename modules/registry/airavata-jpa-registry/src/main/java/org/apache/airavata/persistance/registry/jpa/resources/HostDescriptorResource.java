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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HostDescriptorResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(HostDescriptorResource.class);
    private String hostDescName;
    private String gatewayName;
    private String userName;
    private String content;

    /**
     *
     */
    public HostDescriptorResource() {
    }

    /**
     *
     * @return user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @return  host descriptor name
     */
    public String getHostDescName() {
        return hostDescName;
    }

    /**
     *
     * @return gateway name
     */
    public String getGatewayName() {
        return gatewayName;
    }

    /**
     *
     * @return  content of the host descriptor
     */
    public String getContent() {
        return content;
    }

    /**
     *
     * @param gatewayName gateway name
     */
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    /**
     *
     * @param content content of the host descriptor
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Host descriptor can create an application descriptor
     * @param type child resource type
     * @return child resource
     */
    public Resource create(ResourceType type) {
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
            applicationDescriptorResource.setGatewayName(gatewayName);
            applicationDescriptorResource.setHostDescName(getHostDescName());
            return applicationDescriptorResource;
        }else{
            logger.error("Unsupported resource type for host descriptor resource.", new IllegalArgumentException());
            throw new IllegalArgumentException("Unsupported resource type for host descriptor resource.");
        }

    }

    /**
     * Host descriptor by alone cannot remove any other resource types
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for host descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * Host descriptor by alone cannot get any other resource types
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for host descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * key should be host_descriptor_name
     * @param keys host descriptor names
     * @return list of host descriptors
     */
    public List<Resource> populate(Object[] keys) {
        List<Resource> list = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator = new QueryGenerator(HOST_DESCRIPTOR);
        generator.setParameter(HostDescriptorConstants.GATEWAY_NAME, keys [0]);
        generator.setParameter(HostDescriptorConstants.HOST_DESC_ID, keys[1]);
        Query q = generator.selectQuery(em);
        Host_Descriptor hostDescriptor = (Host_Descriptor)q.getSingleResult();
        HostDescriptorResource hostDescriptorResource =
                (HostDescriptorResource)Utils.getResource(ResourceType.HOST_DESCRIPTOR, hostDescriptor);
        em.getTransaction().commit();
        em.close();
        list.add(hostDescriptorResource);
        return list;
    }

    /**
     * Host descriptors can get a list of application descriptors
     * @param type child resource type
     * @return list of child resources
     */
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        if (type == ResourceType.APPLICATION_DESCRIPTOR) {
            EntityManager em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(APPLICATION_DESCRIPTOR);
            generator.setParameter(ApplicationDescriptorConstants.GATEWAY_NAME, gatewayName);
            generator.setParameter(ApplicationDescriptorConstants.HOST_DESC_ID, getHostDescName());
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Application_Descriptor applicationDescriptor = (Application_Descriptor) result;
                    ApplicationDescriptorResource applicationDescriptorResource =
                            (ApplicationDescriptorResource)Utils.getResource(
                                    ResourceType.APPLICATION_DESCRIPTOR, applicationDescriptor);
                    resourceList.add(applicationDescriptorResource);
                }
            }
            em.getTransaction().commit();
            em.close();
        }
        return resourceList;
    }

    /**
     * save host descriptor to the database
     */
    public void save() {
        try {
            EntityManager em = ResourceUtils.getEntityManager();
            Host_Descriptor existingHost_desc = em.find(Host_Descriptor.class, new Host_Descriptor_PK(gatewayName, hostDescName));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Host_Descriptor hostDescriptor = new Host_Descriptor();
            Gateway existingGateway = em.find(Gateway.class, gatewayName);
            Users existingUser = em.find(Users.class, userName);
//            Gateway gateway = new Gateway();
//            gateway.setGateway_name(gatewayName);
//            Users user = new Users();
//            user.setUser_name(userName);
            hostDescriptor.setHost_descriptor_ID(getHostDescName());
            hostDescriptor.setGateway(existingGateway);
            byte[] contentBytes = content.getBytes();
            hostDescriptor.setHost_descriptor_xml(contentBytes);
            hostDescriptor.setUser(existingUser);
            if (existingHost_desc != null) {
                existingHost_desc.setUser(existingUser);
                existingHost_desc.setHost_descriptor_xml(contentBytes);
                hostDescriptor = em.merge(existingHost_desc);
            } else {
                em.merge(hostDescriptor);
            }

            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param type child resource type
     * @param name child resource name
     * @return boolean whether the child resource already exists
     */
    public boolean isExists(ResourceType type, Object name) {
        logger.error("Unsupported resource type for host descriptor resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param hostDescName host descriptor name
     */
    public void setHostDescName(String hostDescName) {
		this.hostDescName = hostDescName;
	}
}
