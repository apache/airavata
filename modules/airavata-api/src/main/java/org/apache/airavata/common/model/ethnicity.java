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
 * Domain enum: ethnicity. Only used by deprecated NSFDemographics; user/demographic data lives in Keycloak.
 *
 * @deprecated Not used for new development. Preserved for Thrift API compatibility only.
 */
@Deprecated(since = "1.0", forRemoval = false)
public enum ethnicity {
    HISPANIC_LATINO(0),
    NOT_HISPANIC_LATINO(1);

    private final int value;

    ethnicity(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ethnicity findByValue(int value) {
        switch (value) {
            case 0:
                return HISPANIC_LATINO;
            case 1:
                return NOT_HISPANIC_LATINO;
            default:
                return null;
        }
    }
}
