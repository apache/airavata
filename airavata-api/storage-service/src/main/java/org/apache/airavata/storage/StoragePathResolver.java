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

import org.apache.airavata.config.RequestContext;
import org.apache.airavata.grpc.GrpcRequestContext;
import org.apache.airavata.interfaces.GatewayStoragePreferenceProvider;
import org.apache.airavata.interfaces.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference;
import org.apache.airavata.task.AdaptorSupport;
import org.springframework.stereotype.Component;

/**
 * Resolves storage preferences, the effective storage resource id, the storage adaptor
 * (with chroot credential/login overrides), and filesystem paths for storage operations.
 *
 * <p>Extracted verbatim from {@code UserStorageGrpcService} so the same path/credential/adaptor
 * resolution can be shared by both the gRPC handler and the {@code ExperimentStoragePrep} SPI
 * impl. The request scope (gateway/user/credential) is read from {@link GrpcRequestContext#current()}
 * internally, exactly as the gRPC handler did.
 */
@Component
public class StoragePathResolver {

    private final AdaptorSupport adaptorSupport;
    private final GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider;

    public StoragePathResolver(
            AdaptorSupport adaptorSupport, GatewayStoragePreferenceProvider gatewayStoragePreferenceProvider) {
        this.adaptorSupport = adaptorSupport;
        this.gatewayStoragePreferenceProvider = gatewayStoragePreferenceProvider;
    }

    public StoragePreference resolveStoragePreference(String storageResourceId) throws Exception {
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
    public String resolveStorageResourceId(String storageResourceId) throws Exception {
        if (storageResourceId != null && !storageResourceId.isEmpty()) {
            return storageResourceId;
        }
        StoragePreference pref = resolveStoragePreference(storageResourceId);
        return pref != null ? pref.getStorageResourceId() : "";
    }

    public StorageResourceAdaptor getStorageAdaptor(String storageResourceId) throws Exception {
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
    public String resolvePath(String path, String storageResourceId) throws Exception {
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
}
