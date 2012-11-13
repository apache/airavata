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

package org.apache.airavata.services.registry.rest.resources;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentDataList;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentIDList;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkflowInstancesList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is the REST interface for all the provenance data related methods that are exposed
 * by Airavata Registry API
 */
@Path(ResourcePathConstants.ProvenanceResourcePathConstants.REGISTRY_API_PROVENANCEREGISTRY)
public class ProvenanceRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**
     * --------------------------------- Provenance Registry ----------------------------------*
     */

    /**
     * This method will update the experiment execution user
     * @param experimentId experiment ID
     * @param user experiment execution user
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENT_EXECUTIONUSER)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentExecutionUser(@FormParam("experimentId") String experimentId,
                                                  @FormParam("user") String user) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentExecutionUser(experimentId, user);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Experiment execution user updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve experiment execution user
     * @param experimentId  experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_EXECUTIONUSER)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getExperimentExecutionUser(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String user = airavataRegistry.getExperimentExecutionUser(experimentId);
            if (user != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(user);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_MODIFIED);
                builder.entity("Could not get experiment execution user...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve the experiment name for a given experiment ID
     * @param experimentId  experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_NAME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getExperimentName(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String result = airavataRegistry.getExperimentName(experimentId);
            if (result != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(result);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_MODIFIED);
                builder.entity("Experiment name not available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the experiment name
     * @param experimentId experiment ID
     * @param experimentName experiment name
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTNAME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentName(@FormParam("experimentId") String experimentId,
                                         @FormParam("experimentName") String experimentName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentName(experimentId, experimentName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Experiment Name updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve the experiment metadata
     * @param experimentId experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTMETADATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getExperimentMetadata(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String result = airavataRegistry.getExperimentMetadata(experimentId);
            if (result != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(result);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Experiment metadata not available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the experiment metadata
     * @param experimentId experiment ID
     * @param metadata experiment metadata
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_EXPERIMENTMETADATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentMetadata(@FormParam("experimentId") String experimentId,
                                             @FormParam("metadata") String metadata) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentMetadata(experimentId, metadata);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Experiment metadata updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    /**
     * This method will retrieve workflow execution name
     * @param workflowInstanceId workflow instance ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWTEMPLATENAME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWorkflowExecutionTemplateName(@QueryParam("workflowInstanceId") String workflowInstanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String result = airavataRegistry.getWorkflowExecutionTemplateName(workflowInstanceId);
            if (result != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(result);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Workflow template name not available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will set the workflow instance template name
     * @param workflowInstanceId workflow instance id
     * @param templateName template name
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCETEMPLATENAME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setWorkflowInstanceTemplateName(@FormParam("workflowInstanceId") String workflowInstanceId,
                                                    @FormParam("templateName") String templateName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow template name updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will get experiment workflow instances for a given experiment ID
     * @param experimentId experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENTWORKFLOWINSTANCES)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentWorkflowInstances(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowInstance> experimentWorkflowInstances = airavataRegistry.getExperimentWorkflowInstances(experimentId);
            WorkflowInstancesList workflowInstancesList = new WorkflowInstancesList();
            WorkflowInstance[] workflowInstances = new WorkflowInstance[experimentWorkflowInstances.size()];
            for (int i = 0; i < experimentWorkflowInstances.size(); i++) {
                workflowInstances[i] = experimentWorkflowInstances.get(i);
            }
            workflowInstancesList.setWorkflowInstances(workflowInstances);
            if (experimentWorkflowInstances.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowInstancesList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No workflow instances available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will check whether a workflow instance exists
     * @param instanceId workflow instance ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_EXIST_CHECK)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceExists(@QueryParam("instanceId") String instanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Boolean result = airavataRegistry.isWorkflowInstanceExists(instanceId);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Workflow instance available...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("Workflow instance does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will check whether a workflow instance exist and create the workflow instance
     * according to createIfNotPresent flag
     * @param instanceId workflow instance ID
     * @param createIfNotPresent flag whether to create a new workflow instance or not
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST_CREATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceExistsThenCreate(@QueryParam("instanceId") String instanceId,
                                                       @QueryParam("createIfNotPresent") boolean createIfNotPresent) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Boolean result = airavataRegistry.isWorkflowInstanceExists(instanceId, createIfNotPresent);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("New workflow instance has been created...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("Could not create workflow instance...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update workflow instance status
     * @param instanceId  workflow instance ID
     * @param executionStatus workflow execution status
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS_INSTANCEID)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowInstanceStatusByInstance(@FormParam("instanceId") String instanceId,
                                                           @FormParam("executionStatus") String executionStatus) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkflowInstanceStatus.ExecutionStatus status = WorkflowInstanceStatus.ExecutionStatus.valueOf(executionStatus);
            airavataRegistry.updateWorkflowInstanceStatus(instanceId, status);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow instance status updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the workflow instance status
     * @param workflowInstanceId  workflow instance ID
     * @param executionStatus workflow execution status
     * @param statusUpdateTime workflow status update time
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWINSTANCESTATUS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowInstanceStatus(@FormParam("workflowInstanceId") String workflowInstanceId,
                                                 @FormParam("executionStatus") String executionStatus,
                                                 @FormParam("statusUpdateTime") String statusUpdateTime) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(statusUpdateTime);
            WorkflowInstance workflowInstance = airavataRegistry.getWorkflowInstanceData(workflowInstanceId).getWorkflowInstance();
            WorkflowInstanceStatus.ExecutionStatus status = WorkflowInstanceStatus.ExecutionStatus.valueOf(executionStatus);
            WorkflowInstanceStatus workflowInstanceStatus = new WorkflowInstanceStatus(workflowInstance, status, formattedDate);
            airavataRegistry.updateWorkflowInstanceStatus(workflowInstanceStatus);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow instance status updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve workflow instance statuse for a given workflow instance ID
     * @param instanceId workflow instance ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCESTATUS)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkflowInstanceStatus(@QueryParam("instanceId") String instanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkflowInstanceStatus workflowInstanceStatus = airavataRegistry.getWorkflowInstanceStatus(instanceId);
            if (workflowInstanceStatus != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowInstanceStatus);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not get workflow instance status...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update workflowNodeInput
     * @param nodeID workflow node ID
     * @param workflowInstanceID  workflow instance ID
     * @param data input data
     * @return  HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEINPUT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeInput(@FormParam("nodeID") String nodeID,
                                            @FormParam("workflowInstanceId") String workflowInstanceID,
                                            @FormParam("data") String data) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceID);
            WorkflowInstanceNode workflowInstanceNode = workflowInstanceData.getNodeData(nodeID).getWorkflowInstanceNode();
            airavataRegistry.updateWorkflowNodeInput(workflowInstanceNode, data);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow node input saved successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will update workflow node output
     * @param nodeID workflow node ID
     * @param workflowInstanceID workflow instance ID
     * @param data workflow node output data
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODEOUTPUT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeOutput(@FormParam("nodeID") String nodeID,
                                             @FormParam("workflowInstanceId") String workflowInstanceID,
                                             @FormParam("data") String data) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceID);
            WorkflowInstanceNode workflowInstanceNode = workflowInstanceData.getNodeData(nodeID).getWorkflowInstanceNode();
            airavataRegistry.updateWorkflowNodeOutput(workflowInstanceNode, data);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow node output saved successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /*
    @GET
    @Path("search/workflowinstancenodeinput")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response searchWorkflowInstanceNodeInput(@QueryParam("experimentIdRegEx") String experimentIdRegEx,
                                                    @QueryParam("workflowNameRegEx") String workflowNameRegEx,
                                                    @QueryParam("nodeNameRegEx") String nodeNameRegEx) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowNodeIOData> workflowNodeIODataList = airavataRegistry.searchWorkflowInstanceNodeInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
            WorkflowNodeIODataMapping[] workflowNodeIODataCollection = new WorkflowNodeIODataMapping[workflowNodeIODataList.size()];
            WorkflowNodeIODataList workflowNodeIOData = new WorkflowNodeIODataList();
            for (int i = 0; i < workflowNodeIODataList.size(); i++) {
                WorkflowNodeIOData nodeIOData = workflowNodeIODataList.get(i);
                WorkflowNodeIODataMapping workflowNodeIODataMapping = new WorkflowNodeIODataMapping();

                workflowNodeIODataMapping.setExperimentId(nodeIOData.getExperimentId());
                workflowNodeIODataMapping.setWorkflowId(nodeIOData.getWorkflowId());
                workflowNodeIODataMapping.setWorkflowInstanceId(nodeIOData.getWorkflowInstanceId());
                workflowNodeIODataMapping.setWorkflowName(nodeIOData.getWorkflowName());
                workflowNodeIODataMapping.setWorkflowNodeType(nodeIOData.getNodeType().toString());
                workflowNodeIODataCollection[i] = workflowNodeIODataMapping;
            }
            workflowNodeIOData.setWorkflowNodeIOData(workflowNodeIODataCollection);
            if (workflowNodeIODataList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeIOData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("search/workflowinstancenodeoutput")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response searchWorkflowInstanceNodeOutput(@QueryParam("experimentIdRegEx") String experimentIdRegEx,
                                                     @QueryParam("workflowNameRegEx") String workflowNameRegEx,
                                                     @QueryParam("nodeNameRegEx") String nodeNameRegEx) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowNodeIOData> workflowNodeIODataList = airavataRegistry.searchWorkflowInstanceNodeOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
            WorkflowNodeIODataMapping[] workflowNodeIODataCollection = new WorkflowNodeIODataMapping[workflowNodeIODataList.size()];
            WorkflowNodeIODataList workflowNodeIOData = new WorkflowNodeIODataList();
            for (int i = 0; i < workflowNodeIODataList.size(); i++) {
                WorkflowNodeIOData nodeIOData = workflowNodeIODataList.get(i);
                WorkflowNodeIODataMapping workflowNodeIODataMapping = new WorkflowNodeIODataMapping();

                workflowNodeIODataMapping.setExperimentId(nodeIOData.getExperimentId());
                workflowNodeIODataMapping.setWorkflowId(nodeIOData.getWorkflowId());
                workflowNodeIODataMapping.setWorkflowInstanceId(nodeIOData.getWorkflowInstanceId());
                workflowNodeIODataMapping.setWorkflowName(nodeIOData.getWorkflowName());
                workflowNodeIODataMapping.setWorkflowNodeType(nodeIOData.getNodeType().toString());
                workflowNodeIODataCollection[i] = workflowNodeIODataMapping;
            }
            workflowNodeIOData.setWorkflowNodeIOData(workflowNodeIODataCollection);
            if (workflowNodeIODataList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeIOData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/workflowinstancenodeinput")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkflowInstanceNodeInput(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                                 @QueryParam("nodeType") String nodeType) {
        // Airavata JPA Registry method returns null at the moment
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowNodeIOData> workflowNodeIODataList = airavataRegistry.getWorkflowInstanceNodeInput(workflowInstanceId, nodeType);
            WorkflowNodeIODataMapping[] workflowNodeIODataCollection = new WorkflowNodeIODataMapping[workflowNodeIODataList.size()];
            WorkflowNodeIODataList workflowNodeIOData = new WorkflowNodeIODataList();
            for (int i = 0; i < workflowNodeIODataList.size(); i++) {
                WorkflowNodeIOData nodeIOData = workflowNodeIODataList.get(i);
                WorkflowNodeIODataMapping workflowNodeIODataMapping = new WorkflowNodeIODataMapping();

                workflowNodeIODataMapping.setExperimentId(nodeIOData.getExperimentId());
                workflowNodeIODataMapping.setWorkflowId(nodeIOData.getWorkflowId());
                workflowNodeIODataMapping.setWorkflowInstanceId(nodeIOData.getWorkflowInstanceId());
                workflowNodeIODataMapping.setWorkflowName(nodeIOData.getWorkflowName());
                workflowNodeIODataMapping.setWorkflowNodeType(nodeIOData.getNodeType().toString());
                workflowNodeIODataCollection[i] = workflowNodeIODataMapping;
            }
            workflowNodeIOData.setWorkflowNodeIOData(workflowNodeIODataCollection);
            if (workflowNodeIODataList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeIOData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/workflowinstancenodeoutput")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkflowInstanceNodeOutput(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                                  @QueryParam("nodeType") String nodeType) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowNodeIOData> workflowNodeIODataList = airavataRegistry.getWorkflowInstanceNodeOutput(workflowInstanceId, nodeType);
            WorkflowNodeIODataMapping[] workflowNodeIODataCollection = new WorkflowNodeIODataMapping[workflowNodeIODataList.size()];
            WorkflowNodeIODataList workflowNodeIOData = new WorkflowNodeIODataList();
            for (int i = 0; i < workflowNodeIODataList.size(); i++) {
                WorkflowNodeIOData nodeIOData = workflowNodeIODataList.get(i);
                WorkflowNodeIODataMapping workflowNodeIODataMapping = new WorkflowNodeIODataMapping();

                workflowNodeIODataMapping.setExperimentId(nodeIOData.getExperimentId());
                workflowNodeIODataMapping.setWorkflowId(nodeIOData.getWorkflowId());
                workflowNodeIODataMapping.setWorkflowInstanceId(nodeIOData.getWorkflowInstanceId());
                workflowNodeIODataMapping.setWorkflowName(nodeIOData.getWorkflowName());
                workflowNodeIODataMapping.setWorkflowNodeType(nodeIOData.getNodeType().toString());
                workflowNodeIODataCollection[i] = workflowNodeIODataMapping;
            }
            workflowNodeIOData.setWorkflowNodeIOData(workflowNodeIODataCollection);
            if (workflowNodeIODataList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeIOData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }
    */

    /**
     * This method will return all the data related to a given experiment. This will include workflow
     * status, input values, output values to the workflow, node statuses etc.
     * @param experimentId experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperiment(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            ExperimentDataImpl experimentData = (ExperimentDataImpl)airavataRegistry.getExperiment(experimentId);
            if (experimentData != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiments available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the experiment IDs for a given user
     * @param username experiment execution user
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_ID_USER)
    @Produces(MediaType.APPLICATION_XML)
    public Response getExperimentIdByUser(@QueryParam("username") String username) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            ArrayList<String> experiments = (ArrayList)airavataRegistry.getExperimentIdByUser(username);
            ExperimentIDList experimentIDList = new ExperimentIDList();
            experimentIDList.setExperimentIDList(experiments);
            if (experiments.size() != 0){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentIDList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiments available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the experiments for a given user
     * @param username experiment execution user
     * @return  HTTP response
     *
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_USER)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentByUser(@QueryParam("username") String username){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            List<ExperimentData> experimentDataList = airavataRegistry.getExperimentByUser(username);
            ExperimentDataList experimentData = new ExperimentDataList();
            List<ExperimentDataImpl> experimentDatas = new ArrayList<ExperimentDataImpl>();
            for (int i = 0; i < experimentDataList.size(); i ++){
                experimentDatas.add((ExperimentDataImpl)experimentDataList.get(i));
            }
            experimentData.setExperimentDataList(experimentDatas);
            if (experimentDataList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiments available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the workflow node status
     * @param workflowInstanceId  workflow instance ID
     * @param nodeId  node ID
     * @param executionStatus node execution status
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_STATUS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeStatus(@FormParam("workflowInstanceId") String workflowInstanceId,
                                             @FormParam("nodeId") String nodeId,
                                             @FormParam("executionStatus") String executionStatus) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceStatus.ExecutionStatus status = WorkflowInstanceStatus.ExecutionStatus.valueOf(executionStatus);
            airavataRegistry.updateWorkflowNodeStatus(workflowInstanceId, nodeId, status);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow node status updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve workflow status for a given workflow instance and node
     * @param workflowInstanceId workflow instance ID
     * @param nodeId node ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STATUS)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWorkflowNodeStatus(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                          @QueryParam("nodeId") String nodeId){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceId);
            WorkflowInstanceNode workflowInstanceNode = workflowInstanceData.getNodeData(nodeId).getWorkflowInstanceNode();
            WorkflowInstanceNodeStatus workflowNodeStatus = airavataRegistry.getWorkflowNodeStatus(workflowInstanceNode);
            if(workflowNodeStatus != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeStatus);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve workflow node status...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve workflow node started time
     * @param workflowInstanceId workflow instance ID
     * @param nodeId node ID
     * @return  HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWNODE_STARTTIME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWorkflowNodeStartTime(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                             @QueryParam("nodeId") String nodeId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceId);
            WorkflowInstanceNode workflowInstanceNode = workflowInstanceData.getNodeData(nodeId).getWorkflowInstanceNode();
            Date workflowNodeStartTime = airavataRegistry.getWorkflowNodeStartTime(workflowInstanceNode);
            if(workflowNodeStartTime != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowNodeStartTime.toString());
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve workflow node started time...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return workflow started time
     * @param workflowInstanceId workflow instance ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOW_STARTTIME)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWorkflowStartTime(@QueryParam("workflowInstanceId") String workflowInstanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceId);
            WorkflowInstance workflowInstance = workflowInstanceData.getWorkflowInstance();
            Date workflowStartTime = airavataRegistry.getWorkflowStartTime(workflowInstance);
            if(workflowStartTime != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowStartTime.toString());
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve workflow start time...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update workflow node Gram data
     * @param workflowNodeGramData workflow node gram data object as a JSON input
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODE_GRAMDATA)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.updateWorkflowNodeGramData(workflowNodeGramData);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow node Gram data updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will return all the information regarding a workflow instance
     * @param workflowInstanceId workflow instance ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_WORKFLOWINSTANCEDATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWorkflowInstanceData(@QueryParam("workflowInstanceId") String workflowInstanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceData workflowInstanceData = airavataRegistry.getWorkflowInstanceData(workflowInstanceId);
            if (workflowInstanceData != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowInstanceData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve workflow instance data...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method wil check whether a workflow node present
     * @param workflowInstanceId workflow instance ID
     * @param nodeId node ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceNodePresent(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                                  @QueryParam("nodeId") String nodeId){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowInstanceNodePresent = airavataRegistry.isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
            if (workflowInstanceNodePresent){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Workflow instance node exists...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Workflow instance node does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method wil check whether a workflow node present and create a new workflow node according
     * to createIfNotPresent flag
     * @param workflowInstanceId workflow instance Id
     * @param nodeId  node Id
     * @param createIfNotPresent  flag whether to create a new node or not
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_EXIST_CREATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceNodePresentCreate(@FormParam("workflowInstanceId") String workflowInstanceId,
                                                        @FormParam("nodeId") String nodeId,
                                                        @FormParam("createIfNotPresent") boolean createIfNotPresent){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowInstanceNodePresent = airavataRegistry.isWorkflowInstanceNodePresent(workflowInstanceId, nodeId, createIfNotPresent);
            if (workflowInstanceNodePresent){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Workflow instance node exists...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Workflow instance node does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }


    /**
     * This method will return data related to the workflow instance node
     * @param workflowInstanceId workflow instance ID
     * @param nodeId node ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.WORKFLOWINSTANCE_NODE_DATA)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getWorkflowInstanceNodeData(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                                @QueryParam("nodeId") String nodeId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceNodeData workflowInstanceNodeData = airavataRegistry.getWorkflowInstanceNodeData(workflowInstanceId, nodeId);
            if (workflowInstanceNodeData != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowInstanceNodeData);
                return builder.build();
            }  else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve workflow instance node data...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will add a workflow instance
     * @param experimentId experiment ID
     * @param workflowInstanceId  workflow instance ID
     * @param templateName workflow template name
     * @return  HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInstance(@FormParam("experimentId") String experimentId,
                                        @FormParam("workflowInstanceId") String workflowInstanceId,
                                        @FormParam("templateName") String templateName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.addWorkflowInstance(experimentId, workflowInstanceId, templateName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow instance added successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the workflow node type
     * @param workflowInstanceId workflow instance ID
     * @param nodeId node ID
     * @param nodeType node type
     * @return  HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.UPDATE_WORKFLOWNODETYPE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeType(@FormParam("workflowInstanceId") String workflowInstanceId,
                                           @FormParam("nodeId") String nodeId,
                                           @FormParam("nodeType") String nodeType) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceNodeData workflowInstanceNodeData = airavataRegistry.getWorkflowInstanceData(workflowInstanceId).getNodeData(nodeId);
            WorkflowInstanceNode workflowInstanceNode = workflowInstanceNodeData.getWorkflowInstanceNode();
            WorkflowNodeType workflowNodeType = new WorkflowNodeType();

            //currently from API only service node is being used
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
//            workflowNodeType.setNodeType(nodeType);
            airavataRegistry.updateWorkflowNodeType(workflowInstanceNode, workflowNodeType);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow instance node type updated successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    /**
     * This method will add a new node to workflow instance
     * @param workflowInstanceId workflow instance ID
     * @param nodeId  node ID
     * @return  HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.ADD_WORKFLOWINSTANCENODE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInstanceNode(@FormParam("workflowInstanceId") String workflowInstanceId,
                                            @FormParam("nodeId") String nodeId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.addWorkflowInstanceNode(workflowInstanceId, nodeId);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow instance node added successfully...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    /**
     * This method wil check whether the experiment name exists
     * @param experimentName experiment name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.EXPERIMENTNAME_EXISTS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isExperimentNameExist(@QueryParam("experimentName") String experimentName){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean experimentNameExist = airavataRegistry.isExperimentNameExist(experimentName);
            if (experimentNameExist){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Experiment name exists...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Experiment name does not exists...");
                return builder.build();
            }

        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will return only information regarding to the experiment. Node information will
     * not be retrieved
     * @param experimentId experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_EXPERIMENT_METAINFORMATION)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getExperimentMetaInformation(@QueryParam("experimentId") String experimentId){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            ExperimentDataImpl experimentMetaInformation = (ExperimentDataImpl)airavataRegistry.getExperimentMetaInformation(experimentId);
            if (experimentMetaInformation != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentMetaInformation);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not retrieve experiment meta information...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all meta information for all the experiments
     * @param user experiment execution user
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.GET_ALL_EXPERIMENT_METAINFORMATION)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAllExperimentMetaInformation(@QueryParam("user") String user){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            List<ExperimentData> allExperimentMetaInformation = airavataRegistry.getAllExperimentMetaInformation(user);
            ExperimentDataList experimentDataList = new ExperimentDataList();
            List<ExperimentDataImpl> experimentDatas = new ArrayList<ExperimentDataImpl>();
            for (ExperimentData experimentData : allExperimentMetaInformation){
                experimentDatas.add((ExperimentDataImpl)experimentData);
            }
            experimentDataList.setExperimentDataList(experimentDatas);
            if (allExperimentMetaInformation.size() != 0){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentDataList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiment data available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will search all the experiments which has the name like given experiment name for
     * a given experiment execution user
     * @param user experiment execution user
     * @param experimentNameRegex experiment name
     * @return  HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProvenanceResourcePathConstants.SEARCH_EXPERIMENTS)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response searchExperiments(@QueryParam("user") String user,
                                      @QueryParam("experimentNameRegex") String experimentNameRegex){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            List<ExperimentData> experimentDataList = airavataRegistry.searchExperiments(user, experimentNameRegex);
            ExperimentDataList experimentData = new ExperimentDataList();
            List<ExperimentDataImpl> experimentDatas = new ArrayList<ExperimentDataImpl>();
            for (ExperimentData experimentData1 : experimentDataList){
                experimentDatas.add((ExperimentDataImpl)experimentData1);
            }
            experimentData.setExperimentDataList(experimentDatas);
            if (experimentDataList.size() != 0){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentData);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiment data available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }


}
