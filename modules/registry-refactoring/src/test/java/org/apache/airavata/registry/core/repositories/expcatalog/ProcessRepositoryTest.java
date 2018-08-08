/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.job.JobModel;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.*;
import org.apache.airavata.model.task.TaskModel;
import org.apache.airavata.model.task.TaskTypes;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class ProcessRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRepositoryTest.class);

    private GatewayRepository gatewayRepository;
    private ProjectRepository projectRepository;
    private ExperimentRepository experimentRepository;
    private ProcessRepository processRepository;

    public ProcessRepositoryTest() {
        super(Database.EXP_CATALOG);
        gatewayRepository = new GatewayRepository();
        projectRepository = new ProjectRepository();
        experimentRepository = new ExperimentRepository();
        processRepository = new ProcessRepository();
    }

    private Gateway createSampleGateway(String tag) {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway" + tag);
        gateway.setDomain("SEAGRID" + tag);
        gateway.setEmailAddress("abc@d + " + tag + "+.com");
        return gateway;
    }

    private Project createSampleProject(String tag) {
        Project project = new Project();
        project.setName("projectName" + tag);
        project.setOwner("user" + tag);
        return project;
    }

    private ExperimentModel createSampleExperiment(String projectId, String gatewayId, String tag) {
        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user" + tag);
        experimentModel.setExperimentName("name" + tag);
        return experimentModel;
    }

    @Test
    public void addProcessRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setState(ProcessState.CREATED);
        processModel.addToProcessStatuses(processStatus);

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.addToTasks(task);

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.addToTaskStatuses(taskStatus);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType("inputP");
        processModel.addToProcessInputs(inputDataObjectProType);

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType("OutputP");
        processModel.addToProcessOutputs(outputDataObjectProType);

        processModel.addToProcessErrors(new ErrorModel());

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        processModel.setProcessResourceSchedule(computationalResourceSchedulingModel);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertNotNull(processId);
        assertEquals(1, experimentRepository.getExperiment(experimentId).getProcesses().size());
        assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        ProcessModel savedProcessModel = processRepository.getProcess(processId);
        ComputationalResourceSchedulingModel savedComputationalResourceSchedulingModel = processRepository.getProcessResourceSchedule(processId);

        //Fields excluded are tested again separately
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel, savedProcessModel, "__isset_bitfield", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "processResourceSchedule", "lastUpdateTime"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessStatuses(), savedProcessModel.getProcessStatuses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessInputs(), savedProcessModel.getProcessInputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessOutputs(), savedProcessModel.getProcessOutputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getTasks(), savedProcessModel.getTasks(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessErrors(), savedProcessModel.getProcessErrors(), "__isset_bitfield"));

        Assert.assertTrue(EqualsBuilder.reflectionEquals(computationalResourceSchedulingModel, savedComputationalResourceSchedulingModel, "__isset_bitfield"));
    }

    @Test
    public void updateProcessRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setState(ProcessState.CREATED);
        processModel.addToProcessStatuses(processStatus);

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.addToTasks(task);

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.addToTaskStatuses(taskStatus);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType("inputP");
        processModel.addToProcessInputs(inputDataObjectProType);

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType("OutputP");
        processModel.addToProcessOutputs(outputDataObjectProType);

        processModel.addToProcessErrors(new ErrorModel());

        String processId = processRepository.addProcess(processModel, experimentId);
        assertNotNull(processId);
        assertEquals(1, experimentRepository.getExperiment(experimentId).getProcesses().size());

        TaskModel jobSubmissionTask = new TaskModel();
        jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
        jobSubmissionTask.setTaskId("job-task-id");
        JobModel job = new JobModel();
        job.setProcessId(processId);
        job.setJobId("job-id");
        job.setJobDescription("job-description");
        JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
        jobStatus.setStatusId("submitted-job-status-id");
        job.addToJobStatuses(jobStatus);
        jobSubmissionTask.addToJobs(job);
        processModel.addToTasks(jobSubmissionTask);
        processRepository.updateProcess(processModel, processId);

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel.setQueueName("queue");
        processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        assertEquals("queue", processRepository.getProcessResourceSchedule(processId).getQueueName());

        ProcessModel savedProcessModel = processRepository.getProcess(processId);
        ComputationalResourceSchedulingModel savedComputationalResourceSchedulingModel = processRepository.getProcessResourceSchedule(processId);

        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel, savedProcessModel, "__isset_bitfield", "processResourceSchedule", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "lastUpdateTime"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessStatuses(), savedProcessModel.getProcessStatuses(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessInputs(), savedProcessModel.getProcessInputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessOutputs(), savedProcessModel.getProcessOutputs(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getTasks(), savedProcessModel.getTasks(), "__isset_bitfield"));
        Assert.assertTrue(EqualsBuilder.reflectionEquals(processModel.getProcessErrors(), savedProcessModel.getProcessErrors(), "__isset_bitfield"));

        Assert.assertTrue(EqualsBuilder.reflectionEquals(computationalResourceSchedulingModel, savedComputationalResourceSchedulingModel, "__isset_bitfield"));
    }

    @Test
    public void retrieveSingleRepositoryTest() throws RegistryException {
        List<ProcessModel> actualProcessModelList = new ArrayList<>();
        List<String> actualProcessIdList = new ArrayList<>();
        List<String> actualExperimentIdList = new ArrayList<>();
        List<ComputationalResourceSchedulingModel> actualComputationalResourceModelList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            ProcessStatus processStatus = new ProcessStatus();
            processStatus.setState(ProcessState.CREATED);
            processModel.addToProcessStatuses(processStatus);

            TaskModel task = new TaskModel();
            task.setTaskId("task-id" + i);
            task.setTaskType(TaskTypes.ENV_SETUP);
            processModel.addToTasks(task);

            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setStatusId("task-status-id" + i);
            task.addToTaskStatuses(taskStatus);

            InputDataObjectType inputDataObjectProType = new InputDataObjectType("inputP");
            processModel.addToProcessInputs(inputDataObjectProType);

            OutputDataObjectType outputDataObjectProType = new OutputDataObjectType("OutputP");
            processModel.addToProcessOutputs(outputDataObjectProType);

            processModel.addToProcessErrors(new ErrorModel());

            String processId = processRepository.addProcess(processModel, experimentId);
            assertNotNull(processId);
            assertEquals(1, experimentRepository.getExperiment(experimentId).getProcesses().size());

            TaskModel jobSubmissionTask = new TaskModel();
            jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
            jobSubmissionTask.setTaskId("job-task-id" + i);
            JobModel job = new JobModel();
            job.setProcessId(processId);
            job.setJobId("job-id" + i);
            job.setJobDescription("job-description" + i);
            JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
            jobStatus.setStatusId("submitted-job-status-id" + i);
            job.addToJobStatuses(jobStatus);
            jobSubmissionTask.addToJobs(job);
            processModel.addToTasks(jobSubmissionTask);
            processRepository.updateProcess(processModel, processId);

            ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
            assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

            computationalResourceSchedulingModel.setQueueName("queue");
            processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
            assertEquals("queue", processRepository.getProcessResourceSchedule(processId).getQueueName());

            actualComputationalResourceModelList.add(computationalResourceSchedulingModel);
            actualProcessModelList.add(processModel);
            actualProcessIdList.add(processId);
            actualExperimentIdList.add(experimentId);
        }

        for (int j = 0 ; j < 5; j++) {
            ProcessModel savedProcessModel = processRepository.getProcess(actualProcessIdList.get(j));
            List<ProcessModel> savedProcessModelList = processRepository.getProcessList(DBConstants.Process.EXPERIMENT_ID, actualExperimentIdList.get(j));
            Assert.assertEquals(1, savedProcessModelList.size());

            ProcessModel savedProcessListModel = savedProcessModelList.get(0);
            ProcessModel actualProcessModel = actualProcessModelList.get(j);
            ComputationalResourceSchedulingModel savedComputationalResourceSchedulingModel = processRepository.getProcessResourceSchedule(actualProcessIdList.get(j));

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel, savedProcessListModel, "__isset_bitfield", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "processResourceSchedule", "lastUpdateTime"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel, savedProcessModel, "__isset_bitfield", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "processResourceSchedule", "lastUpdateTime"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessStatuses(), savedProcessModel.getProcessStatuses(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessInputs(), savedProcessModel.getProcessInputs(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessOutputs(), savedProcessModel.getProcessOutputs(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getTasks(), savedProcessModel.getTasks(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessErrors(), savedProcessModel.getProcessErrors(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualComputationalResourceModelList.get(j), savedComputationalResourceSchedulingModel, "__isset_bitfield"));
        }
    }

    @Test
    public void retrieveMultipleRepositoryTest() throws RegistryException {
        List<String> actualProcessIdList = new ArrayList<>();
        List<String> actualExperimentIdList = new ArrayList<>();
        HashMap<String, ComputationalResourceSchedulingModel> actualComputationalResourceModelMap = new HashMap<>();
        HashMap<String, ProcessModel> actualProcessModelMapPid = new HashMap<>();
        HashMap<String, ProcessModel> actualProcessModelMapEid = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Gateway gateway = createSampleGateway("" + i);
            String gatewayId = gatewayRepository.addGateway(gateway);
            Assert.assertNotNull(gatewayId);

            Project project = createSampleProject("" + i);
            String projectId = projectRepository.addProject(project, gatewayId);
            Assert.assertNotNull(projectId);

            ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "" + i);
            String experimentId = experimentRepository.addExperiment(experimentModel);
            Assert.assertNotNull(experimentId);

            ProcessModel processModel = new ProcessModel(null, experimentId);
            ProcessStatus processStatus = new ProcessStatus();
            processStatus.setState(ProcessState.CREATED);
            processModel.addToProcessStatuses(processStatus);

            TaskModel task = new TaskModel();
            task.setTaskId("task-id" + i);
            task.setTaskType(TaskTypes.ENV_SETUP);
            processModel.addToTasks(task);

            TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
            taskStatus.setStatusId("task-status-id" + i);
            task.addToTaskStatuses(taskStatus);

            InputDataObjectType inputDataObjectProType = new InputDataObjectType("inputP");
            processModel.addToProcessInputs(inputDataObjectProType);

            OutputDataObjectType outputDataObjectProType = new OutputDataObjectType("OutputP");
            processModel.addToProcessOutputs(outputDataObjectProType);

            processModel.addToProcessErrors(new ErrorModel());

            String processId = processRepository.addProcess(processModel, experimentId);
            assertNotNull(processId);
            assertEquals(1, experimentRepository.getExperiment(experimentId).getProcesses().size());

            TaskModel jobSubmissionTask = new TaskModel();
            jobSubmissionTask.setTaskType(TaskTypes.JOB_SUBMISSION);
            jobSubmissionTask.setTaskId("job-task-id" + i);
            JobModel job = new JobModel();
            job.setProcessId(processId);
            job.setJobId("job-id" + i);
            job.setJobDescription("job-description" + i);
            JobStatus jobStatus = new JobStatus(JobState.SUBMITTED);
            jobStatus.setStatusId("submitted-job-status-id" + i);
            job.addToJobStatuses(jobStatus);
            jobSubmissionTask.addToJobs(job);
            processModel.addToTasks(jobSubmissionTask);
            processRepository.updateProcess(processModel, processId);

            ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
            assertEquals(processId, processRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

            computationalResourceSchedulingModel.setQueueName("queue");
            processRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
            assertEquals("queue", processRepository.getProcessResourceSchedule(processId).getQueueName());

            actualProcessIdList.add(processId);
            actualExperimentIdList.add(experimentId);
            actualProcessModelMapPid.put(processId, processModel);
            actualProcessModelMapEid.put(experimentId, processModel);
            actualComputationalResourceModelMap.put(processId, computationalResourceSchedulingModel);
        }

        for (int j = 0 ; j < 5; j++) {
            ProcessModel savedProcessModel = processRepository.getProcess(actualProcessIdList.get(j));
            List<ProcessModel> savedProcessModelList = processRepository.getProcessList(DBConstants.Process.EXPERIMENT_ID, actualExperimentIdList.get(j));
            Assert.assertEquals(1, savedProcessModelList.size());

            ProcessModel savedProcessListModel = savedProcessModelList.get(0);
            ProcessModel actualProcessModel = actualProcessModelMapPid.get(actualProcessIdList.get(j));
            ComputationalResourceSchedulingModel savedComputationalResourceSchedulingModel = processRepository.getProcessResourceSchedule(actualProcessIdList.get(j));

            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModelMapEid.get(actualExperimentIdList.get(j)), savedProcessListModel, "__isset_bitfield", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "processResourceSchedule", "lastUpdateTime"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel, savedProcessModel, "__isset_bitfield", "processStatuses", "processInputs", "processOutputs", "tasks", "processErrors", "processResourceSchedule", "lastUpdateTime"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessStatuses(), savedProcessModel.getProcessStatuses(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessInputs(), savedProcessModel.getProcessInputs(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessOutputs(), savedProcessModel.getProcessOutputs(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getTasks(), savedProcessModel.getTasks(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualProcessModel.getProcessErrors(), savedProcessModel.getProcessErrors(), "__isset_bitfield"));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualComputationalResourceModelMap.get(actualProcessIdList.get(j)), savedComputationalResourceSchedulingModel, "__isset_bitfield"));

            List<String> processIdList = processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, actualExperimentIdList.get(j));
            assertEquals(1, processIdList.size());
            assertEquals(processIdList.get(0), actualProcessIdList.get(j));
        }
    }

    @Test
    public void removeProcessRepositoryTest() throws RegistryException {
        Gateway gateway = createSampleGateway("1");
        String gatewayId = gatewayRepository.addGateway(gateway);
        Assert.assertNotNull(gatewayId);

        Project project = createSampleProject("1");
        String projectId = projectRepository.addProject(project, gatewayId);
        Assert.assertNotNull(projectId);

        ExperimentModel experimentModel = createSampleExperiment(projectId, gatewayId, "1");
        String experimentId = experimentRepository.addExperiment(experimentModel);
        Assert.assertNotNull(experimentId);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        ProcessStatus processStatus = new ProcessStatus();
        processStatus.setState(ProcessState.CREATED);
        processModel.addToProcessStatuses(processStatus);

        TaskModel task = new TaskModel();
        task.setTaskId("task-id");
        task.setTaskType(TaskTypes.ENV_SETUP);
        processModel.addToTasks(task);

        TaskStatus taskStatus = new TaskStatus(TaskState.CREATED);
        taskStatus.setStatusId("task-status-id");
        task.addToTaskStatuses(taskStatus);

        InputDataObjectType inputDataObjectProType = new InputDataObjectType("inputP");
        processModel.addToProcessInputs(inputDataObjectProType);

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType("OutputP");
        processModel.addToProcessOutputs(outputDataObjectProType);

        processModel.addToProcessErrors(new ErrorModel());

        String processId = processRepository.addProcess(processModel, experimentId);
        assertNotNull(processId);
        assertEquals(1, experimentRepository.getExperiment(experimentId).getProcesses().size());

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        assertFalse(processRepository.isProcessExist(processId));
    }

}
