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
package org.apache.airavata.models.parallelism;

/**
 * Enumeration of application parallelism supported by Airavata.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.parallelism.proto.
 * ApplicationParallelismType}. Constant names and numeric values are kept identical to the
 * original protobuf enum so persisted data (JPA {@code @Enumerated(EnumType.STRING)} columns)
 * and any code keying off {@link #getNumber()} keep working unchanged.
 */
public enum ApplicationParallelismType {
    APPLICATION_PARALLELISM_TYPE_UNKNOWN(0),

    /** Single processor applications without any parallelization. */
    SERIAL(1),

    /** Messaging Passing Interface. */
    MPI(2),

    /** Shared Memory Implementation. */
    OPENMP(3),

    /** Hybrid Applications. */
    OPENMP_MPI(4),

    CCM(5),

    CRAY_MPI(6);

    private final int number;

    ApplicationParallelismType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    /**
     * Looks up the constant with the given numeric value.
     *
     * @return the matching constant, or {@code null} if no constant has that number
     *         (mirrors the generated protobuf enum's {@code forNumber} behavior).
     */
    public static ApplicationParallelismType forNumber(int number) {
        for (ApplicationParallelismType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
