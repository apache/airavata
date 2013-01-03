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
import org.apache.airavata.registry.api.ResourceMetadata;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.rest.mappings.resourcemappings.Workflow;
import org.apache.airavata.rest.mappings.resourcemappings.WorkflowList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserWorkflowResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(UserWorkflowResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;
    private String gateway;

    public UserWorkflowResourceClient(String userName,
                                      String gateway,
                                      String serviceURI,
                                      PasswordCallback callback) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = serviceURI;
        this.gateway = gateway;
    }


    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private com.sun.jersey.api.client.WebResource getUserWFRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.UserWFConstants.REGISTRY_API_USERWFREGISTRY);
        return webResource;
    }

    public boolean isWorkflowExists(String workflowName) {
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.WORKFLOW_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowName", workflowName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_OK) {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
            String exists = response.getEntity(String.class);
            if (exists.equals("True")) {
                return true;
            } else {
                return false;
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowAlreadyExistsException {
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.ADD_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);
        formParams.add("workflowGraphXml", workflowGraphXml);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.type(
                MediaType.APPLICATION_FORM_URLENCODED).accept(
                MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.type(MediaType.APPLICATION_FORM_URLENCODED).accept(
                    MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_BAD_REQUEST){
                logger.debug("Workflow already exists...");
                throw new UserWorkflowAlreadyExistsException(workflowName + " already exists !!!");
            }
            else if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_BAD_REQUEST){
            logger.debug("Descriptor already exists...");
            throw new UserWorkflowAlreadyExistsException(workflowName + " already exists !!!");
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) {
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.UPDATE_WORKFLOW);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowName", workflowName);
        formParams.add("workflowGraphXml", workflowGraphXml);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(MediaType.TEXT_PLAIN).type(
                MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).type(
                    MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formParams);
            status = response.getStatus();
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public String getWorkflowGraphXML(String workflowName) {
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.GET_WORKFLOWGRAPH);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowName", workflowName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, callback.getPassword(userName), cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(
                    MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return null;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else if (status == ClientConstant.HTTP_NO_CONTENT) {
            return null;
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String worlflowGraph = response.getEntity(String.class);
        return worlflowGraph;
    }

    public Map<String, String> getWorkflows() {
        Map<String, String> userWFMap = new HashMap<String, String>();
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.GET_WORKFLOWS);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, null, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, null, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            status = response.getStatus();
            if (status == ClientConstant.HTTP_NO_CONTENT) {
                return userWFMap;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else if (status == ClientConstant.HTTP_NO_CONTENT) {
                return userWFMap;
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowList workflowList = response.getEntity(WorkflowList.class);
        List<Workflow> workflows = workflowList.getWorkflowList();

        for (Workflow workflow : workflows) {
            userWFMap.put(workflow.getWorkflowName(), workflow.getWorkflowGraph());
        }

        return userWFMap;
    }

    public void removeWorkflow(String workflowName) {
        webResource = getUserWFRegistryBaseResource().path(
                ResourcePathConstants.UserWFConstants.REMOVE_WORKFLOW);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowName", workflowName);
        builder = BasicAuthHeaderUtil.getBuilder(
                webResource, queryParams, userName, null, cookie, gateway);

        ClientResponse response = builder.accept(
                MediaType.TEXT_PLAIN).delete(ClientResponse.class);
        int status = response.getStatus();

        if (status == ClientConstant.HTTP_OK) {
            if (response.getCookies().size() > 0) {
                cookie = response.getCookies().get(0).toCookie();
            }
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            builder = BasicAuthHeaderUtil.getBuilder(
                    webResource, queryParams, userName, callback.getPassword(userName), null, gateway);
            response = builder.accept(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
            status = response.getStatus();

            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            } else {
                if (response.getCookies().size() > 0) {
                    cookie = response.getCookies().get(0).toCookie();
                }
            }
        } else {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) {
        //not implemented in the registry API
        return null;
    }

}
