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
package org.apache.airavata.compute.resource.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Domain model: ResourceBinding
 * Associates a {@link Credential} with a {@link Resource}, specifying the login username
 * to use when authenticating to that resource. The {@code metadata} map can hold
 * resource-specific configuration (e.g., proxy settings, jump-host details) as free-form
 * JSON-compatible key-value pairs.
 */
public class ResourceBinding {
    private String bindingId;
    private String credentialId;
    private String resourceId;
    /** The OS-level username used to log in to the remote resource. */
    private String loginUsername;
    /**
     * Free-form JSON-compatible metadata for resource-specific configuration.
     * Values may be strings, numbers, booleans, lists, or nested maps.
     */
    private Map<String, Object> metadata;

    private boolean enabled;
    private String gatewayId;
    private Instant createdAt;
    private Instant updatedAt;

    public ResourceBinding() {}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceBinding that = (ResourceBinding) o;
        return enabled == that.enabled
                && Objects.equals(bindingId, that.bindingId)
                && Objects.equals(credentialId, that.credentialId)
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(loginUsername, that.loginUsername)
                && Objects.equals(metadata, that.metadata)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                bindingId, credentialId, resourceId, loginUsername, metadata, enabled, gatewayId, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "ResourceBinding{" + "bindingId=" + bindingId + ", credentialId=" + credentialId
                + ", resourceId=" + resourceId + ", loginUsername=" + loginUsername + ", metadata=" + metadata
                + ", enabled=" + enabled + ", gatewayId=" + gatewayId + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + "}";
    }
}
