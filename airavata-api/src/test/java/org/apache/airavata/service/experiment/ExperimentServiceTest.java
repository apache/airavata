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
package org.apache.airavata.execution.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Map;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.messaging.service.EventPublisher;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobState;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.sharing.handler.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    @Mock
    SharingRegistryServerHandler sharingHandler;

    @Mock
    EventPublisher eventPublisher;

    ExperimentService experimentService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService(registryHandler, sharingHandler, eventPublisher);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createExperiment_returnsExperimentId() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentName("test-exp");
        experiment.setGatewayId("testGateway");
        experiment.setUserName("testUser");
        experiment.setProjectId("proj-1");

        when(registryHandler.createExperiment("testGateway", experiment)).thenReturn("exp-123");

        String result = experimentService.createExperiment(ctx, experiment);

        assertEquals("exp-123", result);
        verify(registryHandler).createExperiment("testGateway", experiment);
    }

    @Test
    void getExperiment_ownerGetsAccess() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");

        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        ExperimentModel result = experimentService.getExperiment(ctx, "exp-123");

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());
    }

    @Test
    void deleteExperiment_onlyDeletesCreatedExperiments() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.CREATED);
        experiment.addToExperimentStatus(status);
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        when(registryHandler.deleteExperiment("exp-123")).thenReturn(true);
        boolean result = experimentService.deleteExperiment(ctx, "exp-123");
        assertTrue(result);
        verify(registryHandler).deleteExperiment("exp-123");
    }

    @Test
    void deleteExperiment_rejectsNonCreatedExperiment() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.EXECUTING);
        experiment.addToExperimentStatus(status);
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceException.class, () -> experimentService.deleteExperiment(ctx, "exp-123"));
    }

    @Test
    void getExperimentByAdmin_allowsSameGateway() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("otherUser");
        experiment.setGatewayId("testGateway");
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        ExperimentModel result = experimentService.getExperimentByAdmin(ctx, "exp-123");
        assertNotNull(result);
    }

    @Test
    void getExperimentByAdmin_rejectsDifferentGateway() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("otherUser");
        experiment.setGatewayId("otherGateway");
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceAuthorizationException.class, () -> experimentService.getExperimentByAdmin(ctx, "exp-123"));
    }

    @Test
    void getExperimentStatus_delegatesToRegistry() throws Exception {
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.COMPLETED);
        when(registryHandler.getExperimentStatus("exp-123")).thenReturn(status);
        ExperimentStatus result = experimentService.getExperimentStatus(ctx, "exp-123");
        assertEquals(ExperimentState.COMPLETED, result.getState());
    }

    @Test
    void getExperimentOutputs_delegatesToRegistry() throws Exception {
        List<OutputDataObjectType> outputs = List.of(new OutputDataObjectType());
        when(registryHandler.getExperimentOutputs("exp-123")).thenReturn(outputs);
        List<OutputDataObjectType> result = experimentService.getExperimentOutputs(ctx, "exp-123");
        assertEquals(1, result.size());
    }

    @Test
    void getExperimentStatistics_delegatesToRegistry() throws Exception {
        ExperimentStatistics stats = new ExperimentStatistics();
        stats.setAllExperimentCount(5);
        when(registryHandler.getExperimentStatistics("testGateway", 1000L, 2000L, null, null, null, null, 10, 0))
                .thenReturn(stats);
        ExperimentStatistics result =
                experimentService.getExperimentStatistics(ctx, "testGateway", 1000L, 2000L, null, null, null, 10, 0);
        assertEquals(5, result.getAllExperimentCount());
    }

    @Test
    void updateExperiment_ownerCanUpdate() throws Exception {
        ExperimentModel existing = new ExperimentModel();
        existing.setUserName("testUser");
        existing.setGatewayId("testGateway");
        ExperimentModel updated = new ExperimentModel();
        updated.setExperimentName("new-name");
        updated.setProjectId("proj-1");

        when(registryHandler.getExperiment("exp-123")).thenReturn(existing);
        doNothing().when(registryHandler).updateExperiment("exp-123", updated);

        // Should not throw — owner has implicit WRITE
        assertDoesNotThrow(() -> experimentService.updateExperiment(ctx, "exp-123", updated));
        verify(registryHandler).updateExperiment("exp-123", updated);
    }

    @Test
    void getJobStatuses_delegatesToRegistry() throws Exception {
        Map<String, JobStatus> statuses = Map.of("job-1", new JobStatus());
        when(registryHandler.getJobStatuses("exp-123")).thenReturn(statuses);
        Map<String, JobStatus> result = experimentService.getJobStatuses(ctx, "exp-123");
        assertEquals(1, result.size());
    }

    @Test
    void validateExperiment_returnsTrue() throws Exception {
        boolean result = experimentService.validateExperiment(ctx, "exp-123");
        assertTrue(result);
    }

    @Test
    void fetchIntermediateOutputs_throwsWhenNoAccess() throws Exception {
        // User doesn't have owner or write access
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:WRITE"))
                .thenReturn(false);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> experimentService.fetchIntermediateOutputs(ctx, "exp-123", List.of("output1")));
    }

    @Test
    void fetchIntermediateOutputs_throwsWhenNoActiveJob() throws Exception {
        // User has write access
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        experiment.setProcesses(new java.util.ArrayList<>());
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        // No active jobs
        JobModel job = new JobModel();
        JobStatus jobStatus = new JobStatus(JobState.COMPLETE);
        job.addToJobStatuses(jobStatus);
        when(registryHandler.getJobDetails("exp-123")).thenReturn(List.of(job));

        assertThrows(
                ServiceException.class,
                () -> experimentService.fetchIntermediateOutputs(ctx, "exp-123", List.of("output1")));
    }

    @Test
    void fetchIntermediateOutputs_publishesEventWhenValid() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        experiment.setProcesses(new java.util.ArrayList<>()); // no in-progress output fetch processes
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        JobModel job = new JobModel();
        JobStatus jobStatus = new JobStatus(JobState.ACTIVE);
        job.addToJobStatuses(jobStatus);
        when(registryHandler.getJobDetails("exp-123")).thenReturn(List.of(job));

        experimentService.fetchIntermediateOutputs(ctx, "exp-123", List.of("output1"));

        verify(eventPublisher).publishIntermediateOutputs("exp-123", "testGateway", List.of("output1"));
    }

    @Test
    void getIntermediateOutputProcessStatus_throwsWhenNoAccess() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(false);
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:READ"))
                .thenReturn(false);

        assertThrows(
                ServiceAuthorizationException.class,
                () -> experimentService.getIntermediateOutputProcessStatus(ctx, "exp-123", List.of("output1")));
    }

    @Test
    void getIntermediateOutputProcessStatus_returnsLatestStatus() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");

        // Build a process with OUTPUT_FETCHING task and a matching output
        ProcessModel process = new ProcessModel();
        process.setLastUpdateTime(1000L);
        TaskModel task = new TaskModel();
        task.setTaskType(TaskTypes.OUTPUT_FETCHING);
        process.addToTasks(task);
        OutputDataObjectType out = new OutputDataObjectType();
        out.setName("output1");
        process.addToProcessOutputs(out);
        ProcessStatus ps = new ProcessStatus(ProcessState.EXECUTING);
        process.addToProcessStatuses(ps);
        experiment.addToProcesses(process);

        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        ProcessStatus result = experimentService.getIntermediateOutputProcessStatus(ctx, "exp-123", List.of("output1"));

        assertEquals(ProcessState.EXECUTING, result.getState());
    }
}
