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

import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;
import org.apache.airavata.services.registry.rest.resourcemappings.ExperimentList;
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
import java.util.Date;
import java.util.List;

/**
 * This class is a REST interface all the methods related to experiments that are exposed by
 * Airavata Registry API
 */
@Path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_RESOURCE_PATH)
public class ExperimentRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**
     * ---------------------------------Experiments----------------------------------*
     */

    /**
     * This method will delete an experiment with given experiment ID
     * @param experimentId  experiment ID
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.DELETE_EXP)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeExperiment(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeExperiment(experimentId);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Experiment removed successfully...");
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the experiments available
     * @return HTTP response
     *
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.GET_ALL_EXPS)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperiments(){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<AiravataExperiment> airavataExperimentList = airavataRegistry.getExperiments();
            ExperimentList experimentList = new ExperimentList();
            AiravataExperiment[] experiments = new AiravataExperiment[airavataExperimentList.size()];
            for (int i = 0; i < airavataExperimentList.size(); i++) {
                experiments[i] = airavataExperimentList.get(i);
            }
            experimentList.setExperiments(experiments);
            if (airavataExperimentList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No experiments found...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the experiments for a given project
     * @param projectName project name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_PROJECT)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentsByProject(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<AiravataExperiment> airavataExperimentList = airavataRegistry.getExperiments(projectName);
            ExperimentList experimentList = new ExperimentList();
            AiravataExperiment[] experiments = new AiravataExperiment[airavataExperimentList.size()];
            for (int i = 0; i < airavataExperimentList.size(); i++) {
                experiments[i] = airavataExperimentList.get(i);
            }
            experimentList.setExperiments(experiments);
            if (airavataExperimentList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentList);
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
     * This method will return all the experiments in a given period of time
     * @param fromDate starting date
     * @param toDate end date
     * @return  HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_BY_DATE)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentsByDate(@QueryParam("fromDate") String fromDate,
                                         @QueryParam("toDate") String toDate) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedFromDate = dateFormat.parse(fromDate);
            Date formattedToDate = dateFormat.parse(toDate);
            List<AiravataExperiment> airavataExperimentList = airavataRegistry.getExperiments(formattedFromDate, formattedToDate);
            ExperimentList experimentList = new ExperimentList();
            AiravataExperiment[] experiments = new AiravataExperiment[airavataExperimentList.size()];
            for (int i = 0; i < airavataExperimentList.size(); i++) {
                experiments[i] = airavataExperimentList.get(i);
            }
            experimentList.setExperiments(experiments);
            if (airavataExperimentList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentList);
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
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the experiments for a given project in a given period of time
     * @param projectName project name
     * @param fromDate starting date
     * @param toDate end date
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.GET_EXPS_PER_PROJECT_BY_DATE)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentsByProjectDate(@QueryParam("projectName") String projectName,
                                                @QueryParam("fromDate") String fromDate,
                                                @QueryParam("toDate") String toDate) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedFromDate = dateFormat.parse(fromDate);
            Date formattedToDate = dateFormat.parse(toDate);
            List<AiravataExperiment> airavataExperimentList = airavataRegistry.getExperiments(projectName, formattedFromDate, formattedToDate);
            ExperimentList experimentList = new ExperimentList();
            AiravataExperiment[] experiments = new AiravataExperiment[airavataExperimentList.size()];
            for (int i = 0; i < airavataExperimentList.size(); i++) {
                experiments[i] = airavataExperimentList.get(i);
            }
            experimentList.setExperiments(experiments);
            if (airavataExperimentList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(experimentList);
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
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will add a new experiment
     * @param projectName project name
     * @param experimentID experiment ID
     * @param submittedDate submitted date
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.ADD_EXP)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addExperiment(@FormParam("projectName") String projectName,
                                  @FormParam("experimentID") String experimentID,
                                  @FormParam("submittedDate") String submittedDate) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            AiravataExperiment experiment = new AiravataExperiment();
            experiment.setExperimentId(experimentID);
            Gateway gateway = (Gateway) context.getAttribute(RestServicesConstants.GATEWAY);
            AiravataUser airavataUser = (AiravataUser) context.getAttribute(RestServicesConstants.REGISTRY_USER);
            experiment.setGateway(gateway);
            experiment.setUser(airavataUser);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(submittedDate);
            experiment.setSubmittedDate(formattedDate);
            airavataRegistry.addExperiment(projectName, experiment);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Experiment added successfully...");
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will check whether the given experiment ID exists
     * @param experimentId experiment ID
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isExperimentExists(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.isExperimentExists(experimentId);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("Experiment exists...");
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity("Exprtiment does not exist...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity("Exprtiment does not exist...");
            return builder.build();
        }
    }

    /**
     * This method will check whether an experiment exist and create if not exists according to the
     * createIfNotPresent flag
     * @param experimentId  experiment ID
     * @param createIfNotPresent  flag to check whether to create a new experiment or not
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ExperimentResourcePathConstants.EXP_EXISTS_CREATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isExperimentExistsThenCreate(@QueryParam("experimentId") String experimentId,
                                                 @QueryParam("createIfNotPresent") String createIfNotPresent) {
        boolean createIfNotPresentStatus = false;
        if (createIfNotPresent.equals("true")) {
            createIfNotPresentStatus = true;
        }
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.isExperimentExists(experimentId, createIfNotPresentStatus);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("New experiment created...");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

}
