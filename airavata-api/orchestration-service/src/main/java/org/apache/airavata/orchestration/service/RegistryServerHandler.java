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
package org.apache.airavata.orchestration.service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import org.apache.airavata.compute.service.ComputeRegistryService;
import org.apache.airavata.compute.service.ResourceProfileRegistryService;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.GatewayRegistry;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.RegistryHandler;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupComputeResourcePreference;
import org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile;
import org.apache.airavata.model.appcatalog.parser.proto.Parser;
import org.apache.airavata.model.appcatalog.parser.proto.ParserInput;
import org.apache.airavata.model.appcatalog.parser.proto.ParserOutput;
import org.apache.airavata.model.appcatalog.parser.proto.ParsingTemplate;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserComputeResourcePreference;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.proto.UserStoragePreference;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.commons.proto.ErrorModel;
import org.apache.airavata.model.data.movement.proto.DMType;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
import org.apache.airavata.model.data.movement.proto.UnicoreDataMovement;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.experiment.proto.*;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.process.proto.ProcessWorkflow;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.proto.*;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayUsageReportingCommand;
import org.apache.airavata.model.workspace.proto.Notification;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.storage.service.StorageRegistryService;
import org.apache.airavata.task.SchedulerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Delegating facade that routes all methods to domain-specific handlers.
 *
 * <p>Interface methods delegate to:
 * {@link ExperimentRegistryHandler}, {@link ProjectRegistryHandler},
 * {@link AppCatalogRegistryHandler}, {@link ComputeRegistryService},
 * {@link ResourceProfileRegistryService}, {@link StorageRegistryService},
 * {@link GatewayRegistry}.
 */
