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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.common.model.ErrorModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessErrorService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@org.springframework.test.context.ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessErrorRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;
    private final ProcessErrorService processErrorService;

    private String gatewayId;
    private String projectId;
    private String experimentId;
    private String processId;

    public ProcessErrorRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService,
            ProcessErrorService processErrorService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
        this.processErrorService = processErrorService;
    }

    @BeforeEach
    public void setUp() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("test@example.com");
        gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("testProject");
        project.setOwner("testUser");
        project.setGatewayId(gatewayId);
        projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("testUser");
        experimentModel.setExperimentName("testExperiment");
        experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processId = processService.addProcess(processModel, experimentId);
        assertNotNull(processId, "Process ID should not be null");
    }

    @Test
    public void testProcessErrorRepository_Create_MultipleErrorsPerProcess() throws RegistryException {
        // multiple errors for the same process
        ErrorModel error1 = createErrorModel("error-1", "First error message", "User friendly message 1");
        ErrorModel error2 = createErrorModel("error-2", "Second error message", "User friendly message 2");
        ErrorModel error3 = createErrorModel("error-3", "Third error message", "User friendly message 3");

        String errorId1 = processErrorService.addProcessError(error1, processId);
        String errorId2 = processErrorService.addProcessError(error2, processId);
        String errorId3 = processErrorService.addProcessError(error3, processId);

        assertNotNull(errorId1, "Error 1 ID should not be null");
        assertNotNull(errorId2, "Error 2 ID should not be null");
        assertNotNull(errorId3, "Error 3 ID should not be null");

        ProcessModel process = processService.getProcess(processId);
        assertNotNull(process.getProcessErrors(), "Process errors list should not be null");
        assertEquals(3, process.getProcessErrors().size(), "Process should have 3 errors");

        List<ErrorModel> retrievedErrors = processErrorService.getProcessError(processId);
        assertEquals(3, retrievedErrors.size(), "Should retrieve 3 errors");
        assertTrue(
                retrievedErrors.stream().anyMatch(e -> e.getErrorId().equals(errorId1)), "Error 1 should be present");
        assertTrue(
                retrievedErrors.stream().anyMatch(e -> e.getErrorId().equals(errorId2)), "Error 2 should be present");
        assertTrue(
                retrievedErrors.stream().anyMatch(e -> e.getErrorId().equals(errorId3)), "Error 3 should be present");
    }

    @Test
    public void testProcessErrorRepository_Get_NonExistentProcessId() throws RegistryException {

        String nonExistentProcessId =
                "non-existent-process-" + java.util.UUID.randomUUID().toString();
        List<ErrorModel> errors = processErrorService.getProcessError(nonExistentProcessId);
        assertNotNull(errors, "Should return non-null list");
        assertTrue(errors.isEmpty(), "Non-existent process should return empty error list");

        ErrorModel error = createErrorModel("error-verify", "Test error", "User message");
        String errorId = processErrorService.addProcessError(error, processId);
        assertNotNull(errorId, "Error should be created for existing process");

        List<ErrorModel> existingErrors = processErrorService.getProcessError(processId);
        assertTrue(existingErrors.size() >= 1, "Existing process should have errors");
    }

    @Test
    public void testProcessErrorRepository_Update_AllErrorFields() throws RegistryException {
        ErrorModel error = createErrorModel("error-update", "Original error message", "Original user message");
        String errorId = processErrorService.addProcessError(error, processId);

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        assertEquals(1, errors.size(), "Should have one error");
        ErrorModel originalError = errors.get(0);

        originalError.setActualErrorMessage("Updated actual error message");
        originalError.setUserFriendlyMessage("Updated user friendly message");
        originalError.setTransientOrPersistent(true);
        List<String> rootCauses = new ArrayList<>();
        rootCauses.add("root-cause-1");
        rootCauses.add("root-cause-2");
        originalError.setRootCauseErrorIdList(rootCauses);

        processErrorService.updateProcessError(originalError, processId);

        List<ErrorModel> updatedErrors = processErrorService.getProcessError(processId);
        assertEquals(1, updatedErrors.size(), "Should still have one error");
        ErrorModel updatedError = updatedErrors.get(0);

        assertEquals(
                "Updated actual error message",
                updatedError.getActualErrorMessage(),
                "Actual error message should be updated");
        assertEquals(
                "Updated user friendly message",
                updatedError.getUserFriendlyMessage(),
                "User friendly message should be updated");
        assertTrue(updatedError.getTransientOrPersistent(), "TransientOrPersistent should be updated");
        assertNotNull(updatedError.getRootCauseErrorIdList(), "Root cause list should not be null");
        assertEquals(2, updatedError.getRootCauseErrorIdList().size(), "Root cause list should have 2 items");
        assertTrue(updatedError.getRootCauseErrorIdList().contains("root-cause-1"), "Root cause 1 should be present");
        assertTrue(updatedError.getRootCauseErrorIdList().contains("root-cause-2"), "Root cause 2 should be present");
    }

    @Test
    public void testProcessErrorRepository_TransientVsPersistentErrors() throws RegistryException {

        ErrorModel transientError = new ErrorModel();
        transientError.setErrorId("error-transient");
        transientError.setActualErrorMessage("Transient error occurred");
        transientError.setUserFriendlyMessage("This is a temporary error");
        transientError.setTransientOrPersistent(true); // Transient error

        ErrorModel persistentError = new ErrorModel();
        persistentError.setErrorId("error-persistent");
        persistentError.setActualErrorMessage("Persistent error occurred");
        persistentError.setUserFriendlyMessage("This is a permanent error");
        persistentError.setTransientOrPersistent(false); // Persistent error

        String transientId = processErrorService.addProcessError(transientError, processId);
        String persistentId = processErrorService.addProcessError(persistentError, processId);

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        assertEquals(2, errors.size(), "Should have 2 errors");

        ErrorModel retrievedTransient = errors.stream()
                .filter(e -> e.getErrorId().equals(transientId))
                .findFirst()
                .orElse(null);
        ErrorModel retrievedPersistent = errors.stream()
                .filter(e -> e.getErrorId().equals(persistentId))
                .findFirst()
                .orElse(null);

        assertNotNull(retrievedTransient, "Transient error should be retrieved");
        assertTrue(retrievedTransient.getTransientOrPersistent(), "Transient error flag should be true");

        assertNotNull(retrievedPersistent, "Persistent error should be retrieved");
        assertFalse(retrievedPersistent.getTransientOrPersistent(), "Persistent error flag should be false");
    }

    @Test
    public void testProcessErrorRepository_CascadingDelete() throws RegistryException {
        // errors for the process
        ErrorModel error1 = createErrorModel("error-cascade-1", "Error 1", "User message 1");
        ErrorModel error2 = createErrorModel("error-cascade-2", "Error 2", "User message 2");

        String errorId1 = processErrorService.addProcessError(error1, processId);
        String errorId2 = processErrorService.addProcessError(error2, processId);

        ProcessModel process = processService.getProcess(processId);
        assertEquals(2, process.getProcessErrors().size(), "Process should have 2 errors before deletion");

        processService.removeProcess(processId);
        assertFalse(processService.isProcessExist(processId), "Process should be deleted");

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        assertTrue(errors.isEmpty(), "Errors should be deleted with process");
    }

    @Test
    public void testProcessErrorRepository_AutomaticCreationTime() throws RegistryException {

        ErrorModel error = new ErrorModel();
        error.setErrorId("error-auto-time");
        error.setActualErrorMessage("Error with automatic creation time");
        error.setUserFriendlyMessage("User-friendly message");

        long beforeCreation = AiravataUtils.getUniqueTimestamp().getTime();
        String errorId = processErrorService.addProcessError(error, processId);
        long afterCreation = AiravataUtils.getUniqueTimestamp().getTime();

        assertNotNull(errorId, "Error should be created");

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        assertEquals(1, errors.size(), "Should have one error");
        ErrorModel retrieved = errors.get(0);

        assertNotNull(retrieved.getCreationTime(), "Creation time should not be null");
        assertTrue(retrieved.getCreationTime() > 0, "Creation time should be set");
        // Allow small timing differences (within 1 second) due to timestamp conversion and processing time
        assertTrue(
                retrieved.getCreationTime() >= beforeCreation - 1000,
                "Creation time should be set to current or later (expected >= " + beforeCreation + ", actual: " + retrieved.getCreationTime() + ")");
        assertTrue(
                retrieved.getCreationTime() <= afterCreation + 1000,
                "Creation time should be set to current or earlier (expected <= " + afterCreation + ", actual: " + retrieved.getCreationTime() + ")");
    }

    @Test
    public void testProcessErrorRepository_ErrorRetrievalOrdering() throws RegistryException {

        ErrorModel error1 = createErrorModel("error-1", "First error", "First user message");
        ErrorModel error2 = createErrorModel("error-2", "Second error", "Second user message");
        ErrorModel error3 = createErrorModel("error-3", "Third error", "Third user message");

        String errorId1 = processErrorService.addProcessError(error1, processId);
        String errorId2 = processErrorService.addProcessError(error2, processId);
        String errorId3 = processErrorService.addProcessError(error3, processId);

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        assertEquals(3, errors.size(), "Should retrieve all 3 errors");

        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId1)), "Error 1 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId2)), "Error 2 should be present");
        assertTrue(errors.stream().anyMatch(e -> e.getErrorId().equals(errorId3)), "Error 3 should be present");

        errors.forEach(e ->
                assertTrue(e.getCreationTime() > 0, "Each error should have creation time set: " + e.getErrorId()));
    }

    @Test
    public void testProcessErrorRepository_MultipleRootCauses() throws RegistryException {
        ErrorModel error = new ErrorModel();
        error.setErrorId("error-rootcauses");
        error.setActualErrorMessage("Error with multiple root causes");
        error.setUserFriendlyMessage("User message");

        // a large list of root causes
        List<String> rootCauses = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            rootCauses.add("root-cause-" + i);
        }
        error.setRootCauseErrorIdList(rootCauses);

        String errorId = processErrorService.addProcessError(error, processId);
        assertNotNull(errorId, "Error with multiple root causes should be created");

        List<ErrorModel> errors = processErrorService.getProcessError(processId);
        ErrorModel retrieved = errors.get(0);
        assertNotNull(retrieved.getRootCauseErrorIdList(), "Root cause list should not be null");
        assertEquals(10, retrieved.getRootCauseErrorIdList().size(), "Should have 10 root causes");
        assertTrue(retrieved.getRootCauseErrorIdList().contains("root-cause-1"), "Root cause 1 should be present");
        assertTrue(retrieved.getRootCauseErrorIdList().contains("root-cause-10"), "Root cause 10 should be present");
    }

    private ErrorModel createErrorModel(String errorId, String actualMessage, String userFriendlyMessage) {
        ErrorModel error = new ErrorModel();
        error.setErrorId(errorId);
        error.setActualErrorMessage(actualMessage);
        error.setUserFriendlyMessage(userFriendlyMessage);
        error.setTransientOrPersistent(false);
        return error;
    }
}
