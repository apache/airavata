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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.airavata.compute.resource.model.JobState;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.core.model.TaskState;
import org.apache.airavata.research.experiment.model.ExperimentState;
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
    // Transition builder helpers
    // -------------------------------------------------------------------------

    /** Expands {@code from(X).to(A, B, C)} into individual StateTransitions. */
    @SafeVarargs
    static <S extends Enum<S>> Set<StateTransition<S>> from(S source, S... targets) {
        var set = new HashSet<StateTransition<S>>();
        for (S target : targets) {
            set.add(new StateTransition<>(source, target));
        }
        return set;
    }

    /** Merges multiple transition sets into one immutable set. */
    @SafeVarargs
    static <S extends Enum<S>> Set<StateTransition<S>> transitions(Set<StateTransition<S>>... groups) {
        var merged = new HashSet<StateTransition<S>>();
        for (var group : groups) {
            merged.addAll(group);
        }
        return Set.copyOf(merged);
    }

    // -------------------------------------------------------------------------
    // Concrete validators
    // -------------------------------------------------------------------------

    /** Validates ExperimentState transitions. */
    public enum ExperimentStateValidator implements StateValidator<ExperimentState> {
        INSTANCE;

        private static final Set<StateTransition<ExperimentState>> VALID_TRANSITIONS;

        static {
            VALID_TRANSITIONS = transitions(
                    from(
                            ExperimentState.CREATED,
                            ExperimentState.VALIDATED,
                            ExperimentState.SCHEDULED,
                            ExperimentState.LAUNCHED,
                            ExperimentState.FAILED),
                    from(ExperimentState.VALIDATED, ExperimentState.LAUNCHED, ExperimentState.FAILED),
                    from(
                            ExperimentState.SCHEDULED,
                            ExperimentState.LAUNCHED,
                            ExperimentState.SCHEDULED,
                            ExperimentState.CANCELING),
                    from(
                            ExperimentState.LAUNCHED,
                            ExperimentState.EXECUTING,
                            ExperimentState.FAILED,
                            ExperimentState.CANCELING),
                    from(
                            ExperimentState.EXECUTING,
                            ExperimentState.COMPLETED,
                            ExperimentState.FAILED,
                            ExperimentState.CANCELED,
                            ExperimentState.SCHEDULED,
                            ExperimentState.CANCELING),
                    from(ExperimentState.CANCELING, ExperimentState.CANCELING, ExperimentState.CANCELED),
                    // Idempotent terminal self-transitions
                    from(ExperimentState.FAILED, ExperimentState.FAILED),
                    from(ExperimentState.COMPLETED, ExperimentState.COMPLETED),
                    from(ExperimentState.CANCELED, ExperimentState.CANCELED));
        }

        @Override
        public Set<StateTransition<ExperimentState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates JobState transitions. */
    public enum JobStateValidator implements StateValidator<JobState> {
        INSTANCE;

        private static final Set<StateTransition<JobState>> VALID_TRANSITIONS;

        static {
            VALID_TRANSITIONS = transitions(
                    from(
                            JobState.SUBMITTED,
                            JobState.QUEUED,
                            JobState.ACTIVE,
                            JobState.COMPLETED,
                            JobState.CANCELED,
                            JobState.FAILED,
                            JobState.SUSPENDED,
                            JobState.UNKNOWN,
                            JobState.NON_CRITICAL_FAIL),
                    from(
                            JobState.QUEUED,
                            JobState.ACTIVE,
                            JobState.COMPLETED,
                            JobState.CANCELED,
                            JobState.FAILED,
                            JobState.SUSPENDED,
                            JobState.UNKNOWN,
                            JobState.NON_CRITICAL_FAIL),
                    from(
                            JobState.ACTIVE,
                            JobState.COMPLETED,
                            JobState.CANCELED,
                            JobState.FAILED,
                            JobState.SUSPENDED,
                            JobState.UNKNOWN,
                            JobState.NON_CRITICAL_FAIL),
                    from(
                            JobState.NON_CRITICAL_FAIL,
                            JobState.QUEUED,
                            JobState.ACTIVE,
                            JobState.COMPLETED,
                            JobState.CANCELED,
                            JobState.FAILED,
                            JobState.SUSPENDED,
                            JobState.UNKNOWN));
        }

        @Override
        public Set<StateTransition<JobState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates ProcessState transitions. */
    public enum ProcessStateValidator implements StateValidator<ProcessState> {
        INSTANCE;

        private static final Set<StateTransition<ProcessState>> VALID_TRANSITIONS;

        static {
            VALID_TRANSITIONS = transitions(
                    from(
                            ProcessState.CREATED,
                            ProcessState.VALIDATED,
                            ProcessState.LAUNCHED,
                            ProcessState.CANCELING,
                            ProcessState.FAILED),
                    from(ProcessState.VALIDATED, ProcessState.LAUNCHED, ProcessState.CANCELING, ProcessState.FAILED),
                    from(
                            ProcessState.LAUNCHED,
                            ProcessState.PRE_PROCESSING,
                            ProcessState.CONFIGURING_WORKSPACE,
                            ProcessState.INPUT_DATA_STAGING,
                            ProcessState.EXECUTING,
                            ProcessState.QUEUED,
                            ProcessState.CANCELING,
                            ProcessState.FAILED),
                    from(
                            ProcessState.PRE_PROCESSING,
                            ProcessState.CONFIGURING_WORKSPACE,
                            ProcessState.INPUT_DATA_STAGING,
                            ProcessState.EXECUTING,
                            ProcessState.QUEUED,
                            ProcessState.CANCELING,
                            ProcessState.FAILED),
                    from(
                            ProcessState.CONFIGURING_WORKSPACE,
                            ProcessState.INPUT_DATA_STAGING,
                            ProcessState.EXECUTING,
                            ProcessState.QUEUED,
                            ProcessState.CANCELING,
                            ProcessState.FAILED),
                    from(
                            ProcessState.INPUT_DATA_STAGING,
                            ProcessState.EXECUTING,
                            ProcessState.QUEUED,
                            ProcessState.CANCELING,
                            ProcessState.FAILED),
                    from(
                            ProcessState.EXECUTING,
                            ProcessState.MONITORING,
                            ProcessState.OUTPUT_DATA_STAGING,
                            ProcessState.POST_PROCESSING,
                            ProcessState.COMPLETED,
                            ProcessState.FAILED,
                            ProcessState.QUEUED,
                            ProcessState.REQUEUED,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED),
                    from(
                            ProcessState.MONITORING,
                            ProcessState.OUTPUT_DATA_STAGING,
                            ProcessState.POST_PROCESSING,
                            ProcessState.COMPLETED,
                            ProcessState.FAILED,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED),
                    from(
                            ProcessState.OUTPUT_DATA_STAGING,
                            ProcessState.POST_PROCESSING,
                            ProcessState.COMPLETED,
                            ProcessState.FAILED,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED),
                    from(
                            ProcessState.POST_PROCESSING,
                            ProcessState.COMPLETED,
                            ProcessState.FAILED,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED),
                    from(
                            ProcessState.QUEUED,
                            ProcessState.DEQUEUING,
                            ProcessState.EXECUTING,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED,
                            ProcessState.FAILED),
                    from(
                            ProcessState.REQUEUED,
                            ProcessState.QUEUED,
                            ProcessState.EXECUTING,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED,
                            ProcessState.FAILED),
                    from(
                            ProcessState.DEQUEUING,
                            ProcessState.EXECUTING,
                            ProcessState.QUEUED,
                            ProcessState.CANCELING,
                            ProcessState.CANCELED),
                    from(ProcessState.CANCELING, ProcessState.CANCELED, ProcessState.FAILED),
                    // Idempotent terminal self-transitions
                    from(ProcessState.COMPLETED, ProcessState.COMPLETED),
                    from(ProcessState.FAILED, ProcessState.FAILED),
                    from(ProcessState.CANCELED, ProcessState.CANCELED));
        }

        @Override
        public Set<StateTransition<ProcessState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }

    /** Validates TaskState transitions. */
    public enum TaskStateValidator implements StateValidator<TaskState> {
        INSTANCE;

        private static final Set<StateTransition<TaskState>> VALID_TRANSITIONS = transitions(
                from(TaskState.CREATED, TaskState.EXECUTING),
                from(TaskState.EXECUTING, TaskState.COMPLETED, TaskState.FAILED, TaskState.CANCELED));

        @Override
        public Set<StateTransition<TaskState>> getValidTransitions() {
            return VALID_TRANSITIONS;
        }
    }
}
