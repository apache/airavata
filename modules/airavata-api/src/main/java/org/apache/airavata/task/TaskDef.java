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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining task metadata, particularly the human-readable task name.
 *
 * <p>This annotation is used to mark task classes and provide a display name
 * for the task. The name is used for logging, monitoring, and user-facing
 * interfaces to identify tasks.
 *
 * <p>Usage example:
 * <pre>{@code
 * @TaskDef(name = "Job Submission Task")
 * public class JobSubmissionTask extends AiravataTask {
 *     // Task implementation
 * }
 * }</pre>
 *
 * <p>The task name should be descriptive and human-readable, as it may be
 * displayed in user interfaces, logs, and monitoring dashboards.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 * @see org.apache.airavata.task.base.AbstractTask
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TaskDef {
    public String name();
}
