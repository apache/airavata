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
package org.apache.airavata.orchestrator.internal.workflow;

import java.util.UUID;

/**
 * Utility class for generating standardized Dapr workflow identifiers.
 *
 * <p>Provides consistent naming conventions for workflow IDs to ensure
 * traceability and prevent naming conflicts.
 *
 * <p>Naming convention: "{entityId}-{workflowType}-{uniqueId}"
 * Examples:
 * <ul>
 *   <li>process-123-PRE-a1b2c3d4</li>
 *   <li>process-456-CANCEL-e5f6g7h8</li>
 *   <li>process-789-POST-i9j0k1l2</li>
 * </ul>
 */
public final class WorkflowNaming {

    /** Workflow type prefix for pre-workflow (before process execution). */
    public static final String TYPE_PRE = "PRE";

    /** Workflow type prefix for post-workflow (after process execution). */
    public static final String TYPE_POST = "POST";

    /** Workflow type prefix for cancel workflow. */
    public static final String TYPE_CANCEL = "CANCEL";

    /** Workflow type prefix for parsing workflow. */
    public static final String TYPE_PARSING = "PARSING";

    /**
     * Generate a workflow ID for a pre-workflow.
     *
     * @param processId the process identifier
     * @return workflow ID in format "{processId}-PRE-{uuid}"
     */
    public static String preWorkflow(String processId) {
        return String.format("%s-%s-%s", processId, TYPE_PRE, UUID.randomUUID().toString());
    }

    /**
     * Generate a workflow ID for a post-workflow.
     *
     * @param processId the process identifier
     * @return workflow ID in format "{processId}-POST-{uuid}"
     */
    public static String postWorkflow(String processId) {
        return String.format("%s-%s-%s", processId, TYPE_POST, UUID.randomUUID().toString());
    }

    /**
     * Generate a workflow ID for a cancel workflow.
     *
     * @param processId the process identifier
     * @return workflow ID in format "{processId}-CANCEL-{uuid}"
     */
    public static String cancelWorkflow(String processId) {
        return String.format(
                "%s-%s-%s", processId, TYPE_CANCEL, UUID.randomUUID().toString());
    }

    /**
     * Generate a workflow ID for a parsing workflow.
     *
     * @param processId the process identifier
     * @return workflow ID in format "{processId}-PARSING-{uuid}"
     */
    public static String parsingWorkflow(String processId) {
        return String.format(
                "%s-%s-%s", processId, TYPE_PARSING, UUID.randomUUID().toString());
    }

    /**
     * Generate a workflow ID with a custom type.
     *
     * @param entityId the entity identifier (e.g., processId, experimentId)
     * @param workflowType the workflow type
     * @return workflow ID in format "{entityId}-{workflowType}-{uuid}"
     */
    public static String workflow(String entityId, String workflowType) {
        return String.format(
                "%s-%s-%s", entityId, workflowType, UUID.randomUUID().toString());
    }

    private WorkflowNaming() {
        // Utility class - prevent instantiation
    }
}
