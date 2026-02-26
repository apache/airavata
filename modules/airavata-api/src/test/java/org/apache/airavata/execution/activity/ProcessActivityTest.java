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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.airavata.compute.resource.model.ComputeResourceType;
import org.apache.airavata.execution.activity.ProcessActivity.Activities;
import org.apache.airavata.execution.activity.ProcessActivity.CancelInput;
import org.apache.airavata.execution.activity.ProcessActivity.NodeResult;
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
 * <p>The {@link Activities} interface exposes exactly two methods:
 * {@code resolveResourceType} and {@code executeDagNode}.
 */
public class ProcessActivityTest {

    // -------------------------------------------------------------------------
    // Shared fixtures
    // -------------------------------------------------------------------------

    private static final String PROCESS_ID = "proc-unit-001";
    private static final String EXPERIMENT_ID = "exp-unit-001";
    private static final String GATEWAY_ID = "gw-unit-001";
    private static final String TOKEN_ID = "tok-unit-001";

    // -------------------------------------------------------------------------
    // 1. TASK_QUEUE constant
    // -------------------------------------------------------------------------

    @Test
    public void taskQueueConstant_equalsExpectedValue() {
        assertEquals("airavata-workflows", ProcessActivity.TASK_QUEUE, "TASK_QUEUE must equal 'airavata-workflows'");
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

        assertEquals(PROCESS_ID, input.processId(), "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID, input.gatewayId(), "gatewayId accessor mismatch");
        assertEquals(TOKEN_ID, input.tokenId(), "tokenId accessor mismatch");
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

        assertEquals(PROCESS_ID, input.processId(), "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID, input.gatewayId(), "gatewayId accessor mismatch");
        assertTrue(input.forceRun(), "forceRun must be true");
    }

    @Test
    public void postInput_recordAccessors_withForceRunFalse() {
        PostInput input = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertEquals(PROCESS_ID, input.processId(), "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID, input.gatewayId(), "gatewayId accessor mismatch");
        assertFalse(input.forceRun(), "forceRun must be false");
    }

    // -------------------------------------------------------------------------
    // 4. CancelInput record accessors
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_recordAccessors_returnCorrectValues() {
        CancelInput input = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);

        assertEquals(PROCESS_ID, input.processId(), "processId accessor mismatch");
        assertEquals(EXPERIMENT_ID, input.experimentId(), "experimentId accessor mismatch");
        assertEquals(GATEWAY_ID, input.gatewayId(), "gatewayId accessor mismatch");
    }

    // -------------------------------------------------------------------------
    // 5. PreInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void preInput_serialization_roundTrip_preservesAllFields() throws Exception {
        PreInput original = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        PreInput deserialized = serializeDeserialize(original, PreInput.class);

        assertEquals(original.processId(), deserialized.processId(), "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(), deserialized.gatewayId(), "gatewayId lost during serialization");
        assertEquals(original.tokenId(), deserialized.tokenId(), "tokenId lost during serialization");
        assertEquals(original, deserialized, "Deserialized PreInput must equal the original");
    }

    @Test
    public void preInput_serialization_withNullTokenId_roundTrip() throws Exception {
        PreInput original = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, null);
        PreInput deserialized = serializeDeserialize(original, PreInput.class);

        assertEquals(null, deserialized.tokenId(), "null tokenId must survive serialization round-trip");
        assertEquals(original, deserialized);
    }

    // -------------------------------------------------------------------------
    // 6. PostInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void postInput_serialization_roundTrip_withForceRunTrue() throws Exception {
        PostInput original = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput deserialized = serializeDeserialize(original, PostInput.class);

        assertEquals(original.processId(), deserialized.processId(), "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(), deserialized.gatewayId(), "gatewayId lost during serialization");
        assertTrue(deserialized.forceRun(), "forceRun=true must survive serialization round-trip");
        assertEquals(original, deserialized, "Deserialized PostInput must equal the original");
    }

    @Test
    public void postInput_serialization_roundTrip_withForceRunFalse() throws Exception {
        PostInput original = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);
        PostInput deserialized = serializeDeserialize(original, PostInput.class);

        assertFalse(deserialized.forceRun(), "forceRun=false must survive serialization round-trip");
        assertEquals(original, deserialized, "Deserialized PostInput must equal the original");
    }

