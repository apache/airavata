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
package org.apache.airavata.test.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard Java implementation to replace Apache Commons Lang3 EqualsBuilder.reflectionEquals().
 * Performs deep field-by-field comparison using reflection, excluding specified fields.
 */
public class ReflectionEquals {

    /**
     * Compare two objects using reflection, excluding specified fields.
     *
     * @param lhs left-hand side object
     * @param rhs right-hand side object
     * @param excludeFields field names to exclude from comparison
     * @return true if objects are equal (field-by-field), false otherwise
     */
    public static boolean reflectionEquals(Object lhs, Object rhs, String... excludeFields) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }
        if (lhs.getClass() != rhs.getClass()) {
            return false;
        }

        Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeFields));

        Class<?> clazz = lhs.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (excludeSet.contains(field.getName())) {
                    continue;
                }

                // Skip static fields
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                try {
                    Object lhsValue = field.get(lhs);
                    Object rhsValue = field.get(rhs);

                    if (!equals(lhsValue, rhsValue)) {
                        return false;
                    }
                } catch (IllegalAccessException e) {
                    // If we can't access the field, skip it
                    continue;
                }
            }
            clazz = clazz.getSuperclass();
        }

        return true;
    }

    /**
     * Deep equals comparison for values.
     */
    private static boolean equals(Object lhs, Object rhs) {
        if (lhs == rhs) {
            return true;
        }
        if (lhs == null || rhs == null) {
            return false;
        }

        // Use object's equals method if available
        if (lhs.equals(rhs)) {
            return true;
        }

        // For arrays, use Arrays.deepEquals
        if (lhs.getClass().isArray() && rhs.getClass().isArray()) {
            return java.util.Arrays.deepEquals(new Object[] {lhs}, new Object[] {rhs});
        }

        return false;
    }
}
