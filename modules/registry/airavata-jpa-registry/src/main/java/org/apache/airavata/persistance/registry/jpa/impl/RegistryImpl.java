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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.registry.cpi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegistryImpl implements Registry {
    private GatewayResource gatewayResource;
    private UserResource user;
    private final static Logger logger = LoggerFactory.getLogger(RegistryImpl.class);
    private ExperimentRegistry experimentRegistry = null;
    private ProjectRegistry projectRegistry = null;
    private GatewayRegistry gatewayRegistry = null;

    public RegistryImpl() throws RegistryException{
        try {
            if (!ResourceUtils.isGatewayExist(ServerSettings.getDefaultUserGateway())){
                gatewayResource = (GatewayResource) ResourceUtils.createGateway(ServerSettings.getDefaultUserGateway());
                gatewayResource.setGatewayName(ServerSettings.getDefaultUserGateway());
                gatewayResource.save();
            }else {
                gatewayResource = (GatewayResource)ResourceUtils.getGateway(ServerSettings.getDefaultUserGateway());
            }

            if (!ResourceUtils.isUserExist(ServerSettings.getDefaultUser())){
                user = ResourceUtils.createUser(ServerSettings.getDefaultUser(), ServerSettings.getDefaultUserPassword());
                user.save();
            }else {
                user = (UserResource)ResourceUtils.getUser(ServerSettings.getDefaultUser());
            }
            experimentRegistry = new ExperimentRegistry(gatewayResource, user);
            projectRegistry = new ProjectRegistry(gatewayResource, user);
            gatewayRegistry = new GatewayRegistry();
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata server properties..", e);
            throw new RegistryException("Unable to read airavata server properties..", e);
        }
    }

    public RegistryImpl(String gateway, String username, String password) throws RegistryException{
        if (!ResourceUtils.isGatewayExist(gateway)){
            gatewayResource = (GatewayResource) ResourceUtils.createGateway(gateway);
            gatewayResource.save();
        }else {
            gatewayResource = (GatewayResource)ResourceUtils.getGateway(gateway);
        }

        if (!ResourceUtils.isUserExist(username)){
            user = ResourceUtils.createUser(username, password);
            user.save();
        }else {
            user = (UserResource)ResourceUtils.getUser(username);
        }
        experimentRegistry = new ExperimentRegistry(gatewayResource, user);
        projectRegistry = new ProjectRegistry(gatewayResource, user);
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
    public Object add(ParentDataType dataType, Object newObjectToAdd, String gatewayId) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.addProject((Project)newObjectToAdd, gatewayId);
                case EXPERIMENT:
                    return experimentRegistry.addExperiment((Experiment) newObjectToAdd, gatewayId);
                case GATEWAY:
                    return gatewayRegistry.addGateway((Gateway)newObjectToAdd);
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
    public Object add(ChildDataType dataType, Object newObjectToAdd, Object dependentIdentifier) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT_CONFIGURATION_DATA:
                    return experimentRegistry.addUserConfigData((UserConfigurationData) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_OUTPUT:
                    return experimentRegistry.addExpOutputs((List<OutputDataObjectType>) newObjectToAdd, (String) dependentIdentifier);
                case EXPERIMENT_STATUS:
                    return experimentRegistry.addExperimentStatus((ExperimentStatus) newObjectToAdd, (String) dependentIdentifier);
                case WORKFLOW_NODE_DETAIL:
                    return experimentRegistry.addWorkflowNodeDetails((WorkflowNodeDetails) newObjectToAdd, (String) dependentIdentifier);
                case WORKFLOW_NODE_STATUS:
                    return experimentRegistry.addWorkflowNodeStatus((WorkflowNodeStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case NODE_OUTPUT:
                    return experimentRegistry.addNodeOutputs((List<OutputDataObjectType>) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case TASK_DETAIL:
                    return experimentRegistry.addTaskDetails((TaskDetails) newObjectToAdd, (String) dependentIdentifier);
                case APPLICATION_OUTPUT:
                    return experimentRegistry.addApplicationOutputs((List<OutputDataObjectType>) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case TASK_STATUS:
                    return experimentRegistry.addTaskStatus((TaskStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case JOB_DETAIL:
                    return experimentRegistry.addJobDetails((JobDetails) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case JOB_STATUS:
                    return experimentRegistry.addJobStatus((JobStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case APPLICATION_STATUS:
                    return experimentRegistry.addApplicationStatus((ApplicationStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case DATA_TRANSFER_DETAIL:
                    return experimentRegistry.addDataTransferDetails((DataTransferDetails) newObjectToAdd, (String) dependentIdentifier);
                case TRANSFER_STATUS:
                    return experimentRegistry.addTransferStatus((TransferStatus) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    return experimentRegistry.addComputationalResourceScheduling((ComputationalResourceScheduling) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    return experimentRegistry.addOutputDataHandling((AdvancedOutputDataHandling) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case ADVANCE_INPUT_DATA_HANDLING:
                    return experimentRegistry.addInputDataHandling((AdvancedInputDataHandling) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case QOS_PARAM:
                    return experimentRegistry.addQosParams((QualityOfServiceParams) newObjectToAdd, (CompositeIdentifier) dependentIdentifier);
                case ERROR_DETAIL:
                    return experimentRegistry.addErrorDetails((ErrorDetails) newObjectToAdd, dependentIdentifier);
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
    public void update(RegistryModelType dataType, Object newObjectToUpdate, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    projectRegistry.updateProject((Project)newObjectToUpdate, (String)identifier);
                    break;
                case GATEWAY:
                    gatewayRegistry.updateGateway((String)identifier, (Gateway)newObjectToUpdate);
                    break;
                case EXPERIMENT:
                    experimentRegistry.updateExperiment((Experiment) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_CONFIGURATION_DATA:
                    experimentRegistry.updateUserConfigData((UserConfigurationData) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_OUTPUT:
                    experimentRegistry.updateExpOutputs((List<OutputDataObjectType>) newObjectToUpdate, (String) identifier);
                    break;
                case EXPERIMENT_STATUS:
                    experimentRegistry.updateExperimentStatus((ExperimentStatus) newObjectToUpdate, (String) identifier);
                    break;
                case WORKFLOW_NODE_DETAIL:
                    experimentRegistry.updateWorkflowNodeDetails((WorkflowNodeDetails) newObjectToUpdate, (String) identifier);
                    break;
                case WORKFLOW_NODE_STATUS:
                    experimentRegistry.updateWorkflowNodeStatus((WorkflowNodeStatus) newObjectToUpdate, (String) identifier);
                    break;
                case NODE_OUTPUT:
                    experimentRegistry.updateNodeOutputs((List<OutputDataObjectType>) newObjectToUpdate, (String) identifier);
                    break;
                case TASK_DETAIL:
                    experimentRegistry.updateTaskDetails((TaskDetails) newObjectToUpdate, (String) identifier);
                    break;
                case APPLICATION_OUTPUT:
                    experimentRegistry.updateAppOutputs((List<OutputDataObjectType>) newObjectToUpdate, (String) identifier);
                    break;
                case TASK_STATUS:
                    experimentRegistry.updateTaskStatus((TaskStatus) newObjectToUpdate, (String) identifier);
                    break;
                case JOB_DETAIL:
                    experimentRegistry.updateJobDetails((JobDetails) newObjectToUpdate, (CompositeIdentifier) identifier);
                    break;
                case JOB_STATUS:
                    experimentRegistry.updateJobStatus((JobStatus) newObjectToUpdate, (CompositeIdentifier) identifier);
                    break;
                case APPLICATION_STATUS:
                    experimentRegistry.updateApplicationStatus((ApplicationStatus) newObjectToUpdate, (String) identifier);
                    break;
                case DATA_TRANSFER_DETAIL:
                    experimentRegistry.updateDataTransferDetails((DataTransferDetails) newObjectToUpdate, (String) identifier);
                    break;
                case TRANSFER_STATUS:
                    experimentRegistry.updateTransferStatus((TransferStatus) newObjectToUpdate, (String) identifier);
                    break;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    experimentRegistry.updateScheduling((ComputationalResourceScheduling) newObjectToUpdate, (String) identifier, dataType.toString());
                    break;
                case ADVANCE_INPUT_DATA_HANDLING:
                    experimentRegistry.updateInputDataHandling((AdvancedInputDataHandling) newObjectToUpdate, (String) identifier, dataType.toString());
                    break;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    experimentRegistry.updateOutputDataHandling((AdvancedOutputDataHandling) newObjectToUpdate, (String) identifier, dataType.toString());
                    break;
                case QOS_PARAM:
                    experimentRegistry.updateQOSParams((QualityOfServiceParams) newObjectToUpdate, (String) identifier, dataType.toString());
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
    public void update(RegistryModelType dataType, Object identifier, String fieldName, Object value) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    experimentRegistry.updateExperimentField((String) identifier, fieldName, value);
                    break;
                case EXPERIMENT_CONFIGURATION_DATA:
                    experimentRegistry.updateExpConfigDataField((String) identifier, fieldName, value);
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
    public Object get(RegistryModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.getProject((String)identifier);
                case GATEWAY:
                    return gatewayRegistry.getGateway((String)identifier);
                case EXPERIMENT:
                    return experimentRegistry.getExperiment((String) identifier, null);
                case EXPERIMENT_CONFIGURATION_DATA:
                    return experimentRegistry.getConfigData((String) identifier, null);
                case EXPERIMENT_OUTPUT:
                    return experimentRegistry.getExperimentOutputs((String) identifier);
                case EXPERIMENT_STATUS:
                    return experimentRegistry.getExperimentStatus((String) identifier);
                case WORKFLOW_NODE_DETAIL:
                    return experimentRegistry.getWorkflowNodeDetails((String) identifier);
                case WORKFLOW_NODE_STATUS:
                    return experimentRegistry.getWorkflowNodeStatus((String) identifier);
                case NODE_OUTPUT:
                    return experimentRegistry.getNodeOutputs((String) identifier);
                case TASK_DETAIL:
                    return experimentRegistry.getTaskDetails((String) identifier);
                case APPLICATION_OUTPUT:
                    return experimentRegistry.getApplicationOutputs((String) identifier);
                case TASK_STATUS:
                    return experimentRegistry.getTaskStatus((String) identifier);
                case JOB_DETAIL:
                    return experimentRegistry.getJobDetails((CompositeIdentifier) identifier);
                case JOB_STATUS:
                    return experimentRegistry.getJobStatus((CompositeIdentifier) identifier);
                case APPLICATION_STATUS:
                    return experimentRegistry.getApplicationStatus((CompositeIdentifier) identifier);
                case DATA_TRANSFER_DETAIL:
                    return experimentRegistry.getDataTransferDetails((String) identifier);
                case TRANSFER_STATUS:
                    return experimentRegistry.getDataTransferStatus((String) identifier);
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    return experimentRegistry.getComputationalScheduling(dataType, (String) identifier);
                case ADVANCE_INPUT_DATA_HANDLING:
                    return experimentRegistry.getInputDataHandling(dataType, (String) identifier);
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    return experimentRegistry.getOutputDataHandling(dataType, (String) identifier);
                case QOS_PARAM:
                    return experimentRegistry.getQosParams(dataType, (String) identifier);
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
    public List<Object> get(RegistryModelType dataType, String fieldName, Object value) throws RegistryException {
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
                case EXPERIMENT:
                    List<Experiment> experimentList = experimentRegistry.getExperimentList(fieldName, value);
                    for (Experiment experiment : experimentList) {
                        result.add(experiment);
                    }
                    return result;
                case WORKFLOW_NODE_DETAIL:
                    List<WorkflowNodeDetails> wfNodeDetails = experimentRegistry.getWFNodeDetails(fieldName, value);
                    for (WorkflowNodeDetails wf : wfNodeDetails) {
                        result.add(wf);
                    }
                    return result;
                case WORKFLOW_NODE_STATUS:
                    List<WorkflowNodeStatus> wfNodeStatusList = experimentRegistry.getWFNodeStatusList(fieldName, value);
                    for (WorkflowNodeStatus wfs : wfNodeStatusList) {
                        result.add(wfs);
                    }
                    return result;
                case TASK_DETAIL:
                    List<TaskDetails> taskDetails = experimentRegistry.getTaskDetails(fieldName, value);
                    for (TaskDetails task : taskDetails) {
                        result.add(task);
                    }
                    return result;
                case JOB_DETAIL:
                    List<JobDetails> jobDetails = experimentRegistry.getJobDetails(fieldName, value);
                    for (JobDetails job : jobDetails) {
                        result.add(job);
                    }
                    return result;
                case DATA_TRANSFER_DETAIL:
                    List<DataTransferDetails> dataTransferDetails = experimentRegistry.getDataTransferDetails(fieldName, value);
                    for (DataTransferDetails transferDetails : dataTransferDetails) {
                        result.add(transferDetails);
                    }
                    return result;
                case ERROR_DETAIL:
                    List<ErrorDetails> errorDetails = experimentRegistry.getErrorDetails(fieldName, value);
                    for (ErrorDetails error : errorDetails) {
                        result.add(error);
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
    public List<Object> get(RegistryModelType dataType, String fieldName, Object value, int limit,
                            int offset, Object orderByIdentifier, ResultOrderType resultOrderType) throws RegistryException {
        try {
            List<Object> result = new ArrayList<Object>();
            switch (dataType) {
                case PROJECT:
                    List<Project> projectList = projectRegistry
                            .getProjectList(fieldName, value, limit, offset, orderByIdentifier, resultOrderType);
                    for (Project project : projectList ){
                        result.add(project);
                    }
                    return result;
                case EXPERIMENT:
                    List<Experiment> experimentList = experimentRegistry.getExperimentList(fieldName, value,
                            limit, offset, orderByIdentifier, resultOrderType);
                    for (Experiment experiment : experimentList) {
                        result.add(experiment);
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
     * This method is to retrieve list of objects according to a given criteria
     * @param dataType Data type is a predefined type which the programmer should choose according to the object he
     *                 is going to save in to registry
     * @param filters filters is a map of field name and value that you need to use for search filtration
     * @return List of objects according to the given criteria
     */
    @Override
    public List<Object> search(RegistryModelType dataType, Map<String, String> filters) throws RegistryException {
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
    public List<Object> search(RegistryModelType dataType, Map<String, String> filters, int limit,
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
                    List<ExperimentSummary> experimentSummaries = experimentRegistry
                            .searchExperiments(filters, limit, offset, orderByIdentifier,
                                    resultOrderType);
                    for (ExperimentSummary ex : experimentSummaries){
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
    public Object getValue(RegistryModelType dataType, Object identifier, String field) throws RegistryException {
        try {
            switch (dataType) {
                case EXPERIMENT:
                    return experimentRegistry.getExperiment((String) identifier, field);
                case EXPERIMENT_CONFIGURATION_DATA:
                    return experimentRegistry.getConfigData((String) identifier, field);
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
    public List<String> getIds(RegistryModelType dataType, String fieldName, Object value) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.getProjectIDs(fieldName, value);
                case EXPERIMENT:
                    return experimentRegistry.getExperimentIDs(fieldName, value);
                case EXPERIMENT_CONFIGURATION_DATA:
                    return experimentRegistry.getExperimentIDs(fieldName, value);
                case WORKFLOW_NODE_DETAIL:
                    return experimentRegistry.getWorkflowNodeIds(fieldName, value);
                case TASK_DETAIL:
                    return experimentRegistry.getTaskDetailIds(fieldName, value);
                case JOB_DETAIL:
                    return experimentRegistry.getJobDetailIds(fieldName, value);
                case DATA_TRANSFER_DETAIL:
                    return experimentRegistry.getTransferDetailIds(fieldName, value);
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
    public void remove(RegistryModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    projectRegistry.removeProject((String)identifier);
                    break;
                case GATEWAY:
                    gatewayRegistry.removeGateway((String)identifier);
                    break;
                case EXPERIMENT:
                    experimentRegistry.removeExperiment((String) identifier);
                    break;
                case EXPERIMENT_CONFIGURATION_DATA:
                    experimentRegistry.removeExperimentConfigData((String) identifier);
                    break;
                case WORKFLOW_NODE_DETAIL:
                    experimentRegistry.removeWorkflowNode((String) identifier);
                    break;
                case TASK_DETAIL:
                    experimentRegistry.removeTaskDetails((String) identifier);
                    break;
                case JOB_DETAIL:
                    experimentRegistry.removeJobDetails((CompositeIdentifier) identifier);
                    break;
                case DATA_TRANSFER_DETAIL:
                    experimentRegistry.removeDataTransferDetails((String) identifier);
                    break;
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    experimentRegistry.removeComputationalScheduling(dataType, (String) identifier);
                    break;
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    experimentRegistry.removeOutputDataHandling(dataType, (String) identifier);
                    break;
                case ADVANCE_INPUT_DATA_HANDLING:
                    experimentRegistry.removeInputDataHandling(dataType, (String) identifier);
                    break;
                case QOS_PARAM:
                    experimentRegistry.removeQOSParams(dataType, (String) identifier);
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
    public boolean isExist(RegistryModelType dataType, Object identifier) throws RegistryException {
        try {
            switch (dataType) {
                case PROJECT:
                    return projectRegistry.isProjectExist((String)identifier);
                case GATEWAY:
                    return gatewayRegistry.isGatewayExist((String)identifier);
                case EXPERIMENT:
                    return experimentRegistry.isExperimentExist((String) identifier);
                case EXPERIMENT_CONFIGURATION_DATA:
                    return experimentRegistry.isExperimentConfigDataExist((String) identifier);
                case WORKFLOW_NODE_DETAIL:
                    return experimentRegistry.isWFNodeExist((String) identifier);
                case TASK_DETAIL:
                    return experimentRegistry.isTaskDetailExist((String) identifier);
                case JOB_DETAIL:
                    return experimentRegistry.isJobDetailExist((CompositeIdentifier) identifier);
                case DATA_TRANSFER_DETAIL:
                    return experimentRegistry.isTransferDetailExist((String) identifier);
                case COMPUTATIONAL_RESOURCE_SCHEDULING:
                    return experimentRegistry.isComputationalSchedulingExist(dataType, (String) identifier);
                case ADVANCE_INPUT_DATA_HANDLING:
                    return experimentRegistry.isInputDataHandlingExist(dataType, (String) identifier);
                case ADVANCE_OUTPUT_DATA_HANDLING:
                    return experimentRegistry.isOutputDataHandlingExist(dataType, (String) identifier);
                case QOS_PARAM:
                    return experimentRegistry.isQOSParamsExist(dataType, (String) identifier);
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
