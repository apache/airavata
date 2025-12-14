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
 * Domain enum: ResourcePermissionType
 */
public enum ResourcePermissionType {
    WRITE(0),
    READ(1),
    OWNER(2),
    MANAGE_SHARING(3);

    private final int value;

    ResourcePermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ResourcePermissionType findByValue(int value) {
        switch (value) {
            case 0:
                return WRITE;
            case 1:
                return READ;
            case 2:
                return OWNER;
            case 3:
                return MANAGE_SHARING;
            default:
                return null;
        }
    }
}
