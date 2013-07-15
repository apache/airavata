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
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;
import org.apache.airavata.rest.mappings.resourcemappings.Workflow;
import org.apache.airavata.rest.mappings.resourcemappings.WorkflowList;
import org.apache.airavata.rest.mappings.utils.ResourcePathConstants;
import org.apache.airavata.rest.mappings.utils.RegPoolUtils;
import org.apache.airavata.services.registry.rest.utils.WebAppUtil;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is a REST interface to all the operations related to user workflows that has been
 * exposed by Airavata Registry API
 */
@Path(ResourcePathConstants.UserWFConstants.REGISTRY_API_USERWFREGISTRY)
public class UserWorkflowRegistryResource {

    @Context
    ServletContext context;

    /**---------------------------------User Workflow Registry----------------------------------**/

    /**
     * This method will check whether a given user workflow name already exists
     *
     * @param workflowName workflow name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.UserWFConstants.WORKFLOW_EXIST)
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowExists(@QueryParam("workflowName") String workflowName) {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            boolean workflowExists = airavataRegistry.isWorkflowExists(workflowName);
            if (workflowExists) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("False");
                return builder.build();
            }
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.WORKFLOW_EXIST, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

    /**
     * This method will add a new workflow
     *
     * @param workflowName     workflow name
     * @param workflowGraphXml workflow content
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.UserWFConstants.ADD_WORKFLOW)
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED,MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflow(@FormParam("workflowName") String workflowName,
                                @FormParam("workflowGraphXml") String workflowGraphXml) {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            airavataRegistry.addWorkflow(workflowName, workflowGraphXml);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow added successfully...");
            return builder.build();
        } catch (UserWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.ADD_WORKFLOW, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

    /**
     * This method will update the workflow
     *
     * @param workflowName     workflow name
     * @param workflowGraphXml workflow content
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.UserWFConstants.UPDATE_WORKFLOW)
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflow(@FormParam("workflowName") String workflowName,
                                   @FormParam("workflowGraphXml") String workflowGraphXml) {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            airavataRegistry.updateWorkflow(workflowName, workflowGraphXml);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow updated successfully...");
            return builder.build();
        } catch (UserWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.UPDATE_WORKFLOW, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

    /**
     * This method will return the content of the given workflow
     *
     * @param workflowName workflow name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.UserWFConstants.GET_WORKFLOWGRAPH)
    @Produces({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public Response getWorkflowGraphXML(@QueryParam("workflowName") String workflowName) {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            String workflowGraphXML = airavataRegistry.getWorkflowGraphXML(workflowName);
            if (workflowGraphXML != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowGraphXML);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.GET_WORKFLOWGRAPH, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

    /**
     * This method will return all the user workflows
     *
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.UserWFConstants.GET_WORKFLOWS)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkflows() {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            Map<String, String> workflows = airavataRegistry.getWorkflows();
            WorkflowList workflowList = new WorkflowList();
            List<Workflow> workflowsModels = new ArrayList<Workflow>();
            for (String workflowName : workflows.keySet()) {
                Workflow workflow = new Workflow();
                workflow.setWorkflowName(workflowName);
                workflow.setWorkflowGraph(workflows.get(workflowName));
                workflowsModels.add(workflow);
            }
            workflowList.setWorkflowList(workflowsModels);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity(workflowList);
            return builder.build();
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.GET_WORKFLOWS, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }

    /**
     * This method will delete a user workflow with the given user workflow name
     *
     * @param workflowName user workflow name
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.UserWFConstants.REMOVE_WORKFLOW)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeWorkflow(@QueryParam("workflowName") String workflowName) {
        AiravataRegistry2 airavataRegistry = RegPoolUtils.acquireRegistry(context);
        try {
            airavataRegistry.removeWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow removed successfully...");
            return builder.build();
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (Throwable e) {
            return WebAppUtil.reportInternalServerError(ResourcePathConstants.UserWFConstants.REMOVE_WORKFLOW, e);
        } finally {
            if (airavataRegistry != null) {
                RegPoolUtils.releaseRegistry(context, airavataRegistry);
            }
        }
    }
}
