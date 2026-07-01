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
import org.apache.airavata.api.experiment.ExperimentSummaryWithAccess;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.DataProductInterface;
import org.apache.airavata.interfaces.DataReplicaLocationInterface;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSearchFields;
import org.apache.airavata.model.experiment.proto.ExperimentStatistics;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
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
import org.mockito.ArgumentCaptor;
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
    ComputeRegistry computeRegistry;

    @Mock
    DataProductInterface dataProductInterface;

    @Mock
    DataReplicaLocationInterface dataReplicaLocationInterface;

    @Mock
    SharingFacade sharingHandler;

    @Mock
    ProjectService projectService;

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
                experimentRegistry,
                appCatalogRegistry,
                projectRegistry,
                computeRegistry,
                dataProductInterface,
                dataReplicaLocationInterface,
                sharingHandler,
                projectService,
                java.util.Optional.empty());
        ctx = new RequestContext(
                "testUser",
                "testGateway",
                "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"),
                java.util.List.of("admin-rw"));
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
    void createExperimentWithAccess_ownerGetsOwnerAndWriteFlags() throws Exception {
        // Sharing is enabled in this class (see setUp). The caller owns what it just created, so the
        // flags come from ownership and no sharing WRITE check is needed.
        ExperimentModel experiment = ExperimentModel.newBuilder()
                .setExperimentName("test-exp")
                .setGatewayId("testGateway")
                .setUserName("testUser")
                .setProjectId("proj-1")
                .build();

        when(experimentRegistry.createExperiment("testGateway", experiment)).thenReturn("exp-123");
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(experiment);

        ExperimentWithAccess result = experimentService.createExperimentWithAccess(ctx, experiment);

        assertEquals("test-exp", result.getExperiment().getExperimentName());
        assertTrue(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
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
    void updateExperimentWithAccess_ownerGetsOwnerAndWriteFlags() throws Exception {
        // Sharing is enabled in this class (see setUp). The caller owns the experiment, so the update
        // is allowed by ownership and the returned flags are owner+write by ownership.
        ExperimentModel existing = ExperimentModel.newBuilder()
                .setUserName("testUser")
                .setGatewayId("testGateway")
                .build();
        ExperimentModel updated = ExperimentModel.newBuilder()
                .setExperimentName("new-name")
                .setProjectId("proj-1")
                .build();

        // updateExperiment reads the existing row for its WRITE check; getExperimentWithAccess then
        // re-reads it for the returned model + flags.
        when(experimentRegistry.getExperiment("exp-123")).thenReturn(existing);
        doNothing().when(experimentRegistry).updateExperiment("exp-123", updated);

        ExperimentWithAccess result = experimentService.updateExperimentWithAccess(ctx, "exp-123", updated);

        assertEquals("testUser", result.getExperiment().getUserName());
        assertTrue(result.getAccess().getIsOwner());
        assertTrue(result.getAccess().getUserHasWriteAccess());
        verify(experimentRegistry).updateExperiment("exp-123", updated);
    }

    @Test
    void searchExperimentsWithAccess_stampsCallerFlags() throws Exception {
        // Sharing is enabled in this test class (see setUp). The caller owns exp-owned (owner+write
        // by ownership, no sharing call), and has no sharing grant on the other user's exp-other, so
        // it is neither owner nor writable.
        ExperimentSummaryModel owned = ExperimentSummaryModel.newBuilder()
                .setExperimentId("exp-owned")
                .setUserName("testUser")
                .setGatewayId("testGateway")
                .build();
        ExperimentSummaryModel other = ExperimentSummaryModel.newBuilder()
                .setExperimentId("exp-other")
                .setUserName("otherUser")
                .setGatewayId("testGateway")
                .build();

        // The caller has no WRITE/OWNER sharing grant on the other user's experiment.
        when(sharingHandler.userHasAccess(anyString(), anyString(), eq("exp-other"), anyString()))
                .thenReturn(false);
        when(sharingHandler.searchEntityIds(anyString(), anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(List.of("exp-owned", "exp-other"));
        when(experimentRegistry.searchExperiments(
                        eq("testGateway"), eq("testUser"), anyList(), anyMap(), eq(10), eq(0)))
                .thenReturn(List.of(owned, other));

        List<ExperimentSummaryWithAccess> results = experimentService.searchExperimentsWithAccess(
                ctx, "testGateway", "testUser", Map.<ExperimentSearchFields, String>of(), 10, 0);

        assertEquals(2, results.size());

        ExperimentSummaryWithAccess ownedResult = results.get(0);
        assertEquals("exp-owned", ownedResult.getSummary().getExperimentId());
        assertTrue(ownedResult.getAccess().getIsOwner());
        assertTrue(ownedResult.getAccess().getUserHasWriteAccess());

        ExperimentSummaryWithAccess otherResult = results.get(1);
        assertEquals("exp-other", otherResult.getSummary().getExperimentId());
        assertFalse(otherResult.getAccess().getIsOwner());
        assertFalse(otherResult.getAccess().getUserHasWriteAccess());
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
    void fetchIntermediateOutputs_isNotSupported() throws Exception {
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

        assertThrows(
                ServiceException.class,
                () -> experimentService.fetchIntermediateOutputs(ctx, "exp-123", List.of("output1")));
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

    private ExperimentModel experimentWithEmail(boolean enabled) {
        return ExperimentModel.newBuilder()
                .setExperimentName("e")
                .setGatewayId("testGateway")
                .setUserName("testUser")
                .setEnableEmailNotification(enabled)
                .addEmailAddresses("old@example.com")
                .build();
    }

    @Test
    void launchExperimentWithStorageSetup_overridesRecipientsWhenEnabled() throws Exception {
        when(experimentRegistry.getExperiment("exp-1")).thenReturn(experimentWithEmail(true));
        ExperimentService spy = spy(experimentService);
        doNothing().when(spy).launchExperiment(ctx, "exp-1", "testGateway");

        spy.launchExperimentWithStorageSetup(ctx, "exp-1", "testGateway", "me@example.com");

        ArgumentCaptor<ExperimentModel> captor = ArgumentCaptor.forClass(ExperimentModel.class);
        verify(experimentRegistry).updateExperiment(eq("exp-1"), captor.capture());
        assertEquals(List.of("me@example.com"), captor.getValue().getEmailAddressesList());
        verify(spy).launchExperiment(ctx, "exp-1", "testGateway");
    }

    @Test
    void launchExperimentWithStorageSetup_noOverrideWhenNotificationsDisabled() throws Exception {
        when(experimentRegistry.getExperiment("exp-1")).thenReturn(experimentWithEmail(false));
        ExperimentService spy = spy(experimentService);
        doNothing().when(spy).launchExperiment(ctx, "exp-1", "testGateway");

        spy.launchExperimentWithStorageSetup(ctx, "exp-1", "testGateway", "me@example.com");

        verify(experimentRegistry, never()).updateExperiment(anyString(), any());
        verify(spy).launchExperiment(ctx, "exp-1", "testGateway");
    }

    @Test
    void launchExperimentWithStorageSetup_noOverrideWhenEmailBlank() throws Exception {
        when(experimentRegistry.getExperiment("exp-1")).thenReturn(experimentWithEmail(true));
        ExperimentService spy = spy(experimentService);
        doNothing().when(spy).launchExperiment(ctx, "exp-1", "testGateway");

        spy.launchExperimentWithStorageSetup(ctx, "exp-1", "testGateway", "");

        verify(experimentRegistry, never()).updateExperiment(anyString(), any());
        verify(spy).launchExperiment(ctx, "exp-1", "testGateway");
    }
}
