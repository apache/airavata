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

import java.io.Serializable;

/**
 * Represents the effective (resolved/merged) storage preference for a user.
 *
 * <p>This is NOT a JPA entity - it's a plain model class used to represent
 * the final computed storage preference after resolving and merging preferences
 * from multiple levels (GATEWAY, GROUP, USER).
 *
 * <p>Resolution process:
 * <ol>
 *   <li>Start with GATEWAY-level preferences as the base</li>
 *   <li>Overlay GROUP-level preferences (non-null values override)</li>
 *   <li>Overlay USER-level preferences (non-null values override)</li>
 * </ol>
 *
 * <p>The resulting EffectiveStoragePreference contains the merged values
 * where higher-priority levels override lower-priority levels for each field.
 *
 * @see PreferenceLevel
 */
public class EffectiveStoragePreference implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storageResourceId;
    private String fileSystemRootLocation;
    private String loginUserName;
    private String resourceSpecificCredentialStoreToken;

    /**
     * The highest preference level that contributed to this effective preference.
     * Useful for debugging and understanding where preferences came from.
     */
    private PreferenceLevel resolvedFromLevel;

    public EffectiveStoragePreference() {}

    public EffectiveStoragePreference(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    /**
     * Creates a builder for constructing EffectiveStoragePreference instances.
     *
     * @param storageResourceId the storage resource ID (required)
     * @return a new builder instance
     */
    public static Builder builder(String storageResourceId) {
        return new Builder(storageResourceId);
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getFileSystemRootLocation() {
        return fileSystemRootLocation;
    }

    public void setFileSystemRootLocation(String fileSystemRootLocation) {
        this.fileSystemRootLocation = fileSystemRootLocation;
    }

    public String getLoginUserName() {
        return loginUserName;
    }

    public void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    public String getResourceSpecificCredentialStoreToken() {
        return resourceSpecificCredentialStoreToken;
    }

    public void setResourceSpecificCredentialStoreToken(String resourceSpecificCredentialStoreToken) {
        this.resourceSpecificCredentialStoreToken = resourceSpecificCredentialStoreToken;
    }

    public PreferenceLevel getResolvedFromLevel() {
        return resolvedFromLevel;
    }

    public void setResolvedFromLevel(PreferenceLevel resolvedFromLevel) {
        this.resolvedFromLevel = resolvedFromLevel;
    }

    /**
     * Merges another preference into this one, applying values from the source
     * only if they are non-null. This enables hierarchical preference resolution
     * where more specific preferences override less specific ones.
     *
     * @param source the preference to merge from
     * @param sourceLevel the level of the source preference (for tracking)
     */
    public void mergeFrom(EffectiveStoragePreference source, PreferenceLevel sourceLevel) {
        if (source == null) {
            return;
        }
        if (source.getFileSystemRootLocation() != null) {
            this.fileSystemRootLocation = source.getFileSystemRootLocation();
            this.resolvedFromLevel = sourceLevel;
        }
        if (source.getLoginUserName() != null) {
            this.loginUserName = source.getLoginUserName();
            this.resolvedFromLevel = sourceLevel;
        }
        if (source.getResourceSpecificCredentialStoreToken() != null) {
            this.resourceSpecificCredentialStoreToken = source.getResourceSpecificCredentialStoreToken();
            this.resolvedFromLevel = sourceLevel;
        }
    }

    /**
     * Checks if this preference has any non-null configuration values.
     *
     * @return true if at least one configuration value is set
     */
    public boolean hasAnyConfiguration() {
        return fileSystemRootLocation != null
                || loginUserName != null
                || resourceSpecificCredentialStoreToken != null;
    }

    @Override
    public String toString() {
        return "EffectiveStoragePreference{"
                + "storageResourceId='" + storageResourceId + '\''
                + ", fileSystemRootLocation='" + fileSystemRootLocation + '\''
                + ", loginUserName='" + loginUserName + '\''
                + ", resourceSpecificCredentialStoreToken='"
                + (resourceSpecificCredentialStoreToken != null ? "[REDACTED]" : "null") + '\''
                + ", resolvedFromLevel=" + resolvedFromLevel
                + '}';
    }

    /**
     * Builder for EffectiveStoragePreference.
     */
    public static class Builder {
        private final EffectiveStoragePreference instance;

        private Builder(String storageResourceId) {
            instance = new EffectiveStoragePreference(storageResourceId);
        }

        public Builder fileSystemRootLocation(String fileSystemRootLocation) {
            instance.setFileSystemRootLocation(fileSystemRootLocation);
            return this;
        }

        public Builder loginUserName(String loginUserName) {
            instance.setLoginUserName(loginUserName);
            return this;
        }

        public Builder resourceSpecificCredentialStoreToken(String token) {
            instance.setResourceSpecificCredentialStoreToken(token);
            return this;
        }

        public Builder resolvedFromLevel(PreferenceLevel level) {
            instance.setResolvedFromLevel(level);
            return this;
        }

        public EffectiveStoragePreference build() {
            return instance;
        }
    }
}
