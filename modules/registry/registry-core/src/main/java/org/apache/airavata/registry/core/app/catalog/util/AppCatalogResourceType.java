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
package org.apache.airavata.registry.core.app.catalog.util;

public enum AppCatalogResourceType {
	COMPUTE_RESOURCE,
    HOST_ALIAS,
    HOST_IPADDRESS,
    GSISSH_SUBMISSION,
    GSISSH_EXPORT,
    PRE_JOBCOMMAND,
    POST_JOBCOMMAND,
    GLOBUS_SUBMISSION,
    UNICORE_JOB_SUBMISSION,
    UNICORE_DATA_MOVEMENT,
    GLOBUS_GK_ENDPOINT,
    SSH_JOB_SUBMISSION,
	SCP_DATA_MOVEMENT,
	GRIDFTP_DATA_MOVEMENT,
	GRIDFTP_ENDPOINT,
    JOB_SUBMISSION_PROTOCOL,
    DATA_MOVEMENT_PROTOCOL,
    APPLICATION_MODULE,
    APPLICATION_DEPLOYMENT,
    LIBRARY_PREPAND_PATH,
    LIBRARY_APEND_PATH,
    APP_ENVIRONMENT,
    APPLICATION_INTERFACE,
    APP_MODULE_MAPPING,
    APPLICATION_INPUT,
    APPLICATION_OUTPUT,
    GATEWAY_PROFILE,
    USER_RESOURCE_PROFILE,
    COMPUTE_RESOURCE_PREFERENCE,
    USER_COMPUTE_RESOURCE_PREFERENCE,
    STORAGE_PREFERENCE,
    USER_STORAGE_PREFERENCE,
    STORAGE_RESOURCE,
    STORAGE_INTERFACE,
	BATCH_QUEUE,
	COMPUTE_RESOURCE_FILE_SYSTEM,
	JOB_SUBMISSION_INTERFACE,
	DATA_MOVEMENT_INTERFACE,
	RESOURCE_JOB_MANAGER,
	JOB_MANAGER_COMMAND,
    PARALLELISM_PREFIX_COMMAND,
	LOCAL_SUBMISSION,
	LOCAL_DATA_MOVEMENT,
    MODULE_LOAD_CMD,
    ClOUD_SUBMISSION,
}
