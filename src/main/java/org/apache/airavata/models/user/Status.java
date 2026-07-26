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
package org.apache.airavata.models.user;

/**
 * Plain-POJO replacement for the generated {@code org.apache.airavata.model.user.proto.Status}.
 */
public enum Status {
    STATUS_UNKNOWN(0),
    ACTIVE(1),
    CONFIRMED(2),
    APPROVED(3),
    DELETED(4),
    DUPLICATE(5),
    GRACE_PERIOD(6),
    INVITED(7),
    DENIED(8),
    PENDING(9),
    PENDING_APPROVAL(10),
    PENDING_CONFIRMATION(11),
    SUSPENDED(12),
    DECLINED(13),
    EXPIRED(14);

    private final int number;

    Status(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static Status forNumber(int number) {
        for (Status value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
