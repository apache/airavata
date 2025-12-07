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

/**
 * JPA entity for COMMUNITY_USER table.
 */
@Entity
@Table(name = "COMMUNITY_USER")
@IdClass(CommunityUserEntityPK.class)
public class CommunityUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID", length = 256, nullable = false)
    private String gatewayId;

    @Id
    @Column(name = "COMMUNITY_USER_NAME", length = 256, nullable = false)
    private String communityUserName;

    @Column(name = "TOKEN_ID", length = 256)
    private String tokenId;

    @Column(name = "COMMUNITY_USER_EMAIL", length = 256)
    private String communityUserEmail;

    public CommunityUserEntity() {}

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCommunityUserName() {
        return communityUserName;
    }

    public void setCommunityUserName(String communityUserName) {
        this.communityUserName = communityUserName;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getCommunityUserEmail() {
        return communityUserEmail;
    }

    public void setCommunityUserEmail(String communityUserEmail) {
        this.communityUserEmail = communityUserEmail;
    }
}
