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

package org.apache.airavata.experiment.catalog.resources;

import org.apache.airavata.experiment.catalog.Resource;
import org.apache.airavata.experiment.catalog.ResourceType;
import org.apache.airavata.experiment.catalog.ResourceUtils;
import org.apache.airavata.experiment.catalog.model.*;
import org.apache.airavata.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.airavata.registry.cpi.RegistryException;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WorkflowNodeDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowNodeDetailResource.class);
    private String experimentId;
    private String nodeInstanceId;
    private Timestamp creationTime;
    private String nodeName;
    private String executionUnit;
    private String executionUnitData;
    private List<TaskDetailResource> taskDetailResourceList;
    private List<NodeInputResource> nodeInputs;
    private List<NodeOutputResource> nodeOutputs;
    private StatusResource nodeStatus;
    private List<ErrorDetailResource> erros;

    public List<TaskDetailResource> getTaskDetailResourceList() {
        return taskDetailResourceList;
    }

    public void setTaskDetailResourceList(List<TaskDetailResource> taskDetailResourceList) {
        this.taskDetailResourceList = taskDetailResourceList;
    }

    public void setNodeInputs(List<NodeInputResource> nodeInputs) {
        this.nodeInputs = nodeInputs;
    }

    public void setNodeOutputs(List<NodeOutputResource> nodeOutputs) {
        this.nodeOutputs = nodeOutputs;
    }

    public StatusResource getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(StatusResource nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public List<ErrorDetailResource> getErros() {
        return erros;
    }

    public void setErros(List<ErrorDetailResource> erros) {
        this.erros = erros;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
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

    public Resource create(ResourceType type) throws RegistryException{
        switch (type){
            case TASK_DETAIL:
                TaskDetailResource taskDetailResource = new TaskDetailResource();
                taskDetailResource.setNodeId(nodeInstanceId);
                return taskDetailResource;
            case ERROR_DETAIL:
                ErrorDetailResource errorDetailResource = new ErrorDetailResource();
                errorDetailResource.setNodeId(nodeInstanceId);;
                return errorDetailResource;
            case NODE_INPUT:
                NodeInputResource nodeInputResource = new NodeInputResource();
                nodeInputResource.setNodeId(nodeInstanceId);
                return nodeInputResource;
            case NODE_OUTPUT:
                NodeOutputResource nodeOutputResource = new NodeOutputResource();
                nodeOutputResource.setNodeId(nodeInstanceId);
                return nodeOutputResource;
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setNodeId(nodeInstanceId);
                return statusResource;
            default:
                logger.error("Unsupported resource type for workflow node detail resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for workflow node detail resource.");
        }
    }

    public void remove(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
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
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.WORKFLOW_NODE.toString());
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for experiment resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public Resource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case TASK_DETAIL:
                    generator = new QueryGenerator(TASK_DETAIL);
                    generator.setParameter(TaskDetailConstants.TASK_ID, name);
                    q = generator.selectQuery(em);
                    TaskDetail taskDetail = (TaskDetail) q.getSingleResult();
                    TaskDetailResource taskDetailResource = (TaskDetailResource) Utils.getResource(ResourceType.TASK_DETAIL, taskDetail);
                    em.getTransaction().commit();
                    em.close();
                    return taskDetailResource;
                case ERROR_DETAIL:
                    generator = new QueryGenerator(ERROR_DETAIL);
                    generator.setParameter(ErrorDetailConstants.NODE_INSTANCE_ID, name);
                    q = generator.selectQuery(em);
                    ErrorDetail errorDetail = (ErrorDetail) q.getSingleResult();
                    ErrorDetailResource errorDetailResource = (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
                    em.getTransaction().commit();
                    em.close();
                    return errorDetailResource;
                case NODE_INPUT:
                    generator = new QueryGenerator(NODE_INPUT);
                    generator.setParameter(NodeInputConstants.NODE_INSTANCE_ID, name);
                    q = generator.selectQuery(em);
                    NodeInput nodeInput = (NodeInput) q.getSingleResult();
                    NodeInputResource nodeInputResource = (NodeInputResource) Utils.getResource(ResourceType.NODE_INPUT, nodeInput);
                    em.getTransaction().commit();
                    em.close();
                    return nodeInputResource;
                case NODE_OUTPUT:
                    generator = new QueryGenerator(NODE_OUTPUT);
                    generator.setParameter(NodeOutputConstants.NODE_INSTANCE_ID, name);
                    q = generator.selectQuery(em);
                    NodeOutput nodeOutput = (NodeOutput) q.getSingleResult();
                    NodeOutputResource nodeOutputResource = (NodeOutputResource) Utils.getResource(ResourceType.NODE_OUTPUT, nodeOutput);
                    em.getTransaction().commit();
                    em.close();
                    return nodeOutputResource;
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.NODE_INSTANCE_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.WORKFLOW_NODE.toString());
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusResource statusResource = (StatusResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for workflow node resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for workflow node resource.");
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<Resource> get(ResourceType type) throws RegistryException{
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case TASK_DETAIL:
                    generator = new QueryGenerator(TASK_DETAIL);
                    generator.setParameter(TaskDetailConstants.NODE_INSTANCE_ID, nodeInstanceId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            TaskDetail taskDetail = (TaskDetail) result;
                            TaskDetailResource taskDetailResource =
                                    (TaskDetailResource) Utils.getResource(ResourceType.TASK_DETAIL, taskDetail);
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
                                    (ErrorDetailResource) Utils.getResource(ResourceType.ERROR_DETAIL, errorDetail);
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
                                    (NodeInputResource) Utils.getResource(ResourceType.NODE_INPUT, nodeInput);
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
                                    (NodeOutputResource) Utils.getResource(ResourceType.NODE_OUTPUT, nodeOutput);
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
                                    (StatusResource) Utils.getResource(ResourceType.STATUS, status);
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
        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            WorkflowNodeDetail existingNode = em.find(WorkflowNodeDetail.class, nodeInstanceId);
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowNodeDetail workflowNodeDetail = new WorkflowNodeDetail();
            workflowNodeDetail.setNodeId(nodeInstanceId);
            workflowNodeDetail.setExpId(experimentId);
            workflowNodeDetail.setCreationTime(creationTime);
            workflowNodeDetail.setNodeName(nodeName);
            workflowNodeDetail.setExecutionUnit(getExecutionUnit());
            workflowNodeDetail.setExecutionUnitData(getExecutionUnitData());

            if (existingNode != null) {
                existingNode.setExpId(experimentId);
                existingNode.setCreationTime(creationTime);
                existingNode.setNodeName(nodeName);
                existingNode.setExecutionUnit(getExecutionUnit());
                existingNode.setExecutionUnitData(getExecutionUnitData());
                workflowNodeDetail = em.merge(existingNode);
            } else {
                em.persist(workflowNodeDetail);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<NodeInputResource> getNodeInputs() {
        return nodeInputs;
    }

    public List<NodeOutputResource> getNodeOutputs() {
        return nodeOutputs;
    }

    public List<NodeInputResource> getNodeInputs1() throws RegistryException{
        List<NodeInputResource> nodeInputResourceList = new ArrayList<NodeInputResource>();
        List<Resource> resources = get(ResourceType.NODE_INPUT);
        for (Resource resource : resources) {
            NodeInputResource nodeInputResource = (NodeInputResource) resource;
            nodeInputResourceList.add(nodeInputResource);
        }
        return nodeInputResourceList;
    }

    public List<NodeOutputResource> getNodeOutputs1() throws RegistryException{
        List<NodeOutputResource> outputResources = new ArrayList<NodeOutputResource>();
        List<Resource> resources = get(ResourceType.NODE_OUTPUT);
        for (Resource resource : resources) {
            NodeOutputResource nodeOutputResource = (NodeOutputResource) resource;
            outputResources.add(nodeOutputResource);
        }
        return outputResources;
    }

    public StatusResource getWorkflowNodeStatus() throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource nodeStatus = (StatusResource) resource;
            if(nodeStatus.getStatusType().equals(StatusType.WORKFLOW_NODE.toString())){
                if (nodeStatus.getState() == null || nodeStatus.getState().equals("") ){
                    nodeStatus.setState("UNKNOWN");
                }
                return nodeStatus;
            }
        }
        return null;
    }

    public StatusResource getTaskStatus(String taskId) throws RegistryException{
        List<Resource> resources = get(ResourceType.STATUS);
        for (Resource resource : resources) {
            StatusResource taskStatus = (StatusResource) resource;
            if(taskStatus.getStatusType().equals(StatusType.TASK.toString()) && taskStatus.getTaskId().equals(taskId)){
                if (taskStatus.getState() == null || taskStatus.getState().equals("") ){
                    taskStatus.setState("UNKNOWN");
                }
                return taskStatus;
            }
        }
        return null;
    }

    public List<TaskDetailResource> getTaskDetails() throws RegistryException{
        List<TaskDetailResource> taskDetailResources = new ArrayList<TaskDetailResource>();
        List<Resource> resources = get(ResourceType.TASK_DETAIL);
        for (Resource resource : resources) {
            TaskDetailResource taskDetailResource = (TaskDetailResource) resource;
            taskDetailResources.add(taskDetailResource);
        }
        return taskDetailResources;
    }

    public List<ErrorDetailResource> getErrorDetails() throws RegistryException{
        List<ErrorDetailResource> errorDetails = new ArrayList<ErrorDetailResource>();
        List<Resource> resources = get(ResourceType.ERROR_DETAIL);
        for (Resource resource : resources) {
            ErrorDetailResource errorDetailResource = (ErrorDetailResource) resource;
            errorDetails.add(errorDetailResource);
        }
        return errorDetails;
    }

    public TaskDetailResource getTaskDetail(String taskId) throws RegistryException{
        return (TaskDetailResource)get(ResourceType.TASK_DETAIL, taskId);
    }

	public String getExecutionUnit() {
		return executionUnit;
	}

	public void setExecutionUnit(String executionUnit) {
		this.executionUnit = executionUnit;
	}

	public String getExecutionUnitData() {
		return executionUnitData;
	}

	public void setExecutionUnitData(String executionUnitData) {
		this.executionUnitData = executionUnitData;
	}
}
