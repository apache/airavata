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

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

public class QueueStatusPK implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(QueueStatusPK.class);
    private String hostName;
    private String queueName;
    private Long time;


    @Column(name = "HOST_NAME")
    @Id
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Column(name = "QUEUE_NAME")
    @Id
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Column(name = "CREATED_TIME")
    @Id
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueueStatusPK that = (QueueStatusPK) o;

        if (getHostName() != null ? !getHostName().equals(that.getHostName()) : that.getHostName() != null) return false;
        if (getQueueName() != null ? !getQueueName().equals(that.getQueueName()) : that.getQueueName() != null) return false;
        if (getTime() != null ? !getTime().equals(that.getTime()) : that.getTime() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getHostName() != null ? getHostName().hashCode() : 0;
        result = 31 * result + (getQueueName() != null ? getQueueName().hashCode() : 0);
        result = 31 * result + (getTime() != null ? getTime().hashCode() : 0);
        return result;
    }
}