    // -------------------------------------------------------------------------
    // 7. CancelInput serialization round-trip
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_serialization_roundTrip_preservesAllFields() throws Exception {
        CancelInput original = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        CancelInput deserialized = serializeDeserialize(original, CancelInput.class);

        assertEquals(original.processId(), deserialized.processId(), "processId lost during serialization");
        assertEquals(original.experimentId(), deserialized.experimentId(), "experimentId lost during serialization");
        assertEquals(original.gatewayId(), deserialized.gatewayId(), "gatewayId lost during serialization");
        assertEquals(original, deserialized, "Deserialized CancelInput must equal the original");
    }

    // -------------------------------------------------------------------------
    // 8. PreInput equality
    // -------------------------------------------------------------------------

    @Test
    public void preInput_equality_twoIdenticalRecords_areEqual() {
        PreInput first = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        PreInput second = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertEquals(first, second, "Two PreInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal PreInputs must share the same hashCode");
    }

    @Test
    public void preInput_equality_differentProcessId_notEqual() {
        PreInput first = new PreInput("proc-A", EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        PreInput second = new PreInput("proc-B", EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);

        assertNotEquals(first, second, "PreInputs with different processId must not be equal");
    }

    @Test
    public void preInput_equality_differentTokenId_notEqual() {
        PreInput first = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, "token-X");
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
        PostInput first = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput second = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);

