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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.apache.airavata.core.model.DagTaskResult;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the {@link DagTaskResult} sealed interface and its
 * two permitted implementations: {@link DagTaskResult.Success} and
 * {@link DagTaskResult.Failure}.
 * No Spring context or external dependencies required.
 */
public class DagTaskResultTest {

    // ===========================================================================
    // Success — canonical two-arg record constructor
    // ===========================================================================

    @Test
    public void success_storesMessage() {
        DagTaskResult.Success result = new DagTaskResult.Success("job submitted", Map.of("jobId", "42"));

        assertEquals("job submitted", result.message(), "Success must store the message passed to the constructor");
    }

    @Test
    public void success_storesOutput() {
        Map<String, String> output = Map.of("jobId", "42", "queue", "default");
        DagTaskResult.Success result = new DagTaskResult.Success("ok", output);

        assertEquals(output, result.output(), "Success must store the output map passed to the constructor");
    }

    @Test
    public void success_output_containsExpectedEntries() {
        DagTaskResult.Success result = new DagTaskResult.Success("done", Map.of("key", "value"));

        assertEquals("value", result.output().get("key"));
    }

    @Test
    public void success_output_mapIsNotNull() {
        DagTaskResult.Success result = new DagTaskResult.Success("done", Map.of());

        assertNotNull(result.output(), "output map must never be null");
    }

    // ===========================================================================
    // Success — single-arg convenience constructor
    // ===========================================================================

    @Test
    public void success_convenienceConstructor_createsEmptyOutputMap() {
        DagTaskResult.Success result = new DagTaskResult.Success("finished");

        assertNotNull(result.output(), "Single-arg constructor must produce a non-null output map");
        assertTrue(result.output().isEmpty(), "Single-arg constructor must produce an empty output map");
    }

    @Test
    public void success_convenienceConstructor_preservesMessage() {
        DagTaskResult.Success result = new DagTaskResult.Success("provisioning complete");

        assertEquals("provisioning complete", result.message());
    }

    // ===========================================================================
    // Success — record equality and sealed interface membership
    // ===========================================================================

    @Test
    public void success_isInstanceOfDagTaskResult() {
        DagTaskResult result = new DagTaskResult.Success("ok");

        assertInstanceOf(DagTaskResult.class, result, "Success must implement DagTaskResult");
    }

    @Test
    public void success_recordEquality_whenFieldsMatch() {
        DagTaskResult.Success a = new DagTaskResult.Success("msg", Map.of("k", "v"));
        DagTaskResult.Success b = new DagTaskResult.Success("msg", Map.of("k", "v"));

        assertEquals(a, b, "Two Success records with identical fields must be equal");
    }

    // ===========================================================================
    // Failure — canonical three-arg record constructor
    // ===========================================================================

    @Test
    public void failure_storesReason() {
        RuntimeException cause = new RuntimeException("ssh error");
        DagTaskResult.Failure result = new DagTaskResult.Failure("SSH connection refused", true, cause);

        assertEquals(
                "SSH connection refused", result.reason(), "Failure must store the reason passed to the constructor");
    }

    @Test
    public void failure_storesFatalFlag_true() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("disk full", true, null);

