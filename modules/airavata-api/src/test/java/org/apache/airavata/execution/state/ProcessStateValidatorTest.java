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

import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.execution.state.StateValidators;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProcessStateValidator that don't require Spring context or database.
 * These tests verify the state machine validation logic works correctly.
 */
public class ProcessStateValidatorTest {

    @Test
    public void testProcessStateValidator_CreatedTransitions() {
        // CREATED can go to VALIDATED, LAUNCHED, FAILED (also CANCELING per the validator)
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CREATED, ProcessState.VALIDATED),
                "CREATED -> VALIDATED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CREATED, ProcessState.LAUNCHED),
                "CREATED -> LAUNCHED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CREATED, ProcessState.FAILED),
                "CREATED -> FAILED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CREATED, ProcessState.CANCELING),
                "CREATED -> CANCELING should be valid");
    }

    @Test
    public void testProcessStateValidator_LaunchedTransitions() {
        // LAUNCHED can go to PRE_PROCESSING, INPUT_DATA_STAGING, EXECUTING, QUEUED, FAILED (and more)
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.PRE_PROCESSING),
                "LAUNCHED -> PRE_PROCESSING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.CONFIGURING_WORKSPACE),
                "LAUNCHED -> CONFIGURING_WORKSPACE should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.INPUT_DATA_STAGING),
                "LAUNCHED -> INPUT_DATA_STAGING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.EXECUTING),
                "LAUNCHED -> EXECUTING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.QUEUED),
                "LAUNCHED -> QUEUED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.CANCELING),
                "LAUNCHED -> CANCELING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.LAUNCHED, ProcessState.FAILED),
                "LAUNCHED -> FAILED should be valid");
    }

    @Test
    public void testProcessStateValidator_ExecutingTransitions() {
        // EXECUTING can go to MONITORING, OUTPUT_DATA_STAGING, COMPLETED, FAILED, REQUEUED (and more)
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.MONITORING),
                "EXECUTING -> MONITORING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.OUTPUT_DATA_STAGING),
                "EXECUTING -> OUTPUT_DATA_STAGING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.POST_PROCESSING),
                "EXECUTING -> POST_PROCESSING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.QUEUED),
                "EXECUTING -> QUEUED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.REQUEUED),
                "EXECUTING -> REQUEUED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.CANCELING),
                "EXECUTING -> CANCELING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.EXECUTING, ProcessState.CANCELED),
                "EXECUTING -> CANCELED should be valid");
    }

    @Test
    public void testProcessStateValidator_QueuingCycleTransitions() {
        // QUEUED -> DEQUEUING -> EXECUTING cycle
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.QUEUED, ProcessState.DEQUEUING),
                "QUEUED -> DEQUEUING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.QUEUED, ProcessState.EXECUTING),
                "QUEUED -> EXECUTING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.DEQUEUING, ProcessState.EXECUTING),
                "DEQUEUING -> EXECUTING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.DEQUEUING, ProcessState.QUEUED),
                "DEQUEUING -> QUEUED should be valid");

        // REQUEUED can return to QUEUED or continue to EXECUTING
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.REQUEUED, ProcessState.QUEUED),
                "REQUEUED -> QUEUED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.REQUEUED, ProcessState.EXECUTING),
                "REQUEUED -> EXECUTING should be valid");
    }

    @Test
    public void testProcessStateValidator_CancelingTransitions() {
        // CANCELING can go to CANCELED, FAILED
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CANCELING, ProcessState.CANCELED),
                "CANCELING -> CANCELED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CANCELING, ProcessState.FAILED),
                "CANCELING -> FAILED should be valid");
    }

    @Test
    public void testProcessStateValidator_PostExecutionTransitions() {
        // MONITORING -> OUTPUT_DATA_STAGING -> POST_PROCESSING -> COMPLETED path
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.MONITORING, ProcessState.OUTPUT_DATA_STAGING),
                "MONITORING -> OUTPUT_DATA_STAGING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.MONITORING, ProcessState.POST_PROCESSING),
                "MONITORING -> POST_PROCESSING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.MONITORING, ProcessState.COMPLETED),
                "MONITORING -> COMPLETED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.OUTPUT_DATA_STAGING, ProcessState.POST_PROCESSING),
                "OUTPUT_DATA_STAGING -> POST_PROCESSING should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.OUTPUT_DATA_STAGING, ProcessState.COMPLETED),
                "OUTPUT_DATA_STAGING -> COMPLETED should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.POST_PROCESSING, ProcessState.COMPLETED),
                "POST_PROCESSING -> COMPLETED should be valid");
    }

    @Test
    public void testProcessStateValidator_TerminalIdempotency() {
        // Terminal states allow idempotent self-transitions
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.COMPLETED, ProcessState.COMPLETED),
                "COMPLETED -> COMPLETED (idempotent) should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.FAILED, ProcessState.FAILED),
                "FAILED -> FAILED (idempotent) should be valid");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CANCELED, ProcessState.CANCELED),
                "CANCELED -> CANCELED (idempotent) should be valid");
    }

    @Test
    public void testProcessStateValidator_InvalidTransitions() {
        // Terminal states cannot transition to active states
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.COMPLETED, ProcessState.LAUNCHED),
                "COMPLETED -> LAUNCHED should be invalid");
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.FAILED, ProcessState.EXECUTING),
                "FAILED -> EXECUTING should be invalid");
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.COMPLETED, ProcessState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");

        // CREATED cannot skip directly to EXECUTING
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(
                        ProcessState.CREATED, ProcessState.EXECUTING),
                "CREATED -> EXECUTING should be invalid");
    }

    @Test
    public void testProcessStateValidator_NullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(null, ProcessState.CREATED),
                "null -> CREATED should be valid (initial state)");
        assertTrue(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(null, ProcessState.LAUNCHED),
                "null -> LAUNCHED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(ProcessState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(
                StateValidators.ProcessStateValidator.INSTANCE.isValid(null, null),
                "null -> null should be invalid");
    }
}
