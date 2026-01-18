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
 * Domain model: JobStatusChangeEvent
 */
public class JobStatusChangeEvent extends MessagingEvent {
    private JobState state;
    private JobIdentifier jobIdentity;

    public JobStatusChangeEvent() {}

    public JobStatusChangeEvent(JobState state, JobIdentifier jobIdentity) {
        this.state = state;
        this.jobIdentity = jobIdentity;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public JobIdentifier getJobIdentity() {
        return jobIdentity;
    }

    public void setJobIdentity(JobIdentifier jobIdentity) {
        this.jobIdentity = jobIdentity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobStatusChangeEvent that = (JobStatusChangeEvent) o;
        return Objects.equals(state, that.state) && Objects.equals(jobIdentity, that.jobIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, jobIdentity);
    }

    @Override
    public String toString() {
        return "JobStatusChangeEvent{" + "state=" + state + ", jobIdentity=" + jobIdentity + "}";
    }
}
