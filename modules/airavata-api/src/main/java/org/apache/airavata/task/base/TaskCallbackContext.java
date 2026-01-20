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
package org.apache.airavata.task.base;

import java.util.Map;

/**
 * Stub class for TaskCallbackContext.
 *
 * <p>Replaces org.apache.helix.task.TaskCallbackContext as part of the migration from Helix to Dapr.
 * This class provides the same API surface as Helix's TaskCallbackContext for compatibility
 * with existing task implementations.
 */
public class TaskCallbackContext {

    private TaskConfig taskConfig;
    private JobConfig jobConfig;

    public TaskConfig getTaskConfig() {
        return taskConfig;
    }

    public void setTaskConfig(TaskConfig taskConfig) {
        this.taskConfig = taskConfig;
    }

    public JobConfig getJobConfig() {
        return jobConfig;
    }

    public void setJobConfig(JobConfig jobConfig) {
        this.jobConfig = jobConfig;
    }

    public static class TaskConfig {
        private Map<String, String> configMap;

        public Map<String, String> getConfigMap() {
            return configMap;
        }

        public void setConfigMap(Map<String, String> configMap) {
            this.configMap = configMap;
        }
    }

    public static class JobConfig {
        private String jobId;
        private String workflow;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getWorkflow() {
            return workflow;
        }

        public void setWorkflow(String workflow) {
            this.workflow = workflow;
        }
    }
}
