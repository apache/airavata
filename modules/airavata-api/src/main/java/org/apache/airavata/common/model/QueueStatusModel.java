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
 * Domain model: QueueStatusModel
 */
public class QueueStatusModel {
    private String hostName;
    private String queueName;
    private boolean queueUp;
    private int runningJobs;
    private int queuedJobs;
    private long time;

    public QueueStatusModel() {}

    public QueueStatusModel(String hostName, String queueName, boolean isUp, int running, int queued, long timeMillis) {
        this.hostName = hostName;
        this.queueName = queueName;
        this.queueUp = isUp;
        this.runningJobs = running;
        this.queuedJobs = queued;
        this.time = timeMillis;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean getQueueUp() {
        return queueUp;
    }

    public void setQueueUp(boolean queueUp) {
        this.queueUp = queueUp;
    }

    public int getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs = runningJobs;
    }

    public int getQueuedJobs() {
        return queuedJobs;
    }

    public void setQueuedJobs(int queuedJobs) {
        this.queuedJobs = queuedJobs;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueStatusModel that = (QueueStatusModel) o;
        return Objects.equals(hostName, that.hostName)
                && Objects.equals(queueName, that.queueName)
                && Objects.equals(queueUp, that.queueUp)
                && Objects.equals(runningJobs, that.runningJobs)
                && Objects.equals(queuedJobs, that.queuedJobs)
                && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, queueName, queueUp, runningJobs, queuedJobs, time);
    }

    @Override
    public String toString() {
        return "QueueStatusModel{" + "hostName=" + hostName + ", queueName=" + queueName + ", queueUp=" + queueUp
                + ", runningJobs=" + runningJobs + ", queuedJobs=" + queuedJobs + ", time=" + time + "}";
    }
}
