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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: JobStatus
 */
public class JobStatus {
    private JobState jobState;
    private long timeOfStateChange;
    private String reason;
    private String statusId;

    public JobStatus() {}

    public JobStatus(JobState jobState) {
        this.jobState = jobState;
    }

    public JobState getJobState() {
        return jobState;
    }

    public void setJobState(JobState jobState) {
        this.jobState = jobState;
    }

    public long getTimeOfStateChange() {
        return timeOfStateChange;
    }

    public void setTimeOfStateChange(long timeOfStateChange) {
        this.timeOfStateChange = timeOfStateChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobStatus that = (JobStatus) o;
        return Objects.equals(jobState, that.jobState)
                && Objects.equals(timeOfStateChange, that.timeOfStateChange)
                && Objects.equals(reason, that.reason)
                && Objects.equals(statusId, that.statusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobState, timeOfStateChange, reason, statusId);
    }

    @Override
    public String toString() {
        return "JobStatus{" + "jobState=" + jobState + ", timeOfStateChange=" + timeOfStateChange + ", reason=" + reason
                + ", statusId=" + statusId + "}";
    }
}
