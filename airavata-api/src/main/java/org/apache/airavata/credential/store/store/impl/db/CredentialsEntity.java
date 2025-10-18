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
package org.apache.airavata.credential.store.store.impl.db;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.airavata.credential.store.credential.CredentialOwnerType;

@Entity
@Table(name = "CREDENTIALS")
@IdClass(CredentialsEntity.CredentialsPK.class)
public class CredentialsEntity implements Serializable {

    @Id
    @Column(name = "GATEWAY_ID", length = 256, nullable = false)
    private String gatewayId;

    @Id
    @Column(name = "TOKEN_ID", length = 256, nullable = false)
    private String tokenId;

    @Column(name = "CREDENTIAL", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] credential;

    @Column(name = "PORTAL_USER_ID", length = 256, nullable = false)
    private String portalUserId;

    @Column(name = "TIME_PERSISTED")
    private Timestamp timePersisted;

    @Column(name = "DESCRIPTION")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "CREDENTIAL_OWNER_TYPE")
    private CredentialOwnerType credentialOwnerType;

    public CredentialsEntity() {}

    public CredentialsEntity(
            String gatewayId,
            String tokenId,
            byte[] credential,
            String portalUserId,
            Timestamp timePersisted,
            String description,
            CredentialOwnerType credentialOwnerType) {
        this.gatewayId = gatewayId;
        this.tokenId = tokenId;
        this.credential = credential;
        this.portalUserId = portalUserId;
        this.timePersisted = timePersisted;
        this.description = description;
        this.credentialOwnerType = credentialOwnerType;
    }

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

    public CredentialOwnerType getCredentialOwnerType() {
        return credentialOwnerType;
    }

    public void setCredentialOwnerType(CredentialOwnerType credentialOwnerType) {
        this.credentialOwnerType = credentialOwnerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CredentialsEntity)) return false;
        CredentialsEntity that = (CredentialsEntity) o;
        return gatewayId.equals(that.gatewayId) && tokenId.equals(that.tokenId);
    }

    @Override
    public int hashCode() {
        int result = gatewayId != null ? gatewayId.hashCode() : 0;
        result = 31 * result + (tokenId != null ? tokenId.hashCode() : 0);
        return result;
    }

    public static class CredentialsPK implements Serializable {
        private String gatewayId;
        private String tokenId;

        public CredentialsPK() {}

        public CredentialsPK(String gatewayId, String tokenId) {
            this.gatewayId = gatewayId;
            this.tokenId = tokenId;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CredentialsPK)) return false;
            CredentialsPK that = (CredentialsPK) o;
            return gatewayId.equals(that.gatewayId) && tokenId.equals(that.tokenId);
        }

        @Override
        public int hashCode() {
            int result = gatewayId != null ? gatewayId.hashCode() : 0;
            result = 31 * result + (tokenId != null ? tokenId.hashCode() : 0);
            return result;
        }
    }
}
