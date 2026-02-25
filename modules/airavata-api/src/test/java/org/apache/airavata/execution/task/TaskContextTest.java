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
package org.apache.airavata.execution.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.compute.resource.model.JobModel;
import org.apache.airavata.compute.resource.model.ResourceBinding;
import org.apache.airavata.core.model.StatusModel;
import org.apache.airavata.execution.model.ProcessModel;
import org.apache.airavata.core.model.ProcessState;
import org.apache.airavata.research.application.model.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TaskContext}.
 *
 * <p>All tests use real domain objects; no mocking framework is required. The test suite
 * exercises constructor validation, lazy-loading working directory logic, scheduling helper
 * delegates, DAG state mutability, process-state reads, stdout/stderr path construction,
 * and login-username resolution from credential bindings.
 */
class TaskContextTest {

    // Fixed identifiers reused across most tests
    private static final String PROCESS_ID = "proc-001";
    private static final String GATEWAY_ID = "gateway-test";
    private static final String TASK_ID = "task-001";
    private static final String EXPERIMENT_ID = "exp-abc";

    /**
     * Creates a minimal {@link ProcessModel} with the standard experiment ID and no
     * resource schedule unless overridden by a test.
     */
    private ProcessModel minimalProcessModel() {
        ProcessModel pm = new ProcessModel();
        pm.setProcessId(PROCESS_ID);
        pm.setExperimentId(EXPERIMENT_ID);
        return pm;
    }

    /**
     * Creates a {@link ProcessModel} whose resource-schedule map contains the supplied
     * key/value pair in addition to the standard experiment ID.
     */
    private ProcessModel processModelWithSchedule(String key, String value) {
        ProcessModel pm = minimalProcessModel();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put(key, value);
        pm.setResourceSchedule(schedule);
        return pm;
    }

    // -------------------------------------------------------------------------
    // 1. Constructor validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorValidationTests {

