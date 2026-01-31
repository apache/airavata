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
package org.apache.airavata.service.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.common.model.JobState;
import org.apache.airavata.orchestrator.state.StateValidators;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JobStateValidator that don't require Spring context or database.
 * These tests verify the state machine validation logic works correctly.
 */
public class JobStateValidatorTest {

    @Test
    public void testJobStateValidator_AllValidTransitions() {
        // Test all valid JobState transitions according to JobStateValidator
        // SUBMITTED can transition to all other states
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.QUEUED),
                "SUBMITTED -> QUEUED should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.ACTIVE),
                "SUBMITTED -> ACTIVE should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.COMPLETE),
                "SUBMITTED -> COMPLETE should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.FAILED),
                "SUBMITTED -> FAILED should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, JobState.CANCELED),
                "SUBMITTED -> CANCELED should be valid");

        // QUEUED can transition to multiple states
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.ACTIVE),
                "QUEUED -> ACTIVE should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.COMPLETE),
                "QUEUED -> COMPLETE should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.QUEUED, JobState.FAILED),
                "QUEUED -> FAILED should be valid");

        // ACTIVE can transition to terminal states
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.COMPLETE),
                "ACTIVE -> COMPLETE should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.FAILED),
                "ACTIVE -> FAILED should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.ACTIVE, JobState.CANCELED),
                "ACTIVE -> CANCELED should be valid");

        // NON_CRITICAL_FAIL can recover
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.NON_CRITICAL_FAIL, JobState.QUEUED),
                "NON_CRITICAL_FAIL -> QUEUED should be valid");
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.NON_CRITICAL_FAIL, JobState.ACTIVE),
                "NON_CRITICAL_FAIL -> ACTIVE should be valid");
    }

    @Test
    public void testJobStateValidator_InvalidTransitions() {
        // Test invalid JobState transitions
        // COMPLETE cannot transition to other states
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.COMPLETE, JobState.SUBMITTED),
                "COMPLETE -> SUBMITTED should be invalid");
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.COMPLETE, JobState.ACTIVE),
                "COMPLETE -> ACTIVE should be invalid");

        // FAILED cannot transition to SUBMITTED
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.FAILED, JobState.SUBMITTED),
                "FAILED -> SUBMITTED should be invalid");

        // CANCELED cannot transition to active states
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.CANCELED, JobState.ACTIVE),
                "CANCELED -> ACTIVE should be invalid");
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.CANCELED, JobState.QUEUED),
                "CANCELED -> QUEUED should be invalid");
    }

    @Test
    public void testJobStateValidator_NullHandling() {
        // Test that JobStateValidator handles null states correctly
        // null -> any state should be valid (initial state)
        assertTrue(
                StateValidators.JobStateValidator.INSTANCE.isValid(null, JobState.SUBMITTED),
                "null -> SUBMITTED should be valid (initial state)");

        // any state -> null should be invalid
        assertFalse(
                StateValidators.JobStateValidator.INSTANCE.isValid(JobState.SUBMITTED, null), "SUBMITTED -> null should be invalid");

        // null -> null should be invalid
        assertFalse(StateValidators.JobStateValidator.INSTANCE.isValid(null, null), "null -> null should be invalid");
    }
}
