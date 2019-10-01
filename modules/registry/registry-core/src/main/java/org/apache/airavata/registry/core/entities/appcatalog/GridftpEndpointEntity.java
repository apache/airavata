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

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the gridftp_endpoint database table.
 */
@Entity
@Table(name = "GRIDFTP_ENDPOINT")
@IdClass(GridftpEndpointPK.class)
public class GridftpEndpointEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "DATA_MOVEMENT_INTERFACE_ID")
    private String dataMovementInterfaceId;

    @Id
    @Column(name = "ENDPOINT")
    private String endpoint;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @ManyToOne(cascade= CascadeType.MERGE)
    @JoinColumn(name = "DATA_MOVEMENT_INTERFACE_ID")
    private GridftpDataMovementEntity gridftpDataMovement;


    public GridftpEndpointEntity() {
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId = dataMovementInterfaceId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public GridftpDataMovementEntity getGridftpDataMovement() {
        return gridftpDataMovement;
    }

    public void setGridftpDataMovement(GridftpDataMovementEntity gridftpDataMovement) {
        this.gridftpDataMovement = gridftpDataMovement;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}