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
package org.apache.airavata.api.thrift.handler;

import java.util.*;
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
import org.apache.airavata.model.error.AiravataSystemException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegistryServiceHandler implements RegistryService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServiceHandler.class);

    private final org.apache.airavata.service.RegistryService registryService;

    public RegistryServiceHandler(org.apache.airavata.service.RegistryService registryService) {
        this.registryService = registryService;
    }

    // Helper method to convert domain exceptions to Thrift exceptions
    private RegistryServiceException convertToRegistryServiceException(Throwable e, String context) {
        logger.error(context, e);
        RegistryServiceException exception = new RegistryServiceException();
        exception.setMessage(context + ". More info : " + e.getMessage());
        return exception;
    }

    /**
     * Fetch Apache Registry API version
     */
    @Override
    public String getAPIVersion() throws AiravataSystemException {
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
    public boolean isUserExists(String gatewayId, String userName) throws RegistryServiceException {
        return registryService.isUserExists(gatewayId, userName);
    }

    /**
     * Get all users in the gateway
     *
     * @param gatewayId The gateway data model.
     * @return users
     * list of usernames of the users in the gateway
     */
    @Override
    public List<String> getAllUsersInGateway(String gatewayId) throws RegistryServiceException {
        return registryService.getAllUsersInGateway(gatewayId);
    }

    /**
     * Get Gateway details by providing gatewayId
     *
     * @param gatewayId The gateway Id of the Gateway.
     * @return gateway
     * Gateway obejct.
     */
    @Override
    public Gateway getGateway(String gatewayId) throws RegistryServiceException {
        return registryService.getGateway(gatewayId);
    }

    /**
     * Delete a Gateway
     *
     * @param gatewayId The gateway Id of the Gateway to be deleted.
     * @return boolean
     * Boolean identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteGateway(String gatewayId) throws RegistryServiceException {
        return registryService.deleteGateway(gatewayId);
    }

    /**
     * Get All the Gateways Connected to Airavata.
     */
    @Override
    public List<Gateway> getAllGateways() throws RegistryServiceException {
        return registryService.getAllGateways();
    }

    /**
     * Check for the Existance of a Gateway within Airavata
     *
     * @param gatewayId Provide the gatewayId of the gateway you want to check the existancy
     * @return gatewayId
     * return the gatewayId of the existing gateway.
     */
    @Override
    public boolean isGatewayExist(String gatewayId) throws RegistryServiceException {
        return registryService.isGatewayExist(gatewayId);
    }

    @Override
    public boolean deleteNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        return registryService.deleteNotification(gatewayId, notificationId);
    }

    @Override
    public Notification getNotification(String gatewayId, String notificationId) throws RegistryServiceException {
        return registryService.getNotification(gatewayId, notificationId);
    }

    @Override
    public List<Notification> getAllNotifications(String gatewayId) throws RegistryServiceException {
        return registryService.getAllNotifications(gatewayId);
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
    public Project getProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        return registryService.getProject(projectId);
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
    public boolean deleteProject(String projectId) throws RegistryServiceException, ProjectNotFoundException {
        return registryService.deleteProject(projectId);
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
            throws RegistryServiceException {
        return registryService.getUserProjects(gatewayId, userName, limit, offset);
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
            throws RegistryServiceException {
        return registryService.getExperimentStatistics(
                gatewayId,
                fromTime,
                toTime,
                userName,
                applicationName,
                resourceHostName,
                accessibleExpIds,
                limit,
                offset);
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
            throws RegistryServiceException {
        return registryService.getExperimentsInProject(gatewayId, projectId, limit, offset);
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
            throws RegistryServiceException {
        return registryService.getUserExperiments(gatewayId, userName, limit, offset);
    }

    /**
     * Delete an Experiment
     * If the experiment is not already launched experiment can be deleted.
     *
     * @param experimentId@return boolean
     *                            Identifier for the success or failure of the deletion operation.
     */
    @Override
    public boolean deleteExperiment(String experimentId) throws RegistryServiceException {
        return registryService.deleteExperiment(experimentId);
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
    public ExperimentModel getExperiment(String airavataExperimentId)
            throws RegistryServiceException, ExperimentNotFoundException {
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
    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws RegistryServiceException {
        return registryService.getDetailedExperimentTree(airavataExperimentId);
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
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws RegistryServiceException {
        return registryService.getExperimentStatus(airavataExperimentId);
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
            throws RegistryServiceException {
        return registryService.getExperimentOutputs(airavataExperimentId);
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
            throws RegistryServiceException {
        return registryService.getIntermediateOutputs(airavataExperimentId);
    }

    /**
     * Get Job Statuses for an Experiment
     * This method to be used when need to get the job status of an Experiment. An experiment may have one or many jobs; there for one or many job statuses may turnup
     *
     * @param airavataExperimentId@return JobStatus
     *                                    Job status (string) for all all the existing jobs for the experiment will be returned in the form of a map
     */
    @Override
    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws RegistryServiceException {
        return registryService.getJobStatuses(airavataExperimentId);
    }

    @Override
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws RegistryServiceException {
        registryService.addExperimentProcessOutputs(outputType, outputs, id);
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id) throws RegistryServiceException {
        registryService.addErrors(errorType, errorModel, id);
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws RegistryServiceException {
        registryService.addTaskStatus(taskStatus, taskId);
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        registryService.addProcessStatus(processStatus, processId);
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws RegistryServiceException {
        registryService.updateProcessStatus(processStatus, processId);
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId)
            throws RegistryServiceException {
        registryService.updateExperimentStatus(experimentStatus, experimentId);
    }

    @Override
    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws RegistryServiceException {
        registryService.addJobStatus(jobStatus, taskId, jobId);
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws RegistryServiceException {
        registryService.addJob(jobModel, processId);
    }

    @Override
    public void deleteJobs(String processId) throws RegistryServiceException {
        registryService.deleteJobs(processId);
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId) throws RegistryServiceException {
        return registryService.addProcess(processModel, experimentId);
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws RegistryServiceException {
        registryService.updateProcess(processModel, processId);
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws RegistryServiceException {
        return registryService.addTask(taskModel, processId);
    }

    @Override
    public void deleteTasks(String processId) throws RegistryServiceException {
        registryService.deleteTasks(processId);
    }

    @Override
    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryServiceException {
        return registryService.getUserConfigurationData(experimentId);
    }

    @Override
    public ProcessModel getProcess(String processId) throws RegistryServiceException {
        return registryService.getProcess(processId);
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws RegistryServiceException {
        return registryService.getProcessList(experimentId);
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws RegistryServiceException {
        return registryService.getProcessStatus(processId);
    }

    @Override
    public List<ProcessModel> getProcessListInState(ProcessState processState) throws RegistryServiceException {
        return registryService.getProcessListInState(processState);
    }

    @Override
    public List<ProcessStatus> getProcessStatusList(String processId) throws RegistryServiceException {
        return registryService.getProcessStatusList(processId);
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public boolean isJobExist(String queryType, String id) throws RegistryServiceException {
        return registryService.isJobExist(queryType, id);
    }

    /**
     * queryType can be PROCESS_ID or TASK_ID
     */
    @Override
    public JobModel getJob(String queryType, String id) throws RegistryServiceException {
        return registryService.getJob(queryType, id);
    }

    @Override
    public List<JobModel> getJobs(String queryType, String id) throws RegistryServiceException {
        return registryService.getJobs(queryType, id);
    }

    @Override
    public int getJobCount(
            org.apache.airavata.model.status.JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        return registryService.getJobCount(jobStatus, gatewayId, searchBackTimeInMinutes);
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws RegistryServiceException {
        return registryService.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    @Override
    public List<OutputDataObjectType> getProcessOutputs(String processId) throws RegistryServiceException {
        return registryService.getProcessOutputs(processId);
    }

    @Override
    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws RegistryServiceException {
        return registryService.getProcessWorkflows(processId);
    }

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws RegistryServiceException {
        registryService.addProcessWorkflow(processWorkflow);
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws RegistryServiceException {
        return registryService.getProcessIds(experimentId);
    }

    /**
     * Get Job Details for all the jobs within an Experiment.
     * This method to be used when need to get the job details for one or many jobs of an Experiment.
     *
     * @param airavataExperimentId@return list of JobDetails
     *                                    Job details.
     */
    @Override
    public List<JobModel> getJobDetails(String airavataExperimentId) throws RegistryServiceException {
        return registryService.getJobDetails(airavataExperimentId);
    }

    /**
     * Fetch a Application Module.
     *
     * @param appModuleId The unique identifier of the application module required
     * @return applicationModule
     * Returns an Application Module Object.
     */
    @Override
    public ApplicationModule getApplicationModule(String appModuleId) throws RegistryServiceException {
        return registryService.getApplicationModule(appModuleId);
    }

    /**
     * Fetch all Application Module Descriptions.
     *
     * @param gatewayId ID of the gateway which need to list all available application deployment documentation.
     * @return list
     * Returns the list of all Application Module Objects.
     */
    @Override
    public List<ApplicationModule> getAllAppModules(String gatewayId) throws RegistryServiceException {
        return registryService.getAllAppModules(gatewayId);
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
            throws RegistryServiceException {
        return registryService.getAccessibleAppModules(gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    /**
     * Delete an Application Module.
     *
     * @param appModuleId The identifier of the Application Module to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationModule(String appModuleId) throws RegistryServiceException {
        return registryService.deleteApplicationModule(appModuleId);
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
            throws RegistryServiceException {
        return registryService.getApplicationDeployment(appDeploymentId);
    }

    /**
     * Delete an Application Deployment.
     *
     * @param appDeploymentId The unique identifier of application deployment to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationDeployment(String appDeploymentId) throws RegistryServiceException {
        return registryService.deleteApplicationDeployment(appDeploymentId);
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
            throws RegistryServiceException {
        return registryService.getAllApplicationDeployments(gatewayId);
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
            throws RegistryServiceException {
        return registryService.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
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
            throws RegistryServiceException {
        return registryService.getAccessibleApplicationDeploymentsForAppModule(
                gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    /**
     * Fetch a list of Deployed Compute Hosts.
     *
     * @param appModuleId The identifier for the requested application module
     * @return list<string>
     * Returns a list of Deployed Resources.
     */
    @Override
    public List<String> getAppModuleDeployedResources(String appModuleId) throws RegistryServiceException {
        return registryService.getAppModuleDeployedResources(appModuleId);
    }

    @Override
    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId)
            throws RegistryServiceException {
        return registryService.getApplicationDeployments(appModuleId);
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
            throws RegistryServiceException {
        return registryService.getApplicationInterface(appInterfaceId);
    }

    /**
     * Delete an Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteApplicationInterface(String appInterfaceId) throws RegistryServiceException {
        return registryService.deleteApplicationInterface(appInterfaceId);
    }

    /**
     * Fetch name and ID of  Application Interface documents.
     *
     * @param gatewayId
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing ID's
     */
    @Override
    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws RegistryServiceException {
        return registryService.getAllApplicationInterfaceNames(gatewayId);
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
            throws RegistryServiceException {
        return registryService.getAllApplicationInterfaces(gatewayId);
    }

    /**
     * Fetch the list of Application Inputs.
     *
     * @param appInterfaceId The identifier of the application interface which need inputs to be fetched.
     * @return list<application_interface_model.InputDataObjectType>
     * Returns a list of application inputs.
     */
    @Override
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws RegistryServiceException {
        return registryService.getApplicationInputs(appInterfaceId);
    }

    /**
     * Fetch list of Application Outputs.
     *
     * @param appInterfaceId The identifier of the application interface which need outputs to be fetched.
     * @return list<application_interface_model.OutputDataObjectType>
     * Returns a list of application outputs.
     */
    @Override
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws RegistryServiceException {
        return registryService.getApplicationOutputs(appInterfaceId);
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
            throws RegistryServiceException {
        return registryService.getAvailableAppInterfaceComputeResources(appInterfaceId);
    }

    /**
     * Fetch the given Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource
     * @return computeResourceDescription
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws RegistryServiceException {
        return registryService.getComputeResource(computeResourceId);
    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllComputeResourceNames() throws RegistryServiceException {
        return registryService.getAllComputeResourceNames();
    }

    /**
     * Delete a Compute Resource.
     *
     * @param computeResourceId The identifier for the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteComputeResource(String computeResourceId) throws RegistryServiceException {
        return registryService.deleteComputeResource(computeResourceId);
    }

    /**
     * Fetch the given Storage Resource.
     *
     * @param storageResourceId The identifier for the requested storage resource
     * @return storageResourceDescription
     * Storage Resource Object created from the datamodel..
     */
    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId) throws RegistryServiceException {
        return registryService.getStorageResource(storageResourceId);
    }

    /**
     * Fetch all registered Storage Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    public Map<String, String> getAllStorageResourceNames() throws RegistryServiceException {
        return registryService.getAllStorageResourceNames();
    }

    /**
     * Delete a Storage Resource.
     *
     * @param storageResourceId The identifier of the requested compute resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteStorageResource(String storageResourceId) throws RegistryServiceException {
        return registryService.deleteStorageResource(storageResourceId);
    }

    /**
     * This method returns localJobSubmission object
     *
     * @param jobSubmissionId@return LOCALSubmission instance
     */
    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        return registryService.getLocalJobSubmission(jobSubmissionId);
    }

    /**
     * This method returns SSHJobSubmission object
     *
     * @param jobSubmissionId@return SSHJobSubmission instance
     */
    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        return registryService.getSSHJobSubmission(jobSubmissionId);
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
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        return registryService.getUnicoreJobSubmission(jobSubmissionId);
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
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws RegistryServiceException {
        return registryService.getCloudJobSubmission(jobSubmissionId);
    }

    /**
     * This method returns local datamovement object.
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return LOCALDataMovement instance
     */
    @Override
    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws RegistryServiceException {
        return registryService.getLocalDataMovement(dataMovementId);
    }

    /**
     * This method returns SCP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return SCPDataMovement instance
     */
    @Override
    public SCPDataMovement getSCPDataMovement(String dataMovementId) throws RegistryServiceException {
        return registryService.getSCPDataMovement(dataMovementId);
    }

    /**
     * This method returns UNICORE datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return UnicoreDataMovement instance
     */
    @Override
    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws RegistryServiceException {
        return registryService.getUnicoreDataMovement(dataMovementId);
    }

    /**
     * This method returns GridFTP datamovement object
     *
     * @param dataMovementId The identifier of the datamovement Interface to be retrieved.
     * @return GridFTPDataMovement instance
     */
    @Override
    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws RegistryServiceException {
        return registryService.getGridFTPDataMovement(dataMovementId);
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
            throws RegistryServiceException {
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
            throws RegistryServiceException {
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
            throws RegistryServiceException {
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
            throws RegistryServiceException {
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
            throws RegistryServiceException {
        return registryService.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }

    @Override
    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        return registryService.getResourceJobManager(resourceJobManagerId);
    }

    @Override
    public boolean deleteResourceJobManager(String resourceJobManagerId) throws RegistryServiceException {
        return registryService.deleteResourceJobManager(resourceJobManagerId);
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
    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws RegistryServiceException {
        return registryService.deleteBatchQueue(computeResourceId, queueName);
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource.
     * @return gatewayResourceProfile
     * Gateway Resource Profile Object.
     */
    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        return registryService.getGatewayResourceProfile(gatewayID);
    }

    /**
     * Delete the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean deleteGatewayResourceProfile(String gatewayID) throws RegistryServiceException {
        return registryService.deleteGatewayResourceProfile(gatewayID);
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
            throws RegistryServiceException {
        return registryService.getGatewayComputeResourcePreference(gatewayID, computeResourceId);
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
            throws RegistryServiceException {
        return registryService.getGatewayStoragePreference(gatewayID, storageId);
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
            throws RegistryServiceException {
        return registryService.getAllGatewayComputeResourcePreferences(gatewayID);
    }

    /**
     * Fetch all Storage Resource Preferences of a registered gateway profile.
     *
     * @param gatewayID The identifier for the gateway profile to be requested
     * @return StoragePreference
     * Returns the StoragePreference object.
     */
    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayID) throws RegistryServiceException {
        return registryService.getAllGatewayStoragePreferences(gatewayID);
    }

    /**
     * Fetch all Gateway Profiles registered
     *
     * @return GatewayResourceProfile
     * Returns all the GatewayResourcePrifle list object.
     */
    @Override
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws RegistryServiceException {
        return registryService.getAllGatewayResourceProfiles();
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
            throws RegistryServiceException {
        return registryService.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
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
    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws RegistryServiceException {
        return registryService.deleteGatewayStoragePreference(gatewayID, storageId);
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws RegistryServiceException {
        return registryService.getDataProduct(productUri);
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws RegistryServiceException {
        return registryService.getParentDataProduct(productUri);
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws RegistryServiceException {
        return registryService.getChildDataProducts(productUri);
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset)
            throws RegistryServiceException {
        return registryService.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
    }

    @Override
    public String createGroupResourceProfile(GroupResourceProfile groupResourceProfile)
            throws RegistryServiceException {
        return registryService.createGroupResourceProfile(groupResourceProfile);
    }

    @Override
    public void updateGroupResourceProfile(GroupResourceProfile groupResourceProfile) throws RegistryServiceException {
        registryService.updateGroupResourceProfile(groupResourceProfile);
    }

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        return registryService.getGroupResourceProfile(groupResourceProfileId);
    }

    @Override
    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws RegistryServiceException {
        return registryService.isGroupResourceProfileExists(groupResourceProfileId);
    }

    @Override
    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws RegistryServiceException {
        return registryService.removeGroupResourceProfile(groupResourceProfileId);
    }

    @Override
    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws RegistryServiceException {
        return registryService.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
    }

    @Override
    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        return registryService.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
    }

    @Override
    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        return registryService.removeGroupComputeResourcePolicy(resourcePolicyId);
    }

    @Override
    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws RegistryServiceException {
        return registryService.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws RegistryServiceException {
        return registryService.getGroupComputeResourcePreference(computeResourceId, groupResourceProfileId);
    }

    @Override
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws RegistryServiceException {
        return registryService.isGroupComputeResourcePreferenceExists(computeResourceId, groupResourceProfileId);
    }

    @Override
    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        return registryService.getGroupComputeResourcePolicy(resourcePolicyId);
    }

    @Override
    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId)
            throws RegistryServiceException {
        return registryService.getBatchQueueResourcePolicy(resourcePolicyId);
    }

    @Override
    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws RegistryServiceException {
        return registryService.getGroupComputeResourcePrefList(groupResourceProfileId);
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        return registryService.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws RegistryServiceException {
        return registryService.getGroupComputeResourcePolicyList(groupResourceProfileId);
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel)
            throws RegistryServiceException {
        try {
            return registryService.registerReplicaLocation(replicaLocationModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Error in retreiving the replica " + replicaLocationModel.getReplicaName());
        }
    }

    /**
     * API Methods related to replica catalog
     *
     * @param dataProductModel
     */
    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws RegistryServiceException {
        try {
            return registryService.registerDataProduct(dataProductModel);
        } catch (Throwable e) {
            throw convertToRegistryServiceException(
                    e, "Error in registering the data resource" + dataProductModel.getProductName());
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
            String gatewayID, String storageId, StoragePreference storagePreference) throws RegistryServiceException {
        return registryService.updateGatewayStoragePreference(gatewayID, storageId, storagePreference);
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
            throws RegistryServiceException {
        return registryService.updateGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
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
            throws RegistryServiceException {
        return registryService.addGatewayStoragePreference(gatewayID, storageResourceId, dataStoragePreference);
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
            throws RegistryServiceException {
        return registryService.addGatewayComputeResourcePreference(
                gatewayID, computeResourceId, computeResourcePreference);
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
            throws RegistryServiceException {
        return registryService.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
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
            throws RegistryServiceException {
        return registryService.registerGatewayResourceProfile(gatewayResourceProfile);
    }

    @Override
    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException {
        return registryService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
    }

    @Override
    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws RegistryServiceException {
        return registryService.registerResourceJobManager(resourceJobManager);
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
            throws RegistryServiceException {
        return registryService.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
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
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws RegistryServiceException {
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
            throws RegistryServiceException {
        return registryService.addGridFTPDataMovementDetails(
                computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
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
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws RegistryServiceException {
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
            throws RegistryServiceException {
        return registryService.addUnicoreDataMovementDetails(resourceId, dmType, priorityOrder, unicoreDataMovement);
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
            throws RegistryServiceException {
        return registryService.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
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
            throws RegistryServiceException {
        return registryService.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
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
            throws RegistryServiceException {
        return registryService.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
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
            throws RegistryServiceException {
        return registryService.addLocalDataMovementDetails(resourceId, dataMoveType, priorityOrder, localDataMovement);
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
            throws RegistryServiceException {
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
            throws RegistryServiceException {
        return registryService.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
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
            throws RegistryServiceException {
        return registryService.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
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
            throws RegistryServiceException {
        return registryService.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudSubmission);
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
            throws RegistryServiceException {
        return registryService.addUNICOREJobSubmissionDetails(computeResourceId, priorityOrder, unicoreJobSubmission);
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
            throws RegistryServiceException {
        return registryService.addSSHForkJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
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
            throws RegistryServiceException {
        return registryService.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
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
            throws RegistryServiceException {
        return registryService.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
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
            throws RegistryServiceException {
        return registryService.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
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
            throws RegistryServiceException {
        return registryService.updateStorageResource(storageResourceId, storageResourceDescription);
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
            throws RegistryServiceException {
        return registryService.registerStorageResource(storageResourceDescription);
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
            throws RegistryServiceException {
        return registryService.updateComputeResource(computeResourceId, computeResourceDescription);
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
            throws RegistryServiceException {
        return registryService.registerComputeResource(computeResourceDescription);
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
            throws RegistryServiceException {
        return registryService.updateApplicationInterface(appInterfaceId, applicationInterface);
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
            throws RegistryServiceException {
        return registryService.registerApplicationInterface(gatewayId, applicationInterface);
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
            throws RegistryServiceException {
        return registryService.updateApplicationDeployment(appDeploymentId, applicationDeployment);
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
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws RegistryServiceException {
        return registryService.registerApplicationDeployment(gatewayId, applicationDeployment);
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
            throws RegistryServiceException {
        return registryService.updateApplicationModule(appModuleId, applicationModule);
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
            throws RegistryServiceException {
        return registryService.registerApplicationModule(gatewayId, applicationModule);
    }

    @Override
    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling)
            throws RegistryServiceException {
        registryService.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws RegistryServiceException {
        registryService.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
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
            throws RegistryServiceException {
        registryService.updateExperiment(airavataExperimentId, experiment);
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
    public String createExperiment(String gatewayId, ExperimentModel experiment) throws RegistryServiceException {
        return registryService.createExperiment(gatewayId, experiment);
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
            throws RegistryServiceException {
        return registryService.searchExperiments(gatewayId, userName, accessibleExpIds, filters, limit, offset);
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
            throws RegistryServiceException {
        return registryService.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
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
    public void updateProject(String projectId, Project updatedProject) throws RegistryServiceException {
        registryService.updateProject(projectId, updatedProject);
    }

    /**
     * Creates a Project with basic metadata.
     * A Project is a container of experiments.
     *
     * @param gatewayId The identifier for the requested gateway.
     * @param project
     */
    @Override
    public String createProject(String gatewayId, Project project) throws RegistryServiceException {
        return registryService.createProject(gatewayId, project);
    }

    @Override
    public boolean updateNotification(Notification notification) throws RegistryServiceException {
        return registryService.updateNotification(notification);
    }

    /**
     * * API methods to retrieve notifications
     * *
     *
     * @param notification
     */
    @Override
    public String createNotification(Notification notification) throws RegistryServiceException {
        return registryService.createNotification(notification);
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
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws RegistryServiceException {
        return registryService.updateGateway(gatewayId, updatedGateway);
    }

    /**
     * Register a Gateway with Airavata.
     *
     * @param gateway The gateway data model.
     * @return gatewayId
     * Th unique identifier of the  newly registered gateway.
     */
    @Override
    public String addGateway(Gateway gateway) throws RegistryServiceException, DuplicateEntryException {
        return registryService.addGateway(gateway);
    }

    /*This private method wraps the logic of getExperiment method as this method is called internally in the API.*/
    private ExperimentModel getExperimentInternal(String airavataExperimentId)
            throws RegistryServiceException, ExperimentNotFoundException {
        try {
            return registryService.getExperiment(airavataExperimentId);
        } catch (RegistryServiceException e) {
            // Check if this is a "not found" error based on the message
            if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                logger.error("Experiment not found: " + airavataExperimentId, e);
                ExperimentNotFoundException exception = new ExperimentNotFoundException();
                exception.setMessage(
                        "Requested experiment id " + airavataExperimentId + " does not exist in the system.");
                throw exception;
            }
            throw e;
        } catch (Throwable e) {
            logger.error("Error while retrieving the experiment", e);
            RegistryServiceException exception = new RegistryServiceException();
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
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
    public String registerUserResourceProfile(UserResourceProfile userResourceProfile) throws RegistryServiceException {
        return registryService.registerUserResourceProfile(userResourceProfile);
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws RegistryServiceException {
        return registryService.isUserResourceProfileExists(userId, gatewayId);
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param userId The identifier for the requested user resource.
     * @return UserResourceProfile object
     */
    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws RegistryServiceException {
        return registryService.getUserResourceProfile(userId, gatewayId);
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
            throws RegistryServiceException {
        return registryService.updateUserResourceProfile(userId, gatewayID, userResourceProfile);
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
    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws RegistryServiceException {
        return registryService.deleteUserResourceProfile(userId, gatewayID);
    }

    @Override
    public String addUser(UserProfile userProfile) throws RegistryServiceException, DuplicateEntryException {
        return registryService.addUser(userProfile);
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
            throws RegistryServiceException {
        return registryService.addUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, userComputeResourcePreference);
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
            throws RegistryServiceException {
        return registryService.isUserComputeResourcePreferenceExists(userId, gatewayID, computeResourceId);
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
            throws RegistryServiceException {
        return registryService.addUserStoragePreference(userId, gatewayID, storageResourceId, dataStoragePreference);
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
            String userId, String gatewayID, String userComputeResourceId) throws RegistryServiceException {
        return registryService.getUserComputeResourcePreference(userId, gatewayID, userComputeResourceId);
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
            throws RegistryServiceException {
        return registryService.getUserStoragePreference(userId, gatewayID, storageId);
    }

    /**
     * Fetch all User Resource Profiles registered
     *
     * @return UserResourceProfile
     * Returns all the UserResourceProfile list object.
     */
    @Override
    public List<UserResourceProfile> getAllUserResourceProfiles() throws RegistryServiceException {
        return registryService.getAllUserResourceProfiles();
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
            throws RegistryServiceException {
        return registryService.updateUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, userComputeResourcePreference);
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
            throws RegistryServiceException {
        return registryService.updateUserStoragePreference(userId, gatewayID, storageId, userStoragePreference);
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
            throws RegistryServiceException {
        return registryService.deleteUserComputeResourcePreference(userId, gatewayID, computeResourceId);
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
            throws RegistryServiceException {
        return registryService.deleteUserStoragePreference(userId, gatewayID, storageId);
    }

    /**
     * * Get queue statuses of all compute resources
     * *
     */
    @Override
    public List<QueueStatusModel> getLatestQueueStatuses() throws RegistryServiceException {
        return registryService.getLatestQueueStatuses();
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws RegistryServiceException {
        registryService.registerQueueStatuses(queueStatuses);
    }

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws RegistryServiceException {
        return registryService.getQueueStatus(hostName, queueName);
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
            throws RegistryServiceException {
        return registryService.getAllUserComputeResourcePreferences(userId, gatewayID);
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
            throws RegistryServiceException {
        return registryService.getAllUserStoragePreferences(userId, gatewayID);
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups)
            throws RegistryServiceException, DuplicateEntryException {
        try {
            registryService.createGatewayGroups(gatewayGroups);
        } catch (Throwable e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                throw new DuplicateEntryException(e.getMessage());
            }
            throw convertToRegistryServiceException(e, "Error while creating GatewayGroups");
        }
    }

    @Override
    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryServiceException {
        registryService.updateGatewayGroups(gatewayGroups);
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryServiceException {
        return registryService.isGatewayGroupsExists(gatewayId);
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryServiceException {
        return registryService.getGatewayGroups(gatewayId);
    }

    @Override
    public Parser getParser(String parserId, String gatewayId) throws RegistryServiceException {
        return registryService.getParser(parserId, gatewayId);
    }

    @Override
    public String saveParser(Parser parser) throws RegistryServiceException {
        return registryService.saveParser(parser);
    }

    @Override
    public List<Parser> listAllParsers(String gatewayId) throws RegistryServiceException {
        return registryService.listAllParsers(gatewayId);
    }

    @Override
    public void removeParser(String parserId, String gatewayId) throws RegistryServiceException {
        registryService.removeParser(parserId, gatewayId);
    }

    @Override
    public ParserInput getParserInput(String parserInputId, String gatewayId) throws RegistryServiceException {
        return registryService.getParserInput(parserInputId, gatewayId);
    }

    @Override
    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws RegistryServiceException {
        return registryService.getParserOutput(parserOutputId, gatewayId);
    }

    @Override
    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        return registryService.getParsingTemplate(templateId, gatewayId);
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws RegistryServiceException {
        return registryService.getParsingTemplatesForExperiment(experimentId, gatewayId);
    }

    @Override
    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws RegistryServiceException {
        return registryService.saveParsingTemplate(parsingTemplate);
    }

    @Override
    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws RegistryServiceException {
        return registryService.listAllParsingTemplates(gatewayId);
    }

    @Override
    public void removeParsingTemplate(String templateId, String gatewayId) throws RegistryServiceException {
        registryService.removeParsingTemplate(templateId, gatewayId);
    }

    @Override
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        return registryService.isGatewayUsageReportingAvailable(gatewayId, computeResourceId);
    }

    @Override
    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        return registryService.getGatewayReportingCommand(gatewayId, computeResourceId);
    }

    @Override
    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws RegistryServiceException {
        registryService.addGatewayUsageReportingCommand(command);
    }

    @Override
    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId)
            throws RegistryServiceException {
        registryService.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
    }
}
