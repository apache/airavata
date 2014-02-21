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
	// table names
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
	public static final String EXPERIMENT_CONFIG_DATA = "Configuration_Data";
	public static final String EXPERIMENT_INPUT = "Experiment_Input";
	public static final String EXPERIMENT_OUTPUT = "Experiment_Output";
	public static final String WORKFLOW_NODE_DETAIL = "WorkflowNodeDetail";
	public static final String TASK_DETAIL = "TaskDetail";
	public static final String ERROR_DETAIL = "ErrorDetail";
	public static final String APPLICATION_INPUT = "ApplicationInput";
	public static final String APPLICATION_OUTPUT = "ApplicationOutput";
	public static final String NODE_INPUT = "NodeInput";
	public static final String NODE_OUTPUT = "NodeOutput";
	public static final String JOB_DETAIL = "JobDetail";
	public static final String DATA_TRANSFER_DETAIL = "DataTransferDetail";
	public static final String STATUS = "Status";
	public static final String CONFIG_DATA = "Config_Data";
	public static final String COMPUTATIONAL_RESOURCE_SCHEDULING = "Computation_Resource_Scheduling";
	public static final String ADVANCE_INPUT_DATA_HANDLING = "AdvanceInputDataHandling";
	public static final String ADVANCE_OUTPUT_DATA_HANDLING = "AdvanceOutputDataHandling";
	public static final String QOS_PARAMS = "QosParams";


	// Gateway Table
	public final class GatewayConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String GATEWAY_OWNER = "owner";
	}

	// Configuration Table
	public final class ConfigurationConstants {
		// public static final String CONFIG_ID = "config_ID";
		public static final String CONFIG_KEY = "config_key";
		public static final String CONFIG_VAL = "config_val";
		public static final String EXPIRE_DATE = "expire_date";
		public static final String CATEGORY_ID = "category_id";
		public static final String CATEGORY_ID_DEFAULT_VALUE = "SYSTEM";
	}

	// Users table
	public final class UserConstants {
		public static final String USERNAME = "user_name";
		public static final String PASSWORD = "password";
	}

	// Gateway_Worker table
	public final class GatewayWorkerConstants {
		public static final String USERNAME = "user_name";
		public static final String GATEWAY_NAME = "gateway_name";
	}

	// Project table
	public final class ProjectConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String USERNAME = "user_name";
		public static final String PROJECT_NAME = "project_name";
	}

	// Published_Workflow table
	public final class PublishedWorkflowConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String CREATED_USER = "created_user";
		public static final String PUBLISH_WORKFLOW_NAME = "publish_workflow_name";
		public static final String VERSION = "version";
		public static final String PUBLISHED_DATE = "published_date";
		public static final String PATH = "path";
		public static final String WORKFLOW_CONTENT = "workflow_content";
	}

	// User_Workflow table
	public final class UserWorkflowConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String OWNER = "owner";
		public static final String TEMPLATE_NAME = "template_name";
		public static final String LAST_UPDATED_DATE = "last_updated_date";
		public static final String PATH = "path";
		public static final String WORKFLOW_GRAPH = "workflow_graph";
	}

	// Host_Descriptor table
	public final class HostDescriptorConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String UPDATED_USER = "updated_user";
		public static final String HOST_DESC_ID = "host_descriptor_ID";
		public static final String HOST_DESC_XML = "host_descriptor_xml";
	}

	// Service_Descriptor table
	public final class ServiceDescriptorConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String UPDATED_USER = "updated_user";
		public static final String SERVICE_DESC_ID = "service_descriptor_ID";
		public static final String SERVICE_DESC_XML = "service_descriptor_xml";
	}

	// Application_Descriptor table
	public final class ApplicationDescriptorConstants {
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String UPDATED_USER = "updated_user";
		public static final String APPLICATION_DESC_ID = "application_descriptor_ID";
		public static final String HOST_DESC_ID = "host_descriptor_ID";
		public static final String SERVICE_DESC_ID = "service_descriptor_ID";
		public static final String APPLICATION_DESC_XML = "application_descriptor_xml";
	}

	// Experiment table
	public final class ExperimentConstants {
		public static final String PROJECT_NAME = "projectName";
		public static final String EXECUTION_USER = "executionUser";
		public static final String GATEWAY_NAME = "gatewayName";
		public static final String EXPERIMENT_ID = "expId";
		public static final String EXPERIMENT_NAME = "expName";
		public static final String DESCRIPTION = "expDesc";
		public static final String CREATION_TIME = "creationTime";
		public static final String APPLICATION_ID = "applicationId";
		public static final String APPLICATION_VERSION = "appVersion";
		public static final String WORKFLOW_TEMPLATE_ID = "workflowTemplateId";
		public static final String WORKFLOW_TEMPLATE_VERSION = "workflowTemplateVersion";
		public static final String WORKFLOW_EXECUTION_ID = "workflowExecutionId";
	}

    // Experiment Configuration Data table
    public final class ExperimentConfigurationDataConstants {
        public static final String EXPERIMENT_ID = "expId";
        public static final String AIRAVATA_AUTO_SCHEDULE = "airavata_auto_schedule";
        public static final String OVERRIDE_MANUAL_SCHEDULE = "override_manual_schedule";
        public static final String SHARE_EXPERIMENT = "share_experiment";
    }

    //Experiment Input table
    public final class ExperimentInputConstants {
        public static final String EXPERIMENT_ID = "expId";
        public static final String EXPERIMENT_INPUT_KEY = "input_key";
        public static final String EXPERIMENT_INPUT_VAL = "value";
        public static final String INPUT_TYPE = "input_type";
        public static final String METADATA = "metadata";
    }

    //Experiment Output table
    public final class ExperimentOutputConstants {
        public static final String EXPERIMENT_ID = "expId";
        public static final String EXPERIMENT_OUTPUT_KEY = "output_key";
        public static final String EXPERIMENT_OUTPUT_VAL = "value";
        public static final String OUTPUT_TYPE = "output_type";
        public static final String METADATA = "metadata";
    }

	// Workflow_Data table
	public final class WorkflowNodeDetailsConstants {
		public static final String EXPERIMENT_ID = "experiment_ID";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creation_time";
		public static final String NODE_NAME = "node_name";
	}

	// TaskDetail table
	public final class TaskDetailConstants {
		public static final String TASK_ID = "taskId";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creation_type";
		public static final String APPLICATION_ID = "application_id";
		public static final String APPLICATION_VERSION = "application_version";
	}

	// ErrorDetails table
	public final class ErrorDetailConstants {
		public static final String ERROR_ID = "error_id";
		public static final String EXPERIMENT_ID = "expId";
		public static final String TASK_ID = "taskId";
		public static final String JOB_ID = "job_id";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creation_time";
		public static final String ACTUAL_ERROR_MESSAGE = "actual_error_message";
		public static final String USER_FRIEDNLY_ERROR_MSG = "user_friendly_error_msg";
		public static final String TRANSIENT_OR_PERSISTENT = "transient_or_persistent";
		public static final String ERROR_CATEGORY = "error_category";
		public static final String CORRECTIVE_ACTION = "corrective_action";
		public static final String ACTIONABLE_GROUP = "actionable_group";
	}

    // ApplicationInput table
	public final class ApplicationInputConstants {
		public static final String TASK_ID = "taskId";
		public static final String INPUT_KEY = "input_key";
		public static final String INPUT_KEY_TYPE = "input_key_type";
		public static final String METADATA = "metadata";
		public static final String VALUE = "value";
	}

    // ApplicationOutput table
    public final class ApplicationOutputConstants {
        public static final String TASK_ID = "taskId";
        public static final String OUTPUT_KEY = "output_key";
        public static final String OUTPUT_KEY_TYPE = "output_key_type";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // NodeInput table
    public final class NodeInputConstants {
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String INPUT_KEY = "input_key";
        public static final String INPUT_KEY_TYPE = "input_key_type";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // NodeOutput table
    public final class NodeOutputConstants {
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String OUTPUT_KEY = "output_key";
        public static final String OUTPUT_KEY_TYPE = "output_key_type";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // Job Details table constants
    public final class JobDetailConstants{
        public static final String JOB_ID = "job_id";
        public static final String TASK_ID = "taskId";
        public static final String JOB_DESCRIPTION = "job_description";
        public static final String CREATION_TIME = "creation_time";
    }

    // Data transfer Details table constants
    public final class DataTransferDetailConstants{
        public static final String TRANSFER_ID = "transfer_id";
        public static final String TASK_ID = "taskId";
        public static final String TRANSFER_DESC = "transfer_description";
        public static final String CREATION_TIME = "creation_time";
    }

    // Status table constants
    public final class StatusConstants {
        public static final String STATUS_ID = "status_id";
        public static final String EXPERIMENT_ID = "expId";
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String TRANSFER_ID = "transfer_id";
        public static final String TASK_ID = "taskId";
        public static final String JOB_ID = "job_id";
        public static final String STATE = "state";
        public static final String STATUS_UPDATE_TIME = "status_update_time";
        public static final String STATUS_TYPE = "status_type";
    }

    public static final class ComputationalResourceSchedulingConstants{
        public static final String RESOURCE_SCHEDULING_ID = "resource_scheduling_id";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String RESOURCE_HOST_ID = "resource_host_id";
        public static final String CPU_COUNT = "cpu_count";
        public static final String NODE_COUNT = "node_count";
        public static final String NO_OF_THREADS = "no_of_threads";
        public static final String QUEUE_NAME = "queue_name";
        public static final String WALLTIME_LIMIT = "walltime_limit";
        public static final String JOB_START_TIME = "job_start_time";
        public static final String TOTAL_PHYSICAL_MEMORY = "total_physical_memory";
        public static final String COMPUTATIONAL_PROJECT_ACCOUNT = "computational_project_accont";
    }

    public static final class AdvancedInputDataHandlingConstants {
        public static final String INPUT_DATA_HANDLING_ID = "input_data_handling_id";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String WORKING_DIR_PARENT = "working_dir_parent";
        public static final String UNIQUE_WORKING_DIR = "unique_working_dir";
        public static final String STAGE_INPUT_FILES_TO_WORKING_DIR = "stage_input_files_to_working_dir";
        public static final String CLEAN_AFTER_JOB = "clean_after_job";
    }

    public static final class AdvancedOutputDataHandlingConstants {
        public static final String OUTPUT_DATA_HANDLING_ID = "output_data_handling_id";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String OUTPUT_DATA_DIR = "output_data_dir";
        public static final String DATA_REG_URL = "data_reg_url";
        public static final String PERSIST_OUTPUT_DATA = "persist_output_data";
    }

    public static final class QosParamsConstants {
        public static final String QOS_ID = "qos_id";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String START_EXECUTION_AT = "start_execution_at";
        public static final String EXECUTE_BEFORE = "execute_before";
        public static final String NO_OF_RETRIES = "no_of_retries";
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
	public static <T> List<T> getResourceList(List<Resource> resources,
			Class<?> T) {
		List<T> list = new ArrayList<T>();
		for (Resource o : resources) {
			list.add((T) o);
		}
		return list;
	}

}
