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
import org.apache.airavata.common.model.PreferenceResourceType;

/**
 * Composite primary key for the RESOURCE_PREFERENCE table.
 *
 * <p>The key uniquely identifies a single preference value by:
 * <ul>
 *   <li>resourceType - COMPUTE or STORAGE</li>
 *   <li>resourceId - the specific compute or storage resource ID</li>
 *   <li>ownerId - gateway ID, group ID, or user ID depending on level</li>
 *   <li>level - GATEWAY, GROUP, or USER</li>
 *   <li>key - the preference key (e.g., "loginUsername")</li>
 * </ul>
 */
public class ResourcePreferenceEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private PreferenceResourceType resourceType;
    private String resourceId;
    private String ownerId;
    private PreferenceLevel level;
    private String key;

    public ResourcePreferenceEntityPK() {}

    public ResourcePreferenceEntityPK(
            PreferenceResourceType resourceType,
            String resourceId,
            String ownerId,
            PreferenceLevel level,
            String key) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ownerId = ownerId;
        this.level = level;
        this.key = key;
    }

    public PreferenceResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(PreferenceResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePreferenceEntityPK that = (ResourcePreferenceEntityPK) o;
        return resourceType == that.resourceType
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(ownerId, that.ownerId)
                && level == that.level
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, resourceId, ownerId, level, key);
    }
}
