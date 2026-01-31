/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.airavata.registry.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for USER_GROUP_SELECTION table.
 * Used for explicit conflict resolution when multiple groups have the same preference.
 */
public class UserGroupSelectionEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String domainId;
    private String resourceType;
    private String resourceId;
    private String selectionKey;

    public UserGroupSelectionEntityPK() {}

    public UserGroupSelectionEntityPK(
            String userId,
            String domainId,
            String resourceType,
            String resourceId,
            String selectionKey) {
        this.userId = userId;
        this.domainId = domainId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.selectionKey = selectionKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupSelectionEntityPK that = (UserGroupSelectionEntityPK) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(domainId, that.domainId)
                && Objects.equals(resourceType, that.resourceType)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(selectionKey, that.selectionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, domainId, resourceType, resourceId, selectionKey);
    }
}
