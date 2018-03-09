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

    public static class ApplicationDeployment {
        public static final String APPLICATION_DEPLOYMENT_ID = "appDeploymentId";
        public static final String APPLICATION_MODULE_ID = "appModuleId";
        public static final String COMPUTE_HOST_ID = "computeHostId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String ACCESSIBLE_APPLICATION_DEPLOYMENT_IDS = "accessibleAppDeploymentIds";
        public static final String ACCESSIBLE_COMPUTE_HOST_IDS = "accessibleComputeHostIds";
    }

    public static class ApplicationModule {
        public static final String APPLICATION_MODULE_ID = "appModuleId";
        public static final String APPLICATION_MODULE_NAME = "appModuleName";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String ACCESSIBLE_APPLICATION_MODULE_IDS = "accessibleAppModuleIds";
    }

    public static class ApplicationInterface {
        public static final String APPLICATION_INTERFACE_ID = "applicationInterfaceId";
        public static final String APPLICATION_NAME = "applicationName";
        public static final String GATEWAY_ID = "gatewayId";
    }

    public static class ApplicationInput {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String INPUT_KEY = "inputKey";
    }

    public static class ApplicationOutput {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String OUTPUT_KEY = "outputKey";
    }

    public static class AppModuleMapping {
        public static final String APPLICATION_INTERFACE_ID = "interfaceId";
        public static final String APPLICATION_MODULE_ID = "moduleId";
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

    public static class ResourceJobManager {
        public static final String RESOURCE_JOB_MANAGER_ID = "resourceJobManagerId";
    }

    public static class GroupResourceProfile {
        public static final String GATEWAY_ID = "gatewayId";
        public static final String GROUP_RESOURCE_PROFILE_ID = "groupResourceProfileId";
    }

    public final class PreJobCommand {
        public static final String APPLICATION_DEPLOYMENT_ID = "deploymentId";
        public static final String COMMAND = "command";
    }

    public final class PostJobCommand {
        public static final String APPLICATION_DEPLOYMENT_ID = "deploymentId";
        public static final String COMMAND = "command";
    }

    public final class LibraryPrepandPathConstants {
        public static final String APPLICATION_DEPLOYMENT_ID = "deploymentID";
        public static final String NAME = "name";
    }

    public final class LibraryApendPath {
        public static final String APPLICATION_DEPLOYMENT_ID = "deploymentID";
        public static final String NAME = "name";
    }

    public final class AppEnvironment {
        public static final String APPLICATION_DEPLOYMENT_ID = "deploymentID";
        public static final String NAME = "name";
    }

    public final class ModuleLoadCmd {
        public static final String APPLICATION_DEPLOYMENT_ID = "appDeploymentId";
        public static final String COMMAND = "cmd";
    }

}
