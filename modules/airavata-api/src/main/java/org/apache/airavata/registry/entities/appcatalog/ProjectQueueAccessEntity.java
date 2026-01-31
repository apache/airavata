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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * Entity representing a project's access to a specific queue on a compute resource.
 */
@Entity
@Table(name = "PROJECT_QUEUE_ACCESS")
@IdClass(ProjectQueueAccessPK.class)
public class ProjectQueueAccessEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "COMPUTE_RESOURCE_ID", nullable = false)
    private String computeResourceId;

    @Id
    @Column(name = "PROJECT_NAME", nullable = false)
    private String projectName;

    @Id
    @Column(name = "QUEUE_NAME", nullable = false)
    private String queueName;

    @Column(name = "HAS_ACCESS", nullable = false)
    private boolean hasAccess = true;

    @ManyToOne(targetEntity = ComputeResourceProjectEntity.class)
    @JoinColumns({
        @JoinColumn(name = "COMPUTE_RESOURCE_ID", referencedColumnName = "COMPUTE_RESOURCE_ID", insertable = false, updatable = false),
        @JoinColumn(name = "PROJECT_NAME", referencedColumnName = "PROJECT_NAME", insertable = false, updatable = false)
    })
    private ComputeResourceProjectEntity project;

    public ProjectQueueAccessEntity() {}

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

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public ComputeResourceProjectEntity getProject() {
        return project;
    }

    public void setProject(ComputeResourceProjectEntity project) {
        this.project = project;
    }
}