        assertEquals(first, second, "Two PostInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal PostInputs must share the same hashCode");
    }

    @Test
    public void postInput_equality_differentForceRun_notEqual() {
        PostInput withForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput withoutForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertNotEquals(withForce, withoutForce, "PostInputs differing only in forceRun must not be equal");
    }

    // -------------------------------------------------------------------------
    // 10. CancelInput equality
    // -------------------------------------------------------------------------

    @Test
    public void cancelInput_equality_twoIdenticalRecords_areEqual() {
        CancelInput first = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        CancelInput second = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);

        assertEquals(first, second, "Two CancelInputs with identical fields must be equal");
        assertEquals(first.hashCode(), second.hashCode(), "Equal CancelInputs must share the same hashCode");
    }

    @Test
    public void cancelInput_equality_differentGatewayId_notEqual() {
        CancelInput first = new CancelInput(PROCESS_ID, EXPERIMENT_ID, "gw-A");
        CancelInput second = new CancelInput(PROCESS_ID, EXPERIMENT_ID, "gw-B");

        assertNotEquals(first, second, "CancelInputs with different gatewayId must not be equal");
    }

    // -------------------------------------------------------------------------
    // 11. Activities interface — exactly 2 methods present
    // -------------------------------------------------------------------------

    @Test
    public void activitiesInterface_hasExactlyTwoMethods() {
        Method[] methods = Activities.class.getMethods();
        // Filter to only the methods declared on Activities itself (exclude Object methods)
        long count = Arrays.stream(methods)
                .filter(m -> m.getDeclaringClass().equals(Activities.class))
                .count();

        assertEquals(2, count, "Activities interface must declare exactly 2 methods but found: " + count);
    }

    @Test
    public void activitiesInterface_hasResolveResourceTypeMethod() {
        Set<String> methodNames = getActivitiesMethodNames();
        assertTrue(
                methodNames.contains("resolveResourceType"),
                "Activities interface must declare 'resolveResourceType' method");
    }

    @Test
    public void activitiesInterface_hasExecuteDagNodeMethod() {
        Set<String> methodNames = getActivitiesMethodNames();
        assertTrue(methodNames.contains("executeDagNode"), "Activities interface must declare 'executeDagNode' method");
    }

    @Test
    public void activitiesInterface_allTwoExpectedMethodsPresent() {
        Set<String> expected = Set.of("resolveResourceType", "executeDagNode");
        Set<String> actual = getActivitiesMethodNames();

        Set<String> missing = expected.stream().filter(m -> !actual.contains(m)).collect(Collectors.toSet());

        assertTrue(missing.isEmpty(), "Activities interface is missing expected methods: " + missing);
    }

    @Test
    public void activitiesInterface_doesNotContainOldStepMethods() {
        Set<String> methodNames = getActivitiesMethodNames();

        assertFalse(
                methodNames.contains("provisioning"),
                "Activities interface must not declare legacy 'provisioning' method");
        assertFalse(
                methodNames.contains("inputDataStaging"),
                "Activities interface must not declare legacy 'inputDataStaging' method");
        assertFalse(
                methodNames.contains("jobSubmission"),
                "Activities interface must not declare legacy 'jobSubmission' method");
        assertFalse(
                methodNames.contains("deprovisioning"),
                "Activities interface must not declare legacy 'deprovisioning' method");
        assertFalse(
                methodNames.contains("monitoring"), "Activities interface must not declare legacy 'monitoring' method");
        assertFalse(
                methodNames.contains("outputStaging"),
                "Activities interface must not declare legacy 'outputStaging' method");
        assertFalse(methodNames.contains("archive"), "Activities interface must not declare legacy 'archive' method");
        assertFalse(
                methodNames.contains("markFailed"), "Activities interface must not declare legacy 'markFailed' method");
        assertFalse(
                methodNames.contains("executePreDag"),
                "Activities interface must not declare replaced 'executePreDag' method");
        assertFalse(
                methodNames.contains("executePostDag"),
                "Activities interface must not declare replaced 'executePostDag' method");
        assertFalse(
                methodNames.contains("executeCancelDag"),
                "Activities interface must not declare replaced 'executeCancelDag' method");
    }

    // -------------------------------------------------------------------------
    // 12. Activities interface — method signatures via reflection
    // -------------------------------------------------------------------------

    @Test
    public void activitiesInterface_resolveResourceType_returnTypeIsComputeResourceType() throws Exception {
        Method method = Activities.class.getMethod("resolveResourceType", String.class);

        assertEquals(
                ComputeResourceType.class,
                method.getReturnType(),
                "'resolveResourceType' must return ComputeResourceType");
    }

    @Test
    public void activitiesInterface_resolveResourceType_takesOneStringParameter() throws Exception {
        Method method = Activities.class.getMethod("resolveResourceType", String.class);

        assertEquals(1, method.getParameterCount(), "'resolveResourceType' must accept exactly 1 parameter");
        assertEquals(
                String.class,
                method.getParameterTypes()[0],
                "'resolveResourceType' first parameter must be String (processId)");
    }

    @Test
    public void activitiesInterface_executeDagNode_returnTypeIsNodeResult() throws Exception {
        Method method = Activities.class.getMethod(
                "executeDagNode", String.class, String.class, String.class, String.class, Map.class, Map.class);

        assertEquals(NodeResult.class, method.getReturnType(), "'executeDagNode' must return NodeResult");
    }

    @Test
    public void activitiesInterface_executeDagNode_takesSixParameters() throws Exception {
        Method method = Activities.class.getMethod(
                "executeDagNode", String.class, String.class, String.class, String.class, Map.class, Map.class);

        assertEquals(6, method.getParameterCount(), "'executeDagNode' must accept exactly 6 parameters");
        assertEquals(
                String.class, method.getParameterTypes()[0], "'executeDagNode' parameter 0 must be String (processId)");
        assertEquals(
                String.class, method.getParameterTypes()[1], "'executeDagNode' parameter 1 must be String (gatewayId)");
        assertEquals(
                String.class, method.getParameterTypes()[2], "'executeDagNode' parameter 2 must be String (nodeId)");
        assertEquals(
                String.class,
                method.getParameterTypes()[3],
                "'executeDagNode' parameter 3 must be String (taskBeanName)");
        assertEquals(Map.class, method.getParameterTypes()[4], "'executeDagNode' parameter 4 must be Map (dagState)");
        assertEquals(
                Map.class, method.getParameterTypes()[5], "'executeDagNode' parameter 5 must be Map (nodeMetadata)");
    }

    @Test
    public void activitiesInterface_bothMethods_existWithCorrectSignatures() throws Exception {
        // resolveResourceType: ComputeResourceType f(String)
        Method resolveMethod = Activities.class.getMethod("resolveResourceType", String.class);
        assertEquals(
                ComputeResourceType.class,
                resolveMethod.getReturnType(),
                "resolveResourceType must return ComputeResourceType");
        assertEquals(1, resolveMethod.getParameterCount(), "resolveResourceType must accept exactly 1 parameter");

        // executeDagNode: NodeResult f(String, String, String, String, Map, Map)
        Method executeMethod = Activities.class.getMethod(
                "executeDagNode", String.class, String.class, String.class, String.class, Map.class, Map.class);
        assertEquals(NodeResult.class, executeMethod.getReturnType(), "executeDagNode must return NodeResult");
        assertEquals(6, executeMethod.getParameterCount(), "executeDagNode must accept exactly 6 parameters");
    }

    // -------------------------------------------------------------------------
    // 13. Record toString sanity checks
    // -------------------------------------------------------------------------

    @Test
    public void preInput_toString_containsAllFieldValues() {
        PreInput input = new PreInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, TOKEN_ID);
        String str = input.toString();

        assertTrue(str.contains(PROCESS_ID), "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID), "toString must contain gatewayId");
        assertTrue(str.contains(TOKEN_ID), "toString must contain tokenId");
    }

    @Test
    public void postInput_toString_containsAllFieldValues() {
        PostInput input = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);
        String str = input.toString();

        assertTrue(str.contains(PROCESS_ID), "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID), "toString must contain gatewayId");
    }

    @Test
    public void postInput_toString_containsForceRunValue() {
        PostInput withForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, true);
        PostInput withoutForce = new PostInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID, false);

        assertTrue(withForce.toString().contains("true"), "toString of forceRun=true PostInput must contain 'true'");
        assertTrue(
                withoutForce.toString().contains("false"), "toString of forceRun=false PostInput must contain 'false'");
    }

    @Test
    public void cancelInput_toString_containsAllFieldValues() {
        CancelInput input = new CancelInput(PROCESS_ID, EXPERIMENT_ID, GATEWAY_ID);
        String str = input.toString();

        assertTrue(str.contains(PROCESS_ID), "toString must contain processId");
        assertTrue(str.contains(EXPERIMENT_ID), "toString must contain experimentId");
        assertTrue(str.contains(GATEWAY_ID), "toString must contain gatewayId");
    }

    // -------------------------------------------------------------------------
    // 14. NodeResult record tests
    // -------------------------------------------------------------------------

    @Test
    public void nodeResult_recordAccessors_returnCorrectValues() {
        Map<String, String> output = Map.of("key", "value");
        NodeResult result = new NodeResult("success message", output);
        assertEquals("success message", result.message());
        assertEquals(output, result.output());
    }

    @Test
    public void nodeResult_emptyOutput() {
        NodeResult result = new NodeResult("done", Map.of());
        assertEquals("done", result.message());
        assertTrue(result.output().isEmpty());
    }

    @Test
    public void nodeResult_serialization_roundTrip() throws Exception {
        Map<String, String> output = new HashMap<>();
        output.put("jobId", "123");
        output.put("instanceId", "i-abc");
        NodeResult original = new NodeResult("completed", output);
        NodeResult deserialized = serializeDeserialize(original, NodeResult.class);
        assertEquals(original.message(), deserialized.message());
        assertEquals(original.output(), deserialized.output());
        assertEquals(original, deserialized);
    }

    @Test
    public void nodeResult_equality() {
        NodeResult a = new NodeResult("msg", Map.of("k", "v"));
        NodeResult b = new NodeResult("msg", Map.of("k", "v"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void nodeResult_toString_containsFields() {
        NodeResult result = new NodeResult("test message", Map.of("key", "val"));
        String str = result.toString();
        assertTrue(str.contains("test message"));
        assertTrue(str.contains("key"));
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
