/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.ComputationalResourceSchedulingModel;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ExperimentType;
import org.apache.airavata.common.model.Gateway;
import org.apache.airavata.common.model.JobModel;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.JobStatus;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.Project;
import org.apache.airavata.common.model.TaskModel;
import org.apache.airavata.common.model.TaskState;
import org.apache.airavata.common.model.TaskStatus;
import org.apache.airavata.common.model.TaskTypes;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.ExperimentService;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.ProcessService;
import org.apache.airavata.registry.services.ProjectService;
import org.apache.airavata.registry.utils.DBConstants;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class ProcessRepositoryTest extends TestBase {

    private final GatewayService gatewayService;
    private final ProjectService projectService;
    private final ExperimentService experimentService;
    private final ProcessService processService;

    public ProcessRepositoryTest(
            GatewayService gatewayService,
            ProjectService projectService,
            ExperimentService experimentService,
            ProcessService processService) {
        this.gatewayService = gatewayService;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.processService = processService;
    }

    @Test
    public void testProcessRepository() throws Exception {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway-" + java.util.UUID.randomUUID().toString());
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayService.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);
        String projectId = projectService.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");
        String experimentId = experimentService.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel();
        processModel.setExperimentId(experimentId);
        processModel.setApplicationInterfaceId("interface-id");
        processModel.setApplicationDeploymentId("deployment-id");
        processModel.setEnableEmailNotification(true);
        processModel.setEmailAddresses(java.util.Arrays.asList("email@test.com"));

        ComputationalResourceSchedulingModel scheduling = new ComputationalResourceSchedulingModel();
        scheduling.setResourceHostId("host-id");
        scheduling.setQueueName("queue");
        processModel.setProcessResourceSchedule(scheduling);

        String processId = processService.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(processService.isProcessExist(processId));

        ProcessModel retrievedProcess = processService.getProcess(processId);
        assertEquals("interface-id", retrievedProcess.getApplicationInterfaceId());
        assertEquals("deployment-id", retrievedProcess.getApplicationDeploymentId());
        assertTrue(retrievedProcess.getEnableEmailNotification());
        assertEquals(1, retrievedProcess.getEmailAddresses().size());
        assertEquals(ProcessState.CREATED, retrievedProcess.getProcessStatuses().get(0).getState());

        List<String> processIds = processService.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        assertEquals(1, processIds.size());

        processService.removeProcess(processId);
        assertFalse(processService.isProcessExist(processId));
    }
}
