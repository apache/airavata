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
import java.util.List;

/**
 * The persistent class for the storage_resource database table.
 */
@Entity
@Table(name = "STORAGE_RESOURCE")
public class StorageResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "DESCRIPTION")
    private String storageResourceDescription;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @OneToMany(targetEntity = StorageInterfaceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "storageResource", fetch = FetchType.EAGER)
    private List<StorageInterfaceEntity> dataMovementInterfaces;

    public StorageResourceEntity() {
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public String getStorageResourceDescription() {
        return storageResourceDescription;
    }

    public void setStorageResourceDescription(String storageResourceDescription) {
        this.storageResourceDescription = storageResourceDescription;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
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

    public List<StorageInterfaceEntity> getDataMovementInterfaces() {
        return dataMovementInterfaces;
    }

    public void setDataMovementInterfaces(List<StorageInterfaceEntity> dataMovementInterfaces) {
        this.dataMovementInterfaces = dataMovementInterfaces;
    }
}