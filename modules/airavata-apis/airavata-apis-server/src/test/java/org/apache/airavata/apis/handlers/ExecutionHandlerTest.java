package org.apache.airavata.apis.handlers;

import org.apache.airavata.api.execution.ExperimentRegisterRequest;
import org.apache.airavata.api.execution.ExperimentRegisterResponse;
import org.apache.airavata.api.execution.stubs.*;
import org.apache.airavata.apis.db.entity.ApplicationRunInfoEntity;
import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.entity.RunConfigurationEntity;
import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@Transactional
public class ExecutionHandlerTest {

    @Autowired
    ExecutionHandler executionHandler;

    @Autowired
    ExperimentRepository experimentRepository;

    @Test
    void testExperimentMapping() {

        // TODO: ec2
        // TODO: local
        ServerBackend serverBackend = ServerBackend.newBuilder()
                .setHostName("server-hostname")
                .build();
        Application application = Application.newBuilder()
                .setName("test-application")
                .build();
        ApplicationRunInfo applicationRunInfo = ApplicationRunInfo.newBuilder()
                .setApplication(application)
                .build();
        FileLocation sourceLocation = FileLocation.newBuilder()
                .setStorageId("source-location-storage-id")
                .build();
        InDataMovement inDataMovement = InDataMovement.newBuilder()
                .setInputIndex(1)
                .setSourceLocation(sourceLocation)
                .build();
        DataMovementConfiguration dataMovementConfiguration = DataMovementConfiguration.newBuilder()
                .addInMovements(inDataMovement)
                .build();
        RunConfiguration runConfiguration = RunConfiguration.newBuilder()
                .setServer(serverBackend)
                .setAppRunInfo(applicationRunInfo)
                .addDataMovementConfigs(dataMovementConfiguration)
                .build();
        Experiment experiment = Experiment.newBuilder()
                .setCreationTime(System.currentTimeMillis())
                .setDescription("Sample Exp")
                .setExperimentName("Exp Name")
                .setGatewayId("gateway-id")
                .setProjectId("project-id")
                .addRunConfigs(runConfiguration)
                .build();

        ExperimentRegisterRequest experimentRegisterRequest = ExperimentRegisterRequest.newBuilder()
                .setExperiment(experiment).build();

        TestStreamObserver<ExperimentRegisterResponse> responseObserver = new TestStreamObserver<>();
        executionHandler.registerExperiment(experimentRegisterRequest, responseObserver);

        assertTrue(responseObserver.isCompleted());
        String experimentId = responseObserver.getNext().getExperimentId();
        ExperimentEntity experimentEntity = experimentRepository.findById(experimentId).get();

        assertEquals(experiment.getCreationTime(), experimentEntity.getCreationTime());
        assertEquals(experiment.getDescription(), experimentEntity.getDescription());
        assertEquals(experiment.getExperimentName(), experimentEntity.getExperimentName());
        assertEquals(experiment.getGatewayId(), experimentEntity.getGatewayId());
        assertEquals(experiment.getProjectId(), experimentEntity.getProjectId());

        // RunConfiguration
        assertEquals(experiment.getRunConfigsCount(), experimentEntity.getRunConfigs().size());
        RunConfigurationEntity runConfigEntity = experimentEntity.getRunConfigs().get(0);

        // ComputeBackend
        ComputeBackendEntity computeBackendEntity = runConfigEntity.getComputeBackend();
        assertTrue(computeBackendEntity instanceof ServerBackendEntity);
        ServerBackendEntity serverBackendEntity = (ServerBackendEntity) computeBackendEntity;
        assertEquals(serverBackend.getHostName(), serverBackendEntity.getHostName());

        // ApplicationRunInfo
        ApplicationRunInfoEntity applicationRunInfoEntity = runConfigEntity.getAppRunInfo();
        ApplicationEntity applicationEntity = applicationRunInfoEntity.getApplication();
        assertEquals(application.getName(), applicationEntity.getName());

        // DataMovementConfiguration
        assertEquals(runConfiguration.getDataMovementConfigsCount(), runConfigEntity.getDataMovementConfigs().size());
        DataMovementConfigurationEntity dataMovementConfigurationEntity = runConfigEntity.getDataMovementConfigs()
                .get(0);
        assertEquals(dataMovementConfiguration.getInMovementsCount(),
                dataMovementConfigurationEntity.getInMovements().size());

        // InDataMovement
        InDataMovementEntity inDataMovementEntity = dataMovementConfigurationEntity.getInMovements().iterator().next();
        assertEquals(inDataMovement.getInputIndex(), inDataMovementEntity.getInputIndex());
        assertEquals(inDataMovement.getSourceLocation().getStorageId(),
                inDataMovementEntity.getSourceLocation().getStorageId());
    }
}
