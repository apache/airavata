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
 * {@code org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription}.
 */
public record StorageResourceDescription(
        String storageResourceId,
        String hostName,
        String storageResourceDescription,
        boolean enabled,
        long creationTime,
        long updateTime,
        int sftpPort) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String storageResourceId = "";
        private String hostName = "";
        private String storageResourceDescription = "";
        private boolean enabled;
        private long creationTime;
        private long updateTime;
        private int sftpPort;

        private Builder() {}

        private Builder(StorageResourceDescription source) {
            this.storageResourceId = source.storageResourceId;
            this.hostName = source.hostName;
            this.storageResourceDescription = source.storageResourceDescription;
            this.enabled = source.enabled;
            this.creationTime = source.creationTime;
            this.updateTime = source.updateTime;
            this.sftpPort = source.sftpPort;
        }

        public Builder setStorageResourceId(String storageResourceId) {
            this.storageResourceId = storageResourceId;
            return this;
        }

        public Builder setHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder setStorageResourceDescription(String storageResourceDescription) {
            this.storageResourceDescription = storageResourceDescription;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder setSftpPort(int sftpPort) {
            this.sftpPort = sftpPort;
            return this;
        }

        public StorageResourceDescription build() {
            return new StorageResourceDescription(
                    storageResourceId, hostName, storageResourceDescription, enabled, creationTime, updateTime,
                    sftpPort);
        }
    }
}
