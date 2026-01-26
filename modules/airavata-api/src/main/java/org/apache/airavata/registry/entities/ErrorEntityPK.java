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
import org.apache.airavata.common.model.ErrorParentType;

/**
 * Composite primary key for the unified ErrorEntity.
 * Combines errorId, parentId, and parentType to uniquely identify an error record.
 */
public class ErrorEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String errorId;
    private String parentId;
    private ErrorParentType parentType;

    public ErrorEntityPK() {}

    public ErrorEntityPK(String errorId, String parentId, ErrorParentType parentType) {
        this.errorId = errorId;
        this.parentId = parentId;
        this.parentType = parentType;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ErrorParentType getParentType() {
        return parentType;
    }

    public void setParentType(ErrorParentType parentType) {
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
        ErrorEntityPK that = (ErrorEntityPK) obj;
        return Objects.equals(errorId, that.errorId)
                && Objects.equals(parentId, that.parentId)
                && parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorId, parentId, parentType);
    }

    @Override
    public String toString() {
        return "ErrorEntityPK{"
                + "errorId='"
                + errorId
                + '\''
                + ", parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + '}';
    }
}
