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
import java.util.stream.Collectors;

/**
 * Validates state transitions for a given state type.
 */
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
