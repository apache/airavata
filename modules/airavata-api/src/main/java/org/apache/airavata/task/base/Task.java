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
package org.apache.airavata.task.base;

import org.apache.airavata.task.TaskResult;

/**
 * Task interface.
 *
 * <p>Replaces org.apache.helix.task.Task as part of the migration from Helix to Dapr.
 * This interface provides the same API surface as Helix's Task for compatibility
 * with existing task implementations. Tasks will be converted to Dapr Activities in a future phase.
 */
public interface Task {

    /**
     * Initialize the task.
     *
     * @param workflowName The workflow name
     * @param jobName The job name
     * @param taskName The task name
     */
    void init(String workflowName, String jobName, String taskName);

    /**
     * Run the task.
     *
     * @return The task result
     */
    TaskResult run();

    /**
     * Cancel the task.
     */
    void cancel();
}
