/**
 *
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
 */
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * The persistent class for the group_resource_profile database table.
 */
@Entity
@Table(name = "GROUP_RESOURCE_PROFILE")
public class GroupResourceProfileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "GROUP_RESOURCE_PROFILE_NAME")
    private String groupResourceProfileName;

    // TODO: change these timestamp to actual Timestamp
    @Column(name = "CREATION_TIME", updatable = false)
    private Long creationTime;

    @Column(name = "UPDATE_TIME")
    private Long updatedTime;

    @Column(name = "DEFAULT_CREDENTIAL_STORE_TOKEN")
    private String defaultCredentialStoreToken;

    @OneToMany(targetEntity = GroupComputeResourcePrefEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<GroupComputeResourcePrefEntity> computePreferences;

    @OneToMany(targetEntity = ComputeResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ComputeResourcePolicyEntity> computeResourcePolicies;

    @OneToMany(targetEntity = BatchQueueResourcePolicyEntity.class, cascade = CascadeType.ALL,
            mappedBy = "groupResourceProfile", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<BatchQueueResourcePolicyEntity> batchQueueResourcePolicies;

    public GroupResourceProfileEntity() {
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public String getGroupResourceProfileName() {
        return groupResourceProfileName;
    }

    public void setGroupResourceProfileName(String groupResourceProfileName) {
        this.groupResourceProfileName = groupResourceProfileName;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDefaultCredentialStoreToken() {
        return defaultCredentialStoreToken;
    }

    public void setDefaultCredentialStoreToken(String defaultCredentialStoreToken) {
        this.defaultCredentialStoreToken = defaultCredentialStoreToken;
    }

    public List<GroupComputeResourcePrefEntity> getComputePreferences() {
        return computePreferences;
    }

    public void setComputePreferences(List<GroupComputeResourcePrefEntity> computePreferences) {
        this.computePreferences = computePreferences;
    }

    public List<ComputeResourcePolicyEntity> getComputeResourcePolicies() {
        return computeResourcePolicies;
    }

    public void setComputeResourcePolicies(List<ComputeResourcePolicyEntity> computeResourcePolicies) {
        this.computeResourcePolicies = computeResourcePolicies;
    }

    public List<BatchQueueResourcePolicyEntity> getBatchQueueResourcePolicies() {
        return batchQueueResourcePolicies;
    }

    public void setBatchQueueResourcePolicies(List<BatchQueueResourcePolicyEntity> batchQueueResourcePolicies) {
        this.batchQueueResourcePolicies = batchQueueResourcePolicies;
    }
}
