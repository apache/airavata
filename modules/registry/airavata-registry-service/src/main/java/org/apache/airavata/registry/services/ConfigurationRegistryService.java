package org.apache.airavata.registry.services;


import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;

public interface ConfigurationRegistryService{
    public Response getConfiguration(String key);
    public Response getConfigurationList(String key);
    public Response setConfiguration(String key, String value, Date expire);
    public Response addConfiguration(String key, String value, Date expire);
    public Response removeAllConfiguration(String key);
    public Response removeConfiguration(String key, String value);

    public Response getGFacURIs();
    public Response getWorkflowInterpreterURIs();
    public Response getEventingServiceURI();
    public Response getMessageBoxURI();

    public Response addGFacURI(URI uri);
    public Response addWorkflowInterpreterURI(URI uri);
    public Response setEventingURI(URI uri);
    public Response setMessageBoxURI(URI uri);

    public Response addGFacURI(URI uri, Date expire);
    public Response addWorkflowInterpreterURI(URI uri, Date expire);
    public Response setEventingURI(URI uri, Date expire);
    public Response setMessageBoxURI(URI uri, Date expire);

    public Response removeGFacURI(URI uri);
    public Response removeAllGFacURI();
    public Response removeWorkflowInterpreterURI(URI uri);
    public Response removeAllWorkflowInterpreterURI();
    public Response unsetEventingURI();
    public Response unsetMessageBoxURI();

}
