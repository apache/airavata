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
package org.apache.airavata.models.appcatalog.computeresource;

/**
 * Enumeration of resource job manager commands.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.computeresource.proto.JobManagerCommand}.
 *
 * <ul>
 *   <li>{@code SUBMISSION}: Ex: qsub, sbatch.
 *   <li>{@code JOB_MONITORING}: Ex: qstat, squeue.
 *   <li>{@code DELETION}: Ex: qdel, scancel.
 *   <li>{@code CHECK_JOB}: Detailed Status about the Job. Ex: checkjob.
 *   <li>{@code SHOW_QUEUE}: List of Queued Job by the scheduler. Ex: showq.
 *   <li>{@code SHOW_RESERVATION}: List all reservations. Ex: showres.
 *   <li>{@code SHOW_START}: Display the start time of the specified job. Ex: showstart.
 * </ul>
 */
public enum JobManagerCommand {
    JOB_MANAGER_COMMAND_UNKNOWN(0),
    SUBMISSION(1),
    JOB_MONITORING(2),
    DELETION(3),
    CHECK_JOB(4),
    SHOW_QUEUE(5),
    SHOW_RESERVATION(6),
    SHOW_START(7),
    SHOW_CLUSTER_INFO(8),
    SHOW_NO_OF_RUNNING_JOBS(9),
    SHOW_NO_OF_PENDING_JOBS(10);

    private final int number;

    JobManagerCommand(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static JobManagerCommand forNumber(int number) {
        for (JobManagerCommand value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
