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
package org.apache.airavata.persistance.registry.jpa.resources;

import java.net.URI;

import org.apache.airavata.persistance.registry.jpa.JPAConstants;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.registry.api.AiravataRegistryConnectionDataProvider;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.exception.RegistrySettingsException;
import org.apache.airavata.registry.api.util.RegistrySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utils {
    private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String getJDBCFullURL(){
		String jdbcUrl = getJDBCURL();
		String jdbcUser = getJDBCUser();
		String jdbcPassword = getJDBCPassword();
        jdbcUrl = jdbcUrl + "?"  + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;
        return jdbcUrl;
    }

    public static String getJDBCURL(){
    	try {
            return getProvider().getValue(JPAConstants.KEY_JDBC_URL).toString();
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static String getHost(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getHost();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static int getPort(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static String getDBType(){
        try{
            String jdbcURL = getJDBCURL();
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getScheme();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static boolean isDerbyStartEnabled(){
        try {
            String s = getProvider().getValue(JPAConstants.KEY_DERBY_START_ENABLE).toString();
            if("true".equals(s)){
                return true;
            }
        } catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

	private static AiravataRegistryConnectionDataProvider getProvider() {
		return AiravataRegistryFactory.getRegistryConnectionDataProvider();
	}

    static {
        if(AiravataRegistryFactory.getRegistryConnectionDataProvider() == null){
            AiravataRegistryFactory.registerRegistryConnectionDataProvider(new AiravataRegistryConnectionDataProviderImpl());
        }

    }

    public static String getJDBCUser(){
    	try {
			if (getProvider()!=null){
				return getProvider().getValue(JPAConstants.KEY_JDBC_USER).toString();
			} else {
                return RegistrySettings.getSetting(JPAConstants.KEY_JDBC_USER);
            }
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getValidationQuery(){
    	try {
			if (getProvider()!=null){
                if(getProvider().getValue(JPAConstants.VALIDATION_QUERY) != null){
				    return getProvider().getValue(JPAConstants.VALIDATION_QUERY).toString();
                }
			} else {
                if(getProvider().getValue(JPAConstants.VALIDATION_QUERY) != null){
                    return RegistrySettings.getSetting(JPAConstants.VALIDATION_QUERY);
                }
            }
            return "";
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

     public static String getJPAConnectionProperties(){
    	try {
			if (getProvider()!=null){
                if(getProvider().getValue(JPAConstants.CONNECTION_JPA_PROPERTY) != null){
				    return getProvider().getValue(JPAConstants.CONNECTION_JPA_PROPERTY).toString();
                }
			} else {
                if(getProvider().getValue(JPAConstants.CONNECTION_JPA_PROPERTY) != null){
                    return RegistrySettings.getSetting(JPAConstants.CONNECTION_JPA_PROPERTY);
                }
            }
            return "";
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    public static String getJDBCPassword(){
    	try {
			if (getProvider()!=null){
				return getProvider().getValue(JPAConstants.KEY_JDBC_PASSWORD).toString();
			}else {
                return RegistrySettings.getSetting(JPAConstants.KEY_JDBC_PASSWORD);
            }
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}

    }

    public static String getJDBCDriver(){
    	try {
			if (getProvider()!=null){
				return getProvider().getValue(JPAConstants.KEY_JDBC_DRIVER).toString();
			}  else {
                return RegistrySettings.getSetting(JPAConstants.KEY_JDBC_DRIVER);
            }
		} catch (RegistrySettingsException e) {
            logger.error(e.getMessage(), e);
            return null;
		}
    }

    /**
     *
     * @param type model type
     * @param o model type instance
     * @return corresponding resource object
     */
    public static Resource getResource(ResourceType type, Object o) {
        switch (type){
            case GATEWAY:
                if (o instanceof Gateway) {
                    return createGateway((Gateway) o);
                } else {
                    logger.error("Object should be a Gateway.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Gateway.");
                }
            case PROJECT:
                if (o instanceof Project){
                    return createProject((Project) o);
                } else {
                    logger.error("Object should be a Project.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Project.");
                }
            case CONFIGURATION:
                if(o instanceof Configuration){
                    return createConfiguration((Configuration) o);
                }else {
                    logger.error("Object should be a Configuration.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Configuration.");
                }
            case APPLICATION_DESCRIPTOR:
                if (o instanceof Application_Descriptor){
                    return createApplicationDescriptor((Application_Descriptor) o);
                } else {
                    logger.error("Object should be a Application Descriptor.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Application Descriptor.");
                }
            case USER:
                if(o instanceof Users) {
                    return createUser((Users) o);
                }else {
                    logger.error("Object should be a User.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a User.");
                }
            case HOST_DESCRIPTOR:
                if (o instanceof Host_Descriptor){
                    return createHostDescriptor((Host_Descriptor) o);
                }else {
                    logger.error("Object should be a Host Descriptor.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Host Descriptor.");
                }
            case SERVICE_DESCRIPTOR:
                if (o instanceof Service_Descriptor){
                    return createServiceDescriptor((Service_Descriptor) o);
                }else {
                    logger.error("Object should be a Service Descriptor.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Service Descriptor.");
                }
            case PUBLISHED_WORKFLOW:
                if (o instanceof Published_Workflow){
                    return createPublishWorkflow((Published_Workflow) o);
                }else {
                    logger.error("Object should be a Publish Workflow.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Publish Workflow.");
                }
            case USER_WORKFLOW:
                if (o instanceof User_Workflow){
                    return createUserWorkflow((User_Workflow) o);
                }else {
                    logger.error("Object should be a User Workflow.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a User Workflow.");
                }
            case GATEWAY_WORKER:
                if (o instanceof Gateway_Worker){
                    return createGatewayWorker((Gateway_Worker)o);
                } else {
                    logger.error("Object should be a Gateway Worker.", new IllegalArgumentException());
                    throw  new IllegalArgumentException("Object should be a Gateway Worker.");
                }
            case EXPERIMENT:
                if (o instanceof  Experiment){
                    return createExperiment((Experiment)o);
                }else {
                    logger.error("Object should be a Experiment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment.");
                }
            case EXPERIMENT_INPUT:
                if (o instanceof  Experiment_Input){
                    return createExperimentInput((Experiment_Input)o);
                }else {
                    logger.error("Object should be a Experiment input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment input data.");
                }
            case EXPERIMENT_OUTPUT:
                if (o instanceof  Experiment_Output){
                    return createExperimentOutput((Experiment_Output)o);
                }else {
                    logger.error("Object should be a Experiment output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment output data.");
                }
            case WORKFLOW_NODE_DETAIL:
                 if (o instanceof  WorkflowNodeDetail){
                     return createWorkflowNodeDetail((WorkflowNodeDetail)o);
                 }else {
                     logger.error("Object should be a Workflow node data.", new IllegalArgumentException());
                     throw new IllegalArgumentException("Object should be a Workflow node data.");
                 }
            case TASK_DETAIL:
                if (o instanceof  TaskDetail){
                    return createTaskDetail((TaskDetail)o);
                }else {
                    logger.error("Object should be a task detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a task detail data.");
                }
            case ERROR_DETAIL:
                if (o instanceof  ErrorDetail){
                    return createErrorDetail((ErrorDetail)o);
                }else {
                    logger.error("Object should be a error detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a error detail data.");
                }
            case APPLICATION_INPUT:
                if (o instanceof  ApplicationInput){
                    return createApplicationInput((ApplicationInput)o);
                }else {
                    logger.error("Object should be a application input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a application input data.");
                }
            case APPLICATION_OUTPUT:
                if (o instanceof  ApplicationOutput){
                    return createApplicationOutput((ApplicationOutput)o);
                }else {
                    logger.error("Object should be a application output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a application output data.");
                }
            case NODE_INPUT:
                if (o instanceof  NodeInput){
                    return createNodeInput((NodeInput)o);
                }else {
                    logger.error("Object should be a node input data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a node input data.");
                }
            case NODE_OUTPUT:
                if (o instanceof  NodeOutput){
                    return createNodeOutput((NodeOutput)o);
                }else {
                    logger.error("Object should be a node output data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a node output data.");
                }
            case JOB_DETAIL:
                if (o instanceof  JobDetail){
                    return createJobDetail((JobDetail)o);
                }else {
                    logger.error("Object should be a job detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a job detail data.");
                }
            case DATA_TRANSFER_DETAIL:
                if (o instanceof  DataTransferDetail){
                    return createDataTransferResource((DataTransferDetail)o);
                }else {
                    logger.error("Object should be a data transfer detail data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a data transfer detail data.");
                }
            case STATUS:
                if (o instanceof  Status){
                    return createStatusResource((Status)o);
                }else {
                    logger.error("Object should be a status data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a status data.");
                }
            case CONFIG_DATA:
                if (o instanceof  ExperimentConfigData){
                    return createExConfigDataResource((ExperimentConfigData)o);
                }else {
                    logger.error("Object should be a experiment config data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be experiment config data.");
                }
            case COMPUTATIONAL_RESOURCE_SCHEDULING:
                if (o instanceof  Computational_Resource_Scheduling){
                    return createComputationalScheduling((Computational_Resource_Scheduling)o);
                }else {
                    logger.error("Object should be a scheduling resource data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be scheduling resource data.");
                }
            case ADVANCE_INPUT_DATA_HANDLING:
                if (o instanceof  AdvancedInputDataHandling){
                    return createAdvancedInputDataResource((AdvancedInputDataHandling)o);
                }else {
                    logger.error("Object should be a advanced input data handling data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be advanced input data handling data.");
                }
            case ADVANCE_OUTPUT_DATA_HANDLING:
                if (o instanceof  AdvancedOutputDataHandling){
                    return createAdvancedOutputDataResource((AdvancedOutputDataHandling)o);
                }else {
                    logger.error("Object should be a advanced output data handling data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be advanced output data handling data.");
                }
            case QOS_PARAM:
                if (o instanceof  QosParam){
                    return createQosParamResource((QosParam)o);
                }else {
                    logger.error("Object should be a QOSparam data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be QOSparam data.");
                }
            default:
                logger.error("Illegal data type..", new IllegalArgumentException());
                throw new IllegalArgumentException("Illegal data type..");
        }
    }

    /**
     *
     * @param o  Gateway model object
     * @return  GatewayResource object
     */
    private static Resource createGateway(Gateway o) {
        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName(o.getGateway_name());
        return gatewayResource;
    }

    /**
     *
     * @param o Project model object
     * @return ProjectResource object
     */
    private static Resource createProject(Project o) {
        ProjectResource projectResource = new ProjectResource();
        projectResource.setName(o.getProject_name());
        GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
        projectResource.setGateway(gatewayResource);
        Gateway_Worker gateway_worker = new Gateway_Worker();
        gateway_worker.setGateway(o.getGateway());
        gateway_worker.setUser(o.getUsers());
        WorkerResource workerResource = (WorkerResource) createGatewayWorker(gateway_worker);
        projectResource.setWorker(workerResource);
        return projectResource;
    }

    /**
     *
     * @param o configuration model object
     * @return configuration resource object
     */
    private static Resource createConfiguration (Configuration o){
        ConfigurationResource configurationResource = new ConfigurationResource();
        configurationResource.setConfigKey(o.getConfig_key());
        configurationResource.setConfigVal(o.getConfig_val());
        configurationResource.setExpireDate(o.getExpire_date());
        configurationResource.setCategoryID(o.getCategory_id());
        return configurationResource;
    }

    /**
     *
     * @param o application descriptor model object
     * @return  application descriptor resource object
     */
    private static Resource createApplicationDescriptor(Application_Descriptor o) {
        ApplicationDescriptorResource applicationDescriptorResource = new ApplicationDescriptorResource();
        applicationDescriptorResource.setName(o.getApplication_descriptor_ID());
        applicationDescriptorResource.setHostDescName(o.getHost_descriptor_ID());
        applicationDescriptorResource.setServiceDescName(o.getService_descriptor_ID());
        applicationDescriptorResource.setContent(new String(o.getApplication_descriptor_xml()));
        applicationDescriptorResource.setUpdatedUser(o.getUser().getUser_name());
        applicationDescriptorResource.setGatewayName(o.getGateway().getGateway_name());
        return applicationDescriptorResource;
    }



    /**
     *
     * @param o Gateway_Worker model object
     * @return  Gateway_Worker resource object
     */
    private static Resource createGatewayWorker(Gateway_Worker o) {
        GatewayResource gatewayResource = new GatewayResource(o.getGateway().getGateway_name());
        gatewayResource.setOwner(o.getGateway().getOwner());
        WorkerResource workerResource = new WorkerResource(o.getUser_name(), gatewayResource);
        return workerResource;
    }

    /**
     *
     * @param o Host_Descriptor model object
     * @return  HostDescriptor resource object
     */
    private static Resource createHostDescriptor(Host_Descriptor o) {
        try {
            HostDescriptorResource hostDescriptorResource = new HostDescriptorResource();
            hostDescriptorResource.setGatewayName(o.getGateway().getGateway_name());
            hostDescriptorResource.setUserName(o.getUser().getUser_name());
            hostDescriptorResource.setHostDescName(o.getHost_descriptor_ID());
            byte[] bytes = o.getHost_descriptor_xml();
            hostDescriptorResource.setContent(new String(bytes));
            return hostDescriptorResource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param o  Published_Workflow model object
     * @return  Published Workflow resource object
     */
    private static Resource createPublishWorkflow(Published_Workflow o) {
        PublishWorkflowResource publishWorkflowResource = new PublishWorkflowResource();
        GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
        publishWorkflowResource.setGateway(gatewayResource);
        publishWorkflowResource.setCreatedUser(o.getUser().getUser_name());
        publishWorkflowResource.setName(o.getPublish_workflow_name());
        publishWorkflowResource.setContent(new String(o.getWorkflow_content()));
        publishWorkflowResource.setPublishedDate(o.getPublished_date());
        publishWorkflowResource.setVersion(o.getVersion());
        publishWorkflowResource.setPath(o.getPath());
        return publishWorkflowResource;
    }

    /**
     *
     * @param o Service_Descriptor model object
     * @return ServiceDescriptor resource object
     */
    private static Resource createServiceDescriptor(Service_Descriptor o) {
        ServiceDescriptorResource serviceDescriptorResource = new ServiceDescriptorResource();
        serviceDescriptorResource.setGatewayName(o.getGateway().getGateway_name());
        serviceDescriptorResource.setUserName(o.getUser().getUser_name());
        serviceDescriptorResource.setServiceDescName(o.getService_descriptor_ID());
        serviceDescriptorResource.setContent(new String(o.getService_descriptor_xml()));
        return serviceDescriptorResource;
    }

    /**
     *
     * @param o User_Workflow model object
     * @return User_Workflow resource object
     */
    private static Resource createUserWorkflow(User_Workflow o) {
        UserWorkflowResource userWorkflowResource = new UserWorkflowResource();
        userWorkflowResource.setName(o.getTemplate_name());
        GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
        userWorkflowResource.setGateway(gatewayResource);
        Gateway_Worker gateway_worker = new Gateway_Worker();
        gateway_worker.setGateway(o.getGateway());
        gateway_worker.setUser(o.getUser());
        WorkerResource workerResource = (WorkerResource) createGatewayWorker(gateway_worker);
        userWorkflowResource.setWorker(workerResource);
        userWorkflowResource.setLastUpdateDate(o.getLast_updated_date());
        userWorkflowResource.setContent(new String(o.getWorkflow_graph()));
        userWorkflowResource.setPath(o.getPath());
        return userWorkflowResource;
    }

    /**
     *
     * @param o  Users model object
     * @return  UserResource object
     */
    private static Resource createUser(Users o) {
        UserResource userResource = new UserResource();
        userResource.setUserName(o.getUser_name());
        userResource.setPassword(o.getPassword());
        return userResource;
    }

    /**
     * @param o Experiment model object
     * @return  Experiment resource object
     */
    private static Resource createExperiment(Experiment o) {
        ExperimentResource experimentResource = new ExperimentResource();
        GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
        experimentResource.setGateway(gatewayResource);
//        Gateway_Worker gateway_worker = new Gateway_Worker();
//        gateway_worker.setGateway(o.getGateway());
//        gateway_worker.setUser_name(o.getExecutionUser());
        experimentResource.setExecutionUser(o.getExecutionUser());
        if (o.getProject() != null){
            ProjectResource projectResource = (ProjectResource)createProject(o.getProject());
            experimentResource.setProject(projectResource);
        }
        experimentResource.setExpID(o.getExpId());
        experimentResource.setCreationTime(o.getCreationTime());
        experimentResource.setDescription(o.getExpDesc());
        experimentResource.setApplicationId(o.getApplicationId());
        experimentResource.setApplicationVersion(o.getAppVersion());
        experimentResource.setWorkflowTemplateId(o.getWorkflowTemplateId());
        experimentResource.setWorkflowTemplateVersion(o.getWorkflowTemplateVersion());
        experimentResource.setWorkflowExecutionId(o.getWorkflowExecutionId());
        return experimentResource;
    }

    private static Resource createExperimentInput (Experiment_Input o){
        ExperimentInputResource eInputResource = new ExperimentInputResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        eInputResource.setExperimentResource(experimentResource);
        eInputResource.setInputType(o.getInputType());
        eInputResource.setMetadata(o.getMetadata());
        eInputResource.setExperimentKey(o.getEx_key());
        eInputResource.setValue(o.getValue());
        return eInputResource;
    }

    private static Resource createExperimentOutput (Experiment_Output o){
        ExperimentOutputResource eOutputResource = new ExperimentOutputResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        eOutputResource.setExperimentResource(experimentResource);
        eOutputResource.setExperimentKey(o.getEx_key());
        eOutputResource.setValue(o.getValue());
        eOutputResource.setOutputType(o.getOutputKeyType());
        eOutputResource.setMetadata(o.getMetadata());
        return eOutputResource;
    }

    private static Resource createWorkflowNodeDetail (WorkflowNodeDetail o){
        WorkflowNodeDetailResource nodeDetailResource = new WorkflowNodeDetailResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        nodeDetailResource.setExperimentResource(experimentResource);
        nodeDetailResource.setCreationTime(o.getCreationTime());
        nodeDetailResource.setNodeInstanceId(o.getNodeId());
        nodeDetailResource.setNodeName(o.getNodeName());
        return nodeDetailResource;
    }

    private static Resource createTaskDetail(TaskDetail o){
        TaskDetailResource taskDetailResource = new TaskDetailResource();
        WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetail());
        taskDetailResource.setWorkflowNodeDetailResource(nodeDetailResource);
        taskDetailResource.setCreationTime(o.getCreationTime());
        taskDetailResource.setTaskId(o.getTaskId());
        taskDetailResource.setApplicationId(o.getAppId());
        taskDetailResource.setApplicationVersion(o.getAppVersion());
        return taskDetailResource;
    }

    private static Resource createErrorDetail (ErrorDetail o){
        ErrorDetailResource errorDetailResource = new ErrorDetailResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        errorDetailResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            errorDetailResource.setTaskDetailResource(taskDetailResource);
        }
        if (o.getNodeDetails() != null){
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetails());
            errorDetailResource.setNodeDetail(nodeDetailResource);
        }
        errorDetailResource.setErrorId(o.getErrorID());
        errorDetailResource.setJobId(o.getJobId());
        errorDetailResource.setCreationTime(o.getCreationTime());
        errorDetailResource.setActualErrorMsg(new String(o.getActualErrorMsg()));
        errorDetailResource.setUserFriendlyErrorMsg(o.getUserFriendlyErrorMsg());
        errorDetailResource.setTransientPersistent(o.isTransientPersistent());
        errorDetailResource.setErrorCategory(o.getErrorCategory());
        errorDetailResource.setCorrectiveAction(o.getCorrectiveAction());
        errorDetailResource.setActionableGroup(o.getActionableGroup());
        return errorDetailResource;
    }

    private static Resource createApplicationInput (ApplicationInput o){
        ApplicationInputResource inputResource = new ApplicationInputResource();
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        inputResource.setTaskDetailResource(taskDetailResource);
        inputResource.setInputKey(o.getInputKey());
        inputResource.setInputType(o.getInputKeyType());
        inputResource.setValue(o.getValue());
        inputResource.setMetadata(o.getMetadata());
        return inputResource;
    }

    private static Resource createApplicationOutput (ApplicationOutput o){
        ApplicationOutputResource outputResource = new ApplicationOutputResource();
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        outputResource.setTaskDetailResource(taskDetailResource);
        outputResource.setOutputType(o.getOutputKeyType());
        outputResource.setOutputKey(o.getOutputKey());
        outputResource.setValue(o.getValue());
        outputResource.setMetadata(o.getMetadata());
        return outputResource;
    }

    private static Resource createNodeInput (NodeInput o){
        NodeInputResource inputResource = new NodeInputResource();
        WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetails());
        inputResource.setNodeDetailResource(nodeDetailResource);
        inputResource.setInputKey(o.getInputKey());
        inputResource.setInputType(o.getInputKeyType());
        inputResource.setValue(o.getValue());
        inputResource.setMetadata(o.getMetadata());
        return inputResource;
    }

    private static Resource createNodeOutput (NodeOutput o){
        NodeOutputResource outputResource = new NodeOutputResource();
        WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNode());
        outputResource.setNodeDetailResource(nodeDetailResource);
        outputResource.setOutputType(o.getOutputKeyType());
        outputResource.setOutputKey(o.getOutputKey());
        outputResource.setValue(o.getValue());
        outputResource.setMetadata(o.getMetadata());
        return outputResource;
    }

    private static Resource createJobDetail (JobDetail o){
        JobDetailResource jobDetailResource = new JobDetailResource();
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        jobDetailResource.setTaskDetailResource(taskDetailResource);
        jobDetailResource.setJobDescription(o.getJobDescription());
        jobDetailResource.setJobId(o.getJobId());
        jobDetailResource.setCreationTime(o.getCreationTime());
        jobDetailResource.setComputeResourceConsumed(o.getComputeResourceConsumed());
        return jobDetailResource;
    }

    private static Resource createDataTransferResource (DataTransferDetail o){
        DataTransferDetailResource transferDetailResource = new DataTransferDetailResource();
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        transferDetailResource.setTaskDetailResource(taskDetailResource);
        transferDetailResource.setTransferId(o.getTransferId());
        transferDetailResource.setCreationTime(o.getCreationTime());
        transferDetailResource.setTransferDescription(o.getTransferDesc());
        return transferDetailResource;
    }

    private static Resource createStatusResource (Status o){
        StatusResource statusResource = new StatusResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        statusResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            statusResource.setTaskDetailResource(taskDetailResource);
        }
        if (o.getNode() != null){
            WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNode());
            statusResource.setWorkflowNodeDetail(nodeDetailResource);
        }
        if (o.getTransferDetail() != null){
            DataTransferDetailResource transferDetailResource = (DataTransferDetailResource)createDataTransferResource(o.getTransferDetail());
            statusResource.setDataTransferDetail(transferDetailResource);
        }
        statusResource.setStatusId(o.getStatusId());
        statusResource.setJobId(o.getJobId());
        statusResource.setState(o.getState());
        statusResource.setStatusUpdateTime(o.getStatusUpdateTime());
        statusResource.setStatusType(o.getStatusType());
        return statusResource;
    }

    private static Resource createExConfigDataResource (ExperimentConfigData o){
        ConfigDataResource configDataResource = new ConfigDataResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        configDataResource.setExperimentResource(experimentResource);
        configDataResource.setAiravataAutoSchedule(o.isAiravataAutoSchedule());
        configDataResource.setOverrideManualParams(o.isOverrideManualParams());
        configDataResource.setShareExp(o.isShareExp());
        return configDataResource;
    }

    private static Resource createComputationalScheduling (Computational_Resource_Scheduling o){
        ComputationSchedulingResource schedulingResource = new ComputationSchedulingResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        schedulingResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            schedulingResource.setTaskDetailResource(taskDetailResource);
        }
        schedulingResource.setSchedulingId(o.getSchedulingId());
        schedulingResource.setResourceHostId(o.getResourceHostId());
        schedulingResource.setCpuCount(o.getCpuCount());
        schedulingResource.setNodeCount(o.getNodeCount());
        schedulingResource.setNumberOfThreads(o.getNumberOfThreads());
        schedulingResource.setQueueName(o.getQueueName());
        schedulingResource.setWalltimeLimit(o.getWallTimeLimit());
        schedulingResource.setJobStartTime(o.getJobStartTime());
        schedulingResource.setPhysicalMemory(o.getTotalPhysicalmemory());
        schedulingResource.setProjectName(o.getProjectName());
        return schedulingResource;
    }

    private static Resource createAdvancedInputDataResource (AdvancedInputDataHandling o){
        AdvanceInputDataHandlingResource dataHandlingResource = new AdvanceInputDataHandlingResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        dataHandlingResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            dataHandlingResource.setTaskDetailResource(taskDetailResource);
        }
        dataHandlingResource.setDataHandlingId(o.getDataHandlingId());
        dataHandlingResource.setWorkingDirParent(o.getParentWorkingDir());
        dataHandlingResource.setWorkingDir(o.getWorkingDir());
        dataHandlingResource.setStageInputFiles(o.isStageInputsToWorkingDir());
        dataHandlingResource.setCleanAfterJob(o.isCleanAfterJob());
        return dataHandlingResource;
    }

    private static Resource createAdvancedOutputDataResource (AdvancedOutputDataHandling o){
        AdvancedOutputDataHandlingResource dataHandlingResource = new AdvancedOutputDataHandlingResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        dataHandlingResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            dataHandlingResource.setTaskDetailResource(taskDetailResource);
        }
        dataHandlingResource.setOutputDataHandlingId(o.getOutputDataHandlingId());
        dataHandlingResource.setOutputDataDir(o.getOutputDataDir());
        dataHandlingResource.setDataRegUrl(o.getDataRegUrl());
        dataHandlingResource.setPersistOutputData(o.isPersistOutputData());
        return dataHandlingResource;
    }

    private static Resource createQosParamResource (QosParam o){
        QosParamResource qosParamResource = new QosParamResource();
        ExperimentResource experimentResource = (ExperimentResource)createExperiment(o.getExperiment());
        qosParamResource.setExperimentResource(experimentResource);
        if (o.getTask() != null){
            TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
            qosParamResource.setTaskDetailResource(taskDetailResource);
        }
        qosParamResource.setQosId(o.getQosId());
        qosParamResource.setExecuteBefore(o.getExecuteBefore());
        qosParamResource.setStartExecutionAt(o.getStartExecutionAt());
        qosParamResource.setNoOfRetries(o.getNoOfRetries());
        return qosParamResource;
    }
}
