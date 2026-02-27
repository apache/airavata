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
 * Domain model: StorageVolumeInfo
 */
public class StorageVolumeInfo {
    private String totalSize;
    private String usedSize;
    private String availableSize;
    private long totalSizeBytes;
    private long usedSizeBytes;
    private long availableSizeBytes;
    private double percentageUsed;
    private String mountPoint;
    private String filesystemType;

    public StorageVolumeInfo() {}

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(String usedSize) {
        this.usedSize = usedSize;
    }

    public String getAvailableSize() {
        return availableSize;
    }

    public void setAvailableSize(String availableSize) {
        this.availableSize = availableSize;
    }

    public long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public void setTotalSizeBytes(long totalSizeBytes) {
        this.totalSizeBytes = totalSizeBytes;
    }

    public long getUsedSizeBytes() {
        return usedSizeBytes;
    }

    public void setUsedSizeBytes(long usedSizeBytes) {
        this.usedSizeBytes = usedSizeBytes;
    }

    public long getAvailableSizeBytes() {
        return availableSizeBytes;
    }

    public void setAvailableSizeBytes(long availableSizeBytes) {
        this.availableSizeBytes = availableSizeBytes;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getFilesystemType() {
        return filesystemType;
    }

    public void setFilesystemType(String filesystemType) {
        this.filesystemType = filesystemType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageVolumeInfo that = (StorageVolumeInfo) o;
        return Objects.equals(totalSize, that.totalSize)
                && Objects.equals(usedSize, that.usedSize)
                && Objects.equals(availableSize, that.availableSize)
                && Objects.equals(totalSizeBytes, that.totalSizeBytes)
                && Objects.equals(usedSizeBytes, that.usedSizeBytes)
                && Objects.equals(availableSizeBytes, that.availableSizeBytes)
                && Objects.equals(percentageUsed, that.percentageUsed)
                && Objects.equals(mountPoint, that.mountPoint)
                && Objects.equals(filesystemType, that.filesystemType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                totalSize,
                usedSize,
                availableSize,
                totalSizeBytes,
                usedSizeBytes,
                availableSizeBytes,
                percentageUsed,
                mountPoint,
                filesystemType);
    }

    @Override
    public String toString() {
        return "StorageVolumeInfo{" + "totalSize=" + totalSize + ", usedSize=" + usedSize + ", availableSize="
                + availableSize + ", totalSizeBytes=" + totalSizeBytes + ", usedSizeBytes=" + usedSizeBytes
                + ", availableSizeBytes=" + availableSizeBytes + ", percentageUsed=" + percentageUsed + ", mountPoint="
                + mountPoint + ", filesystemType=" + filesystemType + "}";
    }
}
