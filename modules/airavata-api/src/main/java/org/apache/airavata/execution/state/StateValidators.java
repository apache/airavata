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
package org.apache.airavata.execution.state;

import java.util.Objects;
import java.util.Set;
import org.apache.airavata.research.experiment.model.ExperimentState;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * State model types and concrete validator enums for experiment, job, process, and task.
 */
public final class StateValidators {

    private StateValidators() {}

    // -------------------------------------------------------------------------
    // State model types
    // -------------------------------------------------------------------------

    /** Represents a valid state transition from one state to another. */
    public record StateTransition<S extends Enum<S>>(S from, S to) {

        public StateTransition {
            Objects.requireNonNull(from, "from state cannot be null");
            Objects.requireNonNull(to, "to state cannot be null");
        }

        @Override
        public String toString() {
            return from.name() + " -> " + to.name();
        }
    }

    /** Validates state transitions for a given state type. */
    public interface StateValidator<S extends Enum<S>> {

        Set<StateTransition<S>> getValidTransitions();

        default boolean isValid(S from, S to) {
            if (to == null) return false;
            if (from == null) return true;
            return getValidTransitions().contains(new StateTransition<>(from, to));
        }
    }

    /** Centralized service for validating and logging state transitions. */
    public static class StateTransitionService {

        private static final Logger logger = LoggerFactory.getLogger(StateTransitionService.class);

        public static <S extends Enum<S>> boolean validateAndLog(
                StateValidator<S> validator, S from, S to, String entityId, String entityType) {

            if (to == null) {
                logger.warn("State transition rejected: {} {} -> null", entityType, entityId);
                return false;
            }

            boolean valid = validator.isValid(from, to);
            if (valid) {
                logger.info(
                        "State transition: {} {} {} -> {}",
                        entityType,
                        entityId,
                        from != null ? from.name() : "(initial)",
                        to.name());
                MDC.put(entityType + "_state", to.name());
            } else {
                logger.warn(
                        "Invalid state transition: {} {} {} -> {}",
                        entityType,
                        entityId,
                        from != null ? from.name() : "(initial)",
                        to.name());
            }
            return valid;
        }
    }

    // -------------------------------------------------------------------------
    // Concrete validators
    // -------------------------------------------------------------------------

