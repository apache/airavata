package org.apache.airavata.services.registry.rest.resources;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.*;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.gateway.*;
import org.apache.airavata.registry.api.exception.worker.*;
import org.apache.airavata.registry.api.impl.ExperimentDataImpl;
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.services.registry.rest.resourcemappings.*;
import org.apache.airavata.services.registry.rest.utils.DescriptorUtil;
import org.apache.airavata.services.registry.rest.utils.RestServicesConstants;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * RegistryResource for REST interface of Registry API
 *
 */
@Path("/registry/api")
//public class RegistryResource implements ConfigurationRegistryService,
//        ProjectsRegistryService, ProvenanceRegistryService, UserWorkflowRegistryService,
//        PublishedWorkflowRegistryService, DescriptorRegistryService{
public class RegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    public String getVersion() {
        return null;
    }

    /**
     * ---------------------------------Configuration Registry----------------------------------*
     */

    @Path("/configuration")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }

    }


    @GET
    @Path("/configurationlist")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    @POST
    @Path("save/configuration")
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
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/configuration")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addConfiguration(@FormParam("key") String key,
                                     @FormParam("value") String value,
                                     @FormParam("date") String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.addConfiguration(key, value, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/allconfiguration")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllConfiguration(@QueryParam("key") String key) {
        try {
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.removeAllConfiguration(key);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/configuration")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeConfiguration(@QueryParam("key") String key, @QueryParam("value") String value) {
        try {
            airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
            airavataRegistry.removeConfiguration(key, value);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("gfac/urilist")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    @GET
    @Path("workflowinterpreter/urilist")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("eventingservice/uri")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("messagebox/uri")
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
                return builder.build();
            }
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/gfacuri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addGFacURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI gfacURI = new URI(uri);
            airavataRegistry.addGFacURI(gfacURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/workflowinterpreteruri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInterpreterURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI interpreterURI = new URI(uri);
            airavataRegistry.addWorkflowInterpreterURI(interpreterURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/eventinguri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setEventingURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI eventingURI = new URI(uri);
            airavataRegistry.setEventingURI(eventingURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/msgboxuri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setMessageBoxURI(@FormParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI msgBoxURI = new URI(uri);
            airavataRegistry.setMessageBoxURI(msgBoxURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/gfacuri/date")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addGFacURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI gfacURI = new URI(uri);
            airavataRegistry.addGFacURI(gfacURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/workflowinterpreteruri/date")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInterpreterURI(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI interpreterURI = new URI(uri);
            airavataRegistry.addWorkflowInterpreterURI(interpreterURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/eventinguri/date")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setEventingURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI eventingURI = new URI(uri);
            airavataRegistry.setEventingURI(eventingURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/msgboxuri/date")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setMessageBoxURIByDate(@FormParam("uri") String uri, @FormParam("date") String date) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date formattedDate = dateFormat.parse(date);
            URI msgBoxURI = new URI(uri);
            airavataRegistry.setMessageBoxURI(msgBoxURI, formattedDate);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/gfacuri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeGFacURI(@QueryParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI gfacURI = new URI(uri);
            airavataRegistry.removeGFacURI(gfacURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/allgfacuris")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllGFacURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeAllGFacURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/workflowinterpreteruri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeWorkflowInterpreterURI(@QueryParam("uri") String uri) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            URI intURI = new URI(uri);
            airavataRegistry.removeWorkflowInterpreterURI(intURI);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/allworkflowinterpreteruris")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeAllWorkflowInterpreterURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeAllWorkflowInterpreterURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/eventinguri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsetEventingURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.unsetEventingURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/msgboxuri")
    @Produces(MediaType.TEXT_PLAIN)
    public Response unsetMessageBoxURI() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.unsetMessageBoxURI();
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (Exception e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    /**
     * ---------------------------------Descriptor Registry----------------------------------*
     */


    @GET
    @Path("hostdescriptor/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isHostDescriptorExists(@QueryParam("descriptorName") String descriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isHostDescriptorExists(descriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
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

    @POST
    @Path("hostdescriptor/save/test")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addHostDescriptor(@FormParam("hostName") String hostName,
                                      @FormParam("hostAddress") String hostAddress,
                                      @FormParam("hostEndpoint") String hostEndpoint,
                                      @FormParam("gatekeeperEndpoint") String gatekeeperEndpoint) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(hostName, hostAddress, hostEndpoint, gatekeeperEndpoint);
            airavataRegistry.addHostDescriptor(hostDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            // TODO : Use WEbapplicationExcpetion
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("hostdescriptor/save")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addJSONHostDescriptor(HostDescriptor host) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(host);
            airavataRegistry.addHostDescriptor(hostDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("hostdescriptor/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateHostDescriptor(HostDescriptor host) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(host);
            airavataRegistry.updateHostDescriptor(hostDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("host/description")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHostDescriptor(@QueryParam("hostName") String hostName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = airavataRegistry.getHostDescriptor(hostName);
            HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescription);
            if (hostDescription != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(hostDescriptor);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }

    }

    @DELETE
    @Path("hostdescriptor/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeHostDescriptor(@QueryParam("hostName") String hostName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeHostDescriptor(hostName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/hostdescriptors")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHostDescriptors() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<HostDescription> hostDescriptionList = airavataRegistry.getHostDescriptors();
            HostDescriptionList list = new HostDescriptionList();
            HostDescriptor[] hostDescriptions = new HostDescriptor[hostDescriptionList.size()];
            for (int i = 0; i < hostDescriptionList.size(); i++) {
                HostDescriptor hostDescriptor = DescriptorUtil.createHostDescriptor(hostDescriptionList.get(i));
                hostDescriptions[i] = hostDescriptor;
            }
            list.setHostDescriptions(hostDescriptions);
            if (hostDescriptionList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(list);
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
    @Path("servicedescriptor/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isServiceDescriptorExists(@QueryParam("descriptorName") String descriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isServiceDescriptorExists(descriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
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

    @POST
    @Path("servicedescriptor/save")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addJSONServiceDescriptor(ServiceDescriptor service) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(service);
            airavataRegistry.addServiceDescriptor(serviceDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("servicedescriptor/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateServiceDescriptor(ServiceDescriptor service) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(service);
            airavataRegistry.updateServiceDescriptor(serviceDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("servicedescriptor/description")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServiceDescriptor(@QueryParam("serviceName") String serviceName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ServiceDescription serviceDescription = airavataRegistry.getServiceDescriptor(serviceName);
            ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
            if (serviceDescription != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(serviceDescriptor);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("servicedescriptor/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeServiceDescriptor(@QueryParam("serviceName") String serviceName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeServiceDescriptor(serviceName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/servicedescriptors")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getServiceDescriptors() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<ServiceDescription> serviceDescriptors = airavataRegistry.getServiceDescriptors();
            ServiceDescriptionList list = new ServiceDescriptionList();
            ServiceDescriptor[] serviceDescriptions = new ServiceDescriptor[serviceDescriptors.size()];
            for (int i = 0; i < serviceDescriptors.size(); i++) {
                ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescriptors.get(i));
                serviceDescriptions[i] = serviceDescriptor;
            }
            list.setServiceDescriptions(serviceDescriptions);
            if (serviceDescriptors.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(list);
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
    @Path("applicationdescriptor/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isApplicationDescriptorExists(@QueryParam("serviceName") String serviceName,
                                                  @QueryParam("hostName") String hostName,
                                                  @QueryParam("descriptorName") String descriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isApplicationDescriptorExists(serviceName, hostName, descriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
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

    @POST
    @Path("applicationdescriptor/build/save/test")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addApplicationDescriptorTest(@FormParam("appName") String appName, @FormParam("exeuctableLocation") String exeuctableLocation, @FormParam("scratchWorkingDirectory") String scratchWorkingDirectory, @FormParam("hostName") String hostName,
                                                 @FormParam("projAccNumber") String projAccNumber, @FormParam("queueName") String queueName, @FormParam("cpuCount") String cpuCount, @FormParam("nodeCount") String nodeCount, @FormParam("maxMemory") String maxMemory,
                                                 @FormParam("serviceName") String serviceName, @FormParam("inputName1") String inputName1, @FormParam("inputType1") String inputType1, @FormParam("outputName") String outputName, @FormParam("outputType") String outputType) throws Exception {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);

        System.out.println("application descriptor save started ...");
        ServiceDescription serv = DescriptorUtil.getServiceDescription(serviceName, inputName1, inputType1, outputName, outputType);
        // Creating the descriptor as a temporary measure.
        ApplicationDeploymentDescription app = DescriptorUtil.registerApplication(appName, exeuctableLocation, scratchWorkingDirectory,
                hostName, projAccNumber, queueName, cpuCount, nodeCount, maxMemory);
        try {
            if (!airavataRegistry.isHostDescriptorExists(hostName)) {
                System.out.println(hostName + " host not exist");
//                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
//                return builder.build();
            }

            if (airavataRegistry.isServiceDescriptorExists(serv.getType().getName())) {
                System.out.println(serviceName + " service updated ");
                airavataRegistry.updateServiceDescriptor(serv);
            } else {
                System.out.println(serviceName + " service created ");
                airavataRegistry.addServiceDescriptor(serv);
            }

            if (airavataRegistry.isApplicationDescriptorExists(serv.getType().getName(), hostName, app.getType().getApplicationName().getStringValue())) {
                System.out.println(appName + " app already exists. retruning an error");
//                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
//                return builder.build();
            } else {
                System.out.println(appName + " adding the app");
                airavataRegistry.addApplicationDescriptor(serv.getType().getName(), hostName, app);
            }

//            airavataRegistry.addApplicationDescriptor(serviceName, hostName, app);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("applicationdescriptor/build/save")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addJSONApplicationDescriptor(ApplicationDescriptor applicationDescriptor) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String hostdescName = applicationDescriptor.getHostdescName();
            if (!airavataRegistry.isHostDescriptorExists(hostdescName)) {
                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                return builder.build();
            }
            ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
            ServiceDescriptor serviceDescriptor = applicationDescriptor.getServiceDescriptor();
            String serviceName;
            if (serviceDescriptor != null) {
                if (serviceDescriptor.getServiceName() == null) {
                    serviceName = applicationDescriptor.getName();
                    serviceDescriptor.setServiceName(serviceName);
                } else {
                    serviceName = serviceDescriptor.getServiceName();
                }
                ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
                if (!airavataRegistry.isServiceDescriptorExists(serviceName)) {
                    airavataRegistry.addServiceDescriptor(serviceDescription);
                }
            } else {
                serviceName = applicationDescriptor.getName();
            }
            airavataRegistry.addApplicationDescriptor(serviceName, hostdescName, applicationDeploymentDescription);


            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


//
//    @POST
//    @Path("applicationdescriptor/save")
//    @Consumes(MediaType.TEXT_XML)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response addApplicationDesc(@FormParam("serviceName") String serviceName,
//                                       @FormParam("hostName") String hostName,
//                                       String application) {
//        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
//        try{
//            ApplicationDeploymentDescription applicationDeploymentDescription = ApplicationDeploymentDescription.fromXML(application);
//            airavataRegistry.addApplicationDescriptor(serviceName, hostName, applicationDeploymentDescription);
//            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
//            return builder.build();
//        } catch (DescriptorAlreadyExistsException e){
//            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
//            return builder.build();
//        } catch (XmlException e) {
//            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
//            return builder.build();
//        } catch (RegistryException e) {
//            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
//            return builder.build();
//        }
//
//    }

    @POST
    @Path("applicationdescriptor/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response udpateApplicationDescriptorByDescriptors(ApplicationDescriptor applicationDescriptor) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String hostdescName = applicationDescriptor.getHostdescName();
            if (!airavataRegistry.isHostDescriptorExists(hostdescName)) {
                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                return builder.build();
            }
            ApplicationDeploymentDescription applicationDeploymentDescription = DescriptorUtil.createApplicationDescription(applicationDescriptor);
            ServiceDescriptor serviceDescriptor = applicationDescriptor.getServiceDescriptor();
            String serviceName;
            if (serviceDescriptor != null) {
                if (serviceDescriptor.getServiceName() == null) {
                    serviceName = applicationDescriptor.getName();
                    serviceDescriptor.setServiceName(serviceName);
                } else {
                    serviceName = serviceDescriptor.getServiceName();
                }
                ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(serviceDescriptor);
                if (airavataRegistry.isServiceDescriptorExists(serviceName)) {
                    airavataRegistry.updateServiceDescriptor(serviceDescription);
                } else {
                    airavataRegistry.addServiceDescriptor(serviceDescription);
                }

            } else {
                serviceName = applicationDescriptor.getName();
            }
            airavataRegistry.updateApplicationDescriptor(serviceName, hostdescName, applicationDeploymentDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    @GET
    @Path("applicationdescriptor/description")
    @Produces("text/xml")
    public Response getApplicationDescriptor(@QueryParam("serviceName") String serviceName,
                                             @QueryParam("hostName") String hostName,
                                             @QueryParam("applicationName") String applicationName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ApplicationDeploymentDescription applicationDeploymentDescription = airavataRegistry.getApplicationDescriptor(serviceName, hostName, applicationName);
            ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(applicationDeploymentDescription);
            applicationDescriptor.setHostdescName(hostName);
            ServiceDescription serviceDescription = airavataRegistry.getServiceDescriptor(serviceName);
            ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
            applicationDescriptor.setServiceDescriptor(serviceDescriptor);

            if (applicationDeploymentDescription != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(applicationDescriptor);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("applicationdescriptors/alldescriptors/host/service")
    @Produces("text/xml")
    public Response getApplicationDescriptors(@QueryParam("serviceName") String serviceName,
                                              @QueryParam("hostName") String hostName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ApplicationDeploymentDescription applicationDeploymentDescription = airavataRegistry.getApplicationDescriptors(serviceName, hostName);
            ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(applicationDeploymentDescription);
            applicationDescriptor.setHostdescName(hostName);
            ServiceDescription serviceDescription = airavataRegistry.getServiceDescriptor(serviceName);
            ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
            applicationDescriptor.setServiceDescriptor(serviceDescriptor);

            if (applicationDeploymentDescription != null) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(applicationDescriptor);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("applicationdescriptor/alldescriptors/service")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getApplicationDescriptors(@QueryParam("serviceName") String serviceName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Map<String, ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = airavataRegistry.getApplicationDescriptors(serviceName);
            ApplicationDescriptorList applicationDescriptorList = new ApplicationDescriptorList();
            ApplicationDescriptor[] applicationDescriptors = new ApplicationDescriptor[applicationDeploymentDescriptionMap.size()];
            int i = 0;
            for (String hostName : applicationDeploymentDescriptionMap.keySet()) {
                ApplicationDeploymentDescription applicationDeploymentDescription = applicationDeploymentDescriptionMap.get(hostName);
                ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(applicationDeploymentDescription);
                applicationDescriptor.setHostdescName(hostName);

                ServiceDescription serviceDescription = airavataRegistry.getServiceDescriptor(serviceName);
                ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
                applicationDescriptor.setServiceDescriptor(serviceDescriptor);

                applicationDescriptors[i] = applicationDescriptor;
                i++;
            }
            applicationDescriptorList.setApplicationDescriptors(applicationDescriptors);
            if (applicationDeploymentDescriptionMap.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(applicationDescriptorList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (MalformedDescriptorException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("applicationdescriptor/alldescriptors")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getApplicationDescriptors() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Map<String[], ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = airavataRegistry.getApplicationDescriptors();
            ApplicationDescriptorList applicationDescriptorList = new ApplicationDescriptorList();
            ApplicationDescriptor[] applicationDescriptors = new ApplicationDescriptor[applicationDeploymentDescriptionMap.size()];
            int i = 0;
            for (String[] descriptors : applicationDeploymentDescriptionMap.keySet()) {
                ApplicationDeploymentDescription applicationDeploymentDescription = applicationDeploymentDescriptionMap.get(descriptors);
                ApplicationDescriptor applicationDescriptor = DescriptorUtil.createApplicationDescriptor(applicationDeploymentDescription);
                applicationDescriptor.setHostdescName(descriptors[1]);
                ServiceDescription serviceDescription = airavataRegistry.getServiceDescriptor(descriptors[0]);
                if (serviceDescription == null) {
                    Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                    return builder.build();
                }
                ServiceDescriptor serviceDescriptor = DescriptorUtil.createServiceDescriptor(serviceDescription);
                applicationDescriptor.setServiceDescriptor(serviceDescriptor);
                applicationDescriptors[i] = applicationDescriptor;
                i++;
            }
            applicationDescriptorList.setApplicationDescriptors(applicationDescriptors);
            if (applicationDeploymentDescriptionMap.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(applicationDescriptorList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (MalformedDescriptorException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("applicationdescriptor/delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeApplicationDescriptor(@QueryParam("serviceName") String serviceName,
                                                @QueryParam("hostName") String hostName,
                                                @QueryParam("appName") String appName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeApplicationDescriptor(serviceName, hostName, appName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    /**
     * ---------------------------------Project Registry----------------------------------*
     */
    @GET
    @Path("project/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkspaceProjectExists(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            boolean result = airavataRegistry.isWorkspaceProjectExists(projectName);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("project/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkspaceProjectExists(@FormParam("projectName") String projectName,
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
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity("False");
            return builder.build();
        }
    }

    @POST
    @Path("add/project")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkspaceProject(@FormParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkspaceProject workspaceProject = new WorkspaceProject(projectName, airavataRegistry);
            airavataRegistry.addWorkspaceProject(workspaceProject);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (WorkspaceProjectAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/project")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkspaceProject(@FormParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkspaceProject workspaceProject = new WorkspaceProject(projectName, airavataRegistry);
            airavataRegistry.updateWorkspaceProject(workspaceProject);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @DELETE
    @Path("delete/project")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteWorkspaceProject(@QueryParam("projectName") String projectName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.deleteWorkspaceProject(projectName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/project")
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
                return builder.build();
            }
        } catch (WorkspaceProjectDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/projects")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    /**
     * ---------------------------------Experiments----------------------------------*
     */

    @DELETE
    @Path("delete/experiment")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeExperiment(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeExperiment(experimentId);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiments/all")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperiments() throws RegistryException {
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiments/project")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiments/date")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiments/project/date")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("add/experiment")
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
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        } catch (WorkspaceProjectDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }

    }

    @GET
    @Path("experiment/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isExperimentExists(@QueryParam("experimentId") String experimentId) throws RegistryException {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.isExperimentExists(experimentId);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("True");
            return builder.build();
        } catch (ExperimentDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            builder.entity("False");
            return builder.build();
        }
    }

    @GET
    @Path("experiment/notexist/create")
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
            builder.entity("True");
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    /**
     * --------------------------------- Provenance Registry ----------------------------------*
     */

    @POST
    @Path("update/experiment")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentExecutionUser(@FormParam("experimentId") String experimentId,
                                                  @FormParam("user") String user) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentExecutionUser(experimentId, user);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiment/executionuser")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiment/name")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/experimentname")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentName(@FormParam("experimentId") String experimentId,
                                         @FormParam("experimentName") String experimentName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentName(experimentId, experimentName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }


    @GET
    @Path("get/experimentmetadata")
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
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_MODIFIED);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/experimentmetadata")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateExperimentMetadata(@FormParam("experimentId") String experimentId,
                                             @FormParam("metadata") String metadata) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.updateExperimentMetadata(experimentId, metadata);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    /**
     * --------------------------------- Provenance Registry ----------------------------------*
     */

    @GET
    @Path("get/workflowtemplatename")
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
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_MODIFIED);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/workflowinstancetemplatename")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setWorkflowInstanceTemplateName(@FormParam("workflowInstanceId") String workflowInstanceId,
                                                    @FormParam("templateName") String templateName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.setWorkflowInstanceTemplateName(workflowInstanceId, templateName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @GET
    @Path("get/experimentworkflowinstances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentWorkflowInstances(@QueryParam("experimentId") String experimentId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<WorkflowInstance> experimentWorkflowInstances = airavataRegistry.getExperimentWorkflowInstances(experimentId);
            WorkflowInstancesList workflowInstancesList = new WorkflowInstancesList();
            WorkflowInstance[] workflowInstanceMappings = new WorkflowInstance[experimentWorkflowInstances.size()];
            for (int i = 0; i < experimentWorkflowInstances.size(); i++) {
                workflowInstanceMappings[i] = experimentWorkflowInstances.get(i);
            }
            workflowInstancesList.setWorkflowInstances(workflowInstanceMappings);
            if (experimentWorkflowInstances.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowInstancesList);
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
    @Path("workflowinstance/exist/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceExists(@QueryParam("instanceId") String instanceId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Boolean result = airavataRegistry.isWorkflowInstanceExists(instanceId);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }

    }

    @GET
    @Path("workflowinstance/exist/create")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceExistsThenCreate(@QueryParam("instanceId") String instanceId,
                                                       @QueryParam("createIfNotPresent") boolean createIfNotPresent) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Boolean result = airavataRegistry.isWorkflowInstanceExists(instanceId, createIfNotPresent);
            if (result) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @POST
    @Path("update/workflowinstancestatus/instanceid")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowInstanceStatusByInstance(@FormParam("instanceId") String instanceId,
                                                           @FormParam("executionStatus") String executionStatus) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            WorkflowInstanceStatus.ExecutionStatus status = WorkflowInstanceStatus.ExecutionStatus.valueOf(executionStatus);
            airavataRegistry.updateWorkflowInstanceStatus(instanceId, status);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @POST
    @Path("update/workflowinstancestatus")
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
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        } catch (ParseException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }
    }

    @GET
    @Path("get/workflowinstancestatus")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/workflownodeinput")
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
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
            return builder.build();
        }

    }

    @POST
    @Path("update/workflownodeoutput")
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
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);
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

    @GET
    @Path("get/experiment")
    @Produces(MediaType.APPLICATION_XML)
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experimentId/user")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/experiment/user")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getExperimentByUser(@QueryParam("username") String username) throws RegistryException {
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @POST
    @Path("update/workflownode/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeStatus(@FormParam("workflowInstanceId") String workflowInstanceId,
                                             @FormParam("nodeId") String nodeId,
                                             @FormParam("executionStatus") String executionStatus) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            WorkflowInstanceStatus.ExecutionStatus status = WorkflowInstanceStatus.ExecutionStatus.valueOf(executionStatus);
            airavataRegistry.updateWorkflowNodeStatus(workflowInstanceId, nodeId, status);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }


    @GET
    @Path("get/workflownode/status")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/workflownode/starttime")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("get/workflow/starttime")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("update/workflownode/gramdata")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.updateWorkflowNodeGramData(workflowNodeGramData);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }

    }

    @GET
    @Path("get/workflowinstancedata")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            return builder.build();
        }
    }

    @GET
    @Path("workflowinstance/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowInstanceNodePresent(@QueryParam("workflowInstanceId") String workflowInstanceId,
                                                  @QueryParam("nodeId") String nodeId){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowInstanceNodePresent = airavataRegistry.isWorkflowInstanceNodePresent(workflowInstanceId, nodeId);
            if (workflowInstanceNodePresent){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    @GET
    @Path("workflowinstance/nodeData")
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    @POST
    @Path("add/workflowinstance")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInstance(@FormParam("experimentId") String experimentId,
                                        @FormParam("workflowInstanceId") String workflowInstanceId,
                                        @FormParam("templateName") String templateName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.addWorkflowInstance(experimentId, workflowInstanceId, templateName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @POST
    @Path("update/workflownodetype")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflowNodeType(@FormParam("workflowInstanceId") String workflowInstanceId,
                                           @FormParam("nodeId") String nodeId,
                                           @FormParam("nodeType") String nodeType) throws RegistryException {
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
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    @POST
    @Path("add/workflowinstancenode")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflowInstanceNode(@FormParam("workflowInstanceId") String workflowInstanceId,
                                            @FormParam("nodeId") String nodeId) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.addWorkflowInstanceNode(workflowInstanceId, nodeId);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**---------------------------------User Workflow Registry----------------------------------**/

    @GET
    @Path("workflow/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isWorkflowExists(@QueryParam("workflowName") String workflowName){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowExists = airavataRegistry.isWorkflowExists(workflowName);
            if (workflowExists){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            }else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    @POST
    @Path("add/workflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addWorkflow(@FormParam("workflowName") String workflowName,
                                @FormParam("workflowGraphXml") String workflowGraphXml) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.addWorkflow(workflowName, workflowGraphXml);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (UserWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @POST
    @Path("update/workflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateWorkflow(@FormParam("workflowName") String workflowName,
                                   @FormParam("workflowGraphXml") String workflowGraphXml){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.updateWorkflow(workflowName, workflowGraphXml);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (UserWorkflowAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @GET
    @Path("get/workflowgraph")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWorkflowGraphXML(@QueryParam("workflowName") String workflowName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            String workflowGraphXML = airavataRegistry.getWorkflowGraphXML(workflowName);
            if (workflowGraphXML != null){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowGraphXML);
                return builder.build();
            }else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @GET
    @Path("get/workflows")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getWorkflows()  {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            Map<String, String> workflows = airavataRegistry.getWorkflows();
            WorkflowList workflowList = new WorkflowList();
            List<Workflow> workflowsModels = new ArrayList<Workflow>();
            for (String workflowName : workflows.keySet()){
                Workflow workflow = new Workflow();
                workflow.setWorkflowName(workflowName);
                workflow.setWorkflowGraph(workflows.get(workflowName));
                workflowsModels.add(workflow);
            }
            workflowList.setWorkflowList(workflowsModels);
            if(workflows.size() != 0 ){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(workflowList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                return builder.build();
            }

        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    @GET
    @Path("remove/workflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeWorkflow(@QueryParam("workflowName") String workflowName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.removeWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            return builder.build();
        } catch (UserWorkflowDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**---------------------------------Published Workflow Registry----------------------------------**/

    @GET
    @Path("publishwf/exist")
    @Produces(MediaType.TEXT_PLAIN)
    public Response isPublishedWorkflowExists(@QueryParam("workflowname") String workflowname) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            boolean workflowExists = airavataRegistry.isPublishedWorkflowExists(workflowname);
            if (workflowExists){
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            }else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("False");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @POST
    @Path("publish/workflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response publishWorkflow(@FormParam("workflowName") String workflowName,
                                    @FormParam("publishWorkflowName") String publishWorkflowName)  {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.publishWorkflow(workflowName, publishWorkflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
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

    @POST
    @Path("publish/default/workflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response publishWorkflow(@FormParam("workflowName") String workflowName){
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.publishWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
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

    @GET
    @Path("get/publishworkflowgraph")
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

    @GET
    @Path("get/publishworkflownames")
    @Produces(MediaType.TEXT_PLAIN)
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
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @GET
    @Path("get/publishworkflows")
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
                return builder.build();
            }

        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    @GET
    @Path("remove/publishworkflow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response removePublishedWorkflow(@QueryParam("workflowName") String workflowName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try{
            airavataRegistry.removePublishedWorkflow(workflowName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
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


