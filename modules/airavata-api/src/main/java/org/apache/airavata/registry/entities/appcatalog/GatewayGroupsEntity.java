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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Entity for storing gateway-level group associations.
 *
 * <p>This entity stores references to the default groups for a gateway:
 * <ul>
 *   <li>adminsGroupId - Gateway administrators group</li>
 *   <li>readOnlyAdminsGroupId - Read-only administrators group</li>
 *   <li>defaultGatewayUsersGroupId - Default users group</li>
 * </ul>
 *
 * <p>Note: This entity is a simple mapping table that links gateway IDs to group IDs
 * from the sharing registry. Consider migrating this data to the
 * {@link org.apache.airavata.registry.entities.GatewayEntity} entity's fields
 * or the sharing registry's metadata system in a future consolidation.
 *
 * @see org.apache.airavata.sharing.entities.UserGroupEntity
 */
@Entity
@Table(name = "GATEWAY_GROUPS")
public class GatewayGroupsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GATEWAY_ID", nullable = false)
    private String gatewayId;

    @Column(name = "ADMINS_GROUP_ID")
    private String adminsGroupId;

    @Column(name = "READ_ONLY_ADMINS_GROUP_ID")
    private String readOnlyAdminsGroupId;

    @Column(name = "DEFAULT_GATEWAY_USERS_GROUP_ID")
    private String defaultGatewayUsersGroupId;

    public GatewayGroupsEntity() {}

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
