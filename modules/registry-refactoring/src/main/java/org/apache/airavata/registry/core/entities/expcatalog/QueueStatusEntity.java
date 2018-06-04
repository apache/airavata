/*
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
 *
*/
package org.apache.airavata.registry.core.entities.expcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;

/**
 * The persistent class for the queue_status database table.
 */
@Entity
@Table(name = "QUEUE_STATUS")
@IdClass(QueueStatusPK.class)
public class QueueStatusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "HOST_NAME")
    private String hostName;

    @Id
    @Column(name = "QUEUE_NAME")
    private String queueName;

    @Id
    @Column(name = "CREATED_TIME")
    private BigInteger time;

    @Column(name = "QUEUE_UP")
    private boolean queueUp;

    @Column(name = "RUNNING_JOBS")
    private boolean runningJobs;

    @Column(name = "QUEUED_JOBS")
    private int queuedJobs;

    public QueueStatusEntity() {
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

    public BigInteger getTime() {
        return time;
    }

    public void setTime(BigInteger time) {
        this.time = time;
    }

    public boolean isQueueUp() {
        return queueUp;
    }

    public void setQueueUp(boolean queueUp) {
        this.queueUp = queueUp;
    }

    public boolean isRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(boolean runningJobs) {
        this.runningJobs = runningJobs;
    }

    public int getQueuedJobs() {
        return queuedJobs;
    }

    public void setQueuedJobs(int queuedJobs) {
        this.queuedJobs = queuedJobs;
    }

}
