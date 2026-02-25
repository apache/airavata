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

import org.apache.airavata.execution.model.TaskState;
import org.apache.airavata.execution.state.StateValidators;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for TaskStateValidator that don't require Spring context or database.
 * These tests verify the state machine validation logic works correctly.
 */
public class TaskStateValidatorTest {

    @Test
    public void testTaskStateValidator_AllValidTransitions() {
        // CREATED -> EXECUTING is the only valid forward transition from CREATED
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.EXECUTING),
                "CREATED -> EXECUTING should be valid");

        // EXECUTING can terminate as COMPLETED, FAILED, or CANCELED
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.COMPLETED),
                "EXECUTING -> COMPLETED should be valid");
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.FAILED),
                "EXECUTING -> FAILED should be valid");
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.EXECUTING, TaskState.CANCELED),
                "EXECUTING -> CANCELED should be valid");
    }

    @Test
    public void testTaskStateValidator_InvalidTransitions() {
        // Terminal states cannot transition to active states
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.COMPLETED, TaskState.EXECUTING),
                "COMPLETED -> EXECUTING should be invalid");
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.FAILED, TaskState.CREATED),
                "FAILED -> CREATED should be invalid");

        // CREATED cannot skip directly to a terminal state
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.COMPLETED),
                "CREATED -> COMPLETED (skip EXECUTING) should be invalid");
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.FAILED),
                "CREATED -> FAILED (skip EXECUTING) should be invalid");
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, TaskState.CANCELED),
                "CREATED -> CANCELED (skip EXECUTING) should be invalid");
    }

    @Test
    public void testTaskStateValidator_NullHandling() {
        // null -> any state should be valid (initial state)
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(null, TaskState.CREATED),
                "null -> CREATED should be valid (initial state)");
        assertTrue(
                StateValidators.TaskStateValidator.INSTANCE.isValid(null, TaskState.EXECUTING),
                "null -> EXECUTING should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(TaskState.CREATED, null),
                "CREATED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(
                StateValidators.TaskStateValidator.INSTANCE.isValid(null, null),
                "null -> null should be invalid");
    }
}
