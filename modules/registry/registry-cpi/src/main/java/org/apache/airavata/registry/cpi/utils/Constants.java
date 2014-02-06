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

package org.apache.airavata.registry.cpi.utils;

public class Constants {
    public final class FieldConstants {
        public final class BasicMetadataConstants {
            public static final String EXPERIMENT_NAME = "experimentName";
            public static final String USER_NAME = "userName";
            public static final String EXPERIMENT_DESC = "experimentDescription";
            public static final String SHARE_EXP_PUBLIC = "shareExperimentPublicly";
        }

        public final class ConfigurationDataConstants {
            public static final String APPLICATION_ID = "applicationId";
            public static final String APPLICATION_VERSION = "applicationVersion";
            public static final String WORKFLOW_TEMPLATE_ID = "workflowTemplateId";
            public static final String WORKFLOW_TEMPLATE_VERSION = "worklfowTemplateVersion";
            public static final String EXPERIMENT_INPUTS = "experimentInputs";
            public static final String COMPUTATIONAL_RESOURCE_SCHEDULING = "computationalResourceScheduling";
            public static final String ADVANCED_INPUT_HANDLING = "advanceInputDataHandling";
            public static final String ADVANCED_OUTPUT_HANDLING = "advanceOutputDataHandling";
            public static final String QOS_PARAMS = "qosParams";
        }
    }
}
