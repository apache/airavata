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
package org.apache.airavata.common.model;

import java.util.List;
import java.util.Objects;

/**
 * Domain model: StorageResourceDescription
 */
public class StorageResourceDescription {
    private String storageResourceId;
    private String hostName;
    private String storageResourceDescription;
    private boolean enabled;
    private List<DataMovementInterface> dataMovementInterfaces;
    private long creationTime;
    private long updateTime;

    public StorageResourceDescription() {}

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getStorageResourceDescription() {
        return storageResourceDescription;
    }

    public void setStorageResourceDescription(String storageResourceDescription) {
        this.storageResourceDescription = storageResourceDescription;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<DataMovementInterface> getDataMovementInterfaces() {
        return dataMovementInterfaces;
    }

    public void setDataMovementInterfaces(List<DataMovementInterface> dataMovementInterfaces) {
        this.dataMovementInterfaces = dataMovementInterfaces;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageResourceDescription that = (StorageResourceDescription) o;
        return Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(hostName, that.hostName)
                && Objects.equals(storageResourceDescription, that.storageResourceDescription)
                && Objects.equals(enabled, that.enabled)
                && Objects.equals(dataMovementInterfaces, that.dataMovementInterfaces)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                storageResourceId,
                hostName,
                storageResourceDescription,
                enabled,
                dataMovementInterfaces,
                creationTime,
                updateTime);
    }

    @Override
    public String toString() {
        return "StorageResourceDescription{" + "storageResourceId=" + storageResourceId + ", hostName=" + hostName
                + ", storageResourceDescription=" + storageResourceDescription + ", enabled=" + enabled
                + ", dataMovementInterfaces=" + dataMovementInterfaces + ", creationTime=" + creationTime
                + ", updateTime=" + updateTime + "}";
    }
}
