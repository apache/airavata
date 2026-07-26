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
package org.apache.airavata.models.appcatalog.storageresource;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo}.
 */
public record StorageVolumeInfo(
        String totalSize,
        String usedSize,
        String availableSize,
        long totalSizeByteCount,
        long usedSizeByteCount,
        long availableSizeByteCount,
        double percentageUsed,
        String mountPoint,
        String filesystemType) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String totalSize = "";
        private String usedSize = "";
        private String availableSize = "";
        private long totalSizeByteCount;
        private long usedSizeByteCount;
        private long availableSizeByteCount;
        private double percentageUsed;
        private String mountPoint = "";
        private String filesystemType = "";

        private Builder() {}

        private Builder(StorageVolumeInfo source) {
            this.totalSize = source.totalSize;
            this.usedSize = source.usedSize;
            this.availableSize = source.availableSize;
            this.totalSizeByteCount = source.totalSizeByteCount;
            this.usedSizeByteCount = source.usedSizeByteCount;
            this.availableSizeByteCount = source.availableSizeByteCount;
            this.percentageUsed = source.percentageUsed;
            this.mountPoint = source.mountPoint;
            this.filesystemType = source.filesystemType;
        }

        public Builder setTotalSize(String totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        public Builder setUsedSize(String usedSize) {
            this.usedSize = usedSize;
            return this;
        }

        public Builder setAvailableSize(String availableSize) {
            this.availableSize = availableSize;
            return this;
        }

        public Builder setTotalSizeByteCount(long totalSizeByteCount) {
            this.totalSizeByteCount = totalSizeByteCount;
            return this;
        }

        public Builder setUsedSizeByteCount(long usedSizeByteCount) {
            this.usedSizeByteCount = usedSizeByteCount;
            return this;
        }

        public Builder setAvailableSizeByteCount(long availableSizeByteCount) {
            this.availableSizeByteCount = availableSizeByteCount;
            return this;
        }

        public Builder setPercentageUsed(double percentageUsed) {
            this.percentageUsed = percentageUsed;
            return this;
        }

        public Builder setMountPoint(String mountPoint) {
            this.mountPoint = mountPoint;
            return this;
        }

        public Builder setFilesystemType(String filesystemType) {
            this.filesystemType = filesystemType;
            return this;
        }

        public StorageVolumeInfo build() {
            return new StorageVolumeInfo(
                    totalSize, usedSize, availableSize, totalSizeByteCount, usedSizeByteCount,
                    availableSizeByteCount, percentageUsed, mountPoint, filesystemType);
        }
    }
}
