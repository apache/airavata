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
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;

import javax.persistence.EntityManager;

public abstract class AbstractResource implements Resource {
    protected EntityManager em;

    //table names
    public static final String GATEWAY = "Gateway";
    public static final String CONFIGURATION = "Configuration";
    public static final String USERS = "Users";
    public static final String GATEWAY_WORKER = "Gateway_Worker";
    public static final String PROJECT = "Project";
    public static final String PUBLISHED_WORKFLOW = "Published_Workflow";
    public static final String USER_WORKFLOW = "User_Workflow";
    public static final String HOST_DESCRIPTOR = "Host_Descriptor";
    public static final String SERVICE_DESCRIPTOR = "Service_Descriptor";
    public static final String APPLICATION_DESCRIPTOR = "Application_Descriptor";
    public static final String EXPERIMENT = "Experiment";

    //Gateway Table
    public final class GatewayConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String GATEWAY_OWNER = "owner";
    }

    //Configuration Table
    public final class ConfigurationConstants {
        public static final String CONFIG_ID = "config_ID";
        public static final String CONFIG_KEY = "config_key";
        public static final String CONFIG_VAL = "config_val";
        public static final String EXPIRE_DATE = "expire_date";
    }


    //Users table
    public final class UserConstants {
        public static final String USERNAME = "user_name";
        public static final String PASSWORD = "password";
    }

    //Gateway_Worker table
    public final class GatewayWorkerConstants {
        public static final String USERNAME = "user_name";
        public static final String GATEWAY_NAME = "gateway_name";
    }

    //Project table
    public final class ProjectConstants {
        public static final String PROJECT_ID = "project_ID";
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String USERNAME = "user_name";
        public static final String PROJECT_NAME = "project_name";
    }

    //Published_Workflow table
    public final class PublishedWorkflowConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String CREATED_USER = "created_user";
        public static final String PUBLISH_WORKFLOW_NAME = "publish_workflow_name";
        public static final String VERSION = "version";
        public static final String PUBLISHED_DATE = "published_date";
        public static final String PATH = "path";
        public static final String WORKFLOW_CONTENT = "workflow_content";
    }

    //User_Workflow table
    public final class UserWorkflowConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String OWNER = "owner";
        public static final String TEMPLATE_NAME = "template_name";
        public static final String LAST_UPDATED_DATE = "last_updated_date";
        public static final String PATH = "path";
        public static final String WORKFLOW_GRAPH = "workflow_graph";
    }

    //Host_Descriptor table
    public final class HostDescriptorConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String UPDATED_USER = "updated_user";
        public static final String HOST_DESC_ID = "host_descriptor_ID";
        public static final String HOST_DESC_XML = "host_descriptor_xml";
    }

    //Service_Descriptor table
    public final class ServiceDescriptorConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String UPDATED_USER = "updated_user";
        public static final String SERVICE_DESC_ID = "service_descriptor_ID";
        public static final String SERVICE_DESC_XML = "service_descriptor_xml";
    }

    //Application_Descriptor table
    public final class ApplicationDescriptorConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String UPDATED_USER = "updated_user";
        public static final String APPLICATION_DESC_ID = "application_descriptor_ID";
        public static final String HOST_DESC_ID = "host_descriptor_ID";
        public static final String SERVICE_DESC_ID = "service_descriptor_ID";
        public static final String APPLICATION_DESC_XML = "application_descriptor_xml";
    }

    //Experiment table
    public final class ExperimentConstants {
        public static final String PROJECT_ID = "project_ID";
        public static final String USERNAME = "user_name";
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String SUBMITTED_DATE = "submitted_date";

    }

    protected AbstractResource() {
        em = ResourceUtils.getEntityManager();
    }

    protected void begin() {
        if(em == null){
            em = ResourceUtils.getEntityManager();
        }
        em.getTransaction().begin();
    }

    protected void end() {
        em.getTransaction().commit();
//        em.close();

    }

    public boolean isExists(ResourceType type, Object name) {
        try {
            return get(type, name) != null;
        } catch (Exception e) {
            return false;
        }
    }

}
