package org.apache.airavata.services.registry.rest.resources;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.services.registry.rest.resourcemappings.ApplicationDescriptor;
import org.apache.airavata.services.registry.rest.resourcemappings.ServiceDescriptor;
import org.apache.airavata.services.registry.rest.resourcemappings.ServiceParameters;

@Path("/api/application")
public class ApplicationRegistration {

    protected static AiravataRegistry2 airavataRegistry;

    @Context
    ServletContext context;

    public ApplicationRegistration() {
//        airavataRegistry = (AiravataRegistry2) context.getAttribute(RestServicesConstants.AIRAVATA_REGISTRY);
    }
    // Sample JSON is : {"applicationName":"Testing","cpuCount":"12","maxMemory":"0","maxWallTime":"0","minMemory":"0","nodeCount":"0","processorsPerNode":"0"}
	@POST
    @Path("save")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addServiceDescriptor(ApplicationDescriptor application){
        try{
        	application.getName();
        	Response.ResponseBuilder builder = Response.status(Response.Status.ACCEPTED);
            return builder.build();
        } catch (Exception e) {
        	throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

	@GET
    @Path("get")
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ApplicationDescriptor getServiceDescriptor(String applicationName){
        try{
        	ApplicationDescriptor application = new ApplicationDescriptor();
        	application.setName(applicationName);
        	ServiceDescriptor descriptor = new ServiceDescriptor();

        	ServiceParameters parameters = new ServiceParameters();
        	parameters.setName("myinput");
        	parameters.setDataType("input");
        	parameters.setDescription("my input");
        	parameters.setType("String");

        	ServiceParameters parameters1 = new ServiceParameters();
        	parameters1.setName("myinput");
        	parameters1.setDataType("input");
        	parameters1.setDescription("my input");
        	parameters1.setType("String");

        	List<ServiceParameters> inputlist = new ArrayList<ServiceParameters>();
        	inputlist.add(parameters);
        	inputlist.add(parameters1);

        	ServiceParameters parameters2 = new ServiceParameters();
        	parameters2.setName("myoutput");
        	parameters2.setDataType("output");
        	parameters2.setDescription("my output");
        	parameters2.setType("String");

        	ServiceParameters parameters3 = new ServiceParameters();
        	parameters3.setName("myoutput");
        	parameters3.setDataType("output");
        	parameters3.setDescription("my output");
        	parameters3.setType("String");

        	List<ServiceParameters> outputlist = new ArrayList<ServiceParameters>();
        	outputlist.add(parameters2);
        	outputlist.add(parameters3);

        	descriptor.setInputParams(inputlist);
        	descriptor.setOutputParams(outputlist);

        	application.setName("service1");
        	application.setHostdescName("localhost");
            return application;
        } catch (Exception e) {
        	throw new WebApplicationException(e,Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


}
