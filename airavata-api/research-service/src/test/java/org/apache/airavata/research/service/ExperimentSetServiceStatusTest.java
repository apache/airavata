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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.airavata.api.experimentset.AggregateState;
import org.apache.airavata.api.experimentset.ExperimentSet;
import org.apache.airavata.api.experimentset.ExperimentSetStatus;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.process.proto.ProcessModel;
import org.apache.airavata.model.status.proto.ExperimentState;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.research.model.ExperimentSetEntity;
import org.apache.airavata.research.model.ExperimentSetMemberEntity;
import org.apache.airavata.research.repository.ExperimentSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExperimentSetServiceStatusTest {

    @Mock
    ExperimentService experimentService;

    @Mock
    ExperimentSetRepository experimentSetRepository;

    ExperimentSetService experimentSetService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        experimentSetService = new ExperimentSetService(experimentService, experimentSetRepository);
        ctx = new RequestContext(
                "testUser",
                "testGateway",
                "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"),
                List.of("admin-rw"));
    }

    // ---- helpers ----

    private ExperimentSetEntity makeEntity(String setId, String... experimentIds) {
        ExperimentSetEntity entity = new ExperimentSetEntity();
        entity.setId(setId);
        entity.setSetName("test-set");
        entity.setOwner("testUser");
        entity.setGatewayId("testGateway");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        List<ExperimentSetMemberEntity> members = new ArrayList<>();
        for (int i = 0; i < experimentIds.length; i++) {
            ExperimentSetMemberEntity m = new ExperimentSetMemberEntity();
            m.setExperimentId(experimentIds[i]);
            m.setOrdinal(i);
            m.setExperimentSet(entity);
            members.add(m);
        }
        entity.setMembers(members);
        return entity;
    }

    private ExperimentModel makeExperiment(String id, ExperimentState state) {
        return ExperimentModel.newBuilder()
                .setExperimentId(id)
                .setUserName("testUser")
                .setGatewayId("testGateway")
                .addExperimentStatus(ExperimentStatus.newBuilder()
                        .setState(state)
                        .setTimeOfStateChange(System.currentTimeMillis())
                        .build())
                .build();
    }

    private ExperimentModel makeExperimentWithProcess(String id, ExperimentState state, String processId) {
        return ExperimentModel.newBuilder()
                .setExperimentId(id)
                .setUserName("testUser")
                .setGatewayId("testGateway")
                .addExperimentStatus(ExperimentStatus.newBuilder()
                        .setState(state)
                        .setTimeOfStateChange(System.currentTimeMillis())
                        .build())
                .addProcesses(ProcessModel.newBuilder().setProcessId(processId).build())
                .build();
    }

    // ---- aggregate helper unit tests ----

    @Test
    void aggregate_empty_returnsQueued() {
        assertEquals(AggregateState.QUEUED, ExperimentSetService.aggregate(List.of()));
    }

    @Test
    void aggregate_allCompleted_returnsCompleted() {
        assertEquals(
                AggregateState.COMPLETED,
                ExperimentSetService.aggregate(List.of(
                        "EXPERIMENT_STATE_COMPLETED",
                        "EXPERIMENT_STATE_COMPLETED",
                        "EXPERIMENT_STATE_COMPLETED")));
    }

    @Test
    void aggregate_allTerminalFailure_returnsFailed() {
        assertEquals(
                AggregateState.FAILED,
                ExperimentSetService.aggregate(List.of(
                        "EXPERIMENT_STATE_FAILED",
                        "EXPERIMENT_STATE_CANCELED",
                        "EXPERIMENT_STATE_FAILED")));
    }

    @Test
    void aggregate_mixedExecutingAndCreated_returnsRunning() {
        assertEquals(
                AggregateState.RUNNING,
                ExperimentSetService.aggregate(List.of(
                        "EXPERIMENT_STATE_EXECUTING",
                        "EXPERIMENT_STATE_CREATED",
                        "EXPERIMENT_STATE_LAUNCHED")));
    }

    @Test
    void aggregate_allCreated_returnsQueued() {
        assertEquals(
                AggregateState.QUEUED,
                ExperimentSetService.aggregate(List.of(
                        "EXPERIMENT_STATE_CREATED",
                        "EXPERIMENT_STATE_VALIDATED",
                        "EXPERIMENT_STATE_CREATED")));
    }

    @Test
    void aggregate_mixedCompletedAndFailed_returnsMixed() {
        assertEquals(
                AggregateState.MIXED,
                ExperimentSetService.aggregate(List.of(
                        "EXPERIMENT_STATE_COMPLETED",
                        "EXPERIMENT_STATE_COMPLETED",
                        "EXPERIMENT_STATE_EXECUTING",
                        "EXPERIMENT_STATE_FAILED")));
    }

    // ---- getExperimentSetStatus tests ----

    @Test
    void getExperimentSetStatus_mixedStates_correctCountsAndMixed() throws Exception {
        // 4 experiments: COMPLETED, COMPLETED, EXECUTING, FAILED → MIXED
        ExperimentSetEntity entity = makeEntity("set-1", "e1", "e2", "e3", "e4");
        when(experimentSetRepository.findById("set-1")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(makeExperiment("e1", ExperimentState.EXPERIMENT_STATE_COMPLETED));
        when(experimentService.getExperiment(ctx, "e2")).thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_COMPLETED));
        when(experimentService.getExperiment(ctx, "e3")).thenReturn(makeExperiment("e3", ExperimentState.EXPERIMENT_STATE_EXECUTING));
        when(experimentService.getExperiment(ctx, "e4")).thenReturn(makeExperiment("e4", ExperimentState.EXPERIMENT_STATE_FAILED));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-1");

        assertEquals(4, status.getTotal());
        assertEquals(AggregateState.MIXED, status.getAggregate());
        assertEquals(2, status.getCountsByStateOrDefault("EXPERIMENT_STATE_COMPLETED", 0));
        assertEquals(1, status.getCountsByStateOrDefault("EXPERIMENT_STATE_EXECUTING", 0));
        assertEquals(1, status.getCountsByStateOrDefault("EXPERIMENT_STATE_FAILED", 0));
        assertEquals(4, status.getItemsCount());
    }

    @Test
    void getExperimentSetStatus_allCompleted_returnsCompleted() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-2", "e1", "e2");
        when(experimentSetRepository.findById("set-2")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(makeExperiment("e1", ExperimentState.EXPERIMENT_STATE_COMPLETED));
        when(experimentService.getExperiment(ctx, "e2")).thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_COMPLETED));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-2");

        assertEquals(AggregateState.COMPLETED, status.getAggregate());
        assertEquals(2, status.getCountsByStateOrDefault("EXPERIMENT_STATE_COMPLETED", 0));
    }

    @Test
    void getExperimentSetStatus_allFailedOrCanceled_returnsFailed() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-3", "e1", "e2");
        when(experimentSetRepository.findById("set-3")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(makeExperiment("e1", ExperimentState.EXPERIMENT_STATE_FAILED));
        when(experimentService.getExperiment(ctx, "e2")).thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_CANCELED));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-3");

        assertEquals(AggregateState.FAILED, status.getAggregate());
    }

    @Test
    void getExperimentSetStatus_executingAndCreated_returnsRunning() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-4", "e1", "e2");
        when(experimentSetRepository.findById("set-4")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(makeExperiment("e1", ExperimentState.EXPERIMENT_STATE_EXECUTING));
        when(experimentService.getExperiment(ctx, "e2")).thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_CREATED));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-4");

        assertEquals(AggregateState.RUNNING, status.getAggregate());
    }

    @Test
    void getExperimentSetStatus_allCreated_returnsQueued() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-5", "e1", "e2");
        when(experimentSetRepository.findById("set-5")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(makeExperiment("e1", ExperimentState.EXPERIMENT_STATE_CREATED));
        when(experimentService.getExperiment(ctx, "e2")).thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_CREATED));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-5");

        assertEquals(AggregateState.QUEUED, status.getAggregate());
    }

    @Test
    void getExperimentSetStatus_includesProcessId_whenProcessPresent() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-6", "e1");
        when(experimentSetRepository.findById("set-6")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1"))
                .thenReturn(makeExperimentWithProcess("e1", ExperimentState.EXPERIMENT_STATE_EXECUTING, "proc-42"));

        ExperimentSetStatus status = experimentSetService.getExperimentSetStatus(ctx, "set-6");

        assertEquals(1, status.getItemsCount());
        assertEquals("proc-42", status.getItems(0).getProcessId());
        assertEquals("e1", status.getItems(0).getExperimentId());
    }

    @Test
    void getExperimentSetStatus_wrongOwner_throwsNoSuchElement() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-7", "e1");
        entity.setOwner("otherUser");
        when(experimentSetRepository.findById("set-7")).thenReturn(Optional.of(entity));

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.getExperimentSetStatus(ctx, "set-7"));
    }

    @Test
    void getExperimentSetStatus_notFound_throwsNoSuchElement() {
        when(experimentSetRepository.findById("set-x")).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.getExperimentSetStatus(ctx, "set-x"));
    }

    // ---- launchExperimentSet tests ----

    @Test
    void launchExperimentSet_threeMembers_secondThrows_allThreeAttempted_doesNotThrow() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-launch", "e1", "e2", "e3");
        when(experimentSetRepository.findById("set-launch")).thenReturn(Optional.of(entity));

        doNothing().when(experimentService).launchExperimentWithStorageSetup(ctx, "e1", "testGateway", "test@example.com");
        doThrow(new ServiceException("launch failed")).when(experimentService)
                .launchExperimentWithStorageSetup(ctx, "e2", "testGateway", "test@example.com");
        doNothing().when(experimentService).launchExperimentWithStorageSetup(ctx, "e3", "testGateway", "test@example.com");

        // Must NOT throw even though e2 failed
        ExperimentSet result = assertDoesNotThrow(
                () -> experimentSetService.launchExperimentSet(ctx, "set-launch", "test@example.com"));

        verify(experimentService, times(1)).launchExperimentWithStorageSetup(ctx, "e1", "testGateway", "test@example.com");
        verify(experimentService, times(1)).launchExperimentWithStorageSetup(ctx, "e2", "testGateway", "test@example.com");
        verify(experimentService, times(1)).launchExperimentWithStorageSetup(ctx, "e3", "testGateway", "test@example.com");

        assertEquals("set-launch", result.getExperimentSetId());
    }

    @Test
    void launchExperimentSet_wrongOwner_throwsNoSuchElement() {
        ExperimentSetEntity entity = makeEntity("set-8", "e1");
        entity.setOwner("otherUser");
        when(experimentSetRepository.findById("set-8")).thenReturn(Optional.of(entity));

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.launchExperimentSet(ctx, "set-8", "test@example.com"));
    }

    // ---- getExperimentSet tests ----

    @Test
    void getExperimentSet_ownerMatch_returnsProto() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-9", "e1", "e2");
        when(experimentSetRepository.findById("set-9")).thenReturn(Optional.of(entity));

        ExperimentSet result = experimentSetService.getExperimentSet(ctx, "set-9");

        assertEquals("set-9", result.getExperimentSetId());
        assertEquals(2, result.getExperimentIdsCount());
    }

    @Test
    void getExperimentSet_wrongOwner_throwsNoSuchElement() {
        ExperimentSetEntity entity = makeEntity("set-10", "e1");
        entity.setOwner("otherUser");
        when(experimentSetRepository.findById("set-10")).thenReturn(Optional.of(entity));

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.getExperimentSet(ctx, "set-10"));
    }

    // ---- listExperimentSets tests ----

    @Test
    void listExperimentSets_limitsResults() throws Exception {
        ExperimentSetEntity e1 = makeEntity("s1", "exp1");
        ExperimentSetEntity e2 = makeEntity("s2", "exp2");
        ExperimentSetEntity e3 = makeEntity("s3", "exp3");
        when(experimentSetRepository.findByOwnerAndGatewayIdOrderByCreatedAtDesc("testUser", "testGateway"))
                .thenReturn(List.of(e1, e2, e3));

        List<ExperimentSet> result = experimentSetService.listExperimentSets(ctx, 2, 0);

        assertEquals(2, result.size());
        assertEquals("s1", result.get(0).getExperimentSetId());
    }

    @Test
    void listExperimentSets_offsetAndLimit() throws Exception {
        ExperimentSetEntity e1 = makeEntity("s1", "exp1");
        ExperimentSetEntity e2 = makeEntity("s2", "exp2");
        ExperimentSetEntity e3 = makeEntity("s3", "exp3");
        when(experimentSetRepository.findByOwnerAndGatewayIdOrderByCreatedAtDesc("testUser", "testGateway"))
                .thenReturn(List.of(e1, e2, e3));

        List<ExperimentSet> result = experimentSetService.listExperimentSets(ctx, 2, 1);

        assertEquals(2, result.size());
        assertEquals("s2", result.get(0).getExperimentSetId());
        assertEquals("s3", result.get(1).getExperimentSetId());
    }

    @Test
    void listExperimentSets_zeroLimit_returnsAll() throws Exception {
        ExperimentSetEntity e1 = makeEntity("s1", "exp1");
        ExperimentSetEntity e2 = makeEntity("s2", "exp2");
        when(experimentSetRepository.findByOwnerAndGatewayIdOrderByCreatedAtDesc("testUser", "testGateway"))
                .thenReturn(List.of(e1, e2));

        List<ExperimentSet> result = experimentSetService.listExperimentSets(ctx, 0, 0);

        assertEquals(2, result.size());
    }

    // ---- deleteExperimentSet tests ----

    @Test
    void deleteExperimentSet_ownerMatch_deletesById() throws Exception {
        ExperimentSetEntity entity = makeEntity("set-del", "e1");
        when(experimentSetRepository.findById("set-del")).thenReturn(Optional.of(entity));

        experimentSetService.deleteExperimentSet(ctx, "set-del");

        verify(experimentSetRepository, times(1)).deleteById("set-del");
    }

    @Test
    void deleteExperimentSet_wrongOwner_throwsNoSuchElement() {
        ExperimentSetEntity entity = makeEntity("set-del2", "e1");
        entity.setOwner("otherUser");
        when(experimentSetRepository.findById("set-del2")).thenReturn(Optional.of(entity));

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.deleteExperimentSet(ctx, "set-del2"));
        verify(experimentSetRepository, never()).deleteById(any());
    }

    // ---- Fix 1: gateway scoping in loadOwned ----

    @Test
    void getExperimentSet_wrongGateway_throwsNoSuchElement() {
        // Same userId as ctx but different gatewayId — must be treated as not-found
        ExperimentSetEntity entity = makeEntity("set-gw", "e1");
        entity.setGatewayId("otherGateway");
        when(experimentSetRepository.findById("set-gw")).thenReturn(Optional.of(entity));

        assertThrows(java.util.NoSuchElementException.class,
                () -> experimentSetService.getExperimentSet(ctx, "set-gw"));
    }

    // ---- Fix 2: null guard in getExperimentSetStatus ----

    @Test
    void getExperimentSetStatus_nullExperiment_countedAsUnknown_noException() throws Exception {
        // e1 returns null (e.g. experiment from another gateway that caller cannot read),
        // e2 is a normal COMPLETED experiment.
        ExperimentSetEntity entity = makeEntity("set-null", "e1", "e2");
        when(experimentSetRepository.findById("set-null")).thenReturn(Optional.of(entity));
        when(experimentService.getExperiment(ctx, "e1")).thenReturn(null);
        when(experimentService.getExperiment(ctx, "e2"))
                .thenReturn(makeExperiment("e2", ExperimentState.EXPERIMENT_STATE_COMPLETED));

        ExperimentSetStatus status = assertDoesNotThrow(
                () -> experimentSetService.getExperimentSetStatus(ctx, "set-null"));

        // Total includes all members — both e1 and e2
        assertEquals(2, status.getTotal());
        // e1 counted as UNKNOWN
        assertEquals(1, status.getCountsByStateOrDefault("EXPERIMENT_STATE_UNKNOWN", 0));
        // e2 counted as COMPLETED
        assertEquals(1, status.getCountsByStateOrDefault("EXPERIMENT_STATE_COMPLETED", 0));
        // Items list has both members
        assertEquals(2, status.getItemsCount());
        // The unknown item has empty processId
        var unknownItem = status.getItemsList().stream()
                .filter(i -> i.getExperimentId().equals("e1"))
                .findFirst()
                .orElseThrow();
        assertEquals("EXPERIMENT_STATE_UNKNOWN", unknownItem.getState());
        assertEquals("", unknownItem.getProcessId());
    }
}
