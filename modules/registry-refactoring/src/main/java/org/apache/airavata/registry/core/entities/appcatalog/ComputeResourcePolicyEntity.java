/*
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
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * The persistent class for the compute_resource_policy database table.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_POLICY")
public class ComputeResourcePolicyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    // TODO: Store COMPUTE_RESOURCE_ID and QUEUE_NAME in table so it can FK to BATCH_QUEUE
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="COMPUTE_RESOURCE_POLICY_QUEUES", joinColumns = {
            @JoinColumn(name = "RESOURCE_POLICY_ID")})
    @Column(name = "QUEUE_NAME")
    private List<String> allowedBatchQueues;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", nullable = false, updatable = false)
    @ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
    private GroupResourceProfileEntity groupResourceProfile;

    public ComputeResourcePolicyEntity() {
    }

    public String getResourcePolicyId() {
        return resourcePolicyId;
    }

    public void setResourcePolicyId(String resourcePolicyId) {
        this.resourcePolicyId = resourcePolicyId;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGroupResourceProfileId() {
        return groupResourceProfileId;
    }

    public void setGroupResourceProfileId(String groupResourceProfileId) {
        this.groupResourceProfileId = groupResourceProfileId;
    }

    public List<String> getAllowedBatchQueues() {
        return allowedBatchQueues;
    }

    public void setAllowedBatchQueues(List<String> allowedBatchQueues) {
        this.allowedBatchQueues = allowedBatchQueues;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}
