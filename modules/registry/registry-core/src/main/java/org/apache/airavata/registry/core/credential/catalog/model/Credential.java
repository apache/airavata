/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.credential.catalog.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name ="CREDENTIAL")
@IdClass(Credential_PK.class)
public class Credential {
    private String gatewayId;
    private String tokenId;
    private Byte[] credential;
    private String portalUserId;
    private Timestamp timePersisted;

    @Id
    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    @Id
    @Column(name = "TOKEN_ID")
    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    @Lob
    @Column(name = "CREDENTIAL")
    public Byte[] getCredential() {
        return credential;
    }

    public void setCredential(Byte[] credential) {
        this.credential = credential;
    }

    @Column(name = "PORTAL_USER_ID")
    public String getPortalUserId() {
        return portalUserId;
    }

    public void setPortalUserId(String portalUserId) {
        this.portalUserId = portalUserId;
    }

    @Column(name = "TIME_PERSISTED")
    public Timestamp getTimePersisted() {
        return timePersisted;
    }

    public void setTimePersisted(Timestamp timePersisted) {
        this.timePersisted = timePersisted;
    }
}