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

package org.apache.airavata.api.server.handler;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavataAPIConstants;
import org.apache.airavata.api.error.*;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService.Client;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiravataServerHandler implements Airavata.Iface {

    private Registry registry;
	private OrchestratorService.Client orchestratorClient;
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
	 //FIXME: these go in a configuration file or a "constants" class. 
    public static final String ORCHESTRATOR_SERVER_HOST = "localhost";
	 //FIXME: these go in a configuration file or a "constants" class. 
    public static final int ORCHESTRATOR_SERVER_PORT = 8940;
    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String GetAPIVersion() throws TException {
        return airavataAPIConstants.AIRAVATA_API_VERSION;
    }

    /**
     * Create a Project
     *
     * @param project
     * @param userName
     */
    @Override
    public String createProject(Project project, String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            project.setOwner(userName);
            return (String)registry.add(ParentDataType.PROJECT, project);
        } catch (RegistryException e) {
            logger.error("Error while creating the project", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Update a Project
     *
     * @param project
     */
    @Override
    public void updateProject(Project project) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            registry.update(RegistryModelType.PROJECT, project, project.getProjectID());
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Get a Project by ID
     *
     * @param projectId
     */
    @Override
    public Project getProject(String projectId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            return (Project)registry.get(RegistryModelType.PROJECT, projectId);
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Get all Project by user
     *
     * @param userName
     */
    @Override
    public List<Project> getAllUserProjects(String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        List<Project> projects = new ArrayList<Project>();
        try {
            registry = RegistryFactory.getDefaultRegistry();
            List<Object> list = registry.get(RegistryModelType.PROJECT, Constants.FieldConstants.ProjectConstants.OWNER, userName);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    projects.add((Project) o);
                }
            }
            return projects;
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Get all Experiments within a Project
     *
     * @param projectId
     */
    @Override
    public List<Experiment> getAllExperimentsInProject(String projectId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            List<Experiment> experiments = new ArrayList<Experiment>();
            registry = RegistryFactory.getDefaultRegistry();
            if (registry.isExist(RegistryModelType.PROJECT, projectId)){
	            List<Object> list = registry.get(RegistryModelType.EXPERIMENT, Constants.FieldConstants.ExperimentConstants.PROJECT_ID, projectId);
	            if (list != null && !list.isEmpty()){
	                for (Object o : list){
	                    experiments.add((Experiment)o);
	                }
	            }
            }
            return experiments;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Get all Experiments by user
     *
     * @param userName
     */
    @Override
    public List<Experiment> getAllUserExperiments(String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            List<Experiment> experiments = new ArrayList<Experiment>();
            registry = RegistryFactory.getDefaultRegistry();
            List<Object> list = registry.get(RegistryModelType.EXPERIMENT, Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    experiments.add((Experiment)o);
                }
            }
            return experiments;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * but inferred from the authentication header. This experiment is just a persistent place holder. The client
     * has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * registering the experiment in a persistent store.
     *
     * @param experiment@return The server-side generated airavata experiment globally unique identifier.
     * @throws org.apache.airavata.api.error.InvalidRequestException For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.AiravataClientException The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                               <p/>
     *                                                               UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                               step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                               gateway registration steps and retry this request.
     *                                                               <p/>
     *                                                               AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                               For now this is a place holder.
     *                                                               <p/>
     *                                                               INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                               is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                               rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public String createExperiment(Experiment experiment) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            return (String)registry.add(ParentDataType.EXPERIMENT, experiment);
        } catch (Exception e) {
            logger.error("Error while creating the experiment", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Fetch previously created experiment metadata.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public Experiment getExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            return (Experiment)registry.get(RegistryModelType.EXPERIMENT, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }


    /**
     * Configure a previously created experiment with required inputs, scheduling and other quality of service
     * parameters. This method only updates the experiment object within the registry. The experiment has to be launched
     * to make it actionable by the server.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experiment
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void updateExperiment(String airavataExperimentId, Experiment experiment) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            registry.update(RegistryModelType.EXPERIMENT, experiment, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while updating experiment", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationData userConfiguration) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            registry.add(ChildDataType.EXPERIMENT_CONFIGURATION_DATA, userConfiguration, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while updating user configuration", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public void updateResourceScheduleing(String airavataExperimentId, ComputationalResourceScheduling resourceScheduling) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            registry.add(ChildDataType.COMPUTATIONAL_RESOURCE_SCHEDULING, resourceScheduling, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while updating scheduling info", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * *
     * * Validate experiment configuration. A true in general indicates, the experiment is ready to be launched.
     * *
     * * @param experimentID
     * * @return sucess/failure
     * *
     * *
     *
     * @param airavataExperimentId
     */
    @Override
    public boolean validateExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {

        if (getOrchestratorClient().validateExperiment(airavataExperimentId)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fetch the previously configured experiment configuration information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method returns the previously configured experiment configuration data.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            return (ExperimentStatus)registry.get(RegistryModelType.EXPERIMENT_STATUS, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment status", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    @Override
    public List<DataObjectType> getExperimentOutputs(String airavataExperimentId) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            return (List<DataObjectType>)registry.get(RegistryModelType.EXPERIMENT_OUTPUT, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment outputs", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws TException {
        Map<String, JobStatus> jobStatus = new HashMap<String, JobStatus>();
        try {
            registry = RegistryFactory.getDefaultRegistry();
            List<Object> workflowNodes = registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, Constants.FieldConstants.WorkflowNodeConstants.EXPERIMENT_ID, airavataExperimentId);
            if (workflowNodes != null && !workflowNodes.isEmpty()){
                for (Object wf : workflowNodes){
                    String nodeInstanceId = ((WorkflowNodeDetails) wf).getNodeInstanceId();
                    List<Object> taskDetails = registry.get(RegistryModelType.TASK_DETAIL, Constants.FieldConstants.TaskDetailConstants.NODE_ID, nodeInstanceId);
                    if (taskDetails != null && !taskDetails.isEmpty()){
                        for (Object ts : taskDetails){
                            String taskID = ((TaskDetails) ts).getTaskID();
                            List<Object> jobDetails = registry.get(RegistryModelType.JOB_DETAIL, Constants.FieldConstants.JobDetaisConstants.TASK_ID, taskID);
                            if (jobDetails != null && !jobDetails.isEmpty()){
                                for (Object job : jobDetails){
                                    String jobID = ((JobDetails) job).getJobID();
                                    jobStatus.put(jobID, ((JobDetails) job).getJobStatus());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the job statuses", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        return jobStatus;
    }

    /**
     * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
     * notifications and intermediate and output data will be subsequently available for this experiment.
     *
     * @param airavataExperimentId   The identifier for the requested experiment. This is returned during the create experiment step.
     * @param airavataCredStoreToken :
     *                               A requirement to execute experiments within Airavata is to first register the targeted remote computational account
     *                               credentials with Airavata Credential Store. The administrative API (related to credential store) will return a
     *                               generated token associated with the registered credentials. The client has to security posses this token id and is
     *                               required to pass it to Airavata Server for all execution requests.
     *                               Note: At this point only the credential store token is required so the string is directly passed here. In future if
     *                               if more security credentials are enables, then the structure ExecutionSecurityParameters should be used.
     *                               Note: This parameter is not persisted within Airavata Registry for security reasons.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException
     *          For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException
     *          If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException
     *          The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *          <p/>
     *          UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *          step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *          gateway registration steps and retry this request.
     *          <p/>
     *          AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *          For now this is a place holder.
     *          <p/>
     *          INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *          is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException
     *          This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *          rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void launchExperiment(String airavataExperimentId, String airavataCredStoreToken) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        final OrchestratorService.Client orchestratorClient = getOrchestratorClient();
        final String expID = airavataExperimentId;
        (new Thread(){
            public void run(){
                try {
                    if (orchestratorClient.validateExperiment(expID)) {
                        orchestratorClient.launchExperiment(expID);
                    } else {
                        throw new InvalidRequestException("Experiment Validation Failed, please check the configuration");
                    }
                } catch (TException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();
    }

	private OrchestratorService.Client getOrchestratorClient() {
		if(orchestratorClient == null){
            orchestratorClient = OrchestratorClientFactory.createOrchestratorClient(ORCHESTRATOR_SERVER_HOST, ORCHESTRATOR_SERVER_PORT);
        }
		return orchestratorClient;
	}

    /**
     * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
     * The client has to subsequently update this configuration if needed and launch the cloned experiment.
     *
     * @param airavataExperimentIdToBeCloned This is the experiment identifier that is to be cloned.
     * @param updatedExperiment
     * @return The server-side generated airavata experiment globally unique identifier for the newly cloned experiment.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public String cloneExperiment(String airavataExperimentIdToBeCloned, Experiment updatedExperiment) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            UserConfigurationData previousConfiguration = (UserConfigurationData)registry.get(RegistryModelType.EXPERIMENT_CONFIGURATION_DATA, updatedExperiment.getExperimentID());
            updatedExperiment.setUserConfigurationData(previousConfiguration);
            updatedExperiment.setName(airavataExperimentIdToBeCloned);
            return (String)registry.add(ParentDataType.EXPERIMENT, updatedExperiment);
        } catch (Exception e) {
            logger.error("Error while cloning the experiment with existing configuration...", e);
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
    }

    /**
     * Terminate a running experiment.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.api.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.api.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.api.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                                                   <p/>
     *                                                                   UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                                                   step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                                                   gateway registration steps and retry this request.
     *                                                                   <p/>
     *                                                                   AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                                                   For now this is a place holder.
     *                                                                   <p/>
     *                                                                   INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                                                   is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.api.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void terminateExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
    	Client client = getOrchestratorClient();
    	client.terminateExperiment(airavataExperimentId);
    }

}
