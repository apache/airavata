package org.apache.airavata.apis.workflow.task.data;

import org.apache.airavata.apis.workflow.task.common.BaseTask;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskDef;
import org.apache.airavata.apis.workflow.task.common.annotation.TaskParam;
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.*;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.UserTokenAuth;
import org.apache.helix.task.TaskResult;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@TaskDef(name = "DataMovementTask")
public class DataMovementTask extends BaseTask {

    private final static Logger logger = LoggerFactory.getLogger(DataMovementTask.class);

    @TaskParam(name = "secretServiceHost")
    private ThreadLocal<String> secretServiceHost = new ThreadLocal<>();

    @TaskParam(name = "secretServicePort")
    private ThreadLocal<Integer> secretServicePort = new ThreadLocal<>();

    @TaskParam(name = "resourceServiceHost")
    private ThreadLocal<String> resourceServiceHost = new ThreadLocal<>();

    @TaskParam(name = "resourceServicePort")
    private ThreadLocal<Integer> resourceServicePort = new ThreadLocal<>();

    @TaskParam(name = "transferServiceHost")
    private ThreadLocal<String> transferServiceHost = new ThreadLocal<>();

    @TaskParam(name = "transferServicePort")
    private ThreadLocal<Integer> transferServicePort = new ThreadLocal<>();
    
    @TaskParam(name = "sourceStorageId")
    private ThreadLocal<String> sourceStorageId = new ThreadLocal<>();

    @TaskParam(name = "sourceCredentialId")
    private ThreadLocal<String> sourceCredentialId = new ThreadLocal<>();

    @TaskParam(name = "sourcePath")
    private ThreadLocal<String> sourcePath = new ThreadLocal<>();

    @TaskParam(name = "destinationStorageId")
    private ThreadLocal<String> destinationStorageId = new ThreadLocal<>();

    @TaskParam(name = "destinationCredentialId")
    private ThreadLocal<String> destinationCredentialId = new ThreadLocal<>();

    @TaskParam(name = "destinationPath")
    private ThreadLocal<String> destinationPath = new ThreadLocal<>();

    @TaskParam(name = "userToken")
    private ThreadLocal<String> userToken = new ThreadLocal<>();

    @TaskParam(name = "ignoreFailure")
    private ThreadLocal<Boolean> ignoreFailure = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Override
    public TaskResult onRun() throws Exception {
        logger.info("Starting Data Movement task {}", getTaskId());

        try (MFTApiClient mftClient = MFTApiClient.MFTApiClientBuilder.newBuilder()
                .withResourceServicePort(getResourceServicePort())
                .withTransferServicePort(getTransferServicePort())
                .withSecretServicePort(getSecretServicePort())
                .withTransferServiceHost(getTransferServiceHost())
                .withSecretServiceHost(getSecretServiceHost())
                .withResourceServiceHost(getResourceServiceHost()).build()) {

            AuthToken authToken = AuthToken.newBuilder()
                    .setUserTokenAuth(UserTokenAuth.newBuilder()
                            .setToken(getUserToken()).build()).build();
            TransferApiResponse transferResp = mftClient.getTransferClient().submitTransfer(TransferApiRequest.newBuilder()
                    .setMftAuthorizationToken(authToken)
                    .setSourceStorageId(getSourceStorageId())
                    .setSourceSecretId(getSourceCredentialId())
                    .setDestinationStorageId(getDestinationStorageId())
                    .setDestinationSecretId(getDestinationCredentialId())
                    .addEndpointPaths(EndpointPaths.newBuilder()
                            .setSourcePath(getSourcePath())
                            .setDestinationPath(getDestinationPath()).build()).build());

            logger.info("Submitted transfer request {} for source path {} in storage {} and destination path {} in storage {}",
                    transferResp.getTransferId(), getSourcePath(), getSourceStorageId(), getDestinationPath(), getDestinationStorageId());

            Awaitility.with().pollInterval(Duration.of(2, ChronoUnit.SECONDS)).await().atMost(100, TimeUnit.SECONDS).until(() ->  {
                TransferStateSummaryResponse transferState = mftClient.getTransferClient().getTransferStateSummary(TransferStateApiRequest.newBuilder()
                        .setMftAuthorizationToken(authToken)
                        .setTransferId(transferResp.getTransferId()).build());
                logger.info("Transfer state for transfer {} is {}", transferResp.getTransferId(), transferState.getState());
                return transferState.getState().equals("COMPLETED") || transferState.getState().equals("FAILED");
            });

            TransferStateSummaryResponse finalState = mftClient.getTransferClient().getTransferStateSummary(TransferStateApiRequest.newBuilder()
                    .setMftAuthorizationToken(authToken)
                    .setTransferId(transferResp.getTransferId()).build());

            if (finalState.getState().equals("COMPLETED") || getIgnoreFailure()) {
                return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
            } else {
                logger.info("Transfer {} was {}. Exiting task..", transferResp.getTransferId(), finalState.getState());
                return new TaskResult(TaskResult.Status.FAILED, "Failed");
            }
        }
    }

    @Override
    public void onCancel() throws Exception {

    }

    public String getSourceStorageId() {
        return sourceStorageId.get();
    }

    public void setSourceStorageId(String sourceStorageId) {
        this.sourceStorageId.set(sourceStorageId);
    }

    public String getSourceCredentialId() {
        return sourceCredentialId.get();
    }

    public void setSourceCredentialId(String sourceCredentialId) {
        this.sourceCredentialId.set(sourceCredentialId);
    }

    public String getSourcePath() {
        return sourcePath.get();
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath.set(sourcePath);
    }

    public String getDestinationStorageId() {
        return destinationStorageId.get();
    }

    public void setDestinationStorageId(String destinationStorageId) {
        this.destinationStorageId.set(destinationStorageId);
    }

    public String getDestinationCredentialId() {
        return destinationCredentialId.get();
    }

    public void setDestinationCredentialId(String destinationCredentialId) {
        this.destinationCredentialId.set(destinationCredentialId);
    }

    public String getDestinationPath() {
        return destinationPath.get();
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath.set(destinationPath);
    }

    public String getUserToken() {
        return userToken.get();
    }

    public void setUserToken(String userToken) {
        this.userToken.set(userToken);
    }

    public Boolean getIgnoreFailure() {
        return ignoreFailure.get();
    }

    public void setIgnoreFailure(Boolean ignoreFailure) {
        this.ignoreFailure.set(ignoreFailure);
    }

    public String getSecretServiceHost() {
        return secretServiceHost.get();
    }

    public void setSecretServiceHost(String secretServiceHost) {
        this.secretServiceHost.set( secretServiceHost);
    }

    public Integer getSecretServicePort() {
        return secretServicePort.get();
    }

    public void setSecretServicePort(Integer secretServicePort) {
        this.secretServicePort.set(secretServicePort);
    }

    public String getResourceServiceHost() {
        return resourceServiceHost.get();
    }

    public void setResourceServiceHost(String resourceServiceHost) {
        this.resourceServiceHost.set( resourceServiceHost);
    }

    public Integer getResourceServicePort() {
        return resourceServicePort.get();
    }

    public void setResourceServicePort(Integer resourceServicePort) {
        this.resourceServicePort.set(resourceServicePort);
    }

    public String getTransferServiceHost() {
        return transferServiceHost.get();
    }

    public void setTransferServiceHost(String transferServiceHost) {
        this.transferServiceHost.set( transferServiceHost);
    }

    public Integer getTransferServicePort() {
        return transferServicePort.get();
    }

    public void setTransferServicePort(Integer transferServicePort) {
        this.transferServicePort.set(transferServicePort);
    }

}
