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
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.status.proto.QueueStatusModel}.
 */
public record QueueStatusModel(
        String hostName, String queueName, boolean queueUp, int runningJobs, int queuedJobs, long time) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String hostName = "";
        private String queueName = "";
        private boolean queueUp;
        private int runningJobs;
        private int queuedJobs;
        private long time;

        private Builder() {}

        private Builder(QueueStatusModel source) {
            this.hostName = source.hostName;
            this.queueName = source.queueName;
            this.queueUp = source.queueUp;
            this.runningJobs = source.runningJobs;
            this.queuedJobs = source.queuedJobs;
            this.time = source.time;
        }

        public Builder setHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder setQueueUp(boolean queueUp) {
            this.queueUp = queueUp;
            return this;
        }

        public Builder setRunningJobs(int runningJobs) {
            this.runningJobs = runningJobs;
            return this;
        }

        public Builder setQueuedJobs(int queuedJobs) {
            this.queuedJobs = queuedJobs;
            return this;
        }

        public Builder setTime(long time) {
            this.time = time;
            return this;
        }

        public QueueStatusModel build() {
            return new QueueStatusModel(hostName, queueName, queueUp, runningJobs, queuedJobs, time);
        }
    }
}
