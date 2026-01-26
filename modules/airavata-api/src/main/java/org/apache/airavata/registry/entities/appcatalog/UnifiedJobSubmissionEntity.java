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
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.JobSubmissionProtocol;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Unified entity for all job submission types in a single table.
 *
 * <p>This entity consolidates the separate job submission tables (SSH, Local, Cloud, Unicore, Globus)
 * into a single table with protocol-specific configuration stored as JSON. This simplifies:
 * <ul>
 *   <li>Adding new protocols without schema changes</li>
 *   <li>Querying job submissions across all types</li>
 *   <li>Code maintenance with fewer entity classes</li>
 * </ul>
 *
 * <h3>Protocol-specific config examples:</h3>
 * <pre>
 * SSH: {"resourceJobManagerId": "...", "alternativeSshHostname": "...", "sshPort": 22, "monitorMode": "..."}
 * Local: {"resourceJobManagerId": "..."}
 * Cloud: {"nodeId": "...", "executableType": "...", "providerName": "...", "userAccountName": "..."}
 * </pre>
 *
 * @see JobSubmissionProtocol
 * @see SecurityProtocol
 */
@Entity
@Table(
        name = "UNIFIED_JOB_SUBMISSION",
        indexes = {
            @Index(name = "idx_uj_sub_resource", columnList = "COMPUTE_RESOURCE_ID"),
            @Index(name = "idx_uj_sub_protocol", columnList = "PROTOCOL"),
            @Index(name = "idx_uj_sub_priority", columnList = "PRIORITY_ORDER")
        })
public class UnifiedJobSubmissionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this job submission configuration.
     */
    @Id
    @Column(name = "INTERFACE_ID", nullable = false)
    private String interfaceId;

    /**
     * The compute resource this job submission applies to.
     */
    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false)
    private String computeResourceId;

    /**
     * The job submission protocol (SSH, LOCAL, CLOUD, UNICORE, GLOBUS).
     */
    @Column(name = "PROTOCOL", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobSubmissionProtocol protocol;

    /**
     * Security protocol for authentication.
     */
    @Column(name = "SECURITY_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private SecurityProtocol securityProtocol;

    /**
     * Priority order for selecting among multiple submission interfaces.
     * Lower numbers = higher priority.
     */
    @Column(name = "PRIORITY_ORDER")
    private Integer priorityOrder;

    /**
     * Protocol-specific configuration stored as JSON.
     * 
     * <p>SSH config example:
     * <pre>
     * {
     *   "resourceJobManagerId": "slurm-manager-id",
     *   "alternativeSshHostname": "login.cluster.edu",
     *   "sshPort": 22,
     *   "monitorMode": "PUSH"
     * }
     * </pre>
     * 
     * <p>Cloud config example:
     * <pre>
     * {
     *   "nodeId": "node-123",
     *   "executableType": "SHELL",
     *   "providerName": "AWS",
     *   "userAccountName": "user@cloud"
     * }
     * </pre>
     */
    @Lob
    @Column(name = "CONFIG")
    private String config;

    /**
     * Reference to the resource job manager (for SSH/Local protocols).
     * Extracted from config for easier querying.
     */
    @Column(name = "RESOURCE_JOB_MANAGER_ID")
    private String resourceJobManagerId;

    /**
     * When this configuration was created.
     */
    @Column(name = "CREATION_TIME", nullable = false)
    private Timestamp creationTime;

    /**
     * When this configuration was last updated.
     */
    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    public UnifiedJobSubmissionEntity() {}

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

    // ========== Factory Methods ==========

    /**
     * Create an SSH job submission configuration.
     */
    public static UnifiedJobSubmissionEntity forSsh(
            String interfaceId,
            String computeResourceId,
            SecurityProtocol securityProtocol,
            String resourceJobManagerId,
            String config) {
        UnifiedJobSubmissionEntity entity = new UnifiedJobSubmissionEntity();
        entity.setInterfaceId(interfaceId);
        entity.setComputeResourceId(computeResourceId);
        entity.setProtocol(JobSubmissionProtocol.SSH);
        entity.setSecurityProtocol(securityProtocol);
        entity.setResourceJobManagerId(resourceJobManagerId);
        entity.setConfig(config);
        return entity;
    }

    /**
     * Create a Local job submission configuration.
     */
    public static UnifiedJobSubmissionEntity forLocal(
            String interfaceId,
            String computeResourceId,
            String resourceJobManagerId,
            String config) {
        UnifiedJobSubmissionEntity entity = new UnifiedJobSubmissionEntity();
        entity.setInterfaceId(interfaceId);
        entity.setComputeResourceId(computeResourceId);
        entity.setProtocol(JobSubmissionProtocol.LOCAL);
        entity.setResourceJobManagerId(resourceJobManagerId);
        entity.setConfig(config);
        return entity;
    }

    /**
     * Create a Cloud job submission configuration.
     */
    public static UnifiedJobSubmissionEntity forCloud(
            String interfaceId,
            String computeResourceId,
            SecurityProtocol securityProtocol,
            String config) {
        UnifiedJobSubmissionEntity entity = new UnifiedJobSubmissionEntity();
        entity.setInterfaceId(interfaceId);
        entity.setComputeResourceId(computeResourceId);
        entity.setProtocol(JobSubmissionProtocol.CLOUD);
        entity.setSecurityProtocol(securityProtocol);
        entity.setConfig(config);
        return entity;
    }

    // ========== Getters and Setters ==========

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public JobSubmissionProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(JobSubmissionProtocol protocol) {
        this.protocol = protocol;
    }

    public SecurityProtocol getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(SecurityProtocol securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public Integer getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(Integer priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
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

    @Override
    public String toString() {
        return "UnifiedJobSubmissionEntity{"
                + "interfaceId='" + interfaceId + '\''
                + ", computeResourceId='" + computeResourceId + '\''
                + ", protocol=" + protocol
                + ", securityProtocol=" + securityProtocol
                + ", priorityOrder=" + priorityOrder
                + '}';
    }
}
