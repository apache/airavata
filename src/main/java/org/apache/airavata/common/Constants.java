/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.common;

/**
 * Constants used in Airavata should go here.
 */
public final class Constants {
    public static final String JOB = "job";

    // API security relates property names
    public static final String SECURITY_MANAGER_CLASS = "security.manager.class";
    public static final String IS_TLS_ENABLED = "TLS.enabled";
    public static final String TLS_CERT_PATH = "tls.cert.path";
    public static final String TLS_KEY_PATH = "tls.key.path";
    public static final String TLS_CLIENT_TIMEOUT = "TLS.client.timeout";

    public static final String API_METHOD_NAME = "api.method.name";

    // constants in XACML authorization response.

    public static final String AUTHZ_CACHE_MANAGER_CLASS = "authz.cache.manager.class";
    public static final String AUTHZ_CACHE_ENABLED = "authz.cache.enabled";

    public static final String IN_MEMORY_CACHE_SIZE = "in.memory.cache.size";
    public static final String LOCAL_DATA_LOCATION = "local.data.location";

    // Names of the attributes that could be passed in the AuthzToken's claims map.
    public static final String USER_NAME = "userName";
    public static final String GATEWAY_ID = "gatewayID";
    public static final String EMAIL = "email";
    // Server-derived (from the verified access token), CSV-encoded; never
    // client-asserted.
    public static final String REALM_ROLES = "realmRoles";

    // Keycloak realm roles that map to the coarse gateway-admin capabilities.
    public static final String ROLE_GATEWAY_ADMIN = "admin-rw";
    public static final String ROLE_READ_ONLY_ADMIN = "admin-ro";

    public static final String ENABLE_STREAMING_TRANSFER = "enable.streaming.transfer";

    public final class FieldConstants {
        public final class ProjectConstants {
            public static final String PROJECT_ID = "projectID";
            public static final String GATEWAY_ID = "gateway_id";
            public static final String OWNER = "owner";
            public static final String PROJECT_NAME = "name";
            public static final String DESCRIPTION = "description";
            public static final String CREATION_TIME = "creationTime";
        }

        public final class ExperimentConstants {
            public static final String EXPERIMENT_ID = "experimentId";
            public static final String PROJECT_ID = "projectId";
            public static final String GATEWAY_ID = "gatewayId";
            public static final String EXPERIMENT_TYPE = "experimentType";
            public static final String USER_NAME = "userName";
            public static final String EXPERIMENT_NAME = "experimentName";
            public static final String CREATION_TIME = "creationTime";
            public static final String DESCRIPTION = "description";
            public static final String EXECUTION_ID = "executionId";
            public static final String GATEWAY_EXECUTION_ID = "gatewayExecutionId";
            public static final String ENABLE_EMAIL_NOTIFICATION = "enableEmailNotification";
            public static final String EMAIL_ADDRESSES = "emailAddresses";
            public static final String EXPERIMENT_INPUTS = "experimentInputs";
            public static final String EXPERIMENT_OUTPUTS = "experimentOutputs";
            public static final String EXPERIMENT_STATUS = "experimentStatus";
            public static final String EXPERIMENT_ERRORS = "experimentErrors";
            public static final String USER_CONFIGURATION_DATA = "userConfigurationData";
            public static final String FROM_DATE = "fromDate";
            public static final String TO_DATE = "toDate";
            public static final String RESOURCE_HOST_ID = "resourceHostId";
        }

        public final class UserConfigurationDataConstants {
            public static final String EXPERIMENT_ID = "experimentId";
            public static final String AIRAVATA_AUTO_SCHEDULE = "airavataAutoSchedule";
            public static final String OVERRIDE_MANUAL_PARAMS = "overrideManualScheduledParams";
            public static final String SHARE_EXP = "shareExperimentPublicly";
            public static final String COMPUTATIONAL_RESOURCE_SCHEDULING = "computationalResourceScheduling";
        }

        public final class ProcessConstants {
            public static final String EXPERIMENT_ID = "experimentId";
            public static final String PROCESS_ID = "processId";
            public static final String PROCESS_STATUS = "processStatus";
            public static final String PROCESS_ERROR = "processError";
            public static final String PROCESS_INPUTS = "processInputs";
            public static final String PROCESS_OUTPUTS = "processOutputs";
            public static final String PROCESS_RESOURCE_SCHEDULE = "processResourceSchedule";
        }

        public final class TaskConstants {
            public static final String PARENT_PROCESS_ID = "parentProcessId";
            public static final String TASK_ID = "taskId";
            public static final String TASK_STATUS = "taskStatus";
            public static final String TASK_ERROR = "taskError";
        }

        public final class JobConstants {
            public static final String JOB_ID = "jobId";
            public static final String PROCESS_ID = "processId";
            public static final String TASK_ID = "taskId";
            public static final String JOB_STATUS = "taskStatus";
        }
    }
}
