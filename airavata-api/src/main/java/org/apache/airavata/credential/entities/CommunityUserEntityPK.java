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

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for CommunityUserEntity.
 */
public class CommunityUserEntityPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gatewayId;
    private String communityUserName;

    public CommunityUserEntityPK() {}

    public CommunityUserEntityPK(String gatewayId, String communityUserName) {
        this.gatewayId = gatewayId;
        this.communityUserName = communityUserName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityUserEntityPK that = (CommunityUserEntityPK) o;
        return Objects.equals(gatewayId, that.gatewayId) && Objects.equals(communityUserName, that.communityUserName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, communityUserName);
    }
}
