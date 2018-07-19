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

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the batch_queue_resource_policy database table.
 */
@Entity
@Table(name = "BATCH_QUEUE_RESOURCE_POLICY")
@IdClass(BatchQueueResourcePolicyPK.class)
public class BatchQueueResourcePolicyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_POLICY_ID")
    private String resourcePolicyId;

    @Column(name = "COMPUTE_RESOURCE_ID")
    private String computeResourceId;

    @Column(name = "GROUP_RESOURCE_PROFILE_ID")
    private String groupResourceProfileId;

    @Column(name = "QUEUE_NAME")
    private String queuename;

    @Column(name = "MAX_ALLOWED_NODES")
    private int maxAllowedNodes;

    @Column(name = "MAX_ALLOWED_CORES")
    private int maxAllowedCores;

    @Column(name = "MAX_ALLOWED_WALLTIME")
    private int maxAllowedWalltime;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class, cascade = CascadeType.MERGE)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID")
    private GroupResourceProfileEntity groupResourceProfile;

    public BatchQueueResourcePolicyEntity() {
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

    public String getQueuename() {
        return queuename;
    }

    public void setQueuename(String queuename) {
        this.queuename = queuename;
    }

    public int getMaxAllowedNodes() {
        return maxAllowedNodes;
    }

    public void setMaxAllowedNodes(int maxAllowedNodes) {
        this.maxAllowedNodes = maxAllowedNodes;
    }

    public int getMaxAllowedCores() {
        return maxAllowedCores;
    }

    public void setMaxAllowedCores(int maxAllowedCores) {
        this.maxAllowedCores = maxAllowedCores;
    }

    public int getMaxAllowedWalltime() {
        return maxAllowedWalltime;
    }

    public void setMaxAllowedWalltime(int maxAllowedWalltime) {
        this.maxAllowedWalltime = maxAllowedWalltime;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}
