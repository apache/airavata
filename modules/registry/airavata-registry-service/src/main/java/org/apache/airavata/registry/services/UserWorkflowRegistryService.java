package org.apache.airavata.registry.services;


import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.UserWorkflowDoesNotExistsException;

import javax.ws.rs.core.Response;

public interface UserWorkflowRegistryService {
    public Response isWorkflowExists(String workflowName) throws RegistryException;
    public Response addWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowAlreadyExistsException, RegistryException;
    public Response updateWorkflow(String workflowName, String workflowGraphXml) throws UserWorkflowDoesNotExistsException, RegistryException;

    public Response getWorkflowGraphXML(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException;
    public Response getWorkflows() throws RegistryException;

    public Response getWorkflowMetadata(String workflowName) throws RegistryException;

    public Response removeWorkflow(String workflowName) throws UserWorkflowDoesNotExistsException, RegistryException;
}
