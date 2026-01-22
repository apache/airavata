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
package org.apache.airavata.orchestrator.state;

import java.util.Set;
import org.apache.airavata.common.model.JobState;

/**
 * Validates JobState transitions.
 * Terminal states: COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN
 */
public enum JobStateValidator implements StateValidator<JobState> {
    INSTANCE;

    private static final Set<StateTransition<JobState>> VALID_TRANSITIONS = Set.of(
            // From SUBMITTED
            new StateTransition<>(JobState.SUBMITTED, JobState.QUEUED),
            new StateTransition<>(JobState.SUBMITTED, JobState.ACTIVE),
            new StateTransition<>(JobState.SUBMITTED, JobState.COMPLETE),
            new StateTransition<>(JobState.SUBMITTED, JobState.CANCELED),
            new StateTransition<>(JobState.SUBMITTED, JobState.FAILED),
            new StateTransition<>(JobState.SUBMITTED, JobState.SUSPENDED),
            new StateTransition<>(JobState.SUBMITTED, JobState.UNKNOWN),
            new StateTransition<>(JobState.SUBMITTED, JobState.NON_CRITICAL_FAIL),
            // From QUEUED
            new StateTransition<>(JobState.QUEUED, JobState.ACTIVE),
            new StateTransition<>(JobState.QUEUED, JobState.COMPLETE),
            new StateTransition<>(JobState.QUEUED, JobState.CANCELED),
            new StateTransition<>(JobState.QUEUED, JobState.FAILED),
            new StateTransition<>(JobState.QUEUED, JobState.SUSPENDED),
            new StateTransition<>(JobState.QUEUED, JobState.UNKNOWN),
            new StateTransition<>(JobState.QUEUED, JobState.NON_CRITICAL_FAIL),
            // From ACTIVE
            new StateTransition<>(JobState.ACTIVE, JobState.COMPLETE),
            new StateTransition<>(JobState.ACTIVE, JobState.CANCELED),
            new StateTransition<>(JobState.ACTIVE, JobState.FAILED),
            new StateTransition<>(JobState.ACTIVE, JobState.SUSPENDED),
            new StateTransition<>(JobState.ACTIVE, JobState.UNKNOWN),
            new StateTransition<>(JobState.ACTIVE, JobState.NON_CRITICAL_FAIL),
            // From NON_CRITICAL_FAIL (recoverable)
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.COMPLETE),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.CANCELED),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.FAILED),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.SUSPENDED),
            new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.UNKNOWN));

    @Override
    public Set<StateTransition<JobState>> getValidTransitions() {
        return VALID_TRANSITIONS;
    }
}
