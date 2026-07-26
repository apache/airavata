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
package org.apache.airavata.models.application.io;

/**
 * Data Types supported in Airavata. The primitive data types.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.application.io.proto.DataType}.
 */
public enum DataType {
    DATA_TYPE_UNKNOWN(0),
    STRING(1),
    INTEGER(2),
    FLOAT(3),
    URI(4),
    URI_COLLECTION(5),
    STDOUT(6),
    STDERR(7);

    private final int number;

    DataType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static DataType forNumber(int number) {
        for (DataType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
