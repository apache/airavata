/**
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
package org.apache.airavata.registry.entities.expcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import org.apache.airavata.credential.entities.CredentialEntity;

/**
 * Entity binding a project (workspace) to one Slurm account per compute resource.
 * For a given project, on resource R use account ACCOUNT_NAME with credential (GATEWAY_ID, CREDENTIAL_TOKEN).
 * Account name must be one of the accounts discovered for this credential on this resource via slurminfo.
 */
@Entity
@Table(name = "PROJECT_RESOURCE_ACCOUNT")
@IdClass(ProjectResourceAccountPK.class)
public class ProjectResourceAccountEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PROJECT_ID", nullable = false, length = 255)
    private String projectId;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false, length = 255)
    private String computeResourceId;

    @Column(name = "GATEWAY_ID", nullable = false, length = 256)
    private String gatewayId;

    @Column(name = "CREDENTIAL_TOKEN", nullable = false, length = 256)
    private String credentialToken;

    @Column(name = "ACCOUNT_NAME", nullable = false, length = 512)
    private String accountName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID", insertable = false, updatable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", insertable = false, updatable = false),
        @JoinColumn(name = "CREDENTIAL_TOKEN", referencedColumnName = "TOKEN_ID", insertable = false, updatable = false)
    })
    private CredentialEntity credential;

    public ProjectResourceAccountEntity() {}

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public CredentialEntity getCredential() {
        return credential;
    }

    public void setCredential(CredentialEntity credential) {
        this.credential = credential;
    }
}
