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
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.common.utils.AiravataUtils;

/**
 * Base class for resource profile entities providing common credential and identity fields.
 *
 * <p>This MappedSuperclass is extended by {@link ResourceProfileEntity} which unifies
 * gateway-level and user-level resource profiles into a single table.
 *
 * <p><strong>Common Fields:</strong>
 * <ul>
 *   <li>{@code credentialStoreToken} - Token for the credential store</li>
 *   <li>{@code identityServerPwdCredToken} - Identity server password credential token</li>
 *   <li>{@code identityServerTenant} - Identity server tenant identifier</li>
 *   <li>{@code creationTime} - When the profile was created</li>
 *   <li>{@code updateTime} - When the profile was last updated</li>
 * </ul>
 *
 * @see ResourceProfileEntity
 * @see BaseComputeResourcePreferenceEntity
 */
@MappedSuperclass
public abstract class BaseResourceProfileEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Token for accessing the credential store.
     * Used for secure storage and retrieval of credentials.
     */
    @Column(name = "CS_TOKEN")
    private String credentialStoreToken;

    /**
     * Identity server password credential token.
     * Used for authentication with identity providers.
     */
    @Column(name = "IDENTITY_SERVER_PWD_CRED_TOKEN")
    private String identityServerPwdCredToken;

    /**
     * Identity server tenant identifier.
     * Identifies the tenant within a multi-tenant identity server.
     */
    @Column(name = "IDENTITY_SERVER_TENANT")
    private String identityServerTenant;

    /**
     * Timestamp when this profile was created.
     */
    @Column(name = "CREATION_TIME", nullable = false, columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp creationTime;

    /**
     * Timestamp when this profile was last updated.
     */
    @Column(
            name = "UPDATE_TIME",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updateTime;

    protected BaseResourceProfileEntity() {}

    public String getCredentialStoreToken() {
        return credentialStoreToken;
    }

    public void setCredentialStoreToken(String credentialStoreToken) {
        this.credentialStoreToken = credentialStoreToken;
    }

    public String getIdentityServerPwdCredToken() {
        return identityServerPwdCredToken;
    }

    public void setIdentityServerPwdCredToken(String identityServerPwdCredToken) {
        this.identityServerPwdCredToken = identityServerPwdCredToken;
    }

    public String getIdentityServerTenant() {
        return identityServerTenant;
    }

    public void setIdentityServerTenant(String identityServerTenant) {
        this.identityServerTenant = identityServerTenant;
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
