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
package org.apache.airavata.execution.dag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.execution.model.TaskTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pure unit tests for the {@link TaskTypes} enum.
 * No Spring context or external dependencies required.
 */
public class TaskTypesTest {

    // ===========================================================================
    // Value count
    // ===========================================================================

    @Test
    public void taskTypes_hasExactlySixValues() {
        assertEquals(6, TaskTypes.values().length,
                "TaskTypes must declare exactly 6 constants");
    }

    // ===========================================================================
    // Declared values — individual presence checks
    // ===========================================================================

    @Test
    public void taskTypes_containsProvisioning() {
        assertNotNull(TaskTypes.PROVISIONING,
                "PROVISIONING constant must be present (renamed from ENV_SETUP)");
    }

    @Test
    public void taskTypes_containsDataStaging() {
        assertNotNull(TaskTypes.DATA_STAGING, "DATA_STAGING constant must be present");
    }

    @Test
    public void taskTypes_containsJobSubmission() {
        assertNotNull(TaskTypes.JOB_SUBMISSION, "JOB_SUBMISSION constant must be present");
    }

    @Test
    public void taskTypes_containsDeprovisioning() {
        assertNotNull(TaskTypes.DEPROVISIONING,
                "DEPROVISIONING constant must be present (renamed from ENV_CLEANUP)");
    }

    @Test
    public void taskTypes_containsMonitoring() {
        assertNotNull(TaskTypes.MONITORING, "MONITORING constant must be present");
    }

    @Test
    public void taskTypes_containsOutputFetching() {
        assertNotNull(TaskTypes.OUTPUT_FETCHING, "OUTPUT_FETCHING constant must be present");
    }

    // ===========================================================================
    // valueOf round-trip for every active constant
    // ===========================================================================

    @ParameterizedTest(name = "TaskTypes.valueOf(\"{0}\") resolves correctly")
    @ValueSource(strings = {
        "PROVISIONING",
        "DATA_STAGING",
        "JOB_SUBMISSION",
        "DEPROVISIONING",
        "MONITORING",
        "OUTPUT_FETCHING"
    })
    public void taskTypes_valueOf_resolvesEachActiveConstant(String name) {
        TaskTypes resolved = TaskTypes.valueOf(name);
        assertNotNull(resolved, "valueOf must not return null for a valid constant name");
        assertEquals(name, resolved.name(),
                "resolved constant name must match the input string");
    }

    @Test
    public void taskTypes_valueOf_provisioning_returnsSameInstance() {
        assertSame(TaskTypes.PROVISIONING, TaskTypes.valueOf("PROVISIONING"),
                "valueOf must return the canonical enum singleton");
    }

    @Test
    public void taskTypes_valueOf_deprovisioning_returnsSameInstance() {
        assertSame(TaskTypes.DEPROVISIONING, TaskTypes.valueOf("DEPROVISIONING"),
                "valueOf must return the canonical enum singleton");
    }

    // ===========================================================================
    // Removed constants throw IllegalArgumentException
    // ===========================================================================

    @Test
    public void taskTypes_valueOf_envSetup_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskTypes.valueOf("ENV_SETUP"),
                "ENV_SETUP was removed and must no longer be resolvable");
    }

    @Test
    public void taskTypes_valueOf_envCleanup_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskTypes.valueOf("ENV_CLEANUP"),
                "ENV_CLEANUP was removed and must no longer be resolvable");
    }

    // ===========================================================================
    // name() returns the exact constant identifier
    // ===========================================================================

    @Test
    public void taskTypes_name_matchesConstantIdentifier() {
        assertEquals("PROVISIONING",   TaskTypes.PROVISIONING.name());
        assertEquals("DATA_STAGING",   TaskTypes.DATA_STAGING.name());
        assertEquals("JOB_SUBMISSION", TaskTypes.JOB_SUBMISSION.name());
        assertEquals("DEPROVISIONING", TaskTypes.DEPROVISIONING.name());
        assertEquals("MONITORING",     TaskTypes.MONITORING.name());
        assertEquals("OUTPUT_FETCHING", TaskTypes.OUTPUT_FETCHING.name());
    }

    // ===========================================================================
    // Enum properties
    // ===========================================================================

    @Test
    public void taskTypes_isEnum() {
        assertTrue(TaskTypes.class.isEnum(), "TaskTypes must be an enum type");
    }

    @Test
    public void taskTypes_declaringClass_isTaskTypes() {
        assertSame(TaskTypes.class, TaskTypes.PROVISIONING.getDeclaringClass(),
                "Declaring class of every TaskTypes constant must be TaskTypes");
    }

    @Test
    public void taskTypes_ordinals_areStable() {
        assertEquals(0, TaskTypes.PROVISIONING.ordinal(),   "PROVISIONING must be ordinal 0");
        assertEquals(1, TaskTypes.DATA_STAGING.ordinal(),   "DATA_STAGING must be ordinal 1");
        assertEquals(2, TaskTypes.JOB_SUBMISSION.ordinal(), "JOB_SUBMISSION must be ordinal 2");
        assertEquals(3, TaskTypes.DEPROVISIONING.ordinal(), "DEPROVISIONING must be ordinal 3");
        assertEquals(4, TaskTypes.MONITORING.ordinal(),     "MONITORING must be ordinal 4");
        assertEquals(5, TaskTypes.OUTPUT_FETCHING.ordinal(), "OUTPUT_FETCHING must be ordinal 5");
    }
}
