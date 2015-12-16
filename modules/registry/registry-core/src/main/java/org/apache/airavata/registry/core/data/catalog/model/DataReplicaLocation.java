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
package org.apache.airavata.registry.core.data.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "DATA_REPLICA_LOCATION")
public class DataReplicaLocation {
    private final static Logger logger = LoggerFactory.getLogger(DataReplicaLocation.class);
    private String replicaId;
    private String resourceId;
    private String replicaName;
    private String replicaDescription;
    private String storageResourceId;
    private String storageResourceHostName;
    private String storageResourceDataTransferProtocol;
    private int storageResourcePort;
    private String filePath;
    private String replicaUrl;
    private String replicaLocationCategory;
    private String replicaPersistentType;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;

    private DataResource dataResource;
    private Collection<DataReplicaMetaData> dataReplicaMetaData;

    @Id
    @Column(name = "REPLICA_ID")
    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    @Column(name = "RESOURCE_ID")
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Column(name = "REPLICA_URL")
    public String getReplicaUrl() {
        return replicaUrl;
    }

    public void setReplicaUrl(String replicaUrl) {
        this.replicaUrl = replicaUrl;
    }

    @Column(name = "REPLICA_NAME")
    public String getReplicaName() {
        return replicaName;
    }

    public void setReplicaName(String replicaName) {
        this.replicaName = replicaName;
    }

    @Column(name = "REPLICA_DESCRIPTION")
    public String getReplicaDescription() {
        return replicaDescription;
    }

    public void setReplicaDescription(String replicaDescription) {
        this.replicaDescription = replicaDescription;
    }

    @Column(name = "STORAGE_RESOURCE_ID")
    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    @Column(name = "STORAGE_RESOURCE_HOST_NAME")
    public String getStorageResourceHostName() {
        return storageResourceHostName;
    }

    public void setStorageResourceHostName(String storageResourceHostName) {
        this.storageResourceHostName = storageResourceHostName;
    }

    @Column(name = "STORAGE_RESOURCE_DATA_TRANSFER_PROTOCOL")
    public String getStorageResourceDataTransferProtocol() {
        return storageResourceDataTransferProtocol;
    }

    public void setStorageResourceDataTransferProtocol(String storageResourceDataTransferProtocol) {
        this.storageResourceDataTransferProtocol = storageResourceDataTransferProtocol;
    }

    @Column(name = "STORAGE_RESOURCE_PORT")
    public int getStorageResourcePort() {
        return storageResourcePort;
    }

    public void setStorageResourcePort(int storageResourcePort) {
        this.storageResourcePort = storageResourcePort;
    }

    @Column(name = "FILE_PATH")
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "LAST_MODIFIED_TIME")
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Column(name = "REPLICA_LOCATION_CATEGORY")
    public String getReplicaLocationCategory() {
        return replicaLocationCategory;
    }

    public void setReplicaLocationCategory(String replicaLocationCategory) {
        this.replicaLocationCategory = replicaLocationCategory;
    }

    @Column(name = "REPLICA_PERSISTENT_TYPE")
    public String getReplicaPersistentType() {
        return replicaPersistentType;
    }

    public void setReplicaPersistentType(String replicaPersistentType) {
        this.replicaPersistentType = replicaPersistentType;
    }

    @ManyToOne
    @JoinColumn(name = "RESOURCE_ID", referencedColumnName = "RESOURCE_ID")
    public DataResource getDataResource() {
        return dataResource;
    }

    public void setDataResource(DataResource dataResource) {
        this.dataResource = dataResource;
    }

    @OneToMany(mappedBy = "dataReplicaLocation", cascade = {CascadeType.ALL})
    public Collection<DataReplicaMetaData> getDataReplicaMetaData() {
        return dataReplicaMetaData;
    }

    public void setDataReplicaMetaData(Collection<DataReplicaMetaData> dataReplicaMetaData) {
        this.dataReplicaMetaData = dataReplicaMetaData;
    }
}