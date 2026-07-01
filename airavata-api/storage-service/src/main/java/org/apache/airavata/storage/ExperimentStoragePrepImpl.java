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
package org.apache.airavata.storage;

import java.util.List;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.interfaces.ExperimentStoragePrep;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.interfaces.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.springframework.stereotype.Service;

/**
 * Storage-side implementation of {@link ExperimentStoragePrep}. Resolves the storage adaptor and
 * paths through {@link StoragePathResolver} (so the chroot/fileSystemRootLocation anchoring matches
 * the gRPC storage facade and {@code DataStagingTask.buildDestinationFilePath}) and performs the
 * adaptor-level filesystem operations the launch-prep needs.
 *
 * <p>The request scope (gateway/user/credential) is read from {@link GrpcRequestContext#current()}
 * inside {@code StoragePathResolver}, exactly as the gRPC handlers do, so this bean carries no
 * per-request state.
 */
@Service
public class ExperimentStoragePrepImpl implements ExperimentStoragePrep {

    private final StoragePathResolver storagePathResolver;
    private final GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;

    public ExperimentStoragePrepImpl(
            StoragePathResolver storagePathResolver,
            GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider) {
        this.storagePathResolver = storagePathResolver;
        this.gatewayStoragePreferenceProvider = gatewayStoragePreferenceProvider;
    }

    @Override
    public String getDefaultStorageResourceId() {
        try {
            // Mirror UserStorageGrpcService.getDefaultStorageResourceId: the first configured
            // gateway storage preference is the default.
            RequestContext ctx = GrpcRequestContext.current();
            List<StoragePreference> prefs =
                    gatewayStoragePreferenceProvider.getAllGatewayStoragePreferences(ctx.getGatewayId());
            if (prefs == null || prefs.isEmpty()) {
                throw new IllegalStateException(
                        "No storage preferences configured for gateway " + ctx.getGatewayId());
            }
            return prefs.get(0).getStorageResourceId();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve default storage resource id: " + e.getMessage(), e);
        }
    }

    @Override
    public String ensureDir(String storageResourceId, String relPath) {
        try {
            StorageResourceAdaptor adaptor = storagePathResolver.getStorageAdaptor(storageResourceId);
            // Create the directory using the anchored absolute path, but return the bare-relative
            // relPath. experiment_data_dir is stored bare-relative: DataStagingTask anchors it under
            // the storage root at staging time, so persisting an absolute path would double-anchor.
            String resolved = storagePathResolver.resolvePath(relPath, storageResourceId);
            adaptor.createDirectory(resolved, true);
            return relPath;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure directory " + relPath + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String storageResourceId, String path) {
        try {
            StorageResourceAdaptor adaptor = storagePathResolver.getStorageAdaptor(storageResourceId);
            String resolved = storagePathResolver.resolvePath(path, storageResourceId);
            return adaptor.doesFileExist(resolved);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check existence of " + path + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void moveFile(String storageResourceId, String src, String dst) {
        try {
            StorageResourceAdaptor adaptor = storagePathResolver.getStorageAdaptor(storageResourceId);
            String resolvedSrc = storagePathResolver.resolvePath(src, storageResourceId);
            String resolvedDst = storagePathResolver.resolvePath(dst, storageResourceId);
            adaptor.moveFile(resolvedSrc, resolvedDst);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to move " + src + " to " + dst + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void copyFile(String storageResourceId, String src, String dst) {
        try {
            StorageResourceAdaptor adaptor = storagePathResolver.getStorageAdaptor(storageResourceId);
            String resolvedSrc = storagePathResolver.resolvePath(src, storageResourceId);
            String resolvedDst = storagePathResolver.resolvePath(dst, storageResourceId);
            adaptor.copyFile(resolvedSrc, resolvedDst);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy " + src + " to " + dst + ": " + e.getMessage(), e);
        }
    }
}
