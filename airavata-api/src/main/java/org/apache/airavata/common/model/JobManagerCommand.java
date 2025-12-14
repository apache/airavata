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
package org.apache.airavata.common.model;

/**
 * Domain enum: JobManagerCommand
 */
public enum JobManagerCommand {
    SUBMISSION(0),
    JOB_MONITORING(1),
    DELETION(2),
    CHECK_JOB(3),
    SHOW_QUEUE(4),
    SHOW_RESERVATION(5),
    SHOW_START(6),
    SHOW_CLUSTER_INFO(7),
    SHOW_NO_OF_RUNNING_JOBS(8),
    SHOW_NO_OF_PENDING_JOBS(9);

    private final int value;

    JobManagerCommand(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static JobManagerCommand findByValue(int value) {
        switch (value) {
            case 0:
                return SUBMISSION;
            case 1:
                return JOB_MONITORING;
            case 2:
                return DELETION;
            case 3:
                return CHECK_JOB;
            case 4:
                return SHOW_QUEUE;
            case 5:
                return SHOW_RESERVATION;
            case 6:
                return SHOW_START;
            case 7:
                return SHOW_CLUSTER_INFO;
            case 8:
                return SHOW_NO_OF_RUNNING_JOBS;
            case 9:
                return SHOW_NO_OF_PENDING_JOBS;
            default:
                return null;
        }
    }
}
