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
package org.apache.airavata.models.workflow;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.HandlerState}.
 */
public enum HandlerState {
    HANDLER_STATE_UNKNOWN(0),
    HANDLER_STATE_CREATED(1),
    HANDLER_STATE_VALIDATED(2),
    HANDLER_STATE_SCHEDULED(3),
    HANDLER_STATE_LAUNCHED(4),
    HANDLER_STATE_EXECUTING(5),
    HANDLER_STATE_CANCELING(6),
    HANDLER_STATE_CANCELED(7),
    HANDLER_STATE_COMPLETED(8),
    HANDLER_STATE_FAILED(9);

    private final int number;

    HandlerState(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static HandlerState forNumber(int number) {
        for (HandlerState value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
