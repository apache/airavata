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
import org.apache.airavata.api.experiment.ExperimentSpec;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.api.experiment.ResourceSpec;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.DataProductInterface;
import org.apache.airavata.interfaces.DataReplicaLocationInterface;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentType;
import org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel;
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
class ExperimentServiceFromSpecTest {

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
        when(sharingHandler.userHasAccess(anyString(), anyString(), anyString(), anyString()))
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
                List.of("admin-rw"));
    }

    @Test
    void createExperimentFromSpec_buildsCorrectModel() throws Exception {
        ApplicationInterfaceDescription appInterface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("echo-app")
                .addApplicationInputs(InputDataObjectType.newBuilder()
                        .setName("input1")
                        .build())
                .addApplicationOutputs(OutputDataObjectType.newBuilder()
                        .setName("output1")
                        .build())
                .build();

        when(appCatalogRegistry.getApplicationInterface("echo-app")).thenReturn(appInterface);
        when(experimentRegistry.createExperiment(eq("testGateway"), any(ExperimentModel.class)))
                .thenReturn("exp-from-spec-1");

        ExperimentModel createdModel = ExperimentModel.newBuilder()
                .setExperimentId("exp-from-spec-1")
                .setExperimentName("my-echo-run")
                .setProjectId("proj-1")
                .setGatewayId("testGateway")
                .setUserName("testUser")
                .setExecutionId("echo-app")
                .setExperimentType(ExperimentType.SINGLE_APPLICATION)
                .build();
        when(experimentRegistry.getExperiment("exp-from-spec-1")).thenReturn(createdModel);

        ExperimentSpec spec = ExperimentSpec.newBuilder()
                .setExperimentName("my-echo-run")
                .setProjectId("proj-1")
                .setApplicationInterfaceId("echo-app")
                .setDescription("test run")
                .putInputs("input1", "hello-value")
                .setResource(ResourceSpec.newBuilder()
                        .setComputeResourceId("stampede3")
                        .setGroupResourceProfileId("grp-1")
                        .setNodeCount(1)
                        .setTotalCpuCount(4)
                        .setQueueName("normal")
                        .setWallTimeLimit(60)
                        .build())
                .build();

        ExperimentWithAccess result = experimentService.createExperimentFromSpec(ctx, spec);

        // Verify the returned wrapper
        assertNotNull(result);
        assertEquals("exp-from-spec-1", result.getExperiment().getExperimentId());
        assertTrue(result.getAccess().getIsOwner());

        // Capture the ExperimentModel passed to createExperiment
        ArgumentCaptor<ExperimentModel> modelCaptor = ArgumentCaptor.forClass(ExperimentModel.class);
        verify(experimentRegistry).createExperiment(eq("testGateway"), modelCaptor.capture());
        ExperimentModel built = modelCaptor.getValue();

        assertEquals("my-echo-run", built.getExperimentName());
        assertEquals("proj-1", built.getProjectId());
        assertEquals("testGateway", built.getGatewayId());
        assertEquals("testUser", built.getUserName());
        assertEquals("echo-app", built.getExecutionId());
        assertEquals(ExperimentType.SINGLE_APPLICATION, built.getExperimentType());
        assertEquals("test run", built.getDescription());

        // Scheduling
        ComputationalResourceSchedulingModel scheduling =
                built.getUserConfigurationData().getComputationalResourceScheduling();
        assertEquals("stampede3", scheduling.getResourceHostId());
        assertEquals(1, scheduling.getNodeCount());
        assertEquals(4, scheduling.getTotalCpuCount());
        assertEquals("normal", scheduling.getQueueName());
        assertEquals(60, scheduling.getWallTimeLimit());

        // UserConfigurationData
        assertEquals("grp-1", built.getUserConfigurationData().getGroupResourceProfileId());

        // Input wiring: "input1" value should be "hello-value"
        assertEquals(1, built.getExperimentInputsList().size());
        assertEquals("input1", built.getExperimentInputs(0).getName());
        assertEquals("hello-value", built.getExperimentInputs(0).getValue());

        // Outputs copied from interface
        assertEquals(1, built.getExperimentOutputsList().size());
        assertEquals("output1", built.getExperimentOutputs(0).getName());
    }

    @Test
    void createExperimentFromSpec_undeclaredInputNameThrows() throws Exception {
        ApplicationInterfaceDescription appInterface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId("echo-app")
                .addApplicationInputs(InputDataObjectType.newBuilder()
                        .setName("input1")
                        .build())
                .build();

        when(appCatalogRegistry.getApplicationInterface("echo-app")).thenReturn(appInterface);

        ExperimentSpec spec = ExperimentSpec.newBuilder()
                .setExperimentName("my-echo-run")
                .setProjectId("proj-1")
                .setApplicationInterfaceId("echo-app")
                .putInputs("input1", "val1")
                .putInputs("undeclared-input", "val2") // not in the interface
                .setResource(ResourceSpec.newBuilder()
                        .setComputeResourceId("stampede3")
                        .build())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> experimentService.createExperimentFromSpec(ctx, spec));
    }

    @Test
    void createExperimentFromSpec_emptyProjectIdThrows() throws Exception {
        ExperimentSpec spec = ExperimentSpec.newBuilder()
                .setExperimentName("my-echo-run")
                .setProjectId("") // empty
                .setApplicationInterfaceId("echo-app")
                .setResource(ResourceSpec.newBuilder()
                        .setComputeResourceId("stampede3")
                        .build())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> experimentService.createExperimentFromSpec(ctx, spec));
    }

    @Test
    void createExperimentFromSpec_emptyApplicationInterfaceIdThrows() throws Exception {
        ExperimentSpec spec = ExperimentSpec.newBuilder()
                .setExperimentName("my-echo-run")
                .setProjectId("proj-1")
                .setApplicationInterfaceId("") // empty
                .setResource(ResourceSpec.newBuilder()
                        .setComputeResourceId("stampede3")
                        .build())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> experimentService.createExperimentFromSpec(ctx, spec));
    }
}
