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
package org.apache.airavata.accounting.model;

import java.util.Objects;

/**
 * Domain model: CredentialAllocationProject
 * Join record that grants a credential access to an {@link AllocationProject}.
 * The {@code bindingId} links to the {@link ResourceBinding} that defines
 * which credential is used when submitting jobs against this allocation.
 */
public class CredentialAllocationProject {
    /** Credential ID being granted access to the allocation project. */
    private String credentialId;

    private String allocationProjectId;
    /**
     * ID of the {@link ResourceBinding} that provides authentication context
     * (credential + resource + login username) for this credential's allocation access.
     */
    private String bindingId;

    public CredentialAllocationProject() {}

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

    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(String bindingId) {
        this.bindingId = bindingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialAllocationProject that = (CredentialAllocationProject) o;
        return Objects.equals(credentialId, that.credentialId)
                && Objects.equals(allocationProjectId, that.allocationProjectId)
                && Objects.equals(bindingId, that.bindingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credentialId, allocationProjectId, bindingId);
    }

    @Override
    public String toString() {
        return "CredentialAllocationProject{" + "credentialId=" + credentialId + ", allocationProjectId=" + allocationProjectId
                + ", bindingId=" + bindingId + "}";
    }
}
