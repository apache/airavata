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
package org.apache.airavata.agent.connection.service.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity(name = "AGENT_EXECUTION_STATUS")
public class AgentExecutionStatus {

    public static enum ExecutionStatus {
        SUBMITTED_TO_CLUSTER,
        FAILED,
        CONNECTED,
        CONNECTION_BROKEN,
        TERMINATING,
        TERMINATED,
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "AGENT_EXECUTION_STATUS_ID")
    private String id;

    @ManyToOne(targetEntity = AgentExecution.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private AgentExecution agentExecution;

    @Column(name = "UPDATED_TIME")
    private long updateTime;

    @Column(name = "STATUS")
    private ExecutionStatus status;

    @Column(name = "ADDITIONAL_INFO", length = 2000)
    private String additionalInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AgentExecution getAgentExecution() {
        return agentExecution;
    }

    public void setAgentExecution(AgentExecution agentExecution) {
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
