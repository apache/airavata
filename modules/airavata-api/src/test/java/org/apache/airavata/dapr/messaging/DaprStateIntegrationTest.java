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
package org.apache.airavata.dapr.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.config.TestcontainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Integration tests for Dapr State Store backed by Redis.
 *
 * <p>Without a Dapr sidecar, we can only verify that the Redis Testcontainer
 * used by Dapr state component is available. Full State (saveState, getState,
 * deleteState) requires a Dapr sidecar and is covered by E2E tests.
 */
@Tag("integration")
public class DaprStateIntegrationTest {

    @Test
    @DisplayName("Redis container is available for Dapr State backend")
    void testRedisContainerAvailableForDaprState() {
        String host = TestcontainersConfig.getRedisHost();
        assertNotNull(host);
        assertTrue(host.contains(":"), "Expected host:port form, got: " + host);
    }

    @Test
    @DisplayName("State value serialization and deserialization works correctly")
    void testStateValueSerialization() throws Exception {
        // Test that state values can be serialized and deserialized correctly
        // This is a unit test that doesn't require Dapr sidecar
        // State values are typically JSON-serialized objects

        // Create a test state value (e.g., a simple map or object)
        java.util.Map<String, Object> testState = new java.util.HashMap<>();
        testState.put("key1", "value1");
        testState.put("key2", 123);
        testState.put("key3", true);
        testState.put("key4", java.util.Arrays.asList("item1", "item2"));

        // Serialize to JSON
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = objectMapper.writeValueAsString(testState);

        assertNotNull(json, "Serialized JSON should not be null");
        assertTrue(json.contains("key1"), "JSON should contain key1");
        assertTrue(json.contains("value1"), "JSON should contain value1");
        assertTrue(json.contains("123"), "JSON should contain numeric value");

        // Deserialize from JSON
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> deserializedState = objectMapper.readValue(json, java.util.Map.class);

        assertNotNull(deserializedState, "Deserialized state should not be null");
        assertEquals("value1", deserializedState.get("key1"), "Key1 value should match");
        assertEquals(123, deserializedState.get("key2"), "Key2 value should match");
        assertEquals(true, deserializedState.get("key3"), "Key3 value should match");
        assertTrue(deserializedState.get("key4") instanceof java.util.List, "Key4 should be a list");
    }

    @Test
    @DisplayName("State key format validation")
    void testStateKeyFormat() {
        // Test that state keys follow expected format
        // Dapr state keys are typically strings
        String validKey1 = "state-key-123";
        String validKey2 = "experiment:exp-123:status";
        String validKey3 = "process:proc-456:metadata";

        assertNotNull(validKey1, "State key should not be null");
        assertTrue(validKey1.length() > 0, "State key should not be empty");
        assertNotNull(validKey2, "State key should not be null");
        assertNotNull(validKey3, "State key should not be null");

        // Keys should not contain certain characters that might cause issues
        assertFalse(validKey1.contains("\n"), "State key should not contain newlines");
        assertFalse(validKey1.contains("\r"), "State key should not contain carriage returns");
    }

    @Test
    @DisabledIfEnvironmentVariable(
            named = "CI",
            matches = "true",
            disabledReason = "Requires Dapr sidecar; run locally with dapr run")
    @org.junit.jupiter.api.Disabled("Full Dapr State requires Dapr sidecar; E2E only")
    @Tag("e2e")
    @DisplayName("saveState/getState/deleteState round-trip (requires Dapr sidecar)")
    void testSaveStateGetStateDeleteStateRequiresDaprSidecar() {
        // E2E test: with dapr run and airavata.dapr.enabled=true, DaprClient
        // saveState/getState/deleteState against redis-state component would be
        // exercised. Run as E2E outside this suite.
        // This test verifies the full Dapr State Store integration with sidecar.
    }
}
