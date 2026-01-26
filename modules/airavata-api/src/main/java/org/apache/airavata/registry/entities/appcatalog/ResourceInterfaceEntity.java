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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.InterfaceType;
import org.apache.airavata.common.model.ResourceType;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified entity for resource interfaces (job submission, data movement, storage).
 *
 * <p>This entity consolidates the following legacy entities:
 * <ul>
 *   <li>JobSubmissionInterfaceEntity - job submission interfaces for compute resources</li>
 *   <li>DataMovementInterfaceEntity - data movement interfaces for compute resources</li>
 *   <li>StorageInterfaceEntity - data movement interfaces for storage resources</li>
 * </ul>
 *
 * <p>The interface type and resource type discriminators allow a single table to store
 * all interface types while maintaining type-specific queries.
 *
 * <p>Protocol is stored as a string to accommodate different protocol enums:
 * <ul>
 *   <li>JOB_SUBMISSION: Values from JobSubmissionProtocol (LOCAL, SSH, GLOBUS, etc.)</li>
 *   <li>DATA_MOVEMENT/STORAGE: Values from DataMovementProtocol (LOCAL, SCP, SFTP, etc.)</li>
 * </ul>
 */
@Entity(name = "ResourceInterfaceEntity")
@Table(
        name = "RESOURCE_INTERFACE",
        indexes = {
            @Index(name = "idx_res_iface_resource", columnList = "RESOURCE_ID"),
            @Index(name = "idx_res_iface_type", columnList = "INTERFACE_TYPE"),
            @Index(name = "idx_res_iface_resource_type", columnList = "RESOURCE_TYPE"),
            @Index(name = "idx_res_iface_resource_itype", columnList = "RESOURCE_ID, INTERFACE_TYPE")
        })
@IdClass(ResourceInterfaceEntityPK.class)
public class ResourceInterfaceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID", nullable = false)
    private String resourceId;

    @Id
    @Column(name = "INTERFACE_ID", nullable = false)
    private String interfaceId;

    @Id
    @Column(name = "INTERFACE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private InterfaceType interfaceType;

    @Column(name = "RESOURCE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "PROTOCOL", nullable = false)
    private String protocol;

    @Column(name = "PRIORITY_ORDER")
    private int priorityOrder;

    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    @Column(
            name = "UPDATE_TIME",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    public ResourceInterfaceEntity() {}

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
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

    @PrePersist
    void setTimestamps() {
        Timestamp now = AiravataUtils.getCurrentTimestamp();
        if (this.creationTime == null) {
            this.creationTime = now;
        }
        this.updateTime = now;
    }

    @PreUpdate
    void updateTimestamp() {
        this.updateTime = AiravataUtils.getCurrentTimestamp();
    }
}
