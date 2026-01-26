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
 * Enum representing the type of parent entity for unified input/output data records.
 * This allows a single INPUT_DATA or OUTPUT_DATA table to store data for experiments,
 * processes, applications, and handlers.
 */
public enum DataObjectParentType {
    EXPERIMENT(0),
    PROCESS(1),
    APPLICATION(2),
    HANDLER(3);

    private final int value;

    DataObjectParentType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DataObjectParentType findByValue(int value) {
        switch (value) {
            case 0:
                return EXPERIMENT;
            case 1:
                return PROCESS;
            case 2:
                return APPLICATION;
            case 3:
                return HANDLER;
            default:
                return null;
        }
    }
}
