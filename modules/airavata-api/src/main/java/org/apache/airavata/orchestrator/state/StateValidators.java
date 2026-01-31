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
import org.apache.airavata.common.model.ExperimentState;
import org.apache.airavata.common.model.JobState;
import org.apache.airavata.common.model.ProcessState;
import org.apache.airavata.common.model.TaskState;

/**
 * State validator enums for experiment, job, process, and task.
 */
public final class StateValidators {

    private StateValidators() {}

    /** Validates ExperimentState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum ExperimentStateValidator implements StateModel.StateValidator<ExperimentState> {
        INSTANCE;

        private static final Set<StateModel.StateTransition<ExperimentState>> VALID_TRANSITIONS = Set.of(
                new StateModel.StateTransition<>(ExperimentState.CREATED, ExperimentState.SCHEDULED),
                new StateModel.StateTransition<>(ExperimentState.CREATED, ExperimentState.LAUNCHED),
                new StateModel.StateTransition<>(ExperimentState.CREATED, ExperimentState.FAILED),
                new StateModel.StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.LAUNCHED),
                new StateModel.StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.SCHEDULED),
                new StateModel.StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.CANCELING),
                new StateModel.StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                new StateModel.StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.CANCELING),
                new StateModel.StateTransition<>(ExperimentState.EXECUTING, ExperimentState.COMPLETED),
                new StateModel.StateTransition<>(ExperimentState.EXECUTING, ExperimentState.FAILED),
                new StateModel.StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELED),
                new StateModel.StateTransition<>(ExperimentState.EXECUTING, ExperimentState.SCHEDULED),
                new StateModel.StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELING),
                new StateModel.StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELING),
                new StateModel.StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELED));

        @Override
        public Set<StateModel.StateTransition<ExperimentState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates JobState transitions. Terminal states: COMPLETE, CANCELED, FAILED, SUSPENDED, UNKNOWN */
    public enum JobStateValidator implements StateModel.StateValidator<JobState> {
        INSTANCE;

        private static final Set<StateModel.StateTransition<JobState>> VALID_TRANSITIONS = Set.of(
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.QUEUED),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.ACTIVE),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.COMPLETE),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.CANCELED),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.FAILED),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.SUSPENDED),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.UNKNOWN),
                new StateModel.StateTransition<>(JobState.SUBMITTED, JobState.NON_CRITICAL_FAIL),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.ACTIVE),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.COMPLETE),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.CANCELED),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.FAILED),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.SUSPENDED),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.UNKNOWN),
                new StateModel.StateTransition<>(JobState.QUEUED, JobState.NON_CRITICAL_FAIL),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.COMPLETE),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.CANCELED),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.FAILED),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.SUSPENDED),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.UNKNOWN),
                new StateModel.StateTransition<>(JobState.ACTIVE, JobState.NON_CRITICAL_FAIL),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.COMPLETE),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.CANCELED),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.FAILED),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.SUSPENDED),
                new StateModel.StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.UNKNOWN));

        @Override
        public Set<StateModel.StateTransition<JobState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates ProcessState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum ProcessStateValidator implements StateModel.StateValidator<ProcessState> {
        INSTANCE;

        private static final Set<StateModel.StateTransition<ProcessState>> VALID_TRANSITIONS = Set.of(
                new StateModel.StateTransition<>(ProcessState.CREATED, ProcessState.VALIDATED),
                new StateModel.StateTransition<>(ProcessState.CREATED, ProcessState.STARTED),
                new StateModel.StateTransition<>(ProcessState.CREATED, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.CREATED, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.VALIDATED, ProcessState.STARTED),
                new StateModel.StateTransition<>(ProcessState.VALIDATED, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.VALIDATED, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.PRE_PROCESSING),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.CONFIGURING_WORKSPACE),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.INPUT_DATA_STAGING),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.STARTED, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CONFIGURING_WORKSPACE),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.INPUT_DATA_STAGING),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.INPUT_DATA_STAGING),
                new StateModel.StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.MONITORING),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.OUTPUT_DATA_STAGING),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.POST_PROCESSING),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.COMPLETED),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.REQUEUED),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.OUTPUT_DATA_STAGING),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.POST_PROCESSING),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.COMPLETED),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.POST_PROCESSING),
                new StateModel.StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.COMPLETED),
                new StateModel.StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.COMPLETED),
                new StateModel.StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.QUEUED, ProcessState.DEQUEUING),
                new StateModel.StateTransition<>(ProcessState.QUEUED, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.QUEUED, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.REQUEUED, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.REQUEUED, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.REQUEUED, ProcessState.FAILED),
                new StateModel.StateTransition<>(ProcessState.DEQUEUING, ProcessState.EXECUTING),
                new StateModel.StateTransition<>(ProcessState.DEQUEUING, ProcessState.QUEUED),
                new StateModel.StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELLING),
                new StateModel.StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.CANCELLING, ProcessState.CANCELED),
                new StateModel.StateTransition<>(ProcessState.CANCELLING, ProcessState.FAILED));

        @Override
        public Set<StateModel.StateTransition<ProcessState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates TaskState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum TaskStateValidator implements StateModel.StateValidator<TaskState> {
        INSTANCE;

        private static final Set<StateModel.StateTransition<TaskState>> VALID_TRANSITIONS = Set.of(
                new StateModel.StateTransition<>(TaskState.CREATED, TaskState.EXECUTING),
                new StateModel.StateTransition<>(TaskState.EXECUTING, TaskState.COMPLETED),
                new StateModel.StateTransition<>(TaskState.EXECUTING, TaskState.FAILED),
                new StateModel.StateTransition<>(TaskState.EXECUTING, TaskState.CANCELED));

        @Override
        public Set<StateModel.StateTransition<TaskState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }
}
