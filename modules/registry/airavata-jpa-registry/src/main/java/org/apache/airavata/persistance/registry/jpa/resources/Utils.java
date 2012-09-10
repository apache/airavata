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

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.model.*;


public class Utils {

    /**
     *
     * @param type model type
     * @param o model type instance
     * @return corresponding resource object
     */
    static Resource getResource(ResourceType type, Object o) {
        switch (type){
            case GATEWAY:
                if (o instanceof Gateway) {
                    return createGateway((Gateway) o);
                } else {
                    throw new IllegalArgumentException("Object should be a Gateway.");
                }
            case PROJECT:
                if (o instanceof Project){
                    return createProject((Project) o);
                } else {
                    throw new IllegalArgumentException("Object should be a Project.");
                }
            case CONFIGURATION:
                if(o instanceof Configuration){
                    return createConfiguration((Configuration) o);
                }else {
                    throw new IllegalArgumentException("Object should be a Configuration.");
                }
            case APPLICATION_DESCRIPTOR:
                if (o instanceof Application_Descriptor){
                    return createApplicationDescriptor((Application_Descriptor) o);
                } else {
                    throw new IllegalArgumentException("Object should be a Application Descriptor.");
                }
            case EXPERIMENT:
                if (o instanceof Experiment){
                    return createExperiment((Experiment) o);
                }  else {
                    throw new IllegalArgumentException("Object should be a Experiment.");
                }
            case USER:
                if(o instanceof Users) {
                    return createUser((Users) o);
                }else {
                    throw new IllegalArgumentException("Object should be a User.");
                }
            case HOST_DESCRIPTOR:
                if (o instanceof Host_Descriptor){
                    return createHostDescriptor((Host_Descriptor) o);
                }else {
                    throw new IllegalArgumentException("Object should be a Host Descriptor.");
                }
            case SERVICE_DESCRIPTOR:
                if (o instanceof Service_Descriptor){
                    return createServiceDescriptor((Service_Descriptor) o);
                }else {
                    throw new IllegalArgumentException("Object should be a Service Descriptor.");
                }
            case PUBLISHED_WORKFLOW:
                if (o instanceof Published_Workflow){
                    return createPublishWorkflow((Published_Workflow) o);
                }else {
                    throw new IllegalArgumentException("Object should be a Publish Workflow.");
                }
            case USER_WORKFLOW:
                if (o instanceof User_Workflow){
                    return createUserWorkflow((User_Workflow) o);
                }else {
                    throw new IllegalArgumentException("Object should be a User Workflow.");
                }
            case GATEWAY_WORKER:
                if (o instanceof Gateway_Worker){
                    return createGatewayWorker((Gateway_Worker)o);
                } else {
                    throw  new IllegalArgumentException("Object should be a Gateway Worker.");
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
        applicationDescriptorResource.setContent(o.getApplication_descriptor_xml());
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
        return experimentResource;
    }

    /**
     *
     * @param o Gateway_Worker model object
     * @return  Gateway_Worker resource object
     */
    private static Resource createGatewayWorker(Gateway_Worker o) {
        GatewayResource gatewayResource = (GatewayResource)createGateway(o.getGateway());
        return new WorkerResource(o.getUser().getUser_name(),gatewayResource);
    }

    /**
     *
     * @param o Host_Descriptor model object
     * @return  HostDescriptor resource object
     */
    private static Resource createHostDescriptor(Host_Descriptor o) {
        HostDescriptorResource hostDescriptorResource = new HostDescriptorResource();
        hostDescriptorResource.setGatewayName(o.getGateway().getGateway_name());
        hostDescriptorResource.setUserName(o.getUser().getUser_name());
        hostDescriptorResource.setHostDescName(o.getHost_descriptor_ID());
        hostDescriptorResource.setContent(o.getHost_descriptor_xml());
        return hostDescriptorResource;
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
        publishWorkflowResource.setContent(o.getWorkflow_content());
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
        serviceDescriptorResource.setContent(o.getService_descriptor_xml());
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
        userWorkflowResource.setContent(o.getWorkflow_graph());
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

}
