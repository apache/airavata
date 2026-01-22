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
package org.apache.airavata.orchestrator.messaging;

import org.apache.airavata.orchestrator.internal.messaging.DaprTopics;

/**
 * Public constants for messaging topic names.
 *
 * <p>Delegates to internal DaprTopics for actual values.
 */
public final class Topics {

    /** Topic for status change events (experiment, process, task, job status). */
    public static final String STATUS = DaprTopics.STATUS;

    /** Topic for experiment launch and lifecycle events. */
    public static final String EXPERIMENT = DaprTopics.EXPERIMENT;

    /** Topic for process launch and lifecycle events. */
    public static final String PROCESS = DaprTopics.PROCESS;

    /** Topic for parsing completion messages. */
    public static final String PARSING = DaprTopics.PARSING;

    /** Topic for monitoring messages. */
    public static final String MONITORING = DaprTopics.MONITORING;

    /** Topic for job status monitoring messages. */
    public static final String MONITORING_JOB_STATUS = DaprTopics.MONITORING_JOB_STATUS;

    private Topics() {
        // Utility class - prevent instantiation
    }
}
