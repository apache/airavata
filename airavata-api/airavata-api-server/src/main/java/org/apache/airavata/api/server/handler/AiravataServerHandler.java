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

import org.airavata.appcatalog.cpi.*;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.aiaravata.application.catalog.data.resources.*;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogThriftConversion;
import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.airavataAPIConstants;
import org.apache.airavata.api.server.util.AiravataServerThreadPoolExecutor;
import org.apache.airavata.api.server.util.DataModelUtils;
import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.logger.AiravataLogger;
import org.apache.airavata.common.logger.AiravataLoggerFactory;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.messaging.core.MessageContext;
import org.apache.airavata.messaging.core.Publisher;
import org.apache.airavata.messaging.core.PublisherFactory;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.appcatalog.gatewayprofile.ComputeResourcePreference;
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.error.*;
import org.apache.airavata.model.messaging.event.ExperimentStatusChangeEvent;
import org.apache.airavata.model.messaging.event.MessageType;
import org.apache.airavata.model.util.ExecutionType;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.orchestrator.client.OrchestratorClientFactory;
import org.apache.airavata.orchestrator.cpi.OrchestratorService;
import org.apache.airavata.orchestrator.cpi.OrchestratorService.Client;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.apache.airavata.registry.cpi.utils.Constants;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.TaskDetailConstants;
import org.apache.airavata.registry.cpi.utils.Constants.FieldConstants.WorkflowNodeConstants;
import org.apache.airavata.workflow.engine.WorkflowEngine;
import org.apache.airavata.workflow.engine.WorkflowEngineException;
import org.apache.airavata.workflow.engine.WorkflowEngineFactory;
import org.apache.thrift.TException;

import java.util.*;

public class AiravataServerHandler implements Airavata.Iface {
    private static final AiravataLogger logger = AiravataLoggerFactory.getLogger(AiravataServerHandler.class);
    private Registry registry;
    private AppCatalog appCatalog;
    private Publisher publisher;

