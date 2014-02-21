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
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WorkflowNodeDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowNodeDetailResource.class);
    private ExperimentResource experimentResource;
    private String nodeInstanceId;
    private Timestamp creationTime;
    private String nodeName;

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public String getNodeInstanceId() {
        return nodeInstanceId;
    }

    public void setNodeInstanceId(String nodeInstanceId) {
        this.nodeInstanceId = nodeInstanceId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public Resource create(ResourceType type) {
        switch (type){
            case TASK_DETAIL:
                TaskDetailResource taskDetailResource = new TaskDetailResource();
                taskDetailResource.setWorkflowNodeDetailResource(this);
                return taskDetailResource;
            case ERROR_DETAIL:
                ErrorDetailResource errorDetailResource = new ErrorDetailResource();
                errorDetailResource.setNodeDetail(this);
                return errorDetailResource;
            case NODE_INPUT:
                NodeInputResource nodeInputResource = new NodeInputResource();
                nodeInputResource.setNodeDetailResource(this);
                return nodeInputResource;
            case NODE_OUTPUT:
                NodeOutputResource nodeOutputResource = new NodeOutputResource();
                nodeOutputResource.setNodeDetailResource(this);
                return nodeOutputResource;
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setWorkflowNodeDetail(this);
                return statusResource;
            default:
                logger.error("Unsupported resource type for workflow node detail resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for workflow node detail resource.");
        }
    }

    @Override
    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case TASK_DETAIL:
                generator = new QueryGenerator(TASK_DETAIL);
                generator.setParameter(TaskDetailConstants.TASK_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case ERROR_DETAIL:
                generator = new QueryGenerator(ERROR_DETAIL);
                generator.setParameter(ErrorDetailConstants.NODE_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case NODE_INPUT:
                generator = new QueryGenerator(NODE_INPUT);
                generator.setParameter(NodeInputConstants.NODE_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case NODE_OUTPUT:
                generator = new QueryGenerator(NODE_OUTPUT);
                generator.setParameter(NodeOutputConstants.NODE_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.NODE_INSTANCE_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                break;
        }
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case TASK_DETAIL:
                generator = new QueryGenerator(TASK_DETAIL);
                generator.setParameter(TaskDetailConstants.TASK_ID, name);
                q = generator.selectQuery(em);
                TaskDetail taskDetail = (TaskDetail)q.getSingleResult();
                TaskDetailResource taskDetailResource = (TaskDetailResource)Utils.getResource(ResourceType.TASK_DETAIL, taskDetail);
                em.getTransaction().commit();
                em.close();
                return taskDetailResource;
            case ERROR_DETAIL:
                generator = new QueryGenerator(ERROR_DETAIL);
                generator.setParameter(ErrorDetailConstants.NODE_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                ErrorDetail errorDetail = (ErrorDetail)q.getSingleResult();
                ErrorDetailResource errorDetailResource = (ErrorDetailResource)Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                em.getTransaction().commit();
                em.close();
                return errorDetailResource;
            case NODE_INPUT:
                generator = new QueryGenerator(NODE_INPUT);
                generator.setParameter(NodeInputConstants.NODE_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                NodeInput nodeInput = (NodeInput)q.getSingleResult();
                NodeInputResource nodeInputResource = (NodeInputResource)Utils.getResource(ResourceType.NODE_INPUT, nodeInput);
                em.getTransaction().commit();
                em.close();
                return nodeInputResource;
            case NODE_OUTPUT:
                generator = new QueryGenerator(NODE_OUTPUT);
                generator.setParameter(NodeOutputConstants.NODE_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                NodeOutput nodeOutput = (NodeOutput)q.getSingleResult();
                NodeOutputResource nodeOutputResource = (NodeOutputResource)Utils.getResource(ResourceType.NODE_OUTPUT, nodeOutput);
                em.getTransaction().commit();
                em.close();
                return nodeOutputResource;
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.NODE_INSTANCE_ID, name);
                q = generator.selectQuery(em);
                Status status = (Status)q.getSingleResult();
                StatusResource statusResource = (StatusResource)Utils.getResource(ResourceType.STATUS, status);
                em.getTransaction().commit();
                em.close();
                return statusResource;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for workflow node resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for workflow node resource.");
        }
    }

    @Override
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case TASK_DETAIL:
                generator = new QueryGenerator(TASK_DETAIL);
                generator.setParameter(TaskDetailConstants.NODE_INSTANCE_ID, nodeInstanceId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        TaskDetail taskDetail = (TaskDetail) result;
                        TaskDetailResource taskDetailResource =
                                (TaskDetailResource)Utils.getResource(ResourceType.TASK_DETAIL, taskDetail);
                        resourceList.add(taskDetailResource);
                    }
                }
                break;
            case ERROR_DETAIL:
                generator = new QueryGenerator(ERROR_DETAIL);
                generator.setParameter(ErrorDetailConstants.NODE_INSTANCE_ID, nodeInstanceId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ErrorDetail errorDetail = (ErrorDetail) result;
                        ErrorDetailResource errorDetailResource =
                                (ErrorDetailResource)Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                        resourceList.add(errorDetailResource);
                    }
                }
                break;
            case NODE_INPUT:
                generator = new QueryGenerator(NODE_INPUT);
                generator.setParameter(NodeInputConstants.NODE_INSTANCE_ID, nodeInstanceId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        NodeInput nodeInput = (NodeInput) result;
                        NodeInputResource nodeInputResource =
                                (NodeInputResource)Utils.getResource(ResourceType.NODE_INPUT, nodeInput);
                        resourceList.add(nodeInputResource);
                    }
                }
                break;
            case NODE_OUTPUT:
                generator = new QueryGenerator(NODE_OUTPUT);
                generator.setParameter(NodeOutputConstants.NODE_INSTANCE_ID, nodeInstanceId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        NodeOutput nodeOutput = (NodeOutput) result;
                        NodeOutputResource nodeOutputResource =
                                (NodeOutputResource)Utils.getResource(ResourceType.NODE_OUTPUT, nodeOutput);
                        resourceList.add(nodeOutputResource);
                    }
                }
                break;
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.NODE_INSTANCE_ID, nodeInstanceId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Status status = (Status) result;
                        StatusResource statusResource =
                                (StatusResource)Utils.getResource(ResourceType.STATUS, status);
                        resourceList.add(statusResource);
                    }
                }
                break;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for workflow node details resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        WorkflowNodeDetail existingNode = em.find(WorkflowNodeDetail.class, nodeInstanceId);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        WorkflowNodeDetail workflowNodeDetail = new WorkflowNodeDetail();
        workflowNodeDetail.setNodeId(nodeInstanceId);
        Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
        workflowNodeDetail.setExperiment(experiment);
        workflowNodeDetail.setExpId(experimentResource.getExpID());
        workflowNodeDetail.setCreationTime(creationTime);
        workflowNodeDetail.setNodeName(nodeName);
        if (existingNode != null){
            existingNode.setExperiment(experiment);
            existingNode.setExpId(experimentResource.getExpID());
            existingNode.setCreationTime(creationTime);
            existingNode.setNodeName(nodeName);
            workflowNodeDetail = em.merge(existingNode);
        }else {
            em.merge(workflowNodeDetail);
        }
        em.getTransaction().commit();
        em.close();
    }

    public List<NodeInputResource> getNodeInputs(){
        List<NodeInputResource> nodeInputResourceList = new ArrayList<NodeInputResource>();
        List<Resource> resources = get(ResourceType.NODE_INPUT);
        for (Resource resource : resources) {
            NodeInputResource nodeInputResource = (NodeInputResource) resource;
            nodeInputResourceList.add(nodeInputResource);
        }
        return nodeInputResourceList;
    }

    public List<NodeOutputResource> getNodeOutputs(){
        List<NodeOutputResource> outputResources = new ArrayList<NodeOutputResource>();
        List<Resource> resources = get(ResourceType.NODE_OUTPUT);
        for (Resource resource : resources) {
            NodeOutputResource nodeOutputResource = (NodeOutputResource) resource;
            outputResources.add(nodeOutputResource);
        }
        return outputResources;
    }

    public StatusResource getWorkflowNodeStatus(){
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource nodeStatus = (StatusResource) resource;
            if(nodeStatus.getStatusType().equals(StatusType.WORKFLOW_NODE)){
                return nodeStatus;
            }
        }
        return null;
    }

    public List<TaskDetailResource> getTaskDetails(){
        List<TaskDetailResource> taskDetailResources = new ArrayList<TaskDetailResource>();
        List<Resource> resources = get(ResourceType.TASK_DETAIL);
        for (Resource resource : resources) {
            TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
            taskDetailResources.add(taskDetailResource);
        }
        return taskDetailResources;
    }

    public List<ErrorDetailResource> getErrorDetails(){
        List<ErrorDetailResource> errorDetails = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.TASK_DETAIL);
        for (Resource resource : resources) {
            ErrorDetailResource errorDetailResource = (ErrorDetailResource) resource;
            errorDetails.add(errorDetailResource);
        }
        return errorDetails;
    }

    public TaskDetailResource getTaskDetail(String taskId){
        return (TaskDetailResource)get(ResourceType.TASK_DETAIL, taskId);
    }
}
