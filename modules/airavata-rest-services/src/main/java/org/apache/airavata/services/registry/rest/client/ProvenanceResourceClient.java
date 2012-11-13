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
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentDataList;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentIDList;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkflowInstancesList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
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

    private URI getBaseURI() {
        logger.info("Creating Base URI");
        return UriBuilder.fromUri("http://localhost:9080/airavata-services/").build();
    }

    private WebResource getProvenanceRegistryBaseResource (){
        ClientConfig config = new DefaultClientConfig();
        config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,
                Boolean.TRUE);
        Client client = Client.create(config);
        WebResource baseWebResource = client.resource(getBaseURI());
        webResource = baseWebResource.path(ResourcePathConstants.ProvenanceResourcePathConstants.REGISTRY_API_PROVENANCEREGISTRY);
        return webResource;
    }

    public void updateExperimentExecutionUser(String experimentId, String user){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENT_EXECUTIONUSER);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("user", user);
        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public String getExperimentExecutionUser(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_EXECUTIONUSER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();

        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String executionUser = response.getEntity(String.class);
        return executionUser;
    }

    public boolean isExperimentNameExist(String experimentName){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.EXPERIMENTNAME_EXISTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentName", experimentName);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        return true;
    }

    public String getExperimentName(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_NAME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String experimentName = response.getEntity(String.class);
        return experimentName;
    }

    public void updateExperimentName(String experimentId, String experimentName){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTNAME);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("experimentName", experimentName);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

    }

    public String getExperimentMetadata(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTMETADATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String experimentMetadata = response.getEntity(String.class);
        return experimentMetadata;
    }

    public void updateExperimentMetadata(String experimentId, String metadata){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTMETADATA);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("metadata", metadata);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public String getWorkflowExecutionTemplateName(String workflowInstanceId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWTEMPLATENAME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        String workflowTemplateName = response.getEntity(String.class);
        return workflowTemplateName;
    }

    public void setWorkflowInstanceTemplateName(String workflowInstanceId, String templateName){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCETEMPLATENAME);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("templateName", templateName);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public List<WorkflowInstance> getExperimentWorkflowInstances(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTWORKFLOWINSTANCES);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowInstancesList workflowInstancesList = response.getEntity(WorkflowInstancesList.class);
        WorkflowInstance[] workflowInstances = workflowInstancesList.getWorkflowInstances();
        List<WorkflowInstance> workflowInstanceList = new ArrayList<WorkflowInstance>();

        for (WorkflowInstance workflowInstance : workflowInstances){
            workflowInstanceList.add(workflowInstance);
        }

        return workflowInstanceList;
    }

    public boolean isWorkflowInstanceExists(String instanceId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_EXIST_CHECK);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("instanceId", instanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        return true;
    }

    public boolean isWorkflowInstanceExists(String instanceId, boolean createIfNotPresent){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST_CREATE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("instanceId", instanceId);
        formParams.add("createIfNotPresent", createIfNotPresent);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        return true;
    }

    public void updateWorkflowInstanceStatus(String instanceId, WorkflowInstanceStatus.ExecutionStatus executionStatus){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS_INSTANCEID);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("instanceId", instanceId);
        formParams.add("executionStatus", executionStatus.name());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflowInstanceStatus(WorkflowInstanceStatus workflowInstanceStatus){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceStatus.getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("executionStatus", workflowInstanceStatus.getExecutionStatus().name());
        formParams.add("statusUpdateTime", workflowInstanceStatus.getStatusUpdateTime().toString());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public WorkflowInstanceStatus getWorkflowInstanceStatus(String instanceId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCESTATUS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("instanceId", instanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowInstanceStatus workflowInstanceStatus = response.getEntity(WorkflowInstanceStatus.class);
        return workflowInstanceStatus;
    }

    public void updateWorkflowNodeInput(WorkflowInstanceNode node, String data){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEINPUT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("nodeID", node.getNodeId());
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("data", data);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflowNodeOutput(WorkflowInstanceNode node, String data){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEOUTPUT);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("nodeID", node.getNodeId());
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("data", data);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public ExperimentData getExperiment(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentDataImpl experimentData = response.getEntity(ExperimentDataImpl.class);
        return experimentData;
    }

    public ExperimentData getExperimentMetaInformation(String experimentId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_METAINFORMATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("experimentId", experimentId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentDataImpl experimentData = response.getEntity(ExperimentDataImpl.class);
        return experimentData;
    }

    public List<ExperimentData> getAllExperimentMetaInformation(String user){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_ALL_EXPERIMENT_METAINFORMATION);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("user", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        for (ExperimentDataImpl experimentData : dataList){
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public List<ExperimentData> searchExperiments(String user, String experimentNameRegex){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.SEARCH_EXPERIMENTS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("user", user);
        queryParams.add("experimentNameRegex", experimentNameRegex);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        for (ExperimentDataImpl experimentData : dataList){
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public List<String> getExperimentIdByUser(String user){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_ID_USER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("username", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        ExperimentIDList experimentIDList = response.getEntity(ExperimentIDList.class);
        List<String> experimentIDs = experimentIDList.getExperimentIDList();
        return experimentIDs;
    }

    public List<ExperimentData> getExperimentByUser(String user){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_USER);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("username", user);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        ExperimentDataList experimentDataList = response.getEntity(ExperimentDataList.class);
        List<ExperimentDataImpl> dataList = experimentDataList.getExperimentDataList();
        List<ExperimentData> experimentDatas = new ArrayList<ExperimentData>();
        for (ExperimentDataImpl experimentData : dataList){
            experimentDatas.add(experimentData);
        }
        return experimentDatas;
    }

    public void updateWorkflowNodeStatus(WorkflowInstanceNodeStatus workflowStatusNode){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowStatusNode.getWorkflowInstanceNode().getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("nodeId", workflowStatusNode.getWorkflowInstanceNode().getNodeId());
        formParams.add("executionStatus", workflowStatusNode.getExecutionStatus().name());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflowNodeStatus(String workflowInstanceId, String nodeId, WorkflowInstanceStatus.ExecutionStatus executionStatus){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("nodeId", nodeId);
        formParams.add("executionStatus", executionStatus.name());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflowNodeStatus(WorkflowInstanceNode workflowNode, WorkflowInstanceStatus.ExecutionStatus executionStatus){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowNode.getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("nodeId", workflowNode.getNodeId());
        formParams.add("executionStatus", executionStatus.name());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public WorkflowInstanceNodeStatus getWorkflowNodeStatus(WorkflowInstanceNode workflowNode){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STATUS);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowNode.getWorkflowInstance().getWorkflowInstanceId());
        queryParams.add("nodeId", workflowNode.getNodeId());
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowInstanceNodeStatus workflowInstanceNodeStatus = response.getEntity(WorkflowInstanceNodeStatus.class);
        return workflowInstanceNodeStatus;
    }

    public Date getWorkflowNodeStartTime(WorkflowInstanceNode workflowNode){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STARTTIME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowNode.getWorkflowInstance().getWorkflowInstanceId());
        queryParams.add("nodeId", workflowNode.getNodeId());
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
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

    public Date getWorkflowStartTime(WorkflowInstance workflowInstance){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOW_STARTTIME);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstance.getWorkflowInstanceId());
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
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

    public void updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_GRAMDATA);
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, workflowNodeGramData);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public WorkflowInstanceData getWorkflowInstanceData(String workflowInstanceId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCEDATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowInstanceData workflowInstanceData = response.getEntity(WorkflowInstanceData.class);
        return workflowInstanceData;
    }

    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        queryParams.add("nodeId", nodeId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        return true;
    }

    public boolean isWorkflowInstanceNodePresent(String workflowInstanceId, String nodeId, boolean createIfNotPresent){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST_CREATE);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        queryParams.add("nodeId", nodeId);
        queryParams.add("createIfNotPresent", createIfNotPresent);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
        return true;
    }

    public WorkflowInstanceNodeData getWorkflowInstanceNodeData(String workflowInstanceId, String nodeId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_DATA);
        MultivaluedMap queryParams = new MultivaluedMapImpl();
        queryParams.add("workflowInstanceId", workflowInstanceId);
        queryParams.add("nodeId", nodeId);
        ClientResponse response = webResource.queryParams(queryParams).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }

        WorkflowInstanceNodeData workflowInstanceNodeData = response.getEntity(WorkflowInstanceNodeData.class);
        return workflowInstanceNodeData;
    }

    public void addWorkflowInstance(String experimentId, String workflowInstanceId, String templateName){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("experimentId", experimentId);
        formParams.add("workflowInstanceId", workflowInstanceId);
        formParams.add("templateName", templateName);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void updateWorkflowNodeType(WorkflowInstanceNode node, WorkflowNodeType type){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODETYPE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", node.getWorkflowInstance().getWorkflowInstanceId());
        formParams.add("nodeId", node.getNodeId());
        formParams.add("nodeType", type.getNodeType().name());

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

    public void addWorkflowInstanceNode(String workflowInstance, String nodeId){
        webResource = getProvenanceRegistryBaseResource().path(ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCENODE);
        MultivaluedMap formParams = new MultivaluedMapImpl();
        formParams.add("workflowInstanceId", workflowInstance);
        formParams.add("nodeId", nodeId);

        ClientResponse response = webResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, formParams);
        int status = response.getStatus();
        if (status != 200) {
            logger.error("Failed : HTTP error code : " + status);
            throw new RuntimeException("Failed : HTTP error code : "
                    + status);
        }
    }

}
