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
 * Domain model: DataMovementInterface
 */
public class DataMovementInterface {
    private String dataMovementInterfaceId;
    private DataMovementProtocol dataMovementProtocol;
    private int priorityOrder;
    private long creationTime;
    private long updateTime;
    private String storageResourceId;

    public DataMovementInterface() {}

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public DataMovementProtocol getDataMovementProtocol() {
        return dataMovementProtocol;
    }

    public void setDataMovementProtocol(DataMovementProtocol dataMovementProtocol) {
        this.dataMovementProtocol = dataMovementProtocol;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataMovementInterface that = (DataMovementInterface) o;
        return Objects.equals(dataMovementInterfaceId, that.dataMovementInterfaceId)
                && Objects.equals(dataMovementProtocol, that.dataMovementProtocol)
                && Objects.equals(priorityOrder, that.priorityOrder)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(updateTime, that.updateTime)
                && Objects.equals(storageResourceId, that.storageResourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                dataMovementInterfaceId,
                dataMovementProtocol,
                priorityOrder,
                creationTime,
                updateTime,
                storageResourceId);
    }

    @Override
    public String toString() {
        return "DataMovementInterface{" + "dataMovementInterfaceId=" + dataMovementInterfaceId
                + ", dataMovementProtocol=" + dataMovementProtocol + ", priorityOrder=" + priorityOrder
                + ", creationTime=" + creationTime + ", updateTime=" + updateTime + ", storageResourceId="
                + storageResourceId + "}";
    }
}
