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
package org.apache.airavata.core.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StatusModel} factory methods and basic behavior.
 */
class StatusModelTest {

    // ========== of(state) ==========

    @Test
    void of_setsState() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.EXECUTING);
        assertEquals(ProcessState.EXECUTING, status.getState());
    }

    @Test
    void of_setsPositiveTimestamp() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.CREATED);
        assertTrue(
                status.getTimeOfStateChange() > 0,
                "timeOfStateChange should be positive but was " + status.getTimeOfStateChange());
    }

    @Test
    void of_reasonIsNull() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.COMPLETED);
        assertNull(status.getReason());
    }

    @Test
    void of_statusIdIsNull() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.COMPLETED);
        assertNull(status.getStatusId());
    }

    // ========== of(state, reason) ==========

    @Test
    void ofWithReason_setsStateAndReason() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.FAILED, "Out of memory");
        assertEquals(ProcessState.FAILED, status.getState());
        assertEquals("Out of memory", status.getReason());
    }

    @Test
    void ofWithReason_setsPositiveTimestamp() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.FAILED, "timeout");
        assertTrue(status.getTimeOfStateChange() > 0);
    }

    @Test
    void ofWithReason_nullReason() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.CANCELED, null);
        assertEquals(ProcessState.CANCELED, status.getState());
        assertNull(status.getReason());
    }

    // ========== Generic type parameter ==========

    @Test
    void of_worksWithDifferentEnumTypes() {
        StatusModel<TaskState> taskStatus = StatusModel.of(TaskState.EXECUTING);
        assertEquals(TaskState.EXECUTING, taskStatus.getState());
        assertTrue(taskStatus.getTimeOfStateChange() > 0);
    }

    // ========== Constructor ==========

    @Test
    void constructor_setsStateAndTimestamp() {
        StatusModel<ProcessState> status = new StatusModel<>(ProcessState.LAUNCHED);
        assertEquals(ProcessState.LAUNCHED, status.getState());
        assertTrue(status.getTimeOfStateChange() > 0);
    }

    // ========== equals / hashCode ==========

    @Test
    void equals_sameValues_areEqual() {
        StatusModel<ProcessState> a = new StatusModel<>();
        a.setState(ProcessState.EXECUTING);
        a.setTimeOfStateChange(12345L);
        a.setReason("test");
        a.setStatusId("id-1");

        StatusModel<ProcessState> b = new StatusModel<>();
        b.setState(ProcessState.EXECUTING);
        b.setTimeOfStateChange(12345L);
        b.setReason("test");
        b.setStatusId("id-1");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentState_notEqual() {
        StatusModel<ProcessState> a = StatusModel.of(ProcessState.EXECUTING);
        StatusModel<ProcessState> b = StatusModel.of(ProcessState.COMPLETED);
        assertNotEquals(a, b);
    }

    // ========== toString ==========

    @Test
    void toString_containsState() {
        StatusModel<ProcessState> status = StatusModel.of(ProcessState.MONITORING);
        assertTrue(status.toString().contains("MONITORING"));
    }
}
