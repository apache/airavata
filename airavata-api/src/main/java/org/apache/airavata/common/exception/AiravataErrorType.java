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
package org.apache.airavata.common.exception;

/**
 * Domain enum: AiravataErrorType
 */
public enum AiravataErrorType {
    UNKNOWN(0),
    PERMISSION_DENIED(1),
    INTERNAL_ERROR(2),
    AUTHENTICATION_FAILURE(3),
    INVALID_AUTHORIZATION(4),
    AUTHORIZATION_EXPIRED(5),
    UNKNOWN_GATEWAY_ID(6),
    UNSUPPORTED_OPERATION(7);

    private final int value;

    AiravataErrorType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AiravataErrorType findByValue(int value) {
        switch (value) {
            case 0:
                return UNKNOWN;
            case 1:
                return PERMISSION_DENIED;
            case 2:
                return INTERNAL_ERROR;
            case 3:
                return AUTHENTICATION_FAILURE;
            case 4:
                return INVALID_AUTHORIZATION;
            case 5:
                return AUTHORIZATION_EXPIRED;
            case 6:
                return UNKNOWN_GATEWAY_ID;
            case 7:
                return UNSUPPORTED_OPERATION;
            default:
                return null;
        }
    }
}
