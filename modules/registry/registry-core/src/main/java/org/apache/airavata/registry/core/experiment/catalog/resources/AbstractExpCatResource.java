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
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.cpi.RegistryException;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExpCatResource implements ExperimentCatResource {
	// table names
	public static final String GATEWAY = "Gateway";
	public static final String CONFIGURATION = "Configuration";
	public static final String USERS = "Users";
	public static final String GATEWAY_WORKER = "Gateway_Worker";
	public static final String PROJECT = "Project";
	public static final String PROJECT_USER = "ProjectUser";
	public static final String EXPERIMENT = "Experiment";
	public static final String NOTIFICATION_EMAIL = "Notification_Email";
	public static final String EXPERIMENT_CONFIG_DATA = "ExperimentConfigData";
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
	public static final String CONFIG_DATA = "ExperimentConfigData";
	public static final String COMPUTATIONAL_RESOURCE_SCHEDULING = "Computational_Resource_Scheduling";
	public static final String ADVANCE_INPUT_DATA_HANDLING = "AdvancedInputDataHandling";
	public static final String ADVANCE_OUTPUT_DATA_HANDLING = "AdvancedOutputDataHandling";
	public static final String QOS_PARAMS = "QosParam";


	// Gateway Table
	public final class GatewayConstants {
		public static final String GATEWAY_ID = "gateway_id";
		public static final String GATEWAY_NAME = "gateway_name";
		public static final String DOMAIN = "domain";
		public static final String EMAIL_ADDRESS = "emailAddress";
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
		public static final String GATEWAY_ID = "gateway_id";
	}

	// Project table
	public final class ProjectConstants {
		public static final String GATEWAY_ID = "gateway_id";
		public static final String USERNAME = "user_name";
		public static final String PROJECT_NAME = "project_name";
		public static final String PROJECT_ID = "project_id";
		public static final String DESCRIPTION = "description";
        public static final String CREATION_TIME = "creationTime";
	}

    // Project table
    public final class ProjectUserConstants {
        public static final String USERNAME = "userName";
        public static final String PROJECT_ID = "projectID";
    }

	// Experiment table
	public final class ExperimentConstants {
		public static final String PROJECT_ID = "projectID";
		public static final String EXECUTION_USER = "executionUser";
		public static final String GATEWAY_ID = "gatewayId";
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
        public static final String AIRAVATA_AUTO_SCHEDULE = "airavataAutoSchedule";
        public static final String OVERRIDE_MANUAL_SCHEDULE = "overrideManualParams";
        public static final String SHARE_EXPERIMENT = "shareExp";
    }

    public final class NotificationEmailConstants {
        public static final String EXPERIMENT_ID = "experiment_id";
        public static final String TASK_ID = "taskId";
        public static final String EMAIL_ADDRESS = "emailAddress";
    }

    //Experiment Input table
    public final class ExperimentInputConstants {
        public static final String EXPERIMENT_ID = "experiment_id";
        public static final String EXPERIMENT_INPUT_KEY = "ex_key";
        public static final String EXPERIMENT_INPUT_VAL = "value";
        public static final String INPUT_TYPE = "inputType";
        public static final String METADATA = "metadata";
    }

    //Experiment Output table
    public final class ExperimentOutputConstants {
        public static final String EXPERIMENT_ID = "experiment_id";
        public static final String EXPERIMENT_OUTPUT_KEY = "ex_key";
        public static final String EXPERIMENT_OUTPUT_VAL = "value";
        public static final String OUTPUT_TYPE = "outputKeyType";
        public static final String METADATA = "metadata";
    }

	// Workflow_Data table
	public final class WorkflowNodeDetailsConstants {
		public static final String EXPERIMENT_ID = "expId";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creationTime";
		public static final String NODE_NAME = "nodeName";
	}

	// TaskDetail table
	public final class TaskDetailConstants {
		public static final String TASK_ID = "taskId";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creationTime";
		public static final String APPLICATION_ID = "appId";
		public static final String APPLICATION_VERSION = "appVersion";
	}

	// ErrorDetails table
	public final class ErrorDetailConstants {
		public static final String ERROR_ID = "errorID";
		public static final String EXPERIMENT_ID = "expId";
		public static final String TASK_ID = "taskId";
		public static final String JOB_ID = "jobId";
		public static final String NODE_INSTANCE_ID = "nodeId";
		public static final String CREATION_TIME = "creationTime";
		public static final String ACTUAL_ERROR_MESSAGE = "actualErrorMsg";
		public static final String USER_FRIEDNLY_ERROR_MSG = "userFriendlyErrorMsg";
		public static final String TRANSIENT_OR_PERSISTENT = "transientPersistent";
		public static final String ERROR_CATEGORY = "errorCategory";
		public static final String CORRECTIVE_ACTION = "correctiveAction";
		public static final String ACTIONABLE_GROUP = "actionableGroup";
	}

    // ApplicationInput table
	public final class ApplicationInputConstants {
		public static final String TASK_ID = "taskId";
		public static final String INPUT_KEY = "inputKey";
		public static final String INPUT_KEY_TYPE = "inputKeyType";
		public static final String METADATA = "metadata";
		public static final String VALUE = "value";
	}

    // ApplicationOutput table
    public final class ApplicationOutputConstants {
        public static final String TASK_ID = "taskId";
        public static final String OUTPUT_KEY = "outputKey";
        public static final String OUTPUT_KEY_TYPE = "outputKeyType";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // NodeInput table
    public final class NodeInputConstants {
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String INPUT_KEY = "inputKey";
        public static final String INPUT_KEY_TYPE = "inputKeyType";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // NodeOutput table
    public final class NodeOutputConstants {
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String OUTPUT_KEY = "outputKey";
        public static final String OUTPUT_KEY_TYPE = "outputKeyType";
        public static final String METADATA = "metadata";
        public static final String VALUE = "value";
    }

    // Job Details table constants
    public final class JobDetailConstants{
        public static final String JOB_ID = "jobId";
        public static final String TASK_ID = "taskId";
        public static final String JOB_DESCRIPTION = "jobDescription";
        public static final String CREATION_TIME = "jobDescription";
    }

    // Data transfer Details table constants
    public final class DataTransferDetailConstants{
        public static final String TRANSFER_ID = "transferId";
        public static final String TASK_ID = "taskId";
        public static final String TRANSFER_DESC = "transferDesc";
        public static final String CREATION_TIME = "creationTime";
    }

    // Status table constants
    public final class StatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String EXPERIMENT_ID = "expId";
        public static final String NODE_INSTANCE_ID = "nodeId";
        public static final String TRANSFER_ID = "transferId";
        public static final String TASK_ID = "taskId";
        public static final String JOB_ID = "jobId";
        public static final String STATE = "state";
        public static final String STATUS_UPDATE_TIME = "statusUpdateTime";
        public static final String STATUS_TYPE = "statusType";
    }

    public static final class ComputationalResourceSchedulingConstants{
        public static final String RESOURCE_SCHEDULING_ID = "schedulingId";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String RESOURCE_HOST_ID = "resourceHostId";
        public static final String CPU_COUNT = "cpuCount";
        public static final String NODE_COUNT = "nodeCount";
        public static final String NO_OF_THREADS = "numberOfThreads";
        public static final String QUEUE_NAME = "queueName";
        public static final String WALLTIME_LIMIT = "wallTimeLimit";
        public static final String JOB_START_TIME = "jobStartTime";
        public static final String TOTAL_PHYSICAL_MEMORY = "totalPhysicalmemory";
        public static final String COMPUTATIONAL_PROJECT_ACCOUNT = "projectName";
    }

    public static final class AdvancedInputDataHandlingConstants {
        public static final String INPUT_DATA_HANDLING_ID = "dataHandlingId";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String WORKING_DIR_PARENT = "parentWorkingDir";
        public static final String UNIQUE_WORKING_DIR = "workingDir";
        public static final String STAGE_INPUT_FILES_TO_WORKING_DIR = "stageInputsToWorkingDir";
        public static final String CLEAN_AFTER_JOB = "cleanAfterJob";
    }

    public static final class AdvancedOutputDataHandlingConstants {
        public static final String OUTPUT_DATA_HANDLING_ID = "outputDataHandlingId";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String OUTPUT_DATA_DIR = "outputDataDir";
        public static final String DATA_REG_URL = "dataRegUrl";
        public static final String PERSIST_OUTPUT_DATA = "persistOutputData";
    }

    public static final class QosParamsConstants {
        public static final String QOS_ID = "qosId";
        public static final String EXPERIMENT_ID = "expId";
        public static final String TASK_ID = "taskId";
        public static final String START_EXECUTION_AT = "startExecutionAt";
        public static final String EXECUTE_BEFORE = "executeBefore";
        public static final String NO_OF_RETRIES = "noOfRetries";
    }


	protected AbstractExpCatResource() {
	}

	public boolean isExists(ResourceType type, Object name) throws RegistryException {
		try {
			return get(type, name) != null;
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getResourceList(List<ExperimentCatResource> resources,
			Class<?> T) {
		List<T> list = new ArrayList<T>();
		for (ExperimentCatResource o : resources) {
			list.add((T) o);
		}
		return list;
	}

}
