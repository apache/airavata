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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.common.exception.CoreExceptions.AiravataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * State model types: keys, transition record, manager interface, transition service, and validator interface.
 */
public final class StateModel {

    private StateModel() {}

    /** Utility for generating standardized state store keys. Format: "{category}:{entity-type}:{identifier}" */
    public static final class StateKeys {

        private StateKeys() {}

        public static String cancelExperiment(String experimentId) {
            return "cancel:experiment:" + experimentId;
        }

        public static String workflowState(String workflowId) {
            return "workflow:state:" + workflowId;
        }

        public static String processState(String processId) {
            return "process:state:" + processId;
        }

        public static String experimentState(String experimentId) {
            return "experiment:state:" + experimentId;
        }

        public static String cancelProcess(String processId) {
            return "cancel:process:" + processId;
        }

        public static String workflowInstance(String workflowInstanceId) {
            return "workflow:instance:" + workflowInstanceId;
        }

        public static String taskState(String taskId) {
            return "task:state:" + taskId;
        }

        public static String processLock(String processId) {
            return "lock:process:" + processId;
        }
    }

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

    /** Public interface for workflow state operations. */
    public interface StateManager {

        <T> void saveState(String key, T value) throws AiravataException;

        <T> Optional<T> getState(String key, Class<T> type) throws AiravataException;

        void deleteState(String key) throws AiravataException;

        boolean exists(String key) throws AiravataException;

        boolean isAvailable();
    }

    /** Validates state transitions for a given state type. */
    public interface StateValidator<S extends Enum<S>> {

        Set<StateTransition<S>> getValidTransitions();

        default boolean isValid(S from, S to) {
            if (to == null) return false;
            if (from == null) return true;
            return getValidTransitions().contains(new StateTransition<>(from, to));
        }

        default Set<S> getReachableStates(S from) {
            if (from == null) {
                return getValidTransitions().stream().map(StateTransition::to).collect(Collectors.toSet());
            }
            return getValidTransitions().stream()
                    .filter(t -> t.from() == from)
                    .map(StateTransition::to)
                    .collect(Collectors.toSet());
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

        public static <S extends Enum<S>> boolean isValid(StateValidator<S> validator, S from, S to) {
            return validator.isValid(from, to);
        }
    }
}
