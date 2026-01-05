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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: TenantPreferences
 */
public class TenantPreferences {
    private String tenantAdminFirstName;
    private String tenantAdminLastName;
    private String tenantAdminEmail;

    public TenantPreferences() {}

    public String getTenantAdminFirstName() {
        return tenantAdminFirstName;
    }

    public void setTenantAdminFirstName(String tenantAdminFirstName) {
        this.tenantAdminFirstName = tenantAdminFirstName;
    }

    public String getTenantAdminLastName() {
        return tenantAdminLastName;
    }

    public void setTenantAdminLastName(String tenantAdminLastName) {
        this.tenantAdminLastName = tenantAdminLastName;
    }

    public String getTenantAdminEmail() {
        return tenantAdminEmail;
    }

    public void setTenantAdminEmail(String tenantAdminEmail) {
        this.tenantAdminEmail = tenantAdminEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantPreferences that = (TenantPreferences) o;
        return Objects.equals(tenantAdminFirstName, that.tenantAdminFirstName)
                && Objects.equals(tenantAdminLastName, that.tenantAdminLastName)
                && Objects.equals(tenantAdminEmail, that.tenantAdminEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantAdminFirstName, tenantAdminLastName, tenantAdminEmail);
    }

    @Override
    public String toString() {
        return "TenantPreferences{" + "tenantAdminFirstName=" + tenantAdminFirstName + ", tenantAdminLastName="
                + tenantAdminLastName + ", tenantAdminEmail=" + tenantAdminEmail + "}";
    }
}
