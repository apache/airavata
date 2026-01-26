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
package org.apache.airavata.registry.entities.appcatalog;

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.PreferenceLevel;

/**
 * Composite primary key class for the UNIFIED_STORAGE_PREFERENCE table.
 *
 * <p>The key consists of:
 * <ul>
 *   <li>storageResourceId - the storage resource this preference applies to</li>
 *   <li>ownerId - identifies the owner based on level:
 *       <ul>
 *         <li>GATEWAY: gatewayId</li>
 *         <li>USER: userId@gatewayId (airavataInternalUserId format)</li>
 *       </ul>
 *   </li>
 *   <li>level - the preference level (GATEWAY or USER)</li>
 * </ul>
 */
public class UnifiedStoragePreferenceEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String storageResourceId;
    private String ownerId;
    private PreferenceLevel level;

    public UnifiedStoragePreferenceEntityPK() {}

    public UnifiedStoragePreferenceEntityPK(String storageResourceId, String ownerId, PreferenceLevel level) {
        this.storageResourceId = storageResourceId;
        this.ownerId = ownerId;
        this.level = level;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public PreferenceLevel getLevel() {
        return level;
    }

    public void setLevel(PreferenceLevel level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnifiedStoragePreferenceEntityPK that = (UnifiedStoragePreferenceEntityPK) o;
        return Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(ownerId, that.ownerId)
                && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageResourceId, ownerId, level);
    }

    @Override
    public String toString() {
        return "UnifiedStoragePreferenceEntityPK{"
                + "storageResourceId='" + storageResourceId + '\''
                + ", ownerId='" + ownerId + '\''
                + ", level=" + level
                + '}';
    }
}
