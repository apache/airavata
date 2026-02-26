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
package org.apache.airavata.execution.dag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.execution.entity.ProcessEntity;
import org.apache.airavata.execution.repository.ProcessRepository;
import org.apache.airavata.execution.task.TaskContext;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.apache.airavata.research.experiment.repository.ExperimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for {@link TaskContextFactory}.
 *
 * <p>No Spring application context or database is required. All repository
 * interactions are satisfied by Mockito mocks. The tests cover:
 * <ul>
 *   <li>Happy-path context construction with all fields populated</li>
 *   <li>Exception propagation when the process record is absent</li>
 *   <li>Exception propagation when the experiment record is absent</li>
 *   <li>Field-level mapping fidelity for {@code ProcessModel}</li>
 *   <li>Field-level mapping fidelity for {@code ExperimentModel}</li>
 *   <li>Presence of the experiment model on the returned context</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class TaskContextFactoryTest {

    // -------------------------------------------------------------------------
    // Shared test fixtures
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID = "proc-test-001";
    private static final String EXPERIMENT_ID = "exp-test-001";
    private static final String GATEWAY_ID = "gw-test-001";
    private static final String TASK_ID = "task-test-001";
    private static final String APPLICATION_ID = "app-test-001";
    private static final String RESOURCE_ID = "resource-test-001";
    private static final String BINDING_ID = "binding-test-001";

    private static final String EXP_NAME = "Echo Experiment";
    private static final String PROJECT_ID = "project-test-001";
    private static final String USER_NAME = "testuser";
    private static final String DESCRIPTION = "A test experiment description";
    private static final String EXP_APP_ID = "app-exp-test-001";
    private static final String EXP_BINDING_ID = "binding-exp-test-001";

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ResourceService resourceService;

    private TaskContextFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new TaskContextFactory(processRepository, experimentRepository, resourceService);
    }

    // -------------------------------------------------------------------------
    // 1. Happy path — TaskContext is built when both entities are present
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_happyPath_returnsNonNullContext() {
        ProcessEntity processEntity = buildProcessEntity();
        ExperimentEntity experimentEntity = buildExperimentEntity();

        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(processEntity));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(experimentEntity));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertNotNull(context, "buildContext must return a non-null TaskContext");
    }

    @Test
    public void buildContext_happyPath_contextCarriesCorrectProcessId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(PROCESS_ID, context.getProcessId(), "TaskContext.processId must equal the processId argument");
    }

    @Test
    public void buildContext_happyPath_contextCarriesCorrectGatewayId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(GATEWAY_ID, context.getGatewayId(), "TaskContext.gatewayId must equal the gatewayId argument");
    }

    @Test
    public void buildContext_happyPath_contextCarriesCorrectTaskId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(TASK_ID, context.getTaskId(), "TaskContext.taskId must equal the taskId argument");
    }

    @Test
    public void buildContext_happyPath_contextCarriesNonNullProcessModel() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertNotNull(context.getProcessModel(), "TaskContext.processModel must not be null on the happy path");
    }

    // -------------------------------------------------------------------------
    // 2. Process not found — IllegalStateException must be thrown
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_processNotFound_throwsIllegalStateException() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID),
                "buildContext must throw IllegalStateException when the process is not found");
    }

    @Test
    public void buildContext_processNotFound_exceptionMessageContainsProcessId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.empty());

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID));

        assertNotNull(ex.getMessage(), "Exception message must not be null");
        assertEquals(
                true,
                ex.getMessage().contains(PROCESS_ID),
                "Exception message must contain the missing processId; got: " + ex.getMessage());
    }

    @Test
    public void buildContext_processNotFound_experimentRepositoryIsNeverQueried() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID));

        verifyNoInteractions(experimentRepository);
    }

    // -------------------------------------------------------------------------
    // 3. Experiment not found — IllegalStateException must be thrown
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_experimentNotFound_throwsIllegalStateException() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID),
                "buildContext must throw IllegalStateException when the experiment is not found");
    }

    @Test
    public void buildContext_experimentNotFound_exceptionMessageContainsExperimentId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID));

        assertNotNull(ex.getMessage(), "Exception message must not be null");
        assertEquals(
                true,
                ex.getMessage().contains(EXPERIMENT_ID),
                "Exception message must contain the missing experimentId; got: " + ex.getMessage());
    }

    @Test
    public void buildContext_experimentNotFound_processRepositoryWasQueried() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID));

        verify(processRepository).findById(PROCESS_ID);
    }

    // -------------------------------------------------------------------------
    // 4. ProcessModel field mapping — all entity fields propagate correctly
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_processModelMapping_processIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                PROCESS_ID,
                context.getProcessModel().getProcessId(),
                "ProcessModel.processId must be mapped from ProcessEntity.getProcessId()");
    }

    @Test
    public void buildContext_processModelMapping_experimentIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                EXPERIMENT_ID,
                context.getProcessModel().getExperimentId(),
                "ProcessModel.experimentId must be mapped from ProcessEntity.getExperimentId()");
    }

    @Test
    public void buildContext_processModelMapping_applicationIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                APPLICATION_ID,
                context.getProcessModel().getApplicationId(),
                "ProcessModel.applicationId must be mapped from ProcessEntity.getApplicationId()");
    }

    @Test
    public void buildContext_processModelMapping_resourceIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                RESOURCE_ID,
                context.getProcessModel().getResourceId(),
                "ProcessModel.resourceId must be mapped from ProcessEntity.getResourceId()");
    }

    @Test
    public void buildContext_processModelMapping_bindingIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                BINDING_ID,
                context.getProcessModel().getBindingId(),
                "ProcessModel.bindingId must be mapped from ProcessEntity.getBindingId()");
    }

    @Test
    public void buildContext_processModelMapping_allFiveFieldsMappedInSingleCall() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(PROCESS_ID, context.getProcessModel().getProcessId(), "processId mismatch");
        assertEquals(EXPERIMENT_ID, context.getProcessModel().getExperimentId(), "experimentId mismatch");
        assertEquals(APPLICATION_ID, context.getProcessModel().getApplicationId(), "applicationId mismatch");
        assertEquals(RESOURCE_ID, context.getProcessModel().getResourceId(), "resourceId mismatch");
        assertEquals(BINDING_ID, context.getProcessModel().getBindingId(), "bindingId mismatch");
    }

    // -------------------------------------------------------------------------
    // 5. ExperimentModel field mapping — all entity fields propagate correctly
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_experimentModelMapping_experimentIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                EXPERIMENT_ID,
                context.getExperimentModel().getExperimentId(),
                "ExperimentModel.experimentId must be mapped from ExperimentEntity.getExperimentId()");
    }

    @Test
    public void buildContext_experimentModelMapping_experimentNameIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                EXP_NAME,
                context.getExperimentModel().getExperimentName(),
                "ExperimentModel.experimentName must be mapped from ExperimentEntity.getExperimentName()");
    }

    @Test
    public void buildContext_experimentModelMapping_projectIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                PROJECT_ID,
                context.getExperimentModel().getProjectId(),
                "ExperimentModel.projectId must be mapped from ExperimentEntity.getProjectId()");
    }

    @Test
    public void buildContext_experimentModelMapping_gatewayIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                GATEWAY_ID,
                context.getExperimentModel().getGatewayId(),
                "ExperimentModel.gatewayId must be mapped from ExperimentEntity.getGatewayId()");
    }

    @Test
    public void buildContext_experimentModelMapping_userNameIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                USER_NAME,
                context.getExperimentModel().getUserName(),
                "ExperimentModel.userName must be mapped from ExperimentEntity.getUserName()");
    }

    @Test
    public void buildContext_experimentModelMapping_descriptionIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                DESCRIPTION,
                context.getExperimentModel().getDescription(),
                "ExperimentModel.description must be mapped from ExperimentEntity.getDescription()");
    }

    @Test
    public void buildContext_experimentModelMapping_applicationIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                EXP_APP_ID,
                context.getExperimentModel().getApplicationId(),
                "ExperimentModel.applicationId must be mapped from ExperimentEntity.getApplicationId()");
    }

    @Test
    public void buildContext_experimentModelMapping_bindingIdIsCorrect() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                EXP_BINDING_ID,
                context.getExperimentModel().getBindingId(),
                "ExperimentModel.bindingId must be mapped from ExperimentEntity.getBindingId()");
    }

    @Test
    public void buildContext_experimentModelMapping_allEightFieldsMappedInSingleCall() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(EXPERIMENT_ID, context.getExperimentModel().getExperimentId(), "experimentId mismatch");
        assertEquals(EXP_NAME, context.getExperimentModel().getExperimentName(), "experimentName mismatch");
        assertEquals(PROJECT_ID, context.getExperimentModel().getProjectId(), "projectId mismatch");
        assertEquals(GATEWAY_ID, context.getExperimentModel().getGatewayId(), "gatewayId mismatch");
        assertEquals(USER_NAME, context.getExperimentModel().getUserName(), "userName mismatch");
        assertEquals(DESCRIPTION, context.getExperimentModel().getDescription(), "description mismatch");
        assertEquals(EXP_APP_ID, context.getExperimentModel().getApplicationId(), "applicationId mismatch");
        assertEquals(EXP_BINDING_ID, context.getExperimentModel().getBindingId(), "bindingId mismatch");
    }

    // -------------------------------------------------------------------------
    // 6. ExperimentModel is set on the returned TaskContext
    // -------------------------------------------------------------------------

    @Test
    public void buildContext_experimentModel_isNotNullOnContext() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertNotNull(context.getExperimentModel(), "context.getExperimentModel() must not be null after buildContext");
    }

    @Test
    public void buildContext_experimentModel_experimentIdMatchesProcessExperimentId() {
        when(processRepository.findById(PROCESS_ID)).thenReturn(Optional.of(buildProcessEntity()));
        when(experimentRepository.findById(EXPERIMENT_ID)).thenReturn(Optional.of(buildExperimentEntity()));

        TaskContext context = factory.buildContext(PROCESS_ID, GATEWAY_ID, TASK_ID);

        assertEquals(
                context.getProcessModel().getExperimentId(),
                context.getExperimentModel().getExperimentId(),
                "ExperimentModel.experimentId must match ProcessModel.experimentId, "
                        + "confirming the experiment was looked up with the correct key");
    }

    // -------------------------------------------------------------------------
    // Helpers — build real entity instances (avoids nested-mock Mockito issues)
    // -------------------------------------------------------------------------

    private ProcessEntity buildProcessEntity() {
        ProcessEntity entity = new ProcessEntity();
        entity.setProcessId(PROCESS_ID);
        entity.setExperimentId(EXPERIMENT_ID);
        entity.setApplicationId(APPLICATION_ID);
        entity.setResourceId(RESOURCE_ID);
        entity.setBindingId(BINDING_ID);
        return entity;
    }

    private ExperimentEntity buildExperimentEntity() {
        ExperimentEntity entity = new ExperimentEntity();
        entity.setExperimentId(EXPERIMENT_ID);
        entity.setExperimentName(EXP_NAME);
        entity.setProjectId(PROJECT_ID);
        entity.setGatewayId(GATEWAY_ID);
        entity.setUserName(USER_NAME);
        entity.setDescription(DESCRIPTION);
        entity.setApplicationId(EXP_APP_ID);
        entity.setBindingId(EXP_BINDING_ID);
        return entity;
    }
}
