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
import java.util.List;
import java.util.Map;
import org.apache.airavata.api.appcatalog.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationDeploymentDescription;
import org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule;
import org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription;
import org.apache.airavata.model.application.io.proto.InputDataObjectType;
import org.apache.airavata.model.application.io.proto.OutputDataObjectType;
import org.apache.airavata.research.service.ApplicationCatalogService;
import org.springframework.stereotype.Component;

@Component
public class ApplicationCatalogGrpcService extends ApplicationCatalogServiceGrpc.ApplicationCatalogServiceImplBase {

    private final ApplicationCatalogService applicationCatalogService;

    public ApplicationCatalogGrpcService(ApplicationCatalogService applicationCatalogService) {
        this.applicationCatalogService = applicationCatalogService;
    }

    // --- Application Modules ---

    @Override
    public void registerApplicationModule(
            RegisterApplicationModuleRequest request, StreamObserver<RegisterApplicationModuleResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = applicationCatalogService.registerApplicationModule(
                    ctx, request.getGatewayId(), request.getApplicationModule());
            observer.onNext(RegisterApplicationModuleResponse.newBuilder()
                    .setAppModuleId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getApplicationModule(GetApplicationModuleRequest request, StreamObserver<ApplicationModule> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ApplicationModule result = applicationCatalogService.getApplicationModule(ctx, request.getAppModuleId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateApplicationModule(UpdateApplicationModuleRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.updateApplicationModule(
                    ctx, request.getAppModuleId(), request.getApplicationModule());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteApplicationModule(DeleteApplicationModuleRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.deleteApplicationModule(ctx, request.getAppModuleId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllAppModules(GetAllAppModulesRequest request, StreamObserver<GetAllAppModulesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationModule> modules = applicationCatalogService.getAllAppModules(ctx, request.getGatewayId());
            observer.onNext(GetAllAppModulesResponse.newBuilder()
                    .addAllApplicationModules(modules)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAccessibleAppModules(
            GetAccessibleAppModulesRequest request, StreamObserver<GetAccessibleAppModulesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationModule> modules =
                    applicationCatalogService.getAccessibleAppModules(ctx, request.getGatewayId());
            observer.onNext(GetAccessibleAppModulesResponse.newBuilder()
                    .addAllApplicationModules(modules)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Application Deployments ---

    @Override
    public void registerApplicationDeployment(
            RegisterApplicationDeploymentRequest request,
            StreamObserver<RegisterApplicationDeploymentResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = applicationCatalogService.registerApplicationDeployment(
                    ctx, request.getGatewayId(), request.getApplicationDeployment());
            observer.onNext(RegisterApplicationDeploymentResponse.newBuilder()
                    .setAppDeploymentId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getApplicationDeployment(
            GetApplicationDeploymentRequest request, StreamObserver<ApplicationDeploymentDescription> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ApplicationDeploymentDescription result =
                    applicationCatalogService.getApplicationDeployment(ctx, request.getAppDeploymentId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateApplicationDeployment(
            UpdateApplicationDeploymentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.updateApplicationDeployment(
                    ctx, request.getAppDeploymentId(), request.getApplicationDeployment());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteApplicationDeployment(
            DeleteApplicationDeploymentRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.deleteApplicationDeployment(ctx, request.getAppDeploymentId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllApplicationDeployments(
            GetAllApplicationDeploymentsRequest request,
            StreamObserver<GetAllApplicationDeploymentsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationDeploymentDescription> deployments =
                    applicationCatalogService.getAllApplicationDeployments(ctx, request.getGatewayId());
            observer.onNext(GetAllApplicationDeploymentsResponse.newBuilder()
                    .addAllApplicationDeployments(deployments)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAccessibleApplicationDeployments(
            GetAccessibleApplicationDeploymentsRequest request,
            StreamObserver<GetAccessibleApplicationDeploymentsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationDeploymentDescription> deployments =
                    applicationCatalogService.getAccessibleApplicationDeployments(
                            ctx,
                            request.getGatewayId(),
                            org.apache.airavata.model.group.proto.ResourcePermissionType.READ);
            observer.onNext(GetAccessibleApplicationDeploymentsResponse.newBuilder()
                    .addAllApplicationDeployments(deployments)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAppModuleDeployedResources(
            GetAppModuleDeployedResourcesRequest request,
            StreamObserver<GetAppModuleDeployedResourcesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<String> resourceIds =
                    applicationCatalogService.getAppModuleDeployedResources(ctx, request.getAppModuleId());
            observer.onNext(GetAppModuleDeployedResourcesResponse.newBuilder()
                    .addAllComputeResourceIds(resourceIds)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDeploymentsForModuleAndProfile(
            GetDeploymentsForModuleAndProfileRequest request,
            StreamObserver<GetDeploymentsForModuleAndProfileResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationDeploymentDescription> deployments =
                    applicationCatalogService.getApplicationDeploymentsForAppModuleAndGroupResourceProfile(
                            ctx, request.getAppModuleId(), request.getGroupResourceProfileId());
            observer.onNext(GetDeploymentsForModuleAndProfileResponse.newBuilder()
                    .addAllApplicationDeployments(deployments)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Application Interfaces ---

    @Override
    public void registerApplicationInterface(
            RegisterApplicationInterfaceRequest request,
            StreamObserver<RegisterApplicationInterfaceResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = applicationCatalogService.registerApplicationInterface(
                    ctx, request.getGatewayId(), request.getApplicationInterface());
            observer.onNext(RegisterApplicationInterfaceResponse.newBuilder()
                    .setAppInterfaceId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void cloneApplicationInterface(
            CloneApplicationInterfaceRequest request, StreamObserver<CloneApplicationInterfaceResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String id = applicationCatalogService.cloneApplicationInterface(
                    ctx, request.getAppInterfaceId(), request.getNewApplicationName(), request.getGatewayId());
            observer.onNext(CloneApplicationInterfaceResponse.newBuilder()
                    .setAppInterfaceId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getApplicationInterface(
            GetApplicationInterfaceRequest request, StreamObserver<ApplicationInterfaceDescription> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            ApplicationInterfaceDescription result =
                    applicationCatalogService.getApplicationInterface(ctx, request.getAppInterfaceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateApplicationInterface(UpdateApplicationInterfaceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.updateApplicationInterface(
                    ctx, request.getAppInterfaceId(), request.getApplicationInterface());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteApplicationInterface(DeleteApplicationInterfaceRequest request, StreamObserver<Empty> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            applicationCatalogService.deleteApplicationInterface(ctx, request.getAppInterfaceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllApplicationInterfaceNames(
            GetAllApplicationInterfaceNamesRequest request,
            StreamObserver<GetAllApplicationInterfaceNamesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, String> names =
                    applicationCatalogService.getAllApplicationInterfaceNames(ctx, request.getGatewayId());
            observer.onNext(GetAllApplicationInterfaceNamesResponse.newBuilder()
                    .putAllApplicationInterfaceNames(names)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllApplicationInterfaces(
            GetAllApplicationInterfacesRequest request, StreamObserver<GetAllApplicationInterfacesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<ApplicationInterfaceDescription> interfaces =
                    applicationCatalogService.getAllApplicationInterfaces(ctx, request.getGatewayId());
            observer.onNext(GetAllApplicationInterfacesResponse.newBuilder()
                    .addAllApplicationInterfaces(interfaces)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getApplicationInputs(
            GetApplicationInputsRequest request, StreamObserver<GetApplicationInputsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<InputDataObjectType> inputs =
                    applicationCatalogService.getApplicationInputs(ctx, request.getAppInterfaceId());
            observer.onNext(GetApplicationInputsResponse.newBuilder()
                    .addAllApplicationInputs(inputs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getApplicationOutputs(
            GetApplicationOutputsRequest request, StreamObserver<GetApplicationOutputsResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<OutputDataObjectType> outputs =
                    applicationCatalogService.getApplicationOutputs(ctx, request.getAppInterfaceId());
            observer.onNext(GetApplicationOutputsResponse.newBuilder()
                    .addAllApplicationOutputs(outputs)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAvailableComputeResources(
            GetAvailableComputeResourcesRequest request,
            StreamObserver<GetAvailableComputeResourcesResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            Map<String, String> resources = applicationCatalogService.getAvailableAppInterfaceComputeResources(
                    ctx, request.getAppInterfaceId());
            observer.onNext(GetAvailableComputeResourcesResponse.newBuilder()
                    .putAllComputeResourceNames(resources)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
