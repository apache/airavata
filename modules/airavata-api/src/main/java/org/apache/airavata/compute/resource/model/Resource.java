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
import java.util.Objects;

/**
 * Domain model: Resource
 * Represents a computational or storage resource (e.g., an HPC cluster, cloud VM, or file server)
 * registered within a gateway. A resource may expose compute capabilities, storage capabilities,
 * or both, as described by its {@link ResourceCapabilities}.
 */
public class Resource {
    private String resourceId;
    private String gatewayId;
    private String name;
    private String hostName;
    private int port = 22;
    private String description;
    private String resourceType = "COMPUTE";
    private ResourceCapabilities capabilities;
    private Instant createdAt;
    private Instant updatedAt;

    public Resource() {}

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource that = (Resource) o;
        return port == that.port
                && Objects.equals(resourceId, that.resourceId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(name, that.name)
                && Objects.equals(hostName, that.hostName)
                && Objects.equals(description, that.description)
                && Objects.equals(resourceType, that.resourceType)
                && Objects.equals(capabilities, that.capabilities)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceId, gatewayId, name, hostName, port, description, resourceType, capabilities, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Resource{" + "resourceId=" + resourceId + ", gatewayId=" + gatewayId + ", name=" + name
                + ", hostName=" + hostName + ", port=" + port + ", description=" + description
                + ", resourceType=" + resourceType + ", capabilities=" + capabilities
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
