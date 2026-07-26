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
package org.apache.airavata.models.dbevent;

/**
 * Type of db-replication event.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.dbevent.proto.DBEventType}.
 */
public enum DBEventType {
    DB_EVENT_TYPE_UNKNOWN(0),
    PUBLISHER(1),
    SUBSCRIBER(2);

    private final int number;

    DBEventType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static DBEventType forNumber(int number) {
        for (DBEventType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
