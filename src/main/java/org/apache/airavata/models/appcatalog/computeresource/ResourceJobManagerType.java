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
package org.apache.airavata.models.appcatalog.computeresource;

/**
 * Enumeration of local resource job manager types supported by Airavata.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.computeresource.proto.ResourceJobManagerType}.
 *
 * <ul>
 *   <li>{@code FORK}: Forking of commands without any job manager.
 *   <li>{@code PBS}: Job manager supporting the Portal Batch System (PBS) protocol.
 *   <li>{@code SLURM}: The Simple Linux Utility for Resource Management.
 *   <li>{@code UGE}: Univa Grid Engine, a variation of PBS implementation.
 *   <li>{@code LSF}: IBM Platform Load Sharing Facility.
 * </ul>
 */
public enum ResourceJobManagerType {
    RESOURCE_JOB_MANAGER_TYPE_UNKNOWN(0),
    FORK(1),
    PBS(2),
    SLURM(3),
    LSF(4),
    UGE(5),
    CLOUD(6),
    AIRAVATA_CUSTOM(7),
    HTCONDOR(8);

    private final int number;

    ResourceJobManagerType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static ResourceJobManagerType forNumber(int number) {
        for (ResourceJobManagerType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
