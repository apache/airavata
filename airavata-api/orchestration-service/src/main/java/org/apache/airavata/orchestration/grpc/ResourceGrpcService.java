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
package org.apache.airavata.orchestration.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import org.apache.airavata.api.resource.*;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.orchestration.service.ResourceService;
import org.springframework.stereotype.Component;

@Component
public class ResourceGrpcService extends ResourceServiceGrpc.ResourceServiceImplBase {

    private final ResourceService resourceService;

    public ResourceGrpcService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // --- Compute Resources ---

    @Override
    public void registerComputeResource(
            RegisterComputeResourceRequest request, StreamObserver<RegisterComputeResourceResponse> observer) {
        try {
            String id = resourceService.registerComputeResource(request.getComputeResource());
            observer.onNext(RegisterComputeResourceResponse.newBuilder()
                    .setComputeResourceId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getComputeResource(
            GetComputeResourceRequest request, StreamObserver<ComputeResourceDescription> observer) {
        try {
            ComputeResourceDescription result = resourceService.getComputeResource(request.getComputeResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateComputeResource(UpdateComputeResourceRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateComputeResource(request.getComputeResourceId(), request.getComputeResource());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteComputeResource(DeleteComputeResourceRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.deleteComputeResource(request.getComputeResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllComputeResourceNames(
            GetAllComputeResourceNamesRequest request, StreamObserver<GetAllComputeResourceNamesResponse> observer) {
        try {
            Map<String, String> names = resourceService.getAllComputeResourceNames();
            observer.onNext(GetAllComputeResourceNamesResponse.newBuilder()
                    .putAllComputeResourceNames(names)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Storage Resources ---

    @Override
    public void registerStorageResource(
            RegisterStorageResourceRequest request, StreamObserver<RegisterStorageResourceResponse> observer) {
        try {
            String id = resourceService.registerStorageResource(request.getStorageResource());
            observer.onNext(RegisterStorageResourceResponse.newBuilder()
                    .setStorageResourceId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getStorageResource(
            GetStorageResourceRequest request, StreamObserver<StorageResourceDescription> observer) {
        try {
            StorageResourceDescription result = resourceService.getStorageResource(request.getStorageResourceId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateStorageResource(UpdateStorageResourceRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateStorageResource(request.getStorageResourceId(), request.getStorageResource());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteStorageResource(DeleteStorageResourceRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.deleteStorageResource(request.getStorageResourceId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getAllStorageResourceNames(
            GetAllStorageResourceNamesRequest request, StreamObserver<GetAllStorageResourceNamesResponse> observer) {
        try {
            Map<String, String> names = resourceService.getAllStorageResourceNames();
            observer.onNext(GetAllStorageResourceNamesResponse.newBuilder()
                    .putAllStorageResourceNames(names)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Batch Queue ---

    @Override
    public void deleteBatchQueue(DeleteBatchQueueRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.deleteBatchQueue(request.getComputeResourceId(), request.getQueueName());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
