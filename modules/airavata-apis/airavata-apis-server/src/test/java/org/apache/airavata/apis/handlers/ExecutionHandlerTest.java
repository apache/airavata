package org.apache.airavata.apis.handlers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.airavata.api.execution.ExperimentRegisterRequest;
import org.apache.airavata.api.execution.ExperimentRegisterResponse;
import org.apache.airavata.api.execution.ExperimentUpdateRequest;
import org.apache.airavata.api.execution.ExperimentUpdateResponse;
import org.apache.airavata.api.execution.stubs.*;
import org.apache.airavata.api.execution.stubs.Experiment.Builder;
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
import org.apache.airavata.apis.db.entity.application.output.StandardErrorEntity;
import org.apache.airavata.apis.db.entity.application.output.StandardOutEntity;
import org.apache.airavata.apis.db.entity.application.runners.DockerRunnerEntity;
import org.apache.airavata.apis.db.entity.application.runners.SlurmRunnerEntity;
import org.apache.airavata.apis.db.entity.backend.ComputeBackendEntity;
import org.apache.airavata.apis.db.entity.backend.EC2BackendEntity;
import org.apache.airavata.apis.db.entity.backend.LocalBackendEntity;
import org.apache.airavata.apis.db.entity.backend.ServerBackendEntity;
import org.apache.airavata.apis.db.entity.backend.iface.SCPInterfaceEntity;
import org.apache.airavata.apis.db.entity.backend.iface.SSHInterfaceEntity;
import org.apache.airavata.apis.db.entity.data.InDataMovementEntity;
import org.apache.airavata.apis.db.entity.data.OutDataMovementEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.apache.airavata.apis.db.repository.RunConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.Optional;

