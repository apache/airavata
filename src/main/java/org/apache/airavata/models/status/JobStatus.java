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
package org.apache.airavata.models.status;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.status.proto.JobStatus}.
 */
public record JobStatus(JobState jobState, long timeOfStateChange, String reason, String statusId) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private JobState jobState = JobState.JOB_STATE_UNKNOWN;
        private long timeOfStateChange;
        private String reason = "";
        private String statusId = "";

        private Builder() {}

        private Builder(JobStatus source) {
            this.jobState = source.jobState;
            this.timeOfStateChange = source.timeOfStateChange;
            this.reason = source.reason;
            this.statusId = source.statusId;
        }

        public Builder setJobState(JobState jobState) {
            this.jobState = jobState;
            return this;
        }

        public Builder setTimeOfStateChange(long timeOfStateChange) {
            this.timeOfStateChange = timeOfStateChange;
            return this;
        }

        public Builder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder setStatusId(String statusId) {
            this.statusId = statusId;
            return this;
        }

        public JobStatus build() {
            return new JobStatus(jobState, timeOfStateChange, reason, statusId);
        }
    }
}
