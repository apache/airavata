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
package org.apache.airavata.common.model;

/**
 * Enum representing the data type of a preference value.
 * Used to properly serialize/deserialize preference values stored as strings.
 */
public enum PreferenceValueType {
    /**
     * Plain string value. No conversion needed.
     */
    STRING(0),

    /**
     * Integer value. Stored as string, parsed to Integer/Long when read.
     */
    INTEGER(1),

    /**
     * Boolean value. Stored as "true" or "false" string.
     */
    BOOLEAN(2),

    /**
     * JSON value. Complex objects serialized as JSON strings.
     * Used for lists, maps, or nested objects.
     */
    JSON(3),

    /**
     * Timestamp value. Stored as ISO-8601 string or epoch millis.
     */
    TIMESTAMP(4);

    private final int value;

    PreferenceValueType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PreferenceValueType findByValue(int value) {
        switch (value) {
            case 0:
                return STRING;
            case 1:
                return INTEGER;
            case 2:
                return BOOLEAN;
            case 3:
                return JSON;
            case 4:
                return TIMESTAMP;
            default:
                return null;
        }
    }
}
