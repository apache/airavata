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
package org.apache.airavata.execution.dag;

import org.apache.airavata.core.model.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;

/**
 * Cross-cutting concern applied around each {@link DagTask} execution
 * within the activity implementation.
 *
 * <p>Interceptors are ordered by Spring's {@code @Order} annotation.
 * All interceptors see the same {@link TaskContext} and {@link TaskNode},
 * enabling status publishing, error recording, metrics, and logging
 * without coupling to individual task implementations.
 */
public interface TaskInterceptor {

    /**
     * Called before the task executes.
     */
    default void before(TaskContext context, TaskNode node) {}

    /**
     * Called after a successful task execution.
     */
    default void afterSuccess(TaskContext context, TaskNode node, DagTaskResult.Success result) {}

    /**
     * Called after a failed task execution.
     */
    default void afterFailure(TaskContext context, TaskNode node, DagTaskResult.Failure result) {}
}
