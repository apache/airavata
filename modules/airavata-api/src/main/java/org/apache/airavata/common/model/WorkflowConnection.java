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
 * Domain model: WorkflowConnection
 */
public class WorkflowConnection {
    private String id;
    private DataBlock dataBlock;
    private ComponentType fromType;
    private String fromId;
    private String fromOutputName;
    private ComponentType toType;
    private String toId;
    private String toInputName;
    private long createdAt;
    private long updatedAt;

    public WorkflowConnection() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataBlock getDataBlock() {
        return dataBlock;
    }

    public void setDataBlock(DataBlock dataBlock) {
        this.dataBlock = dataBlock;
    }

    public ComponentType getFromType() {
        return fromType;
    }

    public void setFromType(ComponentType fromType) {
        this.fromType = fromType;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromOutputName() {
        return fromOutputName;
    }

    public void setFromOutputName(String fromOutputName) {
        this.fromOutputName = fromOutputName;
    }

    public ComponentType getToType() {
        return toType;
    }

    public void setToType(ComponentType toType) {
        this.toType = toType;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToInputName() {
        return toInputName;
    }

    public void setToInputName(String toInputName) {
        this.toInputName = toInputName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowConnection that = (WorkflowConnection) o;
        return Objects.equals(id, that.id)
                && Objects.equals(dataBlock, that.dataBlock)
                && Objects.equals(fromType, that.fromType)
                && Objects.equals(fromId, that.fromId)
                && Objects.equals(fromOutputName, that.fromOutputName)
                && Objects.equals(toType, that.toType)
                && Objects.equals(toId, that.toId)
                && Objects.equals(toInputName, that.toInputName)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, dataBlock, fromType, fromId, fromOutputName, toType, toId, toInputName, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "WorkflowConnection{" + "id=" + id + ", dataBlock=" + dataBlock + ", fromType=" + fromType + ", fromId="
                + fromId + ", fromOutputName=" + fromOutputName + ", toType=" + toType + ", toId=" + toId
                + ", toInputName=" + toInputName + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
