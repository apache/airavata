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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The persistent class for the resource_job_manager database table.
 */
@Entity
@Table(name = "RESOURCE_JOB_MANAGER")
public class ResourceJobManagerEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_JOB_MANAGER_ID")
    private String resourceJobManagerId;

    @Column(name = "CREATION_TIME", nullable = false, updatable = false)
    private Timestamp creationTime = AiravataUtils.getCurrentTimestamp();

    @Column(name = "JOB_MANAGER_BIN_PATH")
    private String jobManagerBinPath;

    @Column(name = "PUSH_MONITORING_ENDPOINT")
    private String pushMonitoringEndpoint;

    @Column(name = "RESOURCE_JOB_MANAGER_TYPE")
    @Enumerated(EnumType.STRING)
    private ResourceJobManagerType resourceJobManagerType;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    public ResourceJobManagerEntity() {
    }

    public String getResourceJobManagerId() {
        return resourceJobManagerId;
    }

    public void setResourceJobManagerId(String resourceJobManagerId) {
        this.resourceJobManagerId = resourceJobManagerId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getJobManagerBinPath() {
        return jobManagerBinPath;
    }

    public void setJobManagerBinPath(String jobManagerBinPath) {
        this.jobManagerBinPath = jobManagerBinPath;
    }

    public String getPushMonitoringEndpoint() {
        return pushMonitoringEndpoint;
    }

    public void setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
        this.pushMonitoringEndpoint = pushMonitoringEndpoint;
    }

    public ResourceJobManagerType getResourceJobManagerType() {
        return resourceJobManagerType;
    }

    public void setResourceJobManagerType(ResourceJobManagerType resourceJobManagerType) {
        this.resourceJobManagerType = resourceJobManagerType;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
