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
package org.apache.airavata.execution.dag;

import org.apache.airavata.compute.resource.model.ComputeResourceType;

/**
 * Factory for provider-specific {@link ProcessDAG} templates.
 *
 * <p>Provides pre-built DAGs for each execution phase (pre, post, cancel)
 * parameterised by {@link ComputeResourceType}. Each DAG defines the task
 * execution order with success/failure edges and node metadata for the
 * {@link org.apache.airavata.execution.dag.interceptor.StatusPublishingInterceptor}.
 *
 * <h3>Pre-execution DAG (SLURM/PLAIN)</h3>
 * <pre>
 * provision → stageIn → submit → checkIntermediate → [deprovision] → (end)
 *     ↓fail      ↓fail    ↓fail                ↓no
 *    fail        fail     fail                 (end)
 * </pre>
 *
 * <h3>Post-execution DAG</h3>
 * <pre>
 * monitor → checkOutputs → [checkDataMovement → outputStaging] → archive → deprovision → (end)
 *                ↓no                   ↓no                                     ↓fail
 *            deprovision           archive                                    (end)
 * </pre>
 *
 * <h3>Cancel DAG</h3>
 * <pre>
 * cancel → (end)
 * </pre>
 */
public final class DAGTemplates {

    private DAGTemplates() {}

    // -------------------------------------------------------------------------
    // Pre-execution DAGs
    // -------------------------------------------------------------------------

    public static ProcessDAG preDag(ComputeResourceType type) {
        return ProcessDAG.builder("provision")
                .node("provision", provisioningBean(type))
                    .metadata("processState", "CONFIGURING_WORKSPACE")
                    .onSuccess("stageIn").onFailure("fail")
                .node("stageIn", inputStagingBean())
                    .metadata("processState", "INPUT_DATA_STAGING")
                    .onSuccess("submit").onFailure("fail")
                .node("submit", submitBean(type))
                    .metadata("processState", "EXECUTING")
                    .onSuccess("checkIntermediate").onFailure("fail")
                .node("checkIntermediate", "checkIntermediateTransferTask")
                    .onSuccess("preDeprovision").onFailure(null)
                .node("preDeprovision", deprovisioningBean(type))
                    .terminal()
                .node("fail", "markFailedTask")
                    .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Post-execution DAGs
    // -------------------------------------------------------------------------

    public static ProcessDAG postDag(ComputeResourceType type) {
        return ProcessDAG.builder("monitor")
                .node("monitor", monitoringBean(type))
                    .metadata("processState", "MONITORING")
                    .onSuccess("checkOutputs").onFailure("checkOutputs")
                .node("checkOutputs", "checkOutputsTask")
                    .onSuccess("checkDataMovement").onFailure("deprovision")
                .node("checkDataMovement", "checkDataMovementTask")
                    .onSuccess("outputStaging").onFailure("archive")
                .node("outputStaging", outputStagingBean())
                    .metadata("processState", "OUTPUT_DATA_STAGING")
                    .onSuccess("archive").onFailure("archive")
                .node("archive", archiveBean())
                    .onSuccess("deprovision").onFailure("deprovision")
                .node("deprovision", deprovisioningBean(type))
                    .metadata("processState", "COMPLETED")
                    .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Cancel DAGs
    // -------------------------------------------------------------------------

    public static ProcessDAG cancelDag(ComputeResourceType type) {
        return ProcessDAG.builder("cancel")
                .node("cancel", cancelBean(type))
                    .metadata("processState", "CANCELED")
                    .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Bean name helpers
    // -------------------------------------------------------------------------

    private static String provisioningBean(ComputeResourceType type) {
        return switch (type) {
            case AWS -> "awsProvisioningTask";
            case PLAIN -> "localProvisioningTask";
            default -> "slurmProvisioningTask";
        };
    }

    private static String submitBean(ComputeResourceType type) {
        return switch (type) {
            case AWS -> "awsSubmitTask";
            case PLAIN -> "localSubmitTask";
            default -> "slurmSubmitTask";
        };
    }

    private static String monitoringBean(ComputeResourceType type) {
        return switch (type) {
            case AWS -> "awsMonitoringTask";
            case PLAIN -> "localMonitoringTask";
            default -> "slurmMonitoringTask";
        };
    }

    private static String cancelBean(ComputeResourceType type) {
        return switch (type) {
            case AWS -> "awsCancelTask";
            case PLAIN -> "localCancelTask";
            default -> "slurmCancelTask";
        };
    }

    private static String deprovisioningBean(ComputeResourceType type) {
        return switch (type) {
            case AWS -> "awsDeprovisioningTask";
            case PLAIN -> "localDeprovisioningTask";
            default -> "slurmDeprovisioningTask";
        };
    }

    // -------------------------------------------------------------------------
    // Storage bean name helpers (currently SFTP-only; add S3 etc. later)
    // -------------------------------------------------------------------------

    private static String inputStagingBean() {
        return "sftpInputStagingTask";
    }

    private static String outputStagingBean() {
        return "sftpOutputStagingTask";
    }

    private static String archiveBean() {
        return "sftpArchiveTask";
    }
}
