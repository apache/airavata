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
 * provision → stageIn → submit → (end)
 *     ↓fail      ↓fail    ↓fail
 *    fail        fail     fail
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
                .node("provision", computeBean("provisioning", type))
                .metadata("processState", "CONFIGURING_WORKSPACE")
                .metadata("retryTier", "INFRASTRUCTURE")
                .onSuccess("stageIn")
                .onFailure("fail")
                .node("stageIn", INPUT_STAGING)
                .metadata("processState", "INPUT_DATA_STAGING")
                .metadata("retryTier", "DATA")
                .onSuccess("submit")
                .onFailure("fail")
                .node("submit", computeBean("submit", type))
                .metadata("processState", "EXECUTING")
                .metadata("retryTier", "INFRASTRUCTURE")
                .onSuccess(null)
                .onFailure("fail")
                .node("fail", "markFailedTask")
                .metadata("retryTier", "CLEANUP")
                .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Post-execution DAGs
    // -------------------------------------------------------------------------

    public static ProcessDAG postDag(ComputeResourceType type) {
        return ProcessDAG.builder("monitor")
                .node("monitor", computeBean("monitoring", type))
                .metadata("processState", "MONITORING")
                .metadata("retryTier", "MONITOR")
                .onSuccess("checkOutputs")
                .onFailure("checkOutputs")
                .node("checkOutputs", "checkOutputsTask")
                .metadata("retryTier", "CHECK")
                .onSuccess("checkDataMovement")
                .onFailure("deprovision")
                .node("checkDataMovement", "checkDataMovementTask")
                .metadata("retryTier", "CHECK")
                .onSuccess("outputStaging")
                .onFailure("archive")
                .node("outputStaging", OUTPUT_STAGING)
                .metadata("processState", "OUTPUT_DATA_STAGING")
                .metadata("retryTier", "DATA")
                .onSuccess("archive")
                .onFailure("archive")
                .node("archive", ARCHIVE)
                .metadata("retryTier", "DATA")
                .onSuccess("deprovision")
                .onFailure("deprovision")
                .node("deprovision", computeBean("deprovisioning", type))
                .metadata("processState", "COMPLETED")
                .metadata("retryTier", "CLEANUP")
                .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Cancel DAGs
    // -------------------------------------------------------------------------

    public static ProcessDAG cancelDag(ComputeResourceType type) {
        return ProcessDAG.builder("cancel")
                .node("cancel", computeBean("cancel", type))
                .metadata("processState", "CANCELED")
                .metadata("retryTier", "CLEANUP")
                .terminal()
                .build();
    }

    // -------------------------------------------------------------------------
    // Bean name helpers
    // -------------------------------------------------------------------------

    private static final String INPUT_STAGING = "sftpInputStagingTask";
    private static final String OUTPUT_STAGING = "sftpOutputStagingTask";
    private static final String ARCHIVE = "sftpArchiveTask";

    private static String computeBean(String phase, ComputeResourceType type) {
        String prefix =
                switch (type) {
                    case AWS -> "aws";
                    case PLAIN -> "local";
                    default -> "slurm";
                };
        return prefix + Character.toUpperCase(phase.charAt(0)) + phase.substring(1) + "Task";
    }
}
