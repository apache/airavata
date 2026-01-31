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
package org.apache.airavata.registry.entities.appcatalog;

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
import java.sql.Timestamp;
import org.apache.airavata.credential.entities.CredentialEntity;

/**
 * JPA entity for CREDENTIAL_CLUSTER_INFO table.
 * Caches SLURM cluster information (partitions, accounts) fetched on demand per credential and compute resource.
 * References CREDENTIALS via composite (GATEWAY_ID, CREDENTIAL_TOKEN).
 *
 * <p><strong>Partition and project are always (Slurm cluster, credential)-specific.</strong>
 * Partitions and accounts in this cache are those visible to the given credential on the given
 * compute resource. Different credentials on the same cluster may see different accounts/partitions.
 * Use this entity (or {@link org.apache.airavata.service.cluster.ClusterInfoService}) to discover
 * projects/partitions available for a credential before creating or updating group resource profiles.
 */
@Entity
@Table(name = "CREDENTIAL_CLUSTER_INFO")
@IdClass(CredentialClusterInfoPK.class)
public class CredentialClusterInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID", nullable = false, length = 256)
    private String gatewayId;

    @Id
    @Column(name = "CREDENTIAL_TOKEN", nullable = false, length = 256)
    private String credentialToken;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false, length = 255)
    private String computeResourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "GATEWAY_ID", referencedColumnName = "GATEWAY_ID", insertable = false, updatable = false),
        @JoinColumn(name = "CREDENTIAL_TOKEN", referencedColumnName = "TOKEN_ID", insertable = false, updatable = false)
    })
    private CredentialEntity credential;

    @Column(name = "FETCHED_AT", nullable = false)
    private Timestamp fetchedAt;

    @Column(name = "RAW_OUTPUT", columnDefinition = "TEXT")
    private String rawOutput;

    @Column(name = "PARTITIONS_JSON", columnDefinition = "JSON")
    private String partitionsJson;

    @Column(name = "ACCOUNTS", columnDefinition = "JSON")
    private String accountsJson;

    public CredentialClusterInfoEntity() {}

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

    public Timestamp getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Timestamp fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    public String getPartitionsJson() {
        return partitionsJson;
    }

    public void setPartitionsJson(String partitionsJson) {
        this.partitionsJson = partitionsJson;
    }

    public String getAccountsJson() {
        return accountsJson;
    }

    public void setAccountsJson(String accountsJson) {
        this.accountsJson = accountsJson;
    }
}
