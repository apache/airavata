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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.JobStatusService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@org.springframework.transaction.annotation.Transactional("expCatalogTransactionManager")
public class JobStatusRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;
    private String taskId;
    private JobPK jobPK;

    public JobStatusRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            TaskService taskService,
            JobService jobService,
            JobStatusService jobStatusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processId = processService.addProcess(processModel, experimentId);

        TaskModel taskModel = new TaskModel();
        taskModel.setTaskType(TaskTypes.JOB_SUBMISSION);
        taskModel.setParentProcessId(processId);
        taskId = taskService.addTask(taskModel, processId);
        assertNotNull(taskId, "Task ID should not be null");

        JobModel jobModel = new JobModel();
        jobModel.setJobId("test-job-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job");
        String jobId = jobService.addJob(jobModel, processId);
        assertNotNull(jobId, "Job ID should not be null");

        jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);
    }

    @Test
    public void testJobStatusRepository_Create_MultipleStatuses() throws RegistryException, InterruptedException {

        JobStatus status1 = new JobStatus(JobState.SUBMITTED);
        status1.setReason("Job submitted to queue");
        jobStatusService.addJobStatus(status1, jobPK);

        JobStatus status2 = new JobStatus(JobState.QUEUED);
        status2.setReason("Job queued for execution");
        jobStatusService.addJobStatus(status2, jobPK);

        JobStatus status3 = new JobStatus(JobState.ACTIVE);
        status3.setReason("Job is now active");
        jobStatusService.addJobStatus(status3, jobPK);

        JobModel job = jobService.getJob(jobPK);
        assertNotNull(job.getJobStatuses(), "Job statuses should not be null");
        assertTrue(job.getJobStatuses().size() >= 3, "Job should have at least 3 status entries");

        // latest status
        JobStatus latestStatus = jobStatusService.getJobStatus(jobPK);
        assertNotNull(latestStatus, "Latest status should not be null");
        assertEquals(JobState.ACTIVE, latestStatus.getJobState(), "Latest status should be ACTIVE");
    }

    @Test
    public void testJobStatusRepository_StateTransitions() throws RegistryException, InterruptedException {
        // a complete state transition flow
        JobStatus submitted = new JobStatus(JobState.SUBMITTED);
        submitted.setReason("Initial submission");
        jobStatusService.addJobStatus(submitted, jobPK);

        JobStatus queued = new JobStatus(JobState.QUEUED);
        queued.setReason("Queued for resources");
        jobStatusService.addJobStatus(queued, jobPK);

        JobStatus active = new JobStatus(JobState.ACTIVE);
        active.setReason("Job is running");
        jobStatusService.addJobStatus(active, jobPK);

        JobStatus complete = new JobStatus(JobState.COMPLETE);
        complete.setReason("Job completed successfully");
        jobStatusService.addJobStatus(complete, jobPK);

        JobStatus latest = jobStatusService.getJobStatus(jobPK);
        assertNotNull(latest, "Latest status should exist");
        assertEquals(JobState.COMPLETE, latest.getJobState(), "Final state should be COMPLETE");
        assertEquals("Job completed successfully", latest.getReason(), "Reason should match");

        // failure transition - use a separate job to avoid interference
        JobModel failedJobModel = new JobModel();
        failedJobModel.setJobId("failed-job-" + java.util.UUID.randomUUID().toString());
        failedJobModel.setTaskId(taskId);
        failedJobModel.setJobDescription("Failed job test");
        String failedJobId = jobService.addJob(failedJobModel, processId);

        JobPK failedJobPK = new JobPK();
        failedJobPK.setJobId(failedJobId);
        failedJobPK.setTaskId(taskId);

        JobStatus failedSubmitted = new JobStatus(JobState.SUBMITTED);
        jobStatusService.addJobStatus(failedSubmitted, failedJobPK);

        JobStatus failed = new JobStatus(JobState.FAILED);
        failed.setReason("Job execution failed");
        jobStatusService.addJobStatus(failed, failedJobPK);

        // the job has the failed status
        JobModel failedJob = jobService.getJob(failedJobPK);
        assertNotNull(failedJob.getJobStatuses(), "Failed job should have statuses");
        assertTrue(failedJob.getJobStatuses().size() >= 2, "Failed job should have at least 2 statuses");

        JobStatus failedStatus = jobStatusService.getJobStatus(failedJobPK);
        assertNotNull(failedStatus, "Failed job status should exist");
        assertEquals(JobState.FAILED, failedStatus.getJobState(), "Failed job state should be FAILED");
    }

    @Test
    public void testJobStatusRepository_Get_NonExistentJob() throws RegistryException {
        JobPK nonExistentPK = new JobPK();
        nonExistentPK.setJobId("non-existent-job-" + java.util.UUID.randomUUID().toString());
        nonExistentPK.setTaskId(
                "non-existent-task-" + java.util.UUID.randomUUID().toString());

        JobStatus status = jobStatusService.getJobStatus(nonExistentPK);
        assertNull(status, "Non-existent job should return null status");

        JobStatus existingStatus = new JobStatus(JobState.SUBMITTED);
        existingStatus.setReason("Existing job status");
        jobStatusService.addJobStatus(existingStatus, jobPK);

        JobStatus retrieved = jobStatusService.getJobStatus(jobPK);
        assertNotNull(retrieved, "Existing job should have status");
        assertEquals(JobState.SUBMITTED, retrieved.getJobState(), "Status should match");
    }

    @Test
    public void testJobStatusRepository_Update_AllStatusFields() throws RegistryException, InterruptedException {
        JobStatus status = new JobStatus(JobState.QUEUED);
        status.setReason("Initial queued state");
        jobStatusService.addJobStatus(status, jobPK);

        // Ensure updated status has a later timestamp
        JobStatus updatedStatus = new JobStatus(JobState.ACTIVE);
        updatedStatus.setReason("Updated: Job is now active");
        // Don't set timestamp - let updateJobStatus use getUniqueTimestampForJob to ensure proper ordering
        jobStatusService.updateJobStatus(updatedStatus, jobPK);

        JobStatus retrieved = jobStatusService.getJobStatus(jobPK);
        assertNotNull(retrieved, "Updated status should exist");
        assertEquals(JobState.ACTIVE, retrieved.getJobState(), "State should be updated to ACTIVE");
        assertEquals("Updated: Job is now active", retrieved.getReason(), "Reason should be updated");
        assertTrue(retrieved.getTimeOfStateChange() > 0, "Time of state change should be set");
    }

    @Test
    public void testJobStatusRepository_UpdateAddsNewStatusEntry() throws RegistryException {
        JobStatus initialStatus = new JobStatus(JobState.SUBMITTED);
        initialStatus.setReason("Initial submission");
        jobStatusService.addJobStatus(initialStatus, jobPK);

        JobModel jobBeforeUpdate = jobService.getJob(jobPK);
        assertNotNull(jobBeforeUpdate.getJobStatuses(), "Job statuses should not be null");
        int statusCountBefore = jobBeforeUpdate.getJobStatuses().size();
        assertTrue(statusCountBefore >= 1, "Should have at least 1 status before update");

        JobStatus updatedStatus = new JobStatus(JobState.ACTIVE);
        updatedStatus.setReason("Updated to active");
        jobStatusService.updateJobStatus(updatedStatus, jobPK);

        JobStatus latest = jobStatusService.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Latest status should be ACTIVE");
        assertEquals("Updated to active", latest.getReason(), "Latest reason should match");

        JobModel jobAfterUpdate = jobService.getJob(jobPK);
        assertNotNull(jobAfterUpdate.getJobStatuses(), "Job statuses should not be null");
        assertTrue(
                jobAfterUpdate.getJobStatuses().size() > statusCountBefore,
                "Update should add a new status entry, not replace existing");

        List<JobStatus> allStatuses = jobAfterUpdate.getJobStatuses();
        assertTrue(
                allStatuses.stream().anyMatch(s -> s.getJobState() == JobState.SUBMITTED),
                "Original SUBMITTED status should still exist in history");
        assertTrue(
                allStatuses.stream().anyMatch(s -> s.getJobState() == JobState.ACTIVE),
                "New ACTIVE status should exist in history");
    }

    @Test
    public void testJobStatusRepository_TimeOfStateChangeHandling() throws RegistryException {
        // Capture time after status creation to account for time set in addJobStatus
        JobStatus status = new JobStatus(JobState.SUBMITTED);
        status.setReason("Test time handling");

        long beforeTime = AiravataUtils.getUniqueTimestamp().getTime();
        jobStatusService.addJobStatus(status, jobPK);
        long afterTime = AiravataUtils.getUniqueTimestamp().getTime();

        JobStatus retrieved = jobStatusService.getJobStatus(jobPK);
        assertNotNull(retrieved, "Status should exist");
        assertTrue(retrieved.getTimeOfStateChange() > 0, "Time should be set");
        // Allow small timing differences (within 1 second) due to timestamp conversion and processing
        assertTrue(
                retrieved.getTimeOfStateChange() >= beforeTime - 1000,
                "Time should be set to current or later (expected >= " + beforeTime + ", actual: " + retrieved.getTimeOfStateChange() + ")");
        assertTrue(
                retrieved.getTimeOfStateChange() <= afterTime + 1000,
                "Time should be set to current or earlier (expected <= " + afterTime + ", actual: " + retrieved.getTimeOfStateChange() + ")");

        long explicitTime = AiravataUtils.getUniqueTimestamp().getTime() + 1000;
        JobStatus updated = new JobStatus(JobState.ACTIVE);
        updated.setTimeOfStateChange(explicitTime);
        jobStatusService.updateJobStatus(updated, jobPK);

        JobStatus updatedRetrieved = jobStatusService.getJobStatus(jobPK);
        // Allow small timing differences due to timestamp conversion and processing
        assertTrue(
                updatedRetrieved.getTimeOfStateChange() >= explicitTime - 100,
                "Updated time should be set correctly (expected >= " + explicitTime + ", actual: " + updatedRetrieved.getTimeOfStateChange() + ")");
    }

    @Test
    public void testJobStatusRepository_StatusOrdering() throws RegistryException, InterruptedException {

        JobStatus status1 = new JobStatus(JobState.SUBMITTED);
        jobStatusService.addJobStatus(status1, jobPK);

        JobStatus status2 = new JobStatus(JobState.QUEUED);
        jobStatusService.addJobStatus(status2, jobPK);

        JobStatus status3 = new JobStatus(JobState.ACTIVE);
        jobStatusService.addJobStatus(status3, jobPK);

        JobModel job = jobService.getJob(jobPK);
        List<JobStatus> statuses = job.getJobStatuses();
        assertNotNull(statuses, "Statuses list should not be null");
        assertTrue(statuses.size() >= 3, "Should have at least 3 statuses");

        JobStatus latest = jobStatusService.getJobStatus(jobPK);
        assertNotNull(latest, "Latest status should exist");
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Latest status should be ACTIVE");
    }

    @Test
    public void testJobStatusRepository_AutomaticStatusIdGeneration() throws RegistryException {
        JobStatus status = new JobStatus(JobState.SUBMITTED);
        status.setReason("Testing automatic status ID generation");

        jobStatusService.addJobStatus(status, jobPK);

        JobStatus retrieved = jobStatusService.getJobStatus(jobPK);
        assertNotNull(retrieved, "Status should exist");
        assertNotNull(retrieved.getStatusId(), "Status ID should be automatically generated");
        assertFalse(retrieved.getStatusId().isEmpty(), "Status ID should not be empty");
        assertTrue(retrieved.getStatusId().startsWith("JOB_STATE"), "Status ID should start with 'JOB_STATE' prefix");
    }

    @Test
    public void testJobStatusRepository_StatusHistoryCompleteness() throws RegistryException, InterruptedException {
        JobStatus status1 = new JobStatus(JobState.SUBMITTED);
        status1.setReason("Job submitted");
        jobStatusService.addJobStatus(status1, jobPK);

        JobStatus status2 = new JobStatus(JobState.QUEUED);
        status2.setReason("Job queued");
        jobStatusService.addJobStatus(status2, jobPK);

        JobStatus status3 = new JobStatus(JobState.ACTIVE);
        status3.setReason("Job active");
        jobStatusService.addJobStatus(status3, jobPK);

        JobModel job = jobService.getJob(jobPK);
        List<JobStatus> statuses = job.getJobStatuses();
        assertNotNull(statuses, "Statuses list should not be null");
        assertTrue(statuses.size() >= 3, "Should have at least 3 statuses in history");

        assertTrue(
                statuses.stream().anyMatch(s -> s.getJobState() == JobState.SUBMITTED),
                "SUBMITTED status should be in history");
        assertTrue(
                statuses.stream().anyMatch(s -> s.getJobState() == JobState.QUEUED),
                "QUEUED status should be in history");
        assertTrue(
                statuses.stream().anyMatch(s -> s.getJobState() == JobState.ACTIVE),
                "ACTIVE status should be in history");

        JobStatus latest = jobStatusService.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Latest status should be ACTIVE");
        assertTrue(latest.getTimeOfStateChange() > 0, "Latest status should have timestamp");

        statuses.forEach(s -> {
            assertTrue(
                    s.getTimeOfStateChange() > 0, "Each status should have timeOfStateChange set: " + s.getJobState());
            assertNotNull(s.getStatusId(), "Each status should have statusId: " + s.getJobState());
        });
    }

    @Test
    public void testJobStatusRepository_AllJobStates() throws RegistryException {
        // all possible job states
        JobState[] allStates = JobState.values();
        JobPK[] jobPKs = new JobPK[allStates.length];

        for (int i = 0; i < allStates.length; i++) {
            jobPKs[i] = createNewJob("job-state-" + allStates[i].name());
            JobStatus status = new JobStatus(allStates[i]);
            status.setReason("Testing state: " + allStates[i].name());
            jobStatusService.addJobStatus(status, jobPKs[i]);

            JobStatus retrieved = jobStatusService.getJobStatus(jobPKs[i]);
            assertNotNull(retrieved, "Status for " + allStates[i] + " should exist");
            assertEquals(allStates[i], retrieved.getJobState(), "State should match for " + allStates[i]);
        }
    }

    @Test
    public void testJobStatusRepository_RapidStatusUpdates() throws RegistryException, InterruptedException {
        JobStatus status1 = new JobStatus(JobState.SUBMITTED);
        status1.setReason("Rapid update 1");
        jobStatusService.addJobStatus(status1, jobPK);

        JobStatus status2 = new JobStatus(JobState.QUEUED);
        status2.setReason("Rapid update 2");
        jobStatusService.addJobStatus(status2, jobPK);

        JobStatus status3 = new JobStatus(JobState.ACTIVE);
        status3.setReason("Rapid update 3");
        jobStatusService.addJobStatus(status3, jobPK);

        JobModel job = jobService.getJob(jobPK);
        assertNotNull(job.getJobStatuses(), "Job statuses should not be null");
        assertTrue(job.getJobStatuses().size() >= 3, "All rapid status updates should be recorded without loss");

        JobStatus latest = jobStatusService.getJobStatus(jobPK);
        assertEquals(JobState.ACTIVE, latest.getJobState(), "Latest status should reflect the most recent update");
        assertEquals("Rapid update 3", latest.getReason(), "Latest reason should match the most recent update");

        List<JobStatus> statuses = job.getJobStatuses();
        // Verify strict timestamp ordering
        JobStatus s1 = statuses.stream().filter(s -> s.getJobState() == JobState.SUBMITTED).findFirst().orElse(null);
        JobStatus s2 = statuses.stream().filter(s -> s.getJobState() == JobState.QUEUED).findFirst().orElse(null);
        JobStatus s3 = statuses.stream().filter(s -> s.getJobState() == JobState.ACTIVE).findFirst().orElse(null);

        assertNotNull(s1);
        assertNotNull(s2);
        assertNotNull(s3);

        assertTrue(s2.getTimeOfStateChange() > s1.getTimeOfStateChange(),
                "Status 2 timestamp (" + s2.getTimeOfStateChange() + ") should be greater than Status 1 (" + s1.getTimeOfStateChange() + ")");
        assertTrue(s3.getTimeOfStateChange() > s2.getTimeOfStateChange(),
                "Status 3 timestamp (" + s3.getTimeOfStateChange() + ") should be greater than Status 2 (" + s2.getTimeOfStateChange() + ")");
    }

    private JobPK createNewJob(String jobIdPrefix) throws RegistryException {
        JobModel jobModel = new JobModel();
        jobModel.setJobId(jobIdPrefix + "-" + java.util.UUID.randomUUID().toString());
        jobModel.setTaskId(taskId);
        jobModel.setJobDescription("Test job for " + jobIdPrefix);
        String jobId = jobService.addJob(jobModel, processId);

        JobPK jobPK = new JobPK();
        jobPK.setJobId(jobId);
        jobPK.setTaskId(taskId);
        return jobPK;
    }
}
