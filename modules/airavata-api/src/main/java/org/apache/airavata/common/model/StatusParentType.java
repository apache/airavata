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
 * Enum representing the type of parent entity for a unified status record.
 * This allows a single STATUS table to store status history for experiments, processes,
 * tasks, jobs, queues, workflows, applications, and handlers.
 */
public enum StatusParentType {
    EXPERIMENT(0),
    PROCESS(1),
    TASK(2),
    JOB(3),
    QUEUE(4),
    WORKFLOW(5),
    APPLICATION(6),
    HANDLER(7);

    private final int value;

    StatusParentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static StatusParentType findByValue(int value) {
        switch (value) {
            case 0:
                return EXPERIMENT;
            case 1:
                return PROCESS;
            case 2:
                return TASK;
            case 3:
                return JOB;
            case 4:
                return QUEUE;
            case 5:
                return WORKFLOW;
            case 6:
                return APPLICATION;
            case 7:
                return HANDLER;
            default:
                return null;
        }
    }
}
