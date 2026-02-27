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

/**
 * Utility for safe enum conversions, replacing scattered try-catch valueOf patterns.
 */
public final class EnumUtil {

    private EnumUtil() {}

    /**
     * Safely converts a string to an enum value, returning a default if the string
     * is null, blank, or does not match any enum constant.
     */
    public static <E extends Enum<E>> E safeValueOf(Class<E> enumClass, String value, E defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Safely converts a string to an enum value, returning null if no match.
     */
    public static <E extends Enum<E>> E safeValueOf(Class<E> enumClass, String value) {
        return safeValueOf(enumClass, value, null);
    }
}
