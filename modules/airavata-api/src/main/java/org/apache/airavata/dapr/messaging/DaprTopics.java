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
package org.apache.airavata.dapr.messaging;

/**
 * Centralized constants for Dapr Pub/Sub topic names.
 *
 * <p>All Dapr topic names used across the airavata-api module should be defined here
 * to ensure consistency and make topic usage discoverable.
 *
 * <p>Topic naming convention:
 * <ul>
 *   <li>Core topics: simple descriptive names (e.g., "status-topic")</li>
 *   <li>Specialized topics: descriptive names with context (e.g., "parsing-data-topic")</li>
 * </ul>
 */
public final class DaprTopics {

    // Core topics - used for general messaging
    /** Topic for status change events (experiment, process, task, job status). */
    public static final String STATUS = "status-topic";

    /** Topic for experiment launch and lifecycle events. */
    public static final String EXPERIMENT = "experiment-topic";

    /** Topic for process launch and lifecycle events. */
    public static final String PROCESS = "process-topic";

    // Specialized topics - used for specific workflows
    /** Topic for parsing completion messages. */
    public static final String PARSING = "parsing-data-topic";

    /** Topic for monitoring messages (raw JSON format: {jobName, status, task}). */
    public static final String MONITORING = "monitoring-data-topic";

    /** Topic for job status monitoring messages (JobStatusResult format). */
    public static final String MONITORING_JOB_STATUS = "monitoring-job-status-topic";

    private DaprTopics() {
        // Utility class - prevent instantiation
    }
}
