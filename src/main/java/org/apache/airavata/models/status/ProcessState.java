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
package org.apache.airavata.models.status;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.status.proto.ProcessState}.
 */
public enum ProcessState {
    PROCESS_STATE_UNKNOWN(0),
    PROCESS_STATE_CREATED(1),
    PROCESS_STATE_VALIDATED(2),
    PROCESS_STATE_STARTED(3),
    PROCESS_STATE_PRE_PROCESSING(4),
    PROCESS_STATE_CONFIGURING_WORKSPACE(5),
    PROCESS_STATE_INPUT_DATA_STAGING(6),
    PROCESS_STATE_EXECUTING(7),
    PROCESS_STATE_MONITORING(8),
    PROCESS_STATE_OUTPUT_DATA_STAGING(9),
    PROCESS_STATE_POST_PROCESSING(10),
    PROCESS_STATE_COMPLETED(11),
    PROCESS_STATE_FAILED(12),
    PROCESS_STATE_CANCELLING(13),
    PROCESS_STATE_CANCELED(14),
    PROCESS_STATE_QUEUED(15),
    PROCESS_STATE_DEQUEUING(16),
    PROCESS_STATE_REQUEUED(17);

    private final int number;

    ProcessState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static ProcessState forNumber(int number) {
        for (ProcessState value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