        @Test
        @DisplayName("null processId throws IllegalArgumentException")
        void nullProcessIdThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskContext(null, GATEWAY_ID, TASK_ID, minimalProcessModel()),
                    "Expected IllegalArgumentException when processId is null");
        }

        @Test
        @DisplayName("null gatewayId throws IllegalArgumentException")
        void nullGatewayIdThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskContext(PROCESS_ID, null, TASK_ID, minimalProcessModel()),
                    "Expected IllegalArgumentException when gatewayId is null");
        }

        @Test
        @DisplayName("null taskId throws IllegalArgumentException")
        void nullTaskIdThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskContext(PROCESS_ID, GATEWAY_ID, null, minimalProcessModel()),
                    "Expected IllegalArgumentException when taskId is null");
        }

        @Test
        @DisplayName("null processModel throws IllegalArgumentException")
        void nullProcessModelThrows() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, null),
                    "Expected IllegalArgumentException when processModel is null");
        }

        @Test
        @DisplayName("all nulls throws IllegalArgumentException")
        void allNullsThrow() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new TaskContext(null, null, null, null),
                    "Expected IllegalArgumentException when all constructor args are null");
        }

        @Test
        @DisplayName("valid arguments creates TaskContext successfully")
        void validArgumentsSucceeds() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            assertEquals(PROCESS_ID, ctx.getProcessId());
            assertEquals(GATEWAY_ID, ctx.getGatewayId());
            assertEquals(TASK_ID, ctx.getTaskId());
            assertNotNull(ctx.getProcessModel());
        }
    }

    // -------------------------------------------------------------------------
    // 2. getExperimentId delegation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getExperimentId")
    class ExperimentIdTests {

        @Test
        @DisplayName("returns experimentId from processModel")
        void returnsExperimentIdFromProcessModel() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            assertEquals(EXPERIMENT_ID, ctx.getExperimentId());
        }

        @Test
        @DisplayName("returns different experimentId when processModel has different value")
        void returnsDifferentExperimentIdWhenChanged() {
            ProcessModel pm = new ProcessModel();
            pm.setExperimentId("exp-xyz-999");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            assertEquals("exp-xyz-999", ctx.getExperimentId());
        }
    }

    // -------------------------------------------------------------------------
    // 3 & 4. getWorkingDir — static dir and scratch-derived
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getWorkingDir")
    class WorkingDirTests {

        @Test
        @DisplayName("returns staticWorkingDir from resource schedule when set")
        void returnsStaticWorkingDir() throws Exception {
            TaskContext ctx = new TaskContext(
                    PROCESS_ID,
                    GATEWAY_ID,
                    TASK_ID,
                    processModelWithSchedule("staticWorkingDir", "/opt/jobs/static-dir"));

            assertEquals("/opt/jobs/static-dir", ctx.getWorkingDir());
        }

        @Test
        @DisplayName("staticWorkingDir takes precedence over scratchLocation")
        void staticWorkingDirTakesPrecedenceOverScratch() throws Exception {
            ProcessModel pm = minimalProcessModel();
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("staticWorkingDir", "/explicit/working");
            schedule.put("overrideScratchLocation", "/scratch/override");
            pm.setResourceSchedule(schedule);
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals("/explicit/working", ctx.getWorkingDir());
        }

        @Test
        @DisplayName("derives workingDir as scratch + '/' + processId when no static dir configured")
        void derivesWorkingDirFromScratch() throws Exception {
            TaskContext ctx = new TaskContext(
                    PROCESS_ID,
                    GATEWAY_ID,
                    TASK_ID,
                    processModelWithSchedule("overrideScratchLocation", "/scratch/home"));

            assertEquals("/scratch/home/" + PROCESS_ID, ctx.getWorkingDir());
        }

        @Test
        @DisplayName("appends processId directly when scratch ends with '/'")
        void appendsProcessIdWhenScratchEndsWithSlash() throws Exception {
            TaskContext ctx = new TaskContext(
                    PROCESS_ID,
                    GATEWAY_ID,
                    TASK_ID,
                    processModelWithSchedule("overrideScratchLocation", "/scratch/home/"));

            assertEquals("/scratch/home/" + PROCESS_ID, ctx.getWorkingDir());
        }

        @Test
        @DisplayName("getWorkingDir returns same instance on second call (lazy cache)")
        void lazyWorkingDirIsCached() throws Exception {
            TaskContext ctx = new TaskContext(
                    PROCESS_ID,
                    GATEWAY_ID,
                    TASK_ID,
                    processModelWithSchedule("staticWorkingDir", "/cached/dir"));

            String first = ctx.getWorkingDir();
            String second = ctx.getWorkingDir();
            assertSame(first, second, "Working dir string should be the same cached instance");
        }

        @Test
        @DisplayName("setWorkingDir bypasses lazy calculation on next call")
        void setWorkingDirOverridesLazyCalculation() throws Exception {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            ctx.setWorkingDir("/manually/set");

            assertEquals("/manually/set", ctx.getWorkingDir());
        }

        @Test
        @DisplayName("whitespace-only staticWorkingDir falls through to scratch calculation")
        void whitespaceOnlyStaticWorkingDirFallsToScratch() throws Exception {
            ProcessModel pm = minimalProcessModel();
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("staticWorkingDir", "   ");
            schedule.put("overrideScratchLocation", "/scratch/ws");
            pm.setResourceSchedule(schedule);
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals("/scratch/ws/" + PROCESS_ID, ctx.getWorkingDir());
        }
    }

    // -------------------------------------------------------------------------
    // 5, 6 & 7. getScratchLocation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getScratchLocation")
    class ScratchLocationTests {

        @Test
        @DisplayName("returns overrideScratchLocation from resource schedule")
        void returnsOverrideScratchLocationFromSchedule() throws Exception {
            TaskContext ctx = new TaskContext(
                    PROCESS_ID,
                    GATEWAY_ID,
                    TASK_ID,
                    processModelWithSchedule("overrideScratchLocation", "/scratch/override"));

            assertEquals("/scratch/override", ctx.getScratchLocation());
        }

        @Test
        @DisplayName("falls back to credentialResourceBinding metadata scratchLocation")
        void fallsBackToBindingMetadata() throws Exception {
            ProcessModel pm = minimalProcessModel(); // no overrideScratchLocation in schedule
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            ResourceBinding binding = new ResourceBinding();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("scratchLocation", "/scratch/from-binding");
            binding.setMetadata(metadata);
            ctx.setCredentialResourceBinding(binding);

            assertEquals("/scratch/from-binding", ctx.getScratchLocation());
        }

        @Test
        @DisplayName("overrideScratchLocation takes precedence over binding metadata")
        void overrideTakesPrecedenceOverBinding() throws Exception {
            ProcessModel pm = processModelWithSchedule("overrideScratchLocation", "/scratch/schedule");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            ResourceBinding binding = new ResourceBinding();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("scratchLocation", "/scratch/binding");
            binding.setMetadata(metadata);
            ctx.setCredentialResourceBinding(binding);

            assertEquals("/scratch/schedule", ctx.getScratchLocation());
        }

        @Test
        @DisplayName("throws RuntimeException when neither schedule nor binding provides scratch location")
        void throwsWhenNoScratchAvailable() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());

            RuntimeException ex = assertThrows(RuntimeException.class, ctx::getScratchLocation);
            assertTrue(
                    ex.getMessage().contains(PROCESS_ID),
                    "Exception message should include processId for diagnostics");
        }

        @Test
        @DisplayName("throws when binding metadata has no scratchLocation key")
        void throwsWhenBindingMetadataLacksScratchKey() {
            ProcessModel pm = minimalProcessModel();
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            ResourceBinding binding = new ResourceBinding();
            binding.setMetadata(new HashMap<>()); // metadata present but no 'scratchLocation' key
            ctx.setCredentialResourceBinding(binding);

            assertThrows(RuntimeException.class, ctx::getScratchLocation);
        }

        @Test
        @DisplayName("throws when binding metadata scratchLocation value is blank")
        void throwsWhenBindingMetadataScratchLocationIsBlank() {
            ProcessModel pm = minimalProcessModel();
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            ResourceBinding binding = new ResourceBinding();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("scratchLocation", "   "); // blank — not valid
            binding.setMetadata(metadata);
            ctx.setCredentialResourceBinding(binding);

            assertThrows(RuntimeException.class, ctx::getScratchLocation);
        }

        @Test
        @DisplayName("setScratchLocation bypasses lazy calculation")
        void setScratchLocationBypassesLazyCalc() throws Exception {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            ctx.setScratchLocation("/preset/scratch");

            assertEquals("/preset/scratch", ctx.getScratchLocation());
        }
    }

    // -------------------------------------------------------------------------
    // 8. scheduleString helpers — queueName, allocationProjectNumber, reservation,
    //    qualityOfService
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("scheduleString helpers")
    class ScheduleStringHelperTests {

        private ProcessModel buildScheduleModel() {
            ProcessModel pm = minimalProcessModel();
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("queueName", "batch-queue");
            schedule.put("allocationProjectNumber", "ALLOC-12345");
            schedule.put("reservation", "reservation-alpha");
            schedule.put("qualityOfService", "high");
            pm.setResourceSchedule(schedule);
            return pm;
        }

        @Test
        @DisplayName("getQueueName returns queueName from schedule")
        void getQueueNameReturnsScheduledValue() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, buildScheduleModel());
            assertEquals("batch-queue", ctx.getQueueName());
        }

        @Test
        @DisplayName("getAllocationProjectNumber returns allocationProjectNumber from schedule")
        void getAllocationProjectNumberReturnsScheduledValue() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, buildScheduleModel());
            assertEquals("ALLOC-12345", ctx.getAllocationProjectNumber());
        }

        @Test
        @DisplayName("getReservation returns reservation from schedule")
        void getReservationReturnsScheduledValue() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, buildScheduleModel());
            assertEquals("reservation-alpha", ctx.getReservation());
        }

        @Test
        @DisplayName("getQualityOfService returns qualityOfService from schedule")
        void getQualityOfServiceReturnsScheduledValue() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, buildScheduleModel());
            assertEquals("high", ctx.getQualityOfService());
        }

        @Test
        @DisplayName("schedule helper returns null when key is absent")
        void returnsNullForAbsentKey() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            // No schedule set — all helpers should return null
            assertNull(ctx.getQueueName());
            assertNull(ctx.getAllocationProjectNumber());
            assertNull(ctx.getReservation());
            assertNull(ctx.getQualityOfService());
        }
    }

    // -------------------------------------------------------------------------
    // 9. scheduleString with null resource schedule
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("scheduleString with null schedule")
    class NullScheduleTests {

        @Test
        @DisplayName("all schedule helpers return null when resourceSchedule is null")
        void allHelpersReturnNullForNullSchedule() {
            ProcessModel pm = minimalProcessModel();
            pm.setResourceSchedule(null); // explicitly null
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertNull(ctx.getQueueName());
            assertNull(ctx.getAllocationProjectNumber());
            assertNull(ctx.getReservation());
            assertNull(ctx.getQualityOfService());
        }
    }

    // -------------------------------------------------------------------------
    // 10. dagState — mutable and shared
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dagState mutability")
    class DagStateTests {

        @Test
        @DisplayName("getDagState returns non-null map")
        void getDagStateReturnsNonNull() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            assertNotNull(ctx.getDagState());
        }

        @Test
        @DisplayName("dagState supports put and get")
        void dagStatePutAndGetWorks() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            ctx.getDagState().put("output-key", "output-value");

            assertEquals("output-value", ctx.getDagState().get("output-key"));
        }

        @Test
        @DisplayName("dagState is the same map instance on repeated calls")
        void dagStateReturnsSameInstance() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            Map<String, String> first = ctx.getDagState();
            Map<String, String> second = ctx.getDagState();
            assertSame(first, second, "getDagState should always return the same map reference");
        }

        @Test
        @DisplayName("dagState entries survive across multiple accesses")
        void dagStateEntriesPersist() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            ctx.getDagState().put("step1", "done");
            ctx.getDagState().put("step2", "pending");

            assertEquals(2, ctx.getDagState().size());
            assertEquals("done", ctx.getDagState().get("step1"));
            assertEquals("pending", ctx.getDagState().get("step2"));
        }

        @Test
        @DisplayName("dagState is initialized empty on construction")
        void dagStateIsEmptyInitially() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            assertTrue(ctx.getDagState().isEmpty(), "dagState should be empty on fresh construction");
        }
    }

    // -------------------------------------------------------------------------
    // 11 & 12. getProcessState
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getProcessState")
    class ProcessStateTests {

        @Test
        @DisplayName("returns state from first element of processStatuses list")
        void returnsFirstProcessStatus() {
            ProcessModel pm = minimalProcessModel();
            List<StatusModel<ProcessState>> statuses = new ArrayList<>();
            statuses.add(new StatusModel<>(ProcessState.EXECUTING));
            pm.setProcessStatuses(statuses);

            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            assertEquals(ProcessState.EXECUTING, ctx.getProcessState());
        }

        @Test
        @DisplayName("returns null when processStatuses list is null")
        void returnsNullWhenStatusListIsNull() {
            ProcessModel pm = minimalProcessModel();
            pm.setProcessStatuses(null);

            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            assertNull(ctx.getProcessState());
        }

        @Test
        @DisplayName("returns null when processStatuses list is empty")
        void returnsNullWhenStatusListIsEmpty() {
            ProcessModel pm = minimalProcessModel();
            pm.setProcessStatuses(new ArrayList<>());

            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            assertNull(ctx.getProcessState());
        }

        @Test
        @DisplayName("reads first status element when multiple statuses are present")
        void readsFirstStatusFromMultipleEntries() {
            ProcessModel pm = minimalProcessModel();
            List<StatusModel<ProcessState>> statuses = new ArrayList<>();
            statuses.add(new StatusModel<>(ProcessState.COMPLETED));
            statuses.add(new StatusModel<>(ProcessState.EXECUTING));
            pm.setProcessStatuses(statuses);

            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            assertEquals(ProcessState.COMPLETED, ctx.getProcessState());
        }

        @Test
        @DisplayName("setProcessStatus updates processState correctly")
        void setProcessStatusUpdatesState() {
            ProcessModel pm = minimalProcessModel();
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            ctx.setProcessStatus(new StatusModel<>(ProcessState.FAILED));
            assertEquals(ProcessState.FAILED, ctx.getProcessState());
        }
    }

    // -------------------------------------------------------------------------
    // 13 & 14. getStdoutLocation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getStdoutLocation")
    class StdoutLocationTests {

        @Test
        @DisplayName("combines workingDir, application name, and '.stdout' extension")
        void combinesWorkingDirAndAppName() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run1");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            Application app = new Application();
            app.setName("MyApp");
            ctx.setApplication(app);

            assertEquals("/jobs/run1/MyApp.stdout", ctx.getStdoutLocation());
        }

        @Test
        @DisplayName("uses 'application' as default appName when no application model set")
        void usesDefaultAppNameWhenNoApplicationModel() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run2");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            // no applicationModel set

            assertEquals("/jobs/run2/application.stdout", ctx.getStdoutLocation());
        }

        @Test
        @DisplayName("handles workingDir ending with '/' correctly")
        void handlesWorkingDirWithTrailingSlash() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run3/");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            Application app = new Application();
            app.setName("Echo");
            ctx.setApplication(app);

            assertEquals("/jobs/run3/Echo.stdout", ctx.getStdoutLocation());
        }

        @Test
        @DisplayName("getStdoutLocation is cached after first call")
        void stdoutIsCachedAfterFirstCall() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run4");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            String first = ctx.getStdoutLocation();
            String second = ctx.getStdoutLocation();
            assertSame(first, second, "stdoutLocation should be the same cached instance");
        }

        @Test
        @DisplayName("setStdoutLocation bypasses lazy calculation")
        void setStdoutLocationBypasses() throws Exception {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            ctx.setStdoutLocation("/explicitly/set/stdout");

            assertEquals("/explicitly/set/stdout", ctx.getStdoutLocation());
        }
    }

    // -------------------------------------------------------------------------
    // getStderrLocation (mirrors stdout logic)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getStderrLocation")
    class StderrLocationTests {

        @Test
        @DisplayName("combines workingDir, application name, and '.stderr' extension")
        void combinesWorkingDirAndAppName() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run5");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            Application app = new Application();
            app.setName("Solver");
            ctx.setApplication(app);

            assertEquals("/jobs/run5/Solver.stderr", ctx.getStderrLocation());
        }

        @Test
        @DisplayName("uses 'application' as default appName when no application model set")
        void usesDefaultAppNameWhenNoApplicationModel() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/run6");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals("/jobs/run6/application.stderr", ctx.getStderrLocation());
        }
    }

    // -------------------------------------------------------------------------
    // 15. getComputeResourceLoginUserName
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getComputeResourceLoginUserName")
    class LoginUsernameTests {

        @Test
        @DisplayName("returns loginUsername from credentialResourceBinding")
        void returnsLoginUsernameFromBinding() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());

            ResourceBinding binding = new ResourceBinding();
            binding.setLoginUsername("hpc-user");
            ctx.setCredentialResourceBinding(binding);

            assertEquals("hpc-user", ctx.getComputeResourceLoginUserName());
        }

        @Test
        @DisplayName("returns null when no credentialResourceBinding is set")
        void returnsNullWhenNoBindingSet() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            // no binding set
            assertNull(ctx.getComputeResourceLoginUserName());
        }

        @Test
        @DisplayName("returns null when binding has null loginUsername")
        void returnsNullWhenBindingLoginUsernameIsNull() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());

            ResourceBinding binding = new ResourceBinding();
            binding.setLoginUsername(null); // explicitly null
            ctx.setCredentialResourceBinding(binding);

            assertNull(ctx.getComputeResourceLoginUserName());
        }
    }

    // -------------------------------------------------------------------------
    // getJobModel — lazy init
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getJobModel lazy initialization")
    class JobModelTests {

        @Test
        @DisplayName("returns non-null JobModel on first call")
        void returnsNonNullJobModel() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/wdir");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            JobModel job = ctx.getJobModel();
            assertNotNull(job);
        }

        @Test
        @DisplayName("lazy JobModel has processId set")
        void lazyJobModelHasProcessId() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/wdir2");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals(PROCESS_ID, ctx.getJobModel().getProcessId());
        }

        @Test
        @DisplayName("lazy JobModel workingDir matches context workingDir")
        void lazyJobModelWorkingDirMatchesContext() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/wdir3");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals(ctx.getWorkingDir(), ctx.getJobModel().getWorkingDir());
        }

        @Test
        @DisplayName("lazy JobModel has positive creationTime")
        void lazyJobModelHasPositiveCreationTime() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/wdir4");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertTrue(ctx.getJobModel().getCreationTime() > 0,
                    "JobModel creationTime should be a positive epoch millis value");
        }

        @Test
        @DisplayName("same JobModel instance is returned on repeated calls")
        void jobModelIsCached() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/cached");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            JobModel first = ctx.getJobModel();
            JobModel second = ctx.getJobModel();
            assertSame(first, second, "getJobModel should return the same cached instance");
        }

        @Test
        @DisplayName("setJobModel replaces the lazy-init value")
        void setJobModelReplacesLazyValue() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/manual");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            JobModel manual = new JobModel();
            manual.setProcessId("custom-id");
            ctx.setJobModel(manual);

            assertSame(manual, ctx.getJobModel());
            assertEquals("custom-id", ctx.getJobModel().getProcessId());
        }
    }

    // -------------------------------------------------------------------------
    // getComputeResourceId delegation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getComputeResourceId")
    class ComputeResourceIdTests {

        @Test
        @DisplayName("returns resourceId from processModel")
        void returnsResourceIdFromProcessModel() {
            ProcessModel pm = minimalProcessModel();
            pm.setResourceId("compute-resource-hpc1");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals("compute-resource-hpc1", ctx.getComputeResourceId());
        }

        @Test
        @DisplayName("returns null when resourceId not set on processModel")
        void returnsNullWhenResourceIdNotSet() {
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, minimalProcessModel());
            assertNull(ctx.getComputeResourceId());
        }
    }

    // -------------------------------------------------------------------------
    // getInputDir and getOutputDir — delegates to workingDir
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getInputDir and getOutputDir")
    class InputOutputDirTests {

        @Test
        @DisplayName("getInputDir defaults to workingDir when not explicitly set")
        void inputDirDefaultsToWorkingDir() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/io-test");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals(ctx.getWorkingDir(), ctx.getInputDir());
        }

        @Test
        @DisplayName("getOutputDir defaults to workingDir when not explicitly set")
        void outputDirDefaultsToWorkingDir() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/io-test");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);

            assertEquals(ctx.getWorkingDir(), ctx.getOutputDir());
        }

        @Test
        @DisplayName("setInputDir overrides the default")
        void setInputDirOverridesDefault() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/io-test2");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            ctx.setInputDir("/custom/input");

            assertEquals("/custom/input", ctx.getInputDir());
        }

        @Test
        @DisplayName("setOutputDir overrides the default")
        void setOutputDirOverridesDefault() throws Exception {
            ProcessModel pm = processModelWithSchedule("staticWorkingDir", "/jobs/io-test3");
            TaskContext ctx = new TaskContext(PROCESS_ID, GATEWAY_ID, TASK_ID, pm);
            ctx.setOutputDir("/custom/output");

            assertEquals("/custom/output", ctx.getOutputDir());
        }
    }
}
