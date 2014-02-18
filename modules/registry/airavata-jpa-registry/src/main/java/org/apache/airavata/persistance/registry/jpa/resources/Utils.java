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
            default:
        }
        return null;

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
        WorkerResource workerResource = new WorkerResource(o.getUser().getUser_name(), gatewayResource);
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
        Gateway_Worker gateway_worker = new Gateway_Worker();
        gateway_worker.setGateway(o.getGateway());
        gateway_worker.setUser(o.getUser());
        WorkerResource workerResource = (WorkerResource) createGatewayWorker(gateway_worker);
        experimentResource.setWorker(workerResource);
        ProjectResource projectResource = (ProjectResource)createProject(o.getProject());
        experimentResource.setProject(projectResource);
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
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        errorDetailResource.setTaskDetailResource(taskDetailResource);
        WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNodeDetails());
        errorDetailResource.setNodeDetail(nodeDetailResource);
        errorDetailResource.setErrorId(o.getErrorID());
        errorDetailResource.setCreationTime(o.getCreationTime());
        errorDetailResource.setActualErrorMsg(o.getActualErrorMsg());
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
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        statusResource.setTaskDetailResource(taskDetailResource);
        WorkflowNodeDetailResource nodeDetailResource = (WorkflowNodeDetailResource)createWorkflowNodeDetail(o.getNode());
        statusResource.setWorkflowNodeDetail(nodeDetailResource);
        DataTransferDetailResource transferDetailResource = (DataTransferDetailResource)createDataTransferResource(o.getTransferDetail());
        statusResource.setDataTransferDetail(transferDetailResource);
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
        TaskDetailResource taskDetailResource = (TaskDetailResource)createTaskDetail(o.getTask());
        schedulingResource.setTaskDetailResource(taskDetailResource);
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

        /**
         *
         * @param o  Workflow_Data model object
     * @return  WorkflowDataResource object
     */
//    private static Resource createWorkflowData(Workflow_Data o){
//        WorkflowDataResource workflowDataResource = new WorkflowDataResource();
//        workflowDataResource.setExperimentID(o.getExperiment_data().getExperiment_id());
//        workflowDataResource.setWorkflowInstanceID(o.getWorkflow_instanceID());
//        workflowDataResource.setTemplateName(o.getTemplate_name());
//        workflowDataResource.setStatus(o.getStatus());
//        workflowDataResource.setStartTime(o.getStart_time());
//        workflowDataResource.setLastUpdatedTime(o.getLast_update_time());
//        return workflowDataResource;
//    }

    /**
     *
     * @param o  Node_Data model object
     * @return Node Data resource
     */
//    private static Resource createNodeData (Node_Data o){
//        NodeDataResource nodeDataResource = new NodeDataResource();
//        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
//        nodeDataResource.setWorkflowDataResource(workflowDataResource);
//        nodeDataResource.setNodeID(o.getNode_id());
//        nodeDataResource.setNodeType(o.getNode_type());
//        if (o.getInputs()!=null) {
//			nodeDataResource.setInputs(new String(o.getInputs()));
//		}
//		if (o.getOutputs()!=null) {
//			nodeDataResource.setOutputs(new String(o.getOutputs()));
//		}
//		nodeDataResource.setStatus(o.getStatus());
//        nodeDataResource.setStartTime(o.getStart_time());
//        nodeDataResource.setLastUpdateTime(o.getLast_update_time());
//        nodeDataResource.setExecutionIndex(o.getExecution_index());
//        return nodeDataResource;
//    }

    /**
     *
     * @param o GramData model object
     * @return GramData Resource object
     */
//    private static Resource createGramData (Gram_Data o){
//        GramDataResource gramDataResource = new GramDataResource();
//        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
//        gramDataResource.setWorkflowDataResource(workflowDataResource);
//        gramDataResource.setNodeID(o.getNode_id());
//        gramDataResource.setRsl(new String(o.getRsl()));
//        gramDataResource.setInvokedHost(o.getInvoked_host());
//        gramDataResource.setLocalJobID(o.getLocal_Job_ID());
//        return gramDataResource;
//    }

