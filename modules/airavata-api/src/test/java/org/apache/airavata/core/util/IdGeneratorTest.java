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
package org.apache.airavata.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IdGenerator}.
 */
class IdGeneratorTest {

    // ========== ensureId ==========

    @Test
    void ensureId_nullInput_generatesUUID() {
        String result = IdGenerator.ensureId(null);
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void ensureId_emptyString_generatesUUID() {
        String result = IdGenerator.ensureId("");
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void ensureId_blankString_generatesUUID() {
        String result = IdGenerator.ensureId("   ");
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void ensureId_existingId_returnsSameId() {
        String existing = "existing-id";
        assertEquals(existing, IdGenerator.ensureId(existing));
    }

    @Test
    void ensureId_uuidInput_returnsSameUUID() {
        String uuid = UUID.randomUUID().toString();
        assertEquals(uuid, IdGenerator.ensureId(uuid));
    }

    // ========== getCurrentTimestamp ==========

    @Test
    void getCurrentTimestamp_returnsNonNull() {
        Timestamp ts = IdGenerator.getCurrentTimestamp();
        assertNotNull(ts);
    }

    @Test
    void getCurrentTimestamp_returnsPositiveTime() {
        Timestamp ts = IdGenerator.getCurrentTimestamp();
        assertTrue(ts.getTime() > 0);
    }

    // ========== getUniqueTimestamp ==========

    @Test
    void getUniqueTimestamp_consecutiveCallsAreMonotonicallyIncreasing() {
        Timestamp[] timestamps = new Timestamp[100];
        for (int i = 0; i < timestamps.length; i++) {
            timestamps[i] = IdGenerator.getUniqueTimestamp();
        }
        for (int i = 1; i < timestamps.length; i++) {
            long prev = timestamps[i - 1].getTime() * 1000 + timestamps[i - 1].getNanos() / 1000;
            long curr = timestamps[i].getTime() * 1000 + timestamps[i].getNanos() / 1000;
            assertTrue(curr >= prev,
                    "Timestamp at index " + i + " (" + curr + ") should be >= previous (" + prev + ")");
        }
    }

    @Test
    void getUniqueTimestamp_producesUniqueValues() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Timestamp ts = IdGenerator.getUniqueTimestamp();
            String key = ts.getTime() + ":" + ts.getNanos();
            assertTrue(seen.add(key), "Duplicate timestamp at iteration " + i + ": " + key);
        }
    }

    // ========== getTime ==========

    @Test
    void getTime_zeroInput_returnsCurrentTimestamp() {
        Timestamp ts = IdGenerator.getTime(0);
        assertNotNull(ts);
        assertTrue(ts.getTime() > 0);
    }

    @Test
    void getTime_negativeInput_returnsCurrentTimestamp() {
        Timestamp ts = IdGenerator.getTime(-1);
        assertNotNull(ts);
        assertTrue(ts.getTime() > 0);
    }

    @Test
    void getTime_positiveInput_returnsTimestampWithThatTime() {
        long epochMillis = 1700000000000L;
        Timestamp ts = IdGenerator.getTime(epochMillis);
        assertEquals(epochMillis, ts.getTime());
    }

    // ========== getId ==========

    @Test
    void getId_replacesSpacesAndSpecialChars() {
        String result = IdGenerator.getId("My App Name");
        assertTrue(result.startsWith("My_App_Name_"));
    }

    @Test
    void getId_replacesDotsAndSlashes() {
        String result = IdGenerator.getId("org.apache/test\\foo");
        assertTrue(result.startsWith("org_apache_test_foo_"));
    }

    @Test
    void getId_appendsUUID() {
        String result = IdGenerator.getId("test");
        // Format: name_UUID
        String[] parts = result.split("_", 2);
        assertEquals(2, parts.length);
        assertEquals("test", parts[0]);
        assertDoesNotThrow(() -> UUID.fromString(parts[1]));
    }
}
