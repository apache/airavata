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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.api.experiment.ExperimentSpec;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.api.experimentset.CreateExperimentSetRequest;
import org.apache.airavata.api.experimentset.ExperimentSet;
import org.apache.airavata.api.experimentset.SweepSpec;
import org.apache.airavata.api.experimentset.ValueList;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.model.commons.proto.AccessFlags;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.research.model.ExperimentSetEntity;
import org.apache.airavata.research.repository.ExperimentSetRepository;
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
class ExperimentSetServiceTest {

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

    private ExperimentWithAccess stubExperimentWithId(String id) {
        ExperimentModel model = ExperimentModel.newBuilder()
                .setExperimentId(id)
                .setUserName("testUser")
                .setGatewayId("testGateway")
                .build();
        return ExperimentWithAccess.newBuilder()
                .setExperiment(model)
                .setAccess(AccessFlags.newBuilder().setIsOwner(true).build())
                .build();
    }

    private ExperimentSetEntity savedEntityWithId(ExperimentSetEntity entity) {
        entity.setId("set-1");
        return entity;
    }

    @Test
    void createExperimentSet_sweep_callsCreateFromSpecFourTimes_andPersistsSet() throws Exception {
        // 2 x 2 sweep axes
        SweepSpec sweep = SweepSpec.newBuilder()
                .setBase(ExperimentSpec.newBuilder()
                        .setExperimentName("base-run")
                        .setProjectId("proj-1")
                        .setApplicationInterfaceId("echo-app")
                        .putInputs("temp", "300")
                        .putInputs("app", "echo")
                        .build())
                .putSweepAxes("temp", ValueList.newBuilder().addValues("300").addValues("400").build())
                .putSweepAxes("salt", ValueList.newBuilder().addValues("lo").addValues("hi").build())
                .setNamePrefix("run")
                .build();

        CreateExperimentSetRequest req = CreateExperimentSetRequest.newBuilder()
                .setSetName("my-sweep")
                .setSweep(sweep)
                .build();

        when(experimentService.createExperimentFromSpec(eq(ctx), any(ExperimentSpec.class)))
                .thenReturn(stubExperimentWithId("exp-0"))
                .thenReturn(stubExperimentWithId("exp-1"))
                .thenReturn(stubExperimentWithId("exp-2"))
                .thenReturn(stubExperimentWithId("exp-3"));

        when(experimentSetRepository.save(any(ExperimentSetEntity.class))).thenAnswer(inv -> {
            ExperimentSetEntity e = inv.getArgument(0);
            e.setId("set-1");
            return e;
        });

        ExperimentSet result = experimentSetService.createExperimentSet(ctx, req);

        // createExperimentFromSpec called exactly 4 times
        ArgumentCaptor<ExperimentSpec> specCaptor = ArgumentCaptor.forClass(ExperimentSpec.class);
        verify(experimentService, times(4)).createExperimentFromSpec(eq(ctx), specCaptor.capture());

        List<ExperimentSpec> capturedSpecs = specCaptor.getAllValues();
        // Each spec should have the correct name suffix (run_0 through run_3)
        Set<String> names = capturedSpecs.stream().map(ExperimentSpec::getExperimentName).collect(Collectors.toSet());
        assertEquals(Set.of("run_0", "run_1", "run_2", "run_3"), names);

        // All specs carry the base "app" key
        for (ExperimentSpec s : capturedSpecs) {
            assertEquals("echo", s.getInputsMap().get("app"));
        }

        // The sweep covers all temp x salt combinations
        Set<String> temps = capturedSpecs.stream()
                .map(s -> s.getInputsMap().get("temp"))
                .collect(Collectors.toSet());
        assertEquals(Set.of("300", "400"), temps);

        // Repository saved once
        verify(experimentSetRepository, times(1)).save(any(ExperimentSetEntity.class));

        // Result has 4 member ids
        assertEquals(4, result.getExperimentIdsCount());
        assertTrue(result.getExperimentIdsList().containsAll(List.of("exp-0", "exp-1", "exp-2", "exp-3")));

        // sweepSpecJson was stored (non-empty means it was serialized)
        // We validate via the ArgumentCaptor on the saved entity
        ArgumentCaptor<ExperimentSetEntity> entityCaptor = ArgumentCaptor.forClass(ExperimentSetEntity.class);
        verify(experimentSetRepository).save(entityCaptor.capture());
        assertNotNull(entityCaptor.getValue().getSweepSpecJson());
        assertFalse(entityCaptor.getValue().getSweepSpecJson().isEmpty());
    }

    @Test
    void createExperimentSet_sweep_thirdCallThrows_rollsBackAndDoesNotPersist() throws Exception {
        SweepSpec sweep = SweepSpec.newBuilder()
                .setBase(ExperimentSpec.newBuilder()
                        .setExperimentName("base-run")
                        .setProjectId("proj-1")
                        .setApplicationInterfaceId("echo-app")
                        .putInputs("temp", "300")
                        .build())
                .putSweepAxes("temp", ValueList.newBuilder().addValues("300").addValues("400").build())
                .putSweepAxes("salt", ValueList.newBuilder().addValues("lo").addValues("hi").build())
                .setNamePrefix("run")
                .build();

        CreateExperimentSetRequest req = CreateExperimentSetRequest.newBuilder()
                .setSetName("failing-sweep")
                .setSweep(sweep)
                .build();

        when(experimentService.createExperimentFromSpec(eq(ctx), any(ExperimentSpec.class)))
                .thenReturn(stubExperimentWithId("exp-0"))
                .thenReturn(stubExperimentWithId("exp-1"))
                .thenThrow(new ServiceException("creation failed on 3rd"));

        assertThrows(ServiceException.class, () -> experimentSetService.createExperimentSet(ctx, req));

        // The 2 already-created experiments should be deleted (cleanup)
        verify(experimentService).deleteExperiment(ctx, "exp-0");
        verify(experimentService).deleteExperiment(ctx, "exp-1");

        // Repository save must NOT have been called
        verify(experimentSetRepository, never()).save(any());
    }
}
