package org.apache.airavata.apis.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.api.execution.stubs.*;
import org.apache.airavata.api.gateway.ExecutionServiceGrpc;
import org.apache.airavata.api.gateway.ExperimentRegisterRequest;
import org.apache.airavata.api.gateway.ExperimentRegisterResponse;

public class AiravataAPIClient {
    public static void main(String args[]) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7002).usePlaintext().build();
        ExecutionServiceGrpc.ExecutionServiceBlockingStub execApi = ExecutionServiceGrpc.newBlockingStub(channel);

        Application application = Application.newBuilder()
                .setApplicationId("id")
                .addInputs(
                        ApplicationInput.newBuilder()
                                .setFileInput(FileInput.newBuilder().build())
                                .build())
                .build();


        ApplicationRunInfo appRunInfo = ApplicationRunInfo.newBuilder()
                .setApplication(application)
                .setSlurmRunner(SlurmRunner.newBuilder().setCpus(2).setMemory(100).setExecutable("exec")
                        .build()).build();

        ServerBackend serverBackend = ServerBackend.newBuilder()
                .setCommandInterface(
                        SSHInterface.newBuilder().setHostName("localhost").build())
                .setDataInterface(SCPInterface.newBuilder().build()).setHostName("localhost").setPort(22).build();


        DataMovementConfiguration dataMovement = DataMovementConfiguration.newBuilder()
                .addInMovements(
                        InDataMovement.newBuilder()
                                .setInputIndex(0)
                                .setSourceLocation(FileLocation.newBuilder()
                                        .setStorageId("storageId")
                                        .setPath("/data/a.txt")
                                        .setStorageCredentialId("Crede").build())
                                .build()).build();

        RunConfiguration runConfig = RunConfiguration.newBuilder()
                .setAppRunInfo(appRunInfo)
                .setServer(serverBackend)
                .addDataMovementConfigs(dataMovement).build();

        Experiment experiment = Experiment.newBuilder()
                .setCreationTime(System.currentTimeMillis())
                .setDescription("Sample Exp")
                .addRunConfigs(runConfig).build();

        ExperimentRegisterRequest experimentRegisterRequest = ExperimentRegisterRequest.newBuilder()
                .setExperiment(experiment).build();

        ExperimentRegisterResponse experimentRegisterResponse = execApi.registerExperiment(experimentRegisterRequest);

    }
}
