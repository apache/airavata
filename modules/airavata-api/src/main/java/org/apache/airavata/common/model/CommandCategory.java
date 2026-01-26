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
 * Enum representing the category of commands in the unified JOB_MANAGER_COMMAND table.
 * This allows consolidating JobManagerCommand and ParallelismCommand into a single entity.
 */
public enum CommandCategory {
    JOB_MANAGER(0),
    PARALLELISM(1);

    private final int value;

    CommandCategory(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CommandCategory findByValue(int value) {
        switch (value) {
            case 0:
                return JOB_MANAGER;
            case 1:
                return PARALLELISM;
            default:
                return null;
        }
    }
}
