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
package org.apache.airavata.models.workflow;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.WorkflowState}.
 */
public enum WorkflowState {
    WORKFLOW_STATE_UNKNOWN(0),
    WORKFLOW_STATE_CREATED(1),
    WORKFLOW_STATE_VALIDATED(2),
    WORKFLOW_STATE_SCHEDULED(3),
    WORKFLOW_STATE_LAUNCHED(4),
    WORKFLOW_STATE_EXECUTING(5),
    WORKFLOW_STATE_PAUSING(6),
    WORKFLOW_STATE_PAUSED(7),
    WORKFLOW_STATE_RESTARTING(8),
    WORKFLOW_STATE_CANCELING(9),
    WORKFLOW_STATE_CANCELED(10),
    WORKFLOW_STATE_COMPLETED(11),
    WORKFLOW_STATE_FAILED(12);

    private final int number;

    WorkflowState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static WorkflowState forNumber(int number) {
        for (WorkflowState value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
