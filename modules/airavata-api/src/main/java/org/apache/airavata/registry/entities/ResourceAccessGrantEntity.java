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
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.credential.entities.CredentialEntity;

/**
 * Unified entity linking a credential to a compute resource with access and deployment settings.
 * Replaces the separate ResourceAccess + ApplicationDeployment concepts for a simpler model.
 *
 * <p><b>Credential–resource properties:</b> When a credential is assigned to a resource, this row
 * stores properties of that <b>credential–resource pair</b>. {@code loginUsername},
 * {@code defaultQueueName}, {@code executablePath}, {@code defaultNodeCount}, etc. are
 * resource-specific: the same credential on another compute resource can have different values.
 * These properties are used when discovering what is allowed on the resource (e.g. via
 * cluster-info) and when submitting jobs.
 */
@Entity
@Table(
        name = "RESOURCE_ACCESS_GRANT",
        indexes = {
            @Index(name = "idx_rag_gateway", columnList = "GATEWAY_ID"),
            @Index(name = "idx_rag_credential", columnList = "CREDENTIAL_TOKEN"),
            @Index(name = "idx_rag_compute_resource", columnList = "COMPUTE_RESOURCE_ID"),
            @Index(name = "idx_rag_enabled", columnList = "ENABLED")
        })
public class ResourceAccessGrantEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "GATEWAY_ID", nullable = false, length = 256)
    private String gatewayId;

    @Column(name = "CREDENTIAL_TOKEN", nullable = false, length = 256)
    private String credentialToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", insertable = false, updatable = false),
        @JoinColumn(name = "CREDENTIAL_TOKEN", referencedColumnName = "TOKEN_ID", insertable = false, updatable = false)
    })
    private CredentialEntity credential;

    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false, length = 256)
    private String computeResourceId;

    @Column(name = "LOGIN_USERNAME", length = 256)
    private String loginUsername;

    @Column(name = "EXECUTABLE_PATH", length = 1024)
    private String executablePath;

    @Column(name = "DESCRIPTION", length = 1024)
    private String description;

    @Column(name = "DEFAULT_QUEUE_NAME", length = 256)
    private String defaultQueueName;

    @Column(name = "DEFAULT_NODE_COUNT")
    private int defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private int defaultCpuCount;

    @Column(name = "DEFAULT_WALLTIME")
    private int defaultWalltime;

    @Column(name = "PARALLELISM")
    @Enumerated(EnumType.STRING)
    private ApplicationParallelismType parallelism;

    @Column(name = "ENABLED", nullable = false)
    private boolean enabled = true;

    @Column(name = "CREATION_TIME", nullable = false)
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME", nullable = false)
    private Timestamp updateTime;

    public ResourceAccessGrantEntity() {}

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public void setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
    }

    public CredentialEntity getCredential() {
        return credential;
    }

    public void setCredential(CredentialEntity credential) {
        this.credential = credential;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultQueueName() {
        return defaultQueueName;
    }

    public void setDefaultQueueName(String defaultQueueName) {
        this.defaultQueueName = defaultQueueName;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCpuCount() {
        return defaultCpuCount;
    }

    public void setDefaultCpuCount(int defaultCpuCount) {
        this.defaultCpuCount = defaultCpuCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    public ApplicationParallelismType getParallelism() {
        return parallelism;
    }

    public void setParallelism(ApplicationParallelismType parallelism) {
        this.parallelism = parallelism;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
}
