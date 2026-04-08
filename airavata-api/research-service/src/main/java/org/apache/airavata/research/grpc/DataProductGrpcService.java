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
import org.apache.airavata.api.dataproduct.*;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.interfaces.StorageRegistry;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.springframework.stereotype.Component;

@Component
public class DataProductGrpcService extends DataProductServiceGrpc.DataProductServiceImplBase {

    private final StorageRegistry storageRegistry;

    public DataProductGrpcService(StorageRegistry storageRegistry) {
        this.storageRegistry = storageRegistry;
    }

    @Override
    public void registerDataProduct(
            RegisterDataProductRequest request, StreamObserver<RegisterDataProductResponse> observer) {
        try {
            String uri = storageRegistry.registerDataProduct(request.getDataProduct());
            observer.onNext(
                    RegisterDataProductResponse.newBuilder().setProductUri(uri).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDataProduct(GetDataProductRequest request, StreamObserver<DataProductModel> observer) {
        try {
            DataProductModel result = storageRegistry.getDataProduct(request.getProductUri());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void registerReplicaLocation(
            RegisterReplicaLocationRequest request, StreamObserver<RegisterReplicaLocationResponse> observer) {
        try {
            String id = storageRegistry.registerReplicaLocation(request.getReplicaLocation());
            observer.onNext(RegisterReplicaLocationResponse.newBuilder()
                    .setReplicaId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getParentDataProduct(GetParentDataProductRequest request, StreamObserver<DataProductModel> observer) {
        try {
            DataProductModel result = storageRegistry.getParentDataProduct(request.getProductUri());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getChildDataProducts(
            GetChildDataProductsRequest request, StreamObserver<GetChildDataProductsResponse> observer) {
        try {
            List<DataProductModel> results = storageRegistry.getChildDataProducts(request.getProductUri());
            observer.onNext(GetChildDataProductsResponse.newBuilder()
                    .addAllDataProducts(results)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateDataProduct(UpdateDataProductRequest request, StreamObserver<Empty> observer) {
        try {
            storageRegistry.updateDataProduct(request.getDataProduct());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteDataProduct(DeleteDataProductRequest request, StreamObserver<Empty> observer) {
        try {
            storageRegistry.removeDataProduct(request.getProductUri());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getReplicaLocation(
            GetReplicaLocationRequest request, StreamObserver<DataReplicaLocationModel> observer) {
        try {
            DataReplicaLocationModel result = storageRegistry.getReplicaLocation(request.getReplicaId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateReplicaLocation(UpdateReplicaLocationRequest request, StreamObserver<Empty> observer) {
        try {
            storageRegistry.updateReplicaLocation(request.getReplicaLocation());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteReplicaLocation(DeleteReplicaLocationRequest request, StreamObserver<Empty> observer) {
        try {
            storageRegistry.removeReplicaLocation(request.getReplicaId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }
}
