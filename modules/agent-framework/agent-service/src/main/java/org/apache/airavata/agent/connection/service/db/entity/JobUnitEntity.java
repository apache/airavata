/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.agent.connection.service.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.airavata.agent.connection.service.models.JobUnitStatus;

import java.time.Instant;

@Entity
@Table(name = "JOB_UNIT")
public class JobUnitEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BATCH_ID", nullable = false)
    private JobBatchEntity batch;

    @Column(name = "EXPERIMENT_ID", nullable = false)
    private String experimentId;

    @Column(name = "CREATED_AT", updatable = false, insertable = false)
    private Instant createdAt;

    @Lob
    @Column(name = "RESOLVED_COMMAND", nullable = false)
    private String resolvedCommand;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 16, nullable = false)
    private JobUnitStatus status = JobUnitStatus.PENDING;

    @Column(name = "AGENT_ID")
    private String agentId;

    @Column(name = "STARTED_AT")
    private Instant startedAt;

    @Column(name = "COMPLETED_AT")
    private Instant completedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JobBatchEntity getBatch() {
        return batch;
    }

    public void setBatch(JobBatchEntity batch) {
        this.batch = batch;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getResolvedCommand() {
        return resolvedCommand;
    }

    public void setResolvedCommand(String resolvedCommand) {
        this.resolvedCommand = resolvedCommand;
    }

    public JobUnitStatus getStatus() {
        return status;
    }

    public void setStatus(JobUnitStatus status) {
        this.status = status;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
