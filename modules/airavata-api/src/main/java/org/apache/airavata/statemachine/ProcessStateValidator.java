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
package org.apache.airavata.statemachine;

import java.util.Set;
import org.apache.airavata.common.model.ProcessState;

/**
 * Validates ProcessState transitions. Makes allowed transitions explicit and traceable.
 *
 * <p>See DAPR_WORKFLOW_STATE_MACHINE_SPEC.md for complete transition rules.
 * Terminal states: COMPLETED, FAILED, CANCELED (no transitions out).
 */
public enum ProcessStateValidator implements StateValidator<ProcessState> {
    INSTANCE;

    private static final Set<StateTransition<ProcessState>> VALID_TRANSITIONS = Set.of(
            // Initial transitions
            new StateTransition<>(ProcessState.CREATED, ProcessState.VALIDATED),
            new StateTransition<>(ProcessState.CREATED, ProcessState.STARTED),
            new StateTransition<>(ProcessState.CREATED, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.CREATED, ProcessState.FAILED),

            // From VALIDATED
            new StateTransition<>(ProcessState.VALIDATED, ProcessState.STARTED),
            new StateTransition<>(ProcessState.VALIDATED, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.VALIDATED, ProcessState.FAILED),

            // From STARTED - forward flow
            new StateTransition<>(ProcessState.STARTED, ProcessState.PRE_PROCESSING),
            new StateTransition<>(ProcessState.STARTED, ProcessState.CONFIGURING_WORKSPACE),
            new StateTransition<>(ProcessState.STARTED, ProcessState.INPUT_DATA_STAGING),
            new StateTransition<>(ProcessState.STARTED, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.STARTED, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.STARTED, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.STARTED, ProcessState.FAILED),

            // Forward flow through pre-processing stages
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CONFIGURING_WORKSPACE),
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.INPUT_DATA_STAGING),
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.FAILED),
            new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.INPUT_DATA_STAGING),
            new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.FAILED),
            new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.FAILED),

            // From EXECUTING - can go to monitoring, output staging, completion, or queue
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.MONITORING),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.OUTPUT_DATA_STAGING),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.POST_PROCESSING),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.COMPLETED),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.FAILED),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.REQUEUED),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELED),

            // From MONITORING
            new StateTransition<>(ProcessState.MONITORING, ProcessState.OUTPUT_DATA_STAGING),
            new StateTransition<>(ProcessState.MONITORING, ProcessState.POST_PROCESSING),
            new StateTransition<>(ProcessState.MONITORING, ProcessState.COMPLETED),
            new StateTransition<>(ProcessState.MONITORING, ProcessState.FAILED),
            new StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELED),

            // From OUTPUT_DATA_STAGING
            new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.POST_PROCESSING),
            new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.COMPLETED),
            new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.FAILED),
            new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELED),

            // From POST_PROCESSING
            new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.COMPLETED),
            new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.FAILED),
            new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELED),

            // Queue-related transitions
            new StateTransition<>(ProcessState.QUEUED, ProcessState.DEQUEUING),
            new StateTransition<>(ProcessState.QUEUED, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELED),
            new StateTransition<>(ProcessState.QUEUED, ProcessState.FAILED),
            new StateTransition<>(ProcessState.REQUEUED, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.REQUEUED, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELED),
            new StateTransition<>(ProcessState.REQUEUED, ProcessState.FAILED),
            new StateTransition<>(ProcessState.DEQUEUING, ProcessState.EXECUTING),
            new StateTransition<>(ProcessState.DEQUEUING, ProcessState.QUEUED),
            new StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELLING),
            new StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELED),

            // Cancellation flow
            new StateTransition<>(ProcessState.CANCELLING, ProcessState.CANCELED),
            new StateTransition<>(ProcessState.CANCELLING, ProcessState.FAILED));

    @Override
    public Set<StateTransition<ProcessState>> getValidTransitions() {
        return VALID_TRANSITIONS;
    }
}
