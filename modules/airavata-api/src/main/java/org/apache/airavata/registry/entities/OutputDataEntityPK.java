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
import org.apache.airavata.common.model.DataObjectParentType;

/**
 * Composite primary key for the unified OutputDataEntity.
 * Combines parentId, parentType, and name to uniquely identify an output data record.
 */
public class OutputDataEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String parentId;
    private DataObjectParentType parentType;
    private String name;

    public OutputDataEntityPK() {}

    public OutputDataEntityPK(String parentId, DataObjectParentType parentType, String name) {
        this.parentId = parentId;
        this.parentType = parentType;
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public DataObjectParentType getParentType() {
        return parentType;
    }

    public void setParentType(DataObjectParentType parentType) {
        this.parentType = parentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OutputDataEntityPK that = (OutputDataEntityPK) obj;
        return Objects.equals(parentId, that.parentId)
                && parentType == that.parentType
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, parentType, name);
    }

    @Override
    public String toString() {
        return "OutputDataEntityPK{"
                + "parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + ", name='"
                + name
                + '\''
                + '}';
    }
}
