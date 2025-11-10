/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.registry.api.service.handler;

import java.util.*;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewaygroups.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.parser.Parser;
import org.apache.airavata.model.appcatalog.parser.ParserInput;
import org.apache.airavata.model.appcatalog.parser.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.ParsingTemplate;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserStoragePreference;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.data.movement.*;
import org.apache.airavata.model.data.movement.DMType;
import org.apache.airavata.model.data.replica.DataProductModel;
import org.apache.airavata.model.data.replica.DataReplicaLocationModel;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.process.ProcessWorkflow;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayUsageReportingCommand;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.exception.RegistryServiceException;
import org.apache.airavata.registry.api.registry_apiConstants;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryServerHandler implements RegistryService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServerHandler.class);

    private org.apache.airavata.service.RegistryService registryService = new org.apache.airavata.service.RegistryService();

    // Helper method to convert domain exceptions to Thrift exceptions
    private RegistryServiceException convertToRegistryServiceException(RegistryException e, String context) {
        logger.error(context, e);
        RegistryServiceException exception = new RegistryServiceException();
        exception.setMessage(context + ". More info : " + e.getMessage());
        return exception;
    }

    private RegistryServiceException convertToRegistryServiceException(AppCatalogException e, String context) {
        logger.error(context, e);
        RegistryServiceException exception = new RegistryServiceException();
        exception.setMessage(context + ". More info : " + e.getMessage());
        return exception;
    }

    /**
     * Fetch Apache Registry API version
     */
    @Override
    public String getAPIVersion() throws TException {
        return registry_apiConstants.REGISTRY_API_VERSION;
    }

    /**
     * Verify if User Exists within Airavata.
     *
     * @param gatewayId
     * @param userName
     * @return true/false
     */
    @Override
    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException, TException {
        try {
            return registryService.isUserExists(gatewayId, userName);
        } catch (RegistryException e) {
            logger.error("Error while verifying user", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while verifying user. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get all users in the gateway
     *
     * @param gatewayId The gateway data model.
     * @return users
     * list of usernames of the users in the gateway
     */
    @Override
    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getAllUsersInGateway(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving users", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving users. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Gateway details by providing gatewayId
     *
     * @param gatewayId The gateway Id of the Gateway.
     * @return gateway
     * Gateway obejct.
     */
    @Override
    public Gateway getGateway(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getGateway(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting the gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Gateway
     *
     * @param gatewayId The gateway Id of the Gateway to be deleted.
     * @return boolean
     * Boolean identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteGateway(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteGateway(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while deleting the gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get All the Gateways Connected to Airavata.
     */
    @Override
    public List<Gateway> getAllGateways() throws RegistryServiceException, TException {
        try {
            return registryService.getAllGateways();
        } catch (RegistryException e) {
            logger.error("Error while getting all the gateways", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Check for the Existance of a Gateway within Airavata
     *
     * @param gatewayId Provide the gatewayId of the gateway you want to check the existancy
     * @return gatewayId
     * return the gatewayId of the existing gateway.
     */
    @Override
    public boolean isGatewayExist(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deleteNotification(String gatewayId, String notificationId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteNotification(gatewayId, notificationId);
        } catch (RegistryException e) {
            logger.error("Error while deleting notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while deleting notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public Notification getNotification(String gatewayId, String notificationId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getNotification(gatewayId, notificationId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving notification", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retreiving notification. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<Notification> getAllNotifications(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getAllNotifications(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting all notifications", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while getting all notifications. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get a Project by ID
     * This method is to obtain a project by providing a projectId.
     *
     * @param projectId projectId of the project you require.
     * @return project
     * project data model will be returned.
     */
    @Override
    public Project getProject(String projectId) throws RegistryServiceException, TException {
        try {
            return registryService.getProject(projectId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the project", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Project
     * This method is used to delete an existing Project.
     *
     * @param projectId projectId of the project you want to delete.
     * @return boolean
     * Boolean identifier for the success or failure of the deletion operation.
     * <p>
     * NOTE: This method is not used within gateways connected with Airavata.
     */
    @Override
    public boolean deleteProject(String projectId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteProject(projectId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while removing the project");
        }
    }

    /**
     * Get All User Projects
     * Get all Project for the user with pagination. Results will be ordered based on creation time DESC.
     *
     * @param gatewayId The identifier for the requested gateway.
     * @param userName  The identifier of the user.
     * @param limit     The amount results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUserProjects(gatewayId, userName, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving projects");
        }
    }

    /**
     * Get Experiment Statistics
     * Get Experiment Statisitics for a given gateway for a specific time period. This feature is available only for admins of a particular gateway. Gateway admin access is managed by the user roles.
     *
     * @param gatewayId Unique identifier of the gateway making the request to fetch statistics.
     * @param fromTime  Starting date time.
     * @param toTime    Ending data time.
     */
    @Override
    public ExperimentStatistics getExperimentStatistics(
            String gatewayId,
            long fromTime,
            long toTime,
            String userName,
            String applicationName,
            String resourceHostName,
            List<String> accessibleExpIds,
            int limit,
            int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.getExperimentStatistics(
                    gatewayId, fromTime, toTime, userName, applicationName, resourceHostName,
                    accessibleExpIds, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving experiments");
        }
    }

    /**
     * Get All Experiments of the Project
     * Get Experiments within project with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Unique identifier of the gateway.
     * @param projectId Unique identifier of the project.
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the experiments");
        }
    }

    /**
     * Get All Experiments of the User
     * Get experiments by user with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Identifier of the requesting gateway.
     * @param userName  Username of the requested end user.
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUserExperiments(gatewayId, userName, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the experiments");
        }
    }

    /**
     * Delete an Experiment
     * If the experiment is not already launched experiment can be deleted.
     *
     * @param experimentId@return boolean
     *                            Identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteExperiment(String experimentId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteExperiment(experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while deleting the experiment");
        }
    }

    /**
     * *
     * * Get Experiment
     * * Fetch previously created experiment metadata.
     * *
     * * @param airavataExperimentId
     * *    The unique identifier of the requested experiment. This ID is returned during the create experiment step.
     * *
     * * @return ExperimentModel
     * *   This method will return the previously stored experiment metadata.
     * *
     * * @throws org.apache.airavata.model.error.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     * *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * *
     * * @throws org.apache.airavata.model.error.AiravataClientException
     * *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     * *
     * *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     * *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     * *         gateway registration steps and retry this request.
     * *
     * *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     * *         For now this is a place holder.
     * *
     * *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     * *         is implemented, the authorization will be more substantial.
     * *
     * * @throws org.apache.airavata.model.error.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param airavataExperimentId
     */
    @Override
    public ExperimentModel getExperiment(String airavataExperimentId) throws RegistryServiceException, TException {
        ExperimentModel experimentModel = getExperimentInternal(airavataExperimentId);
        return experimentModel;
    }

    /**
     * Get Complete Experiment Details
     * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
     * tasks -> jobs information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return ExperimentModel
     * This method will return the previously stored experiment metadata including application input parameters, computational resource scheduling
     * information, special input output handling and additional quality of service parameters.
     * @throws InvalidRequestException     For any incorrect forming of the request itself.
     * @throws ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                     <p>
     *                                     UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                     step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                     gateway registration steps and retry this request.
     *                                     <p>
     *                                     AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                     For now this is a place holder.
     *                                     <p>
     *                                     INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                     is implemented, the authorization will be more substantial.
     * @throws AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                     rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getDetailedExperimentTree(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the experiment");
        }
    }

    /**
     * Get Experiment Status
     * <p>
     * Obtain the status of an experiment by providing the Experiment Id
     *
     * @param airavataExperimentId Experiment ID of the experimnet you require the status.
     * @return ExperimentStatus
     * ExperimentStatus model with the current status will be returned.
     */
    @Override
    public ExperimentStatus getExperimentStatus(String airavataExperimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getExperimentStatus(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving experiment status");
        }
    }

    /**
     * Get Experiment Outputs
     * This method to be used when need to obtain final outputs of a certain Experiment
     *
     * @param airavataExperimentId Experiment ID of the experimnet you need the outputs.
     * @return list
     * List of experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
     */
    @Override
    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getExperimentOutputs(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the experiment outputs");
        }
    }

    /**
     * Get Intermediate Experiment Outputs
     * This method to be used when need to obtain intermediate outputs of a certain Experiment
     *
     * @param airavataExperimentId Experiment ID of the experimnet you need intermediate outputs.
     * @return list
     * List of intermediate experiment outputs will be returned. They will be returned as a list of OutputDataObjectType for the experiment.
     */
    @Override
    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getIntermediateOutputs(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving intermediate outputs");
        }
    }

    /**
     * Get Job Statuses for an Experiment
     * This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup
     *
     * @param airavataExperimentId@return JobStatus
     *                                    Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map
     */
    @Override
    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getJobStatuses(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the job statuses");
        }
    }

    @Override
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryServiceException, TException {
        try {
            registryService.addExperimentProcessOutputs(outputType, outputs, id);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding outputs");
        }
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id)
            throws RegistryServiceException, TException {
        try {
            registryService.addErrors(errorType, errorModel, id);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding errors");
        }
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryServiceException, TException {
        try {
            registryService.addTaskStatus(taskStatus, taskId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding task status");
        }
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId)
            throws RegistryServiceException, TException {
        try {
            registryService.addProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding process status");
        }
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId)
            throws RegistryServiceException, TException {
        try {
            registryService.updateProcessStatus(processStatus, processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating process status");
        }
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryServiceException, TException {
        try {
            registryService.updateExperimentStatus(experimentStatus, experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating experiment status");
        }
    }

    @Override
    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId)
            throws RegistryServiceException, TException {
        try {
            registryService.addJobStatus(jobStatus, taskId, jobId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding job status");
        }
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws RegistryServiceException, TException {
        try {
            registryService.addJob(jobModel, processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding job");
        }
    }

    @Override
    public void deleteJobs(String processId) throws RegistryServiceException, TException {
        try {
            registryService.deleteJobs(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while deleting job");
        }
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.addProcess(processModel, experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding process");
        }
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException, TException {
        try {
            registryService.updateProcess(processModel, processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating process");
        }
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws RegistryServiceException, TException {
        try {
            return registryService.addTask(taskModel, processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding task");
        }
    }

    @Override
    public void deleteTasks(String processId) throws RegistryServiceException, TException {
        try {
            registryService.deleteTasks(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while deleting tasks");
        }
    }

    @Override
    public UserConfigurationDataModel getUserConfigurationData(String experimentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUserConfigurationData(experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while getting user configuration");
        }
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcess(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process");
        }
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessList(experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process list");
        }
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessStatus(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process status");
        }
    }

    @Override
    public List<ProcessModel> getProcessListInState(ProcessState processState)
            throws RegistryServiceException, TException {
        try {
            return registryService.getProcessListInState(processState);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process list with given status");
        }
    }

    @Override
    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessStatusList(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process status list for given process Id");
        }
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public boolean isJobExist(String queryType, String id) throws RegistryServiceException, TException {
        try {
            return registryService.isJobExist(queryType, id);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving job");
        }
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public JobModel getJob(String queryType, String id) throws RegistryServiceException, TException {
        try {
            return registryService.getJob(queryType, id);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving job");
        }
    }

    @Override
    public List<JobModel> getJobs(String queryType, String id) throws RegistryServiceException, TException {
        try {
            return registryService.getJobs(queryType, id);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving jobs for query " + queryType + " and id " + id);
        }
    }

    @Override
    public int getJobCount(
            org.apache.airavata.model.status.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException, TException {
        try {
            return registryService.getJobCount(jobStatus, gatewayId, searchBackTimeInMinutes);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while getting job count");
        }
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while getting average time distribution");
        }
    }


    @Override
    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessOutputs(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process outputs");
        }
    }

    @Override
    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessWorkflows(processId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process workflows for process id " + processId);
        }
    }

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryServiceException, TException {
        try {
            registryService.addProcessWorkflow(processWorkflow);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding process workflows for process id " + processWorkflow.getProcessId());
        }
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws RegistryServiceException, TException {
        try {
            return registryService.getProcessIds(experimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving process ids");
        }
    }

    /**
     * Get Job Details for all the jobs within an Experiment.
     * This method to be used when need to get the job details for one or many jobs of an Experiment.
     *
     * @param airavataExperimentId@return list of JobDetails
     *                                    Job details.
     */
    @Override
    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryServiceException, TException {
        try {
            return registryService.getJobDetails(airavataExperimentId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving the job details");
        }
    }

    /**
     * Fetch a Application Module.
     *
     * @param appModuleId The unique identifier of the application module required
     * @return applicationModule
     * Returns an Application Module Object.
     */
    @Override
    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application module");
        }
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @return list
     * Returns the list of all Application Module Objects.
     */
    @Override
    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getAllAppModules(gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving all application modules");
        }
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId        ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppIds App IDs that are accessible to the user
     * @return list
     * Returns the list of all Application Module Objects that are accessible to the user.
     */
    @Override
    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving all application modules");
        }
    }

    /**
     * Delete an Application Module.
     *
     * @param appModuleId The identifier of the Application Module to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationModule(String appModuleId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting the application module");
        }
    }

    /**
     * Fetch a Application Deployment.
     *
     * @param appDeploymentId The identifier for the requested application module
     * @return applicationDeployment
     * Returns a application Deployment Object.
     */
    @Override
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationDeployment(appDeploymentId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployment");
        }
    }

    /**
     * Delete an Application Deployment.
     *
     * @param appDeploymentId The unique identifier of application deployment to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteApplicationDeployment(appDeploymentId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting application deployment");
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @param gatewayId
     * @return list<applicationDeployment.
     * Returns the list of all application Deployment Objects.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllApplicationDeployments(gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployments");
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @param gatewayId                  ID of the gateway which need to list all available application deployment documentation.
     * @param accessibleAppDeploymentIds App IDs that are accessible to the user
     * @return list<applicationDeployment.
     * Returns the list of all application Deployment Objects that are accessible to the user.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAccessibleApplicationDeployments(gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployments");
        }
    }

    /**
     * Fetch all accessible Application Deployment Descriptions for the given Application Module.
     *
     * @param gatewayId                    ID of the gateway which need to list all available application deployment documentation.
     * @param appModuleId                  The given Application Module ID.
     * @param accessibleAppDeploymentIds   Application Deployment IDs which are accessible to the current user.
     * @param accessibleComputeResourceIds Compute Resource IDs which are accessible to the current user.
     * @return list<applicationDeployment>
     * Returns the list of all application Deployment Objects.
     */
    @Override
    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAccessibleApplicationDeploymentsForAppModule(gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployments");
        }
    }

    /**
     * Fetch a list of Deployed Compute Hosts.
     *
     * @param appModuleId The identifier for the requested application module
     * @return list<string>
     * Returns a list of Deployed Resources.
     */
    @Override
    public List<String> getAppModuleDeployedResources(String appModuleId) throws RegistryServiceException, TException {
        try {
            return registryService.getAppModuleDeployedResources(appModuleId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployment");
        }
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationDeployments(appModuleId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application deployment");
        }
    }

    /**
     * Fetch an Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface.
     * @return applicationInterface
     * Returns an application Interface Object.
     */
    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application interface");
        }
    }

    /**
     * Delete an Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting application interface");
        }
    }

    /**
     * Fetch name and ID of  Application Interface documents.
     *
     * @param gatewayId
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing ID's
     */
    @Override
    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllApplicationInterfaceNames(gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application interfaces");
        }
    }

    /**
     * Fetch all Application Interface documents.
     *
     * @param gatewayId
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces documents (Application Interface ID, name, description, Inputs and Outputs objects).
     */
    @Override
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllApplicationInterfaces(gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application interfaces");
        }
    }

    /**
     * Fetch the list of Application Inputs.
     *
     * @param appInterfaceId The identifier of the application interface which need inputs to be fetched.
     * @return list<application_interface_model.InputDataObjectType>
     * Returns a list of application inputs.
     */
    @Override
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationInputs(appInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application inputs");
        }
    }

    /**
     * Fetch list of Application Outputs.
     *
     * @param appInterfaceId The identifier of the application interface which need outputs to be fetched.
     * @return list<application_interface_model.OutputDataObjectType>
     * Returns a list of application outputs.
     */
    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving application outputs");
        }
    }

    /**
     * Fetch a list of all deployed Compute Hosts for a given application interfaces.
     *
     * @param appInterfaceId The identifier for the requested application interface.
     * @return map<computeResourceId, computeResourceName>
     * A map of registered compute resource id's and their corresponding hostnames.
     * Deployments of each modules listed within the interfaces will be listed.
     */
    @Override
    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAvailableAppInterfaceComputeResources(appInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving available compute resources");
        }
    }

    /**
     * Fetch the given Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource
     * @return computeResourceDescription
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getComputeResource(computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving compute resource");
        }
    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException, TException {
        try {
            return registryService.getAllComputeResourceNames();
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving compute resource");
        }
    }

    /**
     * Delete a Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteComputeResource(computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting compute resource");
        }
    }

    /**
     * Fetch the given Storage Resource.
     *
     * @param storageResourceId The identifier for the requested storage resource
     * @return storageResourceDescription
     * Storage Resource Object created from the datamodel..
     */
    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getStorageResource(storageResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving storage resource");
        }
    }

    /**
     * Fetch all registered Storage Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException, TException {
        try {
            return registryService.getAllStorageResourceNames();
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving storage resource");
        }
    }

    /**
     * Delete a Storage Resource.
     *
     * @param storageResourceId The identifier of the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteStorageResource(storageResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting storage resource");
        }
    }

    /**
     * This method returns localJobSubmission object
     *
     * @param jobSubmissionId@return LOCALSubmission instance
     */
    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            return registryService.getLocalJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving local job submission interface");
        }
    }

    /**
     * This method returns SSHJobSubmission object
     *
     * @param jobSubmissionId@return SSHJobSubmission instance
     */
    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryServiceException, TException {
        try {
            return registryService.getSSHJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving SSH job submission interface");
        }
    }

    /**
     * *
     * * This method returns UnicoreJobSubmission object
     * *
     * * @param jobSubmissionInterfaceId
     * *   The identifier of the JobSubmission Interface to be retrieved.
     * *  @return UnicoreJobSubmission instance
     * *
     * *
     *
     * @param jobSubmissionId
     */
    @Override
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUnicoreJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving Unicore job submission interface");
        }
    }

    /**
     * *
     * * This method returns cloudJobSubmission object
     * * @param jobSubmissionInterfaceI
     * *   The identifier of the JobSubmission Interface to be retrieved.
     * *  @return CloudJobSubmission instance
     * *
     *
     * @param jobSubmissionId
     */
    @Override
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getCloudJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving Cloud job submission interface");
        }
    }

    /**
     * This method returns local datamovement object.
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return LOCALDataMovement instance
     */
    @Override
    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            return registryService.getLocalDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving local data movement interface");
        }
    }

    /**
     * This method returns SCP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return SCPDataMovement instance
     */
    @Override
    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryServiceException, TException {
        try {
            return registryService.getSCPDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving SCP data movement interface");
        }
    }

    /**
     * This method returns UNICORE datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return UnicoreDataMovement instance
     */
    @Override
    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUnicoreDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving UNICORE data movement interface");
        }
    }

    /**
     * This method returns GridFTP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return GridFTPDataMovement instance
     */
    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGridFTPDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving GridFTP data movement interface");
        }
    }

    /**
     * Change the priority of a given job submisison interface
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @param newPriorityOrder
     * @return status
     * Returns a success/failure of the change.
     */
    @Override
    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder)
            throws RegistryServiceException, TException {
        return false;
    }

    /**
     * Change the priority of a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @param newPriorityOrder
     * @return status
     * Returns a success/failure of the change.
     */
    @Override
    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder)
            throws RegistryServiceException, TException {
        return false;
    }

    /**
     * Change the priorities of a given set of job submission interfaces
     *
     * @param jobSubmissionPriorityMap A Map of identifiers of the JobSubmission Interfaces and thier associated priorities to be set.
     * @return status
     * Returns a success/failure of the changes.
     */
    @Override
    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap)
            throws RegistryServiceException, TException {
        return false;
    }

    /**
     * Change the priorities of a given set of data movement interfaces
     *
     * @param dataMovementPriorityMap A Map of identifiers of the DataMovement Interfaces and thier associated priorities to be set.
     * @return status
     * Returns a success/failure of the changes.
     */
    @Override
    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap)
            throws RegistryServiceException, TException {
        return false;
    }

    /**
     * Delete a given job submisison interface
     *
     * @param computeResourceId
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting job submission interface");
        }
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving resource job manager");
        }
    }

    @Override
    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException, TException {
        try {
            return registryService.deleteResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting resource job manager");
        }
    }

    /**
     * Delete a Compute Resource Queue
     *
     * @param computeResourceId The identifier of the compute resource which has the queue to be deleted
     * @param queueName         Name of the queue need to be deleted. Name is the uniqueue identifier for the queue within a compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteBatchQueue(String computeResourceId, String queueName)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteBatchQueue(computeResourceId, queueName);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting batch queue");
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource.
     * @return gatewayResourceProfile
     * Gateway Resource Profile Object.
     */
    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGatewayResourceProfile(gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving gateway resource profile");
        }
    }

    /**
     * Delete the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryServiceException, TException {
        try {
            return registryService.deleteGatewayResourceProfile(gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing gateway resource profile");
        }
    }

    /**
     * Fetch a Compute Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier for the gateway profile to be requested
     * @param computeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading gateway compute resource preference");
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Stprage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGatewayStoragePreference(gatewayID, storageId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading gateway data storage preference");
        }
    }

    /**
     * Fetch all Compute Resource Preferences of a registered gateway profile.
     *
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllGatewayComputeResourcePreferences(gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading gateway compute resource preferences");
        }
    }

    /**
     * Fetch all Storage Resource Preferences of a registered gateway profile.
     *
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllGatewayStoragePreferences(gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading gateway data storage preferences");
        }
    }

    /**
     * Fetch all Gateway Profiles registered
     *
     * @return GatewayResourceProfile
     * Returns all the GatewayResourcePrifle list object.
     */
    @Override
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryServiceException, TException {
        try {
            return registryService.getAllGatewayResourceProfiles();
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading retrieving all gateway profiles");
        }
    }

    /**
     * Delete the Compute Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier for the gateway profile to be deleted.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway compute resource preference");
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID The identifier of the gateway profile to be deleted.
     * @param storageId ID of the storage preference you want to delete.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway data storage preference");
        }
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException, TException {
        try {
            return registryService.getDataProduct(productUri);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in retreiving the data product " + productUri);
        }
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws RegistryServiceException, TException {
        try {
            return registryService.getParentDataProduct(productUri);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in retreiving the parent data product for " + productUri);
        }
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryServiceException, TException {
        try {
            return registryService.getChildDataProducts(productUri);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in retreiving the child products for " + productUri);
        }
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in searching the data products for name " + productName);
        }
    }

    @Override
    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile)
            throws RegistryServiceException, TException {
        try {
            return registryService.createGroupResourceProfile(groupResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while creating group resource profile");
        }
    }

    @Override
    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile)
            throws RegistryServiceException, TException {
        try {
            registryService.updateGroupResourceProfile(groupResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating group resource profile");
        }
    }

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group resource profile");
        }
    }

    @Override
    public boolean isGroupResourceProfileExists(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.isGroupResourceProfileExists(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group resource profile");
        }
    }

    @Override
    public boolean removeGroupResourceProfile(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.removeGroupResourceProfile(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing group resource profile");
        }
    }

    @Override
    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group resource list");
        }
    }

    @Override
    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing group compute preference");
        }
    }

    @Override
    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException, TException {
        try {
            return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing group compute resource policy");
        }
    }

    @Override
    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException, TException {
        try {
            return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing group batch queue resource policy");
        }
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryServiceException, TException {
        try {
            return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group compute resource preference");
        }
    }

    @Override
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.isGroupComputeResourcePreferenceExists(computeResourceId, groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group compute resource preference");
        }
    }

    @Override
    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupComputeResourcePolicy(resourcePolicyId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving group compute resource policy");
        }
    }

    @Override
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getBatchQueueResourcePolicy(resourcePolicyId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving Batch Queue resource policy");
        }
    }

    @Override
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving retrieving Group Compute Resource Preference list");
        }
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving retrieving Group Batch Queue Resource policy list");
        }
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving retrieving Group Compute Resource policy list");
        }
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerReplicaLocation(replicaLocationModel);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in retreiving the replica " + replicaLocationModel.getReplicaName());
        }
    }

    /**
     * API Methods related to replica catalog
     *
     * @param dataProductModel
     */
    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryServiceException, TException {
        try {
            return registryService.registerDataProduct(dataProductModel);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error in registering the data resource" + dataProductModel.getProductName());
        }
    }

    /**
     * Update a Storage Resource Preference of a registered gateway profile.
     *
     * @param gatewayID         The identifier of the gateway profile to be updated.
     * @param storageId         The Storage resource identifier of the one that you want to update
     * @param storagePreference The storagePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateGatewayStoragePreference(gatewayID, storageId, storagePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway data storage preference");
        }
    }

    /**
     * Update a Compute Resource Preference to a registered gateway profile.
     *
     * @param gatewayID                 The identifier for the gateway profile to be updated.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param computeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway compute resource preference");
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID             The identifier of the gateway profile to be added.
     * @param storageResourceId     Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addGatewayStoragePreference(
            String gatewayID, String storageResourceId, StoragePreference dataStoragePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering gateway resource profile preference");
        }
    }

    /**
     * Add a Compute Resource Preference to a registered gateway profile.
     *
     * @param gatewayID                 The identifier for the gateway profile to be added.
     * @param computeResourceId         Preferences related to a particular compute resource
     * @param computeResourcePreference The ComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.addGatewayComputeResourcePreference(gatewayID, computeResourceId, computeResourcePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering gateway resource profile preference");
        }
    }

    /**
     * Update a Gateway Resource Profile.
     *
     * @param gatewayID              The identifier for the requested gateway resource to be updated.
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway resource profile");
        }
    }

    /**
     * Register a Gateway Resource Profile.
     *
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     *                               The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
     *                               resource profile.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerGatewayResourceProfile(gatewayResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering gateway resource profile");
        }
    }

    @Override
    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating resource job manager");
        }
    }

    @Override
    public String registerResourceJobManager(ResourceJobManager resourceJobManager)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding resource job manager");
        }
    }

    /**
     * Delete a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @param dmType
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting data movement interface");
        }
    }

    /**
     * Update the given GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param gridFTPDataMovement     The GridFTPDataMovement object to be updated.
     * @return boolean
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement)
            throws RegistryServiceException, TException {
        throw new RegistryServiceException("updateGridFTPDataMovementDetails is not yet implemented");
    }

    /**
     * Add a GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri          The identifier of the compute resource to which dataMovement protocol to be added
     *
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param gridFTPDataMovement The GridFTPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.addGridFTPDataMovementDetails(computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding data movement interface to resource compute resource");
        }
    }

    /**
     * Update a selected UNICORE data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param unicoreDataMovement
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws RegistryServiceException, TException {
        throw new RegistryServiceException("updateUnicoreDataMovementDetails is not yet implemented");
    }

    /**
     * Add a UNICORE data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri          The identifier of the compute resource to which data movement protocol to be added
     *
     * @param dmType
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param unicoreDataMovement
     * @return status
     * Returns the unique data movement id.
     */
    @Override
    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding data movement interface to resource compute resource");
        }
    }

    /**
     * Update the given scp data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param scpDataMovement         The SCPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating SCP data movement");
        }
    }

    /**
     * Add a SCP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri      The identifier of the compute resource to which JobSubmission protocol to be added
     *
     * @param dmType
     * @param priorityOrder   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param scpDataMovement The SCPDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding data movement interface to resource compute resource");
        }
    }

    /**
     * Update the given Local data movement details
     *
     * @param dataMovementInterfaceId The identifier of the data movement Interface to be updated.
     * @param localDataMovement       The LOCALDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating local data movement interface");
        }
    }

    /**
     * Add a Local data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     * <p>
     * productUri        The identifier of the compute resource to which JobSubmission protocol to be added
     *
     * @param dataMoveType
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localDataMovement The LOCALDataMovement object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws RegistryServiceException, TException {
        try {
            return registryService.addLocalDataMovementDetails(resourceId, dataMoveType, priorityOrder, localDataMovement);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding data movement interface to resource");
        }
    }

    /**
     * Update the UNIOCRE Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param unicoreJobSubmission
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException, TException {
        throw new RegistryServiceException("updateUnicoreJobSubmissionDetails is not yet implemented");
    }

    /**
     * Update the cloud Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission sshJobSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating Cloud job submission");
        }
    }

    /**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating SSH job submission");
        }
    }

    /**
     * *
     * * Add a Cloud Job Submission details to a compute resource
     * *  App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     * *
     * * @param computeResourceId
     * *   The identifier of the compute resource to which JobSubmission protocol to be added
     * *
     * * @param priorityOrder
     * *   Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * *
     * * @param sshJobSubmission
     * *   The SSHJobSubmission object to be added to the resource.
     * *
     * * @return status
     * *   Returns the unique job submission id.
     * *
     * *
     *
     * @param computeResourceId
     * @param priorityOrder
     * @param cloudSubmission
     */
    @Override
    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding job submission interface to resource compute resource");
        }
    }

    /**
     * Add a UNICORE Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId    The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder        Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param unicoreJobSubmission The UnicoreJobSubmission object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding job submission interface to resource compute resource");
        }
    }

    /**
     * Add a SSH_FORK Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param sshJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding job submission interface to resource compute resource");
        }
    }

    /**
     * Add a SSH Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param sshJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding job submission interface to resource compute resource");
        }
    }

    /**
     * Update the given Local Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param localSubmission          The LOCALSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating local job submission");
        }
    }

    /**
     * Add a Local Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localSubmission   The LOCALSubmission object to be added to the resource.
     * @return status
     * Returns the unique job submission id.
     */
    @Override
    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws RegistryServiceException, TException {
        try {
            return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding job submission interface to resource compute resource");
        }
    }

    /**
     * Update a Storage Resource.
     *
     * @param storageResourceId          The identifier for the requested compute resource to be updated.
     * @param storageResourceDescription Storage Resource Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateStorageResource(storageResourceId, storageResourceDescription);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating storage resource");
        }
    }

    /**
     * Register a Storage Resource.
     *
     * @param storageResourceDescription Storge Resource Object created from the datamodel.
     * @return storageResourceId
     * Returns a server-side generated airavata storage resource globally unique identifier.
     */
    @Override
    public String registerStorageResource(StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerStorageResource(storageResourceDescription);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while saving storage resource");
        }
    }

    /**
     * Update a Compute Resource.
     *
     * @param computeResourceId          The identifier for the requested compute resource to be updated.
     * @param computeResourceDescription Compute Resource Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateComputeResource(computeResourceId, computeResourceDescription);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating compute resource");
        }
    }

    /**
     * Register a Compute Resource.
     *
     * @param computeResourceDescription Compute Resource Object created from the datamodel.
     * @return computeResourceId
     * Returns a server-side generated airavata compute resource globally unique identifier.
     */
    @Override
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerComputeResource(computeResourceDescription);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while saving compute resource");
        }
    }

    /**
     * Update a Application Interface.
     *
     * @param appInterfaceId       The identifier of the requested application deployment to be updated.
     * @param applicationInterface
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateApplicationInterface(appInterfaceId, applicationInterface);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating application interface");
        }
    }

    /**
     * Register a Application Interface.
     *
     * @param gatewayId
     * @param applicationInterface Application Module Object created from the datamodel.
     * @return appInterfaceId
     * Returns a server-side generated airavata application interface globally unique identifier.
     */
    @Override
    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerApplicationInterface(gatewayId, applicationInterface);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding application interface");
        }
    }

    /**
     * Update an Application Deployment.
     *
     * @param appDeploymentId       The identifier of the requested application deployment to be updated.
     * @param applicationDeployment
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating application deployment");
        }
    }

    /**
     * Register an Application Deployment.
     *
     * @param gatewayId             ID of the gateway which is registering the new Application Deployment.
     * @param applicationDeployment Application Module Object created from the datamodel.
     * @return appDeploymentId
     * Returns a server-side generated airavata appDeployment globally unique identifier.
     */
    @Override
    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding application deployment");
        }
    }

    /**
     * Update a Application Module.
     *
     * @param appModuleId       The identifier for the requested application module to be updated.
     * @param applicationModule Application Module Object created from the datamodel.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateApplicationModule(appModuleId, applicationModule);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating application module");
        }
    }

    /**
     * Register a Application Module.
     *
     * @param gatewayId
     * @param applicationModule Application Module Object created from the datamodel.
     * @return appModuleId
     * Returns the server-side generated airavata appModule globally unique identifier.
     * @gatewayId ID of the gateway which is registering the new Application Module.
     */
    @Override
    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerApplicationModule(gatewayId, applicationModule);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding application module");
        }
    }

    @Override
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryServiceException, TException {
        try {
            registryService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating scheduling info");
        }
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryServiceException, TException {
        try {
            registryService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating user configuration");
        }
    }

    /**
     * Update a Previously Created Experiment
     * Configure the CREATED experiment with required inputs, scheduling and other quality of service parameters. This method only updates the experiment object within the registry.
     * The experiment has to be launched to make it actionable by the server.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @param experiment
     * @return This method call does not have a return value.
     * @throws InvalidRequestException     For any incorrect forming of the request itself.
     * @throws ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *                                     <p>
     *                                     UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *                                     step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *                                     gateway registration steps and retry this request.
     *                                     <p>
     *                                     AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *                                     For now this is a place holder.
     *                                     <p>
     *                                     INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *                                     is implemented, the authorization will be more substantial.
     * @throws AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                     rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment)
            throws RegistryServiceException, TException {
        try {
            registryService.updateExperiment(airavataExperimentId, experiment);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating experiment");
        }
    }

    /**
     * *
     * * Create New Experiment
     * * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * *   but inferred from the sshKeyAuthentication header. This experiment is just a persistent place holder. The client
     * *   has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * *   registering the experiment in a persistent store.
     * *
     * * @param gatewayId
     * *    The unique ID of the gateway where the experiment is been created.
     * *
     * * @param ExperimentModel
     * *    The create experiment will require the basic experiment metadata like the name and description, intended user,
     * *      the gateway identifer and if the experiment should be shared public by defualt. During the creation of an experiment
     * *      the ExperimentMetadata is a required field.
     * *
     * * @return
     * *   The server-side generated.airavata.registry.core.experiment.globally unique identifier.
     * *
     * * @throws org.apache.airavata.model.error.InvalidRequestException
     * *    For any incorrect forming of the request itself.
     * *
     * * @throws org.apache.airavata.model.error.AiravataClientException
     * *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     * *
     * *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     * *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     * *         gateway registration steps and retry this request.
     * *
     * *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     * *         For now this is a place holder.
     * *
     * *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     * *         is implemented, the authorization will be more substantial.
     * *
     * * @throws org.apache.airavata.model.error.AiravataSystemException
     * *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     * *       rather an Airavata Administrator will be notified to take corrective action.
     * *
     * *
     *
     * @param gatewayId
     * @param experiment
     */
    @Override
    public String createExperiment(String gatewayId, ExperimentModel experiment)
            throws RegistryServiceException, TException {
        try {
            return registryService.createExperiment(gatewayId, experiment);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while creating the experiment");
        }
    }

    /**
     * Search Experiments.
     * Search Experiments by using multiple filter criteria with pagination. Results will be sorted based on creation time DESC.
     *
     * @param gatewayId Identifier of the requested gateway.
     * @param userName  Username of the user requesting the search function.
     * @param filters   Map of multiple filter criteria. Currenlt search filters includes Experiment Name, Description, Application, etc....
     * @param limit     Amount of results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     * @return ExperimentSummaryModel
     * List of experiments for the given search filter. Here only the Experiment summary will be returned.
     */
    @Override
    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving experiments");
        }
    }

    /**
     * Search User Projects
     * Search and get all Projects for user by project description or/and project name  with pagination.
     * Results will be ordered based on creation time DESC.
     *
     * @param gatewayId The unique identifier of the gateway making the request.
     * @param userName  The identifier of the user.
     * @param filters   Map of multiple filter criteria. Currenlt search filters includes Project Name and Project Description
     * @param limit     The amount results to be fetched.
     * @param offset    The starting point of the results to be fetched.
     */
    @Override
    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws RegistryServiceException, TException {
        try {
            return registryService.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving projects");
        }
    }

    /**
     * Update an Existing Project
     *
     * @param projectId      The projectId of the project needed an update.
     * @param updatedProject
     * @return void
     * Currently this does not return any value.
     */
    @Override
    public void updateProject(String projectId, Project updatedProject) throws RegistryServiceException, TException {
        try {
            registryService.updateProject(projectId, updatedProject);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating the project");
        }
    }

    /**
     * Creates a Project with basic metadata.
     * A Project is a container of experiments.
     *
     * @param gatewayId The identifier for the requested gateway.
     * @param project
     */
    @Override
    public String createProject(String gatewayId, Project project) throws RegistryServiceException, TException {
        try {
            return registryService.createProject(gatewayId, project);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while creating the project");
        }
    }

    @Override
    public boolean updateNotification(Notification notification) throws RegistryServiceException, TException {
        try {
            return registryService.updateNotification(notification);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating notification");
        }
    }

    /**
     * * API methods to retrieve notifications
     * *
     *
     * @param notification
     */
    @Override
    public String createNotification(Notification notification) throws RegistryServiceException, TException {
        try {
            return registryService.createNotification(notification);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while creating notification");
        }
    }

    /**
     * Update previously registered Gateway metadata.
     *
     * @param gatewayId      The gateway Id of the Gateway which require an update.
     * @param updatedGateway
     * @return gateway
     * Modified gateway obejct.
     * @throws AiravataClientException
     */
    @Override
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryServiceException, TException {
        try {
            return registryService.updateGateway(gatewayId, updatedGateway);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating the gateway");
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway profile");
        }
    }

    /**
     * Register a Gateway with Airavata.
     *
     * @param gateway The gateway data model.
     * @return gatewayId
     * Th unique identifier of the  newly registered gateway.
     */
    @Override
    public String addGateway(Gateway gateway) throws RegistryServiceException, DuplicateEntryException, TException {
        try {
            return registryService.addGateway(gateway);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding gateway");
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while adding gateway profile");
        }
    }

    private boolean validateString(String name) {
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0) {
            valid = false;
        }
        return valid;
    }

    /*Following method wraps the logic of isGatewayExist method and this is to be called by any other method of the API as needed.*/
    private boolean isGatewayExistInternal(String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException,
                    TException {
        try {
            return registryService.isGatewayExist(gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*This private method wraps the logic of getExperiment method as this method is called internally in the API.*/
    private ExperimentModel getExperimentInternal(String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, TException {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the experiment", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*Private method wraps the logic of getExperimentStatus method since this method is called internally.*/
    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException,
                    AiravataSystemException, TException {
        try {
            return registryService.getExperimentStatus(airavataExperimentId);
        } catch (RegistryException e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment status. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*This private method wraps the logic of getApplicationOutputs method as this method is called internally in the API.*/
    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            return registryService.getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application outputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application outputs. More info : " + e.getMessage());
            throw exception;
        }
    }


    /**
     * Register a User Resource Profile.
     *
     * @param userResourceProfile User Resource Profile Object.
     *                            The GatewayID should be obtained from Airavata user profile data model and passed to register a corresponding
     *                            resource profile.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile)
            throws RegistryServiceException, TException {
        try {
            return registryService.registerUserResourceProfile(userResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering user resource profile");
        }
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.isUserResourceProfileExists(userId, gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while checking existence of user resource profile");
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param userId The identifier for the requested user resource.
     * @return UserResourceProfile object
     */
    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUserResourceProfile(userId, gatewayId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving user resource profile");
        }
    }

    /**
     * Update a User Resource Profile.
     *
     * @param gatewayID           The identifier for the requested gateway resource to be updated.
     * @param userResourceProfile Gateway Resource Profile Object.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile userResourceProfile)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating gateway resource profile");
        }
    }

    /**
     * Delete the given User Resource Profile.
     *
     * @param userId    identifier for user profile
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserResourceProfile(String userId, String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteUserResourceProfile(userId, gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while removing User resource profile");
        }
    }

    @Override
    public String addUser(UserProfile userProfile)
            throws RegistryServiceException, DuplicateEntryException, TException {
        try {
            return registryService.addUser(userProfile);
        } catch (RegistryException ex) {
            throw convertToRegistryServiceException(ex, "Error while adding user in registry");
        }
    }

    /**
     * Add a User Compute Resource Preference to a registered gateway profile.
     *
     * @param userId
     * @param gatewayID                     The identifier for the gateway profile to be added.
     * @param computeResourceId             Preferences related to a particular compute resource
     * @param userComputeResourcePreference The UserComputeResourcePreference object to be added to the resource profile.
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.addUserComputeResourcePreference(userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering user resource profile preference");
        }
    }

    /**
     * Is a User Compute Resource Preference exists.
     *
     * @param userId
     * @param gatewayID         The identifier for the gateway profile to be added.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the addition. If a resource already exists, this operation will fail.
     */
    @Override
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.isUserComputeResourcePreferenceExists(userId, gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while fetching compute resource preference");
        }
    }

    /**
     * Add a Storage Resource Preference to a registered gateway profile.
     *
     * @param gatewayID             The identifier of the gateway profile to be added.
     * @param storageResourceId     Preferences related to a particular compute resource
     * @param dataStoragePreference
     * @return status
     * Returns a success/failure of the addition. If a profile already exists, this operation will fail.
     * Instead an update should be used.
     */
    @Override
    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dataStoragePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.addUserStoragePreference(userId, gatewayID, storageResourceId, dataStoragePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while registering user resource profile preference");
        }
    }

    /**
     * Fetch a Compute Resource Preference of a registered gateway profile.
     *
     * @param userId
     * @param gatewayID             The identifier for the gateway profile to be requested
     * @param userComputeResourceId Preferences related to a particular compute resource
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String userComputeResourceId) throws RegistryServiceException, TException {
        try {
            return registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading user compute resource preference");
        }
    }

    /**
     * Fetch a Storage Resource Preference of a registered gateway profile.
     *
     * @param userId    identifier for user data model
     * @param gatewayID The identifier of the gateway profile to request to fetch the particular storage resource preference.
     * @param storageId Identifier of the Storage Preference required to be fetched.
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getUserStoragePreference(userId, gatewayID, storageId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading gateway data storage preference");
        }
    }

    /**
     * Fetch all User Resource Profiles registered
     *
     * @return UserResourceProfile
     * Returns all the UserResourceProfile list object.
     */
    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryServiceException, TException {
        try {
            return registryService.getAllUserResourceProfiles();
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading retrieving all gateway profiles");
        }
    }

    /**
     * Update a Compute Resource Preference to a registered user resource profile.
     *
     * @param userId                        identifier for user data model
     * @param gatewayID                     The identifier for the gateway profile to be updated.
     * @param computeResourceId             Preferences related to a particular compute resource
     * @param userComputeResourcePreference The ComputeResourcePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserComputeResourcePreference(
            String userId,
            String gatewayID,
            String computeResourceId,
            UserComputeResourcePreference userComputeResourcePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateUserComputeResourcePreference(userId, gatewayID, computeResourceId, userComputeResourcePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating user compute resource preference");
        }
    }

    /**
     * Update a Storage Resource Preference of a registered user resource profile.
     *
     * @param userId                identifier for user data model
     * @param gatewayID             The identifier of the gateway profile to be updated.
     * @param storageId             The Storage resource identifier of the one that you want to update
     * @param userStoragePreference The storagePreference object to be updated to the resource profile.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference userStoragePreference)
            throws RegistryServiceException, TException {
        try {
            return registryService.updateUserStoragePreference(userId, gatewayID, storageId, userStoragePreference);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while updating user data storage preference");
        }
    }

    /**
     * Delete the Compute Resource Preference of a registered gateway profile.
     *
     * @param userId            The identifier for user data model
     * @param gatewayID         The identifier for the gateway profile to be deleted.
     * @param computeResourceId Preferences related to a particular compute resource
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteUserComputeResourcePreference(userId, gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting user compute resource preference");
        }
    }

    /**
     * Delete the Storage Resource Preference of a registered gateway profile.
     *
     * @param userId    The identifier for user data model
     * @param gatewayID The identifier of the gateway profile to be deleted.
     * @param storageId ID of the storage preference you want to delete.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId)
            throws RegistryServiceException, TException {
        try {
            return registryService.deleteUserStoragePreference(userId, gatewayID, storageId);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while deleting user storage preference");
        }
    }

    /**
     * * Get queue statuses of all compute resources
     * *
     */
    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryServiceException, TException {
        try {
            return registryService.getLatestQueueStatuses();
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while reading queue status models");
        }
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses)
            throws RegistryServiceException, TException {
        try {
            registryService.registerQueueStatuses(queueStatuses);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while storing queue status models");
        }
    }

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName)
            throws RegistryServiceException, TException {
        try {
            return registryService.getQueueStatus(hostName, queueName);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving queue status");
        }
    }

    /**
     * Fetch all User Compute Resource Preferences of a registered User Resource Profile.
     *
     * @param userId
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return computeResourcePreference
     * Returns the ComputeResourcePreference object.
     */
    @Override
    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading User Resource Profile compute resource preferences");
        }
    }

    /**
     * Fetch all Storage Resource Preferences of a registered User Resource Profile.
     *
     * @param userId
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID)
            throws RegistryServiceException, TException {
        try {
            return registryService.getAllUserStoragePreferences(userId, gatewayID);
        } catch (AppCatalogException e) {
            throw convertToRegistryServiceException(e, "Error while reading user resource Profile data storage preferences");
        }
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups)
            throws RegistryServiceException, DuplicateEntryException, TException {
        try {
            registryService.createGatewayGroups(gatewayGroups);
        } catch (RegistryException e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                throw new DuplicateEntryException(e.getMessage());
            }
            throw convertToRegistryServiceException(e, "Error while creating GatewayGroups");
        }
    }

    @Override
    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryServiceException, TException {
        try {
            registryService.updateGatewayGroups(gatewayGroups);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while updating GatewayGroups");
        }
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.isGatewayGroupsExists(gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error checking existence of GatewayGroups");
        }
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getGatewayGroups(gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving GatewayGroups");
        }
    }

    @Override
    public Parser getParser(String parserId, String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.getParser(parserId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving parser");
        }
    }

    @Override
    public String saveParser(Parser parser) throws RegistryServiceException, TException {
        try {
            return registryService.saveParser(parser);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while saving parser");
        }
    }

    @Override
    public List<Parser> listAllParsers(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.listAllParsers(gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while listing parsers");
        }
    }

    @Override
    public void removeParser(String parserId, String gatewayId) throws RegistryServiceException, TException {
        try {
            registryService.removeParser(parserId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while removing parser");
        }
    }

    @Override
    public ParserInput getParserInput(String parserInputId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getParserInput(parserInputId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving parser input");
        }
    }

    @Override
    public ParserOutput getParserOutput(String parserOutputId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getParserOutput(parserOutputId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving parser output");
        }
    }

    @Override
    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getParsingTemplate(templateId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving parsing template");
        }
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving parsing templates for experiment");
        }
    }

    @Override
    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryServiceException, TException {
        try {
            return registryService.saveParsingTemplate(parsingTemplate);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while saving parsing template");
        }
    }

    @Override
    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryServiceException, TException {
        try {
            return registryService.listAllParsingTemplates(gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while listing parsing templates");
        }
    }

    @Override
    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException, TException {
        try {
            registryService.removeParsingTemplate(templateId, gatewayId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while removing parsing template");
        }
    }

    @Override
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.isGatewayUsageReportingAvailable(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while checking gateway usage reporting availability");
        }
    }

    @Override
    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            return registryService.getGatewayReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while retrieving gateway reporting command");
        }
    }

    @Override
    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command)
            throws RegistryServiceException, TException {
        try {
            registryService.addGatewayUsageReportingCommand(command);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while adding gateway usage reporting command");
        }
    }

    @Override
    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException, TException {
        try {
            registryService.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
        } catch (RegistryException e) {
            throw convertToRegistryServiceException(e, "Error while removing gateway usage reporting command");
        }
    }
}
