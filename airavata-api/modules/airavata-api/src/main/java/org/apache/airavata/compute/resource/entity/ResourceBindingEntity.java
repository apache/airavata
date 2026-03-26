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
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Entity that binds a specific credential to a specific resource within a gateway.
 *
 * <p>A binding associates a credential with a {@link ResourceEntity} and
 * records the login username to be used when authenticating against that resource with
 * the given credential. The {@code metadata} JSON map carries any additional resource-specific
 * properties that vary per binding (e.g. scratch directory, allocation project codes).
 *
 * <p>A unique constraint on {@code (CREDENTIAL_ID, RESOURCE_ID, LOGIN_USERNAME)} ensures each
 * credential is bound to a given resource with a specific login username exactly once.
 */
@Entity
@Table(
        name = "resource_binding",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_binding_cred_resource_user",
                    columnNames = {"credential_id", "resource_id", "login_username"})
        })
@EntityListeners(AuditingEntityListener.class)
public class ResourceBindingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "binding_id")
    private String bindingId;

    @Column(name = "credential_id", nullable = false)
    private String credentialId;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "login_username", nullable = false)
    private String loginUsername;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "gateway_id", nullable = false)
    private String gatewayId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", insertable = false, updatable = false)
    private ResourceEntity resource;

    public ResourceBindingEntity() {}

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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

    public ResourceEntity getResource() {
        return resource;
    }

    public void setResource(ResourceEntity resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ResourceBindingEntity{"
                + "bindingId='" + bindingId + '\''
                + ", credentialId='" + credentialId + '\''
                + ", resourceId='" + resourceId + '\''
                + ", loginUsername='" + loginUsername + '\''
                + ", enabled=" + enabled
                + ", gatewayId='" + gatewayId + '\''
                + '}';
    }
}
