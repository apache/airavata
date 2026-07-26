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
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.status.proto.TaskState}.
 */
public enum TaskState {
    TASK_STATE_UNKNOWN(0),
    TASK_STATE_CREATED(1),
    TASK_STATE_EXECUTING(2),
    TASK_STATE_COMPLETED(3),
    TASK_STATE_FAILED(4),
    TASK_STATE_CANCELED(5);

    private final int number;

    TaskState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static TaskState forNumber(int number) {
        for (TaskState value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