@Service
public class RegistryServerHandler implements RegistryHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegistryServerHandler.class);

    // --- Domain handlers ---
    @org.springframework.beans.factory.annotation.Autowired
    private ExperimentRegistry experimentRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private ProjectRegistry projectRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private AppCatalogRegistry appCatalogRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private ComputeRegistryService computeRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private ResourceProfileRegistryService resourceProfileRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private StorageRegistryService storageRegistryHandler;

    @org.springframework.beans.factory.annotation.Autowired
    private GatewayRegistry gatewayRegistryHandler;

    @PostConstruct
    public void registerAsGlobalHandler() {
        SchedulerUtils.setRegistryHandler(this);
        logger.info("RegistryServerHandler registered as global registry handler");
    }

    public String getAPIVersion() throws Exception {
        return "0.20.0";
    }

    // =========================================================================
    // ExperimentRegistry delegation
    // =========================================================================

    @Override
    public ExperimentModel getExperiment(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getExperiment(airavataExperimentId);
    }

    @Override
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getExperimentStatus(airavataExperimentId);
    }

    @Override
    public void updateExperiment(String airavataExperimentId, ExperimentModel experiment) throws Exception {
        experimentRegistryHandler.updateExperiment(airavataExperimentId, experiment);
    }

    @Override
    public void updateExperimentStatus(ExperimentStatus experimentStatus, String experimentId) throws Exception {
        experimentRegistryHandler.updateExperimentStatus(experimentStatus, experimentId);
    }

    @Override
    public void addExperimentProcessOutputs(String outputType, List<OutputDataObjectType> outputs, String id)
            throws Exception {
        experimentRegistryHandler.addExperimentProcessOutputs(outputType, outputs, id);
    }

    @Override
    public String addProcess(ProcessModel processModel, String experimentId) throws Exception {
        return experimentRegistryHandler.addProcess(processModel, experimentId);
    }

    @Override
    public ProcessModel getProcess(String processId) throws Exception {
        return experimentRegistryHandler.getProcess(processId);
    }

    @Override
    public List<ProcessModel> getProcessList(String experimentId) throws Exception {
        return experimentRegistryHandler.getProcessList(experimentId);
    }

    @Override
    public List<ProcessModel> getProcessListInState(ProcessState processState) throws Exception {
        return experimentRegistryHandler.getProcessListInState(processState);
    }

    @Override
    public List<String> getProcessIds(String experimentId) throws Exception {
        return experimentRegistryHandler.getProcessIds(experimentId);
    }

    @Override
    public ProcessStatus getProcessStatus(String processId) throws Exception {
        return experimentRegistryHandler.getProcessStatus(processId);
    }

    @Override
    public void updateProcess(ProcessModel processModel, String processId) throws Exception {
        experimentRegistryHandler.updateProcess(processModel, processId);
    }

    @Override
    public void addProcessStatus(ProcessStatus processStatus, String processId) throws Exception {
        experimentRegistryHandler.addProcessStatus(processStatus, processId);
    }

    @Override
    public void updateProcessStatus(ProcessStatus processStatus, String processId) throws Exception {
        experimentRegistryHandler.updateProcessStatus(processStatus, processId);
    }

    @Override
    public void addProcessWorkflow(ProcessWorkflow processWorkflow) throws Exception {
        experimentRegistryHandler.addProcessWorkflow(processWorkflow);
    }

    @Override
    public String addTask(TaskModel taskModel, String processId) throws Exception {
        return experimentRegistryHandler.addTask(taskModel, processId);
    }

    @Override
    public void addTaskStatus(TaskStatus taskStatus, String taskId) throws Exception {
        experimentRegistryHandler.addTaskStatus(taskStatus, taskId);
    }

    @Override
    public void addJob(JobModel jobModel, String processId) throws Exception {
        experimentRegistryHandler.addJob(jobModel, processId);
    }

    @Override
    public List<JobModel> getJobs(String queryType, String id) throws Exception {
        return experimentRegistryHandler.getJobs(queryType, id);
    }

    @Override
    public void addJobStatus(JobStatus jobStatus, String taskId, String jobId) throws Exception {
        experimentRegistryHandler.addJobStatus(jobStatus, taskId, jobId);
    }

    @Override
    public void deleteJobs(String processId) throws Exception {
        experimentRegistryHandler.deleteJobs(processId);
    }

    @Override
    public int getJobCount(JobStatus jobStatus, String gatewayId, double searchBackTimeInMinutes) throws Exception {
        return experimentRegistryHandler.getJobCount(jobStatus, gatewayId, searchBackTimeInMinutes);
    }

    @Override
    public void addErrors(String errorType, ErrorModel errorModel, String id) throws Exception {
        experimentRegistryHandler.addErrors(errorType, errorModel, id);
    }

    @Override
    public Map<String, Double> getAVGTimeDistribution(String gatewayId, double searchBackTimeInMinutes)
            throws Exception {
        return experimentRegistryHandler.getAVGTimeDistribution(gatewayId, searchBackTimeInMinutes);
    }

    // Additional experiment methods
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
            throws Exception {
        return experimentRegistryHandler.getExperimentStatistics(
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

    public List<ExperimentModel> getExperimentsInProject(String gatewayId, String projectId, int limit, int offset)
            throws Exception {
        return experimentRegistryHandler.getExperimentsInProject(gatewayId, projectId, limit, offset);
    }

    public List<ExperimentModel> getUserExperiments(String gatewayId, String userName, int limit, int offset)
            throws Exception {
        return experimentRegistryHandler.getUserExperiments(gatewayId, userName, limit, offset);
    }

    public boolean deleteExperiment(String experimentId) throws Exception {
        return experimentRegistryHandler.deleteExperiment(experimentId);
    }

    public ExperimentModel getDetailedExperimentTree(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getDetailedExperimentTree(airavataExperimentId);
    }

    public List<OutputDataObjectType> getExperimentOutputs(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getExperimentOutputs(airavataExperimentId);
    }

    public List<OutputDataObjectType> getIntermediateOutputs(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getIntermediateOutputs(airavataExperimentId);
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getJobStatuses(airavataExperimentId);
    }

    public void deleteTasks(String processId) throws Exception {
        experimentRegistryHandler.deleteTasks(processId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws Exception {
        return experimentRegistryHandler.getUserConfigurationData(experimentId);
    }

    public List<ProcessStatus> getProcessStatusList(String processId) throws Exception {
        return experimentRegistryHandler.getProcessStatusList(processId);
    }

    public boolean isJobExist(String queryType, String id) throws Exception {
        return experimentRegistryHandler.isJobExist(queryType, id);
    }

    public JobModel getJob(String queryType, String id) throws Exception {
        return experimentRegistryHandler.getJob(queryType, id);
    }

    public List<OutputDataObjectType> getProcessOutputs(String processId) throws Exception {
        return experimentRegistryHandler.getProcessOutputs(processId);
    }

    public List<ProcessWorkflow> getProcessWorkflows(String processId) throws Exception {
        return experimentRegistryHandler.getProcessWorkflows(processId);
    }

    public List<JobModel> getJobDetails(String airavataExperimentId) throws Exception {
        return experimentRegistryHandler.getJobDetails(airavataExperimentId);
    }

    public void updateResourceScheduleing(
            String airavataExperimentId, ComputationalResourceSchedulingModel resourceScheduling) throws Exception {
        experimentRegistryHandler.updateResourceScheduleing(airavataExperimentId, resourceScheduling);
    }

    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws Exception {
        experimentRegistryHandler.updateExperimentConfiguration(airavataExperimentId, userConfiguration);
    }

    public String createExperiment(String gatewayId, ExperimentModel experiment) throws Exception {
        return experimentRegistryHandler.createExperiment(gatewayId, experiment);
    }

    public List<ExperimentSummaryModel> searchExperiments(
            String gatewayId,
            String userName,
            List<String> accessibleExpIds,
            Map<ExperimentSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception {
        return experimentRegistryHandler.searchExperiments(
                gatewayId, userName, accessibleExpIds, filters, limit, offset);
    }

    // =========================================================================
    // ProjectRegistry delegation
    // =========================================================================

    @Override
    public String createProject(String gatewayId, Project project) throws Exception {
        return projectRegistryHandler.createProject(gatewayId, project);
    }

    @Override
    public List<Project> getUserProjects(String gatewayId, String userName, int limit, int offset) throws Exception {
        return projectRegistryHandler.getUserProjects(gatewayId, userName, limit, offset);
    }

    // Additional project/notification methods
    public boolean deleteNotification(String gatewayId, String notificationId) throws Exception {
        return projectRegistryHandler.deleteNotification(gatewayId, notificationId);
    }

    public Notification getNotification(String gatewayId, String notificationId) throws Exception {
        return projectRegistryHandler.getNotification(gatewayId, notificationId);
    }

    public List<Notification> getAllNotifications(String gatewayId) throws Exception {
        return projectRegistryHandler.getAllNotifications(gatewayId);
    }

    public boolean updateNotification(Notification notification) throws Exception {
        return projectRegistryHandler.updateNotification(notification);
    }

    public String createNotification(Notification notification) throws Exception {
        return projectRegistryHandler.createNotification(notification);
    }

    public Project getProject(String projectId) throws Exception {
        return projectRegistryHandler.getProject(projectId);
    }

    public boolean deleteProject(String projectId) throws Exception {
        return projectRegistryHandler.deleteProject(projectId);
    }

    public void updateProject(String projectId, Project updatedProject) throws Exception {
        projectRegistryHandler.updateProject(projectId, updatedProject);
    }

    public List<Project> searchProjects(
            String gatewayId,
            String userName,
            List<String> accessibleProjIds,
            Map<ProjectSearchFields, String> filters,
            int limit,
            int offset)
            throws Exception {
        return projectRegistryHandler.searchProjects(gatewayId, userName, accessibleProjIds, filters, limit, offset);
    }

    // =========================================================================
    // AppCatalogRegistry delegation
    // =========================================================================

    @Override
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws Exception {
        return appCatalogRegistryHandler.getApplicationDeployment(appDeploymentId);
    }

    @Override
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws Exception {
        return appCatalogRegistryHandler.getApplicationInterface(appInterfaceId);
    }

    @Override
    public Parser getParser(String parserId, String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getParser(parserId, gatewayId);
    }

    @Override
    public ParserInput getParserInput(String parserInputId, String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getParserInput(parserInputId, gatewayId);
    }

    @Override
    public List<ParsingTemplate> getParsingTemplatesForExperiment(String experimentId, String gatewayId)
            throws Exception {
        return appCatalogRegistryHandler.getParsingTemplatesForExperiment(experimentId, gatewayId);
    }

    // Additional app catalog methods
    public ApplicationModule getApplicationModule(String appModuleId) throws Exception {
        return appCatalogRegistryHandler.getApplicationModule(appModuleId);
    }

    public List<ApplicationModule> getAllAppModules(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getAllAppModules(gatewayId);
    }

    public List<ApplicationModule> getAccessibleAppModules(
            String gatewayId, List<String> accessibleAppIds, List<String> accessibleComputeResourceIds)
            throws Exception {
        return appCatalogRegistryHandler.getAccessibleAppModules(
                gatewayId, accessibleAppIds, accessibleComputeResourceIds);
    }

    public boolean deleteApplicationModule(String appModuleId) throws Exception {
        return appCatalogRegistryHandler.deleteApplicationModule(appModuleId);
    }

    public boolean deleteApplicationDeployment(String appDeploymentId) throws Exception {
        return appCatalogRegistryHandler.deleteApplicationDeployment(appDeploymentId);
    }

    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getAllApplicationDeployments(gatewayId);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeployments(
            String gatewayId, List<String> accessibleAppDeploymentIds, List<String> accessibleComputeResourceIds)
            throws Exception {
        return appCatalogRegistryHandler.getAccessibleApplicationDeployments(
                gatewayId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    public List<ApplicationDeploymentDescription> getAccessibleApplicationDeploymentsForAppModule(
            String gatewayId,
            String appModuleId,
            List<String> accessibleAppDeploymentIds,
            List<String> accessibleComputeResourceIds)
            throws Exception {
        return appCatalogRegistryHandler.getAccessibleApplicationDeploymentsForAppModule(
                gatewayId, appModuleId, accessibleAppDeploymentIds, accessibleComputeResourceIds);
    }

    public List<String> getAppModuleDeployedResources(String appModuleId) throws Exception {
        return appCatalogRegistryHandler.getAppModuleDeployedResources(appModuleId);
    }

    public List<ApplicationDeploymentDescription> getApplicationDeployments(String appModuleId) throws Exception {
        return appCatalogRegistryHandler.getApplicationDeployments(appModuleId);
    }

    public boolean deleteApplicationInterface(String appInterfaceId) throws Exception {
        return appCatalogRegistryHandler.deleteApplicationInterface(appInterfaceId);
    }

    public Map<String, String> getAllApplicationInterfaceNames(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getAllApplicationInterfaceNames(gatewayId);
    }

    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getAllApplicationInterfaces(gatewayId);
    }

    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws Exception {
        return appCatalogRegistryHandler.getApplicationInputs(appInterfaceId);
    }

    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws Exception {
        return appCatalogRegistryHandler.getApplicationOutputs(appInterfaceId);
    }

    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) throws Exception {
        return appCatalogRegistryHandler.getAvailableAppInterfaceComputeResources(appInterfaceId);
    }

    public boolean updateApplicationInterface(
            String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws Exception {
        return appCatalogRegistryHandler.updateApplicationInterface(appInterfaceId, applicationInterface);
    }

    public String registerApplicationInterface(String gatewayId, ApplicationInterfaceDescription applicationInterface)
            throws Exception {
        return appCatalogRegistryHandler.registerApplicationInterface(gatewayId, applicationInterface);
    }

    public boolean updateApplicationDeployment(
            String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws Exception {
        return appCatalogRegistryHandler.updateApplicationDeployment(appDeploymentId, applicationDeployment);
    }

    public String registerApplicationDeployment(
            String gatewayId, ApplicationDeploymentDescription applicationDeployment) throws Exception {
        return appCatalogRegistryHandler.registerApplicationDeployment(gatewayId, applicationDeployment);
    }

    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule) throws Exception {
        return appCatalogRegistryHandler.updateApplicationModule(appModuleId, applicationModule);
    }

    public String registerApplicationModule(String gatewayId, ApplicationModule applicationModule) throws Exception {
        return appCatalogRegistryHandler.registerApplicationModule(gatewayId, applicationModule);
    }

    public String saveParser(Parser parser) throws Exception {
        return appCatalogRegistryHandler.saveParser(parser);
    }

    public List<Parser> listAllParsers(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.listAllParsers(gatewayId);
    }

    public void removeParser(String parserId, String gatewayId) throws Exception {
        appCatalogRegistryHandler.removeParser(parserId, gatewayId);
    }

    public ParserOutput getParserOutput(String parserOutputId, String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getParserOutput(parserOutputId, gatewayId);
    }

    public ParsingTemplate getParsingTemplate(String templateId, String gatewayId) throws Exception {
        return appCatalogRegistryHandler.getParsingTemplate(templateId, gatewayId);
    }

    public String saveParsingTemplate(ParsingTemplate parsingTemplate) throws Exception {
        return appCatalogRegistryHandler.saveParsingTemplate(parsingTemplate);
    }

    public List<ParsingTemplate> listAllParsingTemplates(String gatewayId) throws Exception {
        return appCatalogRegistryHandler.listAllParsingTemplates(gatewayId);
    }

    public void removeParsingTemplate(String templateId, String gatewayId) throws Exception {
        appCatalogRegistryHandler.removeParsingTemplate(templateId, gatewayId);
    }

    // =========================================================================
    // ComputeRegistry delegation
    // =========================================================================

    @Override
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws Exception {
        return computeRegistryHandler.getComputeResource(computeResourceId);
    }

    @Override
    public LOCALSubmission getLocalJobSubmission(String jobSubmissionId) throws Exception {
        return computeRegistryHandler.getLocalJobSubmission(jobSubmissionId);
    }

    @Override
    public SSHJobSubmission getSSHJobSubmission(String jobSubmissionId) throws Exception {
        return computeRegistryHandler.getSSHJobSubmission(jobSubmissionId);
    }

    @Override
    public UnicoreJobSubmission getUnicoreJobSubmission(String jobSubmissionId) throws Exception {
        return computeRegistryHandler.getUnicoreJobSubmission(jobSubmissionId);
    }

    @Override
    public CloudJobSubmission getCloudJobSubmission(String jobSubmissionId) throws Exception {
        return computeRegistryHandler.getCloudJobSubmission(jobSubmissionId);
    }

    // Additional compute methods
    public Map<String, String> getAllComputeResourceNames() throws Exception {
        return computeRegistryHandler.getAllComputeResourceNames();
    }

    public boolean deleteComputeResource(String computeResourceId) throws Exception {
        return computeRegistryHandler.deleteComputeResource(computeResourceId);
    }

    public boolean updateComputeResource(
            String computeResourceId, ComputeResourceDescription computeResourceDescription) throws Exception {
        return computeRegistryHandler.updateComputeResource(computeResourceId, computeResourceDescription);
    }

    public String registerComputeResource(ComputeResourceDescription computeResourceDescription) throws Exception {
        return computeRegistryHandler.registerComputeResource(computeResourceDescription);
    }

    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder) throws Exception {
        return computeRegistryHandler.changeJobSubmissionPriority(jobSubmissionInterfaceId, newPriorityOrder);
    }

    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder) throws Exception {
        return storageRegistryHandler.changeDataMovementPriority(dataMovementInterfaceId, newPriorityOrder);
    }

    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap) throws Exception {
        return computeRegistryHandler.changeJobSubmissionPriorities(jobSubmissionPriorityMap);
    }

    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap) throws Exception {
        return storageRegistryHandler.changeDataMovementPriorities(dataMovementPriorityMap);
    }

    public boolean deleteJobSubmissionInterface(String computeResourceId, String jobSubmissionInterfaceId)
            throws Exception {
        return computeRegistryHandler.deleteJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
    }

    public ResourceJobManager getResourceJobManager(String resourceJobManagerId) throws Exception {
        return computeRegistryHandler.getResourceJobManager(resourceJobManagerId);
    }

    public boolean deleteResourceJobManager(String resourceJobManagerId) throws Exception {
        return computeRegistryHandler.deleteResourceJobManager(resourceJobManagerId);
    }

    public boolean deleteBatchQueue(String computeResourceId, String queueName) throws Exception {
        return computeRegistryHandler.deleteBatchQueue(computeResourceId, queueName);
    }

    public boolean updateResourceJobManager(String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws Exception {
        return computeRegistryHandler.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
    }

    public String registerResourceJobManager(ResourceJobManager resourceJobManager) throws Exception {
        return computeRegistryHandler.registerResourceJobManager(resourceJobManager);
    }

    public String addCloudJobSubmissionDetails(
            String computeResourceId, int priorityOrder, CloudJobSubmission cloudSubmission) throws Exception {
        return computeRegistryHandler.addCloudJobSubmissionDetails(computeResourceId, priorityOrder, cloudSubmission);
    }

    public String addUNICOREJobSubmissionDetails(
            String computeResourceId, int priorityOrder, UnicoreJobSubmission unicoreJobSubmission) throws Exception {
        return computeRegistryHandler.addUNICOREJobSubmissionDetails(
                computeResourceId, priorityOrder, unicoreJobSubmission);
    }

    public String addSSHForkJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws Exception {
        return computeRegistryHandler.addSSHForkJobSubmissionDetails(
                computeResourceId, priorityOrder, sshJobSubmission);
    }

    public String addSSHJobSubmissionDetails(
            String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws Exception {
        return computeRegistryHandler.addSSHJobSubmissionDetails(computeResourceId, priorityOrder, sshJobSubmission);
    }

    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws Exception {
        return computeRegistryHandler.updateSSHJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
    }

    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission sshJobSubmission)
            throws Exception {
        return computeRegistryHandler.updateCloudJobSubmissionDetails(jobSubmissionInterfaceId, sshJobSubmission);
    }

    public boolean updateUnicoreJobSubmissionDetails(
            String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission) throws Exception {
        return computeRegistryHandler.updateUnicoreJobSubmissionDetails(jobSubmissionInterfaceId, unicoreJobSubmission);
    }

    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws Exception {
        return computeRegistryHandler.updateLocalSubmissionDetails(jobSubmissionInterfaceId, localSubmission);
    }

    public String addLocalSubmissionDetails(
            String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws Exception {
        return computeRegistryHandler.addLocalSubmissionDetails(computeResourceId, priorityOrder, localSubmission);
    }

    public LOCALDataMovement getLocalDataMovement(String dataMovementId) throws Exception {
        return storageRegistryHandler.getLocalDataMovement(dataMovementId);
    }

    public UnicoreDataMovement getUnicoreDataMovement(String dataMovementId) throws Exception {
        return storageRegistryHandler.getUnicoreDataMovement(dataMovementId);
    }

    public GridFTPDataMovement getGridFTPDataMovement(String dataMovementId) throws Exception {
        return storageRegistryHandler.getGridFTPDataMovement(dataMovementId);
    }

    public boolean deleteDataMovementInterface(String resourceId, String dataMovementInterfaceId, DMType dmType)
            throws Exception {
        return storageRegistryHandler.deleteDataMovementInterface(resourceId, dataMovementInterfaceId, dmType);
    }

    public boolean updateGridFTPDataMovementDetails(
            String dataMovementInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws Exception {
        return storageRegistryHandler.updateGridFTPDataMovementDetails(dataMovementInterfaceId, gridFTPDataMovement);
    }

    public String addGridFTPDataMovementDetails(
            String computeResourceId, DMType dmType, int priorityOrder, GridFTPDataMovement gridFTPDataMovement)
            throws Exception {
        return storageRegistryHandler.addGridFTPDataMovementDetails(
                computeResourceId, dmType, priorityOrder, gridFTPDataMovement);
    }

    public boolean updateUnicoreDataMovementDetails(
            String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement) throws Exception {
        return storageRegistryHandler.updateUnicoreDataMovementDetails(dataMovementInterfaceId, unicoreDataMovement);
    }

    public String addUnicoreDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws Exception {
        return storageRegistryHandler.addUnicoreDataMovementDetails(
                resourceId, dmType, priorityOrder, unicoreDataMovement);
    }

    public boolean updateSCPDataMovementDetails(String dataMovementInterfaceId, SCPDataMovement scpDataMovement)
            throws Exception {
        return storageRegistryHandler.updateSCPDataMovementDetails(dataMovementInterfaceId, scpDataMovement);
    }

    public String addSCPDataMovementDetails(
            String resourceId, DMType dmType, int priorityOrder, SCPDataMovement scpDataMovement) throws Exception {
        return storageRegistryHandler.addSCPDataMovementDetails(resourceId, dmType, priorityOrder, scpDataMovement);
    }

    public boolean updateLocalDataMovementDetails(String dataMovementInterfaceId, LOCALDataMovement localDataMovement)
            throws Exception {
        return storageRegistryHandler.updateLocalDataMovementDetails(dataMovementInterfaceId, localDataMovement);
    }

    public String addLocalDataMovementDetails(
            String resourceId, DMType dataMoveType, int priorityOrder, LOCALDataMovement localDataMovement)
            throws Exception {
        return storageRegistryHandler.addLocalDataMovementDetails(
                resourceId, dataMoveType, priorityOrder, localDataMovement);
    }

    // =========================================================================
    // ResourceProfileRegistry delegation
    // =========================================================================

    @Override
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws Exception {
        return resourceProfileRegistryHandler.getGatewayResourceProfile(gatewayID);
    }

    @Override
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayId, String computeResourceId)
            throws Exception {
        return resourceProfileRegistryHandler.getGatewayComputeResourcePreference(gatewayId, computeResourceId);
    }

    @Override
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws Exception {
        return resourceProfileRegistryHandler.getAllGatewayComputeResourcePreferences(gatewayID);
    }

    @Override
    public StoragePreference getGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        return resourceProfileRegistryHandler.getGatewayStoragePreference(gatewayID, storageId);
    }

    @Override
    public List<StoragePreference> getAllGatewayStoragePreferences(String gatewayId) throws Exception {
        return resourceProfileRegistryHandler.getAllGatewayStoragePreferences(gatewayId);
    }

    @Override
    public GroupResourceProfile getGroupResourceProfile(String groupResourceProfileId) throws Exception {
        return resourceProfileRegistryHandler.getGroupResourceProfile(groupResourceProfileId);
    }

    @Override
    public boolean isGroupResourceProfileExists(String groupResourceProfileId) throws Exception {
        return resourceProfileRegistryHandler.isGroupResourceProfileExists(groupResourceProfileId);
    }

    @Override
    public GroupComputeResourcePreference getGroupComputeResourcePreference(
            String computeResourceId, String groupResourceProfileId) throws Exception {
        return resourceProfileRegistryHandler.getGroupComputeResourcePreference(
                computeResourceId, groupResourceProfileId);
    }

    @Override
    public boolean isGroupComputeResourcePreferenceExists(String computeResourceId, String groupResourceProfileId)
            throws Exception {
        return resourceProfileRegistryHandler.isGroupComputeResourcePreferenceExists(
                computeResourceId, groupResourceProfileId);
    }

    @Override
    public List<BatchQueueResourcePolicy> getGroupBatchQueueResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        return resourceProfileRegistryHandler.getGroupBatchQueueResourcePolicyList(groupResourceProfileId);
    }

    @Override
    public List<ComputeResourcePolicy> getGroupComputeResourcePolicyList(String groupResourceProfileId)
            throws Exception {
        return resourceProfileRegistryHandler.getGroupComputeResourcePolicyList(groupResourceProfileId);
    }

    @Override
    public UserResourceProfile getUserResourceProfile(String userId, String gatewayId) throws Exception {
        return resourceProfileRegistryHandler.getUserResourceProfile(userId, gatewayId);
    }

    @Override
    public boolean isUserResourceProfileExists(String userId, String gatewayId) throws Exception {
        return resourceProfileRegistryHandler.isUserResourceProfileExists(userId, gatewayId);
    }

    @Override
    public UserComputeResourcePreference getUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId) throws Exception {
        return resourceProfileRegistryHandler.getUserComputeResourcePreference(userId, gatewayID, computeResourceId);
    }

    @Override
    public boolean isUserComputeResourcePreferenceExists(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        return resourceProfileRegistryHandler.isUserComputeResourcePreferenceExists(
                userId, gatewayID, computeResourceId);
    }

    @Override
    public boolean isGatewayUsageReportingAvailable(String gatewayId, String computeResourceId) throws Exception {
        return resourceProfileRegistryHandler.isGatewayUsageReportingAvailable(gatewayId, computeResourceId);
    }

    @Override
    public GatewayUsageReportingCommand getGatewayReportingCommand(String gatewayId, String computeResourceId)
            throws Exception {
        return resourceProfileRegistryHandler.getGatewayReportingCommand(gatewayId, computeResourceId);
    }

    // Additional resource profile methods
    public boolean deleteGatewayResourceProfile(String gatewayID) throws Exception {
        return resourceProfileRegistryHandler.deleteGatewayResourceProfile(gatewayID);
    }

    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws Exception {
        return resourceProfileRegistryHandler.deleteGatewayComputeResourcePreference(gatewayID, computeResourceId);
    }

    public boolean deleteGatewayStoragePreference(String gatewayID, String storageId) throws Exception {
        return resourceProfileRegistryHandler.deleteGatewayStoragePreference(gatewayID, storageId);
    }

    public List<GatewayResourceProfile> getAllGatewayResourceProfiles() throws Exception {
        return resourceProfileRegistryHandler.getAllGatewayResourceProfiles();
    }

    public String createGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        return resourceProfileRegistryHandler.createGroupResourceProfile(grp);
    }

    public void updateGroupResourceProfile(GroupResourceProfile grp) throws Exception {
        resourceProfileRegistryHandler.updateGroupResourceProfile(grp);
    }

    public boolean removeGroupResourceProfile(String groupResourceProfileId) throws Exception {
        return resourceProfileRegistryHandler.removeGroupResourceProfile(groupResourceProfileId);
    }

    public List<GroupResourceProfile> getGroupResourceList(String gatewayId, List<String> accessibleGroupResProfileIds)
            throws Exception {
        return resourceProfileRegistryHandler.getGroupResourceList(gatewayId, accessibleGroupResProfileIds);
    }

    public boolean removeGroupComputePrefs(String computeResourceId, String groupResourceProfileId) throws Exception {
        return resourceProfileRegistryHandler.removeGroupComputePrefs(computeResourceId, groupResourceProfileId);
    }

    public boolean removeGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        return resourceProfileRegistryHandler.removeGroupComputeResourcePolicy(resourcePolicyId);
    }

    public boolean removeGroupBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        return resourceProfileRegistryHandler.removeGroupBatchQueueResourcePolicy(resourcePolicyId);
    }

    public ComputeResourcePolicy getGroupComputeResourcePolicy(String resourcePolicyId) throws Exception {
        return resourceProfileRegistryHandler.getGroupComputeResourcePolicy(resourcePolicyId);
    }

    public BatchQueueResourcePolicy getBatchQueueResourcePolicy(String resourcePolicyId) throws Exception {
        return resourceProfileRegistryHandler.getBatchQueueResourcePolicy(resourcePolicyId);
    }

    public List<GroupComputeResourcePreference> getGroupComputeResourcePrefList(String groupResourceProfileId)
            throws Exception {
        return resourceProfileRegistryHandler.getGroupComputeResourcePrefList(groupResourceProfileId);
    }

    public String registerUserResourceProfile(UserResourceProfile urp) throws Exception {
        return resourceProfileRegistryHandler.registerUserResourceProfile(urp);
    }

    public boolean updateUserResourceProfile(String userId, String gatewayID, UserResourceProfile urp)
            throws Exception {
        return resourceProfileRegistryHandler.updateUserResourceProfile(userId, gatewayID, urp);
    }

    public boolean deleteUserResourceProfile(String userId, String gatewayID) throws Exception {
        return resourceProfileRegistryHandler.deleteUserResourceProfile(userId, gatewayID);
    }

    public List<UserResourceProfile> getAllUserResourceProfiles() throws Exception {
        return resourceProfileRegistryHandler.getAllUserResourceProfiles();
    }

    public boolean addUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        return resourceProfileRegistryHandler.addUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, pref);
    }

    public boolean addUserStoragePreference(
            String userId, String gatewayID, String storageResourceId, UserStoragePreference dsp) throws Exception {
        return resourceProfileRegistryHandler.addUserStoragePreference(userId, gatewayID, storageResourceId, dsp);
    }

    public UserStoragePreference getUserStoragePreference(String userId, String gatewayID, String storageId)
            throws Exception {
        return resourceProfileRegistryHandler.getUserStoragePreference(userId, gatewayID, storageId);
    }

    public List<UserComputeResourcePreference> getAllUserComputeResourcePreferences(String userId, String gatewayID)
            throws Exception {
        return resourceProfileRegistryHandler.getAllUserComputeResourcePreferences(userId, gatewayID);
    }

    public List<UserStoragePreference> getAllUserStoragePreferences(String userId, String gatewayID) throws Exception {
        return resourceProfileRegistryHandler.getAllUserStoragePreferences(userId, gatewayID);
    }

    public boolean updateUserComputeResourcePreference(
            String userId, String gatewayID, String computeResourceId, UserComputeResourcePreference pref)
            throws Exception {
        return resourceProfileRegistryHandler.updateUserComputeResourcePreference(
                userId, gatewayID, computeResourceId, pref);
    }

    public boolean updateUserStoragePreference(
            String userId, String gatewayID, String storageId, UserStoragePreference pref) throws Exception {
        return resourceProfileRegistryHandler.updateUserStoragePreference(userId, gatewayID, storageId, pref);
    }

    public boolean deleteUserComputeResourcePreference(String userId, String gatewayID, String computeResourceId)
            throws Exception {
        return resourceProfileRegistryHandler.deleteUserComputeResourcePreference(userId, gatewayID, computeResourceId);
    }

    public boolean deleteUserStoragePreference(String userId, String gatewayID, String storageId) throws Exception {
        return resourceProfileRegistryHandler.deleteUserStoragePreference(userId, gatewayID, storageId);
    }

    public boolean updateGatewayStoragePreference(
            String gatewayID, String storageId, StoragePreference storagePreference) throws Exception {
        return resourceProfileRegistryHandler.updateGatewayStoragePreference(gatewayID, storageId, storagePreference);
    }

    public boolean updateGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        return resourceProfileRegistryHandler.updateGatewayComputeResourcePreference(gatewayID, computeResourceId, crp);
    }

    public boolean addGatewayStoragePreference(String gatewayID, String storageResourceId, StoragePreference dsp)
            throws Exception {
        return resourceProfileRegistryHandler.addGatewayStoragePreference(gatewayID, storageResourceId, dsp);
    }

    public boolean addGatewayComputeResourcePreference(
            String gatewayID, String computeResourceId, ComputeResourcePreference crp) throws Exception {
        return resourceProfileRegistryHandler.addGatewayComputeResourcePreference(gatewayID, computeResourceId, crp);
    }

    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile grp) throws Exception {
        return resourceProfileRegistryHandler.updateGatewayResourceProfile(gatewayID, grp);
    }

    public String registerGatewayResourceProfile(GatewayResourceProfile grp) throws Exception {
        return resourceProfileRegistryHandler.registerGatewayResourceProfile(grp);
    }

    public void addGatewayUsageReportingCommand(GatewayUsageReportingCommand command) throws Exception {
        resourceProfileRegistryHandler.addGatewayUsageReportingCommand(command);
    }

    public void removeGatewayUsageReportingCommand(String gatewayId, String computeResourceId) throws Exception {
        resourceProfileRegistryHandler.removeGatewayUsageReportingCommand(gatewayId, computeResourceId);
    }

    // =========================================================================
    // StorageRegistry delegation
    // =========================================================================

    @Override
    public StorageResourceDescription getStorageResource(String storageResourceId) throws Exception {
        return storageRegistryHandler.getStorageResource(storageResourceId);
    }

    @Override
    public SCPDataMovement getSCPDataMovement(String dataMoveId) throws Exception {
        return storageRegistryHandler.getSCPDataMovement(dataMoveId);
    }

    @Override
    public String registerDataProduct(DataProductModel dataProductModel) throws Exception {
        return storageRegistryHandler.registerDataProduct(dataProductModel);
    }

    @Override
    public DataProductModel getDataProduct(String productUri) throws Exception {
        return storageRegistryHandler.getDataProduct(productUri);
    }

    @Override
    public DataProductModel getParentDataProduct(String productUri) throws Exception {
        return storageRegistryHandler.getParentDataProduct(productUri);
    }

    @Override
    public List<DataProductModel> getChildDataProducts(String productUri) throws Exception {
        return storageRegistryHandler.getChildDataProducts(productUri);
    }

    @Override
    public String registerReplicaLocation(DataReplicaLocationModel replicaLocationModel) throws Exception {
        return storageRegistryHandler.registerReplicaLocation(replicaLocationModel);
    }

    @Override
    public List<DataProductModel> searchDataProductsByName(
            String gatewayId, String userId, String productName, int limit, int offset) throws Exception {
        return storageRegistryHandler.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
    }

    // Additional storage methods
    public Map<String, String> getAllStorageResourceNames() throws Exception {
        return storageRegistryHandler.getAllStorageResourceNames();
    }

    public boolean deleteStorageResource(String storageResourceId) throws Exception {
        return storageRegistryHandler.deleteStorageResource(storageResourceId);
    }

    public boolean updateStorageResource(
            String storageResourceId, StorageResourceDescription storageResourceDescription) throws Exception {
        return storageRegistryHandler.updateStorageResource(storageResourceId, storageResourceDescription);
    }

    public String registerStorageResource(StorageResourceDescription storageResourceDescription) throws Exception {
        return storageRegistryHandler.registerStorageResource(storageResourceDescription);
    }

    // =========================================================================
    // GatewayRegistry delegation
    // =========================================================================

    @Override
    public String addGateway(Gateway gateway) throws Exception {
        return gatewayRegistryHandler.addGateway(gateway);
    }

    @Override
    public Gateway getGateway(String gatewayId) throws Exception {
        return gatewayRegistryHandler.getGateway(gatewayId);
    }

    @Override
    public boolean isGatewayExist(String gatewayId) throws Exception {
        return gatewayRegistryHandler.isGatewayExist(gatewayId);
    }

    @Override
    public boolean updateGateway(String gatewayId, Gateway updatedGateway) throws Exception {
        return gatewayRegistryHandler.updateGateway(gatewayId, updatedGateway);
    }

    @Override
    public boolean deleteGateway(String gatewayId) throws Exception {
        return gatewayRegistryHandler.deleteGateway(gatewayId);
    }

    @Override
    public List<Gateway> getAllGateways() throws Exception {
        return gatewayRegistryHandler.getAllGateways();
    }

    @Override
    public boolean isUserExists(String gatewayId, String userName) throws Exception {
        return gatewayRegistryHandler.isUserExists(gatewayId, userName);
    }

    @Override
    public List<String> getAllUsersInGateway(String gatewayId) throws Exception {
        return gatewayRegistryHandler.getAllUsersInGateway(gatewayId);
    }

    @Override
    public String addUser(UserProfile userProfile) throws Exception {
        return gatewayRegistryHandler.addUser(userProfile);
    }

    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws Exception {
        return gatewayRegistryHandler.isGatewayGroupsExists(gatewayId);
    }

    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws Exception {
        return gatewayRegistryHandler.getGatewayGroups(gatewayId);
    }

    @Override
    public void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        gatewayRegistryHandler.createGatewayGroups(gatewayGroups);
    }

    @Override
    public QueueStatusModel getQueueStatus(String hostName, String queueName) throws Exception {
        return gatewayRegistryHandler.getQueueStatus(hostName, queueName);
    }

    @Override
    public void registerQueueStatuses(List<QueueStatusModel> queueStatuses) throws Exception {
        gatewayRegistryHandler.registerQueueStatuses(queueStatuses);
    }

    // Additional gateway methods
    public void updateGatewayGroups(GatewayGroups gatewayGroups) throws Exception {
        gatewayRegistryHandler.updateGatewayGroups(gatewayGroups);
    }

    public List<QueueStatusModel> getLatestQueueStatuses() throws Exception {
        return gatewayRegistryHandler.getLatestQueueStatuses();
    }
}
