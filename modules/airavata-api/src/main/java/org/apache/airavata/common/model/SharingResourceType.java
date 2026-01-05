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
 * Domain enum: SharingResourceType
 */
public enum SharingResourceType {
    PROJECT(0),
    EXPERIMENT(1),
    DATA(2),
    APPLICATION_DEPLOYMENT(3),
    GROUP_RESOURCE_PROFILE(4),
    CREDENTIAL_TOKEN(5),
    OTHER(6);

    private final int value;

    SharingResourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SharingResourceType findByValue(int value) {
        switch (value) {
            case 0:
                return PROJECT;
            case 1:
                return EXPERIMENT;
            case 2:
                return DATA;
            case 3:
                return APPLICATION_DEPLOYMENT;
            case 4:
                return GROUP_RESOURCE_PROFILE;
            case 5:
                return CREDENTIAL_TOKEN;
            case 6:
                return OTHER;
            default:
                return null;
        }
    }
}
