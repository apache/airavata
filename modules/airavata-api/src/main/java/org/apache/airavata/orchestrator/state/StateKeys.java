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
package org.apache.airavata.orchestrator.state;

/**
 * Utility for generating standardized state store keys.
 * Format: "{category}:{entity-type}:{identifier}"
 */
public final class StateKeys {

    private StateKeys() {}

    public static String cancelExperiment(String experimentId) {
        return "cancel:experiment:" + experimentId;
    }

    public static String workflowState(String workflowId) {
        return "workflow:state:" + workflowId;
    }

    public static String processState(String processId) {
        return "process:state:" + processId;
    }

    public static String experimentState(String experimentId) {
        return "experiment:state:" + experimentId;
    }

    public static String cancelProcess(String processId) {
        return "cancel:process:" + processId;
    }

    public static String workflowInstance(String workflowInstanceId) {
        return "workflow:instance:" + workflowInstanceId;
    }

    public static String taskState(String taskId) {
        return "task:state:" + taskId;
    }

    public static String processLock(String processId) {
        return "lock:process:" + processId;
    }
}
