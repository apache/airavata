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
package org.apache.airavata.registry.core.utils;

public class DBConstants {

    public static int SELECT_MAX_ROWS = 1000;
    public static final String CONFIGURATION = "Configuration";
    public static final String WORKFLOW = "Workflow";

    public static class ApplicationDeployment {
        public static final String APPLICATION_MODULE_ID = "appModuleId";
        public static final String COMPUTE_HOST_ID = "computeHostId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS = "accessibleAppDeploymentIds";
        public static final String ACCESSIBLE_COMPUTE_HOST_IDS = "accessibleComputeHostIds";
    }

    public static class ApplicationModule {
        public static final String APPLICATION_MODULE_NAME = "appModuleName";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationInterface {
        public static final String APPLICATION_NAME = "applicationName";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationInput {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
    }

    public static class ApplicationOutput {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
    }

    public static class ComputeResourcePreference {
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class StorageResourcePreference {
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ComputeResource {
        public static final String HOST_NAME = "hostName";
        public static final String COMPUTE_RESOURCE_ID = "computeResourceId";
    }

    public static class StorageResource {
        public static final String HOST_NAME = "hostName";
    }

    public static class ResourceJobManager {
        public static final String RESOURCE_JOB_MANAGER_ID = "resourceJobManagerId";
    }

    public static class GroupResourceProfile {
        public static final String GATEWAY_ID = "gatewayId";
        public static final String GROUP_RESOURCE_PROFILE_ID = "groupResourceProfileId";
        public static final String ACCESSIBLE_GROUP_RESOURCE_IDS = "accessibleGroupResProfileIds";
    }

    public static class UserResourceProfile {
        public static final String USER_ID = "userId";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class UserComputeResourcePreference {
        public static final String USER_ID = "userId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String COMPUTE_RESOURCE_ID = "computeResourceId";
    }

    public static class UserStoragePreference {
        public static final String USER_ID = "userId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String STORAGE_RESOURCE_ID = "storageResourceId";
    }

    public static class DataProduct {
        public static final String GATEWAY_ID = "gatewayId";
        public static final String OWNER_NAME = "ownerName";
        public static final String PRODUCT_NAME = "productName";
        public static final String PARENT_PRODUCT_URI = "parentProductUri";
    }

    public static class Workflow {
        public static final String GATEWAY_ID = "gatewayId";
        public static final String WORKFLOW_NAME = "workflowName";
    }

}
