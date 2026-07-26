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
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.status.proto.ExperimentState}.
 */
public enum ExperimentState {
    EXPERIMENT_STATE_UNKNOWN(0),
    EXPERIMENT_STATE_CREATED(1),
    EXPERIMENT_STATE_VALIDATED(2),
    EXPERIMENT_STATE_SCHEDULED(3),
    EXPERIMENT_STATE_LAUNCHED(4),
    EXPERIMENT_STATE_EXECUTING(5),
    EXPERIMENT_STATE_CANCELING(6),
    EXPERIMENT_STATE_CANCELED(7),
    EXPERIMENT_STATE_COMPLETED(8),
    EXPERIMENT_STATE_FAILED(9);

    private final int number;

    ExperimentState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static ExperimentState forNumber(int number) {
        for (ExperimentState value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
