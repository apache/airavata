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
 * Domain enum: DataType
 */
public enum DataType {
    STRING(0),
    INTEGER(1),
    FLOAT(2),
    URI(3),
    URI_COLLECTION(4),
    STDOUT(5),
    STDERR(6);

    private final int value;

    DataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DataType findByValue(int value) {
        switch (value) {
            case 0:
                return STRING;
            case 1:
                return INTEGER;
            case 2:
                return FLOAT;
            case 3:
                return URI;
            case 4:
                return URI_COLLECTION;
            case 5:
                return STDOUT;
            case 6:
                return STDERR;
            default:
                return null;
        }
    }
}
