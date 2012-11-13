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

package org.apache.airavata.services.registry.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.services.registry.rest.resourcemappings.*;
import org.apache.airavata.services.registry.rest.utils.DescriptorUtil;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescriptorResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(DescriptorResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getDescriptorRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.DecResourcePathConstants.DESC_RESOURCE_PATH);
        return webResource;
    }

    public boolean isHostDescriptorExists(String hostDescriptorName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostDescriptorName", hostDescriptorName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else {
            return true;
        }
    }

    public void addHostDescriptor (HostDescription hostDescription){
        HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescription);
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_SAVE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateHostDescriptor (HostDescription hostDescription){
        HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescription);
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_UPDATE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, hostDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public HostDescription getHostDescriptor (String hostName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostName", hostName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        HostDescriptor hostDescriptor = response.getEntity(HostDescriptor.class);
        HostDescription hostDescription = DescriptorUtil.createHostDescription(hostDescriptor);
        return hostDescription;
    }

    public void removeHostDescriptor(String hostName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_DELETE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("hostName", hostName);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<HostDescription> getHostDescriptors() {
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        HostDescriptionList hostDescriptionList = response.getEntity(HostDescriptionList.class);
        HostDescriptor[] hostDescriptors = hostDescriptionList.getHostDescriptions();
        List<HostDescription> hostDescriptions = new ArrayList<HostDescription>();
        for (HostDescriptor hostDescriptor : hostDescriptors){
           HostDescription hostDescription = DescriptorUtil.createHostDescription(hostDescriptor);
            hostDescriptions.add(hostDescription);
        }

        return hostDescriptions;
    }

    public List<String> getHostDescriptorNames(){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS_NAMES);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        DescriptorNameList descriptorNameList = response.getEntity(DescriptorNameList.class);
        return descriptorNameList.getDescriptorNames();
    }

    public boolean isServiceDescriptorExists(String serviceDescriptorName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceDescriptorName", serviceDescriptorName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else {
            return true;
        }
    }

    public void saveServiceDescriptor (ServiceDescription serviceDescription){
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_SAVE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateServiceDescriptor(ServiceDescription serviceDescription){
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_UPDATE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, serviceDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ServiceDescription getServiceDescriptor (String serviceName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ServiceDescriptor serviceDescriptor = response.getEntity(ServiceDescriptor.class);
        ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
        return serviceDescription;
    }

    public void removeServiceDescriptor(String serviceName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_DELETE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<ServiceDescription> getServiceDescriptors (){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.GET_SERVICE_DESCS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ServiceDescriptionList serviceDescriptionList = response.getEntity(ServiceDescriptionList.class);
        ServiceDescriptor[] serviceDescriptors = serviceDescriptionList.getServiceDescriptions();
        List<ServiceDescription> serviceDescriptions = new ArrayList<ServiceDescription>();
        for (ServiceDescriptor serviceDescriptor : serviceDescriptors){
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
            serviceDescriptions.add(serviceDescription);
        }
        return serviceDescriptions;
    }

    public boolean isApplicationDescriptorExist (String serviceName, String hostName, String appDescriptorName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APPL_DESC_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostName);
        queryParams.add("appDescName", appDescriptorName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else {
            return true;
        }
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription,
                                         HostDescription hostDescriptor,
                                         ApplicationDeploymentDescription descriptor){
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostDescriptor.getType().getHostName());
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_BUILD_SAVE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor){
        ServiceDescription serviceDescription = getServiceDescriptor(serviceName);
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostName);
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_BUILD_SAVE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription,
                                            HostDescription hostDescriptor,
                                            ApplicationDeploymentDescription descriptor){
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostDescriptor.getType().getHostName());
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_UPDATE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor){
        ServiceDescription serviceDescription = getServiceDescriptor(serviceName);
        ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(descriptor);
        applicationDescriptor.setHostdescName(hostName);
        ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
        applicationDescriptor.setServiceDescriptor(serviceDescriptor);

        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_UPDATE);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, applicationDescriptor);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ApplicationDeploymentDescription getApplicationDescriptor(String serviceName,
                                                                     String hostname,
                                                                     String applicationName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_DESCRIPTION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostname);
        queryParams.add("applicationName", applicationName);

        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptor applicationDescriptor = response.getEntity(ApplicationDescriptor.class);
        ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
        return applicationDeploymentDescription;
    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_PER_HOST_SERVICE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        queryParams.add("hostName", hostname);

        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptor applicationDescriptor = response.getEntity(ApplicationDescriptor.class);
        ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
        return applicationDeploymentDescription;
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCS_SERVICE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("serviceName", serviceName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptorList applicationDescriptorList = response.getEntity(ApplicationDescriptorList.class);
        ApplicationDescriptor[] applicationDescriptors = applicationDescriptorList.getApplicationDescriptors();
        Map<String, ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = new HashMap<String, ApplicationDeploymentDescription>();
        for (ApplicationDescriptor applicationDescriptor : applicationDescriptors){
            ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
            applicationDeploymentDescriptionMap.put(applicationDescriptor.getHostdescName(), applicationDeploymentDescription);
        }
        return applicationDeploymentDescriptionMap;
    }

    public Map<String[], ApplicationDeploymentDescription> getApplicationDescriptors(){
        Map<String[], ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = new HashMap<String[], ApplicationDeploymentDescription>();
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCRIPTORS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ApplicationDescriptorList applicationDescriptorList = response.getEntity(ApplicationDescriptorList.class);
        ApplicationDescriptor[] applicationDescriptors = applicationDescriptorList.getApplicationDescriptors();
        for (ApplicationDescriptor applicationDescriptor : applicationDescriptors){
            ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
            String[] descriptors = {applicationDescriptor.getServiceDescriptor().getServiceName(), applicationDescriptor.getHostdescName()};
            applicationDeploymentDescriptionMap.put(descriptors, applicationDeploymentDescription);
        }
        return applicationDeploymentDescriptionMap;
    }

    private List<String> getApplicationDescriptorNames (){
        webResource = getDescriptorRegistryBaseResource().path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_NAMES);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        DescriptorNameList descriptorNameList = response.getEntity(DescriptorNameList.class);
        return descriptorNameList.getDescriptorNames();
    }
}
