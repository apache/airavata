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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Centralized service for validating and logging state transitions.
 */
public class StateTransitionService {

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
