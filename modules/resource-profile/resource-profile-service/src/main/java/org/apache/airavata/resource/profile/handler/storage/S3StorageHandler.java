/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.airavata.resource.profile.handler.storage;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.resource.profile.service.s3.*;
import org.apache.airavata.resource.profile.sharing.SharingImpl;
import org.apache.airavata.resource.profile.sharing.SharingImpl.AccessLevel;
import org.apache.airavata.resource.profile.storage.s3.entity.S3StoGroupPreferenceEntity;
import org.apache.airavata.resource.profile.storage.s3.entity.S3StoGroupPreferenceId;
import org.apache.airavata.resource.profile.storage.s3.entity.S3StoGroupResourceProfileEntity;
import org.apache.airavata.resource.profile.storage.s3.entity.S3StorageEntity;
import org.apache.airavata.resource.profile.storage.s3.repository.S3StoGroupPreferenceRepository;
import org.apache.airavata.resource.profile.storage.s3.repository.S3StoGroupResourceProfileRepository;
import org.apache.airavata.resource.profile.storage.s3.repository.S3StorageRepository;
import org.apache.airavata.resource.profile.stubs.s3.S3Storage;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GRpcService
public class S3StorageHandler extends S3StorageServiceGrpc.S3StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageHandler.class);

    @Autowired
    private S3StorageRepository storageRepository;

    @Autowired
    private S3StoGroupPreferenceRepository groupPreferenceRepository;

    @Autowired
    private S3StoGroupResourceProfileRepository groupResourceProfileRepository;

    @Autowired
    private SharingImpl sharingImpl;

    private DozerBeanMapper mapper = new DozerBeanMapper();

    @Override
    public void createS3Storage(S3StorageCreateRequest request, StreamObserver<S3StorageCreateResponse> responseObserver) {

        try {
            S3StorageEntity savedEntity = storageRepository.save(mapper.map(request.getS3Storage(), S3StorageEntity.class));

            sharingImpl.createSharingEntity("S3", savedEntity.getS3StorageId(), request.getAuthzToken(), AccessLevel.WRITE);

            S3Storage savedStorage = mapper.map(savedEntity, S3Storage.newBuilder().getClass()).build();
            responseObserver.onNext(S3StorageCreateResponse.newBuilder().setS3Storage(savedStorage).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the S3 Storage with bucket name {}", request.getS3Storage().getBucketName(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the S3 Storage with bucket name " + request.getS3Storage().getBucketName())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3Storage(S3StorageUpdateRequest request, StreamObserver<Empty> responseObserver) {

        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3Storage().getS3StorageId(), request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getS3Storage().getS3StorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getS3Storage().getS3StorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 storage with id" + request.getS3Storage().getS3StorageId() + " does not exist")
                        .asRuntimeException());
            }

            storageRepository.save(mapper.map(request.getS3Storage(), S3StorageEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the S3 Storage with id {}", request.getS3Storage().getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the S3 Storage with id " + request.getS3Storage().getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeS3Storage(S3StorageRemoveRequest request, StreamObserver<Empty> responseObserver) {

        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StorageId(), request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getS3StorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getS3StorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 storage with id " + request.getS3StorageId() + " does not exist")
                        .asRuntimeException());
            }

            storageRepository.deleteById(request.getS3StorageId());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the S3 Storage with id {}", request.getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the S3 Storage with id " + request.getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchS3Storage(S3StorageFetchRequest request, StreamObserver<S3StorageFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StorageId(), request.getAuthzToken(), AccessLevel.READ);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getS3StorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getS3StorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 storage with id " + request.getS3StorageId() + " does not exist")
                        .asRuntimeException());
            }

            Optional<S3StorageEntity> storageEntityOp = storageRepository.findById(request.getS3StorageId());

            S3Storage fetchedStorage = mapper.map(storageEntityOp.get(), S3Storage.newBuilder().getClass()).build();
            responseObserver.onNext(S3StorageFetchResponse.newBuilder().setS3Storage(fetchedStorage).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the S3 Storage with id {}", request.getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the S3 Storage with id " + request.getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listS3Storage(S3StorageListRequest request, StreamObserver<S3StorageListResponse> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("Method not implemented")
                .asRuntimeException());
    }

    @Override
    public void createS3StoGroupPreference(S3StoGroupPreferenceCreateRequest request, StreamObserver<S3StoGroupPreferenceCreateResponse> responseObserver) {

        try {
            S3StoGroupPreferenceEntity savedEntity = groupPreferenceRepository.save(mapper.map(request.getS3StoGroupPreference(), S3StoGroupPreferenceEntity.class));

            sharingImpl.createSharingEntity("S3",
                    savedEntity.getS3StorageId() + "-" + savedEntity.getS3GroupResourceProfileId(),
                    request.getAuthzToken(), AccessLevel.WRITE);

            responseObserver.onNext(mapper.map(savedEntity, S3StoGroupPreferenceCreateResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the S3 storage group preference with storage id {}",
                    request.getS3StoGroupPreference().getS3StorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the S3 storage group preference with storage id " +
                            request.getS3StoGroupPreference().getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3StoGroupPreference(S3StoGroupPreferenceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StoGroupPreference().getS3StorageId() + "-" + request.getS3StoGroupPreference().getS3GroupResourceProfileId(),
                    request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id " +
                                request.getS3StoGroupPreference().getS3StorageId())
                        .asRuntimeException());
            }

            boolean exists = groupPreferenceRepository.existsById(new S3StoGroupPreferenceId(
                    request.getS3StoGroupPreference().getS3StorageId(),
                    request.getS3StoGroupPreference().getS3GroupResourceProfileId()));

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 storage with id" + request.getS3StoGroupPreference().getS3StorageId() + " does not exist")
                        .asRuntimeException());
            }

            groupPreferenceRepository.save(mapper.map(request.getS3StoGroupPreference(), S3StoGroupPreferenceEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the S3 Storage with id {}", request.getS3StoGroupPreference().getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the S3 Storage with id " + request.getS3StoGroupPreference().getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeS3StoGroupPreference(S3StoGroupPreferenceRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StorageId() + "-" + request.getS3GroupResourceProfileId(),
                    request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id  " +
                                request.getS3StorageId())
                        .asRuntimeException());
            }

            S3StoGroupPreferenceId id = new S3StoGroupPreferenceId(
                    request.getS3StorageId(),
                    request.getS3GroupResourceProfileId());

            boolean exists = groupPreferenceRepository.existsById(id);

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 storage group preference with storage id " + request.getS3StorageId() + " does not exist")
                        .asRuntimeException());
            }

            groupPreferenceRepository.deleteById(id);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the S3 storage group preference with storage id {}", request.getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the S3 storage group preference with storage id " + request.getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchS3StoGroupPreference(S3StoGroupPreferenceFetchRequest request, StreamObserver<S3StoGroupPreferenceFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StorageId() + "-" + request.getS3GroupResourceProfileId(),
                    request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id  " +
                                request.getS3StorageId())
                        .asRuntimeException());
            }

            S3StoGroupPreferenceId id = new S3StoGroupPreferenceId(
                    request.getS3StorageId(),
                    request.getS3GroupResourceProfileId());

            boolean exists = groupPreferenceRepository.existsById(id);

            Optional<S3StoGroupPreferenceEntity> groupPreferenceOp = groupPreferenceRepository.findById(id);

            responseObserver.onNext(mapper.map(groupPreferenceOp.get(),
                    S3StoGroupPreferenceFetchResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the S3 storage group preference with storage id {}", request.getS3StorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the S3 storage group preference with storage id  " + request.getS3StorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createS3StoGroupResourceProfile(S3StoGroupResourceProfileCreateRequest request, StreamObserver<S3StoGroupResourceProfileCreateResponse> responseObserver) {
        try {
            S3StoGroupResourceProfileEntity savedEntity = groupResourceProfileRepository.save(
                    mapper.map(request.getS3StoGroupResourceProfile(), S3StoGroupResourceProfileEntity.class));

            sharingImpl.createSharingEntity("S3", savedEntity.getS3GroupResourceProfileId(),
                    request.getAuthzToken(), AccessLevel.WRITE);

            responseObserver.onNext(mapper.map(savedEntity,
                    S3StoGroupResourceProfileCreateResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the S3 group resource profile with name {}",
                    request.getS3StoGroupResourceProfile().getS3GroupResourceProfileName(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the S3 group resource profile with name " +
                            request.getS3StoGroupResourceProfile().getS3GroupResourceProfileName())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3StoGroupResourceProfile(S3StoGroupResourceProfileUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId(), request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for S3 group resource profile with id " +
                                request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(
                    request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 group resource profile with id " +
                                request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            groupResourceProfileRepository.save(mapper.map(request.getS3StoGroupResourceProfile(),
                    S3StoGroupResourceProfileEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the S3 group resource profile with id {}",
                    request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the S3 group resource profile with id " +
                            request.getS3StoGroupResourceProfile().getS3GroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeS3StoGroupResourceProfile(S3StoGroupResourceProfileRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StoGroupResourceProfileId(), request.getAuthzToken(), AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for S3 group resource profile with id " +
                                request.getS3StoGroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(request.getS3StoGroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 group resource profile with id " + request.getS3StoGroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            groupResourceProfileRepository.deleteById(request.getS3StoGroupResourceProfileId());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the S3 group resource profile with id {}", request.getS3StoGroupResourceProfileId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the S3 group resource profile with id {} " + request.getS3StoGroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchS3StoGroupResourceProfile(S3StoGroupResourceProfileFetchRequest request, StreamObserver<S3StoGroupResourceProfileFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("S3",
                    request.getS3StoGroupResourceProfileId(), request.getAuthzToken(), AccessLevel.READ);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for S3 group resource profile with id " +
                                request.getS3StoGroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(request.getS3StoGroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("S3 group resource profile with id " + request.getS3StoGroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            Optional<S3StoGroupResourceProfileEntity> groupResProfOp = groupResourceProfileRepository.findById(request.getS3StoGroupResourceProfileId());

            responseObserver.onNext(mapper.map(groupResProfOp.get(),
                    S3StoGroupResourceProfileFetchResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the S3 group resource profile with id {}", request.getS3StoGroupResourceProfileId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the S3 group resource profile with id " + request.getS3StoGroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listS3StoGroupResourceProfile(S3StoGroupResourceProfileListRequest request, StreamObserver<S3StoGroupResourceProfileListResponse> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("Method not implemented")
                .asRuntimeException());
    }
}
