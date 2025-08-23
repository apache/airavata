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
import java.util.Objects;

@Entity
@Table(name = "COMMUNITY_USER")
@IdClass(CommunityUserEntity.CommunityUserPK.class)
public class CommunityUserEntity implements Serializable {

    @Id
    @Column(name = "GATEWAY_ID", length = 100, nullable = false)
    private String gatewayId;

    @Id
    @Column(name = "COMMUNITY_USER_NAME", length = 100, nullable = false)
    private String communityUserName;

    @Id
    @Column(name = "TOKEN_ID", length = 100, nullable = false)
    private String tokenId;

    @Column(name = "COMMUNITY_USER_EMAIL", length = 256, nullable = false)
    private String communityUserEmail;

    public CommunityUserEntity() {}

    public CommunityUserEntity(String gatewayId, String communityUserName, String tokenId, String communityUserEmail) {
        this.gatewayId = gatewayId;
        this.communityUserName = communityUserName;
        this.tokenId = tokenId;
        this.communityUserEmail = communityUserEmail;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityUserEntity)) return false;
        CommunityUserEntity that = (CommunityUserEntity) o;
        return Objects.equals(gatewayId, that.gatewayId) &&
               Objects.equals(communityUserName, that.communityUserName) &&
               Objects.equals(tokenId, that.tokenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, communityUserName, tokenId);
    }

    public static class CommunityUserPK implements Serializable {
        private String gatewayId;
        private String communityUserName;
        private String tokenId;

        public CommunityUserPK() {}

        public CommunityUserPK(String gatewayId, String communityUserName, String tokenId) {
            this.gatewayId = gatewayId;
            this.communityUserName = communityUserName;
            this.tokenId = tokenId;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CommunityUserPK)) return false;
            CommunityUserPK that = (CommunityUserPK) o;
            return Objects.equals(gatewayId, that.gatewayId) &&
                   Objects.equals(communityUserName, that.communityUserName) &&
                   Objects.equals(tokenId, that.tokenId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(gatewayId, communityUserName, tokenId);
        }
    }
}
