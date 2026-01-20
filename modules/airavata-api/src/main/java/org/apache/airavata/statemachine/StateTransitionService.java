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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Centralized service for validating and logging state transitions.
 * Makes all state changes traceable and enforces state machine rules.
 */
public class StateTransitionService {

    private static final Logger logger = LoggerFactory.getLogger(StateTransitionService.class);

    /**
     * Validates a state transition and logs it for traceability.
     *
     * @param validator the validator for the state type
     * @param from      the current state (null means initial state)
     * @param to        the target state (must not be null)
     * @param entityId  entity identifier for logging (e.g. processId, experimentId)
     * @param entityType type of entity for logging (e.g. "process", "experiment")
     * @return true if transition is valid, false otherwise
     */
    public static <S extends Enum<S>> boolean validateAndLog(
            StateValidator<S> validator, S from, S to, String entityId, String entityType) {

        if (to == null) {
            logger.warn("State transition rejected: {} {} -> null (null target state)", entityType, entityId);
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
                    "Invalid state transition rejected: {} {} {} -> {}",
                    entityType,
                    entityId,
                    from != null ? from.name() : "(initial)",
                    to.name());
        }
        return valid;
    }

    /**
     * Validates a state transition without logging. Useful for conditional checks.
     */
    public static <S extends Enum<S>> boolean isValid(StateValidator<S> validator, S from, S to) {
        return validator.isValid(from, to);
    }

    /**
     * Gets all reachable states from the current state.
     */
    public static <S extends Enum<S>> java.util.Set<S> getReachableStates(StateValidator<S> validator, S from) {
        return validator.getReachableStates(from);
    }
}
