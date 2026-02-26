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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.research.experiment.model.ExperimentState;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ExperimentStateValidator that don't require Spring context or database.
 * These tests verify the state machine validation logic works correctly.
 */
public class ExperimentStateValidatorTest {

    @Test
    public void testExperimentStateValidator_CreatedTransitions() {
        // CREATED can go to VALIDATED, SCHEDULED, LAUNCHED, FAILED
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CREATED, ExperimentState.VALIDATED),
                "CREATED -> VALIDATED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CREATED, ExperimentState.SCHEDULED),
                "CREATED -> SCHEDULED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CREATED, ExperimentState.LAUNCHED),
                "CREATED -> LAUNCHED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CREATED, ExperimentState.FAILED),
                "CREATED -> FAILED should be valid");
    }

    @Test
    public void testExperimentStateValidator_ValidatedTransitions() {
        // VALIDATED can go to LAUNCHED, FAILED
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.VALIDATED, ExperimentState.LAUNCHED),
                "VALIDATED -> LAUNCHED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.VALIDATED, ExperimentState.FAILED),
                "VALIDATED -> FAILED should be valid");
    }

    @Test
    public void testExperimentStateValidator_ScheduledTransitions() {
        // SCHEDULED can go to LAUNCHED, SCHEDULED (self), CANCELING
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.SCHEDULED, ExperimentState.LAUNCHED),
                "SCHEDULED -> LAUNCHED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.SCHEDULED, ExperimentState.SCHEDULED),
                "SCHEDULED -> SCHEDULED (self) should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.SCHEDULED, ExperimentState.CANCELING),
                "SCHEDULED -> CANCELING should be valid");
    }

    @Test
    public void testExperimentStateValidator_LaunchedTransitions() {
        // LAUNCHED can go to EXECUTING, FAILED, CANCELING
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.LAUNCHED, ExperimentState.EXECUTING),
                "LAUNCHED -> EXECUTING should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.LAUNCHED, ExperimentState.FAILED),
                "LAUNCHED -> FAILED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.LAUNCHED, ExperimentState.CANCELING),
                "LAUNCHED -> CANCELING should be valid");
    }

    @Test
    public void testExperimentStateValidator_ExecutingTransitions() {
        // EXECUTING can go to COMPLETED, FAILED, CANCELED, SCHEDULED, CANCELING
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.EXECUTING, ExperimentState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.EXECUTING, ExperimentState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.EXECUTING, ExperimentState.CANCELED),
                "EXECUTING -> CANCELED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.EXECUTING, ExperimentState.SCHEDULED),
                "EXECUTING -> SCHEDULED should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.EXECUTING, ExperimentState.CANCELING),
                "EXECUTING -> CANCELING should be valid");
    }

    @Test
    public void testExperimentStateValidator_CancelingTransitions() {
        // CANCELING can go to CANCELING (self), CANCELED
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CANCELING, ExperimentState.CANCELING),
                "CANCELING -> CANCELING (self) should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CANCELING, ExperimentState.CANCELED),
                "CANCELING -> CANCELED should be valid");
    }

    @Test
    public void testExperimentStateValidator_TerminalIdempotency() {
        // Terminal states allow idempotent self-transitions
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.FAILED, ExperimentState.FAILED),
                "FAILED -> FAILED (idempotent) should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.COMPLETED, ExperimentState.COMPLETED),
                "COMPLETED -> COMPLETED (idempotent) should be valid");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CANCELED, ExperimentState.CANCELED),
                "CANCELED -> CANCELED (idempotent) should be valid");
    }

    @Test
    public void testExperimentStateValidator_InvalidTransitions() {
        // Terminal states cannot transition to active states
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.COMPLETED, ExperimentState.LAUNCHED),
                "COMPLETED -> LAUNCHED should be invalid");
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.FAILED, ExperimentState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.COMPLETED, ExperimentState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.CANCELED, ExperimentState.LAUNCHED),
                "CANCELED -> LAUNCHED should be invalid");
    }

    @Test
    public void testExperimentStateValidator_LaunchedToCompletedIsInvalid() {
        // Critical constraint: LAUNCHED -> COMPLETED is NOT valid; must go through EXECUTING first
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(
                        ExperimentState.LAUNCHED, ExperimentState.COMPLETED),
                "LAUNCHED -> COMPLETED should be invalid (must go through EXECUTING first)");
    }

    @Test
    public void testExperimentStateValidator_NullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(null, ExperimentState.CREATED),
                "null -> CREATED should be valid (initial state)");
        assertTrue(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(null, ExperimentState.LAUNCHED),
                "null -> LAUNCHED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(ExperimentState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(
                StateValidators.ExperimentStateValidator.INSTANCE.isValid(null, null),
                "null -> null should be invalid");
    }
}
