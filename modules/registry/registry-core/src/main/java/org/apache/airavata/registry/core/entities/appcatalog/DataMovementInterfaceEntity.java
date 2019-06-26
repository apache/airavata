/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.airavata.model.data.movement.DataMovementProtocol;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the data_movement_interface database table.
 */
@Entity
@Table(name = "DATA_MOVEMENT_INTERFACE")
@IdClass(DataMovementInterfacePK.class)
public class DataMovementInterfaceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "COMPUTE_RESOURCE_ID")
    @Id
    private String computeResourceId;

    @Column(name = "DATA_MOVEMENT_INTERFACE_ID")
    @Id
    private String dataMovementInterfaceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "DATA_MOVEMENT_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private DataMovementProtocol dataMovementProtocol;

    @Column(name = "PRIORITY_ORDER")
    private int priorityOrder;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @ManyToOne(targetEntity = ComputeResourceEntity.class)
    @JoinColumn(name = "COMPUTE_RESOURCE_ID", nullable = false, updatable = false)
    private ComputeResourceEntity computeResource;

    public DataMovementInterfaceEntity() {
    }

    public ComputeResourceEntity getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResourceEntity computeResource) {
        this.computeResource = computeResource;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
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

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}