    /** Validates ExperimentState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum ExperimentStateValidator implements StateValidator<ExperimentState> {
        INSTANCE;

        private static final Set<StateTransition<ExperimentState>> VALID_TRANSITIONS = Set.of(
                new StateTransition<>(ExperimentState.CREATED, ExperimentState.VALIDATED),
                new StateTransition<>(ExperimentState.CREATED, ExperimentState.SCHEDULED),
                new StateTransition<>(ExperimentState.CREATED, ExperimentState.LAUNCHED),
                new StateTransition<>(ExperimentState.CREATED, ExperimentState.FAILED),
                new StateTransition<>(ExperimentState.VALIDATED, ExperimentState.LAUNCHED),
                new StateTransition<>(ExperimentState.VALIDATED, ExperimentState.FAILED),
                new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.LAUNCHED),
                new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.SCHEDULED),
                new StateTransition<>(ExperimentState.SCHEDULED, ExperimentState.CANCELING),
                new StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                new StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.FAILED),
                new StateTransition<>(ExperimentState.LAUNCHED, ExperimentState.CANCELING),
                new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.COMPLETED),
                new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.FAILED),
                new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELED),
                new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.SCHEDULED),
                new StateTransition<>(ExperimentState.EXECUTING, ExperimentState.CANCELING),
                new StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELING),
                new StateTransition<>(ExperimentState.CANCELING, ExperimentState.CANCELED),
                /* Idempotent terminal state updates (e.g. workflow and launcher both set FAILED) */
                new StateTransition<>(ExperimentState.FAILED, ExperimentState.FAILED),
                new StateTransition<>(ExperimentState.COMPLETED, ExperimentState.COMPLETED),
                new StateTransition<>(ExperimentState.CANCELED, ExperimentState.CANCELED));

        @Override
        public Set<StateTransition<ExperimentState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates JobState transitions. Terminal states: COMPLETED, CANCELED, FAILED, SUSPENDED, UNKNOWN */
    public enum JobStateValidator implements StateValidator<JobState> {
        INSTANCE;

        private static final Set<StateTransition<JobState>> VALID_TRANSITIONS = Set.of(
                new StateTransition<>(JobState.SUBMITTED, JobState.QUEUED),
                new StateTransition<>(JobState.SUBMITTED, JobState.ACTIVE),
                new StateTransition<>(JobState.SUBMITTED, JobState.COMPLETED),
                new StateTransition<>(JobState.SUBMITTED, JobState.CANCELED),
                new StateTransition<>(JobState.SUBMITTED, JobState.FAILED),
                new StateTransition<>(JobState.SUBMITTED, JobState.SUSPENDED),
                new StateTransition<>(JobState.SUBMITTED, JobState.UNKNOWN),
                new StateTransition<>(JobState.SUBMITTED, JobState.NON_CRITICAL_FAIL),
                new StateTransition<>(JobState.QUEUED, JobState.ACTIVE),
                new StateTransition<>(JobState.QUEUED, JobState.COMPLETED),
                new StateTransition<>(JobState.QUEUED, JobState.CANCELED),
                new StateTransition<>(JobState.QUEUED, JobState.FAILED),
                new StateTransition<>(JobState.QUEUED, JobState.SUSPENDED),
                new StateTransition<>(JobState.QUEUED, JobState.UNKNOWN),
                new StateTransition<>(JobState.QUEUED, JobState.NON_CRITICAL_FAIL),
                new StateTransition<>(JobState.ACTIVE, JobState.COMPLETED),
                new StateTransition<>(JobState.ACTIVE, JobState.CANCELED),
                new StateTransition<>(JobState.ACTIVE, JobState.FAILED),
                new StateTransition<>(JobState.ACTIVE, JobState.SUSPENDED),
                new StateTransition<>(JobState.ACTIVE, JobState.UNKNOWN),
                new StateTransition<>(JobState.ACTIVE, JobState.NON_CRITICAL_FAIL),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.COMPLETED),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.CANCELED),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.FAILED),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.SUSPENDED),
                new StateTransition<>(JobState.NON_CRITICAL_FAIL, JobState.UNKNOWN));

        @Override
        public Set<StateTransition<JobState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates ProcessState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum ProcessStateValidator implements StateValidator<ProcessState> {
        INSTANCE;

        private static final Set<StateTransition<ProcessState>> VALID_TRANSITIONS = Set.of(
                new StateTransition<>(ProcessState.CREATED, ProcessState.VALIDATED),
                new StateTransition<>(ProcessState.CREATED, ProcessState.LAUNCHED),
                new StateTransition<>(ProcessState.CREATED, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.CREATED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.VALIDATED, ProcessState.LAUNCHED),
                new StateTransition<>(ProcessState.VALIDATED, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.VALIDATED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.PRE_PROCESSING),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.CONFIGURING_WORKSPACE),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.INPUT_DATA_STAGING),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.LAUNCHED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CONFIGURING_WORKSPACE),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.INPUT_DATA_STAGING),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.PRE_PROCESSING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.INPUT_DATA_STAGING),
                new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.CONFIGURING_WORKSPACE, ProcessState.FAILED),
                new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.INPUT_DATA_STAGING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.MONITORING),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.OUTPUT_DATA_STAGING),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.POST_PROCESSING),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.COMPLETED),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.REQUEUED),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.EXECUTING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.OUTPUT_DATA_STAGING),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.POST_PROCESSING),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.COMPLETED),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.MONITORING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.POST_PROCESSING),
                new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.COMPLETED),
                new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.OUTPUT_DATA_STAGING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.COMPLETED),
                new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.FAILED),
                new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.POST_PROCESSING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.QUEUED, ProcessState.DEQUEUING),
                new StateTransition<>(ProcessState.QUEUED, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.QUEUED, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.QUEUED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.REQUEUED, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.REQUEUED, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.REQUEUED, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.REQUEUED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.DEQUEUING, ProcessState.EXECUTING),
                new StateTransition<>(ProcessState.DEQUEUING, ProcessState.QUEUED),
                new StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELING),
                new StateTransition<>(ProcessState.DEQUEUING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.CANCELING, ProcessState.CANCELED),
                new StateTransition<>(ProcessState.CANCELING, ProcessState.FAILED),
                /* Idempotent terminal state updates */
                new StateTransition<>(ProcessState.COMPLETED, ProcessState.COMPLETED),
                new StateTransition<>(ProcessState.FAILED, ProcessState.FAILED),
                new StateTransition<>(ProcessState.CANCELED, ProcessState.CANCELED));

        @Override
        public Set<StateTransition<ProcessState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates TaskState transitions. Terminal states: COMPLETED, FAILED, CANCELED */
    public enum TaskStateValidator implements StateValidator<TaskState> {
        INSTANCE;

        private static final Set<StateTransition<TaskState>> VALID_TRANSITIONS = Set.of(
                new StateTransition<>(TaskState.CREATED, TaskState.EXECUTING),
                new StateTransition<>(TaskState.EXECUTING, TaskState.COMPLETED),
                new StateTransition<>(TaskState.EXECUTING, TaskState.FAILED),
                new StateTransition<>(TaskState.EXECUTING, TaskState.CANCELED));

        @Override
        public Set<StateTransition<TaskState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }
}
