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
 * Domain model: TenantProfile
 */
public class TenantProfile {
    private String tenantId;
    private String domainName;
    private String contactEmail;

    public TenantProfile() {}

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantProfile that = (TenantProfile) o;
        return Objects.equals(tenantId, that.tenantId)
                && Objects.equals(domainName, that.domainName)
                && Objects.equals(contactEmail, that.contactEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, domainName, contactEmail);
    }

    @Override
    public String toString() {
        return "TenantProfile{" + "tenantId='"
                + tenantId + '\'' + ", domainName='"
                + domainName + '\'' + ", contactEmail='"
                + contactEmail + '\'' + '}';
    }
}
