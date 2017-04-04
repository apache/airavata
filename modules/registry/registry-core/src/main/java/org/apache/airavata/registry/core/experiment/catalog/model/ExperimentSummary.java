/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "EXPERIMENT_SUMMARY")
public class ExperimentSummary {
    private final static Logger logger = LoggerFactory.getLogger(ExperimentSummary.class);
    private String experimentId;
    private String projectId;
    private String gatewayId;
    private String userName;
    private String executionId;
    private String experimentName;
    private Timestamp creationTime;
    private String description;
    private String state;
    private String resourceHostId;
    private Timestamp timeOfStateChange;

    @Id
    @Column(name = "EXPERIMENT_ID")
    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Column(name = "PROJECT_ID")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(name = "GATEWAY_ID")
    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }


    @Column(name = "USER_NAME")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "EXECUTION_ID")
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Column(name = "EXPERIMENT_NAME")
    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    @Column(name = "CREATION_TIME")
    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "STATE")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "RESOURCE_HOST_ID")
    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    @Column(name = "TIME_OF_STATE_CHANGE")
    public Timestamp getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(Timestamp timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ExperimentSummary that = (ExperimentSummary) o;
//
//        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null)
//            return false;
//        if (gatewayId != null ? !gatewayId.equals(that.gatewayId) : that.gatewayId != null)
//            return false;
//        if (creationTime != null ? !creationTime.equals(that.creationTime) : that.creationTime != null) return false;
//        if (description != null ? !description.equals(that.description) : that.description != null) return false;
//        if (experimentId != null ? !experimentId.equals(that.experimentId) : that.experimentId != null) return false;
//        if (experimentName != null ? !experimentName.equals(that.experimentName) : that.experimentName != null)
//            return false;
//        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
//        if (resourceHostId != null ? !resourceHostId.equals(that.resourceHostId) : that.resourceHostId != null)
//            return false;
//        if (state != null ? !state.equals(that.state) : that.state != null) return false;
//        if (timeOfStateChange != null ? !timeOfStateChange.equals(that.timeOfStateChange) : that.timeOfStateChange != null)
//            return false;
//        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = experimentId != null ? experimentId.hashCode() : 0;
//        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
//        result = 31 * result + (userName != null ? userName.hashCode() : 0);
//        result = 31 * result + (gatewayId != null ? gatewayId.hashCode() : 0);
//        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
//        result = 31 * result + (experimentName != null ? experimentName.hashCode() : 0);
//        result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
//        result = 31 * result + (description != null ? description.hashCode() : 0);
//        result = 31 * result + (state != null ? state.hashCode() : 0);
//        result = 31 * result + (resourceHostId != null ? resourceHostId.hashCode() : 0);
//        result = 31 * result + (timeOfStateChange != null ? timeOfStateChange.hashCode() : 0);
//        return result;
//    }
}