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
import org.apache.airavata.resource.profile.service.s3.S3StorageCreateResponse;
import org.apache.airavata.resource.profile.service.s3.S3StorageFetchResponse;
import org.apache.airavata.resource.profile.service.scp.*;
import org.apache.airavata.resource.profile.sharing.SharingImpl;
import org.apache.airavata.resource.profile.storage.scp.entity.SCPStoGroupPreferenceEntity;
import org.apache.airavata.resource.profile.storage.scp.entity.SCPStoGroupPreferenceId;
import org.apache.airavata.resource.profile.storage.scp.entity.SCPStoGroupResourceProfileEntity;
import org.apache.airavata.resource.profile.storage.scp.entity.SCPStorageEntity;
import org.apache.airavata.resource.profile.storage.scp.repository.SCPStoGroupPreferenceRepository;
import org.apache.airavata.resource.profile.storage.scp.repository.SCPStoGroupResourceProfileRepository;
import org.apache.airavata.resource.profile.storage.scp.repository.SCPStorageRepository;
import org.apache.airavata.resource.profile.stubs.s3.S3Storage;
import org.apache.airavata.resource.profile.stubs.scp.SCPStorage;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GRpcService
public class SCPStorageHandler extends SCPStorageServiceGrpc.SCPStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SCPStorageHandler.class);

    @Autowired
    private SCPStorageRepository storageRepository;

    @Autowired
    private SCPStoGroupPreferenceRepository groupPreferenceRepository;

    @Autowired
    private SCPStoGroupResourceProfileRepository groupResourceProfileRepository;

    @Autowired
    private SharingImpl sharingImpl;

    private DozerBeanMapper mapper = new DozerBeanMapper();

    @Override
    public void createSCPStorage(SCPStorageCreateRequest request, StreamObserver<SCPStorageCreateResponse> responseObserver) {
        try {
            SCPStorageEntity savedEntity = storageRepository.save(mapper.map(request.getScpStorage(), SCPStorageEntity.class));

            sharingImpl.createSharingEntity("SCP", savedEntity.getScpStorageId(), request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            SCPStorage savedStorage = mapper.map(savedEntity, SCPStorage.newBuilder().getClass()).build();
            responseObserver.onNext(SCPStorageCreateResponse.newBuilder().setScpStorage(savedStorage).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the SCP Storage with host name {}", request.getScpStorage().getHostName(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the SCP Storage with host name " + request.getScpStorage().getHostName())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSCPStorage(SCPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStorage().getScpStorageId(), request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getScpStorage().getScpStorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getScpStorage().getScpStorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP storage with id" + request.getScpStorage().getScpStorageId() + " does not exist")
                        .asRuntimeException());
            }

            storageRepository.save(mapper.map(request.getScpStorage(), SCPStorageEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the SCP Storage with id {}", request.getScpStorage().getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the SCP Storage with id " + request.getScpStorage().getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeSCPStorage(SCPStorageRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStorageId(), request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getScpStorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getScpStorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP storage with id " + request.getScpStorageId() + " does not exist")
                        .asRuntimeException());
            }

            storageRepository.deleteById(request.getScpStorageId());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the SCP Storage with id {}", request.getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the SCP Storage with id {} " + request.getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchSCPStorage(SCPStorageFetchRequest request, StreamObserver<SCPStorageFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStorageId(), request.getAuthzToken(), SharingImpl.AccessLevel.READ);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage " + request.getScpStorageId())
                        .asRuntimeException());
            }

            boolean exists = storageRepository.existsById(request.getScpStorageId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP storage with id" + request.getScpStorageId() + " does not exist")
                        .asRuntimeException());
            }

            Optional<SCPStorageEntity> storageEntityOp = storageRepository.findById(request.getScpStorageId());

            SCPStorage fetchedStorage = mapper.map(storageEntityOp.get(), SCPStorage.newBuilder().getClass()).build();
            responseObserver.onNext(SCPStorageFetchResponse.newBuilder().setScpStorage(fetchedStorage).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the SCP Storage with id {}", request.getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the SCP Storage with id " + request.getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listSCPStorage(SCPStorageListRequest request, StreamObserver<SCPStorageListResponse> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("Method not implemented")
                .asRuntimeException());
    }

    @Override
    public void createSCPStoGroupPreference(SCPStoGroupPreferenceCreateRequest request, StreamObserver<SCPStoGroupPreferenceCreateResponse> responseObserver) {
        try {
            SCPStoGroupPreferenceEntity savedEntity = groupPreferenceRepository.save(mapper.map(request.getScpStoGroupPreference(), SCPStoGroupPreferenceEntity.class));

            sharingImpl.createSharingEntity("SCP",
                    savedEntity.getScpStorageId() + "-" + savedEntity.getScpGroupResourceProfileId(),
                    request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            responseObserver.onNext(mapper.map(savedEntity, SCPStoGroupPreferenceCreateResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the SCP storage group preference with storage id {}",
                    request.getScpStoGroupPreference().getScpStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the SCP storage group preference with storage id " +
                            request.getScpStoGroupPreference().getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSCPStoGroupPreference(SCPStoGroupPreferenceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStoGroupPreference().getScpStorageId() + "-" + request.getScpStoGroupPreference().getScpGroupResourceProfileId(),
                    request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id " +
                                request.getScpStoGroupPreference().getScpStorageId())
                        .asRuntimeException());
            }

            boolean exists = groupPreferenceRepository.existsById(new SCPStoGroupPreferenceId(
                    request.getScpStoGroupPreference().getScpStorageId(),
                    request.getScpStoGroupPreference().getScpGroupResourceProfileId()));

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP storage with id" + request.getScpStoGroupPreference().getScpStorageId() + " does not exist")
                        .asRuntimeException());
            }

            groupPreferenceRepository.save(mapper.map(request.getScpStoGroupPreference(), SCPStoGroupPreferenceEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the SCP Storage with id {}", request.getScpStoGroupPreference().getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the SCP Storage with id " + request.getScpStoGroupPreference().getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeSCPStoGroupPreference(SCPStoGroupPreferenceRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStorageId() + "-" + request.getScpGroupResourceProfileId(),
                    request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id  " +
                                request.getScpStorageId())
                        .asRuntimeException());
            }

            SCPStoGroupPreferenceId id = new SCPStoGroupPreferenceId(
                    request.getScpStorageId(),
                    request.getScpGroupResourceProfileId());

            boolean exists = groupPreferenceRepository.existsById(id);

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP storage group preference with storage id " + request.getScpStorageId() + " does not exist")
                        .asRuntimeException());
            }

            groupPreferenceRepository.deleteById(id);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the SCP storage group preference with storage id {}", request.getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the SCP storage group preference with storage id " + request.getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchSCPStoGroupPreference(SCPStoGroupPreferenceFetchRequest request, StreamObserver<SCPStoGroupPreferenceFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStorageId() + "-" + request.getScpGroupResourceProfileId(),
                    request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for storage group preference with storage id  " +
                                request.getScpStorageId())
                        .asRuntimeException());
            }

            SCPStoGroupPreferenceId id = new SCPStoGroupPreferenceId(
                    request.getScpStorageId(),
                    request.getScpGroupResourceProfileId());

            boolean exists = groupPreferenceRepository.existsById(id);

            Optional<SCPStoGroupPreferenceEntity> groupPreferenceOp = groupPreferenceRepository.findById(id);

            responseObserver.onNext(mapper.map(groupPreferenceOp.get(),
                    SCPStoGroupPreferenceFetchResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the SCP storage group preference with storage id {}", request.getScpStorageId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the SCP storage group preference with storage id  " + request.getScpStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSCPStoGroupResourceProfile(SCPStoGroupResourceProfileCreateRequest request, StreamObserver<SCPStoGroupResourceProfileCreateResponse> responseObserver) {
        try {
            SCPStoGroupResourceProfileEntity savedEntity = groupResourceProfileRepository.save(
                    mapper.map(request.getScpStoGroupResourceProfile(), SCPStoGroupResourceProfileEntity.class));

            sharingImpl.createSharingEntity("SCP", savedEntity.getScpGroupResourceProfileId(),
                    request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            responseObserver.onNext(mapper.map(savedEntity,
                    SCPStoGroupResourceProfileCreateResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to create the SCP group resource profile with name {}",
                    request.getScpStoGroupResourceProfile().getScpGroupResourceProfileName(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to create the SCP group resource profile with name " +
                            request.getScpStoGroupResourceProfile().getScpGroupResourceProfileName())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSCPStoGroupResourceProfile(SCPStoGroupResourceProfileUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId(), request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for SCP group resource profile with id " +
                                request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(
                    request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP group resource profile with id " +
                                request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            groupResourceProfileRepository.save(mapper.map(request.getScpStoGroupResourceProfile(),
                    SCPStoGroupResourceProfileEntity.class));
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to update the SCP group resource profile with id {}",
                    request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to update the SCP group resource profile with id " +
                            request.getScpStoGroupResourceProfile().getScpGroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void removeSCPStoGroupResourceProfile(SCPStoGroupResourceProfileRemoveRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStoGroupResourceProfileId(), request.getAuthzToken(), SharingImpl.AccessLevel.WRITE);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for SCP group resource profile with id " +
                                request.getScpStoGroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(request.getScpStoGroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP group resource profile with id " + request.getScpStoGroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            groupResourceProfileRepository.deleteById(request.getScpStoGroupResourceProfileId());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to remove the SCP group resource profile with id {}", request.getScpStoGroupResourceProfileId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to remove the SCP group resource profile with id {} " + request.getScpStoGroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void fetchSCPStoGroupResourceProfile(SCPStoGroupResourceProfileFetchRequest request, StreamObserver<SCPStoGroupResourceProfileFetchResponse> responseObserver) {
        try {
            boolean hasAccess = sharingImpl.hasAccess("SCP",
                    request.getScpStoGroupResourceProfileId(), request.getAuthzToken(), SharingImpl.AccessLevel.READ);

            if (!hasAccess) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("User does not have access for SCP group resource profile with id " +
                                request.getScpStoGroupResourceProfileId())
                        .asRuntimeException());
            }

            boolean exists = groupResourceProfileRepository.existsById(request.getScpStoGroupResourceProfileId());

            if (!exists) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("SCP group resource profile with id " + request.getScpStoGroupResourceProfileId() + " does not exist")
                        .asRuntimeException());
            }

            Optional<SCPStoGroupResourceProfileEntity> groupResProfOp = groupResourceProfileRepository.findById(request.getScpStoGroupResourceProfileId());

            responseObserver.onNext(mapper.map(groupResProfOp.get(),
                    SCPStoGroupResourceProfileFetchResponse.newBuilder().getClass()).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Failed to fetch the SCP group resource profile with id {}", request.getScpStoGroupResourceProfileId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed to fetch the SCP group resource profile with id " + request.getScpStoGroupResourceProfileId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listSCPStoGroupResourceProfile(SCPStoGroupResourceProfileListRequest request, StreamObserver<SCPStoGroupResourceProfileListResponse> responseObserver) {
        responseObserver.onError(Status.INTERNAL
                .withDescription("Method not implemented")
                .asRuntimeException());
    }
}
