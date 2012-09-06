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
package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Configuration;
import org.apache.airavata.persistance.registry.jpa.model.Host_Descriptor;
import org.apache.airavata.persistance.registry.jpa.resources.ApplicationDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.HostDescriptorResource;
import org.apache.airavata.persistance.registry.jpa.resources.ServiceDescriptorResource;
import org.apache.airavata.registry.api.*;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.net.URI;
import java.util.*;

public class AiravataJPARegistry extends AiravataRegistry2{
    private final static Logger logger = LoggerFactory.getLogger(AiravataJPARegistry.class);

    private static final String PERSISTENCE_UNIT_NAME = "airavata_registry";
	private EntityManagerFactory factory;

    @Override
    protected void initialize() {

    }

    public Object getConfiguration(String key) {
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key");
        q.setParameter("config_key", key);
        return q.getSingleResult();
    }
    // Not sure about this.. need some description
    public List<Object> getConfigurationList(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setConfiguration(String key, String value, Date expire) {
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Configuration configuraton = new Configuration();
        configuraton.setConfig_key(key);
        configuraton.setConfig_val(value);
        configuraton.setExpire_date((java.sql.Date) expire);
        em.persist(configuraton);
        em.getTransaction().commit();
		em.close();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addConfiguration(String key, String value, Date expire) {
        EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
        Configuration configuraton = new Configuration();
        configuraton.setConfig_key(key);
        configuraton.setConfig_val(value);
        configuraton.setExpire_date((java.sql.Date) expire);
        em.persist(configuraton);
        em.getTransaction().commit();
		em.close();
    }

    public void removeAllConfiguration(String key) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key");
        q.setParameter("config_key", key);
        List<Configuration> resultList = q.getResultList();
        for (Configuration config : resultList) {
            em.remove(config);
        }
        em.getTransaction();
        em.close();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeConfiguration(String key, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("SELECT p FROM Configuration p WHERE p.config_key = :config_key AND p.config_value = :config_value");
        q.setParameter("config_key", key);
        q.setParameter("config_value", value);
        Configuration config = (Configuration)q.getSingleResult();
        em.remove(config);
        em.getTransaction();
        em.close();
    }

    public List<URI> getGFacURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<URI> getWorkflowInterpreterURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getEventingServiceURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getMessageBoxURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetEventingURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetMessageBoxURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    // DescriptorRegistry Implementation
    public void addHostDescriptor(HostDescription descriptor) {
        //todo how to fill other data
        GatewayResource gatewayResource = new GatewayResource();
        HostDescriptorResource resource = (HostDescriptorResource)gatewayResource.create(ResourceType.HOST_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
    }

    public void updateHostDescriptor(HostDescription descriptor) {
        addHostDescriptor(descriptor);
    }

    public HostDescription getHostDescriptor(String hostName) {
        GatewayResource gatewayResource = new GatewayResource();
        Resource resource = gatewayResource.get(ResourceType.HOST_DESCRIPTOR, hostName);
        try {
            return HostDescription.fromXML(((HostDescriptorResource)resource).getContent());
        } catch (XmlException e) {
            logger.error("Error parsing Host Descriptor");
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeHostDescriptor(String hostName) {
       GatewayResource gatewayResource = new GatewayResource();
       gatewayResource.remove(ResourceType.HOST_DESCRIPTOR, hostName);
    }

    public ResourceMetadata getHostDescriptorMetadata(String hostName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addServiceDescriptor(ServiceDescription descriptor) {
         //todo how to fill other data
        GatewayResource gatewayResource = new GatewayResource();
        ServiceDescriptorResource resource = (ServiceDescriptorResource)gatewayResource.create(ResourceType.SERVICE_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) {
        addServiceDescriptor(descriptor);
    }

    public ServiceDescription getServiceDescriptor(String serviceName) {
        GatewayResource gatewayResource = new GatewayResource();
        Resource resource = gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR, serviceName);
        try {
            return ServiceDescription.fromXML(((ServiceDescriptorResource) resource).getContent());
        } catch (XmlException e) {
            logger.error("Error parsing Host Descriptor");
        }
        return null;
    }

    public void removeServiceDescriptor(String serviceName) {
       GatewayResource gatewayResource = new GatewayResource();
       gatewayResource.remove(ResourceType.SERVICE_DESCRIPTOR, serviceName);
    }

    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceDescription.getType().getName(),hostDescriptor.getType().getHostName(),descriptor);
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        GatewayResource gatewayResource = new GatewayResource();
        ApplicationDescriptorResource resource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        resource.setContent(descriptor.toXML());
        resource.setHostDescName(hostName);
        resource.setServiceDescName(serviceName);
        //todo fix the IDs to Names
//        resource.setGatewayID(getGateway().getGatewayName());
//        resource.setUserID(getUser().getUserName());
        resource.save();
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceDescription,hostDescriptor,descriptor);
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        addApplicationDescriptor(serviceName,hostName,descriptor);
    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) {
        //todo finish implementation
        GatewayResource gatewayResource = new GatewayResource();
        ApplicationDescriptorResource resource = (ApplicationDescriptorResource)gatewayResource.create(ResourceType.APPLICATION_DESCRIPTOR);
        resource.setHostDescName(hostname);
        resource.setServiceDescName(serviceName);
//        resource.get()
//        gatewayResource.
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) {
        GatewayResource gatewayResource = new GatewayResource();
        ServiceDescriptorResource resource = (ServiceDescriptorResource)gatewayResource.get(ResourceType.SERVICE_DESCRIPTOR,serviceName);
        resource.setServiceDescName(serviceName);
        List<Resource> resources = resource.get(ResourceType.APPLICATION_DESCRIPTOR);
        HashMap<String, ApplicationDeploymentDescription> stringApplicationDescriptorResourceHashMap =
                new HashMap<String, ApplicationDeploymentDescription>();
        for(Resource applicationDescriptorResource:resources){
            try {
                stringApplicationDescriptorResourceHashMap.put(resource.getServiceDescName(),
                        ApplicationDeploymentDescription.fromXML(((ApplicationDescriptorResource)applicationDescriptorResource).getContent()));
            } catch (XmlException e) {
                logger.error("Error parsing Application Descriptor");
            }
        }
        return stringApplicationDescriptorResourceHashMap;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) {

    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



    public void addWorkspaceProject(WorkspaceProject project) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateWorkspaceProject(WorkspaceProject project) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteWorkspaceProject(String projectName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createExperiment(String projectName, AiravataExperiment experiment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeExperiment(String experimentId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(String projectName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void publishWorkflow(String workflowName, String publishWorkflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void publishWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPublishedWorkflowGraphXML(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removePublishedWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflow(String workflowName, String workflowGraphXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getWorkflowGraphXML(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAiravataRegistry(AiravataRegistry2 registry) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAiravataUser(AiravataUser user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
