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

import java.util.List;
import org.apache.airavata.config.TestBase;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.research.experiment.entity.ExperimentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for {@link ProcessService}.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessServiceTest extends TestBase {

    private final ProcessService processService;
    private String testExperimentId;
    private String otherExperimentId;

    public ProcessServiceTest(ProcessService processService) {
        this.processService = processService;
    }

    @BeforeEach
    void setUp() {
        testExperimentId = createExperiment();
        otherExperimentId = createExperiment();
    }

    private String createExperiment() {
        String id = "exp-" + java.util.UUID.randomUUID();
        ExperimentEntity exp = new ExperimentEntity();
        exp.setExperimentId(id);
        exp.setGatewayId("test-gw");
        exp.setUserName("test-user");
        exp.setExperimentName("Test Experiment");
        exp.setApplicationId("test-app");
        exp.setBindingId("test-binding");
        exp.setState("CREATED");
        entityManager.persist(exp);
        entityManager.flush();
        return id;
    }

    // ========== addProcess ==========

    @Test
    void addProcess_generatesIdWhenNull() throws Exception {
        ProcessModel model = new ProcessModel();

        String processId = processService.addProcess(model, testExperimentId);

        assertNotNull(processId);
        assertFalse(processId.isBlank());
    }

    @Test
    void addProcess_usesProvidedId() throws Exception {
        ProcessModel model = new ProcessModel();
        model.setProcessId("my-process-id");

        String processId = processService.addProcess(model, testExperimentId);

        assertEquals("my-process-id", processId);
    }

    @Test
    void addProcess_setsExperimentId() throws Exception {
        ProcessModel model = new ProcessModel();

        String processId = processService.addProcess(model, testExperimentId);
        flushAndClear();

        ProcessModel retrieved = processService.getProcess(processId);
        assertNotNull(retrieved);
        assertEquals(testExperimentId, retrieved.getExperimentId());
    }

    // ========== getProcess ==========

    @Test
    void getProcess_existingId_returnsModel() throws Exception {
        ProcessModel model = new ProcessModel();
        model.setApplicationId("app-1");
        model.setResourceId("resource-1");

        String processId = processService.addProcess(model, testExperimentId);
        flushAndClear();

        ProcessModel retrieved = processService.getProcess(processId);

        assertNotNull(retrieved);
        assertEquals(processId, retrieved.getProcessId());
        assertEquals("app-1", retrieved.getApplicationId());
        assertEquals("resource-1", retrieved.getResourceId());
    }

    @Test
    void getProcess_nonExistentId_returnsNull() throws Exception {
        ProcessModel result = processService.getProcess("nonexistent-" + java.util.UUID.randomUUID());
        assertNull(result);
    }

    // ========== getProcessList ==========

    @Test
    void getProcessList_byExperimentId_returnsProcesses() throws Exception {
        ProcessModel model1 = new ProcessModel();
        ProcessModel model2 = new ProcessModel();

        processService.addProcess(model1, testExperimentId);
        processService.addProcess(model2, testExperimentId);
        flushAndClear();

        List<ProcessModel> processes = processService.getProcessList(testExperimentId);

        assertEquals(2, processes.size());
    }

    @Test
    void getProcessList_differentExperiment_notIncluded() throws Exception {
        ProcessModel model1 = new ProcessModel();
        ProcessModel model2 = new ProcessModel();

        processService.addProcess(model1, testExperimentId);
        processService.addProcess(model2, otherExperimentId);
        flushAndClear();

        List<ProcessModel> processes = processService.getProcessList(testExperimentId);

        assertEquals(1, processes.size());
    }

    // ========== getProcessIds ==========

    @Test
    void getProcessIds_returnsOnlyIds() throws Exception {
        ProcessModel model = new ProcessModel();

        String processId = processService.addProcess(model, testExperimentId);
        flushAndClear();

        List<String> ids = processService.getProcessIds(testExperimentId);

        assertEquals(1, ids.size());
        assertEquals(processId, ids.get(0));
    }

    // ========== updateProcess ==========

    @Test
    void updateProcess_updatesFields() throws Exception {
        ProcessModel model = new ProcessModel();
        model.setApplicationId("app-old");

        String processId = processService.addProcess(model, testExperimentId);
        flushAndClear();

        ProcessModel updated = new ProcessModel();
        updated.setApplicationId("app-new");
        updated.setExperimentId(testExperimentId);
        processService.updateProcess(updated, processId);
        flushAndClear();

        ProcessModel retrieved = processService.getProcess(processId);
        assertEquals("app-new", retrieved.getApplicationId());
    }

    // ========== removeProcess ==========

    @Test
    void removeProcess_deletesRecord() throws Exception {
        ProcessModel model = new ProcessModel();

        String processId = processService.addProcess(model, testExperimentId);
        flushAndClear();

        assertNotNull(processService.getProcess(processId));

        processService.removeProcess(processId);
        flushAndClear();

        assertNull(processService.getProcess(processId));
    }

    // ========== Helper ==========

    private ProcessModel createTestProcess(String applicationId) throws Exception {
        ProcessModel model = new ProcessModel();
        model.setApplicationId(applicationId);
        String id = processService.addProcess(model, testExperimentId);
        model.setProcessId(id);
        return model;
    }
}
