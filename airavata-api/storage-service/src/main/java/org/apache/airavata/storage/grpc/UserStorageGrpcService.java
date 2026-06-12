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
import org.apache.airavata.model.experiment.proto.ExperimentModel;
import org.apache.airavata.model.experiment.proto.UserConfigurationDataModel;
import org.apache.airavata.task.AdaptorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserStorageGrpcService extends UserStorageServiceGrpc.UserStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserStorageGrpcService.class);

    private final AdaptorSupport adaptorSupport;
    private final ExperimentRegistry experimentRegistry;
    private final GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;
    private final StorageProvider storageProvider;

    public UserStorageGrpcService(
            AdaptorSupport adaptorSupport,
            ExperimentRegistry experimentRegistry,
            GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider,
            StorageProvider storageProvider) {
        this.adaptorSupport = adaptorSupport;
        this.experimentRegistry = experimentRegistry;
        this.gatewayStoragePreferenceProvider = gatewayStoragePreferenceProvider;
        this.storageProvider = storageProvider;
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

    private StoragePreference resolveStoragePreference(String storageResourceId) throws Exception {
        RequestContext ctx = GrpcRequestContext.current();
        var prefs = gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(ctx.getGatewayId());
        if (prefs == null || prefs.isEmpty()) {
            return null;
        }
        String resolvedId = (storageResourceId != null && !storageResourceId.isEmpty())
                ? storageResourceId
                : prefs.get(0).getStorageResourceId();
        for (var pref : prefs) {
            if (pref.getStorageResourceId().equals(resolvedId)) {
                return pref;
            }
        }
        return prefs.get(0);
    }

    /** Resolve the effective storage resource id: the request's, else the gateway default. */
    private String resolveStorageResourceId(String storageResourceId) throws Exception {
        if (storageResourceId != null && !storageResourceId.isEmpty()) {
            return storageResourceId;
        }
        StoragePreference pref = resolveStoragePreference(storageResourceId);
        return pref != null ? pref.getStorageResourceId() : "";
    }

    private StorageResourceAdaptor getStorageAdaptor(String storageResourceId) throws Exception {
        RequestContext ctx = GrpcRequestContext.current();
        String resolvedId = storageResourceId;
        String credentialToken = ctx.getAccessToken(); // fallback
        String loginUser = ctx.getUserId(); // fallback

        // Resolve storage resource, credential, and login user from gateway preferences
        StoragePreference pref = resolveStoragePreference(storageResourceId);
        if (pref != null) {
            if (resolvedId == null || resolvedId.isEmpty()) {
                resolvedId = pref.getStorageResourceId();
            }
            String csToken = pref.getResourceSpecificCredentialStoreToken();
            if (csToken != null && !csToken.isEmpty()) {
                credentialToken = csToken;
            }
            String prefUser = pref.getLoginUserName();
            if (prefUser != null && !prefUser.isEmpty()) {
                loginUser = prefUser;
            }
        }
        if (resolvedId == null || resolvedId.isEmpty()) {
            throw new IllegalStateException("No storage resource configured for gateway " + ctx.getGatewayId());
        }
        return adaptorSupport.fetchStorageAdaptor(ctx.getGatewayId(), resolvedId, credentialToken, loginUser);
    }

    /**
     * Resolve paths like "~/" or "~" to the storage preference's fileSystemRootLocation.
     * SFTP doesn't support shell tilde expansion.
     */
    private String resolvePath(String path, String storageResourceId) throws Exception {
        if (path == null || path.isEmpty()) {
            path = "~";
        }
        // Absolute paths are honored as-is. The home shortcut (~) and bare relative
        // paths both resolve against the storage resource's filesystem root: the SFTP
        // session is chrooted and its starting directory (the chroot root) is not
        // writable, so a bare "project/experiment" must be anchored under the root
        // (e.g. /storage) rather than left to resolve against the chroot root.
        if (path.startsWith("/")) {
            return path;
        }
        StoragePreference pref = resolveStoragePreference(storageResourceId);
        String root = (pref != null && !pref.getFileSystemRootLocation().isEmpty())
                ? pref.getFileSystemRootLocation()
                : "/";
        if (!root.endsWith("/")) root += "/";
        String suffix;
        if (path.startsWith("~/")) {
            suffix = path.substring(2);
        } else if (path.equals("~")) {
            suffix = "";
        } else {
            suffix = path;
        }
        return root + suffix;
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
