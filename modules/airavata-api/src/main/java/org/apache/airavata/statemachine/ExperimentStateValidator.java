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
import org.apache.airavata.common.model.ExperimentState;

/**
 * Validates ExperimentState transitions. Makes allowed transitions explicit and traceable.
 *
 * <p>See DAPR_WORKFLOW_STATE_MACHINE_SPEC.md for complete transition rules.
 * Terminal states: COMPLETED, FAILED, CANCELED (no transitions out).
 */
public enum ExperimentStateValidator implements StateValidator<ExperimentState> {
    INSTANCE;

    private static final Set<StateTransition<ExperimentState>> VALID_TRANSITIONS = Set.of(
            // Initial transitions
            new StateTransition<>(ExperimentState.CREATED, ExperimentState.SCHEDULED),
            new StateTransition<>(ExperimentState.CREATED, ExperimentState.LAUNCHED),
            new StateTransition<>(ExperimentState.CREATED, ExperimentState.FAILED),

            // From SCHEDULED
            new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.LAUNCHED),
            new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.SCHEDULED),
            new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.CANCELING),

            // From LAUNCHED
            new StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
            new StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.CANCELING),

            // From EXECUTING - can complete, fail, cancel, or be rescheduled
            new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.COMPLETED),
            new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.FAILED),
            new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELED),
            new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.SCHEDULED),
            new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELING),

            // Cancellation flow
            new StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELING),
            new StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELED));

    @Override
    public Set<StateTransition<ExperimentState>> getValidTransitions() {
        return VALID_TRANSITIONS;
    }
}
