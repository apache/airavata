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
package org.apache.airavata.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import org.apache.airavata.iam.model.GranteeType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "sharing_permission",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_sharing_perm",
                    columnNames = {
                        "resource_type",
                        "resource_id",
                        "grantee_type",
                        "grantee_id",
                        "permission",
                        "domain_id"
                    })
        },
        indexes = {
            @Index(name = "idx_sp_resource", columnList = "resource_type, resource_id"),
            @Index(name = "idx_sp_grantee", columnList = "grantee_type, grantee_id"),
            @Index(name = "idx_sp_domain", columnList = "domain_id")
        })
public class SharingPermissionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "permission_id", nullable = false, length = 512)
    private String permissionId;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grantee_type", nullable = false, length = 20)
    private GranteeType granteeType;

    @Column(name = "grantee_id", nullable = false)
    private String granteeId;

    @Column(name = "permission", nullable = false)
    private String permission;

    @Column(name = "domain_id")
    private String domainId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SharingPermissionEntity() {}

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public GranteeType getGranteeType() {
        return granteeType;
    }

    public void setGranteeType(GranteeType granteeType) {
        this.granteeType = granteeType;
    }

    public String getGranteeId() {
        return granteeId;
    }

    public void setGranteeId(String granteeId) {
        this.granteeId = granteeId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
