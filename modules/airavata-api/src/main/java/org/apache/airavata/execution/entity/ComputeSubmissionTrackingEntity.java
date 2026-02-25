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
package org.apache.airavata.execution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "compute_submission_tracking")
public class ComputeSubmissionTrackingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "compute_resource_id", nullable = false, length = 255)
    private String computeResourceId;

    @Column(name = "last_submission_time", nullable = false)
    private long lastSubmissionTime;

    public ComputeSubmissionTrackingEntity() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public long getLastSubmissionTime() {
        return lastSubmissionTime;
    }

    public void setLastSubmissionTime(long lastSubmissionTime) {
        this.lastSubmissionTime = lastSubmissionTime;
    }
}
