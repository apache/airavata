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
package org.apache.airavata.core.util;

public class DBConstants {

    public static class Experiment {
        public static final String USER_NAME = "userName";
        public static final String PROJECT_ID = "projectId";
        public static final String GATEWAY_ID = "gatewayId";
        public static final String EXPERIMENT_ID = "experimentId";
        public static final String EXPERIMENT_NAME = "experimentName";
        public static final String DESCRIPTION = "description";
        public static final String EXECUTION_ID = "executionId";
        public static final String CREATION_TIME = "creationTime";
        public static final String RESOURCE_HOST_ID = "resourceHostId";
        public static final String ACCESSIBLE_EXPERIMENT_IDS = "accessibleExperimentIds";
    }

    public static class ExperimentSummary {
        public static final String EXPERIMENT_STATUS = "experimentStatus";
        public static final String FROM_DATE = "fromDate";
        public static final String TO_DATE = "toDate";
    }

    public static class Process {
        public static final String EXPERIMENT_ID = "experimentId";
    }
}
