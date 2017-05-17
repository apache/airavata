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

@Entity
@Table(name = "QUEUE_STATUS")
@IdClass(QueueStatusPK.class)
public class QueueStatus {
    private final static Logger logger = LoggerFactory.getLogger(QueueStatus.class);
    private String hostName;
    private String queueName;
    private Boolean queueUp;
    private int runningJobs;
    private int queuedJobs;
    private Long time;

    @Id
    @Column(name = "HOST_NAME")
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Id
    @Column(name = "QUEUE_NAME")
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Id
    @Column(name = "CREATED_TIME")
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Basic
    @Column(name = "QUEUE_UP")
    public Boolean getQueueUp() {
        return queueUp;
    }

    public void setQueueUp(Boolean queueUp) {
        this.queueUp = queueUp;
    }

    @Basic
    @Column(name = "RUNNING_JOBS")
    public int getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(int runningJobs) {
        this.runningJobs = runningJobs;
    }

    @Basic
    @Column(name = "QUEUED_JOBS")
    public int getQueuedJobs() {
        return queuedJobs;
    }

    public void setQueuedJobs(int queuedJobs) {
        this.queuedJobs = queuedJobs;
    }
}