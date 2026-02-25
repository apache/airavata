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
package org.apache.airavata.execution.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.airavata.execution.activity.ProcessActivity.Activities;
import org.apache.airavata.execution.activity.ProcessActivity.CancelInput;
import org.apache.airavata.execution.activity.ProcessActivity.PostInput;
import org.apache.airavata.execution.activity.ProcessActivity.PreInput;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for {@link ProcessActivity} input records, the
 * {@link Activities} interface contract, and related constants.
 *
 * <p>No Spring application context or Temporal test server is required.
 * All assertions operate directly on Java reflection and the {@link java.io.Serializable}
 * contract provided by the record declarations.
 *
 * <p>The {@link Activities} interface exposes exactly three DAG-phase methods:
 * {@code executePreDag}, {@code executePostDag}, and {@code executeCancelDag}.
 * Each accepts {@code (String processId, String gatewayId)} and returns {@code String}.
 */
public class ProcessActivityTest {

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID    = "proc-unit-001";
    private static final String EXPERIMENT_ID = "exp-unit-001";
    private static final String GATEWAY_ID    = "gw-unit-001";
    private static final String TOKEN_ID      = "tok-unit-001";

    // -------------------------------------------------------------------------
    // 1. TASK_QUEUE constant
    // -------------------------------------------------------------------------

    @Test
    public void taskQueueConstant_equalsExpectedValue() {
        assertEquals(
                "airavata-workflows",
                ProcessActivity.TASK_QUEUE,
                "TASK_QUEUE must equal 'airavata-workflows'");
    }

    @Test
    public void taskQueueConstant_isNotBlank() {
        assertNotNull(ProcessActivity.TASK_QUEUE, "TASK_QUEUE must not be null");
        assertFalse(ProcessActivity.TASK_QUEUE.isBlank(), "TASK_QUEUE must not be blank");
    }

    // -------------------------------------------------------------------------
    // 2. PreInput record accessors
    // -------------------------------------------------------------------------

    @Test
    public void preInput_recordAccessors_returnCorrectValues() {
        PreInput input = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertEquals(PROCESS_ID,    input.processId(),    "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID,    input.gatewayId(),    "gatewayId accessor mismatch");
        assertEquals(TOKEN_ID,      input.tokenId(),      "tokenId accessor mismatch");
    }

    @Test
    public void preInput_nullTokenId_isStoredAsNull() {
        PreInput input = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, null);

        assertNotNull(input.processId());
        assertNotNull(input.experimentId());
        assertNotNull(input.gatewayId());
        assertEquals(null, input.tokenId(), "null tokenId must be preserved");
    }

    // -------------------------------------------------------------------------
    // 3. PostInput record accessors
    // -------------------------------------------------------------------------

    @Test
    public void postInput_recordAccessors_withForceRunTrue() {
        PostInput input = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);

        assertEquals(PROCESS_ID,    input.processId(),    "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID,    input.gatewayId(),    "gatewayId accessor mismatch");
        assertTrue(input.forceRun(), "forceRun must be true");
    }

    @Test
    public void postInput_recordAccessors_withForceRunFalse() {
        PostInput input = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertEquals(PROCESS_ID,    input.processId(),    "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID,    input.gatewayId(),    "gatewayId accessor mismatch");
        assertFalse(input.forceRun(), "forceRun must be false");
    }

    // -------------------------------------------------------------------------
    // 4. CancelInput record accessors
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_recordAccessors_returnCorrectValues() {
        CancelInput input = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);

        assertEquals(PROCESS_ID,    input.processId(),    "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID,    input.gatewayId(),    "gatewayId accessor mismatch");
    }

    // -------------------------------------------------------------------------
    // 5. PreInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void preInput_serialization_roundTrip_preservesAllFields() throws Exception {
        PreInput original = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        PreInput deserialized = serializeDeserialize(original, PreInput.class);

        assertEquals(original.processId(),    deserialized.processId(),    "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(),    deserialized.gatewayId(),    "gatewayId lost during serialization");
        assertEquals(original.tokenId(),      deserialized.tokenId(),      "tokenId lost during serialization");
        assertEquals(original, deserialized, "Deserialized PreInput must equal the original");
    }

    @Test
    public void preInput_serialization_withNullTokenId_roundTrip() throws Exception {
        PreInput original    = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, null);
        PreInput deserialized = serializeDeserialize(original, PreInput.class);

        assertEquals(null, deserialized.tokenId(), "null tokenId must survive serialization round-trip");
        assertEquals(original, deserialized);
    }

