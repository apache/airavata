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
package org.apache.airavata.gateway.model;

import java.util.Objects;

/**
 * Domain model: GatewayGroups
 */
public class GatewayGroups {
    private String gatewayId;
    private String adminsGroupId;
    private String readOnlyAdminsGroupId;
    private String defaultGatewayUsersGroupId;

    public GatewayGroups() {}

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
        if (o == null || getClass() != o.getClass()) return false;
        GatewayGroups that = (GatewayGroups) o;
        return Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(adminsGroupId, that.adminsGroupId)
                && Objects.equals(readOnlyAdminsGroupId, that.readOnlyAdminsGroupId)
                && Objects.equals(defaultGatewayUsersGroupId, that.defaultGatewayUsersGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatewayId, adminsGroupId, readOnlyAdminsGroupId, defaultGatewayUsersGroupId);
    }

    @Override
    public String toString() {
        return "GatewayGroups{" + "gatewayId=" + gatewayId + ", adminsGroupId=" + adminsGroupId
                + ", readOnlyAdminsGroupId=" + readOnlyAdminsGroupId + ", defaultGatewayUsersGroupId="
                + defaultGatewayUsersGroupId + "}";
    }
}
