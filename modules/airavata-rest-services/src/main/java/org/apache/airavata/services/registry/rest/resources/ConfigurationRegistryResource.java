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
import org.apache.airavata.services.registry.rest.resourcemappings.ConfigurationList;
import org.apache.airavata.services.registry.rest.resourcemappings.URLList;
import org.apache.airavata.services.registry.rest.utils.ResourcePathConstants;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class is a REST interface to all the methods related to Configuration which are exposed by
 * Airavata Registry API
 */
@Path(ResourcePathConstants.ConfigResourcePathConstants.CONFIGURATION_REGISTRY_RESOURCE)
public class ConfigurationRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**
     * ---------------------------------Configuration Registry----------------------------------*
     */

    /**
     * This method will return the configuration value corrosponding to given config key
     * @param key configuration key
     * @return HTTP Response
     */
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_CONFIGURATION)
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getConfiguration(@QueryParam("key") String key) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Object configuration = airavataRegistry.getConfiguration(key);
            if (configuration != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(configuration);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Configuration does not exist...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will return configuration list for given configuration key
     * @param key configuration key
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_CONFIGURATION_LIST)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getConfigurationList(@QueryParam("key") String key) {
        try {
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            List<Object> configurationList = airavataRegistry.getConfigurationList(key);
            ConfigurationList list = new ConfigurationList();
            Object[] configValList = new Object[configurationList.size()];
            for (int i = 0; i < configurationList.size(); i++) {
                configValList[i] = configurationList.get(i);
            }
            list.setConfigValList(configValList);
            if (configurationList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(list);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No configuration available with given config key...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    /**
     * This method will add a configuration with given config key, config value and expiration date
     * @param key configuration key
     * @param value configuration value
     * @param date configuration expire data
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.SAVE_CONFIGURATION)
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response setConfiguration(@FormParam("key") String key,
                                     @FormParam("value") String value,
                                     @FormParam("date") String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.setConfiguration(key, value, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Configuration saved successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the configuration according to the given config value and expire
     * date
     * @param key config key
     * @param value config value
     * @param date  expiration date
     * @return  HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.UPDATE_CONFIGURATION)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addConfiguration(@FormParam("key") String key,
                                     @FormParam("value") String value,
                                     @FormParam("date") String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.addConfiguration(key, value, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Configuration updated successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will delete all configuration of the given config key
     * @param key configuration key
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_CONFIGURATION)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllConfiguration(@QueryParam("key") String key) {
        try {
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.removeAllConfiguration(key);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("All configurations with given config key removed successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will delete the configuration with the given config key and config value
     * @param key configuration key
     * @param value configuration value
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_CONFIGURATION)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeConfiguration(@QueryParam("key") String key, @QueryParam("value") String value) {
        try {
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.removeConfiguration(key, value);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Configuration removed successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve all the GFac URIs
     * @return  HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_GFAC_URI_LIST)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getGFacURIs() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<URI> uris = airavataRegistry.getGFacURIs();
            URLList list = new URLList();
            String[] urs = new String[uris.size()];
            for (int i = 0; i < uris.size(); i++) {
                urs[i] = uris.get(i).toString();
            }
            list.setUris(urs);
            if (urs.length != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(list);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No GFac URIs available...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve all the workflow interpreter URIs
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_WFINTERPRETER_URI_LIST)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorkflowInterpreterURIs() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<URI> uris = airavataRegistry.getWorkflowInterpreterURIs();
            URLList list = new URLList();
            String[] urs = new String[uris.size()];
            for (int i = 0; i < uris.size(); i++) {
                urs[i] = uris.get(i).toString();
            }
            list.setUris(urs);
            if (urs.length != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(list);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No Workflow Interpreter URIs available...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve eventing URI
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_EVENTING_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getEventingServiceURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI eventingServiceURI = airavataRegistry.getEventingServiceURI();
            if (eventingServiceURI != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(eventingServiceURI.toString());
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No eventing URI available...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve messagebox URI
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.ConfigResourcePathConstants.GET_MESSAGE_BOX_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getMessageBoxURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI eventingServiceURI = airavataRegistry.getMessageBoxURI();
            if (eventingServiceURI != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(eventingServiceURI.toString());
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No message box URI available...");
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will add new GFac URI
     * @param uri gfac URI
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_GFAC_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addGFacURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI gfacURI = new URI(uri);
            airavataRegistry.addGFacURI(gfacURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("GFac URI added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will add new workflow interpreter URI
     * @param uri workflow interpreter URI
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_WFINTERPRETER_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInterpreterURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI interpreterURI = new URI(uri);
            airavataRegistry.addWorkflowInterpreterURI(interpreterURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow interpreter URI added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will set a new eventing URI
     * @param uri eventing URI
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_EVENTING_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setEventingURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI eventingURI = new URI(uri);
            airavataRegistry.setEventingURI(eventingURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Eventing URI set successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will set message box URI
     * @param uri message box URI
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_MESSAGE_BOX_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setMessageBoxURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI msgBoxURI = new URI(uri);
            airavataRegistry.setMessageBoxURI(msgBoxURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("MessageBox URI set successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update GFac URI expiring date
     * @param uri GFac URI
     * @param date Expiration date
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_GFAC_URI_DATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addGFacURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI gfacURI = new URI(uri);
            airavataRegistry.addGFacURI(gfacURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("GFac URI added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update workflow interpreter URI expiration date
     * @param uri workflow interpreter URI
     * @param date workflow interpreter expiration date
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_WFINTERPRETER_URI_DATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInterpreterURI(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI interpreterURI = new URI(uri);
            airavataRegistry.addWorkflowInterpreterURI(interpreterURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow interpreter URI added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update Eventing URI expiration date
     * @param uri eventing URI
     * @param date eventing URI expiration date
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_EVENTING_URI_DATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setEventingURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI eventingURI = new URI(uri);
            airavataRegistry.setEventingURI(eventingURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Eventing URI added successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     *  This method will update expiration date of Message box URI
     * @param uri message box URI
     * @param date message box expiration date
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.ConfigResourcePathConstants.ADD_MSG_BOX_URI_DATE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response setMessageBoxURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI msgBoxURI = new URI(uri);
            airavataRegistry.setMessageBoxURI(msgBoxURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Message box URI retrieved successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove GFac URI
     * @param uri GFac URI
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_GFAC_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeGFacURI(@QueryParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI gfacURI = new URI(uri);
            airavataRegistry.removeGFacURI(gfacURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("GFac URI deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove all the GFac URIs
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_GFAC_URIS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllGFacURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeAllGFacURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("All GFac URIs deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove workflow interpreter URI
     * @param uri workflow interpreter URI
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_WFINTERPRETER_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeWorkflowInterpreterURI(@QueryParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI intURI = new URI(uri);
            airavataRegistry.removeWorkflowInterpreterURI(intURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Workflow Interpreter URI deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove all the workflow interpreter URIs
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_ALL_WFINTERPRETER_URIS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllWorkflowInterpreterURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeAllWorkflowInterpreterURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("All workflow interpreter URIs deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove eventing URI
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_EVENTING_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsetEventingURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.unsetEventingURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Eventing URI deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will remove message box URI
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.ConfigResourcePathConstants.DELETE_MSG_BOX_URI)
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsetMessageBoxURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.unsetMessageBoxURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("MessageBox URI deleted successfully...");
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

}