    // -------------------------------------------------------------------------
    // 6. PostInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void postInput_serialization_roundTrip_withForceRunTrue() throws Exception {
        PostInput original    = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput deserialized = serializeDeserialize(original, PostInput.class);

        assertEquals(original.processId(),    deserialized.processId(),    "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(),    deserialized.gatewayId(),    "gatewayId lost during serialization");
        assertTrue(deserialized.forceRun(), "forceRun=true must survive serialization round-trip");
        assertEquals(original, deserialized, "Deserialized PostInput must equal the original");
    }

    @Test
    public void postInput_serialization_roundTrip_withForceRunFalse() throws Exception {
        PostInput original    = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);
        PostInput deserialized = serializeDeserialize(original, PostInput.class);

        assertFalse(deserialized.forceRun(), "forceRun=false must survive serialization round-trip");
        assertEquals(original, deserialized, "Deserialized PostInput must equal the original");
    }

    // -------------------------------------------------------------------------
    // 7. CancelInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_serialization_roundTrip_preservesAllFields() throws Exception {
        CancelInput original    = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        CancelInput deserialized = serializeDeserialize(original, CancelInput.class);

        assertEquals(original.processId(),    deserialized.processId(),    "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(),    deserialized.gatewayId(),    "gatewayId lost during serialization");
        assertEquals(original, deserialized, "Deserialized CancelInput must equal the original");
    }

    // -------------------------------------------------------------------------
    // 8. PreInput equality
    // -------------------------------------------------------------------------

    @Test
    public void preInput_equality_twoIdenticalRecords_areEqual() {
        PreInput first  = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        PreInput second = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertEquals(first, second, "Two PreInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal PreInputs must share the same hashCode");
    }

    @Test
    public void preInput_equality_differentProcessId_notEqual() {
        PreInput first  = new PreInput("proc-A", EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        PreInput second = new PreInput("proc-B", EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertNotEquals(first, second, "PreInputs with different processId must not be equal");
    }

    @Test
    public void preInput_equality_differentTokenId_notEqual() {
        PreInput first  = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, "token-X");
        PreInput second = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, "token-Y");

        assertNotEquals(first, second, "PreInputs with different tokenId must not be equal");
    }

    @Test
    public void preInput_equality_sameInstance_isEqual() {
        PreInput input = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertEquals(input, input, "A PreInput must equal itself");
    }

    // -------------------------------------------------------------------------
    // 9. PostInput equality
    // -------------------------------------------------------------------------

    @Test
    public void postInput_equality_twoIdenticalRecords_areEqual() {
        PostInput first  = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput second = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);

        assertEquals(first, second, "Two PostInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal PostInputs must share the same hashCode");
    }

    @Test
    public void postInput_equality_differentForceRun_notEqual() {
        PostInput withForce    = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput withoutForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertNotEquals(withForce, withoutForce,
                "PostInputs differing only in forceRun must not be equal");
    }

    // -------------------------------------------------------------------------
    // 10. CancelInput equality
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_equality_twoIdenticalRecords_areEqual() {
        CancelInput first  = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        CancelInput second = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);

        assertEquals(first, second, "Two CancelInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal CancelInputs must share the same hashCode");
    }

    @Test
    public void cancelInput_equality_differentGatewayId_notEqual() {
        CancelInput first  = new CancelInput(PROCESS_ID, EXPERIMENT_ID, "gw-A");
        CancelInput second = new CancelInput(PROCESS_ID, EXPERIMENT_ID, "gw-B");

        assertNotEquals(first, second, "CancelInputs with different gatewayId must not be equal");
    }

    // -------------------------------------------------------------------------
    // 11. Activities interface — exactly 3 methods present
    // -------------------------------------------------------------------------

    @Test
    public void activitiesInterface_hasExactlyThreeMethods() {
        Method[] methods = Activities.class.getMethods();
        // Filter to only the methods declared on Activities itself (exclude Object methods)
        long count = Arrays.stream(methods)
                .filter(m -> m.getDeclaringClass().equals(Activities.class))
                .count();

        assertEquals(3, count,
                "Activities interface must declare exactly 3 methods but found: " + count);
    }

    @Test
    public void activitiesInterface_hasExecutePreDagMethod() {
        Set<String> methodNames = getActivitiesMethodNames();
        assertTrue(methodNames.contains("executePreDag"),
                "Activities interface must declare 'executePreDag' method");
    }

    @Test
    public void activitiesInterface_hasExecutePostDagMethod() {
        Set<String> methodNames = getActivitiesMethodNames();
        assertTrue(methodNames.contains("executePostDag"),
                "Activities interface must declare 'executePostDag' method");
    }

    @Test
    public void activitiesInterface_hasExecuteCancelDagMethod() {
        Set<String> methodNames = getActivitiesMethodNames();
        assertTrue(methodNames.contains("executeCancelDag"),
                "Activities interface must declare 'executeCancelDag' method");
    }

    @Test
    public void activitiesInterface_allThreeExpectedMethodsPresent() {
        Set<String> expected = Set.of("executePreDag", "executePostDag", "executeCancelDag");
        Set<String> actual   = getActivitiesMethodNames();

        Set<String> missing = expected.stream()
                .filter(m -> !actual.contains(m))
                .collect(Collectors.toSet());

        assertTrue(missing.isEmpty(),
                "Activities interface is missing expected methods: " + missing);
    }

    @Test
    public void activitiesInterface_doesNotContainOldStepMethods() {
        Set<String> methodNames = getActivitiesMethodNames();

        assertFalse(methodNames.contains("provisioning"),
                "Activities interface must not declare legacy 'provisioning' method");
        assertFalse(methodNames.contains("inputDataStaging"),
                "Activities interface must not declare legacy 'inputDataStaging' method");
        assertFalse(methodNames.contains("jobSubmission"),
                "Activities interface must not declare legacy 'jobSubmission' method");
        assertFalse(methodNames.contains("deprovisioning"),
                "Activities interface must not declare legacy 'deprovisioning' method");
        assertFalse(methodNames.contains("monitoring"),
                "Activities interface must not declare legacy 'monitoring' method");
        assertFalse(methodNames.contains("outputStaging"),
                "Activities interface must not declare legacy 'outputStaging' method");
        assertFalse(methodNames.contains("archive"),
                "Activities interface must not declare legacy 'archive' method");
        assertFalse(methodNames.contains("markFailed"),
                "Activities interface must not declare legacy 'markFailed' method");
    }

    // -------------------------------------------------------------------------
    // 12. Activities interface — method signatures via reflection
    // -------------------------------------------------------------------------

    @Test
    public void activitiesInterface_executePreDag_returnTypeIsString() throws Exception {
        Method method = Activities.class.getMethod("executePreDag", String.class, String.class);

        assertEquals(String.class, method.getReturnType(),
                "'executePreDag' must return String");
    }

    @Test
    public void activitiesInterface_executePreDag_takesTwoStringParameters() throws Exception {
        Method method = Activities.class.getMethod("executePreDag", String.class, String.class);

        assertEquals(2, method.getParameterCount(),
                "'executePreDag' must accept exactly 2 parameters");
        assertEquals(String.class, method.getParameterTypes()[0],
                "'executePreDag' first parameter must be String (processId)");
        assertEquals(String.class, method.getParameterTypes()[1],
                "'executePreDag' second parameter must be String (gatewayId)");
    }

    @Test
    public void activitiesInterface_executePostDag_returnTypeIsString() throws Exception {
        Method method = Activities.class.getMethod("executePostDag", String.class, String.class);

        assertEquals(String.class, method.getReturnType(),
                "'executePostDag' must return String");
    }

    @Test
    public void activitiesInterface_executePostDag_takesTwoStringParameters() throws Exception {
        Method method = Activities.class.getMethod("executePostDag", String.class, String.class);

        assertEquals(2, method.getParameterCount(),
                "'executePostDag' must accept exactly 2 parameters");
        assertEquals(String.class, method.getParameterTypes()[0],
                "'executePostDag' first parameter must be String (processId)");
        assertEquals(String.class, method.getParameterTypes()[1],
                "'executePostDag' second parameter must be String (gatewayId)");
    }

    @Test
    public void activitiesInterface_executeCancelDag_returnTypeIsString() throws Exception {
        Method method = Activities.class.getMethod("executeCancelDag", String.class, String.class);

        assertEquals(String.class, method.getReturnType(),
                "'executeCancelDag' must return String");
    }

    @Test
    public void activitiesInterface_executeCancelDag_takesTwoStringParameters() throws Exception {
        Method method = Activities.class.getMethod("executeCancelDag", String.class, String.class);

        assertEquals(2, method.getParameterCount(),
                "'executeCancelDag' must accept exactly 2 parameters");
        assertEquals(String.class, method.getParameterTypes()[0],
                "'executeCancelDag' first parameter must be String (processId)");
        assertEquals(String.class, method.getParameterTypes()[1],
                "'executeCancelDag' second parameter must be String (gatewayId)");
    }

    @Test
    public void activitiesInterface_allThreeMethods_haveUniformSignature() throws Exception {
        // All three methods must share the same signature: String f(String, String)
        for (String name : new String[]{"executePreDag", "executePostDag", "executeCancelDag"}) {
            Method method = Activities.class.getMethod(name, String.class, String.class);

            assertEquals(String.class, method.getReturnType(),
                    name + " must return String");
            assertEquals(2, method.getParameterCount(),
                    name + " must accept exactly 2 parameters");
            assertEquals(String.class, method.getParameterTypes()[0],
                    name + " first parameter must be String");
            assertEquals(String.class, method.getParameterTypes()[1],
                    name + " second parameter must be String");
        }
    }

    // -------------------------------------------------------------------------
    // 13. Record toString sanity checks
    // -------------------------------------------------------------------------

    @Test
    public void preInput_toString_containsAllFieldValues() {
        PreInput input = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        String str     = input.toString();

        assertTrue(str.contains(PROCESS_ID),    "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID),    "toString must contain gatewayId");
        assertTrue(str.contains(TOKEN_ID),      "toString must contain tokenId");
    }

    @Test
    public void postInput_toString_containsAllFieldValues() {
        PostInput input = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);
        String str      = input.toString();

        assertTrue(str.contains(PROCESS_ID),    "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID),    "toString must contain gatewayId");
    }

    @Test
    public void postInput_toString_containsForceRunValue() {
        PostInput withForce    = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput withoutForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertTrue(withForce.toString().contains("true"),
                "toString of forceRun=true PostInput must contain 'true'");
        assertTrue(withoutForce.toString().contains("false"),
                "toString of forceRun=false PostInput must contain 'false'");
    }

    @Test
    public void cancelInput_toString_containsAllFieldValues() {
        CancelInput input = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        String str        = input.toString();

        assertTrue(str.contains(PROCESS_ID),    "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID),    "toString must contain gatewayId");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the set of method names declared directly on the {@link Activities}
     * interface (excludes inherited {@code Object} methods).
     */
    private static Set<String> getActivitiesMethodNames() {
        return Arrays.stream(Activities.class.getMethods())
                .filter(m -> m.getDeclaringClass().equals(Activities.class))
                .map(Method::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Serializes {@code value} to bytes and deserializes it back, returning the
     * reconstructed instance cast to {@code type}.
     */
    @SuppressWarnings("unchecked")
    private static <T> T serializeDeserialize(T value, Class<T> type) throws Exception {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
            bytes = baos.toByteArray();
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
    }
}
