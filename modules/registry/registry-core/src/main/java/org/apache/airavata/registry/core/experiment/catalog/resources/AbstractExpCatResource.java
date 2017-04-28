/**
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
	public static final String USERS = "Users";
	public static final String GATEWAY_WORKER = "GatewayWorker";
	public static final String PROJECT = "Project";
    public static final String NOTIFICATION = "Notification";
	public static final String PROJECT_USER = "ProjectUser";
	public static final String EXPERIMENT = "Experiment";
	public static final String EXPERIMENT_INPUT = "ExperimentInput";
	public static final String EXPERIMENT_OUTPUT = "ExperimentOutput";
    public static final String EXPERIMENT_ERROR = "ExperimentError";
    public static final String EXPERIMENT_STATUS = "ExperimentStatus";
    public static final String USER_CONFIGURATION_DATA = "UserConfigurationData";
    public static final String PROCESS = "Process";
    public static final String PROCESS_ERROR = "ProcessError";
    public static final String PROCESS_RESOURCE_SCHEDULE = "ProcessResourceSchedule";
    public static final String PROCESS_INPUT = "ProcessInput";
    public static final String PROCESS_OUTPUT = "ProcessOutput";
    public static final String PROCESS_STATUS = "ProcessStatus";
    public static final String TASK = "Task";
    public static final String TASK_ERROR = "TaskError";
    public static final String TASK_STATUS = "TaskStatus";
    public static final String JOB = "Job";
    public static final String JOB_STATUS = "JobStatus";


	// Gateway Table
	public final class GatewayConstants {
		public static final String GATEWAY_ID = "gatewayId";
		public static final String GATEWAY_NAME = "gatewayName";
		public static final String DOMAIN = "domain";
		public static final String EMAIL_ADDRESS = "emailAddress";
	}

	// Users table
	public final class UserConstants {
		public static final String USERNAME = "userName";
		public static final String PASSWORD = "password";
		public static final String GATEWAY_ID = "gatewayId";
	}

    // Notifications table
    public final class NotificationConstants {
        public static final String NOTIFICATION_ID = "notificationId";
        public static final String GATEWAY_ID = "gatewayId";
    }

	// Gateway_Worker table
	public final class GatewayWorkerConstants {
		public static final String USERNAME = "userName";
		public static final String GATEWAY_ID = "gatewayId";
	}

	// Project table
	public final class ProjectConstants {
		public static final String GATEWAY_ID = "gatewayId";
		public static final String USERNAME = "userName";
		public static final String PROJECT_NAME = "projectName";
		public static final String PROJECT_ID = "projectId";
		public static final String DESCRIPTION = "description";
        public static final String CREATION_TIME = "creationTime";
	}

    // Project table
    public final class ProjectUserConstants {
        public static final String USERNAME = "userName";
        public static final String PROJECT_ID = "projectId";
    }

	// Experiment table
	public final class ExperimentConstants {
		public static final String PROJECT_ID = "projectId";
		public static final String GATEWAY_ID = "gatewayId";
		public static final String EXPERIMENT_ID = "experimentId";
        public static final String EXECUTION_ID = "executionId";
        public static final String EXPERIMENT_NAME = "experimentName";
        public static final String DESCRIPTION = "description";
        public static final String USER_NAME = "userName";
        public static final String CREATION_TIME = "creationTime";
    }


	// Task table
    public final class TaskConstants {
        public static final String TASK_ID = "taskId";
        public static final String PROCESS_ID = "parentProcessId";
    }

    // Task Error table
    public final class TaskErrorConstants {
        public static final String ERROR_ID = "errorId";
        public static final String TASK_ID = "taskId";
        public static final String CREATION_TIME = "creationTime";
        public static final String ACTUAL_ERROR_MESSAGE = "actualErrorMsg";
        public static final String USER_FRIEDNLY_ERROR_MSG = "userFriendlyErrorMsg";
        public static final String TRANSIENT_OR_PERSISTENT = "transientPersistent";
        public static final String ROOT_CAUSE_ERROR_ID_LIST = "rootCauseErrorIdList";
    }

    //Process Error table
    public final class ProcessErrorConstants {
        public static final String ERROR_ID = "errorId";
        public static final String PROCESS_ID = "processId";
        public static final String CREATION_TIME = "creationTime";
        public static final String ACTUAL_ERROR_MESSAGE = "actualErrorMsg";
        public static final String USER_FRIEDNLY_ERROR_MSG = "userFriendlyErrorMsg";
        public static final String TRANSIENT_OR_PERSISTENT = "transientPersistent";
        public static final String ROOT_CAUSE_ERROR_ID_LIST = "rootCauseErrorIdList";
    }

    //Experiment Error table
    public final class ExperimentErrorConstants {
        public static final String ERROR_ID = "errorId";
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String CREATION_TIME = "creationTime";
        public static final String ACTUAL_ERROR_MESSAGE = "actualErrorMsg";
        public static final String USER_FRIEDNLY_ERROR_MSG = "userFriendlyErrorMsg";
        public static final String TRANSIENT_OR_PERSISTENT = "transientPersistent";
        public static final String ROOT_CAUSE_ERROR_ID_LIST = "rootCauseErrorIdList";
    }

    //Process Input Table
    public final class ProcessInputConstants {
        public static final String PROCESS_ID = "processId";
        public static final String INPUT_NAME = "inputName";
        public static final String INPUT_VALUE = "inputValue";
        public static final String DATA_TYPE = "dataType";
        public static final String APPLICATION_ARGUMENT = "applicationArgument";
        public static final String STANDARD_INPUT = "standardInput";
        public static final String USER_FRIENDLY_DESCRIPTION = "userFriendlyDescription";
        public static final String METADATA = "metadata";
        public static final String INPUT_ORDER = "inputOrder";
        public static final String IS_REQUIRED = "isRequired";
        public static final String REQUIRED_TO_ADDED_TO_CMD = "requiredToAddedToCmd";
        public static final String DATA_STAGED = "dataStaged";
    }

    //Process Output Table
    public final class ProcessOutputConstants {
        public static final String PROCESS_ID = "processId";
        public static final String OUTPUT_NAME = "outputName";
        public static final String OUTPUT_VALUE = "outputValue";
        public static final String DATA_TYPE = "dataType";
        public static final String APPLICATION_ARGUMENT = "applicationArgument";
        public static final String IS_REQUIRED = "isRequired";
        public static final String REQUIRED_TO_ADDED_TO_CMD = "requiredToAddedToCmd";
        public static final String DATA_MOVEMENT = "dataMovement";
        public static final String LOCATION = "location";
        public static final String SEARCH_QUERY = "searchQuery";
    }

    //Experiment Input Table
    public final class ExperimentInputConstants {
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String INPUT_NAME = "inputName";
        public static final String INPUT_VALUE = "inputValue";
        public static final String DATA_TYPE = "dataType";
        public static final String APPLICATION_ARGUMENT = "applicationArgument";
        public static final String STANDARD_INPUT = "standardInput";
        public static final String USER_FRIENDLY_DESCRIPTION = "userFriendlyDescription";
        public static final String METADATA = "metadata";
        public static final String INPUT_ORDER = "inputOrder";
        public static final String IS_REQUIRED = "isRequired";
        public static final String REQUIRED_TO_ADDED_TO_CMD = "requiredToAddedToCmd";
        public static final String DATA_STAGED = "dataStaged";
    }

    //Experiment Output Table
    public final class ExperimentOutputConstants {
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String OUTPUT_NAME = "outputName";
        public static final String OUTPUT_VALUE = "outputValue";
        public static final String DATA_TYPE = "dataType";
        public static final String APPLICATION_ARGUMENT = "applicationArgument";
        public static final String IS_REQUIRED = "isRequired";
        public static final String REQUIRED_TO_ADDED_TO_CMD = "requiredToAddedToCmd";
        public static final String DATA_MOVEMENT = "dataMovement";
        public static final String LOCATION = "location";
        public static final String SEARCH_QUERY = "searchQuery";
    }

    //User Configuration Data Table
    public final class UserConfigurationDataConstants {
        public static final String EXPERIMENT_ID = "experimentId";
    }

    //Process Table
    public final class ProcessConstants {
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String PROCESS_ID = "processId";
    }

    //Process Resource Schedule table
    public final class ProcessResourceScheduleConstants {
        public static final String PROCESS_ID = "processId";
    }

    //Job Table
    public final class JobConstants {
        public static final String JOB_ID = "jobId";
        public static final String PROCESS_ID = "processId";
        public static final String TASK_ID = "taskId";
    }

    // Job Status table
    public final class JobStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String JOB_ID = "jobId";
        public static final String TASK_ID = "taskId";
        public static final String STATE = "state";
        public static final String TIME_OF_STATE_CHANGE = "timeOfStateChange";
        public static final String REASON = "reason";
    }

    // Task Status table
    public final class TaskStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String TASK_ID = "taskId";
        public static final String STATE = "state";
        public static final String TIME_OF_STATE_CHANGE = "timeOfStateChange";
        public static final String REASON = "reason";
    }

    // Process Status table
    public final class ProcessStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String PROCESS_ID = "processId";
        public static final String STATE = "state";
        public static final String TIME_OF_STATE_CHANGE = "timeOfStateChange";
        public static final String REASON = "reason";
    }

    // Experiment Status table
    public final class ExperimentStatusConstants {
        public static final String STATUS_ID = "statusId";
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String STATE = "state";
        public static final String TIME_OF_STATE_CHANGE = "timeOfStateChange";
        public static final String REASON = "reason";
    }

    //Computational Resource Scheduling
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
