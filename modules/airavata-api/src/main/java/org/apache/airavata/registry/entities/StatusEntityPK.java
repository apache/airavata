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
package org.apache.airavata.registry.entities;

import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.StatusParentType;

/**
 * Composite primary key for the unified StatusEntity.
 * Combines statusId, parentId, and parentType to uniquely identify a status record.
 */
public class StatusEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String statusId;
    private String parentId;
    private StatusParentType parentType;

    public StatusEntityPK() {}

    public StatusEntityPK(String statusId, String parentId, StatusParentType parentType) {
        this.statusId = statusId;
        this.parentId = parentId;
        this.parentType = parentType;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public StatusParentType getParentType() {
        return parentType;
    }

    public void setParentType(StatusParentType parentType) {
        this.parentType = parentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StatusEntityPK that = (StatusEntityPK) obj;
        return Objects.equals(statusId, that.statusId)
                && Objects.equals(parentId, that.parentId)
                && parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusId, parentId, parentType);
    }

    @Override
    public String toString() {
        return "StatusEntityPK{"
                + "statusId='"
                + statusId
                + '\''
                + ", parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + '}';
    }
}
