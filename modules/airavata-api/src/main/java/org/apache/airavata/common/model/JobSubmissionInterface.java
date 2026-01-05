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
 * Domain model: JobSubmissionInterface
 */
public class JobSubmissionInterface {
    private String jobSubmissionInterfaceId;
    private JobSubmissionProtocol jobSubmissionProtocol;
    private int priorityOrder;

    public JobSubmissionInterface() {}

    public String getJobSubmissionInterfaceId() {
        return jobSubmissionInterfaceId;
    }

    public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
        this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
    }

    public JobSubmissionProtocol getJobSubmissionProtocol() {
        return jobSubmissionProtocol;
    }

    public void setJobSubmissionProtocol(JobSubmissionProtocol jobSubmissionProtocol) {
        this.jobSubmissionProtocol = jobSubmissionProtocol;
    }

    public int getPriorityOrder() {
        return priorityOrder;
    }

    public void setPriorityOrder(int priorityOrder) {
        this.priorityOrder = priorityOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSubmissionInterface that = (JobSubmissionInterface) o;
        return Objects.equals(jobSubmissionInterfaceId, that.jobSubmissionInterfaceId)
                && Objects.equals(jobSubmissionProtocol, that.jobSubmissionProtocol)
                && Objects.equals(priorityOrder, that.priorityOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobSubmissionInterfaceId, jobSubmissionProtocol, priorityOrder);
    }

    @Override
    public String toString() {
        return "JobSubmissionInterface{" + "jobSubmissionInterfaceId=" + jobSubmissionInterfaceId
                + ", jobSubmissionProtocol=" + jobSubmissionProtocol + ", priorityOrder=" + priorityOrder + "}";
    }
}
