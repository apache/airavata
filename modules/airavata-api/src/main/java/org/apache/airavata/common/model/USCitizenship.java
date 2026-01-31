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
 * Domain enum: USCitizenship. Only used by deprecated NSFDemographics; user/demographic data lives in Keycloak.
 *
 * @deprecated Not used for new development. Preserved for Thrift API compatibility only.
 */
@Deprecated(since = "1.0", forRemoval = false)
public enum USCitizenship {
    US_CITIZEN(0),
    US_PERMANENT_RESIDENT(1),
    OTHER_NON_US_CITIZEN(2);

    private final int value;

    USCitizenship(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static USCitizenship findByValue(int value) {
        switch (value) {
            case 0:
                return US_CITIZEN;
            case 1:
                return US_PERMANENT_RESIDENT;
            case 2:
                return OTHER_NON_US_CITIZEN;
            default:
                return null;
        }
    }
}
