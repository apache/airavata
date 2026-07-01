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
package org.apache.airavata.storage.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.List;
import org.apache.airavata.api.file.*;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.grpc.GrpcStatusMapper;
import org.apache.airavata.interfaces.ExperimentRegistry;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.interfaces.StorageProvider;
import org.apache.airavata.interfaces.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.model.data.replica.proto.DataProductModel;
import org.apache.airavata.model.data.replica.proto.DataReplicaLocationModel;
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.storage.StoragePathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserStorageGrpcService extends UserStorageServiceGrpc.UserStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserStorageGrpcService.class);

    private final ExperimentRegistry experimentRegistry;
    private final GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;
    private final StorageProvider storageProvider;
    private final StoragePathResolver storagePathResolver;

    public UserStorageGrpcService(
            ExperimentRegistry experimentRegistry,
            GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider,
            StorageProvider storageProvider,
            StoragePathResolver storagePathResolver) {
        this.experimentRegistry = experimentRegistry;
        this.gatewayStoragePreferenceProvider = gatewayStoragePreferenceProvider;
        this.storageProvider = storageProvider;
        this.storagePathResolver = storagePathResolver;
    }

    /**
     * Resolve (registering if necessary) the data product URI for a stored file, so listings and
     * metadata can expose a stable URI per file. Returns "" for directories or on any failure
     * (best-effort: a missing data product URI must not fail the listing).
     */
    private String resolveDataProductUri(FileMetadata meta, String path, String storageResourceId) {
        if (meta.isDirectory()) {
            return "";
        }
        try {
            RequestContext ctx = GrpcRequestContext.current();
            String uri = storageProvider.getOrCreateDataProductByPath(
                    ctx.getGatewayId(), ctx.getUserId(), meta.getName(), path, storageResourceId);
            return uri != null ? uri : "";
        } catch (Exception e) {
            logger.warn("Could not resolve data product URI for {}", path, e);
            return "";
        }
    }

    /** Resolve the effective storage resource id: the request's, else the gateway default. */
    private String resolveStorageResourceId(String storageResourceId) throws Exception {
        return storagePathResolver.resolveStorageResourceId(storageResourceId);
    }

    private StorageResourceAdaptor getStorageAdaptor(String storageResourceId) throws Exception {
        return storagePathResolver.getStorageAdaptor(storageResourceId);
    }

    private String resolvePath(String path, String storageResourceId) throws Exception {
        return storagePathResolver.resolvePath(path, storageResourceId);
    }

    @Override
    public void uploadFile(UploadFileRequest request, StreamObserver<DataProductModel> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String remotePath = resolvePath(request.getPath(), request.getStorageResourceId());

            FileMetadata metadata = new FileMetadata();
            metadata.setName(request.getName());
            metadata.setSize(request.getContent().size());

            // Ensure parent directory exists
            String parentDir = Paths.get(remotePath).getParent().toString();
            adaptor.createDirectory(parentDir, true);

            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(request.getContent().toByteArray());
            adaptor.uploadFile(inputStream, metadata, remotePath);

            // Return a minimal DataProductModel — caller can register via DataProductService if needed
            DataProductModel product = DataProductModel.newBuilder()
                    .setProductName(request.getName())
                    .build();
            observer.onNext(product);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void downloadFile(DownloadFileRequest request, StreamObserver<DownloadFileResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String remotePath = resolvePath(request.getPath(), request.getStorageResourceId());

            FileMetadata metadata = adaptor.getFileMetadata(remotePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            adaptor.downloadFile(remotePath, outputStream, metadata);

            observer.onNext(DownloadFileResponse.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(outputStream.toByteArray()))
                    .setName(metadata.getName())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void downloadDataProduct(
            DownloadDataProductRequest request, StreamObserver<DownloadFileResponse> observer) {
        try {
            DataProductModel product = storageProvider.getDataProduct(request.getProductUri());
            if (product == null) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("Data product " + request.getProductUri() + " does not exist")
                        .asRuntimeException());
                return;
            }
            if (product.getReplicaLocationsCount() == 0) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("No replica locations for data product " + request.getProductUri())
                        .asRuntimeException());
                return;
            }
            DataReplicaLocationModel replica = product.getReplicaLocations(0);
            String filePath = replica.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("No replica file path for data product " + request.getProductUri())
                        .asRuntimeException());
                return;
            }
            // Mirror the SDK's data_product_file_path: the storage facade expects a full path
            // (absolute or ~/-prefixed); a bare relative replica path is anchored to the home root.
            if (!filePath.startsWith("/") && !filePath.startsWith("~/")) {
                filePath = "~/" + filePath;
            }

            String storageResourceId = replica.getStorageResourceId();
            StorageResourceAdaptor adaptor = getStorageAdaptor(storageResourceId);
            String remotePath = resolvePath(filePath, storageResourceId);

            FileMetadata metadata = adaptor.getFileMetadata(remotePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            adaptor.downloadFile(remotePath, outputStream, metadata);

            observer.onNext(DownloadFileResponse.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(outputStream.toByteArray()))
                    .setName(metadata.getName())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void fileExists(FileExistsRequest request, StreamObserver<FileExistsResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            boolean exists = adaptor.doesFileExist(path);
            observer.onNext(FileExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void dirExists(DirExistsRequest request, StreamObserver<DirExistsResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            // Check existence via getFileMetadata — if it's a directory, it exists
            boolean exists = false;
            try {
                FileMetadata metadata = adaptor.getFileMetadata(path);
                exists = metadata.isDirectory();
            } catch (Exception e) {
                // getFileMetadata collapses not-found AND permission/IO errors into a generic
                // AgentException; log the cause so genuine storage/permission failures stay
                // traceable instead of masquerading as "directory not found".
                logger.debug("dirExists: getFileMetadata failed for {} (treating as not-found)", path, e);
                exists = false;
            }
            observer.onNext(DirExistsResponse.newBuilder().setExists(exists).build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void listDir(ListDirRequest request, StreamObserver<ListDirResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            String storageResourceId = resolveStorageResourceId(request.getStorageResourceId());

            List<String> entries = adaptor.listDirectory(path);
            ListDirResponse.Builder responseBuilder = ListDirResponse.newBuilder();

            for (String entry : entries) {
                String fullPath = path.endsWith("/") ? path + entry : path + "/" + entry;
                FileMetadata meta = adaptor.getFileMetadata(fullPath);
                FileMetadataResponse fileMeta = toFileMetadataResponse(
                        meta, fullPath, resolveDataProductUri(meta, fullPath, storageResourceId));
                if (meta.isDirectory()) {
                    responseBuilder.addDirectories(fileMeta);
                } else {
                    responseBuilder.addFiles(fileMeta);
                }
            }

            observer.onNext(responseBuilder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteFile(DeleteFileRequest request, StreamObserver<Empty> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            adaptor.deleteFile(path);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void deleteDir(DeleteDirRequest request, StreamObserver<Empty> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            adaptor.deleteDirectory(path);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void moveFile(MoveFileRequest request, StreamObserver<DataProductModel> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String src = resolvePath(request.getSourcePath(), request.getStorageResourceId());
            String dst = resolvePath(request.getDestinationPath(), request.getStorageResourceId());
            adaptor.moveFile(src, dst);

            DataProductModel product = DataProductModel.newBuilder()
                    .setProductName(Paths.get(request.getDestinationPath())
                            .getFileName()
                            .toString())
                    .build();
            observer.onNext(product);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void copyFile(CopyFileRequest request, StreamObserver<DataProductModel> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String src = resolvePath(request.getSourcePath(), request.getStorageResourceId());
            String dst = resolvePath(request.getDestinationPath(), request.getStorageResourceId());
            adaptor.copyFile(src, dst);

            DataProductModel product = DataProductModel.newBuilder()
                    .setProductName(Paths.get(request.getDestinationPath())
                            .getFileName()
                            .toString())
                    .build();
            observer.onNext(product);
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void createDir(CreateDirRequest request, StreamObserver<CreateDirResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            adaptor.createDirectory(path, true);
            observer.onNext(CreateDirResponse.newBuilder()
                    .setCreatedPath(request.getPath())
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void createSymlink(CreateSymlinkRequest request, StreamObserver<Empty> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String target = resolvePath(request.getTargetPath(), request.getStorageResourceId());
            String source = resolvePath(request.getSourcePath(), request.getStorageResourceId());
            adaptor.createSymlink(target, source);
            observer.onNext(Empty.getDefaultInstance());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getFileMetadata(GetFileMetadataRequest request, StreamObserver<FileMetadataResponse> observer) {
        try {
            StorageResourceAdaptor adaptor = getStorageAdaptor(request.getStorageResourceId());
            String path = resolvePath(request.getPath(), request.getStorageResourceId());
            String storageResourceId = resolveStorageResourceId(request.getStorageResourceId());
            FileMetadata meta = adaptor.getFileMetadata(path);
            observer.onNext(toFileMetadataResponse(meta, path, resolveDataProductUri(meta, path, storageResourceId)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void listExperimentDir(ListExperimentDirRequest request, StreamObserver<ListDirResponse> observer) {
        try {
            String experimentId = request.getExperimentId();
            ExperimentModel experiment = experimentRegistry.getExperiment(experimentId);

            // Determine storage resource ID from experiment's user configuration
            UserConfigurationDataModel userConfig = experiment.getUserConfigurationData();
            String storageResourceId = "";
            if (userConfig != null && !userConfig.getOutputStorageResourceId().isEmpty()) {
                storageResourceId = userConfig.getOutputStorageResourceId();
            } else if (userConfig != null
                    && !userConfig.getInputStorageResourceId().isEmpty()) {
                storageResourceId = userConfig.getInputStorageResourceId();
            }

            if (storageResourceId.isEmpty()) {
                // Fall back to default storage resource for the gateway
                RequestContext ctx = GrpcRequestContext.current();
                List<StoragePreference> prefs =
                        gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(ctx.getGatewayId());
                if (!prefs.isEmpty()) {
                    storageResourceId = prefs.get(0).getStorageResourceId();
                }
            }

            if (storageResourceId.isEmpty()) {
                observer.onError(Status.FAILED_PRECONDITION
                        .withDescription("No storage resource configured for experiment " + experimentId)
                        .asRuntimeException());
                return;
            }

            // Determine the experiment data directory path
            String experimentDataDir = userConfig != null ? userConfig.getExperimentDataDir() : "";
            if (experimentDataDir.isEmpty()) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("No experiment data directory set for experiment " + experimentId)
                        .asRuntimeException());
                return;
            }

            // Anchor the (typically bare-relative) experiment data dir under the storage
            // resource's filesystem root, matching DataStagingTask.buildDestinationFilePath,
            // so the listing reads the same chroot-anchored location the data was staged to
            // (an unanchored relative path resolves against the non-writable SFTP chroot root).
            StorageResourceAdaptor adaptor = getStorageAdaptor(storageResourceId);
            String resolvedDataDir = resolvePath(experimentDataDir, storageResourceId);
            List<String> entries = adaptor.listDirectory(resolvedDataDir);
            ListDirResponse.Builder responseBuilder = ListDirResponse.newBuilder();

            for (String entry : entries) {
                String fullPath =
                        resolvedDataDir.endsWith("/") ? resolvedDataDir + entry : resolvedDataDir + "/" + entry;
                FileMetadata meta = adaptor.getFileMetadata(fullPath);
                FileMetadataResponse fileMeta = toFileMetadataResponse(
                        meta, fullPath, resolveDataProductUri(meta, fullPath, storageResourceId));
                if (meta.isDirectory()) {
                    responseBuilder.addDirectories(fileMeta);
                } else {
                    responseBuilder.addFiles(fileMeta);
                }
            }

            observer.onNext(responseBuilder.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    @Override
    public void getDefaultStorageResourceId(
            GetDefaultStorageResourceIdRequest request, StreamObserver<GetDefaultStorageResourceIdResponse> observer) {
        try {
            RequestContext ctx = GrpcRequestContext.current();
            List<StoragePreference> prefs =
                    gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(ctx.getGatewayId());

            if (prefs.isEmpty()) {
                observer.onError(Status.NOT_FOUND
                        .withDescription("No storage preferences configured for gateway " + ctx.getGatewayId())
                        .asRuntimeException());
                return;
            }

            // Return the first configured storage resource as the default
            String storageResourceId = prefs.get(0).getStorageResourceId();
            observer.onNext(GetDefaultStorageResourceIdResponse.newBuilder()
                    .setStorageResourceId(storageResourceId)
                    .build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(GrpcStatusMapper.toStatusException(e));
        }
    }

    private static FileMetadataResponse toFileMetadataResponse(FileMetadata meta, String path, String dataProductUri) {
        return FileMetadataResponse.newBuilder()
                .setName(meta.getName() != null ? meta.getName() : "")
                .setPath(path)
                .setSize(meta.getSize())
                .setIsDirectory(meta.isDirectory())
                .setModifiedTime(meta.getModifiedTime())
                .setContentType(meta.getContentType() != null ? meta.getContentType() : "")
                .setDataProductUri(dataProductUri != null ? dataProductUri : "")
                .build();
    }
}
