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
package org.apache.airavata.research.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.messaging.service.EventPublisher;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentStatistics;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.JobState;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessState;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.model.task.proto.TaskModel;
import org.apache.airavata.model.task.proto.TaskTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExperimentServiceTest {

    @Mock
    ExperimentRegistry experimentRegistry;

    @Mock
    AppCatalogRegistry appCatalogRegistry;

    @Mock
    ProjectRegistry projectRegistry;

    @Mock
    SharingFacade sharingHandler;

    @Mock
    EventPublisher eventPublisher;

    ExperimentService experimentService;
    RequestContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        // Sharing is enabled via airavata-server.properties on the classpath.
        // Configure the sharing mock to allow all access checks and entity operations.
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(sharingHandler.updateEntityMetadata(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        experimentService = new ExperimentService(
                experimentRegistry, appCatalogRegistry, projectRegistry, sharingHandler, eventPublisher);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createExperiment_returnsExperimentId() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setExperimentName("test-exp").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        experiment = experiment.toBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setProjectId("proj-1").build();

        when(experimentRegistry.createExperiment("testGateway", experiment)).thenReturn("exp-123");

        String result = experimentService.createExperiment(ctx, experiment);

        assertEquals("exp-123", result);
        verify(experimentRegistry).createExperiment("testGateway", experiment);
    }

    @Test
    void getExperiment_ownerGetsAccess() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();

        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);

        ExperimentModel result = experimentService.getExperiment(ctx, "exp-123");

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());
    }

    @Test
    void deleteExperiment_onlyDeletesCreatedExperiments() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        ExperimentStatus status = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_CREATED)
                .build();
        experiment = experiment.toBuilder().addExperimentStatus(status).build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);
        when(experimentRegistry.deleteExperiment("exp-123")).thenReturn(true);
        boolean result = experimentService.deleteExperiment(ctx, "exp-123");
        assertTrue(result);
        verify(experimentRegistry).deleteExperiment("exp-123");
    }

    @Test
    void deleteExperiment_rejectsNonCreatedExperiment() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        ExperimentStatus status = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_EXECUTING)
                .build();
        experiment = experiment.toBuilder().addExperimentStatus(status).build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceException.class, () -> experimentService.deleteExperiment(ctx, "exp-123"));
    }

    @Test
    void getExperimentByAdmin_allowsSameGateway() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("otherUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);
        ExperimentModel result = experimentService.getExperimentByAdmin(ctx, "exp-123");
        assertNotNull(result);
    }

    @Test
    void getExperimentByAdmin_rejectsDifferentGateway() throws Exception {
        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("otherUser").build();
        experiment = experiment.toBuilder().setGatewayId("otherGateway").build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceAuthorizationException.class, () -> experimentService.getExperimentByAdmin(ctx, "exp-123"));
    }

    @Test
    void getExperimentStatus_delegatesToRegistry() throws Exception {
        ExperimentStatus status = ExperimentStatus.newBuilder()
                .setState(ExperimentState.EXPERIMENT_STATE_COMPLETED)
                .build();
        when(experimentRegistry.getExperimentStatus("exp-123")).thenReturn(status);
        ExperimentStatus result = experimentService.getExperimentStatus(ctx, "exp-123");
        assertEquals(ExperimentState.EXPERIMENT_STATE_COMPLETED, result.getState());
    }

    @Test
    void getExperimentOutputs_delegatesToRegistry() throws Exception {
        List<OutputDataObjectType> outputs = List.of(OutputDataObjectType.getDefaultInstance());
        when(experimentRegistry.getExperimentOutputs("exp-123")).thenReturn(outputs);
        List<OutputDataObjectType> result = experimentService.getExperimentOutputs(ctx, "exp-123");
        assertEquals(1, result.size());
    }

    @Test
    void getExperimentStatistics_delegatesToRegistry() throws Exception {
        ExperimentStatistics stats =
                ExperimentStatistics.newBuilder().setAllExperimentCount(5).build();
        when(experimentRegistry.getExperimentStatistics("testGateway", 1000L, 2000L, null, null, null, null, 10, 0))
                .thenReturn(stats);
        ExperimentStatistics result =
                experimentService.getExperimentStatistics(ctx, "testGateway", 1000L, 2000L, null, null, null, 10, 0);
        assertEquals(5, result.getAllExperimentCount());
    }

    @Test
    void updateExperiment_ownerCanUpdate() throws Exception {
        ExperimentModel existing =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        existing = existing.toBuilder().setGatewayId("testGateway").build();
        ExperimentModel updated = ExperimentModel.newBuilder()
                .setExperimentName("new-name")
                .setProjectId("proj-1")
                .build();

        when(experimentRegistry.getExperiment("exp-123")).thenReturn(existing);
        doNothing().when(experimentRegistry).updateExperiment("exp-123", updated);

        // Should not throw — owner has implicit WRITE
        assertDoesNotThrow(() -> experimentService.updateExperiment(ctx, "exp-123", updated));
        verify(experimentRegistry).updateExperiment("exp-123", updated);
    }

    @Test
    void getJobStatuses_delegatesToRegistry() throws Exception {
        Map<String, JobStatus> statuses = Map.of("job-1", JobStatus.getDefaultInstance());
        when(experimentRegistry.getJobStatuses("exp-123")).thenReturn(statuses);
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

        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);

        // No active jobs
        JobModel job = JobModel.getDefaultInstance();
        JobStatus jobStatus =
                JobStatus.newBuilder().setJobState(JobState.COMPLETE).build();
        job = job.toBuilder().addJobStatuses(jobStatus).build();
        when(experimentRegistry.getJobDetails("exp-123")).thenReturn(List.of(job));

        assertThrows(
                ServiceException.class,
                () -> experimentService.fetchIntermediateOutputs(ctx, "exp-123", List.of("output1")));
    }

    @Test
    void fetchIntermediateOutputs_publishesEventWhenValid() throws Exception {
        when(sharingHandler.userHasAccess("testGateway", "testUser@testGateway", "exp-123", "testGateway:OWNER"))
                .thenReturn(true);

        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);

        JobModel job = JobModel.getDefaultInstance();
        JobStatus jobStatus =
                JobStatus.newBuilder().setJobState(JobState.ACTIVE).build();
        job = job.toBuilder().addJobStatuses(jobStatus).build();
        when(experimentRegistry.getJobDetails("exp-123")).thenReturn(List.of(job));

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

        ExperimentModel experiment =
                ExperimentModel.newBuilder().setUserName("testUser").build();
        experiment = experiment.toBuilder().setGatewayId("testGateway").build();

        // Build a process with OUTPUT_FETCHING task and a matching output
        ProcessModel process =
                ProcessModel.newBuilder().setLastUpdateTime(1000L).build();
        TaskModel task =
                TaskModel.newBuilder().setTaskType(TaskTypes.OUTPUT_FETCHING).build();
        process = process.toBuilder().addTasks(task).build();
        OutputDataObjectType out =
                OutputDataObjectType.newBuilder().setName("output1").build();
        process = process.toBuilder().addProcessOutputs(out).build();
        ProcessStatus ps = ProcessStatus.newBuilder()
                .setState(ProcessState.PROCESS_STATE_EXECUTING)
                .build();
        process = process.toBuilder().addProcessStatuses(ps).build();
        experiment = experiment.toBuilder().addProcesses(process).build();

        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);

        ProcessStatus result = experimentService.getIntermediateOutputProcessStatus(ctx, "exp-123", List.of("output1"));

        assertEquals(ProcessState.PROCESS_STATE_EXECUTING, result.getState());
    }
}
