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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Entity linking a credential to an {@link AllocationProjectEntity}.
 *
 * <p>Records that a credential ({@code credentialId}) is a member of the given allocation
 * project ({@code allocationProjectId}). The {@code bindingId} references the
 * {@link ResourceBindingEntity} that provides the credential context under
 * which jobs are submitted to the project's associated resource.
 *
 * <p>The composite primary key {@code (CREDENTIAL_ID, ALLOCATION_PROJECT_ID)} is declared
 * via {@link CredentialAllocationProjectPK} and the {@code @IdClass} annotation.
 */
@Entity
@Table(name = "credential_allocation_project")
@IdClass(CredentialAllocationProjectPK.class)
public class CredentialAllocationProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "credential_id")
    private String credentialId;

    @Id
    @Column(name = "allocation_project_id")
    private String allocationProjectId;

    @Column(name = "binding_id", nullable = false)
    private String bindingId;

    public CredentialAllocationProjectEntity() {}

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
    public String toString() {
        return "CredentialAllocationProjectEntity{"
                + "credentialId='" + credentialId + '\''
                + ", allocationProjectId='" + allocationProjectId + '\''
                + ", bindingId='" + bindingId + '\''
                + '}';
    }
}
