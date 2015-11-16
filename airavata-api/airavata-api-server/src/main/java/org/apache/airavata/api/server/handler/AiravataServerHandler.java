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
import org.apache.airavata.api.airavata_apiConstants;
import org.apache.airavata.api.server.security.interceptor.SecurityCheck;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.datamodel.SSHCredential;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.model.Workflow;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.DataStoragePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.experiment.*;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.security.AuthzToken;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService.Client;
import org.apache.airavata.registry.core.app.catalog.resources.*;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogThriftConversion;
import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.core.experiment.catalog.model.Process;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AiravataServerHandler implements Airavata.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AiravataServerHandler.class);
    private ExperimentCatalog experimentCatalog;
    private AppCatalog appCatalog;
    private Publisher publisher;
	private WorkflowCatalog workflowCatalog;
    private CredentialStoreService.Client csClient;

    public AiravataServerHandler() {
        try {
            publisher = PublisherFactory.createActivityPublisher();
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
    }

    /**
     * Query Airavata to fetch the API version
     */
    @Override
    @SecurityCheck
    public String getAPIVersion(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException {

        return airavata_apiConstants.AIRAVATA_API_VERSION;
    }

    @Override
    @SecurityCheck
    public String addGateway(AuthzToken authzToken, Gateway gateway) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!validateString(gateway.getGatewayId())){
                logger.error("Gateway id cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            return (String) experimentCatalog.add(ExpCatParentDataType.GATEWAY, gateway, gateway.getGatewayId());
        } catch (RegistryException e) {
            logger.error("Error while adding gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateGateway(AuthzToken authzToken, String gatewayId, Gateway updatedGateway)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            experimentCatalog.update(ExperimentCatalogModelType.GATEWAY, updatedGateway, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while updating the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Gateway getGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            return (Gateway) experimentCatalog.get(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteGateway(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)){
                logger.error("Gateway does not exist in the system. Please provide a valid gateway ID...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setMessage("Gateway does not exist in the system. Please provide a valid gateway ID...");
                throw exception;
            }
            experimentCatalog.remove(ExperimentCatalogModelType.GATEWAY, gatewayId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while deleting the gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<Gateway> getAllGateways(AuthzToken authzToken) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException {

        try {
            List<Gateway> gateways = new ArrayList<Gateway>();
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.GATEWAY, null, null);
            for (Object gateway : list){
                gateways.add((Gateway)gateway);
            }
            return gateways;
        } catch (RegistryException e) {
            logger.error("Error while getting all the gateways", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting all the gateways. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean isGatewayExist(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*Following method wraps the logic of isGatewayExist method and this is to be called by any other method of the API as needed.*/
    private boolean isGatewayExistInternal(String gatewayId) throws InvalidRequestException, AiravataClientException,
            AiravataSystemException, AuthorizationException, TException{
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            return experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while getting gateway", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while getting gateway. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String generateAndRegisterSSHKeys(AuthzToken authzToken, String gatewayId, String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setUsername(userName);
            sshCredential.setGatewayId(gatewayId);
            return csClient.addSSHCredential(sshCredential);
        }catch (Exception e){
            logger.error("Error occurred while registering SSH Credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while registering SSH Credential. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String getSSHPubKey(AuthzToken authzToken, String airavataCredStoreToken, String gatewayId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            SSHCredential sshCredential = csClient.getSSHCredential(airavataCredStoreToken, gatewayId);
            return sshCredential.getPublicKey();
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH credential", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH credential. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public Map<String, String> getAllUserSSHPubKeys(AuthzToken authzToken, String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            if (csClient == null){
                csClient = getCredentialStoreServiceClient();
            }
            return csClient.getAllSSHKeysForUser(userName);
        }catch (Exception e){
            logger.error("Error occurred while retrieving SSH public keys for user : " + userName , e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error occurred while retrieving SSH public keys for user : " + userName + ". More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Create a Project
     *
     * @param project
     */
    @Override
    @SecurityCheck
    public String createProject(AuthzToken authzToken, String gatewayId, Project project) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!validateString(project.getName()) || !validateString(project.getOwner())){
                logger.error("Project name and owner cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            if (!validateString(gatewayId)){
                logger.error("Gateway ID cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            if (!isGatewayExistInternal(gatewayId)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            return (String) experimentCatalog.add(ExpCatParentDataType.PROJECT, project, gatewayId);
        } catch (RegistryException e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    @SecurityCheck
    public void updateProject(AuthzToken authzToken, String projectId, Project updatedProject) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {

        if (!validateString(projectId) || !validateString(projectId)){
            logger.error("Project id cannot be empty...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty...");
            throw exception;
        }
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            experimentCatalog.update(ExperimentCatalogModelType.PROJECT, updatedProject, projectId);
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            throw exception;
        }

    }

    private boolean validateString(String name){
        boolean valid = true;
        if (name == null || name.equals("") || name.trim().length() == 0){
            valid = false;
        }
        return valid;
    }

    /**
     * Get a Project by ID
     *
     * @param projectId
     */
    @Override
    @SecurityCheck
    public Project getProject(AuthzToken authzToken, String projectId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            return (Project) experimentCatalog.get(ExperimentCatalogModelType.PROJECT, projectId);
        } catch (RegistryException e) {
            logger.error("Error while retrieving the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while retrieving the project. More info : " + e.getMessage());
            throw exception;
        }
    }


    /**
     * Get all Project by user with pagination. Results will be ordered based
     * on creation time DESC
     *
     * @param gatewayId
     *    The identifier for the requested gateway.
     * @param userName
     *    The identifier of the user
     * @param limit
     *    The amount results to be fetched
     * @param offset
     *    The starting point of the results to be fetched
     **/
    @Override
    @SecurityCheck
    public List<Project> getUserProjects(AuthzToken authzToken, String gatewayId, String userName,
                                                          int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        List<Project> projects = new ArrayList<Project>();
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            List<Object> list = experimentCatalog.search(ExperimentCatalogModelType.PROJECT, filters, limit, offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    projects.add((Project) o);
                }
            }
            return projects;
        } catch (RegistryException e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get all Project for user by project name with pagination. Results will be ordered based
     * on creation time DESC
     *
     * @param gatewayId
     *    The identifier for the requested gateway.
     * @param userName
     *    The identifier of the user
     * @param projectName
     *    The name of the project on which the results to be fetched
     * @param limit
     *    The amount results to be fetched
     * @param offset
     *    The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<Project> searchProjectsByProjectName(AuthzToken authzToken, String gatewayId, String userName,
                                                                   String projectName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<Project> projects = new ArrayList<Project>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, projectName);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.PROJECT, filters, limit, offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                projects.add((Project)object);
            }
            return projects;
        }catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search and get all Projects for user by project description with pagination. Results
     * will be ordered based on creation time DESC
     *
     * @param gatewayId
     *    The identifier for the requested gateway.
     * @param userName
     *    The identifier of the user
     * @param description
     *    The description to be matched
     * @param limit
     *    The amount results to be fetched
     * @param offset
     *    The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<Project> searchProjectsByProjectDesc(AuthzToken authzToken, String gatewayId, String userName,
                                                                   String description, int limit, int offset) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<Project> projects = new ArrayList<Project>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, description);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.PROJECT, filters, limit, offset,
                    Constants.FieldConstants.ProjectConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                projects.add((Project)object);
            }
            return projects;
        }catch (Exception e) {
            logger.error("Error while retrieving projects", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by experiment name with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param expName
     *       Experiment name to be matched
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperimentsByName(AuthzToken authzToken, String gatewayId, String userName,
                                                                              String expName, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, expName);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, filters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by experiment name with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param description
     *       Experiment description to be matched
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperimentsByDesc(AuthzToken authzToken, String gatewayId, String userName,
                                                                              String description, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.DESCRIPTION, description);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, filters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by application id with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param applicationId
     *       Application id to be matched
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperimentsByApplication(AuthzToken authzToken, String gatewayId,
                                                                                     String userName, String applicationId,
                                                                                     int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, applicationId);
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, filters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by experiment status with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param experimentState
     *       Experiement state to be matched
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperimentsByStatus(AuthzToken authzToken, String gatewayId,
                                                                                String userName, ExperimentState experimentState,
                                                                                int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, experimentState.toString());
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, filters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by experiment creation time with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param fromTime
     *       Start time of the experiments creation time
     * @param toTime
     *       End time of the  experiement creation time
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperimentsByCreationTime(AuthzToken authzToken, String gatewayId,
                                                                                      String userName, long fromTime,
                                                                                      long toTime, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, String.valueOf(fromTime));
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, String.valueOf(toTime));
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, filters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Search Experiments by using multiple filter criteria with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requested gateway
     * @param userName
     *       Username of the requested user
     * @param filters
     *       map of multiple filter criteria.
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentSummaryModel> searchExperiments(AuthzToken authzToken, String gatewayId, String userName, Map<ExperimentSearchFields,
            String> filters, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummaryModel> summaries = new ArrayList<ExperimentSummaryModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            Map<String, String> regFilters = new HashMap();
            regFilters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            regFilters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            for(Map.Entry<ExperimentSearchFields, String> entry : filters.entrySet())
            {
                if(entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_NAME)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.EXPERIMENT_DESC)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.DESCRIPTION, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.APPLICATION_ID)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXECUTION_ID, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.STATUS)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.FROM_DATE)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.TO_DATE)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, entry.getValue());
                }else if(entry.getKey().equals(ExperimentSearchFields.PROJECT_ID)){
                    regFilters.put(Constants.FieldConstants.ExperimentConstants.PROJECT_ID, entry.getValue());
                }
            }
            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT, regFilters, limit,
                    offset, Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            for (Object object : results) {
                summaries.add((ExperimentSummaryModel) object);
            }
            return summaries;
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Experiment execution statisitics by sending the gateway id and the time period interested in.
     * This method will retrun an ExperimentStatistics object which contains the number of successfully
     * completed experiments, failed experiments etc.
     * @param gatewayId
     * @param fromTime
     * @param toTime
     * @return
     * @throws InvalidRequestException
     * @throws AiravataClientException
     * @throws AiravataSystemException
     * @throws TException
     */
    @Override
    @SecurityCheck
    public ExperimentStatistics getExperimentStatistics(AuthzToken authzToken, String gatewayId, long fromTime, long toTime)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            Map<String, String> filters = new HashMap();
            filters.put(Constants.FieldConstants.ExperimentConstants.GATEWAY_ID, gatewayId);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, fromTime+"");
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, toTime+"");

            List<Object> results = experimentCatalog.search(ExperimentCatalogModelType.EXPERIMENT_STATISTICS, filters);
            return (ExperimentStatistics) results.get(0);
        }catch (Exception e) {
            logger.error("Error while retrieving experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Experiments within project with pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param projectId
     *       Identifier of the project
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentModel> getExperimentsInProject(AuthzToken authzToken, String projectId, int limit, int offset)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, ProjectNotFoundException,
            AuthorizationException, TException {
        if (!validateString(projectId)){
            logger.error("Project id cannot be empty. Please provide a valid project ID...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty. Please provide a valid project ID...");
            throw exception;
        }
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT,
                    Constants.FieldConstants.ExperimentConstants.PROJECT_ID, projectId, limit, offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()) {
                for (Object o : list) {
                    experiments.add((ExperimentModel) o);
                }
            }
            return experiments;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get Experiments by user pagination. Results will be sorted
     * based on creation time DESC
     *
     * @param gatewayId
     *       Identifier of the requesting gateway
     * @param userName
     *       Username of the requested user
     * @param limit
     *       Amount of results to be fetched
     * @param offset
     *       The starting point of the results to be fetched
     */
    @Override
    @SecurityCheck
    public List<ExperimentModel> getUserExperiments(AuthzToken authzToken, String gatewayId, String userName, int limit,
                                                                     int offset) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            if (!ExpCatResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentModel> experiments = new ArrayList<ExperimentModel>();
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            List<Object> list = experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT,
                    Constants.FieldConstants.ExperimentConstants.USER_NAME, userName, limit, offset,
                    Constants.FieldConstants.ExperimentConstants.CREATION_TIME, ResultOrderType.DESC);
            if (list != null && !list.isEmpty()){
                for (Object o : list){
                    experiments.add((ExperimentModel)o);
                }
            }
            return experiments;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiments", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiments. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Create an experiment for the specified user belonging to the gateway. The gateway identity is not explicitly passed
     * but inferred from the authentication header. This experiment is just a persistent place holder. The client
     * has to subsequently configure and launch the created experiment. No action is taken on Airavata Server except
     * registering the experiment in a persistent store.
     *
     * @param experiment@return The server-side generated.airavata.registry.core.experiment.globally unique identifier.
     * @throws org.apache.airavata.model.error.InvalidRequestException For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.AiravataClientException The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
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
     * @throws org.apache.airavata.model.error.AiravataSystemException This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                               rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public String createExperiment(AuthzToken authzToken, String gatewayId, ExperimentModel experiment) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getExperimentCatalog(gatewayId);
            if (!validateString(experiment.getExperimentName())){
                logger.error("Cannot create experiments with empty experiment name");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Cannot create experiments with empty experiment name");
                throw exception;
            }
            if (!isGatewayExistInternal(gatewayId)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }

            if(experiment.getUserConfigurationData() != null && experiment.getUserConfigurationData()
                .getComputationalResourceScheduling() != null){

                String compResourceId = experiment.getUserConfigurationData()
                    .getComputationalResourceScheduling().getResourceHostId();
                ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                    .getComputeResource(compResourceId);
                if(!computeResourceDescription.isEnabled()){
                    logger.error("Compute Resource is not enabled by the Admin!");
                    AiravataSystemException exception = new AiravataSystemException();
                    exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                    exception.setMessage("Compute Resource is not enabled by the Admin!");
                    throw exception;
                }
            }

            String experimentId = (String) experimentCatalog.add(ExpCatParentDataType.EXPERIMENT, experiment, gatewayId);
            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.CREATED,
                    experimentId,
                    gatewayId);
            String messageId = AiravataUtils.getId("EXPERIMENT");
            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
            if(publisher!=null) {
                publisher.publish(messageContext);
            }
            logger.info(experimentId, "Created new experiment with experiment name {}", experiment.getExperimentName());
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getExperimentName());
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * If the experiment is not already launched experiment can be deleted.
     * @param authzToken
     * @param experimentId
     * @return
     * @throws InvalidRequestException
     * @throws AiravataClientException
     * @throws AiravataSystemException
     * @throws AuthorizationException
     * @throws TException
     */
    @Override
    public boolean deleteExperiment(AuthzToken authzToken, String experimentId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, experimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + experimentId + " does not exist in the system..");
            }
            ExperimentModel experimentModel = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            if(!(experimentModel.getExperimentStatus().getState() == ExperimentState.CREATED)){
                logger.error("Error while deleting the experiment");
                throw new ExperimentCatalogException("Experiment is not in CREATED state. Hence cannot deleted. ID:"+ experimentId);
            }
            experimentCatalog.remove(ExperimentCatalogModelType.EXPERIMENT, experimentId);
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch previously created experiment metadata.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
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
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentModel getExperiment(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return getExperimentInternal(airavataExperimentId);
    }

    /**
     * Fetch the completed nested tree structue of previously created experiment metadata which includes processes ->
     * tasks -> jobs information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return experimentMetada
     * This method will return the previously stored experiment metadata.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
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
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentModel getDetailedExperimentTree(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            ExperimentModel experimentModel =  getExperimentInternal(airavataExperimentId);
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            List<Object> processObjects  = experimentCatalog.get(ExperimentCatalogModelType.PROCESS,
                    Constants.FieldConstants.ExperimentConstants.EXPERIMENT_ID, experimentModel.getExperimentId());
            List<ProcessModel> processList = new ArrayList<>();
            if(processObjects != null){
                processObjects.stream().forEach(p -> {
                    //Process already has the task object
                    ((ProcessModel)p).getTasks().stream().forEach(t->{
                        try {
                            List<Object> jobObjects = experimentCatalog.get(ExperimentCatalogModelType.JOB,
                                    Constants.FieldConstants.JobConstants.TASK_ID, ((TaskModel)t).getTaskId());
                            List<JobModel> jobList  = new ArrayList<JobModel>();
                            if(jobObjects != null){
                                jobObjects.stream().forEach(j -> jobList.add((JobModel)j));
                                t.setJobs(jobList);
                            }
                        } catch (RegistryException e) {
                            logger.error(e.getMessage(), e);
                        }
                    });
                    processList.add((ProcessModel)p);
                });
                experimentModel.setProcesses(processList);
            }
            return experimentModel;
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /*This private method wraps the logic of getExperiment method as this method is called internally in the API.*/
    private ExperimentModel getExperimentInternal(String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId);
        } catch (Exception e) {
            logger.error("Error while retrieving the experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment. More info : " + e.getMessage());
            throw exception;
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
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
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
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void updateExperiment(AuthzToken authzToken, String airavataExperimentId, ExperimentModel experiment)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Update request failed, Experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED:
                        if(experiment.getUserConfigurationData() != null && experiment.getUserConfigurationData()
                            .getComputationalResourceScheduling() != null){
                            String compResourceId = experiment.getUserConfigurationData()
                                .getComputationalResourceScheduling().getResourceHostId();
                            ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                                .getComputeResource(compResourceId);
                            if(!computeResourceDescription.isEnabled()){
                                logger.error("Compute Resource is not enabled by the Admin!");
                                AiravataSystemException exception = new AiravataSystemException();
                                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                                exception.setMessage("Compute Resource is not enabled by the Admin!");
                                throw exception;
                            }
                        }
                        experimentCatalog.update(ExperimentCatalogModelType.EXPERIMENT, experiment, airavataExperimentId);
                        logger.info(airavataExperimentId, "Successfully updated experiment {} ", experiment.getExperimentName());
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateExperimentConfiguration(AuthzToken authzToken, String airavataExperimentId, UserConfigurationDataModel userConfiguration)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Update experiment configuration failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED:
                        experimentCatalog.add(ExpCatChildDataType.USER_CONFIGURATION_DATA, userConfiguration, airavataExperimentId);
                        logger.info(airavataExperimentId, "Successfully updated experiment configuration for experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating experiment {}. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ", airavataExperimentId);
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating user configuration", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating user configuration. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public void updateResourceScheduleing(AuthzToken authzToken, String airavataExperimentId,
                                          ComputationalResourceSchedulingModel resourceScheduling) throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.info(airavataExperimentId, "Update resource scheduling failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatusInternal(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED:
                        experimentCatalog.add(ExpCatChildDataType.PROCESS_RESOURCE_SCHEDULE, resourceScheduling, airavataExperimentId);
                        logger.info(airavataExperimentId, "Successfully updated resource scheduling for the experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.error(airavataExperimentId, "Error while updating scheduling info. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        AiravataSystemException exception = new AiravataSystemException();
                        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                        exception.setMessage("Error while updating experiment. Update experiment is only valid for experiments " +
                                "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                                "experiment is in one of above statuses... ");
                        throw exception;
                }
            }
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while updating scheduling info", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating scheduling info. " +
                    "Update experiment is only valid for experiments " +
                    "with status CREATED, VALIDATED, CANCELLED, FAILED and UNKNOWN. Make sure the given " +
                    "experiment is in one of above statuses...  " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean validateExperiment(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
     	try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
 			if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
        } catch (RegistryException e1) {
 			  logger.error(airavataExperimentId, "Error while retrieving projects", e1);
 	            AiravataSystemException exception = new AiravataSystemException();
 	            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
 	            exception.setMessage("Error while retrieving projects. More info : " + e1.getMessage());
 	            throw exception;
 		}

        Client orchestratorClient = getOrchestratorClient();
        try{
        if (orchestratorClient.validateExperiment(airavataExperimentId)) {
            logger.info(airavataExperimentId, "Experiment validation succeed.");
            return true;
        } else {
            logger.info(airavataExperimentId, "Experiment validation failed.");
            return false;
        }}catch (TException e){
            throw e;
        }finally {
            orchestratorClient.getOutputProtocol().getTransport().close();
            orchestratorClient.getInputProtocol().getTransport().close();
        }


    }

    /**
     * Fetch the previously configured experiment configuration information.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method returns the previously configured experiment configuration data.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *<p/>
     *UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *gateway registration steps and retry this request.
     *<p/>
     *AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *For now this is a place holder.
     *<p/>
     *INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *is implemented, the authorization will be more substantial.
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any
     *          Airavata Server side issues and if the problem cannot be corrected by the client
     *         rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public ExperimentStatus getExperimentStatus(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return getExperimentStatusInternal(airavataExperimentId);
    }

    /*Private method wraps the logic of getExperimentStatus method since this method is called internally.*/
    private ExperimentStatus getExperimentStatusInternal(String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving experiment status, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId +
                        " does not exist in the system..");
            }
            return (ExperimentStatus) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT_STATUS, airavataExperimentId);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getExperimentOutputs(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Get experiment outputs failed, experiment {} doesn't exit.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return (List<OutputDataObjectType>) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT_OUTPUT, airavataExperimentId);
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getIntermediateOutputs(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return null;
    }

    @SecurityCheck
    public Map<String, JobStatus> getJobStatuses(AuthzToken authzToken, String airavataExperimentId)
            throws AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving job details, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<Object> processModels = experimentCatalog.get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            Map<String, JobStatus> jobStatus = new HashMap<String, JobStatus>();
            if (processModels != null && !processModels.isEmpty()){
                for (Object process : processModels) {
                    ProcessModel processModel = (ProcessModel) process;
                    List<TaskModel> tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()){
                      for (TaskModel task : tasks){
                          String taskId =  task.getTaskId();
                          List<Object> jobs = experimentCatalog.get(ExperimentCatalogModelType.JOB, Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                          for (Object jobObject : jobs) {
                              String jobID = ((JobModel)jobObject).getJobId();
                              jobStatus.put(jobID, ((JobModel)jobObject).getJobStatus());
                          }
                      }
                    }
                }
            }
            return jobStatus;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job statuses", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job statuses. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<JobModel> getJobDetails(AuthzToken authzToken, String airavataExperimentId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)){
                logger.error(airavataExperimentId, "Error while retrieving job details, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<Object> processModels = experimentCatalog.get(ExperimentCatalogModelType.PROCESS, Constants.FieldConstants.ProcessConstants.EXPERIMENT_ID, airavataExperimentId);
            List<JobModel> jobList = new ArrayList<>();
            if (processModels != null && !processModels.isEmpty()){
                for (Object process : processModels) {
                    ProcessModel processModel = (ProcessModel) process;
                    List<TaskModel> tasks = processModel.getTasks();
                    if (tasks != null && !tasks.isEmpty()){
                        for (TaskModel taskModel : tasks){
                            String taskId =  taskModel.getTaskId();
                            List<Object> jobs = experimentCatalog.get(ExperimentCatalogModelType.JOB, Constants.FieldConstants.JobConstants.TASK_ID, taskId);
                            for (Object jobObject : jobs) {
                                jobList.add ((JobModel)jobObject);
                            }
                        }
                    }
                }
            }
            return jobList;
        } catch (Exception e) {
            logger.error(airavataExperimentId, "Error while retrieving the job details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            throw exception;
        }
    }


    /**
     * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
     * notifications and intermediate and output data will be subsequently available for this experiment.
     *
     *
     * @param airavataExperimentId   The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.model.error.InvalidRequestException
     *          For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     *          If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException
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
     * @throws org.apache.airavata.model.error.AiravataSystemException
     *          This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *          rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void launchExperiment(AuthzToken authzToken, final String airavataExperimentId, String gatewayId)
            throws AuthorizationException, TException {
    	try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
			if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId)) {
                logger.error(airavataExperimentId, "Error while launching experiment, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.GATEWAY, gatewayId)) {
                logger.error(airavataExperimentId, "Error while launching experiment, gatewayId {} doesn't exist.", gatewayId);
                throw new ExperimentNotFoundException("Requested gateway id " + gatewayId + " does not exist in the system..");
            }
        } catch (RegistryException e1) {
            logger.error(airavataExperimentId, "Error while instantiate the registry instance", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while instantiate the registry instance. More info : " + e1.getMessage());
            throw exception;
        }
    	ExperimentModel experiment = getExperimentInternal(airavataExperimentId);
    	OrchestratorService.Client orchestratorClient = getOrchestratorClient();
    	if (orchestratorClient.validateExperiment(airavataExperimentId)) {
    		orchestratorClient.launchExperiment(airavataExperimentId, gatewayId);
    	}else {
            logger.error(airavataExperimentId, "Couldn't identify experiment type, experiment {} is neither single application nor workflow.", airavataExperimentId);
            throw new InvalidRequestException("Experiment '" + airavataExperimentId + "' launch failed. Unable to figureout execution type for application " + experiment.getExecutionId());
        }
    }


    private OrchestratorService.Client getOrchestratorClient() throws TException {
	    try {
		    final String serverHost = ServerSettings.getOrchestratorServerHost();
		    final int serverPort = ServerSettings.getOrchestratorServerPort();
		    return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
	    } catch (AiravataException e) {
		    throw new TException(e);
	    }
    }

    /**
     * Clone an specified experiment with a new name. A copy of the experiment configuration is made and is persisted with new metadata.
     *   The client has to subsequently update this configuration if needed and launch the cloned experiment.
     *
     * @param existingExperimentID
     *    This is the experiment identifier that already exists in the system. Will use this experimentID to retrieve
     *    user configuration which is used with the clone experiment.
     *
     * @param newExperiementName
     *   experiment name that should be used in the cloned experiment
     *
     * @return
     *   The server-side generated.airavata.registry.core.experiment.globally unique identifier for the newly cloned experiment.
     *
     * @throws org.apache.airavata.model.error.InvalidRequestException
     *    For any incorrect forming of the request itself.
     *
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException
     *    If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     *
     * @throws org.apache.airavata.model.error.AiravataClientException
     *    The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
     *
     *      UNKNOWN_GATEWAY_ID - If a Gateway is not registered with Airavata as a one time administrative
     *         step, then Airavata Registry will not have a provenance area setup. The client has to follow
     *         gateway registration steps and retry this request.
     *
     *      AUTHENTICATION_FAILURE - How Authentication will be implemented is yet to be determined.
     *         For now this is a place holder.
     *
     *      INVALID_AUTHORIZATION - This will throw an authorization exception. When a more robust security hand-shake
     *         is implemented, the authorization will be more substantial.
     *
     * @throws org.apache.airavata.model.error.AiravataSystemException
     *    This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *       rather an Airavata Administrator will be notified to take corrective action.
     *
     *
     * @param existingExperimentID
     * @param newExperiementName
     */
    @Override
    @SecurityCheck
    public String cloneExperiment(AuthzToken authzToken, String existingExperimentID, String newExperiementName)
            throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException,
            AuthorizationException, TException {
        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID)){
                logger.error(existingExperimentID, "Error while cloning experiment {}, experiment doesn't exist.", existingExperimentID);
                throw new ExperimentNotFoundException("Requested experiment id " + existingExperimentID + " does not exist in the system..");
            }
            ExperimentModel existingExperiment = (ExperimentModel) experimentCatalog.get(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID);
            String gatewayId = (String) experimentCatalog.getValue(ExperimentCatalogModelType.EXPERIMENT, existingExperimentID, Constants.FieldConstants.ExperimentConstants.GATEWAY_ID);
            existingExperiment.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            if (existingExperiment.getExecutionId() != null){
                List<OutputDataObjectType> applicationOutputs = getApplicationOutputsInternal(existingExperiment.getExecutionId());
                existingExperiment.setExperimentOutputs(applicationOutputs);
            }
            if (validateString(newExperiementName)){
                existingExperiment.setExperimentName(newExperiementName);
            }
            if (existingExperiment.getErrors() != null ){
                existingExperiment.getErrors().clear();
            }
            if(existingExperiment.getUserConfigurationData() != null && existingExperiment.getUserConfigurationData()
                .getComputationalResourceScheduling() != null){
                String compResourceId = existingExperiment.getUserConfigurationData()
                    .getComputationalResourceScheduling().getResourceHostId();

                ComputeResourceDescription computeResourceDescription = appCatalog.getComputeResource()
                    .getComputeResource(compResourceId);
                if(!computeResourceDescription.isEnabled()){
                    existingExperiment.getUserConfigurationData().setComputationalResourceScheduling(null);
                }
            }
            return (String) experimentCatalog.add(ExpCatParentDataType.EXPERIMENT, existingExperiment, gatewayId);
        } catch (Exception e) {
            logger.error(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cloning the experiment with existing configuration. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Terminate a running experiment.
     *
     * @param airavataExperimentId The identifier for the requested experiment. This is returned during the create experiment step.
     * @return This method call does not have a return value.
     * @throws org.apache.airavata.model.error.InvalidRequestException     For any incorrect forming of the request itself.
     * @throws org.apache.airavata.model.error.ExperimentNotFoundException If the specified experiment is not previously created, then an Experiment Not Found Exception is thrown.
     * @throws org.apache.airavata.model.error.AiravataClientException     The following list of exceptions are thrown which Airavata Client can take corrective actions to resolve:
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
     * @throws org.apache.airavata.model.error.AiravataSystemException     This exception will be thrown for any Airavata Server side issues and if the problem cannot be corrected by the client
     *                                                                   rather an Airavata Administrator will be notified to take corrective action.
     */
    @Override
    @SecurityCheck
    public void terminateExperiment(AuthzToken authzToken, String airavataExperimentId, String gatewayId) throws InvalidRequestException,
            ExperimentNotFoundException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!(experimentCatalog.isExist(ExperimentCatalogModelType.EXPERIMENT, airavataExperimentId))){
                logger.error("Experiment does not exist.Please provide a valid experiment id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            Client client = getOrchestratorClient();
            client.terminateExperiment(airavataExperimentId, gatewayId);
        } catch (RegistryException e) {
            logger.error(airavataExperimentId, "Error while cancelling the experiment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while cancelling the experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Application Module.
     *
     * @param applicationModule Application Module Object created from the datamodel.
     * @return appModuleId
     * Returns a server-side generated airavata appModule globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationModule(AuthzToken authzToken, String gatewayId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().addApplicationModule(applicationModule, gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while adding application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Application Module.
     *
     * @param appModuleId The identifier for the requested application module
     * @return applicationModule
     * Returns a application Module Object.
     */
    @Override
    @SecurityCheck
    public ApplicationModule getApplicationModule(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the adding application module. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean updateApplicationModule(AuthzToken authzToken, String appModuleId, ApplicationModule applicationModule)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationModule(appModuleId, applicationModule);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<ApplicationModule> getAllAppModules(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getAllApplicationModules(gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving all application modules...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving all application modules. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Application Module.
     *
     * @param appModuleId The identifier for the requested application module to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationModule(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while deleting application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting the application module. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Application Deployment.
     *
     * @param applicationDeployment@return appModuleId
     *                                     Returns a server-side generated airavata appModule globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationDeployment(AuthzToken authzToken, String gatewayId, ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationDeployment().addApplicationDeployment(applicationDeployment, gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while adding application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application deployment. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public ApplicationDeploymentDescription getApplicationDeployment(AuthzToken authzToken, String appDeploymentId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationDeployment().getApplicationDeployement(appDeploymentId);
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while retrieving application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Application Deployment.
     *
     * @param appDeploymentId       The identifier for the requested application deployment to be updated.
     * @param applicationDeployment
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateApplicationDeployment(AuthzToken authzToken, String appDeploymentId,
                                               ApplicationDeploymentDescription applicationDeployment)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().updateApplicationDeployment(appDeploymentId, applicationDeployment);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while updating application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Application deployment.
     *
     * @param appDeploymentId The identifier for the requested application deployment to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationDeployment(AuthzToken authzToken, String appDeploymentId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().removeAppDeployment(appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appDeploymentId, "Error while deleting application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Deployment Descriptions.
     *
     * @return list applicationDeployment.
     * Returns the list of all application Deployment Objects.
     */
    @Override
    @SecurityCheck
    public List<ApplicationDeploymentDescription> getAllApplicationDeployments(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationDeployment().getAllApplicationDeployements(gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployments. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public List<String> getAppModuleDeployedResources(AuthzToken authzToken, String appModuleId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            List<String> appDeployments = new ArrayList<String>();
            appCatalog = RegistryFactory.getAppCatalog();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments = appCatalog.getApplicationDeployment().getApplicationDeployements(filters);
            for (ApplicationDeploymentDescription description : applicationDeployments){
                appDeployments.add(description.getAppDeploymentId());
            }
            return appDeployments;
        } catch (AppCatalogException e) {
            logger.error(appModuleId, "Error while retrieving application deployments...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application deployment. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Application Interface.
     *
     * @param applicationInterface@return appInterfaceId
     *                                    Returns a server-side generated airavata application interface globally unique identifier.
     */
    @Override
    @SecurityCheck
    public String registerApplicationInterface(AuthzToken authzToken, String gatewayId,
                                               ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().addApplicationInterface(applicationInterface, gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while adding application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch a Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application module
     * @return applicationInterface
     * Returns a application Interface Object.
     */
    @Override
    @SecurityCheck
    public ApplicationInterfaceDescription getApplicationInterface(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update a Application Interface.
     *
     * @param appInterfaceId       The identifier for the requested application deployment to be updated.
     * @param applicationInterface
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateApplicationInterface(AuthzToken authzToken, String appInterfaceId,
                                              ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationInterface(appInterfaceId, applicationInterface);
            return true;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while updating application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a Application Interface.
     *
     * @param appInterfaceId The identifier for the requested application interface to be deleted.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteApplicationInterface(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().removeApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while deleting application interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch name and id of  Application Interface documents.
     *
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces with corresponsing id's
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllApplicationInterfaceNames(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            List<ApplicationInterfaceDescription> allApplicationInterfaces = appCatalog.getApplicationInterface().getAllApplicationInterfaces(gatewayId);
            Map<String, String> allApplicationInterfacesMap = new HashMap<String, String>();
            if (allApplicationInterfaces != null && !allApplicationInterfaces.isEmpty()){
                for (ApplicationInterfaceDescription interfaceDescription : allApplicationInterfaces){
                    allApplicationInterfacesMap.put(interfaceDescription.getApplicationInterfaceId(), interfaceDescription.getApplicationName());
                }
            }
            return allApplicationInterfacesMap;
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all Application Interface documents.
     *
     * @return map<applicationId, applicationInterfaceNames>
     * Returns a list of application interfaces documents
     */
    @Override
    @SecurityCheck
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces(AuthzToken authzToken, String gatewayId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getAllApplicationInterfaces(gatewayId);
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving application interfaces...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application interfaces. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the list of Application Inputs.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return list<applicationInterfaceModel.InputDataObjectType>
     * Returns a list of application inputs.
     */
    @Override
    @SecurityCheck
    public List<InputDataObjectType> getApplicationInputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationInputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application inputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application inputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the list of Application Outputs.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return list<applicationInterfaceModel.OutputDataObjectType>
     * Returns a list of application outputs.
     */
    @Override
    @SecurityCheck
    public List<OutputDataObjectType> getApplicationOutputs(AuthzToken authzToken, String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return getApplicationOutputsInternal(appInterfaceId);
    }

    /*This private method wraps the logic of getApplicationOutputs method as this method is called internally in the API.*/
    private List<OutputDataObjectType> getApplicationOutputsInternal(String appInterfaceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while retrieving application outputs...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving application outputs. More info : " + e.getMessage());
            throw exception;
        }
    }
    /**
     * Fetch a list of all deployed Compute Hosts for a given application interfaces.
     *
     * @param appInterfaceId The identifier for the requested application interface
     * @return map<computeResourceId, computeResourceName>
     * A map of registered compute resource id's and their corresponding hostnames.
     * Deployments of each modules listed within the interfaces will be listed.
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAvailableAppInterfaceComputeResources(AuthzToken authzToken, String appInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ApplicationDeployment applicationDeployment = appCatalog.getApplicationDeployment();
            Map<String, String> allComputeResources = appCatalog.getComputeResource().getAvailableComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface =
                    appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<String,String>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules) {
                    filters.put(AppCatAbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, moduleId);
                    List<ApplicationDeploymentDescription> applicationDeployments =
                            applicationDeployment.getApplicationDeployements(filters);
                    for (ApplicationDeploymentDescription deploymentDescription : applicationDeployments) {
                        if (allComputeResources.get(deploymentDescription.getComputeHostId()) != null){
                            availableComputeResources.put(deploymentDescription.getComputeHostId(),
                                    allComputeResources.get(deploymentDescription.getComputeHostId()));
                        }
                    }
                }
            }
            return availableComputeResources;
        } catch (AppCatalogException e) {
            logger.error(appInterfaceId, "Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public String registerComputeResource(AuthzToken authzToken, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().addComputeResource(computeResourceDescription);
        } catch (AppCatalogException e) {
            logger.error("Error while saving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while saving compute resource. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public ComputeResourceDescription getComputeResource(AuthzToken authzToken, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getComputeResource(computeResourceId);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch all registered Compute Resources.
     *
     * @return A map of registered compute resource id's and thier corresponding hostnames.
     * Compute Resource Object created from the datamodel..
     */
    @Override
    @SecurityCheck
    public Map<String, String> getAllComputeResourceNames(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getAllComputeResourceIdList();
        } catch (AppCatalogException e) {
            logger.error("Error while retrieving compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving compute resource. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean updateComputeResource(AuthzToken authzToken, String computeResourceId, ComputeResourceDescription computeResourceDescription)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().updateComputeResource(computeResourceId, computeResourceDescription);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while updating compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updaing compute resource. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean deleteComputeResource(AuthzToken authzToken, String computeResourceId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeComputeResource(computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while deleting compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting compute resource. More info : " + e.getMessage());
            throw exception;
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
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addLocalSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
            		computeResource.addLocalJobSubmission(localSubmission), JobSubmissionProtocol.LOCAL, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean updateLocalSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, LOCALSubmission localSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            LocalSubmissionResource submission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALSubmission getLocalJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    private String addJobSubmissionInterface(ComputeResource computeResource,
			String computeResourceId, String jobSubmissionInterfaceId,
			JobSubmissionProtocol protocolType, int priorityOrder)
			throws AppCatalogException {
		JobSubmissionInterface jobSubmissionInterface = new JobSubmissionInterface();
		jobSubmissionInterface.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
		jobSubmissionInterface.setPriorityOrder(priorityOrder);
		jobSubmissionInterface.setJobSubmissionProtocol(protocolType);
		return computeResource.addJobSubmissionProtocol(computeResourceId,jobSubmissionInterface);
	}

    /**
     * Add a SSH Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param sshJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSSHJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
            		computeResource.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
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
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSSHForkJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH_FORK, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SSHJobSubmission getSSHJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving SSH job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a Cloud Job Submission details to a compute resource
     * App catalog will return a jobSubmissionInterfaceId which will be added to the jobSubmissionInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param cloudJobSubmission  The SSHJobSubmission object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addCloudJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                               CloudJobSubmission cloudJobSubmission) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addCloudJobSubmission(cloudJobSubmission), JobSubmissionProtocol.CLOUD, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public CloudJobSubmission getCloudJobSubmission(AuthzToken authzToken, String jobSubmissionId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getCloudJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Cloud job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
	public String addUNICOREJobSubmissionDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                                 UnicoreJobSubmission unicoreJobSubmission)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
	        appCatalog = RegistryFactory.getAppCatalog();
	        ComputeResource computeResource = appCatalog.getComputeResource();
	        return addJobSubmissionInterface(computeResource, computeResourceId,
	        		computeResource.addUNICOREJobSubmission(unicoreJobSubmission), JobSubmissionProtocol.UNICORE, priorityOrder);
	    } catch (AppCatalogException e) {
	        logger.error("Error while adding job submission interface to resource compute resource...", e);
	        AiravataSystemException exception = new AiravataSystemException();
	        exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
	        exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
	        throw exception;
	    }
	}

    @Override
    @SecurityCheck
    public UnicoreJobSubmission getUnicoreJobSubmission(AuthzToken authzToken, String jobSubmissionId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(jobSubmissionId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving Unicore job submission interface to resource compute resource...";
            logger.error(jobSubmissionId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean updateSSHJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            SshJobSubmissionResource submission = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given cloud Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param cloudJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean updateCloudJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            CloudSubmissionResource submission = AppCatalogThriftConversion.getCloudJobSubmission(cloudJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreJobSubmissionDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, UnicoreJobSubmission unicoreJobSubmission)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            UnicoreJobSubmissionResource submission = AppCatalogThriftConversion.getUnicoreJobSubmission(unicoreJobSubmission);
            submission.setjobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a Local data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param localDataMovement The LOCALDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addLocalDataMovementDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                              LOCALDataMovement localDataMovement) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addLocalDataMovement(localDataMovement), DataMovementProtocol.LOCAL, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given Local data movement details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param localDataMovement        The LOCALDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateLocalDataMovementDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, LOCALDataMovement localDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            LocalDataMovementResource movment = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public LOCALDataMovement getLocalDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving local data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    private String addDataMovementInterface(ComputeResource computeResource,
			String computeResourceId, String dataMovementInterfaceId,
			DataMovementProtocol protocolType, int priorityOrder)
			throws AppCatalogException {
		DataMovementInterface dataMovementInterface = new DataMovementInterface();
		dataMovementInterface.setDataMovementInterfaceId(dataMovementInterfaceId);
		dataMovementInterface.setPriorityOrder(priorityOrder);
		dataMovementInterface.setDataMovementProtocol(protocolType);
		return computeResource.addDataMovementProtocol(computeResourceId,dataMovementInterface);
	}

    /**
     * Add a SCP data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param computeResourceId The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder     Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param scpDataMovement   The SCPDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addSCPDataMovementDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addScpDataMovement(scpDataMovement), DataMovementProtocol.SCP, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given scp data movement details
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param scpDataMovement          The SCPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the update.
     */
    @Override
    @SecurityCheck
    public boolean updateSCPDataMovementDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, SCPDataMovement scpDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            ScpDataMovementResource movment = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public SCPDataMovement getSCPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSCPDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving SCP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String addUnicoreDataMovementDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
                    computeResource.addUnicoreDataMovement(unicoreDataMovement), DataMovementProtocol.UNICORE_STORAGE_SERVICE, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateUnicoreDataMovementDetails(AuthzToken authzToken, String dataMovementInterfaceId, UnicoreDataMovement unicoreDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            UnicoreDataMovementResource movment = AppCatalogThriftConversion.getUnicoreDMResource(unicoreDataMovement);
            movment.setDataMovementId(dataMovementInterfaceId);
            movment.save();
            return true;
        } catch (Exception e) {
            logger.error(dataMovementInterfaceId, "Error while updating unicore data movement to compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating unicore data movement to compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public UnicoreDataMovement getUnicoreDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving UNICORE data movement interface...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
        }
    }

    /**
     * Add a GridFTP data moevement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param computeResourceId   The identifier of the compute resource to which JobSubmission protocol to be added
     * @param priorityOrder       Specify the priority of this job manager. If this is the only jobmanager, the priority can be zero.
     * @param gridFTPDataMovement The GridFTPDataMovement object to be added to the resource.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public String addGridFTPDataMovementDetails(AuthzToken authzToken, String computeResourceId, int priorityOrder,
                                                GridFTPDataMovement gridFTPDataMovement) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            appCatalog = RegistryFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addGridFTPDataMovement(gridFTPDataMovement), DataMovementProtocol.GridFTP, priorityOrder);
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding data movement interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given GridFTP data movement details to a compute resource
     * App catalog will return a dataMovementInterfaceId which will be added to the dataMovementInterfaces.
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param gridFTPDataMovement      The GridFTPDataMovement object to be updated.
     * @return status
     * Returns a success/failure of the updation.
     */
    @Override
    @SecurityCheck
    public boolean updateGridFTPDataMovementDetails(AuthzToken authzToken, String jobSubmissionInterfaceId, GridFTPDataMovement gridFTPDataMovement)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            GridftpDataMovementResource movment = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (Exception e) {
            logger.error(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public GridFTPDataMovement getGridFTPDataMovement(AuthzToken authzToken, String dataMovementId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getGridFTPDataMovement(dataMovementId);
        } catch (AppCatalogException e) {
            String errorMsg = "Error while retrieving GridFTP data movement interface to resource compute resource...";
            logger.error(dataMovementId, errorMsg, e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(errorMsg + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean changeJobSubmissionPriority(AuthzToken authzToken, String jobSubmissionInterfaceId, int newPriorityOrder) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
    @SecurityCheck
    public boolean changeDataMovementPriority(AuthzToken authzToken, String dataMovementInterfaceId, int newPriorityOrder)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
    @SecurityCheck
    public boolean changeJobSubmissionPriorities(AuthzToken authzToken, Map<String, Integer> jobSubmissionPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
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
    @SecurityCheck
    public boolean changeDataMovementPriorities(AuthzToken authzToken, Map<String, Integer> dataMovementPriorityMap)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        return false;
    }

    /**
     * Delete a given job submisison interface
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteJobSubmissionInterface(AuthzToken authzToken, String computeResourceId, String jobSubmissionInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeJobSubmissionInterface(computeResourceId, jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting job submission interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Delete a given data movement interface
     *
     * @param dataMovementInterfaceId The identifier of the DataMovement Interface to be changed
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    @SecurityCheck
    public boolean deleteDataMovementInterface(AuthzToken authzToken, String computeResourceId, String dataMovementInterfaceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeDataMovementInterface(computeResourceId, dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public String registerResourceJobManager(AuthzToken authzToken, ResourceJobManager resourceJobManager) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().addResourceJobManager(resourceJobManager);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManager.getResourceJobManagerId(), "Error while adding resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean updateResourceJobManager(AuthzToken authzToken, String resourceJobManagerId, ResourceJobManager updatedResourceJobManager)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while updating resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public ResourceJobManager getResourceJobManager(AuthzToken authzToken,String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            return appCatalog.getComputeResource().getResourceJobManager(resourceJobManagerId);
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while retrieving resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteResourceJobManager(AuthzToken authzToken, String resourceJobManagerId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().deleteResourceJobManager(resourceJobManagerId);
            return true;
        } catch (AppCatalogException e) {
            logger.error(resourceJobManagerId, "Error while deleting resource job manager...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting resource job manager. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public boolean deleteBatchQueue(AuthzToken authzToken, String computeResourceId, String queueName) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            appCatalog.getComputeResource().removeBatchQueue(computeResourceId, queueName);
            return true;
        } catch (AppCatalogException e) {
            logger.error(computeResourceId, "Error while deleting batch queue...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting batch queue. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Register a Gateway Resource Profile.
     *
     * @param gatewayResourceProfile Gateway Resource Profile Object.
     *   The GatewayID should be obtained from Airavata gateway registration and passed to register a corresponding
     *      resource profile.
     * @return status.
     * Returns a success/failure of the registration.
     */
    @Override
    @SecurityCheck
    public String registerGatewayResourceProfile(AuthzToken authzToken, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!validateString(gatewayResourceProfile.getGatewayID())){
                logger.error("Cannot create gateway profile with empty gateway id");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Cannot create gateway profile with empty gateway id");
                throw exception;
            }
            if (!isGatewayExistInternal(gatewayResourceProfile.getGatewayID())){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.addGatewayResourceProfile(gatewayResourceProfile);
        } catch (AppCatalogException e) {
            logger.error("Error while registering gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Fetch the given Gateway Resource Profile.
     *
     * @param gatewayID The identifier for the requested gateway resource
     * @return gatewayResourceProfile
     * Gateway Resource Profile Object.
     */
    @Override
    @SecurityCheck
    public GatewayResourceProfile getGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while retrieving gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving gateway resource profile. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean updateGatewayResourceProfile(AuthzToken authzToken, String gatewayID, GatewayResourceProfile gatewayResourceProfile)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while updating gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway resource profile. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean deleteGatewayResourceProfile(AuthzToken authzToken, String gatewayID) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while removing gateway resource profile...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while removing gateway resource profile. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean addGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                       ComputeResourcePreference computeResourcePreference) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
            	throw new AppCatalogException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            profile.addToComputeResourcePreferences(computeResourcePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean addGatewayDataStoragePreference(AuthzToken authzToken, String gatewayID, String dataMoveId, DataStoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                throw new AppCatalogException("Gateway resource profile '"+gatewayID+"' does not exist!!!");
            }
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
//            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            profile.addToDataStoragePreferences(dataStoragePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while registering gateway resource profile preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while registering gateway resource profile preference. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public ComputeResourcePreference getGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            ComputeResource computeResource = appCatalog.getComputeResource();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }
            if (!computeResource.isComputeResourceExists(computeResourceId)){
                logger.error(computeResourceId, "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw exception;
            }
            return gatewayProfile.getComputeResourcePreference(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public DataStoragePreference getGatewayDataStoragePreference(AuthzToken authzToken, String gatewayID, String dataMoveId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            ComputeResource computeResource = appCatalog.getComputeResource();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                logger.error(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }

            return gatewayProfile.getDataStoragePreference(gatewayID, dataMoveId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preference. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(AuthzToken authzToken, String gatewayID)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<DataStoragePreference> getAllGatewayDataStoragePreferences(AuthzToken authzToken, String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID).getDataStoragePreferences();
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway data storage preferences. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
    public List<GatewayResourceProfile> getAllGatewayResourceProfiles(AuthzToken authzToken) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getAllGatewayProfiles();
        } catch (AppCatalogException e) {
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading retrieving all gateway profiles. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean updateGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId,
                                                          ComputeResourcePreference computeResourcePreference)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
            List<ComputeResourcePreference> computeResourcePreferences = profile.getComputeResourcePreferences();
            ComputeResourcePreference preferenceToRemove = null;
            for (ComputeResourcePreference preference : computeResourcePreferences) {
				if (preference.getComputeResourceId().equals(computeResourceId)){
					preferenceToRemove=preference;
					break;
				}
			}
            if (preferenceToRemove!=null) {
				profile.getComputeResourcePreferences().remove(
						preferenceToRemove);
			}
            profile.getComputeResourcePreferences().add(computeResourcePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean updateGatewayDataStoragePreference(AuthzToken authzToken, String gatewayID, String dataMoveId, DataStoragePreference dataStoragePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            GatewayResourceProfile profile = gatewayProfile.getGatewayProfile(gatewayID);
            List<DataStoragePreference> dataStoragePreferences = profile.getDataStoragePreferences();
            DataStoragePreference preferenceToRemove = null;
            for (DataStoragePreference preference : dataStoragePreferences) {
                if (preference.getDataMovememtResourceId().equals(dataMoveId)){
                    preferenceToRemove=preference;
                    break;
                }
            }
            if (preferenceToRemove!=null) {
                profile.getDataStoragePreferences().remove(
                        preferenceToRemove);
            }
            profile.getDataStoragePreferences().add(dataStoragePreference);
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            return true;
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
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
    @SecurityCheck
    public boolean deleteGatewayComputeResourcePreference(AuthzToken authzToken, String gatewayID, String computeResourceId)
            throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
    	try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.removeComputeResourcePreferenceFromGateway(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public boolean deleteGatewayDataStoragePreference(AuthzToken authzToken, String gatewayID, String dataMoveId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        try {
            if (!isGatewayExistInternal(gatewayID)){
                logger.error("Gateway does not exist.Please provide a valid gateway id...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            appCatalog = RegistryFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.removeDataStoragePreferenceFromGateway(gatewayID, dataMoveId);
        } catch (AppCatalogException e) {
            logger.error(gatewayID, "Error while reading gateway data storage preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway data storage preference. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    @SecurityCheck
	public List<String> getAllWorkflows(AuthzToken authzToken, String gatewayId) throws InvalidRequestException,
			AiravataClientException, AiravataSystemException, AuthorizationException, TException {

        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
			return getWorkflowCatalog().getAllWorkflows(gatewayId);
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving all workflow template Ids.";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public Workflow getWorkflow(AuthzToken authzToken, String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException, AuthorizationException, AiravataSystemException, TException {
		try {
			return getWorkflowCatalog().getWorkflow(workflowTemplateId);
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving the workflow "+workflowTemplateId+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public void deleteWorkflow(AuthzToken authzToken, String workflowTemplateId)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			getWorkflowCatalog().deleteWorkflow(workflowTemplateId);
		} catch (AppCatalogException e) {
			String msg = "Error in deleting the workflow "+workflowTemplateId+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public String registerWorkflow(AuthzToken authzToken, String gatewayId, Workflow workflow)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
        if (!isGatewayExistInternal(gatewayId)){
            logger.error("Gateway does not exist.Please provide a valid gateway id...");
            throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
        }
        try {
			return getWorkflowCatalog().registerWorkflow(workflow, gatewayId);
		} catch (AppCatalogException e) {
			String msg = "Error in registering the workflow "+workflow.getName()+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public void updateWorkflow(AuthzToken authzToken, String workflowTemplateId, Workflow workflow)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			getWorkflowCatalog().updateWorkflow(workflowTemplateId, workflow);
		} catch (AppCatalogException e) {
			String msg = "Error in updating the workflow "+workflow.getName()+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public String getWorkflowTemplateId(AuthzToken authzToken, String workflowName)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			return getWorkflowCatalog().getWorkflowTemplateId(workflowName);
		} catch (AppCatalogException e) {
			String msg = "Error in retrieving the workflow template id for "+workflowName+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	@Override
    @SecurityCheck
	public boolean isWorkflowExistWithName(AuthzToken authzToken, String workflowName)
			throws InvalidRequestException, AiravataClientException, AiravataSystemException, AuthorizationException, TException {
		try {
			return getWorkflowCatalog().isWorkflowExistWithName(workflowName);
		} catch (AppCatalogException e) {
			String msg = "Error in veriying the workflow for workflow name "+workflowName+".";
			logger.error(msg, e);
			AiravataSystemException exception = new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage(msg+" More info : " + e.getMessage());
            throw exception;
		}
	}

	private WorkflowCatalog getWorkflowCatalog() {
		if (workflowCatalog == null) {
			try {
				workflowCatalog = RegistryFactory.getAppCatalog().getWorkflowCatalog();
			} catch (Exception e) {
				logger.error("Unable to create Workflow Catalog", e);
			}
		}
		return workflowCatalog;
	}

    @Override
    @SecurityCheck
    public boolean deleteProject(AuthzToken authzToken, String projectId) throws InvalidRequestException,
            AiravataClientException, AiravataSystemException, ProjectNotFoundException, AuthorizationException, TException {

        try {
            experimentCatalog = RegistryFactory.getDefaultExpCatalog();
            if (!experimentCatalog.isExist(ExperimentCatalogModelType.PROJECT, projectId)) {
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            experimentCatalog.remove(ExperimentCatalogModelType.PROJECT, projectId);
            return true;
        } catch (RegistryException e) {
            logger.error("Error while removing the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while removing the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    private CredentialStoreService.Client getCredentialStoreServiceClient() throws TException, ApplicationSettingsException {
        final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
        final String serverHost = ServerSettings.getCredentialStoreServerHost();
        try {
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException e) {
            throw new TException("Unable to create credential store client...", e);
        }
    }
}
