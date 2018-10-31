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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The persistent class for the batch_queue_resource_policy database table.
 */
@Entity
@Table(name = "BATCH_QUEUE_RESOURCE_POLICY")
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
    private Integer maxAllowedNodes;

    @Column(name = "MAX_ALLOWED_CORES")
    private Integer maxAllowedCores;

    @Column(name = "MAX_ALLOWED_WALLTIME")
    private Integer maxAllowedWalltime;

    @ManyToOne(targetEntity = GroupResourceProfileEntity.class)
    @JoinColumn(name = "GROUP_RESOURCE_PROFILE_ID", nullable = false, updatable = false)
    @ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
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

    public Integer getMaxAllowedNodes() {
        return maxAllowedNodes;
    }

    public void setMaxAllowedNodes(Integer maxAllowedNodes) {
        this.maxAllowedNodes = maxAllowedNodes;
    }

    public Integer getMaxAllowedCores() {
        return maxAllowedCores;
    }

    public void setMaxAllowedCores(Integer maxAllowedCores) {
        this.maxAllowedCores = maxAllowedCores;
    }

    public Integer getMaxAllowedWalltime() {
        return maxAllowedWalltime;
    }

    public void setMaxAllowedWalltime(Integer maxAllowedWalltime) {
        this.maxAllowedWalltime = maxAllowedWalltime;
    }

    public GroupResourceProfileEntity getGroupResourceProfile() {
        return groupResourceProfile;
    }

    public void setGroupResourceProfile(GroupResourceProfileEntity groupResourceProfile) {
        this.groupResourceProfile = groupResourceProfile;
    }
}
