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
package org.apache.airavata.storage.resource.model;

import java.util.Objects;

/**
 * Domain model: StorageCapability
 * Describes the file-system access capabilities of a {@link Resource}.
 * The {@code basePath} is the root directory under which per-user or per-experiment
 * working directories will be created.
 */
public class StorageCapability {
    /**
     * File-transfer protocol used to access this storage.
     * Accepted values: {@code "SFTP"}, {@code "SCP"}.
     */
    private String protocol;
    /** Absolute base path on the remote host (e.g., {@code "/scratch/airavata"}). */
    private String basePath;

    public StorageCapability() {}

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageCapability that = (StorageCapability) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(basePath, that.basePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, basePath);
    }

    @Override
    public String toString() {
        return "StorageCapability{" + "protocol=" + protocol + ", basePath=" + basePath + "}";
    }
}
