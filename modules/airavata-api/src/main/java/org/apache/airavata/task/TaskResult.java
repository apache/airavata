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
package org.apache.airavata.task;

/**
 * Task execution result.
 *
 * <p>Replaces org.apache.helix.task.TaskResult as part of the migration from Helix to Dapr.
 * This class provides the same API surface as Helix's TaskResult for compatibility
 * with existing task implementations.
 */
public class TaskResult {

    public enum Status {
        COMPLETED,
        FAILED,
        FATAL_FAILED
    }

    private final Status status;
    private final String info;

    public TaskResult(Status status, String info) {
        this.status = status;
        this.info = info;
    }

    public TaskResult(Status status) {
        this(status, null);
    }

    public Status getStatus() {
        return status;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "TaskResult{status=" + status + ", info='" + info + "'}";
    }
}
