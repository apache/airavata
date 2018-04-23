package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.process.ProcessModel;
import org.apache.airavata.model.scheduling.ComputationalResourceSchedulingModel;
import org.apache.airavata.model.status.ProcessState;
import org.apache.airavata.model.status.ProcessStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ProcessRepositoryTest {

    private static Initialize initialize;
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    ExperimentRepository experimentRepository;
    ProcessRepository processRepository;
    ProcessResourceScheduleRepository processResourceScheduleRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProcessRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
            experimentRepository = new ExperimentRepository();
            processRepository = new ProcessRepository();
            processResourceScheduleRepository = new ProcessResourceScheduleRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void ProcessRepositoryTest() throws RegistryException {
        Gateway gateway = new Gateway();
        gateway.setGatewayId("gateway");
        gateway.setDomain("SEAGRID");
        gateway.setEmailAddress("abc@d.com");
        String gatewayId = gatewayRepository.addGateway(gateway);

        Project project = new Project();
        project.setName("projectName");
        project.setOwner("user");
        project.setGatewayId(gatewayId);

        String projectId = projectRepository.addProject(project, gatewayId);

        ExperimentModel experimentModel = new ExperimentModel();
        experimentModel.setProjectId(projectId);
        experimentModel.setGatewayId(gatewayId);
        experimentModel.setExperimentType(ExperimentType.SINGLE_APPLICATION);
        experimentModel.setUserName("user");
        experimentModel.setExperimentName("name");

        String experimentId = experimentRepository.addExperiment(experimentModel);

        ProcessModel processModel = new ProcessModel(null, experimentId);
        String processId = processRepository.addProcess(processModel, experimentId);
        assertTrue(processId != null);
        assertTrue(experimentRepository.getExperiment(experimentId).getProcesses().size() == 1);

        processModel.setProcessDetail("detail");
        processRepository.updateProcess(processModel, processId);

        ProcessModel retrievedProcessModel = processRepository.getProcess(processId);
        assertEquals(experimentId, retrievedProcessModel.getExperimentId());
        assertEquals("detail", retrievedProcessModel.getProcessDetail());

        ComputationalResourceSchedulingModel computationalResourceSchedulingModel = new ComputationalResourceSchedulingModel();
        assertEquals(processId, processResourceScheduleRepository.addProcessResourceSchedule(computationalResourceSchedulingModel, processId));

        computationalResourceSchedulingModel.setQueueName("queue");
        processResourceScheduleRepository.updateProcessResourceSchedule(computationalResourceSchedulingModel, processId);
        assertEquals("queue", processResourceScheduleRepository.getProcessResourceSchedule(processId).getQueueName());

        InputDataObjectType inputDataObjectProType = new InputDataObjectType();
        inputDataObjectProType.setName("inputP");

        List<InputDataObjectType> inputDataObjectTypeProList = new ArrayList<>();
        inputDataObjectTypeProList.add(inputDataObjectProType);

        OutputDataObjectType outputDataObjectProType = new OutputDataObjectType();
        outputDataObjectProType.setName("outputP");

        List<OutputDataObjectType> outputDataObjectTypeProList = new ArrayList<>();
        outputDataObjectTypeProList.add(outputDataObjectProType);

        assertEquals(processId, processRepository.addProcessInputs(inputDataObjectTypeProList, processId));

        inputDataObjectProType.setValue("iValueP");
        processRepository.updateProcessInputs(inputDataObjectTypeProList, processId);

        List<InputDataObjectType> retrievedProInputsList = processRepository.getProcessInputs(processId);
        assertTrue(retrievedProInputsList.size() == 1);
        assertEquals("iValueP", retrievedProInputsList.get(0).getValue());

        assertEquals(processId, processRepository.addProcessOutputs(outputDataObjectTypeProList, processId));

        outputDataObjectProType.setValue("oValueP");
        processRepository.updateProcessOutputs(outputDataObjectTypeProList, processId);

        List<OutputDataObjectType> retrievedProOutputList = processRepository.getProcessOutputs(processId);
        assertTrue(retrievedProOutputList.size() == 1);
        assertEquals("oValueP", retrievedProOutputList.get(0).getValue());

        ProcessStatus processStatus = new ProcessStatus(ProcessState.CREATED);
        assertEquals(processId, processRepository.addProcessStatus(processStatus, processId));

        processStatus.setState(ProcessState.EXECUTING);
        processRepository.updateProcessStatus(processStatus, processId);

        List<ProcessStatus> retrievedStatusList = processRepository.getProcessStatus(processId);
//        assertTrue(retrievedStatusList.size() == 1);
//        assertEquals(ProcessState.EXECUTING, retrievedStatusList.get(0).getState());

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        assertEquals(processId, processRepository.addProcessError(errorModel, processId));

        errorModel.setActualErrorMessage("message");
        processRepository.updateProcessError(errorModel, processId);

        List<ErrorModel> retrievedErrorList = processRepository.getProcessError(processId);
        assertTrue(retrievedErrorList.size() == 1);
        assertEquals("message", retrievedErrorList.get(0).getActualErrorMessage());

        List<String> processIdList = processRepository.getProcessIds(DBConstants.Process.EXPERIMENT_ID, experimentId);
        assertTrue(processIdList.size() == 1);
        assertTrue(processIdList.get(0).equals(processId));

        experimentRepository.removeExperiment(experimentId);
        processRepository.removeProcess(processId);
        assertFalse(processRepository.isProcessExist(processId));
    }

}
