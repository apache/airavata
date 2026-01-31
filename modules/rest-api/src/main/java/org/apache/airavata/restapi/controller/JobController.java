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

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.utils.DBConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@RestController
@RequestMapping("/api/v1/jobs")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId, @RequestParam(required = false) String taskId) {
        try {
            var jobPK = new JobPK();
            jobPK.setJobId(jobId);
            if (taskId != null) {
                jobPK.setTaskId(taskId);
            }
            var job = jobService.getJob(jobPK);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(job);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createJob(@RequestParam String processId, @RequestBody JobModel job) {
        try {
            var jobId = jobService.addJob(job, processId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("jobId", jobId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJob(
            @PathVariable String jobId, @RequestParam(required = false) String taskId, @RequestBody JobModel job) {
        try {
            var jobPK = new JobPK();
            jobPK.setJobId(jobId);
            if (taskId != null) {
                jobPK.setTaskId(taskId);
            }
            job.setJobId(jobId);
            if (taskId != null) {
                job.setTaskId(taskId);
            }
            var result = jobService.updateJob(job, jobPK);
            return ResponseEntity.ok(Map.of("jobId", result));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getJobs(
            @RequestParam(required = false) String processId,
            @RequestParam(required = false) String taskId,
            @RequestParam(required = false) String jobId) {
        try {
            List<JobModel> jobs;
            if (processId != null) {
                jobs = jobService.getJobList(DBConstants.Job.PROCESS_ID, processId);
            } else if (taskId != null) {
                jobs = jobService.getJobList(DBConstants.Job.TASK_ID, taskId);
            } else if (jobId != null) {
                jobs = jobService.getJobList(DBConstants.Job.JOB_ID, jobId);
            } else {
                return ResponseEntity.badRequest().body("One of processId, taskId, or jobId must be provided");
            }
            return ResponseEntity.ok(jobs);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