    public AiravataServerHandler() {
        try {
            if (ServerSettings.isRabbitMqPublishEnabled()) {
                publisher = PublisherFactory.createPublisher();
            }
        } catch (ApplicationSettingsException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        } catch (AiravataException e) {
            logger.error("Error occured while reading airavata-server properties..", e);
        }
    }

//    private void storeServerConfig() throws ApplicationSettingsException {
//        String zkhostPort = AiravataZKUtils.getZKhostPort();
//        String airavataServerHostPort = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.API_SERVER_HOST)
//                            + ":" + ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.API_SERVER_PORT);
//
//        try {
//            zk = new ZooKeeper(zkhostPort, 6000, this);   // no watcher is required, this will only use to store some data
//            String apiServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_API_SERVER_NODE,"/airavata-server");
//            String OrchServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_ORCHESTRATOR_SERVER_NODE,"/orchestrator-server");
//            String gfacServer = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_SERVER_NODE,"/gfac-server");
//            String gfacExperiments = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE,"/gfac-experiments");
//
//            synchronized (mutex) {
//                mutex.wait();  // waiting for the syncConnected event
//            }
//            Stat zkStat = zk.exists(apiServer, false);
//            if (zkStat == null) {
//                zk.create(apiServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                        CreateMode.PERSISTENT);
//            }
//            String instantNode = apiServer + File.separator + String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
//            zkStat = zk.exists(instantNode, false);
//            if (zkStat == null) {
//                zk.create(instantNode,
//                        airavataServerHostPort.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                        CreateMode.EPHEMERAL);      // other component will watch these childeren creation deletion to monitor the status of the node
//                logger.info("Successfully created airavata-server node");
//            }
//
//            zkStat = zk.exists(OrchServer, false);
//            if (zkStat == null) {
//                zk.create(OrchServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                        CreateMode.PERSISTENT);
//                logger.info("Successfully created orchestrator-server node");
//            }
//            zkStat = zk.exists(gfacServer, false);
//            if (zkStat == null) {
//                zk.create(gfacServer, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                        CreateMode.PERSISTENT);
//                logger.info("Successfully created gfac-server node");
//            }
//            zkStat = zk.exists(gfacServer, false);
//            if (zkStat == null) {
//                zk.create(gfacExperiments, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                        CreateMode.PERSISTENT);
//                logger.info("Successfully created gfac-server node");
//            }
//            logger.info("Finished starting ZK: " + zk);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        }
//    }
//    synchronized public void process(WatchedEvent watchedEvent) {
//        synchronized (mutex) {
//            Event.KeeperState state = watchedEvent.getState();
//            logger.info(state.name());
//            if (state == Event.KeeperState.SyncConnected) {
//                mutex.notify();
//            } else if(state == Event.KeeperState.Expired ||
//                    state == Event.KeeperState.Disconnected){
//                try {
//                    mutex = -1;
//                    zk = new ZooKeeper(AiravataZKUtils.getZKhostPort(), 6000, this);
//                    synchronized (mutex) {
//                        mutex.wait();  // waiting for the syncConnected event
//                    }
//                    storeServerConfig();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (ApplicationSettingsException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * Query Airavata to fetch the API version
     */
    @Override
    public String getAPIVersion() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        return airavataAPIConstants.AIRAVATA_API_VERSION;
    }

    /**
     * Create a Project
     *
     * @param project
     */
    @Override
    public String createProject(Project project) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!validateString(project.getName()) || !validateString(project.getOwner())){
                logger.error("Project name and owner cannot be empty...");
                throw new AiravataSystemException(AiravataErrorType.INTERNAL_ERROR);
            }
            return (String)registry.add(ParentDataType.PROJECT, project);
        } catch (RegistryException e) {
            logger.error("Error while creating the project", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    public void updateProject(String projectId, Project updatedProject) throws InvalidRequestException,
                                                                               AiravataClientException,
                                                                               AiravataSystemException,
                                                                               ProjectNotFoundException,
                                                                               TException {
        if (!validateString(projectId) || !validateString(projectId)){
            logger.error("Project id cannot be empty...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty...");
            throw exception;
        }
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            registry.update(RegistryModelType.PROJECT, updatedProject, projectId);
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
    public Project getProject(String projectId) throws InvalidRequestException,
                                                       AiravataClientException,
                                                       AiravataSystemException,
                                                       ProjectNotFoundException,
                                                       TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            return (Project)registry.get(RegistryModelType.PROJECT, projectId);
        } catch (RegistryException e) {
            logger.error("Error while updating the project", e);
            ProjectNotFoundException exception = new ProjectNotFoundException();
            exception.setMessage("Error while updating the project. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Get all Project by user
     *
     * @param userName
     */
    @Override
    public List<Project> getAllUserProjects(String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        List<Project> projects = new ArrayList<Project>();
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            registry = RegistryFactory.getDefaultRegistry();
            List<Object> list = registry.get(RegistryModelType.PROJECT, Constants.FieldConstants.ProjectConstants.OWNER, userName);
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

    public List<Project> searchProjectsByProjectName(String userName, String projectName) throws InvalidRequestException,
                                                                                                 AiravataClientException,
                                                                                                 AiravataSystemException,
                                                                                                 TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<Project> projects = new ArrayList<Project>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.PROJECT_NAME, projectName);
            List<Object> results = registry.search(RegistryModelType.PROJECT, filters);
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

    public List<Project> searchProjectsByProjectDesc(String userName, String description) throws InvalidRequestException,
                                                                                                 AiravataClientException,
                                                                                                 AiravataSystemException,
                                                                                                 TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<Project> projects = new ArrayList<Project>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ProjectConstants.OWNER, userName);
            filters.put(Constants.FieldConstants.ProjectConstants.DESCRIPTION, description);
            List<Object> results = registry.search(RegistryModelType.PROJECT, filters);
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

    public List<ExperimentSummary> searchExperimentsByName(String userName, String expName) throws InvalidRequestException,
                                                                                                   AiravataClientException,
                                                                                                   AiravataSystemException,
                                                                                                   TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummary> summaries = new ArrayList<ExperimentSummary>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_NAME, expName);
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            for (Object object : results) {
                summaries.add((ExperimentSummary) object);
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

    public List<ExperimentSummary> searchExperimentsByDesc(String userName, String description) throws InvalidRequestException,
                                                                                                       AiravataClientException,
                                                                                                       AiravataSystemException,
                                                                                                       TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummary> summaries = new ArrayList<ExperimentSummary>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_DESC, description);
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            for (Object object : results) {
                summaries.add((ExperimentSummary) object);
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

    public List<ExperimentSummary> searchExperimentsByApplication(String userName, String applicationId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummary> summaries = new ArrayList<ExperimentSummary>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.APPLICATION_ID, applicationId);
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            for (Object object : results) {
                summaries.add((ExperimentSummary) object);
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

    @Override
    public List<ExperimentSummary> searchExperimentsByStatus(String userName, ExperimentState experimentState) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummary> summaries = new ArrayList<ExperimentSummary>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.EXPERIMENT_STATUS, experimentState.toString());
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            for (Object object : results) {
                summaries.add((ExperimentSummary) object);
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

    @Override
    public List<ExperimentSummary> searchExperimentsByCreationTime(String userName, long fromTime, long toTime) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
            List<ExperimentSummary> summaries = new ArrayList<ExperimentSummary>();
            registry = RegistryFactory.getDefaultRegistry();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(Constants.FieldConstants.ExperimentConstants.USER_NAME, userName);
            filters.put(Constants.FieldConstants.ExperimentConstants.FROM_DATE, String.valueOf(fromTime));
            filters.put(Constants.FieldConstants.ExperimentConstants.TO_DATE, String.valueOf(toTime));
            List<Object> results = registry.search(RegistryModelType.EXPERIMENT, filters);
            for (Object object : results) {
                summaries.add((ExperimentSummary) object);
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
     * Get all Experiments within a Project
     *
     * @param projectId
     */
    @Override
    public List<Experiment> getAllExperimentsInProject(String projectId) throws InvalidRequestException,
                                                                                AiravataClientException,
                                                                                AiravataSystemException,
                                                                                ProjectNotFoundException,
                                                                                TException {
        if (!validateString(projectId)){
            logger.error("Project id cannot be empty. Please provide a valid project ID...");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Project id cannot be empty. Please provide a valid project ID...");
            throw exception;
        }
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.PROJECT, projectId)){
                logger.error("Project does not exist in the system. Please provide a valid project ID...");
                ProjectNotFoundException exception = new ProjectNotFoundException();
                exception.setMessage("Project does not exist in the system. Please provide a valid project ID...");
                throw exception;
            }
            List<Experiment> experiments = new ArrayList<Experiment>();
            List<Object> list = registry.get(RegistryModelType.EXPERIMENT, Constants.FieldConstants.ExperimentConstants.PROJECT_ID, projectId);
            if (list != null && !list.isEmpty()) {
                for (Object o : list) {
                    experiments.add((Experiment) o);
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
     * Get all Experiments by user
     *
     * @param userName
     */
    @Override
    public List<Experiment> getAllUserExperiments(String userName) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        if (!validateString(userName)){
            logger.error("Username cannot be empty. Please provide a valid user..");
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Username cannot be empty. Please provide a valid user..");
            throw exception;
        }
        try {
            if (!ResourceUtils.isUserExist(userName)){
                logger.error("User does not exist in the system. Please provide a valid user..");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("User does not exist in the system. Please provide a valid user..");
                throw exception;
            }
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
     * @param experiment@return The server-side generated airavata experiment globally unique identifier.
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
    public String createExperiment(Experiment experiment) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!validateString(experiment.getName())){
                logger.error("Cannot create experiments with empty experiment name");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Cannot create experiments with empty experiment name");
                throw exception;
            }
            String experimentId = (String)registry.add(ParentDataType.EXPERIMENT, experiment);
            if (ServerSettings.isRabbitMqPublishEnabled()){
                String gatewayId = ServerSettings.getDefaultUserGateway();
                ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.CREATED,
                        experimentId,
                        gatewayId);
                String messageId = AiravataUtils.getId("EXPERIMENT");
                MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT,messageId,gatewayId);
                messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                publisher.publish(messageContext);
            }
            logger.infoId(experimentId, "Created new experiment with experiment name {}", experiment.getName());
            return experimentId;
        } catch (Exception e) {
            logger.error("Error while creating the experiment with experiment name {}", experiment.getName());
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while creating the experiment. More info : " + e.getMessage());
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
    public Experiment getExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return (Experiment)registry.get(RegistryModelType.EXPERIMENT, airavataExperimentId);
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
    public void updateExperiment(String airavataExperimentId, Experiment experiment) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)) {
                logger.errorId(airavataExperimentId, "Update request failed, Experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatus(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getExperimentState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED: case UNKNOWN:
                        registry.update(RegistryModelType.EXPERIMENT, experiment, airavataExperimentId);
                        logger.infoId(airavataExperimentId, "Successfully updated experiment {} ", experiment.getName());
                        break;
                    default:
                        logger.errorId(airavataExperimentId, "Error while updating experiment. Update experiment is only valid for experiments " +
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
            logger.errorId(airavataExperimentId, "Error while updating experiment", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating experiment. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public void updateExperimentConfiguration(String airavataExperimentId, UserConfigurationData userConfiguration) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.errorId(airavataExperimentId, "Update experiment configuration failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatus(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getExperimentState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED: case UNKNOWN:
                        registry.add(ChildDataType.EXPERIMENT_CONFIGURATION_DATA, userConfiguration, airavataExperimentId);
                        logger.infoId(airavataExperimentId, "Successfully updated experiment configuration for experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.errorId(airavataExperimentId, "Error while updating experiment {}. Update experiment is only valid for experiments " +
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
            logger.errorId(airavataExperimentId, "Error while updating user configuration", e);
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
    public void updateResourceScheduleing(String airavataExperimentId, ComputationalResourceScheduling resourceScheduling) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.infoId(airavataExperimentId, "Update resource scheduling failed, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            ExperimentStatus experimentStatus = getExperimentStatus(airavataExperimentId);
            if (experimentStatus != null){
                ExperimentState experimentState = experimentStatus.getExperimentState();
                switch (experimentState){
                    case CREATED: case VALIDATED: case CANCELED: case FAILED: case UNKNOWN:
                        registry.add(ChildDataType.COMPUTATIONAL_RESOURCE_SCHEDULING, resourceScheduling, airavataExperimentId);
                        logger.infoId(airavataExperimentId, "Successfully updated resource scheduling for the experiment {}.", airavataExperimentId);
                        break;
                    default:
                        logger.errorId(airavataExperimentId, "Error while updating scheduling info. Update experiment is only valid for experiments " +
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
            logger.errorId(airavataExperimentId, "Error while updating scheduling info", e);
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
    public boolean validateExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
     	try {
            registry = RegistryFactory.getDefaultRegistry();
 			if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)) {
                logger.errorId(airavataExperimentId, "Experiment validation failed , experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
        } catch (RegistryException e1) {
 			  logger.errorId(airavataExperimentId, "Error while retrieving projects", e1);
 	            AiravataSystemException exception = new AiravataSystemException();
 	            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
 	            exception.setMessage("Error while retrieving projects. More info : " + e1.getMessage());
 	            throw exception;
 		}

        Client orchestratorClient = getOrchestratorClient();
        try{
        if (orchestratorClient.validateExperiment(airavataExperimentId)) {
            logger.infoId(airavataExperimentId, "Experiment validation succeed.");
            return true;
        } else {
            logger.infoId(airavataExperimentId, "Experiment validation failed.");
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
    public ExperimentStatus getExperimentStatus(String airavataExperimentId) throws InvalidRequestException,
                                                                                    ExperimentNotFoundException,
                                                                                    AiravataClientException,
                                                                                    AiravataSystemException,
                                                                                    TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.errorId(airavataExperimentId, "Error while retrieving experiment status, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId +
                                                      " does not exist in the system..");
            }
            return (ExperimentStatus)registry.get(RegistryModelType.EXPERIMENT_STATUS, airavataExperimentId);
        } catch (Exception e) {
            logger.errorId(airavataExperimentId, "Error while retrieving the experiment status", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment status. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
    public List<DataObjectType> getExperimentOutputs(String airavataExperimentId) throws TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.errorId(airavataExperimentId, "Get experiment outputs failed, experiment {} doesn't exit.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            return (List<DataObjectType>)registry.get(RegistryModelType.EXPERIMENT_OUTPUT, airavataExperimentId);
        } catch (Exception e) {
            logger.errorId(airavataExperimentId, "Error while retrieving the experiment outputs", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the experiment outputs. More info : " + e.getMessage());
            throw exception;
        }
    }

    public Map<String, JobStatus> getJobStatuses(String airavataExperimentId) throws TException {
        Map<String, JobStatus> jobStatus = new HashMap<String, JobStatus>();
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.errorId(airavataExperimentId, "Error while retrieving job status, the experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
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
            logger.errorId(airavataExperimentId, "Error while retrieving the job statuses", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job statuses. More info : " + e.getMessage());
            throw exception;
        }
        return jobStatus;
    }

    @Override
    public List<JobDetails> getJobDetails(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        List<JobDetails> jobDetailsList = new ArrayList<JobDetails>();
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)){
                logger.errorId(airavataExperimentId, "Error while retrieving job details, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
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
                                    jobDetailsList.add((JobDetails) job);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.errorId(airavataExperimentId, "Error while retrieving the job details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the job details. More info : " + e.getMessage());
            throw exception;
        }
        return jobDetailsList;
    }

    @Override
    public List<DataTransferDetails> getDataTransferDetails(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        List<DataTransferDetails> dataTransferDetailList = new ArrayList<DataTransferDetails>();
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)) {
                logger.errorId(airavataExperimentId, "Error while retrieving data transfer details, experiment {} doesn't exit.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
            List<Object> workflowNodes = registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, Constants.FieldConstants.WorkflowNodeConstants.EXPERIMENT_ID, airavataExperimentId);
            if (workflowNodes != null && !workflowNodes.isEmpty()){
                for (Object wf : workflowNodes){
                    String nodeInstanceId = ((WorkflowNodeDetails) wf).getNodeInstanceId();
                    List<Object> taskDetails = registry.get(RegistryModelType.TASK_DETAIL, Constants.FieldConstants.TaskDetailConstants.NODE_ID, nodeInstanceId);
                    if (taskDetails != null && !taskDetails.isEmpty()){
                        for (Object ts : taskDetails){
                            String taskID = ((TaskDetails) ts).getTaskID();
                            List<Object> dataTransferDetails = registry.get(RegistryModelType.DATA_TRANSFER_DETAIL, Constants.FieldConstants.JobDetaisConstants.TASK_ID, taskID);
                            if (dataTransferDetails != null && !dataTransferDetails.isEmpty()){
                                for (Object dataTransfer : dataTransferDetails){
                                    dataTransferDetailList.add((DataTransferDetails) dataTransfer);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.errorId(airavataExperimentId, "Error while retrieving the data transfer details", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving the data transfer details. More info : " + e.getMessage());
            throw exception;
        }
        return dataTransferDetailList;
    }

    /**
     * Launch a previously created and configured experiment. Airavata Server will then start processing the request and appropriate
     * notifications and intermediate and output data will be subsequently available for this experiment.
     *
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
    public void launchExperiment(final String airavataExperimentId, String airavataCredStoreToken) throws TException {
    	try {
            registry = RegistryFactory.getDefaultRegistry();
			if (!registry.isExist(RegistryModelType.EXPERIMENT, airavataExperimentId)) {
                logger.errorId(airavataExperimentId, "Error while launching experiment, experiment {} doesn't exist.", airavataExperimentId);
                throw new ExperimentNotFoundException("Requested experiment id " + airavataExperimentId + " does not exist in the system..");
            }
        } catch (RegistryException e1) {
            logger.errorId(airavataExperimentId, "Error while retrieving projects", e1);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while retrieving projects. More info : " + e1.getMessage());
            throw exception;
        }

        final String expID = airavataExperimentId;
        final String token = airavataCredStoreToken;
        synchronized (this) {
    		Experiment experiment = getExperiment(expID);
			ExecutionType executionType = DataModelUtils.getExecutionType(experiment);
			Thread thread = null;
			if (executionType==ExecutionType.SINGLE_APP) {
                //its an single application execution experiment
                logger.debugId(airavataExperimentId, "Launching single application experiment {}.", airavataExperimentId);
                final OrchestratorService.Client orchestratorClient = getOrchestratorClient();
                if (orchestratorClient.validateExperiment(expID)) {
                   AiravataServerThreadPoolExecutor.getFixedThreadPool().execute(new SingleAppExperimentRunner(expID, token, orchestratorClient));
                } else {
                    logger.errorId(airavataExperimentId, "Experiment validation failed. Please check the configurations.");
                    throw new InvalidRequestException("Experiment Validation Failed, please check the configuration");
                }

            } else if (executionType == ExecutionType.WORKFLOW){
					//its a workflow execution experiment
                logger.debugId(airavataExperimentId, "Launching workflow experiment {}.", airavataExperimentId);
					thread = new Thread() {
	                    public void run() {
	                        try {
                        		launchWorkflowExperiment(expID, token);
	                        } catch (TException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
							}
	                    }
	                };
                thread.start();
            } else {
                logger.errorId(airavataExperimentId, "Couldn't identify experiment type, experiment {} is neither single application nor workflow.", airavataExperimentId);
                throw new InvalidRequestException("Experiment '" + expID + "' launch failed. Unable to figureout execution type for application " + experiment.getApplicationId());
            }
        }
    }

    private void launchWorkflowExperiment(String experimentId, String airavataCredStoreToken) throws TException {
    	try {
			WorkflowEngine workflowEngine = WorkflowEngineFactory.getWorkflowEngine();
			workflowEngine.launchExperiment(experimentId, airavataCredStoreToken);
		} catch (WorkflowEngineException e) {
            logger.errorId(experimentId, "Error while launching experiment.", e);
        }
    }

    private class SingleAppExperimentRunner implements Runnable {

        String experimentId;
        String airavataCredStoreToken;
        Client client;
        public SingleAppExperimentRunner(String experimentId,String airavataCredStoreToken,Client client){
            this.experimentId = experimentId;
            this.airavataCredStoreToken = airavataCredStoreToken;
            this.client = client;
        }
        @Override
        public void run() {
            try {
                launchSingleAppExperiment();
            } catch (TException e) {
                e.printStackTrace();
            }
        }

        private boolean launchSingleAppExperiment() throws TException {
            Experiment experiment = null;
            try {
                List<String> ids = registry.getIds(RegistryModelType.WORKFLOW_NODE_DETAIL, WorkflowNodeConstants.EXPERIMENT_ID, experimentId);
                for (String workflowNodeId : ids) {
//                WorkflowNodeDetails workflowNodeDetail = (WorkflowNodeDetails) registry.get(RegistryModelType.WORKFLOW_NODE_DETAIL, workflowNodeId);
                    List<Object> taskDetailList = registry.get(RegistryModelType.TASK_DETAIL, TaskDetailConstants.NODE_ID, workflowNodeId);
                    for (Object o : taskDetailList) {
                        TaskDetails taskData = (TaskDetails) o;
                        //iterate through all the generated tasks and performs the job submisssion+monitoring
                        experiment = (Experiment) registry.get(RegistryModelType.EXPERIMENT, experimentId);
                        if (experiment == null) {
                            logger.errorId(experimentId, "Error retrieving the Experiment by the given experimentID: {}", experimentId);
                            return false;
                        }
                        ExperimentStatus status = new ExperimentStatus();
                        status.setExperimentState(ExperimentState.LAUNCHED);
                        status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                        experiment.setExperimentStatus(status);
                        registry.update(RegistryModelType.EXPERIMENT_STATUS, status, experimentId);
                        if (ServerSettings.isRabbitMqPublishEnabled()) {
                            String gatewayId = ServerSettings.getDefaultUserGateway();
                            ExperimentStatusChangeEvent event = new ExperimentStatusChangeEvent(ExperimentState.LAUNCHED,
                                    experimentId,
                                    gatewayId);
                            String messageId = AiravataUtils.getId("EXPERIMENT");
                            MessageContext messageContext = new MessageContext(event, MessageType.EXPERIMENT, messageId, gatewayId);
                            messageContext.setUpdatedTime(AiravataUtils.getCurrentTimestamp());
                            publisher.publish(messageContext);
                        }
                        registry.update(RegistryModelType.TASK_DETAIL, taskData, taskData.getTaskID());
                        //launching the experiment
                        client.launchTask(taskData.getTaskID(), airavataCredStoreToken);
                    }
                }

            } catch (Exception e) {
                // Here we really do not have to do much because only potential failure can happen
                // is in gfac, if there are errors in gfac, it will handle the experiment/task/job statuses
                // We might get failures in registry access before submitting the jobs to gfac, in that case we
                // leave the status of these as created.
                ExperimentStatus status = new ExperimentStatus();
                status.setExperimentState(ExperimentState.FAILED);
                status.setTimeOfStateChange(Calendar.getInstance().getTimeInMillis());
                experiment.setExperimentStatus(status);
                try {
                    registry.update(RegistryModelType.EXPERIMENT_STATUS, status, experimentId);
                } catch (RegistryException e1) {
                    logger.errorId(experimentId, "Error while updating experiment status to " + status.toString(), e);
                    throw new TException(e);
                }
                logger.errorId(experimentId, "Error while updating task status, hence updated experiment status to " + status.toString(), e);
                throw new TException(e);
            } finally {
                client.getOutputProtocol().getTransport().close();
                client.getInputProtocol().getTransport().close();

            }
            return true;
        }
    }
    
	private OrchestratorService.Client getOrchestratorClient() {
		final int serverPort = Integer.parseInt(ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_PORT,"8940"));
        final String serverHost = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ORCHESTRATOR_SERVER_HOST, null);
        return OrchestratorClientFactory.createOrchestratorClient(serverHost, serverPort);
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
     *   The server-side generated airavata experiment globally unique identifier for the newly cloned experiment.
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
    public String cloneExperiment(String existingExperimentID, String newExperiementName) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
        try {
            registry = RegistryFactory.getDefaultRegistry();
            if (!registry.isExist(RegistryModelType.EXPERIMENT, existingExperimentID)){
                logger.errorId(existingExperimentID, "Error while cloning experiment {}, experiment doesn't exist.", existingExperimentID);
                throw new ExperimentNotFoundException("Requested experiment id " + existingExperimentID + " does not exist in the system..");
            }
            Experiment existingExperiment = (Experiment)registry.get(RegistryModelType.EXPERIMENT, existingExperimentID);
            existingExperiment.setCreationTime(AiravataUtils.getCurrentTimestamp().getTime());
            if (validateString(newExperiementName)){
                existingExperiment.setName(newExperiementName);
            }
            if (existingExperiment.getWorkflowNodeDetailsList() != null){
                existingExperiment.getWorkflowNodeDetailsList().clear();
            }
            if (existingExperiment.getErrors() != null ){
                existingExperiment.getErrors().clear();
            }
            return (String)registry.add(ParentDataType.EXPERIMENT, existingExperiment);
        } catch (Exception e) {
            logger.errorId(existingExperimentID, "Error while cloning the experiment with existing configuration...", e);
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
    public void terminateExperiment(String airavataExperimentId) throws InvalidRequestException, ExperimentNotFoundException, AiravataClientException, AiravataSystemException, TException {
    	Client client = getOrchestratorClient();
    	client.terminateExperiment(airavataExperimentId);
    }

    /**
     * Register a Application Module.
     *
     * @param applicationModule Application Module Object created from the datamodel.
     * @return appModuleId
     * Returns a server-side generated airavata appModule globally unique identifier.
     */
    @Override
    public String registerApplicationModule(ApplicationModule applicationModule) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().addApplicationModule(applicationModule);
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
    public ApplicationModule getApplicationModule(String appModuleId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.errorId(appModuleId, "Error while retrieving application module...", e);
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
    public boolean updateApplicationModule(String appModuleId, ApplicationModule applicationModule) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationModule(appModuleId, applicationModule);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(appModuleId, "Error while updating application module...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating application module. More info : " + e.getMessage());
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
    public boolean deleteApplicationModule(String appModuleId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().removeApplicationModule(appModuleId);
        } catch (AppCatalogException e) {
            logger.errorId(appModuleId, "Error while deleting application module...", e);
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
    public String registerApplicationDeployment(ApplicationDeploymentDescription applicationDeployment) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationDeployment().addApplicationDeployment(applicationDeployment);
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
    public ApplicationDeploymentDescription getApplicationDeployment(String appDeploymentId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationDeployment().getApplicationDeployement(appDeploymentId);
        } catch (AppCatalogException e) {
            logger.errorId(appDeploymentId, "Error while retrieving application deployment...", e);
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
    public boolean updateApplicationDeployment(String appDeploymentId, ApplicationDeploymentDescription applicationDeployment) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().updateApplicationDeployment(appDeploymentId, applicationDeployment);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(appDeploymentId, "Error while updating application deployment...", e);
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
    public boolean deleteApplicationDeployment(String appDeploymentId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getApplicationDeployment().removeAppDeployment(appDeploymentId);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(appDeploymentId, "Error while deleting application deployment...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting application deployment. More info : " + e.getMessage());
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
    public List<String> getAppModuleDeployedResources(String appModuleId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            List<String> appDeployments = new ArrayList<String>();
            appCatalog = AppCatalogFactory.getAppCatalog();
            Map<String, String> filters = new HashMap<String, String>();
            filters.put(AbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, appModuleId);
            List<ApplicationDeploymentDescription> applicationDeployments = appCatalog.getApplicationDeployment().getApplicationDeployements(filters);
            for (ApplicationDeploymentDescription description : applicationDeployments){
                appDeployments.add(description.getAppDeploymentId());
            }
            return appDeployments;
        } catch (AppCatalogException e) {
            logger.errorId(appModuleId, "Error while retrieving application deployments...", e);
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
    public String registerApplicationInterface(ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().addApplicationInterface(applicationInterface);
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
    public ApplicationInterfaceDescription getApplicationInterface(String appInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while retrieving application interface...", e);
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
    public boolean updateApplicationInterface(String appInterfaceId, ApplicationInterfaceDescription applicationInterface) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getApplicationInterface().updateApplicationInterface(appInterfaceId, applicationInterface);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while updating application interface...", e);
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
    public boolean deleteApplicationInterface(String appInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().removeApplicationInterface(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while deleting application interface...", e);
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
    public Map<String, String> getAllApplicationInterfaceNames() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            List<ApplicationInterfaceDescription> allApplicationInterfaces = appCatalog.getApplicationInterface().getAllApplicationInterfaces();
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
    public List<ApplicationInterfaceDescription> getAllApplicationInterfaces() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getAllApplicationInterfaces();
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
    public List<InputDataObjectType> getApplicationInputs(String appInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationInputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while retrieving application inputs...", e);
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
    public List<OutputDataObjectType> getApplicationOutputs(String appInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getApplicationInterface().getApplicationOutputs(appInterfaceId);
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while retrieving application outputs...", e);
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
    public Map<String, String> getAvailableAppInterfaceComputeResources(String appInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ApplicationDeployment applicationDeployment = appCatalog.getApplicationDeployment();
            Map<String, String> allComputeResources = appCatalog.getComputeResource().getAllComputeResourceIdList();
            Map<String, String> availableComputeResources = new HashMap<String, String>();
            ApplicationInterfaceDescription applicationInterface =
                    appCatalog.getApplicationInterface().getApplicationInterface(appInterfaceId);
            HashMap<String, String> filters = new HashMap<String,String>();
            List<String> applicationModules = applicationInterface.getApplicationModules();
            if (applicationModules != null && !applicationModules.isEmpty()){
                for (String moduleId : applicationModules) {
                    filters.put(AbstractResource.ApplicationDeploymentConstants.APP_MODULE_ID, moduleId);
                    List<ApplicationDeploymentDescription> applicationDeployments =
                            applicationDeployment.getApplicationDeployements(filters);
                    for (ApplicationDeploymentDescription deploymentDescription : applicationDeployments) {
                        availableComputeResources.put(deploymentDescription.getComputeHostId(),
                                allComputeResources.get(deploymentDescription.getComputeHostId()));
                    }
                }
            }
            return availableComputeResources;
        } catch (AppCatalogException e) {
            logger.errorId(appInterfaceId, "Error while saving compute resource...", e);
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
    public String registerComputeResource(ComputeResourceDescription computeResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
    public ComputeResourceDescription getComputeResource(String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getComputeResource(computeResourceId);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while retrieving compute resource...", e);
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
    public Map<String, String> getAllComputeResourceNames() throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
    public boolean updateComputeResource(String computeResourceId, ComputeResourceDescription computeResourceDescription) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getComputeResource().updateComputeResource(computeResourceId, computeResourceDescription);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while updating compute resource...", e);
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
    public boolean deleteComputeResource(String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getComputeResource().removeComputeResource(computeResourceId);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while deleting compute resource...", e);
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
    public String addLocalSubmissionDetails(String computeResourceId, int priorityOrder, LOCALSubmission localSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
            		computeResource.addLocalJobSubmission(localSubmission), JobSubmissionProtocol.LOCAL, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
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
    public boolean updateLocalSubmissionDetails(String jobSubmissionInterfaceId, LOCALSubmission localSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            LocalSubmissionResource submission = AppCatalogThriftConversion.getLocalJobSubmission(localSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    public String addSSHJobSubmissionDetails(String computeResourceId, int priorityOrder, SSHJobSubmission sshJobSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
            		computeResource.addSSHJobSubmission(sshJobSubmission), JobSubmissionProtocol.SSH, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    public String addCloudJobSubmissionDetails(String computeResourceId, int priorityOrder, CloudJobSubmission cloudJobSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addJobSubmissionInterface(computeResource, computeResourceId,
                    computeResource.addCloudJobSubmission(cloudJobSubmission), JobSubmissionProtocol.CLOUD, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    @Override
	public String addUNICOREJobSubmissionDetails(String computeResourceId,
			int priorityOrder, UnicoreJobSubmission unicoreJobSubmission)
			throws InvalidRequestException, AiravataClientException,
			AiravataSystemException, TException {
		try {
	        appCatalog = AppCatalogFactory.getAppCatalog();
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

	/**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param sshJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean updateSSHJobSubmissionDetails(String jobSubmissionInterfaceId, SSHJobSubmission sshJobSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            SshJobSubmissionResource submission = AppCatalogThriftConversion.getSSHJobSubmission(sshJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
            throw exception;
        }
    }

    /**
     * Update the given SSH Job Submission details
     *
     * @param jobSubmissionInterfaceId The identifier of the JobSubmission Interface to be updated.
     * @param cloudJobSubmission         The SSHJobSubmission object to be updated.
     * @return status
     * Returns a success/failure of the deletion.
     */
    @Override
    public boolean updateCloudJobSubmissionDetails(String jobSubmissionInterfaceId, CloudJobSubmission cloudJobSubmission) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            CloudSubmissionResource submission = AppCatalogThriftConversion.getCloudJobSubmission(cloudJobSubmission);
            submission.setJobSubmissionInterfaceId(jobSubmissionInterfaceId);
            submission.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
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
    public String addLocalDataMovementDetails(String computeResourceId, int priorityOrder, LOCALDataMovement localDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addLocalDataMovement(localDataMovement), DataMovementProtocol.LOCAL, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
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
    public boolean updateLocalDataMovementDetails(String jobSubmissionInterfaceId, LOCALDataMovement localDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            LocalDataMovementResource movment = AppCatalogThriftConversion.getLocalDataMovement(localDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    public String addSCPDataMovementDetails(String computeResourceId, int priorityOrder, SCPDataMovement scpDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addScpDataMovement(scpDataMovement), DataMovementProtocol.SCP, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
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
    public boolean updateSCPDataMovementDetails(String jobSubmissionInterfaceId, SCPDataMovement scpDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            ScpDataMovementResource movment = AppCatalogThriftConversion.getSCPDataMovementDescription(scpDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    public String addGridFTPDataMovementDetails(String computeResourceId, int priorityOrder, GridFTPDataMovement gridFTPDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            ComputeResource computeResource = appCatalog.getComputeResource();
            return addDataMovementInterface(computeResource, computeResourceId,
            		computeResource.addGridFTPDataMovement(gridFTPDataMovement), DataMovementProtocol.GridFTP, priorityOrder);
        } catch (AppCatalogException e) {
            logger.errorId(computeResourceId, "Error while adding data movement interface to resource compute resource...", e);
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
    public boolean updateGridFTPDataMovementDetails(String jobSubmissionInterfaceId, GridFTPDataMovement gridFTPDataMovement) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            GridftpDataMovementResource movment = AppCatalogThriftConversion.getGridFTPDataMovementDescription(gridFTPDataMovement);
            movment.setDataMovementInterfaceId(jobSubmissionInterfaceId);
            movment.save();
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while adding job submission interface to resource compute resource...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while adding job submission interface to resource compute resource. More info : " + e.getMessage());
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
    public boolean changeJobSubmissionPriority(String jobSubmissionInterfaceId, int newPriorityOrder) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
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
    public boolean changeDataMovementPriority(String dataMovementInterfaceId, int newPriorityOrder) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
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
    public boolean changeJobSubmissionPriorities(Map<String, Integer> jobSubmissionPriorityMap) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
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
    public boolean changeDataMovementPriorities(Map<String, Integer> dataMovementPriorityMap) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
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
    public boolean deleteJobSubmissionInterface(String jobSubmissionInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getComputeResource().removeJobSubmissionInterface(jobSubmissionInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(jobSubmissionInterfaceId, "Error while deleting job submission interface...", e);
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
    public boolean deleteDataMovementInterface(String dataMovementInterfaceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
        try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            appCatalog.getComputeResource().removeDataMovementInterface(dataMovementInterfaceId);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(dataMovementInterfaceId, "Error while deleting data movement interface...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while deleting data movement interface. More info : " + e.getMessage());
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
    public String registerGatewayResourceProfile(GatewayResourceProfile gatewayResourceProfile) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
    public GatewayResourceProfile getGatewayResourceProfile(String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID);
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while retrieving gateway resource profile...", e);
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
    public boolean updateGatewayResourceProfile(String gatewayID, GatewayResourceProfile gatewayResourceProfile) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.updateGatewayResourceProfile(gatewayID, gatewayResourceProfile);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while updating gateway resource profile...", e);
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
    public boolean deleteGatewayResourceProfile(String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            gatewayProfile.removeGatewayResourceProfile(gatewayID);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while removing gateway resource profile...", e);
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
    public boolean addGatewayComputeResourcePreference(String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
            logger.errorId(gatewayID, "Error while registering gateway resource profile preference...", e);
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
    public ComputeResourcePreference getGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            ComputeResource computeResource = appCatalog.getComputeResource();
            if (!gatewayProfile.isGatewayResourceProfileExists(gatewayID)){
                logger.errorId(gatewayID, "Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given gateway profile does not exist in the system. Please provide a valid gateway id...");
                throw exception;
            }
            if (!computeResource.isComputeResourceExists(computeResourceId)){
                logger.errorId(computeResourceId, "Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                AiravataSystemException exception = new AiravataSystemException();
                exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
                exception.setMessage("Given compute resource does not exist in the system. Please provide a valid compute resource id...");
                throw exception;
            }
            return gatewayProfile.getComputeResourcePreference(gatewayID, computeResourceId);
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preference. More info : " + e.getMessage());
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
    public List<ComputeResourcePreference> getAllGatewayComputeResourcePreferences(String gatewayID) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
            GwyResourceProfile gatewayProfile = appCatalog.getGatewayProfile();
            return gatewayProfile.getGatewayProfile(gatewayID).getComputeResourcePreferences();
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while reading gateway compute resource preferences...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while reading gateway compute resource preferences. More info : " + e.getMessage());
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
    public boolean updateGatewayComputeResourcePreference(String gatewayID, String computeResourceId, ComputeResourcePreference computeResourcePreference) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
            logger.errorId(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
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
    public boolean deleteGatewayComputeResourcePreference(String gatewayID, String computeResourceId) throws InvalidRequestException, AiravataClientException, AiravataSystemException, TException {
    	try {
            appCatalog = AppCatalogFactory.getAppCatalog();
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
            gatewayProfile.updateGatewayResourceProfile(gatewayID, profile);
            return true;
        } catch (AppCatalogException e) {
            logger.errorId(gatewayID, "Error while reading gateway compute resource preference...", e);
            AiravataSystemException exception = new AiravataSystemException();
            exception.setAiravataErrorType(AiravataErrorType.INTERNAL_ERROR);
            exception.setMessage("Error while updating gateway compute resource preference. More info : " + e.getMessage());
            throw exception;
        }
    }

}
