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
 * Domain model: TaskStatusChangeRequestEvent
 */
public class TaskStatusChangeRequestEvent extends MessagingEvent {
    private TaskState state;
    private TaskIdentifier taskIdentity;

    public TaskStatusChangeRequestEvent() {}

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public TaskIdentifier getTaskIdentity() {
        return taskIdentity;
    }

    public void setTaskIdentity(TaskIdentifier taskIdentity) {
        this.taskIdentity = taskIdentity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskStatusChangeRequestEvent that = (TaskStatusChangeRequestEvent) o;
        return Objects.equals(state, that.state) && Objects.equals(taskIdentity, that.taskIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, taskIdentity);
    }

    @Override
    public String toString() {
        return "TaskStatusChangeRequestEvent{" + "state=" + state + ", taskIdentity=" + taskIdentity + "}";
    }
}
