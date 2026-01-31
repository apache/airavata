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
package org.apache.airavata.registry.entities.appcatalog;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;

/**
 * Entity representing a SLURM project/account associated with a compute resource (static catalog).
 * Projects can have access to specific queues/partitions on the resource.
 *
 * <p>This is resource-level metadata. Live partition and account visibility for a given
 * credential are credential- and cluster-specific; see {@link CredentialClusterInfoEntity}
 * and {@link org.apache.airavata.service.cluster.ClusterInfoService} for on-demand
 * partition/account data per credential.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE_PROJECT")
@IdClass(ComputeResourceProjectPK.class)
public class ComputeResourceProjectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false)
    private String computeResourceId;

    @Id
    @Column(name = "PROJECT_NAME", nullable = false)
    private String projectName;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToOne(targetEntity = ComputeResourceEntity.class)
    @JoinColumn(name = "COMPUTE_RESOURCE_ID", insertable = false, updatable = false)
    private ComputeResourceEntity computeResource;

    @OneToMany(
            targetEntity = ProjectQueueAccessEntity.class,
            cascade = CascadeType.ALL,
            mappedBy = "project",
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<ProjectQueueAccessEntity> queueAccess;

    public ComputeResourceProjectEntity() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComputeResourceEntity getComputeResource() {
        return computeResource;
    }

    public void setComputeResource(ComputeResourceEntity computeResource) {
        this.computeResource = computeResource;
    }

    public List<ProjectQueueAccessEntity> getQueueAccess() {
        return queueAccess;
    }

    public void setQueueAccess(List<ProjectQueueAccessEntity> queueAccess) {
        this.queueAccess = queueAccess;
    }
}
