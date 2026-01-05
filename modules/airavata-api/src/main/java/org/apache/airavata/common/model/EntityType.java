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
 * Domain enum: EntityType
 */
public enum EntityType {
    USER_PROFILE(0),
    TENANT(1),
    GROUP(2),
    PROJECT(3),
    EXPERIMENT(4),
    APPLICATION(5),
    SHARING(6),
    REGISTRY(7);

    private final int value;

    EntityType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EntityType findByValue(int value) {
        switch (value) {
            case 0:
                return USER_PROFILE;
            case 1:
                return TENANT;
            case 2:
                return GROUP;
            case 3:
                return PROJECT;
            case 4:
                return EXPERIMENT;
            case 5:
                return APPLICATION;
            case 6:
                return SHARING;
            case 7:
                return REGISTRY;
            default:
                return null;
        }
    }
}
