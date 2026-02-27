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

import java.time.Instant;
import java.util.UUID;

public class IdGenerator {

    private static volatile long lastTimestampMillis = 0;
    private static volatile int lastMicrosecondFraction = 0;
    private static final Object timestampLock = new Object();

    /**
     * Gets the current timestamp with microsecond precision.
     * This method uses getUniqueTimestamp() internally to ensure consistency
     * and maximum precision across the codebase.
     *
     * @return An Instant with microsecond precision
     */
    public static Instant getCurrentTimestamp() {
        return getUniqueTimestamp();
    }

    /**
     * Converts a long time value to an Instant.
     * If the time is 0 or negative, returns the current unique timestamp.
     * Otherwise, creates an Instant from the provided time value.
     *
     * @param time Time in milliseconds since epoch
     * @return An Instant object
     */
    public static Instant getTime(long time) {
        if (time == 0 || time < 0) {
            return getUniqueTimestamp();
        }
        return Instant.ofEpochMilli(time);
    }

    /**
     * Gets a unique timestamp with microsecond precision that ensures each call returns a different value.
     * Uses System.currentTimeMillis() with System.nanoTime() for precise microsecond tracking.
     * The database column is TIMESTAMP(6) which supports microsecond precision.
     *
     * @return An Instant with microsecond precision that is guaranteed to be unique and monotonically increasing
     */
    public static Instant getUniqueTimestamp() {
        synchronized (timestampLock) {
            long currentTimeMillis = System.currentTimeMillis();
            // Use nanoTime to get precise sub-millisecond timing
            // nanoTime is relative, but we can use it to ensure uniqueness within the same millisecond
            long nanoTime = System.nanoTime();
            // Extract microsecond fraction (0-999) from nanoseconds
            // We use modulo to get a value between 0 and 999999, then divide by 1000 to get microseconds
            int microsecondFraction = (int) ((nanoTime % 1_000_000) / 1000);

            // Ensure timestamp is always increasing, even if called in rapid succession
            if (currentTimeMillis <= lastTimestampMillis) {
                // Same millisecond, or real time hasn't caught up to our incremented value.
                // Either way, keep incrementing from where we are.
                if (currentTimeMillis == lastTimestampMillis && microsecondFraction > lastMicrosecondFraction) {
                    // nanoTime advanced within the same millisecond, use it
                    lastMicrosecondFraction = microsecondFraction;
                } else {
                    // Increment manually to stay monotonic
                    lastMicrosecondFraction = (lastMicrosecondFraction + 1) % 1000;
                    if (lastMicrosecondFraction == 0) {
                        // Wrapped around all 1000 microseconds, increment millisecond
                        lastTimestampMillis++;
                    }
                }
            } else {
                // New millisecond - use current microsecond fraction
                lastTimestampMillis = currentTimeMillis;
                lastMicrosecondFraction = microsecondFraction;
            }

            // Create Instant with microsecond precision
            // Instant.ofEpochSecond takes seconds + nanoAdjustment
            long epochSeconds = lastTimestampMillis / 1000;
            int remainderMillisNanos = (int) (lastTimestampMillis % 1000) * 1_000_000;
            int microsNanos = lastMicrosecondFraction * 1000;
            return Instant.ofEpochSecond(epochSeconds, remainderMillisNanos + microsNanos);
        }
    }

    /**
     * Returns the given ID if it is non-null and non-blank, otherwise generates a new UUID.
     *
     * <p>Consolidates the repeated null-check + UUID pattern used across service create methods.
     *
     * @param id the candidate ID (may be null or blank)
     * @return the original ID or a freshly generated UUID string
     */
    public static String ensureId(String id) {
        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }

    public static String getId(String name) {
        String id = name.trim().replaceAll("\\s|\\.|/|\\\\", "_");
        return id + "_" + UUID.randomUUID();
    }
}
