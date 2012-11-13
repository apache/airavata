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

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.DescriptorDoesNotExistsException;
import org.apache.airavata.registry.api.exception.gateway.MalformedDescriptorException;
import org.apache.airavata.services.registry.rest.resourcemappings.*;
import org.apache.airavata.services.registry.rest.utils.DescriptorUtil;
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
 * This class is the REST interface for all the operation regarding descriptors that are
 * exposed from Airavatas Registry API
 */
@Path(ResourcePathConstants.DecResourcePathConstants.DESC_RESOURCE_PATH)
public class DescriptorRegistryResource {
    private AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    /**
     * ---------------------------------Descriptor Registry----------------------------------*
     */


    /**
     * This method will check whether the host descriptor exists
     * @param hostDescriptorName host descriptor name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_EXISTS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isHostDescriptorExists(@QueryParam("hostDescriptorName") String hostDescriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isHostDescriptorExists(hostDescriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("Host Descriptor exists...");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Host Descriptor does not exist..");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will save the host descriptor
     * @param host JSON message send according to HostDescriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_SAVE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addHostDescriptor(HostDescriptor host) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(host);
            airavataRegistry.addHostDescriptor(hostDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Host descriptor saved successfully...");
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the host descriptor
     * @param host JSON message send according to HostDescriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_UPDATE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateHostDescriptor(HostDescriptor host) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            HostDescription hostDescription = DescriptorUtil.createHostDescription(host);
            airavataRegistry.updateHostDescriptor(hostDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Host descriptor updated successfully...");
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve host descriptor. Clients will get a JSON message that is generated
     * according to HostDescriptor class
     * @param hostName host name
     * @return   HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC)
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
                builder.entity("Host Descriptor does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }

    }

    /**
     * This method will delete the given host descriptor
     * @param hostName host descriptor name
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.DecResourcePathConstants.HOST_DESC_DELETE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeHostDescriptor(@QueryParam("hostName") String hostName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeHostDescriptor(hostName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("Host descriptor deleted successfully...");
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
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
     * This method will retrieve all the host descriptors available.
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS)
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
                builder.entity("No host descriptors available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will return all the host descriptor names available
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.GET_HOST_DESCS_NAMES)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getHostDescriptorNames() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            List<HostDescription> hostDescriptionList = airavataRegistry.getHostDescriptors();
            List<String> hostDescriptorNames = new ArrayList<String>();
            DescriptorNameList descriptorNameList = new DescriptorNameList();
            for (int i = 0; i < hostDescriptionList.size(); i++) {
                hostDescriptorNames.add(hostDescriptionList.get(i).getType().getHostName());
            }
            descriptorNameList.setDescriptorNames(hostDescriptorNames);
            if (hostDescriptionList.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(descriptorNameList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No host descriptors available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will check whether the service descriptor available
     * @param serviceDescriptorName service descriptor name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_EXISTS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isServiceDescriptorExists(@QueryParam("serviceDescriptorName") String serviceDescriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isServiceDescriptorExists(serviceDescriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Service descriptor does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will save the service descriptor
     * @param service this is a JSON message created according to ServiceDescriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_SAVE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response addServiceDescriptor(ServiceDescriptor service) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(service);
            airavataRegistry.addServiceDescriptor(serviceDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Service descriptor saved successfully...");
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the service descriptor
     * @param service this is a JSON message generated according to Service Descriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_UPDATE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateServiceDescriptor(ServiceDescriptor service) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            ServiceDescription serviceDescription = DescriptorUtil.createServiceDescription(service);
            airavataRegistry.updateServiceDescriptor(serviceDescription);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Service descriptor updated successfully...");
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve service descriptor for a given service descriptor name. Clients
     * will get a JSON message that is generated according to Service Descriptor class
     * @param serviceName  service name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC)
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
                builder.entity("No service descriptor available with given service name...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will delete a given service descriptor
     * @param serviceName service descriptor name
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.DecResourcePathConstants.SERVICE_DESC_DELETE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeServiceDescriptor(@QueryParam("serviceName") String serviceName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeServiceDescriptor(serviceName);
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Service descriptor deleted successfully...");
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
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
     * This method will retrieve all the service descriptors
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.GET_SERVICE_DESCS)
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
                builder.entity("No service descriptors available...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }


    /**
     * This method will check whether the given application descriptor exists
     * @param serviceName service descriptor name
     * @param hostName host descriptor name
     * @param appDescriptorName application descriptor name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APPL_DESC_EXIST)
    @Produces(MediaType.TEXT_PLAIN)
    public Response isApplicationDescriptorExists(@QueryParam("serviceName") String serviceName,
                                                  @QueryParam("hostName") String hostName,
                                                  @QueryParam("appDescName") String appDescriptorName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        boolean state;
        try {
            state = airavataRegistry.isApplicationDescriptorExists(serviceName, hostName, appDescriptorName);
            if (state) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity("True");
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("Application descriptor does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will save given application descriptor
     * @param applicationDescriptor this is a JSON message created according to
     * ApplicationDescriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_BUILD_SAVE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addApplicationDescriptor(ApplicationDescriptor applicationDescriptor) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String hostdescName = applicationDescriptor.getHostdescName();
            if (!airavataRegistry.isHostDescriptorExists(hostdescName)) {
                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                builder.entity("Given host does not exist...");
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


            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Application descriptor saved successfully...");
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will update the application descriptor
     * @param applicationDescriptor JSON message of ApplicationDescriptor class
     * @return HTTP response
     */
    @POST
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_UPDATE)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response udpateApplicationDescriptor(ApplicationDescriptor applicationDescriptor) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            String hostdescName = applicationDescriptor.getHostdescName();
            if (!airavataRegistry.isHostDescriptorExists(hostdescName)) {
                Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                builder.entity("Host does not available...");
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
            Response.ResponseBuilder builder = Response.status(Response.Status.OK);
            builder.entity("Application descriptor updated successfully...");
            return builder.build();
        } catch (DescriptorAlreadyExistsException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
            builder.entity(e.getMessage());
            return builder.build();
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve an application descriptor according to given service name, host name
     * and application name
     * @param serviceName  service name
     * @param hostName  host name
     * @param applicationName  application name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_DESCRIPTION)
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
                builder.entity("Application descriptor does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will retrieve application descriptors for a given service and host
     * @param serviceName service name
     * @param hostName host name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_PER_HOST_SERVICE)
    @Produces("text/xml")
    public Response getApplicationDescriptorPerServiceHost(@QueryParam("serviceName") String serviceName,
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
                builder.entity("Application descriptor does not exist...");
                return builder.build();
            }
        } catch (RegistryException e) {
            Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            builder.entity(e.getMessage());
            return builder.build();
        }
    }

    /**
     * This method will get all the application descriptors for a given service
     * @param serviceName service name
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCS_SERVICE)
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
                builder.entity("Application descriptor does not exist...");
                return builder.build();
            }
        } catch (MalformedDescriptorException e) {
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
     * This method will retrieve all the application descriptors available
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_ALL_DESCRIPTORS)
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
                builder.entity("No application descriptors available...");
                return builder.build();
            }
        } catch (MalformedDescriptorException e) {
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
     * This method will return all the application names available
     * @return HTTP response
     */
    @GET
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_NAMES)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getApplicationDescriptorNames() {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            Map<String[], ApplicationDeploymentDescription> applicationDeploymentDescriptionMap = airavataRegistry.getApplicationDescriptors();
            DescriptorNameList descriptorNameList = new DescriptorNameList();
            List<String> appDesNames = new ArrayList<String>();
            for (String[] descriptors : applicationDeploymentDescriptionMap.keySet()) {
                ApplicationDeploymentDescription applicationDeploymentDescription = applicationDeploymentDescriptionMap.get(descriptors);
                appDesNames.add(applicationDeploymentDescription.getType().getApplicationName().getStringValue());
            }
            descriptorNameList.setDescriptorNames(appDesNames);
            if (applicationDeploymentDescriptionMap.size() != 0) {
                Response.ResponseBuilder builder = Response.status(Response.Status.OK);
                builder.entity(descriptorNameList);
                return builder.build();
            } else {
                Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
                builder.entity("No application descriptors available...");
                return builder.build();
            }
        } catch (MalformedDescriptorException e) {
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
     * This method wil remove an application descriptor according to given service name, host name
     * and application name
     * @param serviceName service name
     * @param hostName  host name
     * @param appName application name
     * @return HTTP response
     */
    @DELETE
    @Path(ResourcePathConstants.DecResourcePathConstants.APP_DESC_DELETE)
    @Produces(MediaType.TEXT_PLAIN)
    public Response removeApplicationDescriptor(@QueryParam("serviceName") String serviceName,
                                                @QueryParam("hostName") String hostName,
                                                @QueryParam("appName") String appName) {
        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
        try {
            airavataRegistry.removeApplicationDescriptor(serviceName, hostName, appName);
            Response.ResponseBuilder builder = Response.status(Response.Status.NO_CONTENT);
            builder.entity("Application descriptor deleted successfully...");
            return builder.build();
        } catch (DescriptorDoesNotExistsException e) {
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
