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
import org.apache.airavata.model.appcatalog.computeresource.proto.CloudJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.LOCALSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.computeresource.proto.UnicoreJobSubmission;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.data.movement.proto.DMType;
import org.apache.airavata.model.data.movement.proto.GridFTPDataMovement;
import org.apache.airavata.model.data.movement.proto.LOCALDataMovement;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
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

    // --- Job Submissions ---

    @Override
    public void addLocalSubmission(
            AddLocalSubmissionRequest request, StreamObserver<AddLocalSubmissionResponse> observer) {
        try {
            String id = resourceService.addLocalSubmissionDetails(
                    request.getComputeResourceId(), request.getPriority(), request.getLocalSubmission());
            observer.onNext(
                    AddLocalSubmissionResponse.newBuilder().setSubmissionId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateLocalSubmission(UpdateLocalSubmissionRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateLocalSubmissionDetails(request.getSubmissionId(), request.getLocalSubmission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getLocalJobSubmission(GetLocalJobSubmissionRequest request, StreamObserver<LOCALSubmission> observer) {
        try {
            LOCALSubmission result = resourceService.getLocalJobSubmission(request.getSubmissionId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addSSHJobSubmission(
            AddSSHJobSubmissionRequest request, StreamObserver<AddSSHJobSubmissionResponse> observer) {
        try {
            String id = resourceService.addSSHJobSubmissionDetails(
                    request.getComputeResourceId(), request.getPriority(), request.getSshJobSubmission());
            observer.onNext(
                    AddSSHJobSubmissionResponse.newBuilder().setSubmissionId(id).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addSSHForkJobSubmission(
            AddSSHForkJobSubmissionRequest request, StreamObserver<AddSSHForkJobSubmissionResponse> observer) {
        try {
            String id = resourceService.addSSHForkJobSubmissionDetails(
                    request.getComputeResourceId(), request.getPriority(), request.getSshJobSubmission());
            observer.onNext(AddSSHForkJobSubmissionResponse.newBuilder()
                    .setSubmissionId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getSSHJobSubmission(GetSSHJobSubmissionRequest request, StreamObserver<SSHJobSubmission> observer) {
        try {
            SSHJobSubmission result = resourceService.getSSHJobSubmission(request.getSubmissionId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateSSHJobSubmission(UpdateSSHJobSubmissionRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateSSHJobSubmissionDetails(request.getSubmissionId(), request.getSshJobSubmission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addCloudJobSubmission(
            AddCloudJobSubmissionRequest request, StreamObserver<AddCloudJobSubmissionResponse> observer) {
        try {
            String id = resourceService.addCloudJobSubmissionDetails(
                    request.getComputeResourceId(), request.getPriority(), request.getCloudJobSubmission());
            observer.onNext(AddCloudJobSubmissionResponse.newBuilder()
                    .setSubmissionId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getCloudJobSubmission(
            GetCloudJobSubmissionRequest request, StreamObserver<CloudJobSubmission> observer) {
        try {
            CloudJobSubmission result = resourceService.getCloudJobSubmission(request.getSubmissionId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateCloudJobSubmission(UpdateCloudJobSubmissionRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateCloudJobSubmissionDetails(request.getSubmissionId(), request.getCloudJobSubmission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addUnicoreJobSubmission(
            AddUnicoreJobSubmissionRequest request, StreamObserver<AddUnicoreJobSubmissionResponse> observer) {
        try {
            String id = resourceService.addUNICOREJobSubmissionDetails(
                    request.getComputeResourceId(), request.getPriority(), request.getUnicoreJobSubmission());
            observer.onNext(AddUnicoreJobSubmissionResponse.newBuilder()
                    .setSubmissionId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getUnicoreJobSubmission(
            GetUnicoreJobSubmissionRequest request, StreamObserver<UnicoreJobSubmission> observer) {
        try {
            UnicoreJobSubmission result = resourceService.getUnicoreJobSubmission(request.getSubmissionId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateUnicoreJobSubmission(UpdateUnicoreJobSubmissionRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateUnicoreJobSubmissionDetails(
                    request.getSubmissionId(), request.getUnicoreJobSubmission());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteJobSubmissionInterface(
            DeleteJobSubmissionInterfaceRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.deleteJobSubmissionInterface(request.getComputeResourceId(), request.getSubmissionId());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    // --- Data Movements ---

    @Override
    public void addLocalDataMovement(
            AddLocalDataMovementRequest request, StreamObserver<AddLocalDataMovementResponse> observer) {
        try {
            DMType dmType = DMType.valueOf(request.getDmType());
            String id = resourceService.addLocalDataMovementDetails(
                    request.getComputeResourceId(), dmType, request.getPriority(), request.getLocalDataMovement());
            observer.onNext(AddLocalDataMovementResponse.newBuilder()
                    .setDataMovementId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateLocalDataMovement(UpdateLocalDataMovementRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateLocalDataMovementDetails(request.getDataMovementId(), request.getLocalDataMovement());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getLocalDataMovement(GetLocalDataMovementRequest request, StreamObserver<LOCALDataMovement> observer) {
        try {
            LOCALDataMovement result = resourceService.getLocalDataMovement(request.getDataMovementId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addSCPDataMovement(
            AddSCPDataMovementRequest request, StreamObserver<AddSCPDataMovementResponse> observer) {
        try {
            DMType dmType = DMType.valueOf(request.getDmType());
            String id = resourceService.addSCPDataMovementDetails(
                    request.getComputeResourceId(), dmType, request.getPriority(), request.getScpDataMovement());
            observer.onNext(AddSCPDataMovementResponse.newBuilder()
                    .setDataMovementId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateSCPDataMovement(UpdateSCPDataMovementRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateSCPDataMovementDetails(request.getDataMovementId(), request.getScpDataMovement());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getSCPDataMovement(GetSCPDataMovementRequest request, StreamObserver<SCPDataMovement> observer) {
        try {
            SCPDataMovement result = resourceService.getSCPDataMovement(request.getDataMovementId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void addGridFTPDataMovement(
            AddGridFTPDataMovementRequest request, StreamObserver<AddGridFTPDataMovementResponse> observer) {
        try {
            DMType dmType = DMType.valueOf(request.getDmType());
            String id = resourceService.addGridFTPDataMovementDetails(
                    request.getComputeResourceId(), dmType, request.getPriority(), request.getGridftpDataMovement());
            observer.onNext(AddGridFTPDataMovementResponse.newBuilder()
                    .setDataMovementId(id)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void updateGridFTPDataMovement(UpdateGridFTPDataMovementRequest request, StreamObserver<Empty> observer) {
        try {
            resourceService.updateGridFTPDataMovementDetails(
                    request.getDataMovementId(), request.getGridftpDataMovement());
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getGridFTPDataMovement(
            GetGridFTPDataMovementRequest request, StreamObserver<GridFTPDataMovement> observer) {
        try {
            GridFTPDataMovement result = resourceService.getGridFTPDataMovement(request.getDataMovementId());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteDataMovementInterface(
            DeleteDataMovementInterfaceRequest request, StreamObserver<Empty> observer) {
        try {
            // TODO: The service layer expects a DMType but the proto doesn't include it.
            // Passing COMPUTE as default — revisit when mappers are added.
            resourceService.deleteDataMovementInterface(
                    request.getComputeResourceId(), request.getDataMovementId(), DMType.COMPUTE_RESOURCE);
            observer.onNext(Empty.getDefaultInstance());
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
