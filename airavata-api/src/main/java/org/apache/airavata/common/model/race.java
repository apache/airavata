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
 * Domain enum: race
 */
public enum race {
    ASIAN(0),
    AMERICAN_INDIAN_OR_ALASKAN_NATIVE(1),
    BLACK_OR_AFRICAN_AMERICAN(2),
    NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER(3),
    WHITE(4);

    private final int value;

    race(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static race findByValue(int value) {
        switch (value) {
            case 0:
                return ASIAN;
            case 1:
                return AMERICAN_INDIAN_OR_ALASKAN_NATIVE;
            case 2:
                return BLACK_OR_AFRICAN_AMERICAN;
            case 3:
                return NATIVE_HAWAIIAN_OR_PACIFIC_ISLANDER;
            case 4:
                return WHITE;
            default:
                return null;
        }
    }
}
