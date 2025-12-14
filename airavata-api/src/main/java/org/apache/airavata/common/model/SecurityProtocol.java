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
 * Domain enum: SecurityProtocol
 */
public enum SecurityProtocol {
    USERNAME_PASSWORD(0),
    SSH_KEYS(1),
    GSI(2),
    KERBEROS(3),
    OAUTH(4),
    LOCAL(5);

    private final int value;

    SecurityProtocol(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SecurityProtocol findByValue(int value) {
        switch (value) {
            case 0:
                return USERNAME_PASSWORD;
            case 1:
                return SSH_KEYS;
            case 2:
                return GSI;
            case 3:
                return KERBEROS;
            case 4:
                return OAUTH;
            case 5:
                return LOCAL;
            default:
                return null;
        }
    }
}