//    private static Resource createExecutionError(Execution_Error o){
//        ExecutionErrorResource executionErrorResource = new ExecutionErrorResource();
//        ExperimentMetadataResource experimentDataResource = (ExperimentMetadataResource)createExperimentMetadata(o.getExperiment_MetaData());
//        executionErrorResource.setMetadataResource(experimentDataResource);
//        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
//        executionErrorResource.setWorkflowDataResource(workflowDataResource);
//        executionErrorResource.setNodeID(o.getNode_id());
//        executionErrorResource.setErrorID(o.getError_id());
//        executionErrorResource.setGfacJobID(o.getGfacJobID());
//        executionErrorResource.setSourceType(o.getSource_type());
//        executionErrorResource.setErrorTime(o.getError_date());
//        executionErrorResource.setErrorMsg(o.getError_msg());
//        executionErrorResource.setErrorDes(o.getError_des());
//        executionErrorResource.setErrorCode(o.getError_code());
//        executionErrorResource.setErrorReporter(o.getError_reporter());
//        executionErrorResource.setErrorLocation(o.getError_location());
//        executionErrorResource.setActionTaken(o.getAction_taken());
//        executionErrorResource.setErrorReference(o.getError_reference());
//        return executionErrorResource;
//    }

//    private static Resource createGfacJobData (GFac_Job_Data o){
//        GFacJobDataResource gFacJobDataResource = new GFacJobDataResource();
//        ExperimentMetadataResource experimentDataResource = (ExperimentMetadataResource)createExperimentMetadata(o.getExperiment_metadata());
//        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
//        gFacJobDataResource.setMetadataResource(experimentDataResource);
//        gFacJobDataResource.setWorkflowDataResource(workflowDataResource);
//        gFacJobDataResource.setNodeID(o.getNode_id());
//        gFacJobDataResource.setApplicationDescID(o.getApplication_descriptor_ID());
//        gFacJobDataResource.setServiceDescID(o.getService_descriptor_ID());
//        gFacJobDataResource.setHostDescID(o.getHost_descriptor_ID());
//        gFacJobDataResource.setJobData(o.getJob_data());
//        gFacJobDataResource.setLocalJobID(o.getLocal_Job_ID());
//        gFacJobDataResource.setSubmittedTime(o.getSubmitted_time());
//        gFacJobDataResource.setStatusUpdateTime(o.getStatus_update_time());
//        gFacJobDataResource.setStatus(o.getStatus());
//        gFacJobDataResource.setMetadata(o.getMetadata());
//        return gFacJobDataResource;
//    }

//    private static Resource createGfacJobStatus(GFac_Job_Status o) {
//        GFacJobStatusResource gFacJobStatusResource = new GFacJobStatusResource();
//        gFacJobStatusResource.setLocalJobID(o.getLocal_Job_ID());
//        gFacJobStatusResource.setStatus(o.getStatus());
//        gFacJobStatusResource.setStatusUpdateTime(o.getStatus_update_time());
//        GFacJobDataResource gFacJobDataResource = (GFacJobDataResource)createGfacJobData(o.getgFac_job_data());
//        gFacJobStatusResource.setgFacJobDataResource(gFacJobDataResource);
//        return gFacJobStatusResource;
//    }

//    private static Resource createOrchestratorData(Orchestrator data){
//    	OrchestratorDataResource dataResource = new OrchestratorDataResource();
//    	dataResource.setExperimentID(data.getExperiment_ID());
//    	dataResource.setUserName(data.getUserName());
//    	dataResource.setApplicationName(data.getApplicationName());
//    	dataResource.setStatus(data.getStatus());
//    	dataResource.setGfacEPR(data.getGfacEPR());
//    	dataResource.setJobRequest(data.getJobRequest());
//    	dataResource.setSubmittedTime(data.getSubmittedTime());
//    	dataResource.setStatusUpdateTime(data.getStatusUpdateTime());
//    	return dataResource;
//    }
//    public static byte[] getByteArray(String content){
//        byte[] contentBytes = content.getBytes();
//        return contentBytes;
//    }
}
