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
package org.apache.airavata.execution.orchestration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.ValidationExceptions.LaunchValidationException;
import org.apache.airavata.core.exception.ValidationExceptions.ValidationResults;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.research.experiment.model.ExperimentModel;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.status.model.ErrorModel;
import org.apache.airavata.status.service.StatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pure unit tests for {@link ValidationService}.
 *
 * <p>All collaborators ({@link ServerProperties} and {@link StatusService}) are replaced with
 * Mockito mocks. No Spring context or database access is required.
 */
@ExtendWith(MockitoExtension.class)
public class ValidationServiceTest {

    @Mock
    private ServerProperties properties;

    @Mock
    private StatusService errorService;

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService(properties, errorService);
    }

    // ---------------------------------------------------------------------------
    // validateExperiment tests
    // ---------------------------------------------------------------------------

    @Test
    void validateExperiment_succeeds_whenStateIsCreated() {
        when(properties.validationEnabled()).thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-001");
        experiment.setState(ExperimentState.CREATED);

        ValidationResults result = assertDoesNotThrow(
                () -> validationService.validateExperiment(experiment),
                "Validation should pass for an experiment in CREATED state");

        assertNotNull(result, "ValidationResults must not be null");
        assertTrue(result.getValidationState(), "Validation state must be true for CREATED experiment");
        verifyNoInteractions(errorService);
    }

    @ParameterizedTest
    @EnumSource(value = ExperimentState.class, names = {"EXECUTING", "LAUNCHED"})
    void validateExperiment_throwsLaunchValidationException_whenStateIsNotCreated(ExperimentState state)
            throws Exception {
        when(properties.validationEnabled()).thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-002");
        experiment.setState(state);

        assertThrows(
                LaunchValidationException.class,
                () -> validationService.validateExperiment(experiment),
                "Validation must throw LaunchValidationException when experiment state is " + state);
    }

    @Test
    void validateExperiment_succeeds_whenValidationDisabled() {
        when(properties.validationEnabled()).thenReturn(false);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-003");
        experiment.setState(ExperimentState.EXECUTING);

        ValidationResults result = assertDoesNotThrow(
                () -> validationService.validateExperiment(experiment),
                "Validation should always pass when validation is disabled, regardless of state");

        assertNotNull(result, "ValidationResults must not be null");
        assertTrue(result.getValidationState(), "Validation state must be true when validation is disabled");
        verifyNoInteractions(errorService);
    }

    // ---------------------------------------------------------------------------
    // validateProcess tests
    // ---------------------------------------------------------------------------

    @Test
    void validateProcess_succeeds_whenExperimentStateIsCreated() {
        when(properties.validationEnabled()).thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-004");
        experiment.setState(ExperimentState.CREATED);

        ProcessModel process = new ProcessModel();
        process.setProcessId("proc-001");

        ValidationResults result = assertDoesNotThrow(
                () -> validationService.validateProcess(experiment, process),
                "Process validation should pass when experiment is in CREATED state");

        assertNotNull(result, "ValidationResults must not be null");
        assertTrue(result.getValidationState(), "Validation state must be true for CREATED experiment");
        verifyNoInteractions(errorService);
    }

    @ParameterizedTest
    @EnumSource(value = ExperimentState.class, names = {"EXECUTING", "LAUNCHED"})
    void validateProcess_throwsLaunchValidationException_whenStateIsNotCreated(ExperimentState state)
            throws Exception {
        when(properties.validationEnabled()).thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-005");
        experiment.setState(state);

        ProcessModel process = new ProcessModel();
        process.setProcessId("proc-002");

        assertThrows(
                LaunchValidationException.class,
                () -> validationService.validateProcess(experiment, process),
                "Process validation must throw LaunchValidationException when experiment state is " + state);
    }

    @Test
    void validateProcess_recordsError_whenValidationFails() throws Exception {
        when(properties.validationEnabled()).thenReturn(true);

        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentId("exp-006");
        experiment.setState(ExperimentState.EXECUTING);

        ProcessModel process = new ProcessModel();
        process.setProcessId("proc-003");

        // The call is expected to throw because validation fails; we capture the exception and
        // then verify that errorService.addProcessError was called with the correct process ID.
        assertThrows(
                LaunchValidationException.class,
                () -> validationService.validateProcess(experiment, process));

        verify(errorService).addProcessError(any(ErrorModel.class), eq("proc-003"));
    }
}
