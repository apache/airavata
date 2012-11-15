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
import org.apache.airavata.registry.api.AiravataUser;
import org.apache.airavata.registry.api.Gateway;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ResourcePathConstants.BasicRegistryConstants.REGISTRY_API_BASICREGISTRY)
public class BasicRegistryResouce {
    protected static AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    @GET
    @Path(ResourcePathConstants.BasicRegistryConstants.GET_GATEWAY)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getGateway (){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Gateway gateway = airavataRegistry.getGateway();
            if (gateway != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(gateway);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Gateway does not exist...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @GET
    @Path(ResourcePathConstants.BasicRegistryConstants.GET_USER)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAiravataUser (){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            AiravataUser airavataUser = airavataRegistry.getAiravataUser();
            if (airavataUser != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(airavataUser);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Airavata User does not exist...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @POST
    @Path(ResourcePathConstants.BasicRegistryConstants.SET_GATEWAY)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response setGateway (Gateway gateway){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.setGateway(gateway);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Gateway added successfully");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @POST
    @Path(ResourcePathConstants.BasicRegistryConstants.SET_USER)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response setAiravataUser (AiravataUser user){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.setAiravataUser(user);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Airavata user added successfully");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }
}
