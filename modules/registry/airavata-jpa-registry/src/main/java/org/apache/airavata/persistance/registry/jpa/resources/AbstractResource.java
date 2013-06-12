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

import java.util.ArrayList;
import java.util.List;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;

public abstract class AbstractResource implements Resource {
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
    public static final String EXPERIMENT_DATA = "Experiment_Data";
    public static final String WORKFLOW_DATA = "Workflow_Data";
    public static final String EXPERIMENT_METADATA = "Experiment_Metadata";
    public static final String EXECUTION_ERROR = "Execution_Error";
    public static final String GFAC_JOB_DATA = "GFac_Job_Data";
    public static final String GFAC_JOB_STATUS = "GFac_Job_Status";

    //Gateway Table
    public final class GatewayConstants {
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String GATEWAY_OWNER = "owner";
    }

    //Configuration Table
    public final class ConfigurationConstants {
//        public static final String CONFIG_ID = "config_ID";
        public static final String CONFIG_KEY = "config_key";
        public static final String CONFIG_VAL = "config_val";
        public static final String EXPIRE_DATE = "expire_date";
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY_ID_DEFAULT_VALUE = "SYSTEM";
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
        public static final String PROJECT_NAME = "project_name";
        public static final String USERNAME = "user_name";
        public static final String GATEWAY_NAME = "gateway_name";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String SUBMITTED_DATE = "submitted_date";

    }

    //Experiment_Data table
    public final class ExperimentDataConstants{
        public static final String EXPERIMENT_ID="experiment_ID";
        public static final String NAME = "name";
        public static final String USERNAME = "username";
        public static final String METADATA = "metadata";
    }

    //Workflow_Data table
    public final class WorkflowDataConstants{
        public static final String EXPERIMENT_ID="experiment_ID";
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String TEMPLATE_NAME = "template_name";
        public static final String STATUS = "status";
        public static final String START_TIME = "start_time";
        public static final String LAST_UPDATE_TIME = "last_update_time";
    }

    //Node_Data table
    public final class NodeDataConstants{
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String NODE_TYPE = "node_type";
        public static final String INPUTS = "inputs";
        public static final String OUTPUTS = "outputs";
        public static final String STATUS = "status";
        public static final String START_TIME = "start_time";
        public static final String LAST_UPDATE_TIME = "last_update_time";
    }

    //Gram_Data table
    public final class GramDataConstants{
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String RSL = "rsl";
        public static final String INVOKED_HOST = "invoked_host";
        public static final String LOCAL_JOB_ID = "local_Job_ID";
    }

    public final class ExecutionErrorConstants {
        public static final String ERROR_ID = "error_id";
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String WORKFLOW_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String GFAC_JOB_ID = "gfacJobID";
        public static final String SOURCE_TYPE = "source_type";
        public static final String ERROR_DATE = "error_date";
        public static final String ERROR_MSG = "error_msg";
        public static final String ERROR_DES = "error_des";
        public static final String ERROR_CODE = "error_code";
    }

    public final class GFacJobDataConstants {
        public static final String EXPERIMENT_ID = "experiment_ID";
        public static final String WORKFLOW_INSTANCE_ID = "workflow_instanceID";
        public static final String NODE_ID = "node_id";
        public static final String APP_DESC_ID = "application_descriptor_ID";
        public static final String HOST_DESC_ID = "host_descriptor_ID";
        public static final String SERVICE_DESC_ID = "service_descriptor_ID";
        public static final String JOB_DATA = "job_data";
        public static final String LOCAL_JOB_ID = "local_Job_ID";
        public static final String SUBMITTED_TIME = "submitted_time";
        public static final String STATUS_UPDATE_TIME = "status_update_time";
        public static final String STATUS = "status";
        public static final String METADATA = "metadata";
    }

    public final class GFacJobStatusConstants {
        public static final String LOCAL_JOB_ID = "local_Job_ID";
        public static final String STATUS = "status";
        public static final String STATUS_UPDATE_TIME = "status_update_time";
    }

    protected AbstractResource() {
    }

    public boolean isExists(ResourceType type, Object name) {
        try {
            return get(type, name) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
	public static <T> List<T> getResourceList(List<Resource> resources, Class<?> T){
    	List<T> list=new ArrayList<T>();
    	for (Resource o : resources) {
    		list.add((T) o);
		}
    	return list;
    }

}
