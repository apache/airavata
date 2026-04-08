/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.experiment.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.ExperimentSearchFields;
import org.apache.airavata.model.experiment.proto.ExperimentStatistics;
import org.apache.airavata.model.experiment.proto.ExperimentSummaryModel;
import org.apache.airavata.model.job.proto.JobModel;
import org.apache.airavata.model.status.proto.ExperimentStatus;
import org.apache.airavata.model.status.proto.JobStatus;
import org.apache.airavata.model.status.proto.ProcessStatus;
import org.apache.airavata.research.service.ExperimentService;
import org.springframework.stereotype.Component;

@Component
public class ExperimentGrpcService extends ExperimentServiceGrpc.ExperimentServiceImplBase {

    private final ExperimentService experimentService;

    public ExperimentGrpcService(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @Override
    public void createExperiment(CreateExperimentRequest request, StreamObserver<CreateExperimentResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = experimentService.createExperiment(ctx, request.getExperiment());
            observer.onNext(
                    CreateExperimentResponse.newBuilder().setExperimentId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperiment(GetExperimentRequest request, StreamObserver<ExperimentModel> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentModel result = experimentService.getExperiment(ctx, request.getExperimentId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentByAdmin(GetExperimentByAdminRequest request, StreamObserver<ExperimentModel> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentModel result = experimentService.getExperimentByAdmin(ctx, request.getExperimentId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateExperiment(UpdateExperimentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.updateExperiment(ctx, request.getExperimentId(), request.getExperiment());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteExperiment(DeleteExperimentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.deleteExperiment(ctx, request.getExperimentId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void searchExperiments(
            SearchExperimentsRequest request, StreamObserver<SearchExperimentsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            // TODO: Map string filter keys to ExperimentSearchFields enum — needs mapper
            Map<ExperimentSearchFields, String> filters = new HashMap<>();
            for (Map.Entry<String, String> entry : request.getFiltersMap().entrySet()) {
                filters.put(ExperimentSearchFields.valueOf(entry.getKey()), entry.getValue());
            }
            List<ExperimentSummaryModel> results = experimentService.searchExperiments(
                    ctx,
                    request.getGatewayId(),
                    request.getUserName(),
                    filters,
                    request.getLimit(),
                    request.getOffset());
            observer.onNext(SearchExperimentsResponse.newBuilder()
                    .addAllExperiments(results)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentStatus(GetExperimentStatusRequest request, StreamObserver<ExperimentStatus> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentStatus result = experimentService.getExperimentStatus(ctx, request.getExperimentId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentOutputs(
            GetExperimentOutputsRequest request, StreamObserver<GetExperimentOutputsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<OutputDataObjectType> outputs = experimentService.getExperimentOutputs(ctx, request.getExperimentId());
            observer.onNext(GetExperimentOutputsResponse.newBuilder()
                    .addAllOutputs(outputs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentsInProject(
            GetExperimentsInProjectRequest request, StreamObserver<GetExperimentsInProjectResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ExperimentModel> results = experimentService.getExperimentsInProject(
                    ctx, request.getProjectId(), request.getLimit(), request.getOffset());
            observer.onNext(GetExperimentsInProjectResponse.newBuilder()
                    .addAllExperiments(results)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUserExperiments(
            GetUserExperimentsRequest request, StreamObserver<GetUserExperimentsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ExperimentModel> results = experimentService.getUserExperiments(
                    ctx, request.getGatewayId(), request.getUserName(), request.getLimit(), request.getOffset());
            observer.onNext(GetUserExperimentsResponse.newBuilder()
                    .addAllExperiments(results)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDetailedExperimentTree(
            GetDetailedExperimentTreeRequest request, StreamObserver<ExperimentModel> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentModel result = experimentService.getDetailedExperimentTree(ctx, request.getExperimentId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateExperimentConfiguration(
            UpdateExperimentConfigurationRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.updateExperimentConfiguration(ctx, request.getExperimentId(), request.getConfiguration());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateResourceScheduling(UpdateResourceSchedulingRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.updateResourceScheduleing(ctx, request.getExperimentId(), request.getScheduling());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void validateExperiment(
            ValidateExperimentRequest request, StreamObserver<ValidateExperimentResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            boolean isValid = experimentService.validateExperiment(ctx, request.getExperimentId());
            observer.onNext(
                    ValidateExperimentResponse.newBuilder().setIsValid(isValid).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void launchExperiment(LaunchExperimentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.launchExperiment(ctx, request.getExperimentId(), request.getGatewayId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void terminateExperiment(TerminateExperimentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.terminateExperiment(ctx, request.getExperimentId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void cloneExperiment(CloneExperimentRequest request, StreamObserver<CloneExperimentResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String newId = experimentService.cloneExperiment(
                    ctx,
                    request.getExperimentId(),
                    request.getNewExperimentName(),
                    request.getNewExperimentProjectId(),
                    false);
            observer.onNext(
                    CloneExperimentResponse.newBuilder().setExperimentId(newId).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getJobStatuses(GetJobStatusesRequest request, StreamObserver<GetJobStatusesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, JobStatus> statuses = experimentService.getJobStatuses(ctx, request.getExperimentId());
            observer.onNext(GetJobStatusesResponse.newBuilder()
                    .putAllJobStatuses(statuses)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getJobDetails(GetJobDetailsRequest request, StreamObserver<GetJobDetailsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<JobModel> jobs = experimentService.getJobDetails(ctx, request.getExperimentId());
            observer.onNext(GetJobDetailsResponse.newBuilder().addAllJobs(jobs).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void fetchIntermediateOutputs(FetchIntermediateOutputsRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            experimentService.fetchIntermediateOutputs(ctx, request.getExperimentId(), request.getOutputNamesList());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getIntermediateOutputProcessStatus(
            GetIntermediateOutputProcessStatusRequest request, StreamObserver<ProcessStatus> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            // The service expects output names but the proto request only has experiment_id.
            // Pass empty list — the service layer handles this case.
            ProcessStatus result =
                    experimentService.getIntermediateOutputProcessStatus(ctx, request.getExperimentId(), List.of());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getExperimentStatistics(
            GetExperimentStatisticsRequest request, StreamObserver<ExperimentStatistics> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ExperimentStatistics stats = experimentService.getExperimentStatistics(
                    ctx,
                    request.getGatewayId(),
                    request.getFromTime(),
                    request.getToTime(),
                    request.getUserName(),
                    request.getApplicationName(),
                    request.getResourceHostName(),
                    request.getLimit(),
                    request.getOffset());
            observer.onNext(stats);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
