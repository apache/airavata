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
 * Helix-free task SPI for the DB-transactional executor. A {@link DbTask} performs the
 * unit of work for a single TASK row, given a {@link TaskContext} already resolved from
 * the registry. The executor sequences tasks via {@code PROCESS.TASK_DAG} and persists
 * state in {@code EXEC_STATUS}; implementations do their work and return a {@link DbTaskResult}.
 */
public interface DbTask {

    /** Run the task. The context carries processId/gatewayId/taskId plus resolved models. */
    DbTaskResult run(TaskContext ctx);

    /** Best-effort cancellation hook; default no-op. */
    default void cancel(TaskContext ctx) {}
}
