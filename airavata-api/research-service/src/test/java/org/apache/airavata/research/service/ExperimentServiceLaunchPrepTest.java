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
import java.util.Optional;
import org.apache.airavata.api.experiment.ExperimentWithAccess;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.interfaces.AppCatalogRegistry;
import org.apache.airavata.interfaces.ComputeRegistry;
import org.apache.airavata.interfaces.DataProductInterface;
import org.apache.airavata.interfaces.DataReplicaLocationInterface;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.ExperimentStoragePrep;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.DataType;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.data.replica.proto.ReplicaLocationCategory;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.model.workspace.proto.Project;
import org.apache.airavata.orchestration.service.LaunchOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for the server-side launch-prep ported into {@link ExperimentService#launchExperiment}.
 * Sharing is enabled via airavata-server.properties on the classpath (matching ExperimentServiceTest),
 * so the sharing mock allows all access checks. The launch path past prep is greased so launch reaches
 * the orchestrator: a group resource profile id is preset (skips the backfill), auto-schedule is on
 * with no auto-scheduled list (skips app-deployment access checks), and a mock LaunchOrchestrator is
 * provided.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExperimentServiceLaunchPrepTest {

    private static final String GATEWAY = "testGateway";
    private static final String USER = "testUser";
    private static final String EXP_ID = "exp-123";
    private static final String DEFAULT_STORAGE_ID = "storage-default";
    private static final String GRP_ID = "grp-1";
    private static final String EXEC_ID = "appiface-1";

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

    @Mock
    LaunchOrchestrator launchOrchestrator;

    @Mock
    ExperimentStoragePrep storagePrep;

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
                Optional.of(launchOrchestrator));

        ctx = new RequestContext(
                USER, GATEWAY, "token123", Map.of("userName", USER, "gatewayId", GATEWAY), List.of("admin-rw"));

        // Grease the launch path past prep so it reaches the orchestrator.
        ApplicationInterfaceDescription appIface = ApplicationInterfaceDescription.newBuilder()
                .setApplicationInterfaceId(EXEC_ID)
                .addApplicationModules("module-1")
                .build();
        when(appCatalogRegistry.getApplicationInterface(EXEC_ID)).thenReturn(appIface);
        when(appCatalogRegistry.getApplicationDeployments("module-1"))
                .thenReturn(List.of(ApplicationDeploymentDescription.getDefaultInstance()));
    }

    // A CREATED experiment with a preset GRP (skips backfill), auto-schedule on (skips deployment
    // checks), and the given user configuration / inputs.
    private ExperimentModel experimentWith(UserConfigurationDataModel.Builder config, InputDataObjectType... inputs) {
        config.setGroupResourceProfileId(GRP_ID).setAiravataAutoSchedule(true);
        ExperimentModel.Builder builder = ExperimentModel.newBuilder()
                .setExperimentId(EXP_ID)
                .setExperimentName("My Experiment")
                .setUserName(USER)
                .setGatewayId(GATEWAY)
                .setProjectId("proj-1")
                .setExecutionId(EXEC_ID)
                .setUserConfigurationData(config.build());
        for (InputDataObjectType input : inputs) {
            builder.addExperimentInputs(input);
        }
        return builder.build();
    }

    @Test
    void launch_storagePrepNull_doesNoPrep() throws Exception {
        // storagePrep is left unset (null) -> launch proceeds with no prep side-effects.
        ExperimentModel experiment =
                experimentWith(UserConfigurationDataModel.newBuilder().setExperimentDataDir("proj/exp"));
        when(experimentRegistry.getExperiment(EXP_ID)).thenReturn(experiment);

        experimentService.launchExperiment(ctx, EXP_ID, GATEWAY);

        verifyNoInteractions(storagePrep);
        verifyNoInteractions(dataProductInterface);
        verifyNoInteractions(dataReplicaLocationInterface);
        // No configuration was rewritten by prep.
        verify(experimentRegistry, never()).updateExperimentConfiguration(anyString(), any());
        verify(launchOrchestrator).launchExperiment(EXP_ID, GATEWAY);
    }

    @Test
    void launch_setsStorageIdsAndDataDir_whenEmpty() throws Exception {
        experimentService.setExperimentStoragePrep(storagePrep);

        // Empty storage ids and data dir -> all derived/filled.
        ExperimentModel experiment = experimentWith(UserConfigurationDataModel.newBuilder());
        when(experimentRegistry.getExperiment(EXP_ID)).thenReturn(experiment);
        when(storagePrep.getDefaultStorageResourceId()).thenReturn(DEFAULT_STORAGE_ID);
        Project project = Project.newBuilder().setName("My Project").build();
        when(projectRegistry.getProject("proj-1")).thenReturn(project);
        // "<project>/<experiment>" sanitized: spaces -> underscores.
        String expectedRelPath = "My_Project/My_Experiment";
        when(storagePrep.ensureDir(DEFAULT_STORAGE_ID, expectedRelPath))
                .thenReturn("/storage/My_Project/My_Experiment");

        experimentService.launchExperiment(ctx, EXP_ID, GATEWAY);

        verify(storagePrep).ensureDir(DEFAULT_STORAGE_ID, expectedRelPath);

        ArgumentCaptor<UserConfigurationDataModel> captor = ArgumentCaptor.forClass(UserConfigurationDataModel.class);
        verify(experimentRegistry).updateExperimentConfiguration(eq(EXP_ID), captor.capture());
        UserConfigurationDataModel persisted = captor.getValue();
        assertEquals(DEFAULT_STORAGE_ID, persisted.getInputStorageResourceId());
        assertEquals(DEFAULT_STORAGE_ID, persisted.getOutputStorageResourceId());
        assertEquals("/storage/My_Project/My_Experiment", persisted.getExperimentDataDir());

        verify(launchOrchestrator).launchExperiment(EXP_ID, GATEWAY);
    }

    @Test
    void launch_keepsExistingStorageIdsAndDataDir() throws Exception {
        experimentService.setExperimentStoragePrep(storagePrep);

        // Non-empty values must never be clobbered or re-derived.
        ExperimentModel experiment = experimentWith(UserConfigurationDataModel.newBuilder()
                .setInputStorageResourceId("preset-input")
                .setOutputStorageResourceId("preset-output")
                .setExperimentDataDir("preset/dir"));
        when(experimentRegistry.getExperiment(EXP_ID)).thenReturn(experiment);

        experimentService.launchExperiment(ctx, EXP_ID, GATEWAY);

        // Storage fully pre-set: the gateway default is never resolved (lazy), the existing data dir is
        // ensured-to-exist on the EFFECTIVE input storage but kept, and the project is never resolved.
        verify(storagePrep, never()).getDefaultStorageResourceId();
        verify(storagePrep).ensureDir("preset-input", "preset/dir");
        verify(projectRegistry, never()).getProject(anyString());

        ArgumentCaptor<UserConfigurationDataModel> captor = ArgumentCaptor.forClass(UserConfigurationDataModel.class);
        verify(experimentRegistry).updateExperimentConfiguration(eq(EXP_ID), captor.capture());
        UserConfigurationDataModel persisted = captor.getValue();
        assertEquals("preset-input", persisted.getInputStorageResourceId());
        assertEquals("preset-output", persisted.getOutputStorageResourceId());
        assertEquals("preset/dir", persisted.getExperimentDataDir());
    }

    @Test
    void launch_movesTmpInput_andRepointsReplica_preservingUri() throws Exception {
        experimentService.setExperimentStoragePrep(storagePrep);

        String uri = "airavata-dp://tmpinput";
        InputDataObjectType input = InputDataObjectType.newBuilder()
                .setName("in1")
                .setType(DataType.URI)
                .setValue(uri)
                .build();
        ExperimentModel experiment =
                experimentWith(UserConfigurationDataModel.newBuilder().setExperimentDataDir("proj/exp"), input);
        when(experimentRegistry.getExperiment(EXP_ID)).thenReturn(experiment);
        when(storagePrep.getDefaultStorageResourceId()).thenReturn(DEFAULT_STORAGE_ID);

        // A tmp-resident replica (parent dir basename == "tmp"), with a replica id so it gets repointed.
        DataReplicaLocationModel replica = DataReplicaLocationModel.newBuilder()
                .setReplicaId("rep-1")
                .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                .setStorageResourceId(DEFAULT_STORAGE_ID)
                .setFilePath("data/tmp/input.dat")
                .build();
        DataProductModel product = DataProductModel.newBuilder()
                .setProductName("input.dat")
                .addReplicaLocations(replica)
                .build();
        when(dataProductInterface.getDataProduct(uri)).thenReturn(product);
        // Source present, destination absent -> a real move happens (not the already-moved guard).
        when(storagePrep.fileExists(DEFAULT_STORAGE_ID, "proj/exp/input.dat")).thenReturn(false);
        when(storagePrep.fileExists(DEFAULT_STORAGE_ID, "data/tmp/input.dat")).thenReturn(true);

        experimentService.launchExperiment(ctx, EXP_ID, GATEWAY);

        // Bytes moved into the data dir under the product name.
        verify(storagePrep).moveFile(DEFAULT_STORAGE_ID, "data/tmp/input.dat", "proj/exp/input.dat");

        // Replica repointed to the destination, preserving the URI (no input value rewrite).
        ArgumentCaptor<DataReplicaLocationModel> replicaCaptor =
                ArgumentCaptor.forClass(DataReplicaLocationModel.class);
        verify(dataReplicaLocationInterface).updateReplicaLocation(replicaCaptor.capture());
        assertEquals("proj/exp/input.dat", replicaCaptor.getValue().getFilePath());
        assertEquals("rep-1", replicaCaptor.getValue().getReplicaId());

        // The input value string is unchanged (URI preserved); no updateExperiment was issued.
        verify(experimentRegistry, never()).updateExperiment(anyString(), any());
    }

    @Test
    void launch_skipsNonTmpInput() throws Exception {
        experimentService.setExperimentStoragePrep(storagePrep);

        String uri = "airavata-dp://notatmpinput";
        InputDataObjectType input = InputDataObjectType.newBuilder()
                .setName("in1")
                .setType(DataType.URI)
                .setValue(uri)
                .build();
        ExperimentModel experiment =
                experimentWith(UserConfigurationDataModel.newBuilder().setExperimentDataDir("proj/exp"), input);
        when(experimentRegistry.getExperiment(EXP_ID)).thenReturn(experiment);
        when(storagePrep.getDefaultStorageResourceId()).thenReturn(DEFAULT_STORAGE_ID);

        // Parent dir basename is "inputs", not "tmp" -> not a tmp upload -> skipped.
        DataReplicaLocationModel replica = DataReplicaLocationModel.newBuilder()
                .setReplicaId("rep-2")
                .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                .setStorageResourceId(DEFAULT_STORAGE_ID)
                .setFilePath("/storage/proj/exp/inputs/keep.dat")
                .build();
        DataProductModel product = DataProductModel.newBuilder()
                .setProductName("keep.dat")
                .addReplicaLocations(replica)
                .build();
        when(dataProductInterface.getDataProduct(uri)).thenReturn(product);

        experimentService.launchExperiment(ctx, EXP_ID, GATEWAY);

        // No move, no repoint for a non-tmp input.
        verify(storagePrep, never()).moveFile(anyString(), anyString(), anyString());
        verify(dataReplicaLocationInterface, never()).updateReplicaLocation(any());
        // The input URI value is unchanged.
        assertEquals(uri, input.getValue());
    }

    private DataProductModel gatewayDataProduct(String name, String storageId, String filePath) {
        return DataProductModel.newBuilder()
                .setProductName(name)
                .addReplicaLocations(DataReplicaLocationModel.newBuilder()
                        .setReplicaLocationCategory(ReplicaLocationCategory.GATEWAY_DATA_STORE)
                        .setStorageResourceId(storageId)
                        .setFilePath(filePath)
                        .build())
                .build();
    }

    @Test
    void cloneExperimentWithInputFiles_copiesUriInputAndNullsDataDir() throws Exception {
        ExperimentModel source = ExperimentModel.newBuilder()
                .setExperimentName("src")
                .setGatewayId(GATEWAY)
                .setUserName(USER)
                .setProjectId("proj-1")
                .addExperimentInputs(InputDataObjectType.newBuilder()
                        .setName("in")
                        .setType(DataType.URI)
                        .setValue("airavata-dp://src")
                        .build())
                .build();
        ExperimentModel cloned = source.toBuilder()
                .setExperimentId("clone-1")
                .setUserConfigurationData(
                        UserConfigurationDataModel.newBuilder().setExperimentDataDir("stale/dir"))
                .build();
        ExperimentWithAccess expected =
                ExperimentWithAccess.newBuilder().setExperiment(cloned).build();

        ExperimentService spy = spy(experimentService);
        spy.setExperimentStoragePrep(storagePrep);
        doReturn(source).when(spy).getExperiment(ctx, EXP_ID);
        doReturn("clone-1").when(spy).cloneExperiment(ctx, EXP_ID, "Clone of src", "proj-1", false);
        when(experimentRegistry.getExperiment("clone-1")).thenReturn(cloned);
        doNothing().when(spy).updateExperiment(eq(ctx), eq("clone-1"), any());
        doReturn(expected).when(spy).getExperimentWithAccess(ctx, "clone-1");

        when(dataProductInterface.getDataProduct("airavata-dp://src"))
                .thenReturn(gatewayDataProduct("file.dat", "s1", "/storage/tmp/file.dat"));
        when(storagePrep.fileExists("s1", "/storage/tmp/file.dat")).thenReturn(true);
        when(dataProductInterface.registerDataProduct(any())).thenReturn("airavata-dp://copy");

        ExperimentWithAccess result = spy.cloneExperimentWithInputFiles(ctx, EXP_ID, "", "proj-1");

        // Native copy into the tmp upload dir (no download/upload round-trip).
        verify(storagePrep).copyFile("s1", "/storage/tmp/file.dat", "tmp/file.dat");
        ArgumentCaptor<ExperimentModel> captor = ArgumentCaptor.forClass(ExperimentModel.class);
        verify(spy).updateExperiment(eq(ctx), eq("clone-1"), captor.capture());
        // Input rewritten to the freshly registered copy; data dir nulled for fresh creation at launch.
        assertEquals(
                "airavata-dp://copy", captor.getValue().getExperimentInputs(0).getValue());
        assertEquals("", captor.getValue().getUserConfigurationData().getExperimentDataDir());
        assertSame(expected, result);
    }

    @Test
    void cloneExperimentWithInputFiles_dropsUriInputWhenSourceFileMissing() throws Exception {
        ExperimentModel source = ExperimentModel.newBuilder()
                .setExperimentName("src")
                .setGatewayId(GATEWAY)
                .setUserName(USER)
                .setProjectId("proj-1")
                .addExperimentInputs(InputDataObjectType.newBuilder()
                        .setName("in")
                        .setType(DataType.URI)
                        .setValue("airavata-dp://gone")
                        .build())
                .build();
        ExperimentModel cloned = source.toBuilder().setExperimentId("clone-1").build();

        ExperimentService spy = spy(experimentService);
        spy.setExperimentStoragePrep(storagePrep);
        doReturn(source).when(spy).getExperiment(ctx, EXP_ID);
        doReturn("clone-1").when(spy).cloneExperiment(ctx, EXP_ID, "Clone of src", "proj-1", false);
        when(experimentRegistry.getExperiment("clone-1")).thenReturn(cloned);
        doNothing().when(spy).updateExperiment(eq(ctx), eq("clone-1"), any());
        doReturn(ExperimentWithAccess.getDefaultInstance()).when(spy).getExperimentWithAccess(ctx, "clone-1");

        when(dataProductInterface.getDataProduct("airavata-dp://gone"))
                .thenReturn(gatewayDataProduct("file.dat", "s1", "/storage/tmp/file.dat"));
        when(storagePrep.fileExists("s1", "/storage/tmp/file.dat")).thenReturn(false);

        spy.cloneExperimentWithInputFiles(ctx, EXP_ID, "", "proj-1");

        verify(storagePrep, never()).copyFile(anyString(), anyString(), anyString());
        ArgumentCaptor<ExperimentModel> captor = ArgumentCaptor.forClass(ExperimentModel.class);
        verify(spy).updateExperiment(eq(ctx), eq("clone-1"), captor.capture());
        // Missing source file -> input value dropped to empty.
        assertEquals("", captor.getValue().getExperimentInputs(0).getValue());
    }
}
