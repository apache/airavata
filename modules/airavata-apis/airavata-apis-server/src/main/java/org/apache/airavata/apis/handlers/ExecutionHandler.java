package org.apache.airavata.apis.handlers;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.execution.*;
import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.apis.exception.EntityNotFoundException;
import org.apache.airavata.apis.scheduling.MetaScheduler;
import org.apache.airavata.apis.service.ExecutionService;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class ExecutionHandler extends ExecutionServiceGrpc.ExecutionServiceImplBase {

    @Autowired
    private MetaScheduler metaScheduler;

    @Autowired
    private ExecutionService executionService;

    @Override
    public void registerExperiment(ExperimentRegisterRequest request, StreamObserver<ExperimentRegisterResponse> responseObserver) {

        Experiment experiment = request.getExperiment();

        Experiment savedExperiment = executionService.createExperiment(experiment);

        responseObserver.onNext(ExperimentRegisterResponse.newBuilder()
                .setExperimentId(savedExperiment.getExperimentId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateExperiment(ExperimentUpdateRequest request,
            StreamObserver<ExperimentUpdateResponse> responseObserver) {

        Experiment experiment = request.getExperiment();
        try {
            executionService.updateExperiment(experiment);
            responseObserver.onNext(ExperimentUpdateResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (EntityNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asException());
        }
    }

    @Override
    public void launchExperiment(ExperimentLaunchRequest request, StreamObserver<ExperimentLaunchResponse> responseObserver) {
        metaScheduler.scheduleExperiment(request);
        responseObserver.onNext(ExperimentLaunchResponse.newBuilder().setStatus(true).build());
        responseObserver.onCompleted();
    }
}
