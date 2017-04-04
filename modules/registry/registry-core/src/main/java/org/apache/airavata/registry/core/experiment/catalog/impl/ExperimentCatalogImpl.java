/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentSummaryModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.resources.GatewayResource;
import org.apache.airavata.registry.core.experiment.catalog.resources.UserResource;
import org.apache.airavata.registry.cpi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExperimentCatalogImpl implements ExperimentCatalog {
    private GatewayResource gatewayResource;
    private UserResource user;
    private final static Logger logger = LoggerFactory.getLogger(ExperimentCatalogImpl.class);
    private ExperimentRegistry experimentRegistry = null;
    private ProjectRegistry projectRegistry = null;
    private GatewayRegistry gatewayRegistry = null;
    private NotificationRegistry notificationRegistry = null;

    public ExperimentCatalogImpl() throws RegistryException{
        try {
            if (!ExpCatResourceUtils.isGatewayExist(ServerSettings.getDefaultUserGateway())){
                gatewayResource = (GatewayResource) ExpCatResourceUtils.createGateway(ServerSettings.getDefaultUserGateway());
                gatewayResource.setGatewayName(ServerSettings.getDefaultUserGateway());
                gatewayResource.save();
            }else {
                gatewayResource = (GatewayResource) ExpCatResourceUtils.getGateway(ServerSettings.getDefaultUserGateway());
            }

            if (!ExpCatResourceUtils.isUserExist(ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserGateway())){
                user = ExpCatResourceUtils.createUser(ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserPassword(), ServerSettings.getDefaultUserGateway());
                user.save();
            }else {
                user = (UserResource) ExpCatResourceUtils.getUser(ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserGateway());
            }
            experimentRegistry = new ExperimentRegistry(gatewayResource, user);
            projectRegistry = new ProjectRegistry(gatewayResource, user);
            gatewayRegistry = new GatewayRegistry();
            notificationRegistry = new NotificationRegistry();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata server properties..", e);
            throw new RegistryException("Unable to read airavata server properties..", e);
        }
    }

    public ExperimentCatalogImpl(String gateway, String username, String password) throws RegistryException{
        if (!ExpCatResourceUtils.isGatewayExist(gateway)){
            gatewayResource = (GatewayResource) ExpCatResourceUtils.createGateway(gateway);
            gatewayResource.setGatewayName(gateway);
            gatewayResource.save();
        }else {
            gatewayResource = (GatewayResource) ExpCatResourceUtils.getGateway(gateway);
        }

        if (!ExpCatResourceUtils.isUserExist(username, gatewayResource.getGatewayId())){
            user = ExpCatResourceUtils.createUser(username, password, gateway);
            user.save();
        }else {
            user = (UserResource) ExpCatResourceUtils.getUser(username, gateway);
        }
        experimentRegistry = new ExperimentRegistry(gatewayResource, user);
        projectRegistry = new ProjectRegistry(gatewayResource, user);
        gatewayRegistry = new GatewayRegistry();
        notificationRegistry = new NotificationRegistry();
    }

    /**
     * This method is to add an object in to the registry
     *
     * @param dataType       Data type is a predefined type which the programmer should choose according to the object he
     *                       is going to save in to registry
     * @param newObjectToAdd Object which contains the fields that need to be saved in to registry. This object is a
     *                       thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                       etc
     * @return return the identifier to identify the object
     */
    @Override
    public Object add(ExpCatParentDataType dataType, Object newObjectToAdd, String gatewayId) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.addProject((Project)newObjectToAdd, gatewayId);
                case EXPERIMENT:
                    return experimentRegistry.addExperiment((ExperimentModel) newObjectToAdd);
                case GATEWAY:
                    return gatewayRegistry.addGateway((Gateway)newObjectToAdd);
                case NOTIFICATION:
                    return notificationRegistry.createNotification((Notification)newObjectToAdd);
                case QUEUE_STATUS:
                    return experimentRegistry.createQueueStatuses((List<QueueStatusModel>) newObjectToAdd);
                default:
                    logger.error("Unsupported top level type..", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while adding the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while adding the resource " + dataType.toString(), e);
        }
    }

    /**
     * This method is to add an object in to the registry
     *
     * @param dataType            Data type is a predefined type which the programmer should choose according to the object he
     *                            is going to save in to registry
     * @param newObjectToAdd      Object which contains the fields that need to be saved in to registry. This object is a
     *                            thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                            etc
     * @param dependentIdentifier Object which contains the identifier if the object that is going to add is not a top
     *                            level object in the data model. If it is a top level object, programmer can pass it as
     *                            null
     */
    @Override
    public Object add(ExpCatChildDataType dataType, Object newObjectToAdd, Object dependentIdentifier) throws RegistryException {
        try {
            switch (dataType) {
                case USER_CONFIGURATION_DATA:
                    return experimentRegistry.addUserConfigData((UserConfigurationDataModel) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_INPUT:
                    return experimentRegistry.addExpInputs((List<InputDataObjectType>) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_OUTPUT:
                    return experimentRegistry.addExpOutputs((List<OutputDataObjectType>) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_STATUS:
                    return experimentRegistry.addExperimentStatus((ExperimentStatus) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_ERROR:
                    return experimentRegistry.addExperimentError((ErrorModel) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS:
                    return experimentRegistry.addProcess((ProcessModel) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS_RESOURCE_SCHEDULE:
                    return experimentRegistry.addProcessResourceSchedule((ComputationalResourceSchedulingModel) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS_INPUT:
                    return experimentRegistry.addProcessInputs((List<InputDataObjectType>) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS_OUTPUT:
                    return experimentRegistry.addProcessOutputs((List<OutputDataObjectType>) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS_STATUS:
                    return experimentRegistry.addProcessStatus((ProcessStatus) newObjectToAdd, (String) dependentIdentifier);
                case PROCESS_ERROR:
                    return experimentRegistry.addProcessError((ErrorModel) newObjectToAdd, (String) dependentIdentifier);
                case TASK:
                    return experimentRegistry.addTask((TaskModel) newObjectToAdd, (String) dependentIdentifier);
                case TASK_STATUS:
                    return experimentRegistry.addTaskStatus((TaskStatus) newObjectToAdd, (String) dependentIdentifier);
                case TASK_ERROR:
                    return experimentRegistry.addTaskError((ErrorModel) newObjectToAdd, (String) dependentIdentifier);
                case JOB:
                    return experimentRegistry.addJob((JobModel) newObjectToAdd, (String) dependentIdentifier);
                case JOB_STATUS:
                    return experimentRegistry.addJobStatus((JobStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                default:
                    logger.error("Unsupported dependent data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while adding " + dataType.toString() , new RegistryException(e));
            throw new RegistryException("Error while adding " + dataType.toString(), e);
        }

    }

    /**
     * This method is to update the whole object in registry
     *
     * @param dataType          Data type is a predefined type which the programmer should choose according to the object he
     *                          is going to save in to registry
     * @param newObjectToUpdate Object which contains the fields that need to be updated in to registry. This object is a
     *                          thrift model object. In experiment case this object can be BasicMetadata, ConfigurationData
     *                          etc. CPI programmer can only fill necessary fields that need to be updated. He does not
     *                          have to fill the whole object. He needs to only fill the mandatory fields and whatever the
     *                          other fields that need to be updated.
     */
    @Override
    public void update(ExperimentCatalogModelType dataType, Object newObjectToUpdate, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    projectRegistry.updateProject((Project)newObjectToUpdate, (String)identifier);
                    break;
                case GATEWAY:
                    gatewayRegistry.updateGateway((String)identifier, (Gateway)newObjectToUpdate);
                    break;
                case NOTIFICATION:
                    notificationRegistry.updateNotification((Notification)newObjectToUpdate);
                    break;
                case EXPERIMENT:
                    experimentRegistry.updateExperiment((ExperimentModel) newObjectToUpdate, (String) identifier);
                    break;
                case USER_CONFIGURATION_DATA:
                    experimentRegistry.updateUserConfigData((UserConfigurationDataModel) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_INPUT:
                    experimentRegistry.updateExpInputs((List<InputDataObjectType>) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_OUTPUT:
                    experimentRegistry.updateExpOutputs((List<OutputDataObjectType>) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_STATUS:
                    experimentRegistry.updateExperimentStatus((ExperimentStatus) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_ERROR:
                    experimentRegistry.updateExperimentError((ErrorModel) newObjectToUpdate, (String) identifier);
                    break;
                case PROCESS:
                    experimentRegistry.updateProcess((ProcessModel) newObjectToUpdate, (String) identifier);
                    break;
                case PROCESS_RESOURCE_SCHEDULE:
                    experimentRegistry.updateProcessResourceSchedule((ComputationalResourceSchedulingModel) newObjectToUpdate, (String) identifier);
                    break;
                case PROCESS_STATUS:
                    experimentRegistry.updateProcessStatus((ProcessStatus) newObjectToUpdate, (String) identifier);
                    break;
                case PROCESS_ERROR:
                    experimentRegistry.updateProcessError((ErrorModel) newObjectToUpdate, (String) identifier);
                    break;
                case TASK:
                    experimentRegistry.updateTask((TaskModel) newObjectToUpdate, (String) identifier);
                    break;
                case TASK_STATUS:
                    experimentRegistry.updateTaskStatus((TaskStatus) newObjectToUpdate, (String) identifier);
                    break;
                case TASK_ERROR:
                    experimentRegistry.updateTaskError((ErrorModel) newObjectToUpdate, (String) identifier);
                    break;
                case JOB:
                    experimentRegistry.updateJob((JobModel) newObjectToUpdate, (CompositeIdentifier) identifier);
                    break;
                case JOB_STATUS:
                    experimentRegistry.updateJobStatus((JobStatus) newObjectToUpdate, (CompositeIdentifier) identifier);
                    break;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while updating the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while updating the resource.." + dataType.toString(), e);
        }
    }

    /**
     * This method is to update a specific field of the data model
     *
     * @param dataType   Data type is a predefined type which the programmer should choose according to the object he
     *                   is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param fieldName  Field which need to be updated in the registry. In Experiment_Basic_Type, if you want to update the
     *                   description, field will be "description". Field names are defined in
     *                   org.apache.airavata.registry.cpi.utils.Constants
     * @param value      Value by which the given field need to be updated. If the field is "description", that field will be
     *                   updated by given value
     */
    @Override
    public void update(ExperimentCatalogModelType dataType, Object identifier, String fieldName, Object value) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    experimentRegistry.updateExperimentField((String) identifier, fieldName, value);
                    break;
                case USER_CONFIGURATION_DATA:
                    experimentRegistry.updateUserConfigDataField((String) identifier, fieldName, value);
                    break;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while updating the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while updating the resource " + dataType.toString(), e);
        }

    }

    /**
     * This method is to retrieve object according to the identifier. In the experiment basic data type, if you give the
     * experiment id, this method will return the BasicMetadata object
     *
     * @param dataType   Data type is a predefined type which the programmer should choose according to the object he
     *                   is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return object according to the given identifier.
     */
    @Override
    public Object get(ExperimentCatalogModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.getProject((String)identifier);
                case GATEWAY:
                    return gatewayRegistry.getGateway((String)identifier);
                case NOTIFICATION:
                    return notificationRegistry.getNotification((String) identifier);
                case EXPERIMENT:
                    return experimentRegistry.getExperiment((String) identifier, null);
                case USER_CONFIGURATION_DATA:
                    return experimentRegistry.getUserConfigData((String) identifier, null);
                case EXPERIMENT_INPUT:
                    return experimentRegistry.getExperimentInputs((String) identifier);
                case EXPERIMENT_OUTPUT:
                    return experimentRegistry.getExperimentOutputs((String) identifier);
                case EXPERIMENT_STATUS:
                    return experimentRegistry.getExperimentStatus((String) identifier);
                case EXPERIMENT_ERROR:
                    return experimentRegistry.getExperimentErrors((String) identifier);
                case PROCESS:
                    return experimentRegistry.getProcess((String) identifier, null);
                case PROCESS_RESOURCE_SCHEDULE:
                    return experimentRegistry.getProcessResourceSchedule((String) identifier);
                case PROCESS_INPUT:
                    return experimentRegistry.getProcessInputs((String) identifier);
                case PROCESS_OUTPUT:
                    return experimentRegistry.getProcessOutputs((String) identifier);
                case PROCESS_STATUS:
                    return experimentRegistry.getProcessStatus((String) identifier);
                case PROCESS_ERROR:
                    return experimentRegistry.getProcessError((String) identifier);
                case TASK:
                    return experimentRegistry.getTask((String) identifier, null);
                case TASK_STATUS:
                    return experimentRegistry.getTaskStatus((String) identifier);
                case TASK_ERROR:
                    return experimentRegistry.getTaskError((String) identifier);
                case JOB:
                    return experimentRegistry.getJob((CompositeIdentifier) identifier, null);
                case JOB_STATUS:
                    return experimentRegistry.getJobStatus((CompositeIdentifier) identifier);
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString() , e);
        }
    }

    /**
     * This method is to retrieve list of objects according to a given criteria
     *
     * @param dataType  Data type is a predefined type which the programmer should choose according to the object he
     *                  is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                  the experiments for a given user, filterBy will be "userName"
     * @param value     value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> get(ExperimentCatalogModelType dataType, String fieldName, Object value) throws RegistryException {
        try {
            List<Object> result = new ArrayList<Object>();
            switch (dataType) {
                case PROJECT:
                    List<Project> projectList = projectRegistry.getProjectList(fieldName, value);
                    for (Project project : projectList ){
                        result.add(project);
                    }
                    return result;
                case GATEWAY:
                    List<Gateway> allGateways = gatewayRegistry.getAllGateways();
                    for (Gateway gateway : allGateways){
                        result.add(gateway);
                    }
                    return result;
                case NOTIFICATION:
                    List<Notification> notifications = notificationRegistry.getAllGatewayNotifications((String) value);
                    for(Notification n : notifications)
                        result.add(n);
                    return result;
                case EXPERIMENT:
                    List<ExperimentModel> experimentList = experimentRegistry.getExperimentList(fieldName, value);
                    for (ExperimentModel experiment : experimentList) {
                        result.add(experiment);
                    }
                    return result;
                case PROCESS:
                    List<ProcessModel> processList = experimentRegistry.getProcessList(fieldName, value);
                    for (ProcessModel process : processList) {
                        result.add(process);
                    }
                    return result;
                case TASK:
                    List<TaskModel> taskList = experimentRegistry.getTaskList(fieldName, value);
                    for (TaskModel task : taskList) {
                        result.add(task);
                    }
                    return result;
                case JOB:
                    List<JobModel> jobList = experimentRegistry.getJobList(fieldName, value);
                    for (JobModel task : jobList) {
                        result.add(task);
                    }
                    return result;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString(), e);
        }

    }

    /**
     * This method is to retrieve list of objects according to a given criteria with pagination and ordering
     *
     * @param dataType  Data type is a predefined type which the programmer should choose according to the object he
     *                  is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                  the experiments for a given user, filterBy will be "userName"
     * @param value     value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @param limit     Size of the results to be returned
     * @param offset    Start position of the results to be retrieved
     * @param orderByIdentifier     Named of the column in which the ordering is based
     * @param resultOrderType       Type of ordering i.e ASC or DESC
     * @return
     * @throws RegistryException
     */
    @Override
    public List<Object> get(ExperimentCatalogModelType dataType, String fieldName, Object value, int limit,
                            int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        try {
            List<Object> result = new ArrayList<Object>();
            switch (dataType) {
                case PROJECT:
                    List<Project> projectList = projectRegistry
                            .getProjectList(fieldName, value, limit, offset, orderByIdentifier, resultOrderType);
                    result.addAll(projectList.stream().collect(Collectors.toList()));
                    return result;
                case EXPERIMENT:
                    List<ExperimentModel> experimentList = experimentRegistry.getExperimentList(fieldName, value,
                            limit, offset, orderByIdentifier, resultOrderType);
                    result.addAll(experimentList.stream().collect(Collectors.toList()));
                    return result;
                case QUEUE_STATUS:
                    List<QueueStatusModel> queueStatusModelsList = experimentRegistry.getLatestQueueStatuses();
                    result.addAll(queueStatusModelsList.stream().collect(Collectors.toList()));
                    return result;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString(), e);
        }
    }

    /**
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filters filters is a map of field name and value that you need to use for search filtration
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> search(ExperimentCatalogModelType dataType, Map<String, String> filters) throws RegistryException {
        return search(dataType, filters, -1, -1, null, null);
    }

    /**
     * This method is to retrieve list of objects with pagination according to a given criteria sorted
     * according by the specified  identified and specified ordering (i.e either ASC or DESC)
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filters            filters is a map of field name and value that you need to use for search filtration
     * @param limit              amount of the results to be returned
     * @param offset             offset of the results from the sorted list to be fetched from
     * @param orderByIdentifier  identifier (i.e the column) which will be used as the basis to sort the results
     * @param resultOrderType    The type of ordering (i.e ASC or DESC) that has to be used when retrieving the results
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> search(ExperimentCatalogModelType dataType, Map<String, String> filters, int limit,
        int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        try {
            List<Object> result = new ArrayList<Object>();
            switch (dataType) {
                case PROJECT:
                    List<Project> projectList
                            = projectRegistry.searchProjects(filters, limit, offset,
                            orderByIdentifier, resultOrderType);
                    for (Project project : projectList ){
                        result.add(project);
                    }
                    return result;
                case EXPERIMENT:
                    List<ExperimentSummaryModel> experimentSummaries = experimentRegistry
                            .searchExperiments(filters, limit, offset, orderByIdentifier,
                                    resultOrderType);
                    for (ExperimentSummaryModel ex : experimentSummaries){
                        result.add(ex);
                    }
                    return result;
                case EXPERIMENT_STATISTICS:
                    result.add(experimentRegistry.getExperimentStatistics(filters));
                    return result;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString(), e);
        }
    }

    /**
     * This method search all the accessible resources given the set of ids of all accessible resource IDs.
     *
     * @param dataType          Data type is a predefined type which the programmer should choose according to the object he
     *                          is going to save in to registry
     * @param accessibleIds     list of string IDs of all accessible resources
     * @param filters           filters is a map of field name and value that you need to use for search filtration
     * @param limit             amount of the results to be returned
     * @param offset            offset of the results from the sorted list to be fetched from
     * @param orderByIdentifier identifier (i.e the column) which will be used as the basis to sort the results
     * @param resultOrderType   The type of ordering (i.e ASC or DESC) that has to be used when retrieving the results
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> searchAllAccessible(ExperimentCatalogModelType dataType, List<String> accessibleIds, Map<String,
            String> filters, int limit, int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        try {
            List<Object> result = new ArrayList<Object>();
            switch (dataType) {
                case PROJECT:
                    List<Project> projectList
                            = projectRegistry.searchAllAccessibleProjects(accessibleIds, filters, limit, offset,
                            orderByIdentifier, resultOrderType);
                    for (Project project : projectList ){
                        result.add(project);
                    }
                    return result;
                case EXPERIMENT:
                    List<ExperimentSummaryModel> experimentSummaries = experimentRegistry
                            .searchAllAccessibleExperiments(accessibleIds, filters, limit, offset, orderByIdentifier,
                                    resultOrderType);
                    for (ExperimentSummaryModel ex : experimentSummaries){
                        result.add(ex);
                    }
                    return result;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString(), e);
        }
    }

    /**
     * This method is to retrieve a specific value for a given field.
     *
     * @param dataType   Data type is a predefined type which the programmer should choose according to the object he
     *                   is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @param field      field that filtering should be done. For example, if we want to execution user for a given
     *                   experiment, field will be "userName"
     * @return return the value for the specific field where data model is identified by the unique identifier that has
     * given
     */
    @Override
    public Object getValue(ExperimentCatalogModelType dataType, Object identifier, String field) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    return experimentRegistry.getExperiment((String) identifier, field);
                case USER_CONFIGURATION_DATA:
                    return experimentRegistry.getUserConfigData((String) identifier, field);
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the resource " + dataType.toString(), e);
        }

    }

    /**
     * This method is to retrieve all the identifiers according to given filtering criteria. For an example, if you want
     * to get all the experiment ids for a given gateway, your field name will be "gateway" and the value will be the
     * name of the gateway ("default"). Similar manner you can retrieve all the experiment ids for a given user.
     *
     * @param dataType  Data type is a predefined type which the programmer should choose according to the object he
     *                  is going to save in to registry
     * @param fieldName FieldName is the field that filtering should be done. For example, if we want to retrieve all
     *                  the experiments for a given user, filterBy will be "userName"
     * @param value     value for the filtering field. In the experiment case, value for "userName" can be "admin"
     * @return id list according to the filtering criteria
     */
    @Override
    public List<String> getIds(ExperimentCatalogModelType dataType, String fieldName, Object value) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.getProjectIDs(fieldName, value);
                case EXPERIMENT:
                    return experimentRegistry.getExperimentIDs(fieldName, value);
                case PROCESS:
                    return experimentRegistry.getProcessIds(fieldName, value);
                case TASK:
                    return experimentRegistry.getTaskIds(fieldName, value);
                case JOB:
                    return experimentRegistry.getJobIds(fieldName, value);
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while retrieving the ids for" + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while retrieving the ids for " + dataType.toString(), e);
        }

    }

    /**
     * This method is to remove a item from the registry
     *
     * @param dataType   Data type is a predefined type which the programmer should choose according to the object he
     *                   is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     */
    @Override
    public void remove(ExperimentCatalogModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    projectRegistry.removeProject((String)identifier);
                    break;
                case GATEWAY:
                    gatewayRegistry.removeGateway((String)identifier);
                    break;
                case NOTIFICATION:
                    notificationRegistry.deleteNotification((String)identifier);
                    break;
                case EXPERIMENT:
                    experimentRegistry.removeExperiment((String) identifier);
                    break;
                case USER_CONFIGURATION_DATA:
                    experimentRegistry.removeUserConfigData((String) identifier);
                    break;
                case PROCESS:
                    experimentRegistry.removeProcess((String) identifier);
                    break;
                case PROCESS_RESOURCE_SCHEDULE:
                    experimentRegistry.removeProcessResourceSchedule((String) identifier);
                    break;
                case TASK:
                    experimentRegistry.removeTask((String) identifier);
                    break;
                case JOB:
                    experimentRegistry.removeJob((CompositeIdentifier) identifier);
                    break;
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while removing the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while removing the resource " + dataType.toString(), e);
        }

    }

    /**
     * This method will check whether a given data type which can be identified with the identifier exists or not
     *
     * @param dataType   Data type is a predefined type which the programmer should choose according to the object he
     *                   is going to save in to registry
     * @param identifier Identifier which will uniquely identify the data model. For example, in Experiment_Basic_Type,
     *                   identifier will be generated experimentID
     * @return whether the given data type exists or not
     */
    @Override
    public boolean isExist(ExperimentCatalogModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.isProjectExist((String) identifier);
                case GATEWAY:
                    return gatewayRegistry.isGatewayExist((String) identifier);
                case EXPERIMENT:
                    return experimentRegistry.isExperimentExist((String) identifier);
                case USER_CONFIGURATION_DATA:
                    return experimentRegistry.isUserConfigDataExist((String) identifier);
                case PROCESS:
                    return experimentRegistry.isProcessExist((String) identifier);
                case PROCESS_RESOURCE_SCHEDULE:
                    return experimentRegistry.isProcessResourceScheduleExist((String) identifier);
                case TASK:
                    return experimentRegistry.isTaskExist((String) identifier);
                case JOB:
                    return experimentRegistry.isJobExist((CompositeIdentifier) identifier);
                default:
                    logger.error("Unsupported data type...", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
        } catch (Exception e) {
            logger.error("Error while checking existence of the resource " + dataType.toString(), new RegistryException(e));
            throw new RegistryException("Error while checking existence of the resource " + dataType.toString(), e);
        }
    }

}
