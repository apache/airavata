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
import org.apache.airavata.services.registry.rest.resourcemappings.PublishWorkflowNamesList;
import org.apache.airavata.services.registry.rest.resourcemappings.Workflow;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkflowList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishedWorkflowResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(PublishedWorkflowResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getPublishedWFRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.PublishedWFConstants.REGISTRY_API_PUBLISHWFREGISTRY);
        return webResource;
    }

    public boolean isPublishedWorkflowExists(String workflowName){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.PUBLISHWF_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowname", workflowName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        return true;
    }

    public void publishWorkflow(String workflowName, String publishWorkflowName){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.PUBLISH_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);
        formParams.add("publishWorkflowName", publishWorkflowName);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void publishWorkflow(String workflowName){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.PUBLISH_DEFAULT_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

    }

    public String getPublishedWorkflowGraphXML(String workflowName){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWGRAPH);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowname", workflowName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String wfGraph = response.getEntity(String.class);
        return wfGraph;

    }

    public List<String> getPublishedWorkflowNames(){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWNAMES);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        PublishWorkflowNamesList workflowNamesList = response.getEntity(PublishWorkflowNamesList.class);
        List<String> publishWorkflowNames = workflowNamesList.getPublishWorkflowNames();
        return publishWorkflowNames;
    }

    public Map<String, String> getPublishedWorkflows(){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        Map<String, String> publishWFmap = new HashMap<String, String>();
        WorkflowList workflowList = response.getEntity(WorkflowList.class);
        List<Workflow> workflows = workflowList.getWorkflowList();

        for (Workflow workflow : workflows){
            publishWFmap.put(workflow.getWorkflowName(), workflow.getWorkflowGraph());
        }

        return publishWFmap;
    }

    public void removePublishedWorkflow(String workflowName){
        webResource = getPublishedWFRegistryBaseResource().path(ResourcePathConstants.PublishedWFConstants.REMOVE_PUBLISHWORKFLOW);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowname", workflowName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

    }
}
