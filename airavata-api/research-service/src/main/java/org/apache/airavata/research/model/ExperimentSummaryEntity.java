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
package org.apache.airavata.research.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

/**
 * Read-only experiment summary, derived live from EXPERIMENT plus the latest EXPERIMENT_STATUS and the
 * process scheduling host. Mapped with {@link Subselect} rather than a physical EXPERIMENT_SUMMARY table:
 * the upstream summary was a DB view, but nothing populates a table, so the dashboard "recent experiments"
 * search came back empty. A {@code @Subselect} entity has no table, so it is excluded from {@code ddl-auto}
 * validation and always reflects current data. {@link Synchronize} flushes the backing tables before reads.
 */
@Entity
@Immutable
@Synchronize({"EXPERIMENT", "EXPERIMENT_STATUS", "PROCESS", "PROCESS_RESOURCE_SCHEDULE"})
@Subselect("select e.EXPERIMENT_ID as EXPERIMENT_ID, e.PROJECT_ID as PROJECT_ID,"
        + " e.GATEWAY_ID as GATEWAY_ID, e.CREATION_TIME as CREATION_TIME, e.USER_NAME as USER_NAME,"
        + " e.EXPERIMENT_NAME as EXPERIMENT_NAME, e.DESCRIPTION as DESCRIPTION, e.EXECUTION_ID as EXECUTION_ID,"
        + " COALESCE((select s.STATE from EXPERIMENT_STATUS s where s.EXPERIMENT_ID = e.EXPERIMENT_ID"
        + "   order by s.TIME_OF_STATE_CHANGE desc limit 1), '') as STATE,"
        + " COALESCE((select prs.RESOURCE_HOST_ID from PROCESS p"
        + "   join PROCESS_RESOURCE_SCHEDULE prs on prs.PROCESS_ID = p.PROCESS_ID"
        + "   where p.EXPERIMENT_ID = e.EXPERIMENT_ID order by p.CREATION_TIME desc limit 1), '') as RESOURCE_HOST_ID,"
        + " (select s.TIME_OF_STATE_CHANGE from EXPERIMENT_STATUS s where s.EXPERIMENT_ID = e.EXPERIMENT_ID"
        + "   order by s.TIME_OF_STATE_CHANGE desc limit 1) as TIME_OF_STATE_CHANGE"
        + " from EXPERIMENT e")
public class ExperimentSummaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "EXPERIMENT_ID")
    private String experimentId;

    @Column(name = "PROJECT_ID")
    private String projectId;

    @Column(name = "GATEWAY_ID")
    private String gatewayId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EXPERIMENT_NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "EXECUTION_ID")
    private String executionId;

    @Column(name = "STATE")
    private String experimentStatus;

    @Column(name = "RESOURCE_HOST_ID")
    private String resourceHostId;

    @Column(name = "TIME_OF_STATE_CHANGE")
    private Timestamp statusUpdateTime;

    public ExperimentSummaryEntity() {}

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(String experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }
}
