package org.apache.airavata.apis.handlers;

import org.apache.airavata.api.execution.ExperimentRegisterRequest;
import org.apache.airavata.api.execution.ExperimentRegisterResponse;
import org.apache.airavata.api.execution.stubs.*;
import org.apache.airavata.apis.db.entity.ApplicationRunInfoEntity;
import org.apache.airavata.apis.db.entity.DataMovementConfigurationEntity;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.entity.RunConfigurationEntity;
import org.apache.airavata.apis.db.entity.application.ApplicationEntity;
import org.apache.airavata.apis.db.entity.application.input.ApplicationInputEntity;
import org.apache.airavata.apis.db.entity.application.input.CommandLineInputEntity;
import org.apache.airavata.apis.db.entity.application.input.EnvironmentInputEntity;
import org.apache.airavata.apis.db.entity.application.input.FileInputEntity;
import org.apache.airavata.apis.db.entity.application.output.ApplicationOutputEntity;
import org.apache.airavata.apis.db.entity.application.output.FileOutputEntity;
import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.apache.airavata.apis.db.entity.backend.iface.SCPInterfaceEntity;
import org.apache.airavata.apis.db.entity.backend.iface.SSHInterfaceEntity;
import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class ExecutionHandlerTest {

    @Autowired
    ExecutionHandler executionHandler;

    @Autowired
    ExperimentRepository experimentRepository;

    @Test
    void testExperimentMapping() {

        SSHInterface sshInterface = SSHInterface.newBuilder().setHostName("ssh-hostname").setPort(2222)
                .setSshCredentialId("ssh-credential-id").build();
        SCPInterface scpInterface = SCPInterface.newBuilder().setHostName("scp-hostname").setPort(4422)
                .setSshCredentialId("ssh-credential-id").build();
        ServerBackend serverBackend = ServerBackend.newBuilder().setHostName("server-hostname").setPort(1888)
                .setCommandInterface(sshInterface).setDataInterface(scpInterface)
                .setWorkingDirectory("/tmp/working-directory/some_wd/").build();
        // ApplicationInputs
        CommandLineInput commandLineInput = CommandLineInput.newBuilder().setPosition(1).setPrefix("--prefix")
                .setValue("input-value").build();
        ApplicationInput applicationInput = ApplicationInput.newBuilder().setIndex(1)
                .setCommandLineInput(commandLineInput).setRequired(true).build();
        FileInput fileInput = FileInput.newBuilder().setFriendlyName("config file")
                .setDestinationPath("/scratch/workdirs/W123/config.conf").build();
        ApplicationInput applicationInput2 = ApplicationInput.newBuilder().setIndex(2).setFileInput(fileInput)
                .setRequired(true).build();
        EnvironmentInput environmentInput = EnvironmentInput.newBuilder().setKey("env-key").setValue("env-value")
                .build();
        ApplicationInput applicationInput3 = ApplicationInput.newBuilder().setIndex(3)
                .setEnvironmentInput(environmentInput).setRequired(false).build();
        // ApplicationOutputs
        // TODO: add StandardOut and StandardError outputs as well
        FileOutput fileOutput = FileOutput.newBuilder().setFriendlyName("output-file")
                .setDestinationPath("/scratch/workdir/output.file").build();
        ApplicationOutput applicationOutput = ApplicationOutput.newBuilder().setIndex(1).setFileOutput(fileOutput)
                .setRequired(true).build();
        Application application = Application.newBuilder().setName("test-application").addInputs(applicationInput)
                .addInputs(applicationInput2).addInputs(applicationInput3).addOutputs(applicationOutput)
                .build();
        ApplicationRunInfo applicationRunInfo = ApplicationRunInfo.newBuilder().setApplication(application).build();
        FileLocation sourceLocation = FileLocation.newBuilder().setStorageId("source-location-storage-id").build();
        InDataMovement inDataMovement = InDataMovement.newBuilder().setInputIndex(1).setSourceLocation(sourceLocation)
                .build();
        DataMovementConfiguration dataMovementConfiguration = DataMovementConfiguration.newBuilder()
                .addInMovements(inDataMovement).build();
        // TODO: add RunConfiguration for ec2
        // TODO: add RunConfiguration for local
        RunConfiguration runConfiguration = RunConfiguration.newBuilder().setServer(serverBackend)
                .setAppRunInfo(applicationRunInfo).addDataMovementConfigs(dataMovementConfiguration).build();
        Experiment experiment = Experiment.newBuilder().setCreationTime(System.currentTimeMillis())
                .setDescription("Sample Exp").setExperimentName("Exp Name").setGatewayId("gateway-id")
                .setProjectId("project-id").addRunConfigs(runConfiguration).build();

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
        assertEquals(serverBackend.getPort(), serverBackendEntity.getPort());
        assertEquals(serverBackend.getWorkingDirectory(), serverBackendEntity.getWorkingDirectory());
        SSHInterfaceEntity commandInterface = serverBackendEntity.getCommandInterface();
        assertEquals(sshInterface.getHostName(), commandInterface.getHostName());
        assertEquals(sshInterface.getPort(), commandInterface.getPort().intValue());
        assertEquals(sshInterface.getSshCredentialId(), commandInterface.getSshCredentialId());
        SCPInterfaceEntity scpInterfaceEntity = serverBackendEntity.getDataInterface();
        assertEquals(scpInterface.getHostName(), scpInterfaceEntity.getHostName());
        assertEquals(scpInterface.getPort(), scpInterfaceEntity.getPort().intValue());
        assertEquals(scpInterface.getSshCredentialId(), scpInterfaceEntity.getSshCredentialId());

        // ApplicationRunInfo
        ApplicationRunInfoEntity applicationRunInfoEntity = runConfigEntity.getAppRunInfo();
        ApplicationEntity applicationEntity = applicationRunInfoEntity.getApplication();
        assertEquals(application.getName(), applicationEntity.getName());
        assertEquals(application.getInputsCount(), applicationEntity.getInputs().size());
        Optional<ApplicationInputEntity> maybeApplicationInputEntity = applicationEntity.getInputs().stream()
                .filter(i -> i.getIndex() == applicationInput.getIndex()).findFirst();
        assertTrue(maybeApplicationInputEntity.isPresent());
        assertEquals(applicationInput.getRequired(), maybeApplicationInputEntity.get().isRequired());
        // CommandLineInput, index=1
        CommandLineInputEntity commandLineInputEntity = maybeApplicationInputEntity.get().getCommandLineInput();
        assertNotNull(commandLineInputEntity);
        assertEquals(commandLineInput.getPosition(), commandLineInputEntity.getPosition());
        assertEquals(commandLineInput.getPrefix(), commandLineInputEntity.getPrefix());
        assertEquals(commandLineInput.getValue(), commandLineInputEntity.getValue());
        maybeApplicationInputEntity = applicationEntity.getInputs().stream()
                .filter(i -> i.getIndex() == applicationInput2.getIndex()).findFirst();
        assertTrue(maybeApplicationInputEntity.isPresent());
        assertEquals(applicationInput2.getRequired(), maybeApplicationInputEntity.get().isRequired());
        // FileInput, index=2
        FileInputEntity fileInputEntity = maybeApplicationInputEntity.get().getFileInput();
        assertNotNull(fileInputEntity);
        assertEquals(fileInput.getFriendlyName(), fileInputEntity.getFriendlyName());
        assertEquals(fileInput.getDestinationPath(), fileInputEntity.getDestinationPath());
        maybeApplicationInputEntity = applicationEntity.getInputs().stream()
                .filter(i -> i.getIndex() == applicationInput3.getIndex()).findFirst();
        assertTrue(maybeApplicationInputEntity.isPresent());
        assertEquals(applicationInput3.getRequired(), maybeApplicationInputEntity.get().isRequired());
        // EnvironmentInputEntity, index=3
        EnvironmentInputEntity environmentInputEntity = maybeApplicationInputEntity.get().getEnvironmentInput();
        assertNotNull(environmentInputEntity);
        assertEquals(environmentInput.getKey(), environmentInputEntity.getKey());
        assertEquals(environmentInput.getValue(), environmentInputEntity.getValue());
        assertEquals(application.getOutputsCount(), applicationEntity.getOutputs().size());
        Optional<ApplicationOutputEntity> maybeApplicationOutputEntity = applicationEntity.getOutputs().stream()
                .filter(o -> o.getIndex() == applicationOutput.getIndex()).findFirst();
        assertTrue(maybeApplicationOutputEntity.isPresent());
        assertEquals(applicationOutput.getRequired(), maybeApplicationOutputEntity.get().isRequired());
        // FileOutput, index=1
        FileOutputEntity fileOutputEntity = maybeApplicationOutputEntity.get().getFileOutput();
        assertNotNull(fileOutputEntity);
        assertEquals(fileOutput.getFriendlyName(), fileOutputEntity.getFriendlyName());
        assertEquals(fileOutput.getDestinationPath(), fileOutputEntity.getDestinationPath());

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
