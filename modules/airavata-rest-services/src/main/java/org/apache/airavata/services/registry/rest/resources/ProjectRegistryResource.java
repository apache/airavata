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
import org.apache.airavata.registry.api.WorkspaceProject;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;
import org.apache.airavata.services.registry.rest.resourcemappings.WorkspaceProjectList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * This class is a REST interface for all the operations related to Project that are exposed in
 * Airavata Registry API
 */
@Path(ResourcePathConstants.ProjectResourcePathConstants.REGISTRY_API_PROJECTREGISTRY)
public class ProjectRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**
     * ---------------------------------Project Registry----------------------------------*
     */

    /**
     * This method will check whether a given project name exists
     * @param projectName project name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkspaceProjectExists(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            boolean result = airavataRegistry.isWorkspaceProjectExists(projectName);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Project exists...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("Project does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will check whether a project exists and create according to the createIfNotExists
     * flag
     * @param projectName project name
     * @param createIfNotExists  flag to check whether a new project should be created or not
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProjectResourcePathConstants.PROJECT_EXIST_CREATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkspaceProjectExistsCreate(@FormParam("projectName") String projectName,
                                             @FormParam("createIfNotExists") String createIfNotExists) {
        boolean createIfNotExistStatus = false;
        if (createIfNotExists.equals("true")) {
            createIfNotExistStatus = true;
        }
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            boolean result = airavataRegistry.isWorkspaceProjectExists(projectName, createIfNotExistStatus);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("New project has been created...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("Could not create the project...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will add new workspace project
     * @param projectName project name
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProjectResourcePathConstants.ADD_PROJECT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkspaceProject(@FormParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkspaceProject workspaceProject = new WorkspaceProject(projectName, airavataRegistry);
            airavataRegistry.addWorkspaceProject(workspaceProject);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workspace project added successfully...");
            return builder.build();
        } catch (WorkspaceProjectAlreadyExistsException e) {
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
     * This method will update the workspace project
     * @param projectName project name
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ProjectResourcePathConstants.UPDATE_PROJECT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkspaceProject(@FormParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkspaceProject workspaceProject = new WorkspaceProject(projectName, airavataRegistry);
            airavataRegistry.updateWorkspaceProject(workspaceProject);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workspace project updated successfully...");
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
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
     * This method will delete workspace project
     * @param projectName project name
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ProjectResourcePathConstants.DELETE_PROJECT)
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteWorkspaceProject(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.deleteWorkspaceProject(projectName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workspace project deleted successfully...");
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
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
     * This method will retrieve the workspace project
     * @param projectName project name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECT)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkspaceProject(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkspaceProject workspaceProject = airavataRegistry.getWorkspaceProject(projectName);
            if (workspaceProject != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workspaceProject);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("No workspace projects available...");
                return builder.build();
            }
        } catch (WorkspaceProjectDoesNotExistsException e) {
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
     * This method will retrieve all the workspace projects
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ProjectResourcePathConstants.GET_PROJECTS)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkspaceProjects() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkspaceProject> workspaceProjects = airavataRegistry.getWorkspaceProjects();
            WorkspaceProjectList workspaceProjectList = new WorkspaceProjectList();
            WorkspaceProject[] workspaceProjectSet = new WorkspaceProject[workspaceProjects.size()];
            for (int i = 0; i < workspaceProjects.size(); i++) {
                workspaceProjectSet[i] = workspaceProjects.get(i);
            }
            workspaceProjectList.setWorkspaceProjects(workspaceProjectSet);
            if (workspaceProjects.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workspaceProjectList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No workspace projects available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }
}