        assertTrue(result.fatal(), "fatal flag must be true when constructed with fatal=true");
    }

    @Test
    public void failure_storesFatalFlag_false() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("timeout", false, null);

        assertFalse(result.fatal(), "fatal flag must be false when constructed with fatal=false");
    }

    @Test
    public void failure_storesCause() {
        RuntimeException cause = new RuntimeException("underlying error");
        DagTaskResult.Failure result = new DagTaskResult.Failure("wrapped", false, cause);

        assertEquals(cause, result.cause(), "Failure must store the cause exception passed to the constructor");
    }

    @Test
    public void failure_cause_canBeNull() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("no cause", false, null);

        assertNull(result.cause(), "cause may legitimately be null when no exception is available");
    }

    // ===========================================================================
    // Failure — two-arg convenience constructor (reason + fatal)
    // ===========================================================================

    @Test
    public void failure_twoArgConstructor_setsNullCause() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("resource unavailable", true);

        assertNull(result.cause(), "Two-arg constructor must leave cause as null");
    }

    @Test
    public void failure_twoArgConstructor_preservesReason() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("provision failed", false);

        assertEquals("provision failed", result.reason());
    }

    @Test
    public void failure_twoArgConstructor_preservesFatalFlag() {
        DagTaskResult.Failure fatal = new DagTaskResult.Failure("critical", true);
        DagTaskResult.Failure nonFatal = new DagTaskResult.Failure("transient", false);

        assertTrue(fatal.fatal(), "fatal must be true");
        assertFalse(nonFatal.fatal(), "fatal must be false");
    }

    // ===========================================================================
    // Failure — single-arg convenience constructor (reason only)
    // ===========================================================================

    @Test
    public void failure_singleArgConstructor_setsNullCause() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("unexpected error");

        assertNull(result.cause(), "Single-arg constructor must leave cause as null");
    }

    @Test
    public void failure_singleArgConstructor_setsFatalToFalse() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("timeout waiting for job");

        assertFalse(result.fatal(), "Single-arg constructor must default fatal to false");
    }

    @Test
    public void failure_singleArgConstructor_preservesReason() {
        DagTaskResult.Failure result = new DagTaskResult.Failure("stage-in failed");

        assertEquals("stage-in failed", result.reason());
    }

    // ===========================================================================
    // Failure — sealed interface membership
    // ===========================================================================

    @Test
    public void failure_isInstanceOfDagTaskResult() {
        DagTaskResult result = new DagTaskResult.Failure("bad");

        assertInstanceOf(DagTaskResult.class, result, "Failure must implement DagTaskResult");
    }

    @Test
    public void failure_recordEquality_whenFieldsMatch() {
        DagTaskResult.Failure a = new DagTaskResult.Failure("err", false, null);
        DagTaskResult.Failure b = new DagTaskResult.Failure("err", false, null);

        assertEquals(a, b, "Two Failure records with identical fields must be equal");
    }

    // ===========================================================================
    // Pattern matching — switch expression over sealed type
    // ===========================================================================

    @Test
    public void patternMatchingSwitch_classifiesSuccess() {
        DagTaskResult result = new DagTaskResult.Success("all good");

        String classification =
                switch (result) {
                    case DagTaskResult.Success s -> "success:" + s.message();
                    case DagTaskResult.Failure f -> "failure:" + f.reason();
                };

        assertEquals(
                "success:all good", classification, "Pattern matching switch must route Success to the success branch");
    }

    @Test
    public void patternMatchingSwitch_classifiesFailure() {
        DagTaskResult result = new DagTaskResult.Failure("timed out", true);

        String classification =
                switch (result) {
                    case DagTaskResult.Success s -> "success:" + s.message();
                    case DagTaskResult.Failure f -> "failure:" + f.reason();
                };

        assertEquals(
                "failure:timed out",
                classification,
                "Pattern matching switch must route Failure to the failure branch");
    }

    @Test
    public void patternMatchingSwitch_extractsOutputFromSuccess() {
        DagTaskResult result = new DagTaskResult.Success("submitted", Map.of("jobId", "99"));

        String jobId =
                switch (result) {
                    case DagTaskResult.Success s -> s.output().get("jobId");
                    case DagTaskResult.Failure f -> null;
                };

        assertEquals("99", jobId, "Pattern matching must allow access to Success output fields");
    }

    @Test
    public void patternMatchingSwitch_extractsFatalFlagFromFailure() {
        DagTaskResult result = new DagTaskResult.Failure("disk full", true, null);

        boolean isFatal =
                switch (result) {
                    case DagTaskResult.Success s -> false;
                    case DagTaskResult.Failure f -> f.fatal();
                };

        assertTrue(isFatal, "Pattern matching must allow access to Failure.fatal()");
    }

    @Test
    public void patternMatchingSwitch_extractsCauseFromFailure() {
        RuntimeException cause = new RuntimeException("root cause");
        DagTaskResult result = new DagTaskResult.Failure("wrapped", false, cause);

        Throwable extracted =
                switch (result) {
                    case DagTaskResult.Success s -> null;
                    case DagTaskResult.Failure f -> f.cause();
                };

        assertEquals(cause, extracted, "Pattern matching must expose the stored Throwable cause");
    }

    // ===========================================================================
    // Success — output map immutability (Map.of returns unmodifiable map)
    // ===========================================================================

    @Test
    public void success_output_fromMapOf_isUnmodifiable() {
        DagTaskResult.Success result = new DagTaskResult.Success("ok", Map.of("k", "v"));

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.output().put("extra", "value"),
                "Output map created via Map.of must be unmodifiable");
    }

    @Test
    public void success_convenienceConstructor_output_isUnmodifiable() {
        DagTaskResult.Success result = new DagTaskResult.Success("ok");

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.output().put("extra", "value"),
                "Output map from single-arg constructor (Map.of()) must be unmodifiable");
    }
}
