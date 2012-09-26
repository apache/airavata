package org.apache.airavata.registry.services;


import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataExperiment;
import org.apache.airavata.registry.api.exception.worker.ExperimentDoesNotExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectAlreadyExistsException;
import org.apache.airavata.registry.api.exception.worker.WorkspaceProjectDoesNotExistsException;

import javax.ws.rs.core.Response;
import java.util.Date;

public interface ProjectsRegistryService {
    //------------Project management
    public Response isWorkspaceProjectExists(String projectName) throws RegistryException;
    public Response isWorkspaceProjectExists(String projectName, String createIfNotExists) throws RegistryException;
    public Response addWorkspaceProject(String projectName) throws WorkspaceProjectAlreadyExistsException, RegistryException;
    public Response updateWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException;
    public Response deleteWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException;
    public Response getWorkspaceProject(String projectName) throws WorkspaceProjectDoesNotExistsException, RegistryException;
    public Response getWorkspaceProjects() throws RegistryException;

    //------------Experiment management
    public Response addExperiment(String projectName, AiravataExperiment experiment) throws WorkspaceProjectDoesNotExistsException, ExperimentDoesNotExistsException, RegistryException;
    public Response removeExperiment(String experimentId) throws ExperimentDoesNotExistsException;
    public Response getExperiments() throws RegistryException;
    public Response getExperiments(String projectName)throws RegistryException;
    public Response getExperiments(Date from, Date to)throws RegistryException;
    public Response getExperiments(String projectName, Date from, Date to) throws RegistryException;
}
