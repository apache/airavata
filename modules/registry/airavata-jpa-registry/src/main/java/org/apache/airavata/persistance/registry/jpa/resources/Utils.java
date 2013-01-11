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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.apache.airavata.persistance.registry.jpa.JPAConstants;
import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.Application_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Configuration;
import org.apache.airavata.persistance.registry.jpa.model.Experiment;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Data;
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Metadata;
import org.apache.airavata.persistance.registry.jpa.model.Gateway;
import org.apache.airavata.persistance.registry.jpa.model.Gateway_Worker;
import org.apache.airavata.persistance.registry.jpa.model.Gram_Data;
import org.apache.airavata.persistance.registry.jpa.model.Host_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.Node_Data;
import org.apache.airavata.persistance.registry.jpa.model.Project;
import org.apache.airavata.persistance.registry.jpa.model.Published_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Service_Descriptor;
import org.apache.airavata.persistance.registry.jpa.model.User_Workflow;
import org.apache.airavata.persistance.registry.jpa.model.Users;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.apache.airavata.registry.api.AiravataRegistryConnectionDataProvider;
import org.apache.airavata.registry.api.AiravataRegistryFactory;
import org.apache.airavata.registry.api.exception.RegistrySettingsException;
import org.apache.airavata.registry.api.exception.UnknownRegistryConnectionDataException;
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
            case EXPERIMENT:
                if (o instanceof Experiment){
                    return createExperiment((Experiment) o);
                }  else {
                    logger.error("Object should be a Experiment.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment.");
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
            case EXPERIMENT_DATA:
                if (o instanceof  Experiment_Data){
                    return createExperimentData((Experiment_Data)o);
                }else {
                    logger.error("Object should be a Experiment Data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment Data.");
                }
            case EXPERIMENT_METADATA:
                if (o instanceof  Experiment_Metadata){
                    return createExperimentMetadata((Experiment_Metadata)o);
                }else {
                    logger.error("Object should be a Experiment Metadata.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Experiment Metadata.");
                }
            case WORKFLOW_DATA:
                if (o instanceof  Workflow_Data){
                    return createWorkflowData((Workflow_Data) o);
                }else {
                    logger.error("Object should be a Workflow Data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Workflow Data.");
                }
            case NODE_DATA:
                if (o instanceof  Node_Data){
                    return createNodeData((Node_Data) o);
                }else {
                    logger.error("Object should be a Node Data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Node Data.");
                }
            case GRAM_DATA:
                if (o instanceof  Gram_Data){
                    return createGramData((Gram_Data) o);
                }else {
                    logger.error("Object should be a Gram Data.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Object should be a Gram Data.");
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
        experimentResource.setExpID(o.getExperiment_ID());
        experimentResource.setSubmittedDate(o.getSubmitted_date());
        return experimentResource;
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
     *
     * @param o Experiment Data model object
     * @return Experiment Data resource object
     */
    private static Resource createExperimentData(Experiment_Data o){
        ExperimentDataResource experimentDataResource = new ExperimentDataResource();
        experimentDataResource.setExperimentID(o.getExperiment_ID());
        experimentDataResource.setExpName(o.getName());
        experimentDataResource.setUserName(o.getUsername());
        return experimentDataResource;
    }

    /**
     *
     * @param o Experiment MetaData model object
     * @return Experiment MetaData resource object
     */
    private static Resource createExperimentMetadata(Experiment_Metadata o) {
        ExperimentMetadataResource experimentMetadataResource = new ExperimentMetadataResource();
        experimentMetadataResource.setExpID(o.getExperiment_ID());
        experimentMetadataResource.setMetadata(new String(o.getMetadata()));
        return experimentMetadataResource;
    }

    /**
     *
     * @param o  Workflow_Data model object
     * @return  WorkflowDataResource object
     */
    private static Resource createWorkflowData(Workflow_Data o){
        WorkflowDataResource workflowDataResource = new WorkflowDataResource();
        workflowDataResource.setExperimentID(o.getExperiment_data().getExperiment_ID());
        workflowDataResource.setWorkflowInstanceID(o.getWorkflow_instanceID());
        workflowDataResource.setTemplateName(o.getTemplate_name());
        workflowDataResource.setStatus(o.getStatus());
        workflowDataResource.setStartTime(o.getStart_time());
        workflowDataResource.setLastUpdatedTime(o.getLast_update_time());
        return workflowDataResource;
    }

    /**
     *
     * @param o  Node_Data model object
     * @return Node Data resource
     */
    private static Resource createNodeData (Node_Data o){
        NodeDataResource nodeDataResource = new NodeDataResource();
        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
        nodeDataResource.setWorkflowDataResource(workflowDataResource);
        nodeDataResource.setNodeID(o.getNode_id());
        nodeDataResource.setNodeType(o.getNode_type());
        if (o.getInputs()!=null) {
			nodeDataResource.setInputs(new String(o.getInputs()));
		}
		if (o.getOutputs()!=null) {
			nodeDataResource.setOutputs(new String(o.getOutputs()));
		}
		nodeDataResource.setStatus(o.getStatus());
        nodeDataResource.setStartTime(o.getStart_time());
        nodeDataResource.setLastUpdateTime(o.getLast_update_time());
        nodeDataResource.setExecutionIndex(o.getExecution_index());
        return nodeDataResource;
    }

    /**
     *
     * @param o GramData model object
     * @return GramData Resource object
     */
    private static Resource createGramData (Gram_Data o){
        GramDataResource gramDataResource = new GramDataResource();
        WorkflowDataResource workflowDataResource = (WorkflowDataResource)createWorkflowData(o.getWorkflow_Data());
        gramDataResource.setWorkflowDataResource(workflowDataResource);
        gramDataResource.setNodeID(o.getNode_id());
        gramDataResource.setRsl(new String(o.getRsl()));
        gramDataResource.setInvokedHost(o.getInvoked_host());
        gramDataResource.setLocalJobID(o.getLocal_Job_ID());
        return gramDataResource;
    }

//    public static byte[] getByteArray(String content){
//        byte[] contentBytes = content.getBytes();
//        return contentBytes;
//    }
}
