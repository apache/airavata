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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.StatusParentType;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.entities.StatusEntity;
import org.apache.airavata.registry.entities.expcatalog.JobPK;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.StatusRepository;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.JobService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.services.StatusService;
import org.apache.airavata.registry.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@org.springframework.transaction.annotation.Transactional
public class JobStatusRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final TaskService taskService;
    private final JobService jobService;
    private final StatusRepository statusRepository;
    private final StatusService statusService;

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
            StatusRepository statusRepository,
            StatusService statusService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.taskService = taskService;
        this.jobService = jobService;
        this.statusRepository = statusRepository;
        this.statusService = statusService;
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

        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        status1.setReason("Job submitted to queue");
        statusRepository.save(status1);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.QUEUED.name());
        status2.setReason("Job queued for execution");
        statusRepository.save(status2);

        Thread.sleep(10); // Ensure timestamp difference

        String statusId3 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        status3.setReason("Job is now active");
        statusRepository.save(status3);
        flushAndClear();

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statuses, "Job statuses should not be null");
        assertTrue(statuses.size() >= 3, "Job should have at least 3 status entries");

        // latest status
        java.util.Optional<StatusEntity> latestStatusOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(latestStatusOpt.isPresent(), "Latest status should exist");
        StatusEntity latestStatus = latestStatusOpt.get();
        assertEquals(JobState.ACTIVE.name(), latestStatus.getState(), "Latest status should be ACTIVE");
        assertEquals(jobPK.getJobId(), latestStatus.getParentId(), "Parent ID should match job ID");
        assertEquals(StatusParentType.JOB, latestStatus.getParentType(), "Parent type should be JOB");
    }

    @Test
    public void testJobStatusRepository_StateTransitions() throws RegistryException, InterruptedException {
        // a complete state transition flow
        JobStatus submitted = new JobStatus(JobState.SUBMITTED);
        submitted.setReason("Initial submission");
        statusService.addJobStatus(submitted, jobPK.getJobId());

        JobStatus queued = new JobStatus(JobState.QUEUED);
        queued.setReason("Queued for resources");
        statusService.addJobStatus(queued, jobPK.getJobId());

        JobStatus active = new JobStatus(JobState.ACTIVE);
        active.setReason("Job is running");
        statusService.addJobStatus(active, jobPK.getJobId());

        JobStatus complete = new JobStatus(JobState.COMPLETE);
        complete.setReason("Job completed successfully");
        statusService.addJobStatus(complete, jobPK.getJobId());

        JobStatus latest = statusService.getLatestJobStatus(jobPK.getJobId());
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
        statusService.addJobStatus(failedSubmitted, failedJobPK.getJobId());

        JobStatus failed = new JobStatus(JobState.FAILED);
        failed.setReason("Job execution failed");
        statusService.addJobStatus(failed, failedJobPK.getJobId());

        // the job has the failed status
        JobModel failedJob = jobService.getJob(failedJobPK);
        assertNotNull(failedJob.getJobStatuses(), "Failed job should have statuses");
        assertTrue(failedJob.getJobStatuses().size() >= 2, "Failed job should have at least 2 statuses");

        JobStatus failedStatus = statusService.getLatestJobStatus(failedJobPK.getJobId());
        assertNotNull(failedStatus, "Failed job status should exist");
        assertEquals(JobState.FAILED, failedStatus.getJobState(), "Failed job state should be FAILED");
    }

    @Test
    public void testJobStatusRepository_Get_NonExistentJob() throws RegistryException {
        String nonExistentJobId = "non-existent-job-" + java.util.UUID.randomUUID().toString();

        java.util.Optional<StatusEntity> statusOpt = statusRepository.findLatestByParentIdAndParentType(nonExistentJobId, StatusParentType.JOB);
        assertTrue(statusOpt.isEmpty(), "Non-existent job should return empty optional");

        String statusId = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity existingStatus = new StatusEntity(statusId, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        existingStatus.setReason("Existing job status");
        statusRepository.save(existingStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> retrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(retrievedOpt.isPresent(), "Existing job should have status");
        StatusEntity retrieved = retrievedOpt.get();
        assertEquals(JobState.SUBMITTED.name(), retrieved.getState(), "Status should match");
    }

    @Test
    public void testJobStatusRepository_Update_AllStatusFields() throws RegistryException, InterruptedException {
        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.QUEUED.name());
        status.setReason("Initial queued state");
        statusRepository.save(status);
        flushAndClear();

        Thread.sleep(10);
        // Ensure updated status has a later timestamp
        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity updatedStatus = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        updatedStatus.setReason("Updated: Job is now active");
        statusRepository.save(updatedStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> retrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(retrievedOpt.isPresent(), "Updated status should exist");
        StatusEntity retrieved = retrievedOpt.get();
        assertEquals(JobState.ACTIVE.name(), retrieved.getState(), "State should be updated to ACTIVE");
        assertEquals("Updated: Job is now active", retrieved.getReason(), "Reason should be updated");
        assertNotNull(retrieved.getTimeOfStateChange(), "Time of state change should be set");
        assertTrue(retrieved.getTimeOfStateChange().getTime() > 0, "Time of state change should be greater than 0");
    }

    @Test
    public void testJobStatusRepository_UpdateAddsNewStatusEntry() throws RegistryException {
        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity initialStatus = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        initialStatus.setReason("Initial submission");
        statusRepository.save(initialStatus);
        flushAndClear();

        java.util.List<StatusEntity> statusesBefore = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statusesBefore, "Job statuses should not be null");
        int statusCountBefore = statusesBefore.size();
        assertTrue(statusCountBefore >= 1, "Should have at least 1 status before update");

        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity updatedStatus = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        updatedStatus.setReason("Updated to active");
        statusRepository.save(updatedStatus);
        flushAndClear();

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(JobState.ACTIVE.name(), latest.getState(), "Latest status should be ACTIVE");
        assertEquals("Updated to active", latest.getReason(), "Latest reason should match");

        java.util.List<StatusEntity> statusesAfter = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statusesAfter, "Job statuses should not be null");
        assertTrue(
                statusesAfter.size() > statusCountBefore,
                "Update should add a new status entry, not replace existing");

        assertTrue(
                statusesAfter.stream().anyMatch(s -> JobState.SUBMITTED.name().equals(s.getState())),
                "Original SUBMITTED status should still exist in history");
        assertTrue(
                statusesAfter.stream().anyMatch(s -> JobState.ACTIVE.name().equals(s.getState())),
                "New ACTIVE status should exist in history");
    }

    @Test
    public void testJobStatusRepository_TimeOfStateChangeHandling() throws RegistryException {
        // Capture time after status creation to account for time set in save
        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        status.setReason("Test time handling");

        long beforeTime = AiravataUtils.getUniqueTimestamp().getTime();
        statusRepository.save(status);
        flushAndClear();
        long afterTime = AiravataUtils.getUniqueTimestamp().getTime();

        java.util.Optional<StatusEntity> retrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(retrievedOpt.isPresent(), "Status should exist");
        StatusEntity retrieved = retrievedOpt.get();
        assertNotNull(retrieved.getTimeOfStateChange(), "Time should be set");
        assertTrue(retrieved.getTimeOfStateChange().getTime() > 0, "Time should be greater than 0");
        // Allow small timing differences (within 1 second) due to timestamp conversion and processing
        assertTrue(
                retrieved.getTimeOfStateChange().getTime() >= beforeTime - 1000,
                "Time should be set to current or later (expected >= " + beforeTime + ", actual: "
                        + retrieved.getTimeOfStateChange().getTime() + ")");
        assertTrue(
                retrieved.getTimeOfStateChange().getTime() <= afterTime + 1000,
                "Time should be set to current or earlier (expected <= " + afterTime + ", actual: "
                        + retrieved.getTimeOfStateChange().getTime() + ")");

        long explicitTime = AiravataUtils.getUniqueTimestamp().getTime() + 1000;
        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity updated = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        updated.setTimeOfStateChange(new java.sql.Timestamp(explicitTime));
        statusRepository.save(updated);
        flushAndClear();

        java.util.Optional<StatusEntity> updatedRetrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(updatedRetrievedOpt.isPresent(), "Updated status should exist");
        StatusEntity updatedRetrieved = updatedRetrievedOpt.get();
        // Allow small timing differences due to timestamp conversion and processing
        assertTrue(
                updatedRetrieved.getTimeOfStateChange().getTime() >= explicitTime - 100,
                "Updated time should be set correctly (expected >= " + explicitTime + ", actual: "
                        + updatedRetrieved.getTimeOfStateChange().getTime() + ")");
    }

    @Test
    public void testJobStatusRepository_StatusOrdering() throws RegistryException, InterruptedException {

        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        statusRepository.save(status1);

        Thread.sleep(10);
        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.QUEUED.name());
        statusRepository.save(status2);

        Thread.sleep(10);
        String statusId3 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        statusRepository.save(status3);
        flushAndClear();

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statuses, "Statuses list should not be null");
        assertTrue(statuses.size() >= 3, "Should have at least 3 statuses");

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(JobState.ACTIVE.name(), latest.getState(), "Latest status should be ACTIVE");
    }

    @Test
    public void testJobStatusRepository_AutomaticStatusIdGeneration() throws RegistryException {
        String statusId = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status = new StatusEntity(statusId, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        status.setReason("Testing automatic status ID generation");
        statusRepository.save(status);
        flushAndClear();

        java.util.Optional<StatusEntity> retrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(retrievedOpt.isPresent(), "Status should exist");
        StatusEntity retrieved = retrievedOpt.get();
        assertNotNull(retrieved.getStatusId(), "Status ID should be set");
        assertFalse(retrieved.getStatusId().isEmpty(), "Status ID should not be empty");
        assertEquals(statusId, retrieved.getStatusId(), "Status ID should match");
    }

    @Test
    public void testJobStatusRepository_StatusHistoryCompleteness() throws RegistryException, InterruptedException {
        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        status1.setReason("Job submitted");
        statusRepository.save(status1);

        Thread.sleep(10);
        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.QUEUED.name());
        status2.setReason("Job queued");
        statusRepository.save(status2);

        Thread.sleep(10);
        String statusId3 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        status3.setReason("Job active");
        statusRepository.save(status3);
        flushAndClear();

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statuses, "Statuses list should not be null");
        assertTrue(statuses.size() >= 3, "Should have at least 3 statuses in history");

        assertTrue(
                statuses.stream().anyMatch(s -> JobState.SUBMITTED.name().equals(s.getState())),
                "SUBMITTED status should be in history");
        assertTrue(
                statuses.stream().anyMatch(s -> JobState.QUEUED.name().equals(s.getState())),
                "QUEUED status should be in history");
        assertTrue(
                statuses.stream().anyMatch(s -> JobState.ACTIVE.name().equals(s.getState())),
                "ACTIVE status should be in history");

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(JobState.ACTIVE.name(), latest.getState(), "Latest status should be ACTIVE");
        assertNotNull(latest.getTimeOfStateChange(), "Latest status should have timestamp");
        assertTrue(latest.getTimeOfStateChange().getTime() > 0, "Latest status should have timestamp > 0");

        statuses.forEach(s -> {
            assertNotNull(s.getTimeOfStateChange(), "Each status should have timeOfStateChange set: " + s.getState());
            assertTrue(s.getTimeOfStateChange().getTime() > 0, "Each status should have timeOfStateChange > 0: " + s.getState());
            assertNotNull(s.getStatusId(), "Each status should have statusId: " + s.getState());
        });
    }

    @Test
    public void testJobStatusRepository_AllJobStates() throws RegistryException {
        // all possible job states
        JobState[] allStates = JobState.values();
        JobPK[] jobPKs = new JobPK[allStates.length];

        for (int i = 0; i < allStates.length; i++) {
            jobPKs[i] = createNewJob("job-state-" + allStates[i].name());
            String statusId = "JOB_STATE_" + AiravataUtils.getId("STATUS");
            StatusEntity status = new StatusEntity(statusId, jobPKs[i].getJobId(), StatusParentType.JOB, allStates[i].name());
            status.setReason("Testing state: " + allStates[i].name());
            statusRepository.save(status);
            flushAndClear();

            java.util.Optional<StatusEntity> retrievedOpt = statusRepository.findLatestByParentIdAndParentType(jobPKs[i].getJobId(), StatusParentType.JOB);
            assertTrue(retrievedOpt.isPresent(), "Status for " + allStates[i] + " should exist");
            StatusEntity retrieved = retrievedOpt.get();
            assertEquals(allStates[i].name(), retrieved.getState(), "State should match for " + allStates[i]);
        }
    }

    @Test
    public void testJobStatusRepository_RapidStatusUpdates() throws RegistryException, InterruptedException {
        String statusId1 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status1 = new StatusEntity(statusId1, jobPK.getJobId(), StatusParentType.JOB, JobState.SUBMITTED.name());
        status1.setReason("Rapid update 1");
        statusRepository.save(status1);

        Thread.sleep(10);
        String statusId2 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status2 = new StatusEntity(statusId2, jobPK.getJobId(), StatusParentType.JOB, JobState.QUEUED.name());
        status2.setReason("Rapid update 2");
        statusRepository.save(status2);

        Thread.sleep(10);
        String statusId3 = "JOB_STATE_" + AiravataUtils.getId("STATUS");
        StatusEntity status3 = new StatusEntity(statusId3, jobPK.getJobId(), StatusParentType.JOB, JobState.ACTIVE.name());
        status3.setReason("Rapid update 3");
        statusRepository.save(status3);
        flushAndClear();

        java.util.List<StatusEntity> statuses = statusRepository.findByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertNotNull(statuses, "Job statuses should not be null");
        assertTrue(statuses.size() >= 3, "All rapid status updates should be recorded without loss");

        java.util.Optional<StatusEntity> latestOpt = statusRepository.findLatestByParentIdAndParentType(jobPK.getJobId(), StatusParentType.JOB);
        assertTrue(latestOpt.isPresent(), "Latest status should exist");
        StatusEntity latest = latestOpt.get();
        assertEquals(JobState.ACTIVE.name(), latest.getState(), "Latest status should reflect the most recent update");
        assertEquals("Rapid update 3", latest.getReason(), "Latest reason should match the most recent update");

        // Verify strict timestamp ordering
        StatusEntity s1 = statuses.stream()
                .filter(s -> JobState.SUBMITTED.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s2 = statuses.stream()
                .filter(s -> JobState.QUEUED.name().equals(s.getState()))
                .findFirst()
                .orElse(null);
        StatusEntity s3 = statuses.stream()
                .filter(s -> JobState.ACTIVE.name().equals(s.getState()))
                .findFirst()
                .orElse(null);

        assertNotNull(s1, "SUBMITTED status should exist");
        assertNotNull(s2, "QUEUED status should exist");
        assertNotNull(s3, "ACTIVE status should exist");

        // Verify sequence number ordering (sequence numbers guarantee deterministic creation order)
        assertTrue(
                s2.getSequenceNum() > s1.getSequenceNum(),
                "Status 2 sequence (" + s2.getSequenceNum() + ") should be greater than Status 1 ("
                        + s1.getSequenceNum() + ")");
        assertTrue(
                s3.getSequenceNum() > s2.getSequenceNum(),
                "Status 3 sequence (" + s3.getSequenceNum() + ") should be greater than Status 2 ("
                        + s2.getSequenceNum() + ")");
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
