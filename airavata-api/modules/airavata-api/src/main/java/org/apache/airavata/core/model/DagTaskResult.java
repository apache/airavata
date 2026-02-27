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
package org.apache.airavata.core.model;

import java.util.Map;

/**
 * Result of a {@link org.apache.airavata.execution.dag.DagTask} execution.
 *
 * <p>Sealed to exactly two outcomes: {@link Success} or {@link Failure}.
 * The DAG engine uses pattern matching to determine whether to follow
 * the success or failure edge from the current node.
 */
public sealed interface DagTaskResult {

    /**
     * The task completed successfully.
     *
     * @param message human-readable description of what happened
     * @param output  key-value pairs to merge into the DAG state map,
     *                making them available to downstream tasks
     */
    record Success(String message, Map<String, String> output) implements DagTaskResult {
        public Success(String message) {
            this(message, Map.of());
        }
    }

    /**
     * The task failed.
     *
     * @param reason human-readable failure description
     * @param fatal  if {@code true}, the failure is non-retryable
     * @param cause  optional underlying exception
     */
    record Failure(String reason, boolean fatal, Throwable cause) implements DagTaskResult {
        public Failure(String reason, boolean fatal) {
            this(reason, fatal, null);
        }

        public Failure(String reason) {
            this(reason, false, null);
        }
    }
}
