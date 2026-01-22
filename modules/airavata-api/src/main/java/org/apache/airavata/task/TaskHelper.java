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

import org.apache.airavata.agents.api.AdaptorSupport;

/**
 * Helper interface providing access to adaptor support for task execution.
 *
 * <p>Tasks use {@code TaskHelper} to access adaptor support, which provides
 * connections to compute resources, storage resources, and other infrastructure
 * needed for task execution.
 *
 * <p>Tasks receive a {@code TaskHelper} instance through their {@code onRun(TaskHelper)}
 * method, allowing them to interact with compute resources, transfer data, and
 * perform other operations required for task execution.
 *
 * <p>Example usage:
 * <pre>{@code
 * public TaskResult onRun(TaskHelper helper) {
 *     AdaptorSupport adaptorSupport = helper.getAdaptorSupport();
 *     // Use adaptorSupport to interact with compute resources
 *     return onSuccess("Task completed");
 * }
 * }</pre>
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 * @see org.apache.airavata.task.base.AbstractTask
 * @see org.apache.airavata.agents.api.AdaptorSupport
 */
public interface TaskHelper {
    public AdaptorSupport getAdaptorSupport();
}
