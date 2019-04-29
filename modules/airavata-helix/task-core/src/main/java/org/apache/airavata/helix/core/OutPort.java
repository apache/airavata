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
 */
package org.apache.airavata.helix.core;

import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class OutPort {

    private String nextJobId;
    private AbstractTask task;

    public OutPort(String nextJobId, AbstractTask task) {
        this.nextJobId = nextJobId;
        this.task = task;
    }

    public TaskResult invoke(TaskResult taskResult) {
        task.sendNextJob(nextJobId);
        return taskResult;
    }

    public String getNextJobId() {
        return nextJobId;
    }

    public OutPort setNextJobId(String nextJobId) {
        this.nextJobId = nextJobId;
        return this;
    }

    public AbstractTask getTask() {
        return task;
    }

    public OutPort setTask(AbstractTask task) {
        this.task = task;
        return this;
    }
}
