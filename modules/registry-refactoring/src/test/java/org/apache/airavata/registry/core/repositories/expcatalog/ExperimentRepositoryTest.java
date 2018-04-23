package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.application.io.InputDataObjectType;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.model.commons.ErrorModel;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentType;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.Project;
import org.apache.airavata.registry.core.repositories.expcatalog.util.Initialize;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.cpi.RegistryException;
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

public class ExperimentRepositoryTest {

    private static Initialize initialize;
    GatewayRepository gatewayRepository;
    ProjectRepository projectRepository;
    UserConfigurationDataRepository userConfigurationDataRepository;
    ExperimentRepository experimentRepository;
    private static final Logger logger = LoggerFactory.getLogger(ExperimentRepositoryTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("expcatalog-derby.sql");
            initialize.initializeDB();
            gatewayRepository = new GatewayRepository();
            projectRepository = new ProjectRepository();
            userConfigurationDataRepository = new UserConfigurationDataRepository();
            experimentRepository = new ExperimentRepository();
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
    public void ExperimentRepositoryTest() throws RegistryException {
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
        assertTrue(experimentId != null);

        experimentModel.setDescription("description");
        experimentRepository.updateExperiment(experimentModel, experimentId);

        ExperimentModel retrievedExperimentModel = experimentRepository.getExperiment(experimentId);
        assertEquals(experimentModel.getDescription(), retrievedExperimentModel.getDescription());

        UserConfigurationDataModel userConfigurationDataModel = new UserConfigurationDataModel();
        userConfigurationDataModel.setAiravataAutoSchedule(true);
        userConfigurationDataModel.setOverrideManualScheduledParams(false);
        assertEquals(experimentId, userConfigurationDataRepository.addUserConfigurationData(userConfigurationDataModel, experimentId));

        userConfigurationDataModel.setStorageId("storage2");
        userConfigurationDataRepository.updateUserConfigurationData(userConfigurationDataModel, experimentId);

        assertEquals("storage2", userConfigurationDataRepository.getUserConfigurationData(experimentId).getStorageId());

        InputDataObjectType inputDataObjectTypeExp = new InputDataObjectType();
        inputDataObjectTypeExp.setName("inputE");

        List<InputDataObjectType> inputDataObjectTypeExpList = new ArrayList<>();
        inputDataObjectTypeExpList.add(inputDataObjectTypeExp);

        OutputDataObjectType outputDataObjectTypeExp = new OutputDataObjectType();
        outputDataObjectTypeExp.setName("outputE");

        List<OutputDataObjectType> outputDataObjectTypeExpList = new ArrayList<>();
        outputDataObjectTypeExpList.add(outputDataObjectTypeExp);

        assertEquals(experimentId, experimentRepository.addExperimentInputs(inputDataObjectTypeExpList, experimentId));

        inputDataObjectTypeExp.setValue("iValueE");
        experimentRepository.updateExperimentInputs(inputDataObjectTypeExpList, experimentId);

        List<InputDataObjectType> retrievedExpInputsList = experimentRepository.getExperimentInputs(experimentId);
        assertTrue(retrievedExpInputsList.size() == 1);
        assertEquals("iValueE", retrievedExpInputsList.get(0).getValue());

        assertEquals(experimentId, experimentRepository.addExperimentOutputs(outputDataObjectTypeExpList, experimentId));

        outputDataObjectTypeExp.setValue("oValueE");
        experimentRepository.updateExperimentOutputs(outputDataObjectTypeExpList, experimentId);

        List<OutputDataObjectType> retrievedExpOutputList = experimentRepository.getExperimentOutputs(experimentId);
        assertTrue(retrievedExpOutputList.size() == 1);
        assertEquals("oValueE", retrievedExpOutputList.get(0).getValue());

        ExperimentStatus experimentStatus = new ExperimentStatus(ExperimentState.CREATED);
        assertEquals(experimentId, experimentRepository.addExperimentStatus(experimentStatus, experimentId));

        experimentStatus.setState(ExperimentState.EXECUTING);
        experimentRepository.updateExperimentStatus(experimentStatus, experimentId);

        List<ExperimentStatus> retrievedExpStatusList = experimentRepository.getExperimentStatus(experimentId);
//        assertTrue(retrievedExpStatusList.size() == 1);
//        assertEquals(ExperimentState.EXECUTING, retrievedExpStatusList.get(0).getState());

        ErrorModel errorModel = new ErrorModel();
        errorModel.setErrorId("error");

        assertEquals(experimentId, experimentRepository.addExperimentError(errorModel, experimentId));

        errorModel.setActualErrorMessage("message");
        experimentRepository.updateExperimentError(errorModel, experimentId);

        List<ErrorModel> retrievedErrorList = experimentRepository.getExperimentErrors(experimentId);
        assertTrue(retrievedErrorList.size() == 1);
        assertEquals("message", retrievedErrorList.get(0).getActualErrorMessage());

        List<String> experimentIdList = experimentRepository.getExperimentIDs(DBConstants.Experiment.GATEWAY_ID, gatewayId);
        assertTrue(experimentIdList.size() == 1);
        assertTrue(experimentIdList.get(0).equals(experimentId));

        experimentRepository.removeExperiment(experimentId);
        assertFalse(experimentRepository.isExperimentExist(experimentId));
    }

}
