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
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkspaceProjectList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ProjectResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ProjectResourceClient.class);

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getProjectRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.ProjectResourcePathConstants.REGISTRY_API_PROJECTREGISTRY);
        return webResource;
    }

    public boolean isWorkspaceProjectExists(String projectName){
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
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

    public boolean isWorkspaceProjectExists(String projectName, boolean createIfNotExists){
        String createStatus = "false";
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST);
        if (createIfNotExists){
            createStatus = "true";
        }
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName );
        formParams.add("createIfNotExists", createStatus );

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }else {
            return true;
        }
    }

    public void addWorkspaceProject(String projectName){
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.ADD_PROJECT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName );

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkspaceProject(String projectName){
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.UPDATE_PROJECT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName );

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void  deleteWorkspaceProject(String projectName){
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.DELETE_PROJECT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkspaceProject workspaceProject = response.getEntity(WorkspaceProject.class);
        return workspaceProject;
    }

    public List<WorkspaceProject> getWorkspaceProjects(){
        webResource = getProjectRegistryBaseResource().path(ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECTS);
        ClientResponse response = webResource.get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkspaceProjectList workspaceProjectList = response.getEntity(WorkspaceProjectList.class);
        WorkspaceProject[] workspaceProjects = workspaceProjectList.getWorkspaceProjects();
        List<WorkspaceProject> workspaceProjectsList = new ArrayList<WorkspaceProject>();
        for (WorkspaceProject workspaceProject : workspaceProjects){
            workspaceProjectsList.add(workspaceProject);
        }
        return workspaceProjectsList;
    }

}