import static java.util.Arrays.asList;
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

    @Autowired
    RunConfigurationRepository runConfigurationRepository;

    @Autowired
    EntityManager entityManager;

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
    ApplicationInput applicationInput = ApplicationInput.newBuilder().setIndex(1).setCommandLineInput(commandLineInput)
            .setRequired(true).build();
    FileInput fileInput = FileInput.newBuilder().setFriendlyName("config file")
            .setDestinationPath("/scratch/workdirs/W123/config.conf").build();
    ApplicationInput applicationInput2 = ApplicationInput.newBuilder().setIndex(2).setFileInput(fileInput)
            .setRequired(true).build();
    EnvironmentInput environmentInput = EnvironmentInput.newBuilder().setKey("env-key").setValue("env-value").build();
    ApplicationInput applicationInput3 = ApplicationInput.newBuilder().setIndex(3).setEnvironmentInput(environmentInput)
            .setRequired(false).build();
    // ApplicationOutputs
    FileOutput fileOutput = FileOutput.newBuilder().setFriendlyName("output-file")
            .setDestinationPath("/scratch/workdir/output.file").build();
    ApplicationOutput applicationOutput = ApplicationOutput.newBuilder().setIndex(1).setFileOutput(fileOutput)
            .setRequired(true).build();
    StandardOut standardOut = StandardOut.newBuilder().setDestinationPath("/scratch/workdir/stdout").build();
    ApplicationOutput applicationOutput2 = ApplicationOutput.newBuilder().setIndex(2).setStdOut(standardOut)
            .setRequired(false).build();
    StandardError standardError = StandardError.newBuilder().setDestinationPath("/scratch/workdir/stderr").build();
    ApplicationOutput applicationOutput3 = ApplicationOutput.newBuilder().setIndex(3).setStdErr(standardError)
            .setRequired(false).build();
    Application application = Application.newBuilder().setName("test-application").addInputs(applicationInput)
            .addInputs(applicationInput2).addInputs(applicationInput3).addOutputs(applicationOutput)
            .addOutputs(applicationOutput2).addOutputs(applicationOutput3).build();
    DockerRunner dockerRunner = DockerRunner.newBuilder().setImageName("docker-image-name")
            .setImageTag("docker-image-tag").setRepository("docker-repository")
            .setDockerCredentialId("docker-credential-id").setRunCommand("docker-run-command").build();
    SlurmRunner slurmRunner = SlurmRunner.newBuilder().setNodes(3).setCpus(7).setMemory(23).setWallTime(37)
            .addAllPreJobCommands(asList("prejob1", "prejob2", "prejob3"))
            .addAllModuleLoadCommands(asList("module1", "module2", "module3")).setExecutable("/path/to/executable")
            .addAllPostJobCommands(asList("postjob1", "postjob2", "postjob13")).setQueue("shared")
            .addAllNotificationEmails(asList("notify1@example.com", "group-list@example.com")).build();
    ApplicationRunInfo applicationRunInfo = ApplicationRunInfo.newBuilder().setApplication(application)
            .setDockerRunner(dockerRunner).build();
    ApplicationRunInfo applicationRunInfoSlurmRunner = ApplicationRunInfo.newBuilder()
            .setApplication(cloneApplication(application)).setSlurmRunner(slurmRunner).build();
    FileLocation sourceLocation = FileLocation.newBuilder().setStorageId("source-location-storage-id")
            .setPath("/path/to/source").setStorageCredentialId("source-storage-credential-id").build();
    InDataMovement inDataMovement = InDataMovement.newBuilder().setInputIndex(1).setSourceLocation(sourceLocation)
            .build();

    FileLocation destinationLocation = FileLocation.newBuilder().setStorageId("destination-location-storage-id")
            .setPath("/path/to/destination").setStorageCredentialId("dest-storage-credential-id").build();
    OutDataMovement outDataMovement = OutDataMovement.newBuilder().setOutputIndex(2)
            .setDestinationLocation(destinationLocation).build();
    DataMovementConfiguration dataMovementConfiguration = DataMovementConfiguration.newBuilder()
            .addInMovements(inDataMovement).addOutMovements(outDataMovement).build();
    EC2Backend ec2Backend = EC2Backend.newBuilder().setFlavor("t1.micro").setRegion("us-east-1")
            .setAwsCredentialId("aws-credential-id").build();
    LocalBackend localBackend = LocalBackend.newBuilder().setAgentId("agent-id").setAgentTokenId("agent-token-id")
            .build();
    RunConfiguration runConfigurationServer = RunConfiguration.newBuilder().setServer(serverBackend)
            .setAppRunInfo(applicationRunInfo).addDataMovementConfigs(dataMovementConfiguration).build();
    RunConfiguration runConfigurationEC2Backend = cloneRunConfiguration(runConfigurationServer).toBuilder()
            .setEc2(ec2Backend).setAppRunInfo(applicationRunInfoSlurmRunner).build();
    RunConfiguration runConfigurationLocalBackend = cloneRunConfiguration(runConfigurationServer).toBuilder()
            .setLocal(localBackend).build();

    Experiment experiment = Experiment.newBuilder().setCreationTime(System.currentTimeMillis())
            .setDescription("Sample Exp").setExperimentName("Exp Name").setGatewayId("gateway-id")
            .setProjectId("project-id").addRunConfigs(runConfigurationServer).addRunConfigs(runConfigurationEC2Backend)
            .addRunConfigs(runConfigurationLocalBackend).build();

    @Test
    void testRegisterExperiment() {

        ExperimentRegisterRequest experimentRegisterRequest = ExperimentRegisterRequest.newBuilder()
                .setExperiment(experiment).build();

        TestStreamObserver<ExperimentRegisterResponse> responseObserver = new TestStreamObserver<>();
        executionHandler.registerExperiment(experimentRegisterRequest, responseObserver);

        assertTrue(responseObserver.isCompleted());
        String experimentId = responseObserver.getNext().getExperimentId();
        flushAndClear();
        ExperimentEntity experimentEntity = experimentRepository.findById(experimentId).get();

        assertEquals(experiment.getCreationTime(), experimentEntity.getCreationTime());
        assertEquals(experiment.getDescription(), experimentEntity.getDescription());
        assertEquals(experiment.getExperimentName(), experimentEntity.getExperimentName());
        assertEquals(experiment.getGatewayId(), experimentEntity.getGatewayId());
        assertEquals(experiment.getProjectId(), experimentEntity.getProjectId());

        // RunConfiguration
        assertEquals(experiment.getRunConfigsCount(), experimentEntity.getRunConfigs().size());
        RunConfigurationEntity runConfigWithServerEntity = experimentEntity.getRunConfigs().stream()
                .filter(rc -> rc.getServer() != null).findFirst().get();
        RunConfigurationEntity runConfigWithEC2Entity = experimentEntity.getRunConfigs().stream()
                .filter(rc -> rc.getEc2() != null).findFirst().get();
        RunConfigurationEntity runConfigWithLocalBackendEntity = experimentEntity.getRunConfigs().stream()
                .filter(rc -> rc.getLocal() != null).findFirst().get();

        // ComputeBackend
        ComputeBackendEntity computeBackendEntity = runConfigWithServerEntity.getComputeBackend();
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

        // EC2Backend
        EC2BackendEntity ec2BackendEntity = runConfigWithEC2Entity.getEc2();
        assertTrue(runConfigWithEC2Entity.getComputeBackend() instanceof EC2BackendEntity);
        assertEquals(ec2Backend.getFlavor(), ec2BackendEntity.getFlavor());
        assertEquals(ec2Backend.getRegion(), ec2BackendEntity.getRegion());
        assertEquals(ec2Backend.getAwsCredentialId(), ec2BackendEntity.getAwsCredentialId());

        // LocalBackend
        LocalBackendEntity localBackendEntity = runConfigWithLocalBackendEntity.getLocal();
        assertTrue(runConfigWithLocalBackendEntity.getComputeBackend() instanceof LocalBackendEntity);
        assertEquals(localBackend.getAgentId(), localBackendEntity.getAgentId());
        assertEquals(localBackend.getAgentTokenId(), localBackendEntity.getAgentTokenId());

        // ApplicationRunInfo
        ApplicationRunInfoEntity applicationRunInfoEntity = runConfigWithServerEntity.getAppRunInfo();
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
        maybeApplicationOutputEntity = applicationEntity.getOutputs().stream()
                .filter(o -> o.getIndex() == applicationOutput2.getIndex()).findFirst();
        assertTrue(maybeApplicationOutputEntity.isPresent());
        assertEquals(applicationOutput2.getRequired(), maybeApplicationOutputEntity.get().isRequired());
        // StandardOut, index=2
        StandardOutEntity standardOutEntity = maybeApplicationOutputEntity.get().getStdOut();
        assertNotNull(standardOutEntity);
        assertEquals(standardOut.getDestinationPath(), standardOutEntity.getDestinationPath());
        maybeApplicationOutputEntity = applicationEntity.getOutputs().stream()
                .filter(o -> o.getIndex() == applicationOutput3.getIndex()).findFirst();
        assertTrue(maybeApplicationOutputEntity.isPresent());
        assertEquals(applicationOutput3.getRequired(), maybeApplicationOutputEntity.get().isRequired());
        // StandardError, index=3
        StandardErrorEntity standardErrorEntity = maybeApplicationOutputEntity.get().getStdErr();
        assertNotNull(standardErrorEntity);
        assertEquals(standardError.getDestinationPath(), standardErrorEntity.getDestinationPath());

        // DockerRunner
        DockerRunnerEntity dockerRunnerEntity = applicationRunInfoEntity.getDockerRunner();
        assertEquals(dockerRunner.getImageName(), dockerRunnerEntity.getImageName());
        assertEquals(dockerRunner.getImageTag(), dockerRunnerEntity.getImageTag());
        assertEquals(dockerRunner.getRepository(), dockerRunnerEntity.getRepository());
        assertEquals(dockerRunner.getDockerCredentialId(), dockerRunnerEntity.getDockerCredentialId());
        assertEquals(dockerRunner.getRunCommand(), dockerRunnerEntity.getRunCommand());

        // SlurmRunner
        SlurmRunnerEntity slurmRunnerEntity = runConfigWithEC2Entity.getAppRunInfo().getSlurmRunner();
        assertEquals(slurmRunner.getNodes(), slurmRunnerEntity.getNodes());
        assertEquals(slurmRunner.getCpus(), slurmRunnerEntity.getCpus());
        assertEquals(slurmRunner.getMemory(), slurmRunnerEntity.getMemory());
        assertEquals(slurmRunner.getWallTime(), slurmRunnerEntity.getWallTime());
        assertEquals(slurmRunner.getPreJobCommandsList(), slurmRunnerEntity.getPreJobCommands());
        assertEquals(slurmRunner.getModuleLoadCommandsList(), slurmRunnerEntity.getModuleLoadCommands());
        assertEquals(slurmRunner.getExecutable(), slurmRunnerEntity.getExecutable());
        assertEquals(slurmRunner.getPostJobCommandsList(), slurmRunnerEntity.getPostJobCommands());
        assertEquals(slurmRunner.getQueue(), slurmRunnerEntity.getQueue());
        assertEquals(slurmRunner.getNotificationEmailsList(), slurmRunnerEntity.getNotificationEmails());

        // DataMovementConfiguration
        assertEquals(runConfigurationServer.getDataMovementConfigsCount(),
                runConfigWithServerEntity.getDataMovementConfigs().size());
        DataMovementConfigurationEntity dataMovementConfigurationEntity = runConfigWithServerEntity
                .getDataMovementConfigs().get(0);
        assertEquals(dataMovementConfiguration.getInMovementsCount(),
                dataMovementConfigurationEntity.getInMovements().size());

        // InDataMovement
        InDataMovementEntity inDataMovementEntity = dataMovementConfigurationEntity.getInMovements().iterator().next();
        assertEquals(inDataMovement.getInputIndex(), inDataMovementEntity.getInputIndex());
        assertEquals(inDataMovement.getSourceLocation().getStorageId(),
                inDataMovementEntity.getSourceLocation().getStorageId());
        assertEquals(inDataMovement.getSourceLocation().getPath(), inDataMovementEntity.getSourceLocation().getPath());
        assertEquals(inDataMovement.getSourceLocation().getStorageCredentialId(),
                inDataMovementEntity.getSourceLocation().getStorageCredentialId());

        // OutDataMovement
        OutDataMovementEntity outDataMovementEntity = dataMovementConfigurationEntity.getOutMovements().iterator()
                .next();
        assertEquals(outDataMovement.getOutputIndex(), outDataMovementEntity.getOutputIndex());
        assertEquals(outDataMovement.getDestinationLocation().getStorageId(),
                outDataMovementEntity.getDestinationLocation().getStorageId());
        assertEquals(outDataMovement.getDestinationLocation().getPath(),
                outDataMovementEntity.getDestinationLocation().getPath());
        assertEquals(outDataMovement.getDestinationLocation().getStorageCredentialId(),
                outDataMovementEntity.getDestinationLocation().getStorageCredentialId());

    }

    @Test
    public void testUpdateExperiment() {

        ExperimentRegisterRequest experimentRegisterRequest = ExperimentRegisterRequest.newBuilder()
                .setExperiment(experiment).build();

        TestStreamObserver<ExperimentRegisterResponse> responseObserver = new TestStreamObserver<>();
        executionHandler.registerExperiment(experimentRegisterRequest, responseObserver);

        assertTrue(responseObserver.isCompleted());
        String experimentId = responseObserver.getNext().getExperimentId();
        flushAndClear();

        // Set the experiment id
        Builder builder = experiment.toBuilder().setExperimentId(experimentId);
        Experiment updatedExperiment = builder.build();

        ExperimentUpdateRequest experimentUpdateRequest = ExperimentUpdateRequest.newBuilder()
                .setExperiment(updatedExperiment).build();

        TestStreamObserver<ExperimentUpdateResponse> updateResponseObserver = new TestStreamObserver<>();
        executionHandler.updateExperiment(experimentUpdateRequest, updateResponseObserver);

        assertTrue(responseObserver.isCompleted());
        flushAndClear();

        // Just making sure that we only have one run configuration now
        assertEquals(experiment.getRunConfigsCount(), runConfigurationRepository.count());
    }

    void flushAndClear() {
        // Force flushing experiment to database and removing from session so we can
        // then reload from database
        entityManager.flush();
        entityManager.clear();
    }

    RunConfiguration cloneRunConfiguration(RunConfiguration runConfiguration) {

        try {
            return RunConfiguration.parseFrom(runConfiguration.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    Application cloneApplication(Application application) {
        try {
            return Application.parseFrom(application.toByteArray());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
