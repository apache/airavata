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
 * Outcome of a {@link DbTask} run. Mirrors Helix {@code TaskResult.Status} so the executor
 * can advance TASK/PROCESS state without depending on Helix.
 */
public final class DbTaskResult {

    public enum Status {
        /** Task finished successfully; advance to the next DAG task. */
        COMPLETED,
        /** Task failed but may be retried (executor decides based on attempt count). */
        FAILED,
        /** Task failed unrecoverably; fail the process immediately, no retry. */
        FATAL_FAILED,
        /** Task was skipped (treated as success for DAG advancement). */
        SKIPPED
    }

    private final Status status;
    private final String message;

    private DbTaskResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status status() {
        return status;
    }

    public String message() {
        return message;
    }

    public static DbTaskResult completed(String message) {
        return new DbTaskResult(Status.COMPLETED, message);
    }

    public static DbTaskResult failed(String message) {
        return new DbTaskResult(Status.FAILED, message);
    }

    public static DbTaskResult fatal(String message) {
        return new DbTaskResult(Status.FATAL_FAILED, message);
    }

    public static DbTaskResult skipped(String message) {
        return new DbTaskResult(Status.SKIPPED, message);
    }
}
