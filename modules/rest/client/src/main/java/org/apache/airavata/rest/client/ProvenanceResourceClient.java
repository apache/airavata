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
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.impl.WorkflowExecutionDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.rest.mappings.resourcemappings.ExperimentDataList;
import org.apache.airavata.rest.mappings.resourcemappings.ExperimentIDList;
import org.apache.airavata.rest.mappings.resourcemappings.WorkflowInstancesList;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.utils.BasicAuthHeaderUtil;
import org.apache.airavata.rest.utils.ClientConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.*;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProvenanceResourceClient {
    private WebResource webResource;
    private final static Logger logger = LoggerFactory.getLogger(ProvenanceResourceClient.class);
    private String userName;
    private PasswordCallback callback;
    private String baseURI;
    private Cookie cookie;
    private WebResource.Builder builder;

    public ProvenanceResourceClient(String userName,
                                    String serviceURL,
                                    PasswordCallback callback) {
        this.userName = userName;
        this.callback = callback;
        this.baseURI = serviceURL;
    }

    private URI getBaseURI() {
        logger.debug("Creating Base URI");
        return UriBuilder.fromUri(baseURI).build();
    }

    private WebResource getProvenanceRegistryBaseResource() {
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(
                ResourcePathConstants.ProvenanceResourcePathConstants.REGISTRY_API_PROVENANCEREGISTRY);
        return webResource;
    }

    public void updateExperimentExecutionUser(String experimentId, String user) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENT_EXECUTIONUSER);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("user", user);
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

    public String getExperimentExecutionUser(String experimentId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_EXECUTIONUSER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

        String executionUser = response.getEntity(String.class);
        return executionUser;
    }

    public boolean isExperimentNameExist(String experimentName) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.EXPERIMENTNAME_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentName", experimentName);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

    public String getExperimentName(String experimentId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_NAME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

        String experimentName = response.getEntity(String.class);
        return experimentName;
    }

    public void updateExperimentName(String experimentId, String experimentName) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTNAME);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("experimentName", experimentName);

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
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

    }

    public String getExperimentMetadata(String experimentId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTMETADATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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
        String experimentMetadata = response.getEntity(String.class);
        return experimentMetadata;
    }

    public void updateExperimentMetadata(String experimentId, String metadata) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTMETADATA);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("metadata", metadata);

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
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public String getWorkflowExecutionTemplateName(String workflowInstanceId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWTEMPLATENAME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

        String workflowTemplateName = response.getEntity(String.class);
        return workflowTemplateName;
    }

    public void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCETEMPLATENAME);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("templateName", templateName);

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
                response = builder.post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.post(ClientResponse.class, formParams);
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

    public List<WorkflowExecution> getExperimentWorkflowInstances(String experimentId) {
        List<WorkflowExecution> workflowInstanceList = new ArrayList<WorkflowExecution>();
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTWORKFLOWINSTANCES);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return workflowInstanceList;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        WorkflowInstancesList workflowInstancesList = response.getEntity(WorkflowInstancesList.class);
        WorkflowExecution[] workflowInstances = workflowInstancesList.getWorkflowInstances();


        for (WorkflowExecution workflowInstance : workflowInstances) {
            workflowInstanceList.add(workflowInstance);
        }

        return workflowInstanceList;
    }

    public boolean isWorkflowInstanceExists(String instanceId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_EXIST_CHECK);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("instanceId", instanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);

        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

    public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_EXIST_CREATE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("instanceId", instanceId);
        formParams.add("createIfNotPresent", String.valueOf(createIfNotPresent));

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

    public void updateWorkflowInstanceStatus(String instanceId, WorkflowExecutionStatus.State executionStatus) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS_INSTANCEID);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("instanceId", instanceId);
        formParams.add("executionStatus", executionStatus.name());

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
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
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

    public void updateWorkflowInstanceStatus(WorkflowExecutionStatus workflowInstanceStatus) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String statusUpdateDate = dateFormat.format(workflowInstanceStatus.getStatusUpdateTime());
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId",
                workflowInstanceStatus.getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("executionStatus",
                workflowInstanceStatus.getExecutionStatus().name());
        formParams.add("statusUpdateTime",
                statusUpdateDate);

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
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
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

    public WorkflowExecutionStatus getWorkflowInstanceStatus(String instanceId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCESTATUS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("instanceId", instanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        WorkflowExecutionStatus workflowInstanceStatus = response.getEntity(WorkflowExecutionStatus.class);
        return workflowInstanceStatus;
    }

    public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEINPUT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("nodeID", node.getNodeId());
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("data", data);

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

    public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEOUTPUT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("nodeID", node.getNodeId());
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("data", data);

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
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
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

    public ExperimentData getExperiment(String experimentId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
        ExperimentDataImpl experimentData = response.getEntity(ExperimentDataImpl.class);
        return experimentData;
    }

    public ExperimentData getExperimentMetaInformation(String experimentId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_METAINFORMATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        ExperimentDataImpl experimentData = response.getEntity(ExperimentDataImpl.class);
        return experimentData;
    }

    public List<ExperimentData> getAllExperimentMetaInformation(String user) {
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_ALL_EXPERIMENT_METAINFORMATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("user", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return experimentDatas;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();

        for (ExperimentDataImpl experimentData : dataList) {
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public List<ExperimentData> searchExperiments(String user, String experimentNameRegex) {
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.SEARCH_EXPERIMENTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("user", user);
        queryParams.add("experimentNameRegex", experimentNameRegex);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return experimentDatas;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();

        for (ExperimentDataImpl experimentData : dataList) {
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public List<String> getExperimentIdByUser(String user) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_ID_USER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("username", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return new ArrayList<String>();
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }

        ExperimentIDList experimentIDList = response.getEntity(ExperimentIDList.class);
        List<String> experimentIDs = experimentIDList.getExperimentIDList();
        return experimentIDs;
    }

    public List<ExperimentData> getExperimentByUser(String user) {
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_USER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("username", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
                cookie = response.getCookies().get(0).toCookie();
            }
            status = response.getStatus();
            if(status == ClientConstant.HTTP_NO_CONTENT){
                return experimentDatas;
            }
            if (status != ClientConstant.HTTP_OK) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();

        for (ExperimentDataImpl experimentData : dataList) {
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public void updateWorkflowNodeStatus(WorkflowInstanceNodeStatus workflowStatusNode) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId",
                workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("nodeId",
                workflowStatusNode.getWorkflowInstanceNode().getNodeId());
        formParams.add("executionStatus",
                workflowStatusNode.getExecutionStatus().name());

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

    public void updateWorkflowNodeStatus(String workflowInstanceId,
                                         String nodeId,
                                         WorkflowExecutionStatus.State executionStatus) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("nodeId", nodeId);
        formParams.add("executionStatus", executionStatus.name());

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

    public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode,
                                         WorkflowExecutionStatus.State executionStatus) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId",
                workflowNode.getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("nodeId", workflowNode.getNodeId());
        formParams.add("executionStatus", executionStatus.name());

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

    public WorkflowInstanceNodeStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STATUS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowNode.getWorkflowInstance().getWorkflowExecutionId());
        queryParams.add("nodeId", workflowNode.getNodeId());
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        WorkflowInstanceNodeStatus workflowInstanceNodeStatus =
                response.getEntity(WorkflowInstanceNodeStatus.class);
        return workflowInstanceNodeStatus;
    }

    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STARTTIME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowNode.getWorkflowInstance().getWorkflowExecutionId());
        queryParams.add("nodeId", workflowNode.getNodeId());
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

        String wfNodeStartTime = response.getEntity(String.class);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date formattedDate = dateFormat.parse(wfNodeStartTime);
            return formattedDate;
        } catch (ParseException e) {
            logger.error("Error in date format...", e);
            return null;
        }
    }

    public Date getWorkflowStartTime(WorkflowExecution workflowInstance) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOW_STARTTIME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstance.getWorkflowExecutionId());
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

        String wfStartTime = response.getEntity(String.class);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date formattedDate = dateFormat.parse(wfStartTime);
            return formattedDate;
        } catch (ParseException e) {
            logger.error("Error in date format...", e);
            return null;
        }
    }

    public void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_GRAMDATA);
        ClientResponse response = webResource.accept(
                MediaType.TEXT_PLAIN).type(
                MediaType.APPLICATION_JSON).post(ClientResponse.class, workflowNodeGramData);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).type(
                        MediaType.APPLICATION_JSON).post(ClientResponse.class, workflowNodeGramData);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, null, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).type(
                        MediaType.APPLICATION_JSON).post(ClientResponse.class, workflowNodeGramData);
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

    public WorkflowExecutionData getWorkflowInstanceData(String workflowInstanceId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCEDATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        WorkflowExecutionDataImpl workflowInstanceData = response.getEntity(WorkflowExecutionDataImpl.class);
        return workflowInstanceData;
    }

    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        queryParams.add("nodeId", nodeId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
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

    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId,
                                                 String nodeId,
                                                 boolean createIfNotPresent) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST_CREATE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("nodeId", nodeId);
        formParams.add("createIfNotPresent", String.valueOf(createIfNotPresent));
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
                        webResource, null, userName, callback.getPassword(userName),cookie);
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

    public NodeExecutionData getWorkflowInstanceNodeData(String workflowInstanceId,
                                                                String nodeId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_DATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        queryParams.add("nodeId", nodeId);
        ClientResponse response = webResource.queryParams(queryParams).accept(
                MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
            logger.error(response.getEntity(String.class));
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        } else if (status == ClientConstant.HTTP_UNAUTHORIZED) {
            if (cookie != null){
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), cookie);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            } else {
                builder = BasicAuthHeaderUtil.getBuilder(
                        webResource, queryParams, userName, callback.getPassword(userName), null);
                response = builder.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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

        NodeExecutionData workflowInstanceNodeData =
                response.getEntity(NodeExecutionData.class);
        return workflowInstanceNodeData;
    }

    public void addWorkflowInstance(String experimentId,
                                    String workflowInstanceId,
                                    String templateName) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("templateName", templateName);

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
            if (status != ClientConstant.HTTP_OK && status != ClientConstant.HTTP_UNAUTHORIZED) {
                logger.error(response.getEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + status);
            }
        }
    }

    public void updateWorkflowNodeType(WorkflowInstanceNode node,
                                       WorkflowNodeType type) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODETYPE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowExecutionId());
        formParams.add("nodeId", node.getNodeId());
        formParams.add("nodeType", type.getNodeType().name());

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

    public void addWorkflowInstanceNode(String workflowInstance,
                                        String nodeId) {
        webResource = getProvenanceRegistryBaseResource().path(
                ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCENODE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstance);
        formParams.add("nodeId", nodeId);

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
                response = builder.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
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

    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeInput(String experimentIdRegEx,
                                                                    String workflowNameRegEx,
                                                                    String nodeNameRegEx) {
        //not implemented in Registry API
        return null;
    }

    public List<WorkflowNodeIOData> searchWorkflowInstanceNodeOutput(String experimentIdRegEx,
                                                                     String workflowNameRegEx,
                                                                     String nodeNameRegEx) {
        //not implemented in Registry API
        return null;
    }

    public List<ExperimentData> getExperimentByUser(String user,
                                                    int pageSize,
                                                    int pageNo) {
        //not implemented in Registry API
        return null;
    }

    public List<WorkflowNodeIOData> getWorkflowInstanceNodeInput(String workflowInstanceId,
                                                                 String nodeType) {
        //not implemented in Registry API
        return null;
    }

    public List<WorkflowNodeIOData> getWorkflowInstanceNodeOutput(String workflowInstanceId,
                                                                  String nodeType) {
        //not implemented in Registry API
        return null;
    }

    public void saveWorkflowExecutionOutput(String experimentId,
                                            String outputNodeName,
                                            String output) {
        //not implemented in Registry API
    }

    public void saveWorkflowExecutionOutput(String experimentId,
                                            WorkflowIOData data) {
        //not implemented in Registry API
    }

    public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
                                                     String outputNodeName) {
        //not implemented in Registry API
        return null;
    }

    public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) {
        //not implemented in Registry API
        return null;
    }

    public String[] getWorkflowExecutionOutputNames(String exeperimentId) {
        //not implemented in Registry API
        return null;
    }


}
