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
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The persistent class for the batch_queue database table.
 */
@Entity
@Table(name = "batch_queue")
public class BatchQueueEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private BatchQueuePK id;

    @Column(name = "MAX_JOB_IN_QUEUE")
    private int maxJobInQueue;

    @Column(name = "MAX_MEMORY")
    private int maxMemory;

    @Column(name = "MAX_NODES")
    private int maxNodes;

    @Column(name = "MAX_PROCESSORS")
    private int maxProcessors;

    @Column(name = "MAX_RUNTIME")
    private int maxRuntime;

    @Column(name = "QUEUE_DESCRIPTION")
    private String queueDescription;

    public BatchQueueEntity() {
    }

    public BatchQueuePK getId() {
        return id;
    }

    public void setId(BatchQueuePK id) {
        this.id = id;
    }

    public int getMaxJobInQueue() {
        return maxJobInQueue;
    }

    public void setMaxJobInQueue(int maxJobInQueue) {
        this.maxJobInQueue = maxJobInQueue;
    }

    public int getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMaxProcessors() {
        return maxProcessors;
    }

    public void setMaxProcessors(int maxProcessors) {
        this.maxProcessors = maxProcessors;
    }

    public int getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(int maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription = queueDescription;
    }
}