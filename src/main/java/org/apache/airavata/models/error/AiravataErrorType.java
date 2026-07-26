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
package org.apache.airavata.models.error;

/**
 * A list of Airavata API Error Message Types.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.error.proto.AiravataErrorType}.
 *
 * <ul>
 *   <li>{@code AIRAVATA_ERROR_TYPE_UNKNOWN}: No information available about the error
 *   <li>{@code PERMISSION_DENIED}: Not permitted to perform action
 *   <li>{@code INTERNAL_ERROR}: Unexpected problem with the service
 *   <li>{@code AUTHENTICATION_FAILURE}: The client failed to authenticate.
 *   <li>{@code INVALID_AUTHORIZATION}: Security Token and/or Username and/or password is incorrect
 *   <li>{@code AUTHORIZATION_EXPIRED}: Authentication token expired
 *   <li>{@code UNKNOWN_GATEWAY_ID}: The gateway is not registered with Airavata.
 *   <li>{@code UNSUPPORTED_OPERATION}: Operation denied because it is currently unsupported.
 * </ul>
 */
public enum AiravataErrorType {
    AIRAVATA_ERROR_TYPE_UNKNOWN(0),
    PERMISSION_DENIED(1),
    INTERNAL_ERROR(2),
    AUTHENTICATION_FAILURE(3),
    INVALID_AUTHORIZATION(4),
    AUTHORIZATION_EXPIRED(5),
    UNKNOWN_GATEWAY_ID(6),
    UNSUPPORTED_OPERATION(7);

    private final int number;

    AiravataErrorType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static AiravataErrorType forNumber(int number) {
        for (AiravataErrorType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
