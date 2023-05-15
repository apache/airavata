package org.apache.airavata.apis.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.gateway.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ExecutionHandler extends ExecutionServiceGrpc.ExecutionServiceImplBase {
    @Override
    public void registerExperiment(ExperimentRegisterRequest request, StreamObserver<ExperimentRegisterResponse> responseObserver) {
        super.registerExperiment(request, responseObserver);
    }

    @Override
    public void launchExperiment(ExperimentLaunchRequest request, StreamObserver<ExperimentLaunchResponse> responseObserver) {
        super.launchExperiment(request, responseObserver);
    }
}
