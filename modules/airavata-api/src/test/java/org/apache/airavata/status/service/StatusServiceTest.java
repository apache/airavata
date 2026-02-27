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
package org.apache.airavata.status.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.config.TestBase;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.core.model.TaskState;
import org.apache.airavata.status.model.ErrorModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for {@link StatusService}.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class StatusServiceTest extends TestBase {

    private final StatusService statusService;
    private String testProcessId;

    public StatusServiceTest(StatusService statusService) {
        this.statusService = statusService;
    }

    @BeforeEach
    void setUp() {
        testProcessId = "process-" + java.util.UUID.randomUUID();
    }

    // ========== addProcessStatus / getLatestProcessStatus ==========

    @Test
    void addProcessStatus_persistsAndReturnsEventId() throws Exception {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.CREATED);

        String eventId = statusService.addProcessStatus(status, testProcessId);

        assertNotNull(eventId);
        assertFalse(eventId.isBlank());
    }

    @Test
    void getLatestProcessStatus_returnsLatest() throws Exception {
        statusService.addProcessStatus(StatusModel.of(ProcessState.CREATED), testProcessId);
        statusService.addProcessStatus(StatusModel.of(ProcessState.EXECUTING), testProcessId);
        statusService.addProcessStatus(StatusModel.of(ProcessState.COMPLETED, "finished"), testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);

        assertNotNull(latest);
        assertEquals(ProcessState.COMPLETED, latest.getState());
        assertEquals("finished", latest.getReason());
    }

    @Test
    void getLatestProcessStatus_noStatusExists_returnsNull() throws Exception {
        StatusModel<ProcessState> latest =
                statusService.getLatestProcessStatus("nonexistent-" + java.util.UUID.randomUUID());

        assertNull(latest);
    }

    @Test
    void addProcessStatus_sequenceNumberIncreases() throws Exception {
        String eventId1 = statusService.addProcessStatus(StatusModel.of(ProcessState.CREATED), testProcessId);
        String eventId2 = statusService.addProcessStatus(StatusModel.of(ProcessState.EXECUTING), testProcessId);

        assertNotNull(eventId1);
        assertNotNull(eventId2);
        assertNotEquals(eventId1, eventId2);

        // Verify the latest is the second one
        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.EXECUTING, latest.getState());
    }

    // ========== addTaskStatus (engulfed as process status) ==========

    @Test
    void addTaskStatus_convertsToProcessStatus() throws Exception {
        StatusModel<TaskState> taskStatus = StatusModel.of(TaskState.EXECUTING);

        String eventId = statusService.addTaskStatus(taskStatus, testProcessId);
        assertNotNull(eventId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertNotNull(latest);
        assertEquals(ProcessState.EXECUTING, latest.getState());
    }

    @Test
    void addTaskStatus_completedTaskMapsToCompletedProcess() throws Exception {
        StatusModel<TaskState> taskStatus = StatusModel.of(TaskState.COMPLETED);

        statusService.addTaskStatus(taskStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.COMPLETED, latest.getState());
    }

    @Test
    void addTaskStatus_failedTaskMapsToFailedProcess() throws Exception {
        StatusModel<TaskState> taskStatus = StatusModel.of(TaskState.FAILED);

        statusService.addTaskStatus(taskStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.FAILED, latest.getState());
    }

    // ========== addJobStatus (engulfed as process status) ==========

    @Test
    void addJobStatus_submittedMapsToLaunched() throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(JobState.SUBMITTED);

        statusService.addJobStatus(jobStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.LAUNCHED, latest.getState());
    }

    @Test
    void addJobStatus_activeMapsToExecuting() throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(JobState.ACTIVE);

        statusService.addJobStatus(jobStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.EXECUTING, latest.getState());
    }

    @Test
    void addJobStatus_queuedMapsToQueued() throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(JobState.QUEUED);

        statusService.addJobStatus(jobStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.QUEUED, latest.getState());
    }

    @Test
    void addJobStatus_suspendedMapsToMonitoring() throws Exception {
        StatusModel<JobState> jobStatus = StatusModel.of(JobState.SUSPENDED);

        statusService.addJobStatus(jobStatus, testProcessId);

        StatusModel<ProcessState> latest = statusService.getLatestProcessStatus(testProcessId);
        assertEquals(ProcessState.MONITORING, latest.getState());
    }

    // ========== addProcessError ==========

    @Test
    void addProcessError_persistsAndReturnsId() throws Exception {
        ErrorModel error = new ErrorModel();
        error.setActualErrorMessage("NullPointerException at line 42");
        error.setUserFriendlyMessage("An unexpected error occurred");
        error.setTransientError(false);

        String errorId = statusService.addProcessError(error, testProcessId);

        assertNotNull(errorId);
        assertFalse(errorId.isBlank());
    }

    @Test
    void addProcessError_withRootCauseIds() throws Exception {
        ErrorModel error = new ErrorModel();
        error.setActualErrorMessage("Chained exception");
        error.setUserFriendlyMessage("Something went wrong");
        error.setRootCauseErrorIdList(List.of("cause-1", "cause-2"));

        String errorId = statusService.addProcessError(error, testProcessId);

        assertNotNull(errorId);
    }

    // ========== addTaskError (engulfed) ==========

    @Test
    void addTaskError_persistsAndReturnsId() throws Exception {
        ErrorModel error = new ErrorModel();
        error.setActualErrorMessage("Task failed");
        error.setUserFriendlyMessage("The task could not be completed");

        String errorId = statusService.addTaskError(error, testProcessId);

        assertNotNull(errorId);
        assertFalse(errorId.isBlank());
    }
}
