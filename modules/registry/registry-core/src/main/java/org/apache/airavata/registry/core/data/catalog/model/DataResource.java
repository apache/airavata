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
@Table(name = "DATA_RESOURCE")
public class DataResource {
    private final static Logger logger = LoggerFactory.getLogger(DataResource.class);
    private String resourceId;
    private String gatewayId;
    private String resourceName;
    private String resourceDescription;
    private String dataResourceType;
    private String ownerName;
    private String parentResourceId;
    private int resourceSize;
    private Timestamp creationTime;
    private Timestamp lastModifiedTime;

    private DataResource parentResource;
    private Collection<DataReplicaLocation> dataReplicaLocations;
    private Collection<DataResourceMetaData> dataResourceMetaData;
    private Collection<DataResource> childDataResources;

    @Id
    @Column(name = "RESOURCE_ID")
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Column(name = "RESOURCE_NAME")
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Column(name = "RESOURCE_DESCRIPTION")
    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    @Column(name = "OWNER_NAME")
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Column(name = "PARENT_RESOURCE_ID")
    public String getParentResourceId() {
        return parentResourceId;
    }

    public void setParentResourceId(String parentResourceId) {
        this.parentResourceId = parentResourceId;
    }

    @Column(name = "RESOURCE_TYPE")
    public String getDataResourceType() {
        return dataResourceType;
    }

    public void setDataResourceType(String dataResourceType) {
        this.dataResourceType = dataResourceType;
    }

    @Column(name = "RESOURCE_SIZE")
    public int getResourceSize() {
        return resourceSize;
    }

    public void setResourceSize(int resourceSize) {
        this.resourceSize = resourceSize;
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

    @OneToMany(mappedBy = "dataResource", cascade = {CascadeType.ALL})
    public Collection<DataReplicaLocation> getDataReplicaLocations() {
        return dataReplicaLocations;
    }

    public void setDataReplicaLocations(Collection<DataReplicaLocation> dataReplicaLocations) {
        this.dataReplicaLocations = dataReplicaLocations;
    }

    @OneToMany(mappedBy = "dataResource", cascade = {CascadeType.ALL})
    public Collection<DataResourceMetaData> getDataResourceMetaData() {
        return dataResourceMetaData;
    }

    public void setDataResourceMetaData(Collection<DataResourceMetaData> dataResourceMetaData) {
        this.dataResourceMetaData = dataResourceMetaData;
    }

    @ManyToOne
    @JoinColumn(name = "PARENT_RESOURCE_ID", referencedColumnName = "RESOURCE_ID")
    public DataResource getParentResource() {
        return parentResource;
    }

    public void setParentResource(DataResource parentResource) {
        this.parentResource = parentResource;
    }

    @OneToMany(mappedBy = "parentResource", cascade = {CascadeType.ALL})
    public Collection<DataResource> getChildDataResources() {
        return childDataResources;
    }

    public void setChildDataResources(Collection<DataResource> childDataResources) {
        this.childDataResources = childDataResources;
    }
}