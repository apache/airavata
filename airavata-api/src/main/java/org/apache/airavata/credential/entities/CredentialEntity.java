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

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * JPA entity for CREDENTIALS table.
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
    @Column(name = "CREDENTIAL", nullable = false)
    private byte[] credential;

    @Column(name = "PORTAL_USER_ID", length = 256, nullable = false)
    private String portalUserId;

    @Column(name = "TIME_PERSISTED", nullable = false)
    private Timestamp timePersisted;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREDENTIAL_OWNER_TYPE", length = 50, nullable = false)
    private String credentialOwnerType;

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

    public String getPortalUserId() {
        return portalUserId;
    }

    public void setPortalUserId(String portalUserId) {
        this.portalUserId = portalUserId;
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

    public String getCredentialOwnerType() {
        return credentialOwnerType;
    }

    public void setCredentialOwnerType(String credentialOwnerType) {
        this.credentialOwnerType = credentialOwnerType;
    }
}
