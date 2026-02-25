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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Pure unit tests for {@link DAGTemplates}.
 * Verifies that each pre-built DAG template has the correct nodes,
 * edges, entry points, bean names, and metadata for every
 * {@link ComputeResourceType}.
 * No Spring context or external dependencies required.
 */
public class DAGTemplatesTest {

    // ===========================================================================
    // preDag — entry node
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) entry node is 'provision'")
    @EnumSource(ComputeResourceType.class)
    public void preDag_entryNodeIs_provision(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("provision", dag.entryNodeId(),
                "preDag must always start at the 'provision' node");
    }

    // ===========================================================================
    // preDag — node presence
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) contains all required nodes")
    @EnumSource(ComputeResourceType.class)
    public void preDag_containsAllRequiredNodes(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertNotNull(dag.getNode("provision"),        "'provision' node must be present");
        assertNotNull(dag.getNode("stageIn"),          "'stageIn' node must be present");
        assertNotNull(dag.getNode("submit"),           "'submit' node must be present");
        assertNotNull(dag.getNode("checkIntermediate"),"'checkIntermediate' node must be present");
        assertNotNull(dag.getNode("preDeprovision"),   "'preDeprovision' node must be present");
        assertNotNull(dag.getNode("fail"),             "'fail' node must be present");
    }

    @ParameterizedTest(name = "preDag({0}) has exactly 6 nodes")
    @EnumSource(ComputeResourceType.class)
    public void preDag_hasExactlySixNodes(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals(6, dag.nodes().size(),
                "preDag must contain exactly 6 nodes");
    }

    // ===========================================================================
    // preDag — SLURM provider bean names
    // ===========================================================================

    @Test
    public void preDag_slurm_provisionNode_usesSlurmProvisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.SLURM);

        assertEquals("slurmProvisioningTask", dag.getNode("provision").taskBeanName(),
                "SLURM preDag 'provision' must use 'slurmProvisioningTask'");
    }

    @Test
    public void preDag_slurm_submitNode_usesSlurmSubmitTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.SLURM);

        assertEquals("slurmSubmitTask", dag.getNode("submit").taskBeanName(),
                "SLURM preDag 'submit' must use 'slurmSubmitTask'");
    }

    @Test
    public void preDag_slurm_stageInNode_usesSftpInputStagingTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.SLURM);

        assertEquals("sftpInputStagingTask", dag.getNode("stageIn").taskBeanName(),
                "preDag 'stageIn' must use 'sftpInputStagingTask' for SLURM");
    }

    @Test
    public void preDag_slurm_preDeprovisionNode_usesSlurmDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.SLURM);

        assertEquals("slurmDeprovisioningTask", dag.getNode("preDeprovision").taskBeanName(),
                "SLURM preDag 'preDeprovision' must use 'slurmDeprovisioningTask'");
    }

    // ===========================================================================
    // preDag — AWS provider bean names
    // ===========================================================================

    @Test
    public void preDag_aws_provisionNode_usesAwsProvisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.AWS);

        assertEquals("awsProvisioningTask", dag.getNode("provision").taskBeanName(),
                "AWS preDag 'provision' must use 'awsProvisioningTask'");
    }

    @Test
    public void preDag_aws_submitNode_usesAwsSubmitTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.AWS);

        assertEquals("awsSubmitTask", dag.getNode("submit").taskBeanName(),
                "AWS preDag 'submit' must use 'awsSubmitTask'");
    }

    @Test
    public void preDag_aws_stageInNode_usesSftpInputStagingTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.AWS);

        assertEquals("sftpInputStagingTask", dag.getNode("stageIn").taskBeanName(),
                "AWS preDag 'stageIn' must use 'sftpInputStagingTask'");
    }

    @Test
    public void preDag_aws_preDeprovisionNode_usesAwsDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.AWS);

        assertEquals("awsDeprovisioningTask", dag.getNode("preDeprovision").taskBeanName(),
                "AWS preDag 'preDeprovision' must use 'awsDeprovisioningTask'");
    }

    // ===========================================================================
    // preDag — PLAIN provider bean names
    // ===========================================================================

    @Test
    public void preDag_plain_submitNode_usesLocalSubmitTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.PLAIN);

        assertEquals("localSubmitTask", dag.getNode("submit").taskBeanName(),
                "PLAIN preDag 'submit' must use 'localSubmitTask'");
    }

    @Test
    public void preDag_plain_provisionNode_usesLocalProvisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.PLAIN);

        assertEquals("localProvisioningTask", dag.getNode("provision").taskBeanName(),
                "PLAIN preDag 'provision' must use 'localProvisioningTask'");
    }

    @Test
    public void preDag_plain_preDeprovisionNode_usesLocalDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.preDag(ComputeResourceType.PLAIN);

        assertEquals("localDeprovisioningTask", dag.getNode("preDeprovision").taskBeanName(),
                "PLAIN preDag 'preDeprovision' must use 'localDeprovisioningTask'");
    }

    // ===========================================================================
    // preDag — shared node bean names (constant across all providers)
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) checkIntermediate uses 'checkIntermediateTransferTask'")
    @EnumSource(ComputeResourceType.class)
    public void preDag_checkIntermediateNode_usesCheckIntermediateTransferTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("checkIntermediateTransferTask",
                dag.getNode("checkIntermediate").taskBeanName(),
                "checkIntermediate must always use 'checkIntermediateTransferTask'");
    }

    @ParameterizedTest(name = "preDag({0}) fail uses 'markFailedTask'")
    @EnumSource(ComputeResourceType.class)
    public void preDag_failNode_usesMarkFailedTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("markFailedTask", dag.getNode("fail").taskBeanName(),
                "fail node must always use 'markFailedTask'");
    }

    // ===========================================================================
    // preDag — edge wiring
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) provision → stageIn on success, fail on failure")
    @EnumSource(ComputeResourceType.class)
    public void preDag_provisionNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode provision = dag.getNode("provision");

        assertEquals("stageIn", provision.onSuccess(),
                "provision.onSuccess must be 'stageIn'");
        assertEquals("fail", provision.onFailure(),
                "provision.onFailure must be 'fail'");
    }

    @ParameterizedTest(name = "preDag({0}) stageIn → submit on success, fail on failure")
    @EnumSource(ComputeResourceType.class)
    public void preDag_stageInNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode stageIn = dag.getNode("stageIn");

        assertEquals("submit", stageIn.onSuccess(),
                "stageIn.onSuccess must be 'submit'");
        assertEquals("fail", stageIn.onFailure(),
                "stageIn.onFailure must be 'fail'");
    }

    @ParameterizedTest(name = "preDag({0}) submit → checkIntermediate on success, fail on failure")
    @EnumSource(ComputeResourceType.class)
    public void preDag_submitNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode submit = dag.getNode("submit");

        assertEquals("checkIntermediate", submit.onSuccess(),
                "submit.onSuccess must be 'checkIntermediate'");
        assertEquals("fail", submit.onFailure(),
                "submit.onFailure must be 'fail'");
    }

    @ParameterizedTest(name = "preDag({0}) checkIntermediate → preDeprovision on success, null on failure")
    @EnumSource(ComputeResourceType.class)
    public void preDag_checkIntermediateNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode checkIntermediate = dag.getNode("checkIntermediate");

        assertEquals("preDeprovision", checkIntermediate.onSuccess(),
                "checkIntermediate.onSuccess must be 'preDeprovision'");
        assertNull(checkIntermediate.onFailure(),
                "checkIntermediate.onFailure must be null (no intermediate transfer path)");
    }

    @ParameterizedTest(name = "preDag({0}) preDeprovision is terminal")
    @EnumSource(ComputeResourceType.class)
    public void preDag_preDeprovisionNode_isTerminal(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode preDeprovision = dag.getNode("preDeprovision");

        assertNull(preDeprovision.onSuccess(), "preDeprovision.onSuccess must be null (terminal)");
        assertNull(preDeprovision.onFailure(), "preDeprovision.onFailure must be null (terminal)");
    }

    @ParameterizedTest(name = "preDag({0}) fail is terminal")
    @EnumSource(ComputeResourceType.class)
    public void preDag_failNode_isTerminal(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);
        TaskNode fail = dag.getNode("fail");

        assertNull(fail.onSuccess(), "fail.onSuccess must be null (terminal)");
        assertNull(fail.onFailure(), "fail.onFailure must be null (terminal)");
    }

    // ===========================================================================
    // preDag — metadata
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) provision metadata processState = CONFIGURING_WORKSPACE")
    @EnumSource(ComputeResourceType.class)
    public void preDag_provisionNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("CONFIGURING_WORKSPACE",
                dag.getNode("provision").metadata().get("processState"),
                "provision node must publish processState=CONFIGURING_WORKSPACE");
    }

    @ParameterizedTest(name = "preDag({0}) stageIn metadata processState = INPUT_DATA_STAGING")
    @EnumSource(ComputeResourceType.class)
    public void preDag_stageInNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("INPUT_DATA_STAGING",
                dag.getNode("stageIn").metadata().get("processState"),
                "stageIn node must publish processState=INPUT_DATA_STAGING");
    }

    @ParameterizedTest(name = "preDag({0}) submit metadata processState = EXECUTING")
    @EnumSource(ComputeResourceType.class)
    public void preDag_submitNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertEquals("EXECUTING",
                dag.getNode("submit").metadata().get("processState"),
                "submit node must publish processState=EXECUTING");
    }

    // ===========================================================================
    // postDag — entry node
    // ===========================================================================

    @ParameterizedTest(name = "postDag({0}) entry node is 'monitor'")
    @EnumSource(ComputeResourceType.class)
    public void postDag_entryNodeIs_monitor(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("monitor", dag.entryNodeId(),
                "postDag must always start at the 'monitor' node");
    }

    // ===========================================================================
    // postDag — node presence
    // ===========================================================================

    @ParameterizedTest(name = "postDag({0}) contains all required nodes")
    @EnumSource(ComputeResourceType.class)
    public void postDag_containsAllRequiredNodes(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertNotNull(dag.getNode("monitor"),           "'monitor' node must be present");
        assertNotNull(dag.getNode("checkOutputs"),      "'checkOutputs' node must be present");
        assertNotNull(dag.getNode("checkDataMovement"), "'checkDataMovement' node must be present");
        assertNotNull(dag.getNode("outputStaging"),     "'outputStaging' node must be present");
        assertNotNull(dag.getNode("archive"),           "'archive' node must be present");
        assertNotNull(dag.getNode("deprovision"),       "'deprovision' node must be present");
    }

    @ParameterizedTest(name = "postDag({0}) has exactly 6 nodes")
    @EnumSource(ComputeResourceType.class)
    public void postDag_hasExactlySixNodes(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals(6, dag.nodes().size(),
                "postDag must contain exactly 6 nodes");
    }

    // ===========================================================================
    // postDag — bean names
    // ===========================================================================

    @Test
    public void postDag_slurm_monitorNode_usesSlurmMonitoringTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.SLURM);

        assertEquals("slurmMonitoringTask", dag.getNode("monitor").taskBeanName(),
                "SLURM postDag 'monitor' must use 'slurmMonitoringTask'");
    }

    @Test
    public void postDag_aws_monitorNode_usesAwsMonitoringTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.AWS);

        assertEquals("awsMonitoringTask", dag.getNode("monitor").taskBeanName(),
                "AWS postDag 'monitor' must use 'awsMonitoringTask'");
    }

    @Test
    public void postDag_plain_monitorNode_usesLocalMonitoringTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.PLAIN);

        assertEquals("localMonitoringTask", dag.getNode("monitor").taskBeanName(),
                "PLAIN postDag 'monitor' must use 'localMonitoringTask'");
    }

    @ParameterizedTest(name = "postDag({0}) checkOutputs uses 'checkOutputsTask'")
    @EnumSource(ComputeResourceType.class)
    public void postDag_checkOutputsNode_usesCheckOutputsTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("checkOutputsTask", dag.getNode("checkOutputs").taskBeanName());
    }

    @ParameterizedTest(name = "postDag({0}) checkDataMovement uses 'checkDataMovementTask'")
    @EnumSource(ComputeResourceType.class)
    public void postDag_checkDataMovementNode_usesCheckDataMovementTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("checkDataMovementTask", dag.getNode("checkDataMovement").taskBeanName());
    }

    @ParameterizedTest(name = "postDag({0}) outputStaging uses 'sftpOutputStagingTask'")
    @EnumSource(ComputeResourceType.class)
    public void postDag_outputStagingNode_usesSftpOutputStagingTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("sftpOutputStagingTask", dag.getNode("outputStaging").taskBeanName());
    }

    @ParameterizedTest(name = "postDag({0}) archive uses 'sftpArchiveTask'")
    @EnumSource(ComputeResourceType.class)
    public void postDag_archiveNode_usesSftpArchiveTask(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("sftpArchiveTask", dag.getNode("archive").taskBeanName());
    }

    @Test
    public void postDag_slurm_deprovisionNode_usesSlurmDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.SLURM);

        assertEquals("slurmDeprovisioningTask", dag.getNode("deprovision").taskBeanName(),
                "SLURM postDag 'deprovision' must use 'slurmDeprovisioningTask'");
    }

    @Test
    public void postDag_aws_deprovisionNode_usesAwsDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.AWS);

        assertEquals("awsDeprovisioningTask", dag.getNode("deprovision").taskBeanName(),
                "AWS postDag 'deprovision' must use 'awsDeprovisioningTask'");
    }

    @Test
    public void postDag_plain_deprovisionNode_usesLocalDeprovisioningTask() {
        ProcessDAG dag = DAGTemplates.postDag(ComputeResourceType.PLAIN);

        assertEquals("localDeprovisioningTask", dag.getNode("deprovision").taskBeanName(),
                "PLAIN postDag 'deprovision' must use 'localDeprovisioningTask'");
    }

    // ===========================================================================
    // postDag — edge wiring
    // ===========================================================================

    @ParameterizedTest(name = "postDag({0}) monitor → checkOutputs on both success and failure")
    @EnumSource(ComputeResourceType.class)
    public void postDag_monitorNode_alwaysGoesToCheckOutputs(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode monitor = dag.getNode("monitor");

        assertEquals("checkOutputs", monitor.onSuccess(),
                "monitor.onSuccess must be 'checkOutputs'");
        assertEquals("checkOutputs", monitor.onFailure(),
                "monitor.onFailure must also be 'checkOutputs' (monitoring failure is non-fatal)");
    }

    @ParameterizedTest(name = "postDag({0}) checkOutputs → checkDataMovement on success, deprovision on failure")
    @EnumSource(ComputeResourceType.class)
    public void postDag_checkOutputsNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode checkOutputs = dag.getNode("checkOutputs");

        assertEquals("checkDataMovement", checkOutputs.onSuccess(),
                "checkOutputs.onSuccess must be 'checkDataMovement'");
        assertEquals("deprovision", checkOutputs.onFailure(),
                "checkOutputs.onFailure must be 'deprovision' (skip staging when no outputs)");
    }

    @ParameterizedTest(name = "postDag({0}) checkDataMovement → outputStaging on success, archive on failure")
    @EnumSource(ComputeResourceType.class)
    public void postDag_checkDataMovementNode_edgesAreCorrect(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode checkDataMovement = dag.getNode("checkDataMovement");

        assertEquals("outputStaging", checkDataMovement.onSuccess(),
                "checkDataMovement.onSuccess must be 'outputStaging'");
        assertEquals("archive", checkDataMovement.onFailure(),
                "checkDataMovement.onFailure must be 'archive' (skip staging when no data movement)");
    }

    @ParameterizedTest(name = "postDag({0}) outputStaging → archive on both success and failure")
    @EnumSource(ComputeResourceType.class)
    public void postDag_outputStagingNode_alwaysGoesToArchive(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode outputStaging = dag.getNode("outputStaging");

        assertEquals("archive", outputStaging.onSuccess(),
                "outputStaging.onSuccess must be 'archive'");
        assertEquals("archive", outputStaging.onFailure(),
                "outputStaging.onFailure must also be 'archive' (archive regardless)");
    }

    @ParameterizedTest(name = "postDag({0}) archive → deprovision on both success and failure")
    @EnumSource(ComputeResourceType.class)
    public void postDag_archiveNode_alwaysGoesToDeprovision(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode archive = dag.getNode("archive");

        assertEquals("deprovision", archive.onSuccess(),
                "archive.onSuccess must be 'deprovision'");
        assertEquals("deprovision", archive.onFailure(),
                "archive.onFailure must also be 'deprovision' (always deprovision)");
    }

    @ParameterizedTest(name = "postDag({0}) deprovision is terminal")
    @EnumSource(ComputeResourceType.class)
    public void postDag_deprovisionNode_isTerminal(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);
        TaskNode deprovision = dag.getNode("deprovision");

        assertNull(deprovision.onSuccess(), "deprovision.onSuccess must be null (terminal)");
        assertNull(deprovision.onFailure(), "deprovision.onFailure must be null (terminal)");
    }

    // ===========================================================================
    // postDag — metadata
    // ===========================================================================

    @ParameterizedTest(name = "postDag({0}) monitor metadata processState = MONITORING")
    @EnumSource(ComputeResourceType.class)
    public void postDag_monitorNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("MONITORING",
                dag.getNode("monitor").metadata().get("processState"),
                "monitor node must publish processState=MONITORING");
    }

    @ParameterizedTest(name = "postDag({0}) outputStaging metadata processState = OUTPUT_DATA_STAGING")
    @EnumSource(ComputeResourceType.class)
    public void postDag_outputStagingNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("OUTPUT_DATA_STAGING",
                dag.getNode("outputStaging").metadata().get("processState"),
                "outputStaging node must publish processState=OUTPUT_DATA_STAGING");
    }

    @ParameterizedTest(name = "postDag({0}) deprovision metadata processState = COMPLETED")
    @EnumSource(ComputeResourceType.class)
    public void postDag_deprovisionNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertEquals("COMPLETED",
                dag.getNode("deprovision").metadata().get("processState"),
                "deprovision node must publish processState=COMPLETED");
    }

    // ===========================================================================
    // cancelDag — entry node
    // ===========================================================================

    @ParameterizedTest(name = "cancelDag({0}) entry node is 'cancel'")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_entryNodeIs_cancel(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);

        assertEquals("cancel", dag.entryNodeId(),
                "cancelDag must always start at the 'cancel' node");
    }

    // ===========================================================================
    // cancelDag — node presence
    // ===========================================================================

    @ParameterizedTest(name = "cancelDag({0}) has exactly 1 node")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_hasExactlyOneNode(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);

        assertEquals(1, dag.nodes().size(),
                "cancelDag must contain exactly 1 node");
    }

    // ===========================================================================
    // cancelDag — bean names
    // ===========================================================================

    @Test
    public void cancelDag_slurm_cancelNode_usesSlurmCancelTask() {
        ProcessDAG dag = DAGTemplates.cancelDag(ComputeResourceType.SLURM);

        assertEquals("slurmCancelTask", dag.getNode("cancel").taskBeanName(),
                "SLURM cancelDag 'cancel' must use 'slurmCancelTask'");
    }

    @Test
    public void cancelDag_aws_cancelNode_usesAwsCancelTask() {
        ProcessDAG dag = DAGTemplates.cancelDag(ComputeResourceType.AWS);

        assertEquals("awsCancelTask", dag.getNode("cancel").taskBeanName(),
                "AWS cancelDag 'cancel' must use 'awsCancelTask'");
    }

    @Test
    public void cancelDag_plain_cancelNode_usesLocalCancelTask() {
        ProcessDAG dag = DAGTemplates.cancelDag(ComputeResourceType.PLAIN);

        assertEquals("localCancelTask", dag.getNode("cancel").taskBeanName(),
                "PLAIN cancelDag 'cancel' must use 'localCancelTask'");
    }

    // ===========================================================================
    // cancelDag — edges (terminal)
    // ===========================================================================

    @ParameterizedTest(name = "cancelDag({0}) cancel node is terminal")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_cancelNode_isTerminal(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);
        TaskNode cancel = dag.getNode("cancel");

        assertNull(cancel.onSuccess(), "cancel.onSuccess must be null (terminal)");
        assertNull(cancel.onFailure(), "cancel.onFailure must be null (terminal)");
    }

    // ===========================================================================
    // cancelDag — metadata
    // ===========================================================================

    @ParameterizedTest(name = "cancelDag({0}) cancel metadata processState = CANCELED")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_cancelNode_metadataProcessState(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);

        assertEquals("CANCELED",
                dag.getNode("cancel").metadata().get("processState"),
                "cancel node must publish processState=CANCELED");
    }

    // ===========================================================================
    // All DAG templates — valid entry nodes (non-null)
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) entry node is not null")
    @EnumSource(ComputeResourceType.class)
    public void preDag_entryNode_isNotNull(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        assertNotNull(dag.getNode(dag.entryNodeId()),
                "preDag entry node id must resolve to a real node in the graph");
    }

    @ParameterizedTest(name = "postDag({0}) entry node is not null")
    @EnumSource(ComputeResourceType.class)
    public void postDag_entryNode_isNotNull(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        assertNotNull(dag.getNode(dag.entryNodeId()),
                "postDag entry node id must resolve to a real node in the graph");
    }

    @ParameterizedTest(name = "cancelDag({0}) entry node is not null")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_entryNode_isNotNull(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);

        assertNotNull(dag.getNode(dag.entryNodeId()),
                "cancelDag entry node id must resolve to a real node in the graph");
    }

    // ===========================================================================
    // All DAG templates — referenced successor nodes exist in the graph
    // ===========================================================================

    @ParameterizedTest(name = "preDag({0}) all successor node references are resolvable")
    @EnumSource(ComputeResourceType.class)
    public void preDag_allSuccessorReferences_areResolvable(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.preDag(type);

        dag.nodes().values().forEach(node -> {
            if (node.onSuccess() != null) {
                assertNotNull(dag.getNode(node.onSuccess()),
                        "onSuccess ref '" + node.onSuccess() + "' from node '" + node.id() + "' must exist");
            }
            if (node.onFailure() != null) {
                assertNotNull(dag.getNode(node.onFailure()),
                        "onFailure ref '" + node.onFailure() + "' from node '" + node.id() + "' must exist");
            }
        });
    }

    @ParameterizedTest(name = "postDag({0}) all successor node references are resolvable")
    @EnumSource(ComputeResourceType.class)
    public void postDag_allSuccessorReferences_areResolvable(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.postDag(type);

        dag.nodes().values().forEach(node -> {
            if (node.onSuccess() != null) {
                assertNotNull(dag.getNode(node.onSuccess()),
                        "onSuccess ref '" + node.onSuccess() + "' from node '" + node.id() + "' must exist");
            }
            if (node.onFailure() != null) {
                assertNotNull(dag.getNode(node.onFailure()),
                        "onFailure ref '" + node.onFailure() + "' from node '" + node.id() + "' must exist");
            }
        });
    }

    @ParameterizedTest(name = "cancelDag({0}) all successor node references are resolvable")
    @EnumSource(ComputeResourceType.class)
    public void cancelDag_allSuccessorReferences_areResolvable(ComputeResourceType type) {
        ProcessDAG dag = DAGTemplates.cancelDag(type);

        dag.nodes().values().forEach(node -> {
            if (node.onSuccess() != null) {
                assertNotNull(dag.getNode(node.onSuccess()),
                        "onSuccess ref '" + node.onSuccess() + "' from node '" + node.id() + "' must exist");
            }
            if (node.onFailure() != null) {
                assertNotNull(dag.getNode(node.onFailure()),
                        "onFailure ref '" + node.onFailure() + "' from node '" + node.id() + "' must exist");
            }
        });
    }

    // ===========================================================================
    // DAGTemplates — non-instantiability
    // ===========================================================================

    @Test
    public void dagTemplates_hasPrivateConstructor() throws NoSuchMethodException {
        var constructor = DAGTemplates.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null),
                "DAGTemplates must not have a public constructor (utility class)");
    }

    // Workaround: assertFalse is not imported by default — define local helper.
    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
