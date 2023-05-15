package org.apache.airavata.apis.handlers;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.api.execution.stubs.RunConfiguration;
import org.apache.airavata.api.gateway.*;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.entity.RunConfigurationEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.apache.airavata.apis.scheduling.MetaScheduler;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@GRpcService
public class ExecutionHandler extends ExecutionServiceGrpc.ExecutionServiceImplBase {

    @Autowired
    private MetaScheduler metaScheduler;

    @Autowired
    ExperimentRepository experimentRepository;

    @Override
    public void registerExperiment(ExperimentRegisterRequest request, StreamObserver<ExperimentRegisterResponse> responseObserver) {

        ExperimentEntity experimentEntity = new ExperimentEntity();
        experimentEntity.setExperimentName(request.getExperiment().getExperimentName());
        experimentEntity.setDescription(request.getExperiment().getDescription());

        List<RunConfigurationEntity> runConfigs = new ArrayList<>();
        for(RunConfiguration rc: request.getExperiment().getRunConfigsList()) {
            RunConfigurationEntity runConfigurationEntity = new RunConfigurationEntity();
            // Fill

        }
        experimentEntity.setRunConfigs(runConfigs);
        experimentRepository.save(experimentEntity);
    }

    @Override
    public void launchExperiment(ExperimentLaunchRequest request, StreamObserver<ExperimentLaunchResponse> responseObserver) {
        metaScheduler.scheduleExperiment(request);
        responseObserver.onNext(ExperimentLaunchResponse.newBuilder().setStatus(true).build());
        responseObserver.onCompleted();
    }
}
