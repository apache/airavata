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
import org.apache.airavata.common.model.MetadataParentType;

/**
 * Composite primary key for the unified MetadataEntity.
 * Combines parentType, parentId, and key to uniquely identify a metadata record.
 */
public class MetadataEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private MetadataParentType parentType;
    private String parentId;
    private String key;

    public MetadataEntityPK() {}

    public MetadataEntityPK(MetadataParentType parentType, String parentId, String key) {
        this.parentType = parentType;
        this.parentId = parentId;
        this.key = key;
    }

    public MetadataParentType getParentType() {
        return parentType;
    }

    public void setParentType(MetadataParentType parentType) {
        this.parentType = parentType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MetadataEntityPK that = (MetadataEntityPK) obj;
        return parentType == that.parentType
                && Objects.equals(parentId, that.parentId)
                && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentType, parentId, key);
    }

    @Override
    public String toString() {
        return "MetadataEntityPK{"
                + "parentType="
                + parentType
                + ", parentId='"
                + parentId
                + '\''
                + ", key='"
                + key
                + '\''
                + '}';
    }
}
