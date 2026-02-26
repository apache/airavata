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
package org.apache.airavata.compute.resource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import org.apache.airavata.compute.resource.model.ResourceCapabilities;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity representing a compute or storage resource accessible within a gateway.
 *
 * <p>A resource corresponds to a physical or virtual host that Airavata can submit
 * jobs to or transfer data from/to. The {@code capabilities} JSON field captures
 * the set of supported job submission and data movement protocols as a structured object.
 * The {@code resourceType} field classifies the resource (e.g., {@code "COMPUTE"} or
 * {@code "STORAGE"}).
 */
@Entity
@Table(name = "resource")
@EntityListeners(AuditingEntityListener.class)
public class ResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "host_name", nullable = false)
    private String hostName;

    @Column(name = "port")
    private Integer port = 22;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType = "COMPUTE";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "capabilities", nullable = false, columnDefinition = "json")
    private ResourceCapabilities capabilities;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gateway_id", insertable = false, updatable = false)
    private GatewayEntity gateway;

    public ResourceEntity() {}

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public ResourceCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(ResourceCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public GatewayEntity getGateway() {
        return gateway;
    }

    public void setGateway(GatewayEntity gateway) {
        this.gateway = gateway;
    }

    @Override
    public String toString() {
        return "ResourceEntity{"
                + "resourceId='" + resourceId + '\''
                + ", gatewayId='" + gatewayId + '\''
                + ", name='" + name + '\''
                + ", hostName='" + hostName + '\''
                + ", port=" + port
                + ", resourceType='" + resourceType + '\''
                + '}';
    }
}
