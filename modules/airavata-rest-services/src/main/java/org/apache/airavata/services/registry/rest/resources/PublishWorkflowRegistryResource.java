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
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;
import org.apache.airavata.services.registry.rest.resourcemappings.PublishWorkflowNamesList;
import org.apache.airavata.services.registry.rest.resourcemappings.Workflow;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkflowList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is the REST interface for all the operations related to published workflows that has
 * been exposed by Airavata Registry API
 */
@Path(ResourcePathConstants.PublishedWFConstants.REGISTRY_API_PUBLISHWFREGISTRY)
public class PublishWorkflowRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**---------------------------------Published Workflow Registry----------------------------------**/

    /**
     * This method will check whether a given published workflow name already exists
     * @param workflowname publish workflow name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.PublishedWFConstants.PUBLISHWF_EXIST)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isPublishedWorkflowExists(@QueryParam("workflowname") String workflowname) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowExists = airavataRegistry.isPublishedWorkflowExists(workflowname);
            if (workflowExists){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Publish workflow exists...");
                return builder.build();
            }else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Publish workflow does not exists...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will make a user workflow as a published workflow with the given name
     * @param workflowName user workflow name
     * @param publishWorkflowName name need to save the published workflow as
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.PublishedWFConstants.PUBLISH_WORKFLOW)
    @Produces(MediaType.TEXT_PLAIN)
    public Response publishWorkflow(@FormParam("workflowName") String workflowName,
                                    @FormParam("publishWorkflowName") String publishWorkflowName)  {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.publishWorkflow(workflowName, publishWorkflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow published successfully...");
            return builder.build();
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (PublishedWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will publish a workflow with the default workflow name
     * @param workflowName workflow name
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.PublishedWFConstants.PUBLISH_DEFAULT_WORKFLOW)
    @Produces(MediaType.TEXT_PLAIN)
    public Response publishWorkflow(@FormParam("workflowName") String workflowName){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.publishWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow published successfully...");
            return builder.build();
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (PublishedWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return the worklflow graph
     * @param workflowName workflow name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWGRAPH)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPublishedWorkflowGraphXML(@QueryParam("workflowName") String workflowName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            String publishedWorkflowGraphXML = airavataRegistry.getPublishedWorkflowGraphXML(workflowName);
            if (publishedWorkflowGraphXML !=null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(publishedWorkflowGraphXML);
                return builder.build();
            }else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Could not find workflow graph...");
                return builder.build();
            }
        } catch (PublishedWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the published workflow names
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWNAMES)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPublishedWorkflowNames() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            List<String> publishedWorkflowNames = airavataRegistry.getPublishedWorkflowNames();
            PublishWorkflowNamesList publishWorkflowNamesList = new PublishWorkflowNamesList();
            publishWorkflowNamesList.setPublishWorkflowNames(publishedWorkflowNames);
            if (publishedWorkflowNames.size() != 0){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(publishWorkflowNamesList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No published workflows available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the published workflows
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.PublishedWFConstants.GET_PUBLISHWORKFLOWS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPublishedWorkflows() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            Map<String, String> publishedWorkflows = airavataRegistry.getPublishedWorkflows();
            WorkflowList workflowList = new WorkflowList();
            List<Workflow> workflowsModels = new ArrayList<Workflow>();
            for (String workflowName : publishedWorkflows.keySet()){
                Workflow workflow = new Workflow();
                workflow.setWorkflowName(workflowName);
                workflow.setWorkflowGraph(publishedWorkflows.get(workflowName));
                workflowsModels.add(workflow);
            }
            workflowList.setWorkflowList(workflowsModels);
            if(publishedWorkflows.size() != 0 ){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Publish workflows does not exists...");
                return builder.build();
            }

        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will delete a published workflow with the given published workflow name
     * @param workflowName published workflow name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.PublishedWFConstants.REMOVE_PUBLISHWORKFLOW)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removePublishedWorkflow(@QueryParam("workflowName") String workflowName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.removePublishedWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Publish workflow removed successfully...");
            return builder.build();
        } catch (PublishedWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }
}
