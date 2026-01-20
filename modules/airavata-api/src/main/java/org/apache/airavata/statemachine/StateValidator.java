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
import java.util.stream.Collectors;

/**
 * Validates state transitions for a given state type.
 * Provides a clean, declarative way to define allowed transitions.
 */
public interface StateValidator<S extends Enum<S>> {

    /**
     * Returns the set of valid transitions for this state machine.
     * This is the single source of truth for all allowed transitions.
     */
    Set<StateTransition<S>> getValidTransitions();

    /**
     * Checks if a transition from the given 'from' state to the 'to' state is valid.
     *
     * @param from the current state (null means initial state)
     * @param to   the target state (must not be null)
     * @return true if the transition is valid, false otherwise
     */
    default boolean isValid(S from, S to) {
        if (to == null) {
            return false;
        }
        // Initial state transition (from is null) is always valid if allowed in transitions
        if (from == null) {
            return true;
        }
        // Self-transitions are valid if explicitly defined
        if (from == to) {
            return getValidTransitions().contains(new StateTransition<>(from, to));
        }
        return getValidTransitions().contains(new StateTransition<>(from, to));
    }

    /**
     * Returns all states that can be reached from the given state.
     */
    default Set<S> getReachableStates(S from) {
        if (from == null) {
            return getValidTransitions().stream().map(StateTransition::to).collect(Collectors.toSet());
        }
        return getValidTransitions().stream()
                .filter(t -> t.from() == from)
                .map(StateTransition::to)
                .collect(Collectors.toSet());
    }

    /**
     * Returns all states that can transition to the given state.
     */
    default Set<S> getSourceStates(S to) {
        return getValidTransitions().stream()
                .filter(t -> t.to() == to)
                .map(StateTransition::from)
                .collect(Collectors.toSet());
    }
}
