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

package org.apache.airavata.rest.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.airavata.registry.api.PasswordCallback;
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.rest.mappings.resourcemappings.WorkspaceProjectList;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.utils.BasicAuthHeaderUtil;
import org.apache.airavata.rest.utils.ClientConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ProjectResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ProjectResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;

    public ProjectResourceClient(String userName, String serviceURI, PasswordCallback callback) {
        this.callback = callback;
        this.userName = userName;
        this.baseURI = serviceURI;
    }

    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getProjectRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.ProjectResourcePathConstants.REGISTRY_API_PROJECTREGISTRY);
        return webResource;
    }

    public boolean isWorkspaceProjectExists(String projectName) {
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = webResource.queryParams(queryParams).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = webResource.queryParams(queryParams).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            String exists = response.getEntity(String.class);
            if (exists.equals("True")){
                return true;
            } else {
                return false;
            }
        }
        else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public boolean isWorkspaceProjectExists(String projectName, boolean createIfNotExists) {
        String createStatus = "false";
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST);
        if (createIfNotExists) {
            createStatus = "true";
        }
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", projectName);
        formParams.add("createIfNotExists", createStatus);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            String exists = response.getEntity(String.class);
            if (exists.equals("True")){
                return true;
            } else {
                return false;
            }
        }
        else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addWorkspaceProject(WorkspaceProject project) {
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.ADD_PROJECT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", project.getProjectName());

        ClientResponse response = webResource.accept(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), cookie);
                response = builder.accept(
                        MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(
                        MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public void updateWorkspaceProject(WorkspaceProject project) {
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.UPDATE_PROJECT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("projectName", project.getProjectName());

        ClientResponse response = webResource.accept(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), cookie);
                response = builder.accept(
                        MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(
                        MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public void deleteWorkspaceProject(String projectName) {
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.DELETE_PROJECT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        ClientResponse response = webResource.queryParams(queryParams).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.delete(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.delete(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("projectName", projectName);
        ClientResponse response = webResource.queryParams(queryParams).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return null;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
        WorkspaceProject workspaceProject = response.getEntity(WorkspaceProject.class);
        return workspaceProject;
    }

    public List<WorkspaceProject> getWorkspaceProjects() {
        List<WorkspaceProject> workspaceProjectsList = new ArrayList<WorkspaceProject>();
        webResource = getProjectRegistryBaseResource().path(
                ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECTS);
        ClientResponse response = webResource.get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), cookie);
                response = builder.get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return workspaceProjectsList;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        WorkspaceProjectList workspaceProjectList = response.getEntity(WorkspaceProjectList.class);
        WorkspaceProject[] workspaceProjects = workspaceProjectList.getWorkspaceProjects();

        for (WorkspaceProject workspaceProject : workspaceProjects) {
            workspaceProjectsList.add(workspaceProject);
        }
        return workspaceProjectsList;
    }

}
