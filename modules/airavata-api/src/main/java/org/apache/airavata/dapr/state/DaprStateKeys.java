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
package org.apache.airavata.dapr.state;

/**
 * Utility class for generating standardized Dapr state store keys.
 *
 * <p>All Dapr state keys should be generated using methods in this class to ensure
 * consistent naming conventions and make state key usage discoverable.
 *
 * <p>Key naming convention: "{category}:{entity-type}:{identifier}"
 * Examples:
 * <ul>
 *   <li>cancel:experiment:exp-123</li>
 *   <li>workflow:state:workflow-456</li>
 * </ul>
 */
public final class DaprStateKeys {

    private static final String PREFIX_CANCEL = "cancel";
    private static final String PREFIX_WORKFLOW = "workflow";
    private static final String PREFIX_PROCESS = "process";
    private static final String PREFIX_EXPERIMENT = "experiment";

    /**
     * Generate a state key for experiment cancellation state.
     *
     * @param experimentId the experiment identifier
     * @return state key in format "cancel:experiment:{experimentId}"
     */
    public static String cancelExperiment(String experimentId) {
        return String.join(":", PREFIX_CANCEL, PREFIX_EXPERIMENT, experimentId);
    }

    /**
     * Generate a state key for workflow state.
     *
     * @param workflowId the workflow identifier
     * @return state key in format "workflow:state:{workflowId}"
     */
    public static String workflowState(String workflowId) {
        return String.join(":", PREFIX_WORKFLOW, "state", workflowId);
    }

    /**
     * Generate a state key for process state.
     *
     * @param processId the process identifier
     * @return state key in format "process:state:{processId}"
     */
    public static String processState(String processId) {
        return String.join(":", PREFIX_PROCESS, "state", processId);
    }

    /**
     * Generate a state key for experiment state.
     *
     * @param experimentId the experiment identifier
     * @return state key in format "experiment:state:{experimentId}"
     */
    public static String experimentState(String experimentId) {
        return String.join(":", PREFIX_EXPERIMENT, "state", experimentId);
    }

    /**
     * Generate a state key for process cancellation state.
     *
     * @param processId the process identifier
     * @return state key in format "cancel:process:{processId}"
     */
    public static String cancelProcess(String processId) {
        return String.join(":", PREFIX_CANCEL, PREFIX_PROCESS, processId);
    }

    private DaprStateKeys() {
        // Utility class - prevent instantiation
    }
}
