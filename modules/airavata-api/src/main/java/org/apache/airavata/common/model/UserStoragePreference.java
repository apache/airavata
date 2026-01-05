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

import java.util.Objects;

/**
 * Domain model: UserStoragePreference
 */
public class UserStoragePreference {
    private String storageResourceId;
    private String loginUserName;
    private String fileSystemRootLocation;
    private String resourceSpecificCredentialStoreToken;

    public UserStoragePreference() {}

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getFileSystemRootLocation() {
        return fileSystemRootLocation;
    }

    public void setFileSystemRootLocation(String fileSystemRootLocation) {
        this.fileSystemRootLocation = fileSystemRootLocation;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStoragePreference that = (UserStoragePreference) o;
        return Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(loginUserName, that.loginUserName)
                && Objects.equals(fileSystemRootLocation, that.fileSystemRootLocation)
                && Objects.equals(resourceSpecificCredentialStoreToken, that.resourceSpecificCredentialStoreToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                storageResourceId, loginUserName, fileSystemRootLocation, resourceSpecificCredentialStoreToken);
    }

    @Override
    public String toString() {
        return "UserStoragePreference{" + "storageResourceId=" + storageResourceId + ", loginUserName=" + loginUserName
                + ", fileSystemRootLocation=" + fileSystemRootLocation + ", resourceSpecificCredentialStoreToken="
                + resourceSpecificCredentialStoreToken + "}";
    }
}
