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
package org.apache.airavata.agent.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "agent_execution_status")
public class AgentExecutionStatusEntity {

    public enum ExecutionStatus {
        SUBMITTED_TO_CLUSTER,
        FAILED,
        CONNECTED,
        CONNECTION_BROKEN,
        TERMINATING,
        TERMINATED,
    }

    @Id
    @UuidGenerator
    @Column(name = "agent_execution_status_id")
    private String id;

    @ManyToOne(targetEntity = AgentExecutionEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AgentExecutionEntity agentExecution;

    @Column(name = "updated_at")
    private long updateTime;

    @Column(name = "status")
    private ExecutionStatus status;

    @Column(name = "additional_info", length = 2000)
    private String additionalInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AgentExecutionEntity getAgentExecution() {
        return agentExecution;
    }

    public void setAgentExecution(AgentExecutionEntity agentExecution) {
        this.agentExecution = agentExecution;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
