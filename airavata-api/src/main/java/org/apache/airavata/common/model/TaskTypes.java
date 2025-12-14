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
 * Domain enum: TaskTypes
 */
public enum TaskTypes {
    ENV_SETUP(0),
    DATA_STAGING(1),
    JOB_SUBMISSION(2),
    ENV_CLEANUP(3),
    MONITORING(4),
    OUTPUT_FETCHING(5);

    private final int value;

    TaskTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskTypes findByValue(int value) {
        switch (value) {
            case 0:
                return ENV_SETUP;
            case 1:
                return DATA_STAGING;
            case 2:
                return JOB_SUBMISSION;
            case 3:
                return ENV_CLEANUP;
            case 4:
                return MONITORING;
            case 5:
                return OUTPUT_FETCHING;
            default:
                return null;
        }
    }
}
