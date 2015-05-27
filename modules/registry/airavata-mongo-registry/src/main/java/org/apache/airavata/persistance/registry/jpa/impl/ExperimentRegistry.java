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

package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.mongo.dao.ExperimentDao;
import org.apache.airavata.persistance.registry.jpa.resources.*;
import org.apache.airavata.persistance.registry.jpa.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.RegistryModelType;
import org.apache.airavata.registry.cpi.ResultOrderType;
import org.apache.airavata.registry.cpi.utils.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class ExperimentRegistry {
    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private final static AiravataLogger logger = AiravataLoggerFactory.getLogger(ExperimentRegistry.class);

    private ExperimentDao experimentDao;

    public ExperimentRegistry(GatewayResource gateway, UserResource user) throws RegistryException {
        gatewayResource = gateway;
        if (!gatewayResource.isExists(ResourceType.GATEWAY_WORKER, user.getUserName())) {
            workerResource = ResourceUtils.addGatewayWorker(gateway, user);
        } else {
            workerResource = (WorkerResource) ResourceUtils.getWorker(gateway.getGatewayId(), user.getUserName());
        }

        this.experimentDao = new ExperimentDao();
    }

    public String addExperiment(Experiment experiment, String gatewayId) throws RegistryException {
        try {
            if (!ResourceUtils.isUserExist(experiment.getUserName())) {
                ResourceUtils.addUser(experiment.getUserName(), null);
            }
            if (!workerResource.isProjectExists(experiment.getProjectId())) {
                logger.error("Project does not exist in the system..");
                throw new Exception("Project does not exist in the system, Please" +
                        " create the project first...");
            }
            //setting up unique ids
            experiment.setExperimentId(getExperimentId(experiment.getName()));
            for (WorkflowNodeDetails wfnd : experiment.getWorkflowNodeDetailsList()) {
                wfnd.setNodeInstanceId(getNodeInstanceID(wfnd.getNodeName()));
                for (TaskDetails taskDetails : wfnd.getTaskDetailsList()) {
                    taskDetails.setTaskId(getTaskId(wfnd.getNodeName()));
                    for (DataTransferDetails dtd : taskDetails.getDataTransferDetailsList()) {
                        dtd.setTransferId(getDataTransferID(taskDetails.getTaskId()));
                    }
                }
            }
            experimentDao.createExperiment(experiment);
        } catch (Exception e) {
            logger.error("Error while saving experiment to registry", e);
            throw new RegistryException(e);
        }
        return experiment.getExperimentId();
    }

    public String addUserConfigData(UserConfigurationData configurationData, String expId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            experiment.setUserConfigurationData(configurationData);
            experimentDao.updateExperiment(experiment);
            return expId;
        } catch (Exception e) {
            logger.error("Unable to save user config data", e);
            throw new RegistryException(e);
        }
    }

    public String addExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
        return updateExpOutputs(exOutput, expId);
    }

    public String updateExpOutputs(List<OutputDataObjectType> exOutput, String expId) throws RegistryException {
        try {
            Experiment experiement = experimentDao.getExperiment(expId);
            experiement.setExperimentOutputs(exOutput);
            experimentDao.updateExperiment(experiement);
            return expId;
        } catch (Exception e) {
            logger.error("Error while updating experiment outputs", e);
            throw new RegistryException(e);
        }
    }

    public String addNodeOutputs(List<OutputDataObjectType> wfOutputs, CompositeIdentifier ids) throws RegistryException {
        return updateNodeOutputs(wfOutputs, (String) ids.getSecondLevelIdentifier());
    }

    public String updateNodeOutputs(List<OutputDataObjectType> wfOutputs, String nodeId) throws RegistryException {
        try {
            WorkflowNodeDetails wfnd = experimentDao.getWFNode(nodeId);
            wfnd.setNodeOutputs(wfOutputs);
            experimentDao.updateWFNode(wfnd);
            return nodeId;
        } catch (Exception e) {
            logger.error("Error while updating node outputs...", e);
            throw new RegistryException(e);
        }
    }

    public String addApplicationOutputs(List<OutputDataObjectType> appOutputs, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getThirdLevelIdentifier());
            taskDetails.getApplicationOutputs().addAll(appOutputs);
            experimentDao.updateTaskDetail(taskDetails);
            return (String) ids.getSecondLevelIdentifier();
        } catch (Exception e) {
            logger.error("Error while adding application outputs...", e);
            throw new RegistryException(e);
        }
    }

    public String updateExperimentStatus(ExperimentStatus experimentStatus, String expId)
            throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            String currentState = (experiment.getExperimentStatus() == null)
                    ? ExperimentState.UNKNOWN.name()
                    : experiment.getExperimentStatus().getExperimentState().name();
            if (isValidStatusTransition(ExperimentState.valueOf(currentState),
                    experimentStatus.getExperimentState())) {
                experiment.setExperimentStatus(experimentStatus);
                experimentDao.updateExperiment(experiment);
                logger.debugId(expId, "Updated experiment {} status to {}.",
                        expId, experimentStatus.toString());
                return experiment.getExperimentId();
            }
        } catch (Exception e) {
            logger.errorId(expId, "Error while updating experiment status...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public String addWorkflowNodeStatus(WorkflowNodeStatus status, CompositeIdentifier ids) throws RegistryException {
        return updateWorkflowNodeStatus(status, (String) ids.getSecondLevelIdentifier());
    }

    public String updateWorkflowNodeStatus(WorkflowNodeStatus status, String nodeId) throws RegistryException {
        try {
            WorkflowNodeDetails wfnd = experimentDao.getWFNode(nodeId);
            wfnd.setWorkflowNodeStatus(status);
            experimentDao.updateWFNode(wfnd);
            return nodeId;
        } catch (Exception e) {
            logger.errorId(nodeId, "Error while updating workflow node status to " + status.toString() + "...", e);
            throw new RegistryException(e);
        }
    }

    public String addTaskStatus(TaskStatus status, CompositeIdentifier ids) throws RegistryException {
        return updateTaskStatus(status, (String) ids.getThirdLevelIdentifier());
    }

    public String updateTaskStatus(TaskStatus status, String taskId) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
            taskDetails.setTaskStatus(status);
            experimentDao.updateTaskDetail(taskDetails);
            return taskId;
        } catch (Exception e) {
            logger.errorId(taskId, "Error while updating task status to " + status.toString() + "...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param status job status
     * @param ids    composite id will contain taskid and jobid
     * @return status id
     */
    public String addJobStatus(JobStatus status, CompositeIdentifier ids) throws RegistryException {
        return updateJobStatus(status, ids);
    }

    public String updateJobStatus(JobStatus status, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    if (status.getJobState() == null) {
                        status.setJobState(JobState.UNKNOWN);
                    }
                    jobDetails.setJobStatus(status);
                    experimentDao.updateTaskDetail(taskDetails);
                    return (String) ids.getSecondLevelIdentifier();
                }
            }
            return null;
        } catch (Exception e) {
            logger.errorId(ids.toString(), "Error while updating job status to " + status.toString() + " ...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param status application status
     * @param ids    composite id will contain taskid and jobid
     * @return status id
     */
    public String addApplicationStatus(ApplicationStatus status, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    jobDetails.setApplicationStatus(status);
                    experimentDao.updateTaskDetail(taskDetails);
                    return (String) ids.getSecondLevelIdentifier();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Unable to read airavata-server properties", e);
            throw new RegistryException(e);
        }
    }

    public void updateApplicationStatus(ApplicationStatus status, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    jobDetails.setApplicationStatus(status);
                    experimentDao.updateTaskDetail(taskDetails);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating application status...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param status data transfer status
     * @param ids    contains taskId and transfer id
     * @return status id
     */
    public String addTransferStatus(TransferStatus status, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (DataTransferDetails dataTransferDetails : taskDetails.getDataTransferDetailsList()) {
                if (dataTransferDetails.getTransferId().equals(ids.getSecondLevelIdentifier())) {
                    if (status.getTransferState() == null) {
                        status.setTransferState(TransferState.UNKNOWN);
                    }
                    dataTransferDetails.setTransferStatus(status);
                    experimentDao.updateTaskDetail(taskDetails);
                    return (String) ids.getSecondLevelIdentifier();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while adding transfer status...", e);
            throw new RegistryException(e);
        }
    }

    public void updateTransferStatus(TransferStatus status, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (DataTransferDetails dataTransferDetails : taskDetails.getDataTransferDetailsList()) {
                if (dataTransferDetails.getTransferId().equals(ids.getSecondLevelIdentifier())) {
                    dataTransferDetails.setTransferStatus(status);
                    experimentDao.updateTaskDetail(taskDetails);
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating transfer status...", e);
            throw new RegistryException(e);
        }
    }

    public String addWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String expId) throws RegistryException {
        try {
            nodeDetails.setNodeInstanceId(getNodeInstanceID(nodeDetails.getNodeName()));
            for (TaskDetails taskDetails : nodeDetails.getTaskDetailsList()) {
                taskDetails.setTaskId(getTaskId(nodeDetails.getNodeName()));
                for (DataTransferDetails dtd : taskDetails.getDataTransferDetailsList()) {
                    dtd.setTransferId(getDataTransferID(taskDetails.getTaskId()));
                }
            }
            experimentDao.createWFNode(expId, nodeDetails);
            return nodeDetails.getNodeInstanceId();
        } catch (Exception e) {
            logger.error("Error while adding workflow node details...", e);
            throw new RegistryException(e);
        }
    }

    public void updateWorkflowNodeDetails(WorkflowNodeDetails nodeDetails, String nodeId) throws RegistryException {
        try {
            experimentDao.updateWFNode(nodeDetails);
        } catch (Exception e) {
            logger.error("Error while updating workflow node details...", e);
            throw new RegistryException(e);
        }
    }

    public String addTaskDetails(TaskDetails taskDetails, String nodeId) throws RegistryException {
        try {
            experimentDao.createTaskDetail(nodeId, taskDetails);
            return taskDetails.getTaskId();
        } catch (Exception e) {
            logger.error("Error while adding task details...", e);
            throw new RegistryException(e);
        }
    }

    public String updateTaskDetails(TaskDetails taskDetails, String taskId) throws RegistryException {
        try {
            experimentDao.updateTaskDetail(taskDetails);
            return taskDetails.getTaskId();
        } catch (Exception e) {
            logger.error("Error while updating task details...", e);
            throw new RegistryException(e);
        }
    }

    public void updateAppOutputs(List<OutputDataObjectType> appOutputs, String taskId) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
            taskDetails.setApplicationOutputs(appOutputs);
            experimentDao.updateTaskDetail(taskDetails);
        } catch (Exception e) {
            logger.error("Error while updating application outputs...", e);
            throw new RegistryException(e);
        }
    }

    public String addJobDetails(JobDetails jobDetails, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            jobDetails.setJobId((String) ids.getSecondLevelIdentifier());
            taskDetails.getJobDetailsList().add(jobDetails);
            experimentDao.updateTaskDetail(taskDetails);
            return (String) ids.getSecondLevelIdentifier();
        } catch (Exception e) {
            logger.error("Error while adding job details...", e);
            throw new RegistryException(e);
        }
    }

    // ids - taskId + jobid
    public void updateJobDetails(JobDetails jobDetails, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            List<JobDetails> jobDetailsList = taskDetails.getJobDetailsList();
            for (JobDetails exisitingJobDetail : taskDetails.getJobDetailsList()) {
                if (exisitingJobDetail.getJobId().equals(jobDetails.getJobId())) {
                    jobDetailsList.remove(exisitingJobDetail);
                    jobDetailsList.add(jobDetails);
                    taskDetails.setJobDetailsList(jobDetailsList);
                    experimentDao.updateTaskDetail(taskDetails);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error while updating job details...", e);
            throw new RegistryException(e);
        }
    }

    public String addDataTransferDetails(DataTransferDetails transferDetails, String taskId) throws RegistryException {
        try {
            if (transferDetails.getTransferDescription() == null) {
                throw new RegistryException("Data transfer description cannot be empty");
            }
            TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
            taskDetails.getDataTransferDetailsList().add(transferDetails);
            experimentDao.updateTaskDetail(taskDetails);
            return taskId;
        } catch (Exception e) {
            logger.error("Error while adding transfer details...", e);
            throw new RegistryException(e);
        }
    }

    public String updateDataTransferDetails(DataTransferDetails transferDetails, CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetail = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (DataTransferDetails dtd : taskDetail.getDataTransferDetailsList()) {
                if (dtd.getTransferId().equals(ids.getSecondLevelIdentifier())) {
                    taskDetail.getDataTransferDetailsList().remove(dtd);
                    taskDetail.getDataTransferDetailsList().add(transferDetails);
                    experimentDao.updateTaskDetail(taskDetail);
                    return (String) ids.getSecondLevelIdentifier();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while updating transfer details...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param scheduling computational resource object
     * @param ids        contains expId and taskId, if it is an experiment, task id can be null
     * @return scheduling id
     */
    public String addComputationalResourceScheduling(ComputationalResourceScheduling scheduling, CompositeIdentifier ids) throws RegistryException {
        try {
            if (ids.getSecondLevelIdentifier() == null) {
                Experiment experiment = experimentDao.getExperiment((String) ids.getTopLevelIdentifier());
                experiment.getUserConfigurationData().setComputationalResourceScheduling(scheduling);
                experimentDao.updateExperiment(experiment);
                return (String) ids.getTopLevelIdentifier();
            } else {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getSecondLevelIdentifier());
                taskDetails.setTaskScheduling(scheduling);
                experimentDao.updateTaskDetail(taskDetails);
                return (String) ids.getSecondLevelIdentifier();
            }
        } catch (Exception e) {
            logger.error("Error while adding scheduling parameters...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param dataHandling advanced input data handling object
     * @param ids          contains expId and taskId
     * @return data handling id
     */
    public String addInputDataHandling(AdvancedInputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
        try {
            if (ids.getSecondLevelIdentifier() == null) {
                Experiment experiment = experimentDao.getExperiment((String) ids.getTopLevelIdentifier());
                experiment.getUserConfigurationData().setAdvanceInputDataHandling(dataHandling);
                experimentDao.updateExperiment(experiment);
                return (String) ids.getTopLevelIdentifier();
            } else {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getSecondLevelIdentifier());
                taskDetails.setAdvancedInputDataHandling(dataHandling);
                experimentDao.updateTaskDetail(taskDetails);
                return (String) ids.getSecondLevelIdentifier();
            }
        } catch (Exception e) {
            logger.error("Error while adding input data handling...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * @param dataHandling advanced output data handling object
     * @param ids          contains expId and taskId
     * @return data handling id
     */
    public String addOutputDataHandling(AdvancedOutputDataHandling dataHandling, CompositeIdentifier ids) throws RegistryException {
        try {
            if (ids.getSecondLevelIdentifier() == null) {
                Experiment experiment = experimentDao.getExperiment((String) ids.getTopLevelIdentifier());
                experiment.getUserConfigurationData().setAdvanceOutputDataHandling(dataHandling);
                experimentDao.updateExperiment(experiment);
                return (String) ids.getTopLevelIdentifier();
            } else {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getSecondLevelIdentifier());
                taskDetails.setAdvancedOutputDataHandling(dataHandling);
                experimentDao.updateTaskDetail(taskDetails);
                return (String) ids.getSecondLevelIdentifier();
            }
        } catch (Exception e) {
            logger.error("Error while adding output data handling...", e);
            throw new RegistryException(e);
        }
    }

    public String addQosParams(QualityOfServiceParams qosParams, String expId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            experiment.getUserConfigurationData().setQosParams(qosParams);
            experimentDao.updateExperiment(experiment);
            return expId;
        } catch (Exception e) {
            logger.error("Error while adding QOS params...", e);
            throw new RegistryException(e);
        }
    }

    public String addErrorDetails(ErrorDetails error, Object id) throws RegistryException {
        try {
            // FIXME : for .12 we only saveExperiment task related errors
            if(id instanceof String){
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) id);
                taskDetails.getErrors().add(error);
                experimentDao.updateTaskDetail(taskDetails);
                return (String) id;
            } else if (id instanceof CompositeIdentifier) {
                CompositeIdentifier cid = (CompositeIdentifier) id;
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) cid.getTopLevelIdentifier());
                for(JobDetails jobDetails: taskDetails.getJobDetailsList()){
                    if(jobDetails.getJobId().equals(cid.getSecondLevelIdentifier())){
                        jobDetails.getErrors().add(error);
                        experimentDao.updateTaskDetail(taskDetails);
                        return (String) cid.getSecondLevelIdentifier();
                    }
                }
            } else {
                logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Unable to add error details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public String getNodeInstanceID(String nodeName) {
        String node = nodeName.replaceAll("\\s", "");
        return node + "_" + UUID.randomUUID();
    }

    public String getExperimentId(String experimentName) {
        String exp = experimentName.replaceAll("\\s", "");
        return exp + "_" + UUID.randomUUID();
    }

    public String getTaskId(String nodeName) {
        String node = nodeName.replaceAll("\\s", "");
        return node + "_" + UUID.randomUUID();
    }

    public String getDataTransferID(String taskId) {
        String task = taskId.replaceAll("\\s", "");
        return task + "_" + UUID.randomUUID();
    }

    public void updateExperimentField(String expID, String fieldName, Object value) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expID);
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                experiment.setName((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                experiment.setUserName((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                experiment.setDescription((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                experiment.setApplicationId((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
                experiment.setApplicationVersion((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
                experiment.setWorkflowTemplateId((String) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
                experiment.setWorkflowTemplateVersion((String) value);
                experimentDao.updateExperiment(experiment);
            } else {
                logger.error("Unsupported field type for Experiment");
            }
        } catch (Exception e) {
            logger.error("Error while updating fields in experiment...", e);
            throw new RegistryException(e);
        }
    }

    public void updateExpConfigDataField(String expID, String fieldName, Object value) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expID);
            if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                experiment.getUserConfigurationData().setAiravataAutoSchedule((Boolean) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                experiment.getUserConfigurationData().setOverrideManualScheduledParams((Boolean) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
                experiment.getUserConfigurationData().setShareExperimentPublicly((Boolean) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants
                    .ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
                experiment.getUserConfigurationData()
                        .setComputationalResourceScheduling((ComputationalResourceScheduling) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)) {
                experiment.getUserConfigurationData()
                        .setAdvanceInputDataHandling((AdvancedInputDataHandling) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)) {
                experiment.getUserConfigurationData()
                        .setAdvanceOutputDataHandling((AdvancedOutputDataHandling) value);
                experimentDao.updateExperiment(experiment);
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)) {
                experiment.getUserConfigurationData()
                        .setQosParams((QualityOfServiceParams) value);
                experimentDao.updateExperiment(experiment);
            } else {
                logger.error("Unsupported field type for Experiment config data");
            }
        } catch (Exception e) {
            logger.error("Error while updating fields in experiment config...", e);
            throw new RegistryException(e);
        }
    }

    public void updateExperiment(Experiment experiment, String expId) throws RegistryException {
        try {
            if (!ResourceUtils.isUserExist(experiment.getUserName())) {
                ResourceUtils.addUser(experiment.getUserName(), null);
            }
            if (!workerResource.isProjectExists(experiment.getProjectId())) {
                logger.error("Project does not exist in the system..");
                throw new Exception("Project does not exist in the system, Please create the project first...");
            }
            //making sure id is set
            experiment.setExperimentId(expId);
            experimentDao.updateExperiment(experiment);
        } catch (Exception e) {
            logger.error("Error while updating experiment...", e);
            throw new RegistryException(e);
        }
    }

    public void updateUserConfigData(UserConfigurationData configData, String expId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            experiment.setUserConfigurationData(configData);
            experimentDao.updateExperiment(experiment);
            return;
        } catch (Exception e) {
            logger.error("Error while updating user config data...", e);
            throw new RegistryException(e);
        }
    }

    /**
     * Method to getExperiment matching experiment list
     *
     * @param fieldName
     * @param value
     * @return
     * @throws RegistryException
     */
    public List<Experiment> getExperimentList(String fieldName, Object value) throws RegistryException {
        return getExperimentList(fieldName, value, -1, -1, null, null);
    }

    /**
     * Method to getExperiment matching experiment list with pagination and ordering
     *
     * @param fieldName
     * @param value
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<Experiment> getExperimentList(String fieldName, Object value, int limit, int offset,
                                              Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<Experiment> experiments = new ArrayList();
        Map<String, String> filters = new HashMap();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                filters.put(fieldName, (String)value);
                return experimentDao.searchExperiments(filters, limit, offset, orderByIdentifier, resultOrderType);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                filters.put(fieldName, (String)value);
                return experimentDao.searchExperiments(filters, limit, offset, orderByIdentifier, resultOrderType);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                filters.put(fieldName, (String)value);
                return experimentDao.searchExperiments(filters, limit, offset, orderByIdentifier, resultOrderType);
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)) {
                if (value instanceof List<?>) {
                    return getExperimentList(fieldName, ((List<?>) value).get(0));
                } else if (value instanceof WorkflowNodeDetails) {
                    List<Experiment> experimentList = new ArrayList();
                    experimentList.add(experimentDao.getParentExperimentOfWFNode(
                            ((WorkflowNodeDetails)value).getNodeInstanceId())
                    );
                } else {
                    logger.error("Unsupported field value to retrieve workflow node detail list...");
                }

            } else {
                logger.error("Unsupported field name to retrieve experiment list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment list...", e);
            throw new RegistryException(e);
        }
        return experiments;
    }


    public List<WorkflowNodeDetails> getWFNodeDetails(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.WorkflowNodeConstants.EXPERIMENT_ID)) {
                Experiment experiment = experimentDao.getExperiment((String) value);
                return experiment.getWorkflowNodeDetailsList();
            }
            if (fieldName.equals(Constants.FieldConstants.WorkflowNodeConstants.TASK_LIST)) {
                if (value instanceof List<?>) {
                    return getWFNodeDetails(fieldName, ((List<?>) value).get(0));
                } else if (value instanceof TaskDetails) {
                    List<WorkflowNodeDetails> workflowNodeDetailsList = new ArrayList();
                    workflowNodeDetailsList.add(experimentDao.getParentWFNodeOfTask(((TaskDetails)value).getTaskId()));
                    return workflowNodeDetailsList;
                } else {
                    logger.error("Unsupported field value to retrieve workflow node detail list...");
                }
            } else {
                logger.error("Unsupported field name to retrieve workflow detail list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting workfkow details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<WorkflowNodeStatus> getWFNodeStatusList(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.WorkflowNodeStatusConstants.EXPERIMENT_ID)) {
                Experiment experiment = experimentDao.getExperiment((String) value);
                List<WorkflowNodeStatus> workflowNodeStatuses = experiment.getWorkflowNodeDetailsList().stream().map(WorkflowNodeDetails::getWorkflowNodeStatus).collect(Collectors.toList());
                return workflowNodeStatuses;
            } else {
                logger.error("Unsupported field name to retrieve workflow status list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting workflow status...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<TaskDetails> getTaskDetails(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.TaskDetailConstants.NODE_ID)) {
                WorkflowNodeDetails wfnd = experimentDao.getWFNode((String) value);
                return wfnd.getTaskDetailsList();
            } else {
                logger.error("Unsupported field name to retrieve task detail list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting task details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<JobDetails> getJobDetails(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.JobDetaisConstants.TASK_ID)) {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) value);
                return taskDetails.getJobDetailsList();
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Error while job details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<DataTransferDetails> getDataTransferDetails(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.DataTransferDetailConstants.TASK_ID)) {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) value);
                return taskDetails.getDataTransferDetailsList();
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Error while getting data transfer details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<ErrorDetails> getErrorDetails(String fieldName, Object value) throws RegistryException {
        try {
            if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.EXPERIMENT_ID)) {
                Experiment experiment = experimentDao.getExperiment((String) value);
                return experiment.getErrors();
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.NODE_ID)) {
                WorkflowNodeDetails wfnd = experimentDao.getWFNode((String) value);
                wfnd.getErrors();
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.TASK_ID)) {
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) value);
                return taskDetails.getErrors();
            } else if (fieldName.equals(Constants.FieldConstants.ErrorDetailsConstants.JOB_ID)) {
                CompositeIdentifier cid = (CompositeIdentifier) value;
                TaskDetails taskDetails = experimentDao.getTaskDetail((String) cid.getTopLevelIdentifier());
                for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                    if (jobDetails.getJobId().equals(cid.getSecondLevelIdentifier())) {
                        return jobDetails.getErrors();
                    }
                }
            } else {
                logger.error("Unsupported field name to retrieve job details list...");
            }
        } catch (Exception e) {
            logger.error("Unable to get error details...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getExperiment(String expId, String fieldName) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            if (fieldName == null) {
                return experiment;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)) {
                return experiment.getUserName();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)) {
                return experiment.getGatewayExecutionId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME)) {
                return experiment.getName();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC)) {
                return experiment.getDescription();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID)) {
                return experiment.getApplicationId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                return experiment.getProjectId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.APPLICATION_VERSION)) {
                return experiment.getApplicationVersion();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_ID)) {
                return experiment.getWorkflowTemplateId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_TEMPLATE_VERSION)) {
                return experiment.getWorkflowTemplateVersion();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_INPUTS)) {
                return experiment.getExperimentInputs();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_OUTPUTS)) {
                return experiment.getExperimentOutputs();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS)) {
                return experiment.getExperimentStatus();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_CONFIGURATION_DATA)) {
                return experiment.getUserConfigurationData();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_EXECUTION_ID)) {
                return experiment.getWorkflowExecutionInstanceId();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.STATE_CHANGE_LIST)) {
                final List<WorkflowNodeStatus> statusList = new ArrayList();
                        experiment.getWorkflowNodeDetailsList().stream()
                        .forEach(wfnd->statusList.add(wfnd.getWorkflowNodeStatus()));
                return statusList;
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.WORKFLOW_NODE_LIST)) {
                return experiment.getWorkflowNodeDetailsList();
            } else if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.ERROR_DETAIL_LIST)) {
                return experiment.getErrors();
            } else {
                logger.error("Unsupported field name for experiment basic data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting experiment info...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public Object getConfigData(String expId, String fieldName) throws RegistryException {
        try {
            UserConfigurationData configurationData = experimentDao.getExperiment(expId).getUserConfigurationData();
            if (fieldName == null) {
                return configurationData;
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.AIRAVATA_AUTO_SCHEDULE)) {
                return configurationData.isAiravataAutoSchedule();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.OVERRIDE_MANUAL_PARAMS)) {
                return configurationData.isOverrideManualScheduledParams();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.SHARE_EXP)) {
                return configurationData.isShareExperimentPublicly();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.COMPUTATIONAL_RESOURCE_SCHEDULING)) {
                return configurationData.getComputationalResourceScheduling();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_INPUT_HANDLING)) {
                return configurationData.getAdvanceInputDataHandling();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.ADVANCED_OUTPUT_HANDLING)) {
                return configurationData.getAdvanceOutputDataHandling();
            } else if (fieldName.equals(Constants.FieldConstants.ConfigurationDataConstants.QOS_PARAMS)) {
                return configurationData.getQosParams();
            } else {
                logger.error("Unsupported field name for experiment configuration data..");
            }
        } catch (Exception e) {
            logger.error("Error while getting config data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public List<OutputDataObjectType> getExperimentOutputs(String expId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            return experiment.getExperimentOutputs();
        } catch (Exception e) {
            logger.error("Error while getting experiment outputs...", e);
        }
        return null;
    }

    public ExperimentStatus getExperimentStatus(String expId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(expId);
            return experiment.getExperimentStatus();
        } catch (Exception e) {
            logger.error("Error while getting experiment status...", e);
            throw new RegistryException(e);
        }
    }

    public ComputationalResourceScheduling getComputationalScheduling(RegistryModelType type, String id) throws RegistryException {
        try {
            ComputationSchedulingResource computationScheduling = null;
            switch (type) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getComputationalResourceScheduling();
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getTaskScheduling();
            }
            if (computationScheduling != null) {
                return ThriftDataModelConversion.getComputationalResourceScheduling(computationScheduling);
            }
        } catch (Exception e) {
            logger.error("Error while getting scheduling data..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public AdvancedInputDataHandling getInputDataHandling(RegistryModelType type, String id) throws RegistryException {
        try {
            AdvanceInputDataHandlingResource dataHandlingResource = null;
            switch (type) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getAdvanceInputDataHandling();
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getAdvancedInputDataHandling();
            }
            if (dataHandlingResource != null) {
                return ThriftDataModelConversion.getAdvanceInputDataHandling(dataHandlingResource);
            }
        } catch (Exception e) {
            logger.error("Error while getting input data handling..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public AdvancedOutputDataHandling getOutputDataHandling(RegistryModelType type, String id) throws RegistryException {
        try {
            AdvancedOutputDataHandlingResource dataHandlingResource = null;
            switch (type) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getAdvanceOutputDataHandling();
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getAdvancedOutputDataHandling();
            }
            if (dataHandlingResource != null) {
                return ThriftDataModelConversion.getAdvanceOutputDataHandling(dataHandlingResource);
            }
        } catch (Exception e) {
            logger.error("Error while getting output data handling...", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public QualityOfServiceParams getQosParams(RegistryModelType type, String id) throws RegistryException {
        try {
            switch (type) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getQosParams();
            }
        } catch (Exception e) {
            logger.error("Error while getting qos params..", e);
            throw new RegistryException(e);
        }
        return null;
    }

    public WorkflowNodeDetails getWorkflowNodeDetails(String nodeId) throws RegistryException {
        try {
            return experimentDao.getWFNode(nodeId);
        } catch (Exception e) {
            logger.error("Error while getting workflow node details...", e);
            throw new RegistryException(e);
        }
    }

    public WorkflowNodeStatus getWorkflowNodeStatus(String nodeId) throws RegistryException {
        try {
            WorkflowNodeDetails wfnd = experimentDao.getWFNode(nodeId);
            return wfnd.getWorkflowNodeStatus();
        } catch (Exception e) {
            logger.error("Error while getting workflow node status..", e);
            throw new RegistryException(e);
        }
    }

    public List<OutputDataObjectType> getNodeOutputs(String nodeId) throws RegistryException {
        try {
            WorkflowNodeDetails wfnd = experimentDao.getWFNode(nodeId);
            return wfnd.getNodeOutputs();
        } catch (Exception e) {
            logger.error("Error while getting node outputs..", e);
            throw new RegistryException(e);
        }
    }

    public TaskDetails getTaskDetails(String taskId) throws RegistryException {
        try {
            return experimentDao.getTaskDetail(taskId);
        } catch (Exception e) {
            logger.error("Error while getting task details..", e);
            throw new RegistryException(e);
        }
    }

    public List<OutputDataObjectType> getApplicationOutputs(String taskId) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
            return taskDetails.getApplicationOutputs();
        } catch (Exception e) {
            logger.error("Error while getting application outputs..", e);
            throw new RegistryException(e);
        }
    }

    public TaskStatus getTaskStatus(String taskId) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail(taskId);
            return taskDetails.getTaskStatus();
        } catch (Exception e) {
            logger.error("Error while getting experiment outputs..", e);
            throw new RegistryException(e);
        }
    }

    // ids contains task id + job id
    public JobDetails getJobDetails(CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    return jobDetails;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while getting job details..", e);
            throw new RegistryException(e);
        }
    }

    // ids contains task id + job id
    public JobStatus getJobStatus(CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    return jobDetails.getJobStatus();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while getting job status..", e);
            throw new RegistryException(e);
        }
    }

    public ApplicationStatus getApplicationStatus(CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    return jobDetails.getApplicationStatus();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while getting application status..", e);
            throw new RegistryException(e);
        }
    }

    public DataTransferDetails getDataTransferDetails(CompositeIdentifier cid) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String)cid.getTopLevelIdentifier());
            for(DataTransferDetails dtd: taskDetails.getDataTransferDetailsList()){
                if(dtd.getTransferId().equals(cid.getSecondLevelIdentifier())){
                    return dtd;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while getting data transfer details..", e);
            throw new RegistryException(e);
        }
    }

    public TransferStatus getDataTransferStatus(CompositeIdentifier cid) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String)cid.getTopLevelIdentifier());
            for(DataTransferDetails dtd: taskDetails.getDataTransferDetailsList()){
                if(dtd.getTransferId().equals(cid.getSecondLevelIdentifier())){
                    return dtd.getTransferStatus();
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Error while getting data transfer status..", e);
            throw new RegistryException(e);
        }
    }

    public List<String> getExperimentIds(String fieldName, Object value) throws RegistryException {
        List<String> expIDs = new ArrayList();
        try {
            if (fieldName.equals(Constants.FieldConstants.ExperimentConstants.GATEWAY)
                        || fieldName.equals(Constants.FieldConstants.ExperimentConstants.USER_NAME)
                        || fieldName.equals(Constants.FieldConstants.ExperimentConstants.PROJECT_ID)) {
                getExperimentList(fieldName, value).stream().forEach(ex->expIDs.add(ex.getExperimentId()));
                return expIDs;
            }
        } catch (Exception e) {
            logger.error("Error while retrieving experiment ids..", e);
            throw new RegistryException(e);
        }
        return expIDs;
    }

    public List<String> getWorkflowNodeIds(String fieldName, Object value) throws RegistryException {
        List<String> wfIds = new ArrayList();
        List<WorkflowNodeDetails> wfNodeDetails = getWFNodeDetails(fieldName, value);
        wfIds.addAll(wfNodeDetails.stream().map(WorkflowNodeDetails::getNodeInstanceId).collect(Collectors.toList()));
        return wfIds;
    }

    public List<String> getTaskDetailIds(String fieldName, Object value) throws RegistryException {
        List<String> taskDetailIds = new ArrayList();
        List<TaskDetails> taskDetails = getTaskDetails(fieldName, value);
        taskDetailIds.addAll(taskDetails.stream().map(TaskDetails::getTaskId).collect(Collectors.toList()));
        return taskDetailIds;
    }

    public List<String> getJobDetailIds(String fieldName, Object value) throws RegistryException {
        List<String> jobIds = new ArrayList<String>();
        List<JobDetails> jobDetails = getJobDetails(fieldName, value);
        jobIds.addAll(jobDetails.stream().map(JobDetails::getJobId).collect(Collectors.toList()));
        return jobIds;
    }

    public List<String> getTransferDetailIds(String fieldName, Object value) throws RegistryException {
        List<String> transferIds = new ArrayList<String>();
        List<DataTransferDetails> dataTransferDetails = getDataTransferDetails(fieldName, value);
        transferIds.addAll(dataTransferDetails.stream().map(DataTransferDetails::getTransferId).collect(Collectors.toList()));
        return transferIds;
    }

    public void removeExperiment(String experimentId) throws RegistryException {
        try {
            Experiment experiment = new Experiment();
            experiment.setExperimentId(experimentId);
            experimentDao.deleteExperiment(experiment);
        } catch (Exception e) {
            logger.error("Error while removing experiment..", e);
            throw new RegistryException(e);
        }
    }

    public void removeExperimentConfigData(String experimentId) throws RegistryException {
        try {
            Experiment experiment = experimentDao.getExperiment(experimentId);
            experiment.setUserConfigurationData(null);
            experimentDao.updateExperiment(experiment);
        } catch (Exception e) {
            logger.error("Error while removing experiment config..", e);
            throw new RegistryException(e);
        }
    }

    public void removeWorkflowNode(String nodeId) throws RegistryException {
        try {
            WorkflowNodeDetails wfnd = new WorkflowNodeDetails();
            wfnd.setNodeInstanceId(nodeId);
            experimentDao.deleteWFNode(wfnd);
        } catch (Exception e) {
            logger.error("Error while removing workflow node..", e);
            throw new RegistryException(e);
        }
    }

    public void removeTaskDetails(String taskId) throws RegistryException {
        try {
            TaskDetails taskDetails = new TaskDetails();
            taskDetails.setTaskId(taskId);
            experimentDao.deleteTaskDetail(taskDetails);
        } catch (Exception e) {
            logger.error("Error while removing task details..", e);
            throw new RegistryException(e);
        }
    }

    public void removeJobDetails(CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    taskDetails.getJobDetailsList().remove(jobDetails);
                    experimentDao.updateTaskDetail(taskDetails);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error while removing job details..", e);
            throw new RegistryException(e);
        }
    }

    public void removeDataTransferDetails(CompositeIdentifier cid) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String)cid.getTopLevelIdentifier());
            for(DataTransferDetails dtd: taskDetails.getDataTransferDetailsList()){
                if(dtd.getTransferId().equals(cid.getSecondLevelIdentifier())){
                    taskDetails.getDataTransferDetailsList().remove(dtd);
                    experimentDao.updateTaskDetail(taskDetails);
                }
            }
        } catch (Exception e) {
            logger.error("Error while removing transfer details..", e);
            throw new RegistryException(e);
        }
    }

    public void removeComputationalScheduling(RegistryModelType dataType, String id) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    experiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                    experimentDao.updateExperiment(experiment);
                    return;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    taskDetails.setTaskScheduling(null);
                    experimentDao.updateTaskDetail(taskDetails);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing scheduling data..", e);
            throw new RegistryException(e);
        }
    }

    public void removeInputDataHandling(RegistryModelType dataType, String id) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    experiment.getUserConfigurationData().setAdvanceInputDataHandling(null);
                    experimentDao.updateExperiment(experiment);
                    break;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    taskDetails.setAdvancedInputDataHandling(null);
                    experimentDao.updateTaskDetail(taskDetails);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing input data handling..", e);
            throw new RegistryException(e);
        }
    }

    public void removeOutputDataHandling(RegistryModelType dataType, String id) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    experiment.getUserConfigurationData().setAdvanceOutputDataHandling(null);
                    experimentDao.updateExperiment(experiment);
                    break;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    taskDetails.setAdvancedOutputDataHandling(null);
                    experimentDao.updateTaskDetail(taskDetails);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing output data handling..", e);
            throw new RegistryException(e);
        }
    }

    public void removeQOSParams(RegistryModelType dataType, String id) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    Experiment experiment = experimentDao.getExperiment(id);
                    experiment.getUserConfigurationData().setQosParams(null);
                    experimentDao.updateExperiment(experiment);
                    break;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while removing QOS params", e);
            throw new RegistryException(e);
        }
    }

    public boolean isExperimentExist(String expID) throws RegistryException {
        try {
            return experimentDao.getExperiment(expID) != null;
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isExperimentConfigDataExist(String expID) throws RegistryException {
        try {
            return experimentDao.getExperiment(expID).getUserConfigurationData() != null;
        } catch (Exception e) {
            logger.error("Error while retrieving experiment...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isWFNodeExist(String nodeId) throws RegistryException {
        try {
            return experimentDao.getWFNode(nodeId) != null;
        } catch (Exception e) {
            logger.error("Error while retrieving workflow...", e);
            throw new RegistryException(e);
        }
    }

    public boolean isTaskDetailExist(String taskId) throws RegistryException {
        try {
            return experimentDao.getTaskDetail(taskId) != null;
        } catch (Exception e) {
            logger.error("Error while retrieving task.....", e);
            throw new RegistryException(e);
        }
    }

    public boolean isJobDetailExist(CompositeIdentifier ids) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) ids.getTopLevelIdentifier());
            for (JobDetails jobDetails : taskDetails.getJobDetailsList()) {
                if (jobDetails.getJobId().equals(ids.getSecondLevelIdentifier())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error while retrieving job details.....", e);
            throw new RegistryException(e);
        }
    }

    public boolean isTransferDetailExist(CompositeIdentifier cid) throws RegistryException {
        try {
            TaskDetails taskDetails = experimentDao.getTaskDetail((String) cid.getTopLevelIdentifier());
            for (DataTransferDetails dtd : taskDetails.getDataTransferDetailsList()) {
                if (dtd.getTransferId().equals(cid.getSecondLevelIdentifier())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error while retrieving transfer details.....", e);
            throw new RegistryException(e);
        }
    }

    public boolean isComputationalSchedulingExist(RegistryModelType dataType, String id) throws RegistryException {
        try {
            Experiment experiment;
            switch (dataType) {
                case EXPERIMENT:
                    experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getComputationalResourceScheduling() != null;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getTaskScheduling() != null;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving scheduling data.....", e);
            throw new RegistryException(e);
        }
        return false;
    }

    public boolean isInputDataHandlingExist(RegistryModelType dataType, String id) throws RegistryException {
        try {
            Experiment experiment;
            switch (dataType) {
                case EXPERIMENT:
                    experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getAdvanceInputDataHandling() != null;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getAdvancedInputDataHandling() != null;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving input data handling.....", e);
            throw new RegistryException(e);
        }
        return false;
    }

    public boolean isOutputDataHandlingExist(RegistryModelType dataType, String id) throws RegistryException {
        try {
            Experiment experiment;
            switch (dataType) {
                case EXPERIMENT:
                    experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getAdvanceOutputDataHandling() != null;
                case TASK_DETAIL:
                    TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                    return taskDetails.getAdvancedOutputDataHandling() != null;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving output data handling..", e);
            throw new RegistryException(e);
        }
        return false;
    }

    public boolean isQOSParamsExist(RegistryModelType dataType, String id) throws RegistryException {
        try {
            Experiment experiment;
            switch (dataType) {
                case EXPERIMENT:
                    experiment = experimentDao.getExperiment(id);
                    return experiment.getUserConfigurationData().getQosParams() != null;
                default:
                    logger.error("Unsupported data type...");
            }
        } catch (Exception e) {
            logger.error("Error while retrieving qos params..", e);
            throw new RegistryException(e);
        }
        return false;
    }

    public void updateScheduling(ComputationalResourceScheduling scheduling, String id, String type) throws RegistryException {
        try {
            if (type.equals(RegistryModelType.EXPERIMENT.toString())) {
                Experiment experiment = experimentDao.getExperiment(id);
                experiment.getUserConfigurationData().setComputationalResourceScheduling(scheduling);
                experimentDao.updateExperiment(experiment);
                return;
            } else if (type.equals(RegistryModelType.TASK_DETAIL.toString())) {
                TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                taskDetails.setTaskScheduling(scheduling);
                experimentDao.updateTaskDetail(taskDetails);
                return;
            }
        } catch (Exception e) {
            logger.error("Error while updating scheduling..", e);
            throw new RegistryException(e);
        }
    }

    public void updateInputDataHandling(AdvancedInputDataHandling dataHandling, String id, String type) throws RegistryException {
        try {
            if (type.equals(RegistryModelType.EXPERIMENT.toString())) {
                Experiment experiment = experimentDao.getExperiment(id);
                experiment.getUserConfigurationData().setAdvanceInputDataHandling(dataHandling);
                experimentDao.updateExperiment(experiment);
                return;
            } else if (type.equals(RegistryModelType.TASK_DETAIL.toString())) {
                TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                taskDetails.setAdvancedInputDataHandling(dataHandling);
                experimentDao.updateTaskDetail(taskDetails);
                return;
            }
        } catch (Exception e) {
            logger.error("Error while updating input data handling..", e);
            throw new RegistryException(e);
        }
    }

    public void updateOutputDataHandling(AdvancedOutputDataHandling dataHandling, String id, String type) throws RegistryException {
        try {
            if (type.equals(RegistryModelType.EXPERIMENT.toString())) {
                Experiment experiment = experimentDao.getExperiment(id);
                experiment.getUserConfigurationData().setAdvanceOutputDataHandling(dataHandling);
                experimentDao.updateExperiment(experiment);
                return;
            } else if (type.equals(RegistryModelType.TASK_DETAIL.toString())) {
                TaskDetails taskDetails = experimentDao.getTaskDetail(id);
                taskDetails.setAdvancedOutputDataHandling(dataHandling);
                experimentDao.updateTaskDetail(taskDetails);
                return;
            }
        } catch (Exception e) {
            logger.error("Error while updating output data handling", e);
            throw new RegistryException(e);
        }
    }

    public void updateQOSParams(QualityOfServiceParams params, String id, String type) throws RegistryException {
        try {
            if (type.equals(RegistryModelType.EXPERIMENT.toString())) {
                Experiment experiment = experimentDao.getExperiment(id);
                experiment.getUserConfigurationData().setQosParams(params);
                experimentDao.updateExperiment(experiment);
            } else {
                logger.error("Unsupported data type... " + type);
            }
        } catch (Exception e) {
            logger.error("Error while updating QOS data..", e);
            throw new RegistryException(e);
        }
    }

    /**
     * To search the experiments of user with the given filter criteria and retrieve the results with
     * pagination support. Results can be ordered based on an identifier (i.e column) either ASC or
     * DESC.
     *
     * @param filters
     * @param limit
     * @param offset
     * @param orderByIdentifier
     * @param resultOrderType
     * @return
     * @throws RegistryException
     */
    public List<ExperimentSummary> searchExperiments(Map<String, String> filters, int limit,
            int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        List<Experiment> experimentList = experimentDao.searchExperiments(
                filters, limit, offset, orderByIdentifier, resultOrderType);
        List<ExperimentSummary> experimentSummaries = new ArrayList();
        experimentList.stream().forEach(experiment->{
            ExperimentSummary expSummary = new ExperimentSummary();
            expSummary.setExperimentId(experiment.getExperimentId());
            expSummary.setProjectId(experiment.getProjectId());
            expSummary.setName(experiment.getName());
            expSummary.setDescription(experiment.getDescription());
            expSummary.setUserName(experiment.getUserName());
            expSummary.setCreationTime(experiment.getCreationTime());
            expSummary.setApplicationId(experiment.getApplicationId());
            expSummary.setExperimentStatus(experiment.getExperimentStatus());
            expSummary.setErrors(experiment.getErrors());
            experimentSummaries.add(expSummary);
        });
        return experimentSummaries;
    }

    private boolean isValidStatusTransition(ExperimentState oldState, ExperimentState nextState) {
        if (nextState == null) {
            return false;
        }
        switch (oldState) {
            case CREATED:
                return true;
            case VALIDATED:
                return nextState != ExperimentState.CREATED;
            case SCHEDULED:
                return nextState != ExperimentState.CREATED
                        || nextState != ExperimentState.VALIDATED;
            case LAUNCHED:
                return nextState != ExperimentState.CREATED
                        || nextState != ExperimentState.VALIDATED
                        || nextState != ExperimentState.SCHEDULED;
            case EXECUTING:
                return nextState != ExperimentState.CREATED
                        || nextState != ExperimentState.VALIDATED
                        || nextState != ExperimentState.SCHEDULED
                        || nextState != ExperimentState.LAUNCHED;

            case CANCELING:
                return nextState == ExperimentState.CANCELING
                        || nextState == ExperimentState.CANCELED
                        || nextState == ExperimentState.COMPLETED
                        || nextState == ExperimentState.FAILED;
            case CANCELED:
                return nextState == ExperimentState.CANCELED;
            case COMPLETED:
                return nextState == ExperimentState.COMPLETED;
            case FAILED:
                return nextState == ExperimentState.FAILED;
            //case SUSPENDED:  // We don't change state to SUSPEND
            case UNKNOWN:
                return true;
            default:
                return false;
        }
    }
}