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

public class UserWorkflowResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(UserWorkflowResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private com.sun.jersey.api.client.WebResource getUserWFRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.UserWFConstants.REGISTRY_API_USERWFREGISTRY);
        return webResource;
    }

    public boolean isWorkflowExists(String workflowName){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.WORKFLOW_EXIST);
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

    public void addWorkflow(String workflowName, String workflowGraphXml){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.ADD_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);
        formParams.add("workflowGraphXml", workflowGraphXml);

        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.UPDATE_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);
        formParams.add("workflowGraphXml", workflowGraphXml);

        ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public String getWorkflowGraphXML(String workflowName){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.GET_WORKFLOWGRAPH);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowname", workflowName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String worlflowGraph = response.getEntity(String.class);
        return worlflowGraph;
    }

    public Map<String, String> getWorkflows(){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.GET_WORKFLOWS);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        Map<String, String> userWFMap = new HashMap<String, String>();
        WorkflowList workflowList = response.getEntity(WorkflowList.class);
        List<Workflow> workflows = workflowList.getWorkflowList();

        for (Workflow workflow : workflows){
            userWFMap.put(workflow.getWorkflowName(), workflow.getWorkflowGraph());
        }

        return userWFMap;
    }

    public void removeWorkflow(String workflowName){
        webResource = getUserWFRegistryBaseResource().path(ResourcePathConstants.UserWFConstants.REMOVE_WORKFLOW);
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
