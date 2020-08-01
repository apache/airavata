/*
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

package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "GATEWAY_GROUPS")
public class GatewayGroupsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "ADMINS_GROUP_ID")
    private String adminsGroupId;

    @Column(name = "READ_ONLY_ADMINS_GROUP_ID")
    private String readOnlyAdminsGroupId;

    @Column(name = "DEFAULT_GATEWAY_USERS_GROUP_ID")
    private String defaultGatewayUsersGroupId;

    public GatewayGroupsEntity() {

    }

    public GatewayGroupsEntity(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getAdminsGroupId() {
        return adminsGroupId;
    }

    public void setAdminsGroupId(String adminsGroupId) {
        this.adminsGroupId = adminsGroupId;
    }

    public String getReadOnlyAdminsGroupId() {
        return readOnlyAdminsGroupId;
    }

    public void setReadOnlyAdminsGroupId(String readOnlyAdminsGroupId) {
        this.readOnlyAdminsGroupId = readOnlyAdminsGroupId;
    }

    public String getDefaultGatewayUsersGroupId() {
        return defaultGatewayUsersGroupId;
    }

    public void setDefaultGatewayUsersGroupId(String defaultGatewayUsersGroupId) {
        this.defaultGatewayUsersGroupId = defaultGatewayUsersGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GatewayGroupsEntity)) return false;

        GatewayGroupsEntity that = (GatewayGroupsEntity) o;

        return gatewayId.equals(that.gatewayId);
    }

    @Override
    public int hashCode() {
        return gatewayId.hashCode();
    }
}
