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
package org.apache.airavata.restapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.model.ProcessState;

import org.apache.airavata.execution.service.ProcessService;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.status.service.StatusService;
import org.apache.airavata.core.util.DBConstants;
import org.apache.airavata.restapi.exception.InvalidRequestException;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Job REST API. Job role engulfed by Process: jobId = processId.
 * Uses ProcessService for persistence.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs")
public class JobController {
    private static final String METADATA_PARENT_PROCESS_ID = "parentProcessId";

    private final ProcessService processService;
    private final StatusService statusService;

    public JobController(ProcessService processService, StatusService statusService) {
        this.processService = processService;
        this.statusService = statusService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId, @RequestParam(required = false) String taskId)
            throws RegistryException {
        ProcessModel process = processService.getProcess(jobId);
        if (process == null) {
            throw new ResourceNotFoundException("Job", jobId);
        }
        JobModel job = processToJobModel(process);
        return ResponseEntity.ok(job);
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestParam String processId, @RequestBody JobModel job) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of("error", "Job API deprecated; use Process API with processType=JOB_SUBMISSION"));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJob(
            @PathVariable String jobId, @RequestParam(required = false) String taskId, @RequestBody JobModel job)
            throws RegistryException {
        ProcessModel process = processService.getProcess(jobId);
        if (process == null) {
            throw new ResourceNotFoundException("Job", jobId);
        }
        if (job.getJobStatuses() != null && !job.getJobStatuses().isEmpty()) {
            for (StatusModel<JobState> js : job.getJobStatuses()) {
                statusService.addJobStatus(js, jobId);
            }
        }
        return ResponseEntity.ok(Map.of("jobId", jobId));
    }

    @GetMapping
    public ResponseEntity<?> getJobs(
            @RequestParam(required = false) String processId,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String jobId)
            throws RegistryException {
        List<JobModel> jobs = new ArrayList<>();
        if (jobId != null) {
            ProcessModel p = processService.getProcess(jobId);
            if (p != null) jobs.add(processToJobModel(p));
        } else if (processId != null || taskId != null) {
            String parentId = processId != null ? processId : taskId;
            ProcessModel parent = processService.getProcess(parentId);
            if (parent != null) {
                List<ProcessModel> processes =
                        processService.getProcessList(DBConstants.Process.EXPERIMENT_ID, parent.getExperimentId());
                if (processes != null) {
                    for (ProcessModel p : processes) {
                        String metaParent = getParentProcessIdFromMetadata(p.getProcessMetadata());
                        if (parentId.equals(metaParent)
                                && "JOB_SUBMISSION".equals(p.getProcessType())) {
                            jobs.add(processToJobModel(p));
                        }
                    }
                }
            }
        } else {
            throw new InvalidRequestException("One of processId, taskId, or jobId must be provided");
        }
        return ResponseEntity.ok(jobs);
    }

    private static JobModel processToJobModel(ProcessModel process) {
        JobModel job = new JobModel();
        job.setJobId(process.getProcessId());
        job.setProcessId(getParentProcessIdFromMetadata(process.getProcessMetadata()));
        job.setCreationTime(process.getCreationTime());
        if (process.getProcessStatuses() != null) {
            List<StatusModel<JobState>> list = new ArrayList<>();
            for (StatusModel<ProcessState> ps : process.getProcessStatuses()) {
                StatusModel<JobState> js = new StatusModel<>();
                js.setState(processStateToJobState(ps.getState()));
                js.setTimeOfStateChange(ps.getTimeOfStateChange());
                js.setReason(ps.getReason());
                js.setStatusId(ps.getStatusId());
                list.add(js);
            }
            job.setJobStatuses(list);
        }
        return job;
    }

    private static String getParentProcessIdFromMetadata(Map<String, Object> metadata) {
        if (metadata == null) return null;
        Object v = metadata.get(METADATA_PARENT_PROCESS_ID);
        return v != null ? v.toString() : null;
    }

    private static JobState processStateToJobState(ProcessState ps) {
        if (ps == null) return JobState.SUBMITTED;
        return switch (ps) {
            case CREATED -> JobState.SUBMITTED;
            case VALIDATED -> JobState.SUBMITTED;
            case LAUNCHED -> JobState.ACTIVE;
            case PRE_PROCESSING -> JobState.ACTIVE;
            case CONFIGURING_WORKSPACE -> JobState.ACTIVE;
            case OUTPUT_DATA_STAGING -> JobState.ACTIVE;
            case EXECUTING -> JobState.ACTIVE;
            case MONITORING -> JobState.ACTIVE;
            case COMPLETED -> JobState.COMPLETED;
            case FAILED -> JobState.FAILED;
            case CANCELED, CANCELING -> JobState.CANCELED;
            default -> JobState.SUBMITTED;
        };
    }
}
