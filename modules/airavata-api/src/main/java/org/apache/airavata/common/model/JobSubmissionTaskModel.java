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
 * Domain model: JobSubmissionTaskModel
 */
public class JobSubmissionTaskModel {
    private JobSubmissionProtocol jobSubmissionProtocol;
    private MonitorMode monitorMode;
    private int wallTime;

    public JobSubmissionTaskModel() {}

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public MonitorMode getMonitorMode() {
        return monitorMode;
    }

    public void setMonitorMode(MonitorMode monitorMode) {
        this.monitorMode = monitorMode;
    }

    public int getWallTime() {
        return wallTime;
    }

    public void setWallTime(int wallTime) {
        this.wallTime = wallTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSubmissionTaskModel that = (JobSubmissionTaskModel) o;
        return Objects.equals(jobSubmissionProtocol, that.jobSubmissionProtocol)
                && Objects.equals(monitorMode, that.monitorMode)
                && Objects.equals(wallTime, that.wallTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobSubmissionProtocol, monitorMode, wallTime);
    }

    @Override
    public String toString() {
        return "JobSubmissionTaskModel{" + "jobSubmissionProtocol=" + jobSubmissionProtocol + ", monitorMode="
                + monitorMode + ", wallTime=" + wallTime + "}";
    }
}
