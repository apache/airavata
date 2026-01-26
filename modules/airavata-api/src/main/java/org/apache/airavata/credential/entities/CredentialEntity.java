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
package org.apache.airavata.credential.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * JPA entity for CREDENTIALS table.
 *
 * <p>Stores SSH keys and other credentials used for accessing compute and storage resources.
 * The {@code userId} field tracks who owns/created this credential.
 */
@Entity
@Table(name = "CREDENTIALS")
@IdClass(CredentialEntityPK.class)
public class CredentialEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID", length = 256, nullable = false)
    private String gatewayId;

    @Id
    @Column(name = "TOKEN_ID", length = 256, nullable = false)
    private String tokenId;

    @Lob
    @Column(name = "CREDENTIAL", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] credential;

    /**
     * The user ID of the credential owner.
     * This is the Airavata user ID (not the internal user ID).
     */
    @Column(name = "USER_ID", length = 256, nullable = false)
    private String userId;

    @Column(
            name = "TIME_PERSISTED",
            nullable = false,
            columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Timestamp timePersisted;

    @Column(name = "DESCRIPTION")
    private String description;

    public CredentialEntity() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public byte[] getCredential() {
        return credential;
    }

    public void setCredential(byte[] credential) {
        this.credential = credential;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimePersisted() {
        return timePersisted;
    }

    public void setTimePersisted(Timestamp timePersisted) {
        this.timePersisted = timePersisted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
