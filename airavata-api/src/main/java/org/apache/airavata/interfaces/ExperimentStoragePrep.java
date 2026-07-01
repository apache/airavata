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
package org.apache.airavata.interfaces;

/**
 * Filesystem-only SPI for the storage work that experiment launch-prep needs (default storage
 * resolution, directory creation, file existence, move/copy). Implemented in {@code storage-service}
 * and setter-injected into {@code ExperimentService} via composition, so {@code research-service}
 * never imports a storage-service type and no Maven cycle is introduced.
 *
 * <p>The request scope (gateway/user) and the chroot credential/login/root anchoring are resolved
 * inside the implementation from the request-scoped {@code GrpcRequestContext.current()}; callers
 * pass only filesystem coordinates. {@code storageResourceId} may be empty, in which case the impl
 * resolves the gateway default. Paths follow the same anchoring rules as the storage facade: an
 * absolute path is honored as-is, a bare-relative path is anchored under the storage resource's
 * filesystem root.
 */
public interface ExperimentStoragePrep {

    /** The gateway's default storage resource id (first configured gateway storage preference). */
    String getDefaultStorageResourceId();

    /**
     * Idempotently create {@code relPath} (recursive) and return the resolved bare-relative directory
     * ({@code resolvePath(relPath)}), suitable for persisting as the experiment data dir.
     */
    String ensureDir(String storageResourceId, String relPath);

    boolean fileExists(String storageResourceId, String path);

    void moveFile(String storageResourceId, String src, String dst);

    void copyFile(String storageResourceId, String src, String dst);
}
