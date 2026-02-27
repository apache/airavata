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
package org.apache.airavata.compute.resource.model;

import java.util.Objects;
import org.apache.airavata.storage.resource.model.StorageCapability;

/**
 * Domain model: ResourceCapabilities
 * Describes what a {@link Resource} is capable of. Both fields are nullable: a resource
 * may expose only compute, only storage, or both.
 */
public class ResourceCapabilities {
    /** Compute capability - present when the resource can execute jobs. May be null. */
    private ComputeCapability compute;
    /** Storage capability - present when the resource exposes a file system. May be null. */
    private StorageCapability storage;

    public ResourceCapabilities() {}

    public ComputeCapability getCompute() {
        return compute;
    }

    public void setCompute(ComputeCapability compute) {
        this.compute = compute;
    }

    public StorageCapability getStorage() {
        return storage;
    }

    public void setStorage(StorageCapability storage) {
        this.storage = storage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceCapabilities that = (ResourceCapabilities) o;
        return Objects.equals(compute, that.compute) && Objects.equals(storage, that.storage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compute, storage);
    }

    @Override
    public String toString() {
        return "ResourceCapabilities{" + "compute=" + compute + ", storage=" + storage + "}";
    }
}
