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
package org.apache.airavata.common.exception;

import java.util.UUID;

/**
 * Utility class for generating standardized error codes.
 * 
 * <p>Error codes are used for tracking and correlating errors across the system.
 * They provide a unique identifier for each error occurrence, making it easier
 * to trace errors in logs and support requests.
 */
public class ErrorCodeGenerator {

    private ErrorCodeGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generate a unique error code.
     * Uses UUID to ensure uniqueness across the system.
     *
     * @return A unique error code string
     */
    public static String generateErrorCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate an error code with a prefix for categorization.
     *
     * @param prefix The prefix to categorize the error (e.g., "EXP", "PROC", "TASK")
     * @return A unique error code with prefix
     */
    public static String generateErrorCode(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString());
    }

    /**
     * Generate an error code with prefix and timestamp.
     *
     * @param prefix The prefix to categorize the error
     * @return A unique error code with prefix and timestamp
     */
    public static String generateErrorCodeWithTimestamp(String prefix) {
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%d-%s", prefix, timestamp, UUID.randomUUID().toString().substring(0, 8));
    }
}

