package org.apache.airavata.apis.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.execution.*;
import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.apache.airavata.apis.mapper.ExperimentMapper;
import org.apache.airavata.apis.scheduling.MetaScheduler;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class ExecutionHandler extends ExecutionServiceGrpc.ExecutionServiceImplBase {

    @Autowired
    private MetaScheduler metaScheduler;

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    ExperimentMapper experimentMapper;

    @Override
    public void registerExperiment(ExperimentRegisterRequest request, StreamObserver<ExperimentRegisterResponse> responseObserver) {

        Experiment experiment = request.getExperiment();

        ExperimentEntity experimentEntity = experimentMapper.mapModelToEntity(experiment);
        ExperimentEntity savedExperimentEntity = experimentRepository.save(experimentEntity);

        responseObserver.onNext(ExperimentRegisterResponse.newBuilder()
                .setExperimentId(savedExperimentEntity.getExperimentId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void launchExperiment(ExperimentLaunchRequest request, StreamObserver<ExperimentLaunchResponse> responseObserver) {
        metaScheduler.scheduleExperiment(request);
        responseObserver.onNext(ExperimentLaunchResponse.newBuilder().setStatus(true).build());
        responseObserver.onCompleted();
    }
}
