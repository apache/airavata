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
package org.apache.airavata.models.experiment;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.experiment.proto.ExperimentSearchFields}.
 */
public enum ExperimentSearchFields {
    EXPERIMENT_SEARCH_FIELDS_UNKNOWN(0),
    EXPERIMENT_NAME(1),
    EXPERIMENT_DESC(2),
    APPLICATION_ID(3),
    FROM_DATE(4),
    TO_DATE(5),
    STATUS(6),
    PROJECT_ID(7),
    USER_NAME(8),
    JOB_ID(9);

    private final int number;

    ExperimentSearchFields(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static ExperimentSearchFields forNumber(int number) {
        for (ExperimentSearchFields value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
