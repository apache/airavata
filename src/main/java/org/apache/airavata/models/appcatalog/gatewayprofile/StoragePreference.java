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
package org.apache.airavata.models.appcatalog.gatewayprofile;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.appcatalog.gatewayprofile.proto.StoragePreference}.
 */
public record StoragePreference(
        String storageResourceId, String loginUserName, String fileSystemRootLocation,
        String resourceSpecificCredentialStoreToken) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String storageResourceId = "";
        private String loginUserName = "";
        private String fileSystemRootLocation = "";
        private String resourceSpecificCredentialStoreToken = "";

        private Builder() {}

        private Builder(StoragePreference source) {
            this.storageResourceId = source.storageResourceId;
            this.loginUserName = source.loginUserName;
            this.fileSystemRootLocation = source.fileSystemRootLocation;
            this.resourceSpecificCredentialStoreToken = source.resourceSpecificCredentialStoreToken;
        }

        public Builder setStorageResourceId(String storageResourceId) {
            this.storageResourceId = storageResourceId;
            return this;
        }

        public Builder setLoginUserName(String loginUserName) {
            this.loginUserName = loginUserName;
            return this;
        }

        public Builder setFileSystemRootLocation(String fileSystemRootLocation) {
            this.fileSystemRootLocation = fileSystemRootLocation;
            return this;
        }

        public Builder setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
            this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
            return this;
        }

        public StoragePreference build() {
            return new StoragePreference(
                    storageResourceId, loginUserName, fileSystemRootLocation, resourceSpecificCredentialStoreToken);
        }
    }
}
