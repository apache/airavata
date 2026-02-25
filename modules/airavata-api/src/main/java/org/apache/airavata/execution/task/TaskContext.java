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
package org.apache.airavata.execution.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.research.application.model.Application;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.compute.resource.model.Resource;
import org.apache.airavata.compute.resource.model.ResourceBinding;
import org.apache.airavata.compute.resource.model.ResourceJobManagerType;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobSubmissionProtocol;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessState;
import org.apache.airavata.execution.model.TaskModel;
import org.apache.airavata.execution.model.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context object carrying resolved state for a single task execution.
 *
 * <p>This is a simplified version of the original TaskContext, updated to work with the new domain
 * model. The old preference/profile resolution logic has been removed; scheduling parameters are now
 * read directly from {@link ProcessModel#getResourceSchedule()} as a plain {@code Map<String,Object>}.
 * Compute and storage resource metadata is carried via {@link Resource} and
 * {@link ResourceBinding} rather than legacy description/preference types.
 *
 * <p>Properties use a lazy-loading pattern: fields remain {@code null} until first accessed.
 */
public class TaskContext {

    private static final Logger logger = LoggerFactory.getLogger(TaskContext.class);

    // Core identifiers
    private String processId;
    private String gatewayId;
    private String taskId;

    // Domain models
    private ProcessModel processModel;
    private ExperimentModel experimentModel;
    private JobModel jobModel;
    private Object subTaskModel = null;

    // Resolved resource and application metadata (new model)
    private Resource computeResource;
    private Application applicationModel;
    private ResourceBinding credentialResourceBinding;

    // Job submission
    private JobSubmissionProtocol jobSubmissionProtocol;

    // Working directories
    private String workingDir;
    private String scratchLocation;
    private String inputDir;
    private String outputDir;
    private String stdoutLocation;
    private String stderrLocation;

    // DAG state — shared mutable state map for passing data between tasks in a DAG
    private final Map<String, String> dagState = new HashMap<>();

    public TaskContext(String processId, String gatewayId, String taskId, ProcessModel processModel) {
        if (processId == null || gatewayId == null || taskId == null || processModel == null) {
            throw new IllegalArgumentException("processId, gatewayId, taskId, and processModel must not be null");
        }
        this.processId = processId;
        this.gatewayId = gatewayId;
        this.taskId = taskId;
        this.processModel = processModel;
    }

    // -------------------------------------------------------------------------
    // Identity accessors
    // -------------------------------------------------------------------------

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExperimentId() {
        return processModel != null ? processModel.getExperimentId() : null;
    }

    // -------------------------------------------------------------------------
    // Domain model accessors
    // -------------------------------------------------------------------------

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
    }

    public ExperimentModel getExperimentModel() {
        return experimentModel;
    }

    public void setExperimentModel(ExperimentModel experimentModel) {
        this.experimentModel = experimentModel;
    }

    public JobModel getJobModel() throws Exception {
        if (jobModel == null) {
            jobModel = new JobModel();
            jobModel.setProcessId(processId);
            jobModel.setWorkingDir(getWorkingDir());
            jobModel.setCreatedAt(IdGenerator.getCurrentTimestamp().toInstant());
        }
        return jobModel;
    }

    public void setJobModel(JobModel jobModel) {
        this.jobModel = jobModel;
    }

    // -------------------------------------------------------------------------
    // New resource / application / binding accessors
    // -------------------------------------------------------------------------

    public Resource getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(Resource computeResource) {
        this.computeResource = computeResource;
    }

    public String getComputeResourceId() {
        return processModel != null ? processModel.getResourceId() : null;
    }

    public Application getApplication() {
        return applicationModel;
    }

    public void setApplication(Application applicationModel) {
        this.applicationModel = applicationModel;
    }

    public ResourceBinding getCredentialResourceBinding() {
        return credentialResourceBinding;
    }

    public void setCredentialResourceBinding(ResourceBinding credentialResourceBinding) {
        this.credentialResourceBinding = credentialResourceBinding;
    }

    public String getComputeResourceLoginUserName() {
        if (credentialResourceBinding != null && credentialResourceBinding.getLoginUsername() != null) {
            return credentialResourceBinding.getLoginUsername();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Scheduling helpers (read from ProcessModel.resourceSchedule map)
    // -------------------------------------------------------------------------

    private String scheduleString(String key) {
        if (processModel == null || processModel.getResourceSchedule() == null) {
            return null;
        }
        Object val = processModel.getResourceSchedule().get(key);
        return val != null ? val.toString() : null;
    }

    public String getQueueName() {
        return scheduleString("queueName");
    }

    public String getAllocationProjectNumber() {
        return scheduleString("allocationProjectNumber");
    }

    public String getReservation() {
        return scheduleString("reservation");
    }

    public String getQualityOfService() {
        return scheduleString("qualityOfService");
    }

    // -------------------------------------------------------------------------
    // Working directory accessors
    // -------------------------------------------------------------------------

    public String getWorkingDir() throws Exception {
        if (workingDir == null) {
            String staticDir = scheduleString("staticWorkingDir");
            if (staticDir != null && !staticDir.trim().isEmpty()) {
                workingDir = staticDir;
            } else {
                String scratch = getScratchLocation();
                workingDir = scratch.endsWith("/") ? scratch + processId : scratch + "/" + processId;
            }
        }
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public String getScratchLocation() throws Exception {
        if (scratchLocation == null) {
            String override = scheduleString("overrideScratchLocation");
            if (isValid(override)) {
                scratchLocation = override;
            } else {
                if (credentialResourceBinding != null && credentialResourceBinding.getMetadata() != null) {
                    Object val = credentialResourceBinding.getMetadata().get("scratchLocation");
                    if (val != null && isValid(val.toString())) {
                        scratchLocation = val.toString();
                    }
                }
            }
            if (scratchLocation == null) {
                throw new RuntimeException("Cannot determine scratch location for process " + processId
                        + ". Set 'scratchLocation' in the credential binding metadata or "
                        + "'overrideScratchLocation' in the resource schedule.");
            }
        }
        return scratchLocation;
    }

    public void setScratchLocation(String scratchLocation) {
        this.scratchLocation = scratchLocation;
    }

    public String getInputDir() throws Exception {
        if (inputDir == null) {
            inputDir = getWorkingDir();
        }
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() throws Exception {
        if (outputDir == null) {
            outputDir = getWorkingDir();
        }
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getStdoutLocation() throws Exception {
        if (stdoutLocation == null) {
            String appName = applicationModel != null ? applicationModel.getName() : "application";
            String dir = getWorkingDir();
            stdoutLocation = (dir.endsWith("/") ? dir : dir + "/") + appName + ".stdout";
        }
        return stdoutLocation;
    }

    public void setStdoutLocation(String stdoutLocation) {
        this.stdoutLocation = stdoutLocation;
    }

    public String getStderrLocation() throws Exception {
        if (stderrLocation == null) {
            String appName = applicationModel != null ? applicationModel.getName() : "application";
            String dir = getWorkingDir();
            stderrLocation = (dir.endsWith("/") ? dir : dir + "/") + appName + ".stderr";
        }
        return stderrLocation;
    }

    public void setStderrLocation(String stderrLocation) {
        this.stderrLocation = stderrLocation;
    }

    // -------------------------------------------------------------------------
    // Job submission accessors
    // -------------------------------------------------------------------------

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public ResourceJobManagerType getJobManagerTypeEnum() {
        if (computeResource != null && computeResource.getCapabilities() != null
                && computeResource.getCapabilities().getCompute() != null) {
            return computeResource.getCapabilities().getCompute().getJobManagerTypeEnum();
        }
        return ResourceJobManagerType.FORK;
    }

    /**
     * @deprecated Storage is now part of Resource capabilities.
     */
    public String getStorageResourceId() {
        return getComputeResourceId();
    }

    // -------------------------------------------------------------------------
    // Process / task state
    // -------------------------------------------------------------------------

    public ProcessState getProcessState() {
        if (processModel != null
                && processModel.getProcessStatuses() != null
                && !processModel.getProcessStatuses().isEmpty()) {
            return processModel.getProcessStatuses().get(0).getState();
        }
        return null;
    }

    public void setProcessStatus(StatusModel<ProcessState> status) {
        if (status != null) {
            logger.info(
                    "expId: {}, processId: {} :- Process status changed {} -> {}",
                    getExperimentId(),
                    processId,
                    getProcessState() != null ? getProcessState().name() : "(none)",
                    status.getState().name());
            List<StatusModel<ProcessState>> processStatuses = new ArrayList<>();
            processStatuses.add(status);
            processModel.setProcessStatuses(processStatuses);
        }
    }

    public StatusModel<ProcessState> getProcessStatus() {
        if (processModel != null
                && processModel.getProcessStatuses() != null
                && !processModel.getProcessStatuses().isEmpty()) {
            return processModel.getProcessStatuses().get(0);
        }
        return null;
    }

    public TaskState getTaskState() {
        TaskModel current = getCurrentTaskModel();
        if (current != null
                && current.getTaskStatuses() != null
                && !current.getTaskStatuses().isEmpty()) {
            return current.getTaskStatuses().get(0).getState();
        }
        return null;
    }

    public StatusModel<TaskState> getTaskStatus() {
        TaskModel current = getCurrentTaskModel();
        if (current != null
                && current.getTaskStatuses() != null
                && !current.getTaskStatuses().isEmpty()) {
            return current.getTaskStatuses().get(0);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Task model accessor
    // -------------------------------------------------------------------------

    public TaskModel getCurrentTaskModel() {
        return null;
    }

    public Object getSubTaskModel() throws TaskFailureException {
        try {
            if (subTaskModel == null) {
                subTaskModel = getCurrentTaskModel();
            }
            return subTaskModel;
        } catch (Exception e) {
            logger.error("Error while getting sub task model", e);
            throw new TaskFailureException("Error while getting sub task model", true, e);
        }
    }

    public String getComputeResourceCredentialToken() {
        return scheduleString("credentialToken");
    }

    public String getInputStorageResourceId() {
        return scheduleString("inputStorageResourceId");
    }

    public String getOutputStorageResourceId() {
        return scheduleString("outputStorageResourceId");
    }

    // -------------------------------------------------------------------------
    // DAG state
    // -------------------------------------------------------------------------

    /**
     * Returns the mutable DAG state map shared across all tasks in a DAG execution.
     * Tasks can read values set by predecessors and write values for successors.
     */
    public Map<String, String> getDagState() {
        return dagState;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private boolean isValid(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
