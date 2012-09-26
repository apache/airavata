package org.apache.airavata.registry.services;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.gateway.PublishedWorkflowDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;

import javax.ws.rs.core.Response;

public interface PublishedWorkflowRegistryService {
    public Response isPublishedWorkflowExists(String workflowName) throws RegistryException;
    public Response publishWorkflow(String workflowName, String publishWorkflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegistryException;
    public Response publishWorkflow(String workflowName) throws PublishedWorkflowAlreadyExistsException, UserWorkflowDoesNotExistsException, RegistryException;

    public Response getPublishedWorkflowGraphXML(String workflowName) throws PublishedWorkflowDoesNotExistsException, RegistryException;
    public Response getPublishedWorkflowNames() throws RegistryException;
    public Response getPublishedWorkflows() throws RegistryException;
    public Response getPublishedWorkflowMetadata(String workflowName) throws RegistryException;

    public Response removePublishedWorkflow(String workflowName)throws PublishedWorkflowDoesNotExistsException, RegistryException;
}
