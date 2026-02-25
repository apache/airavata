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
 * A single executable unit in a process DAG.
 *
 * <p>Implementations are Spring beans with only task-specific dependencies
 * injected via constructor. Cross-cutting concerns (status publishing, error
 * recording, metrics, logging) are handled by {@link TaskInterceptor}s
 * applied by the {@link ProcessDAGEngine}.
 *
 * <p>Tasks must be stateless between executions; all mutable state lives
 * in the {@link TaskContext} and DAG state map.
 */
public interface DagTask {

    /**
     * Executes this task's logic.
     *
     * @param context the resolved task context containing process, experiment,
     *                resource, and application metadata
     * @return a {@link DagTaskResult} indicating success or failure
     */
    DagTaskResult execute(TaskContext context);
}
