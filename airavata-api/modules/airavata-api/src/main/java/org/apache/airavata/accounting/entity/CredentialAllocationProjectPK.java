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
package org.apache.airavata.accounting.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link CredentialAllocationProjectEntity}.
 *
 * <p>Combines {@code credentialId} and {@code allocationProjectId} to uniquely identify
 * the membership of a credential in an allocation project. Used with {@code @IdClass} on
 * the owning entity.
 */
public class CredentialAllocationProjectPK implements Serializable {
    private static final long serialVersionUID = 1L;

    private String credentialId;
    private String allocationProjectId;

    public CredentialAllocationProjectPK() {}

    public CredentialAllocationProjectPK(String credentialId, String allocationProjectId) {
        this.credentialId = credentialId;
        this.allocationProjectId = allocationProjectId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getAllocationProjectId() {
        return allocationProjectId;
    }

    public void setAllocationProjectId(String allocationProjectId) {
        this.allocationProjectId = allocationProjectId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CredentialAllocationProjectPK that = (CredentialAllocationProjectPK) obj;
        return Objects.equals(credentialId, that.credentialId)
                && Objects.equals(allocationProjectId, that.allocationProjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialId, allocationProjectId);
    }

    @Override
    public String toString() {
        return "CredentialAllocationProjectPK{"
                + "credentialId='" + credentialId + '\''
                + ", allocationProjectId='" + allocationProjectId + '\''
                + '}';
    }
}
