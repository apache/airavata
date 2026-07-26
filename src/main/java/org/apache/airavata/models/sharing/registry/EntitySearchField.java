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
package org.apache.airavata.models.sharing.registry;

/**
 * Fields that can be used to search entities.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.sharing.registry.models.proto.EntitySearchField}.
 */
public enum EntitySearchField {
    ENTITY_SEARCH_FIELD_UNKNOWN(0),
    NAME(1),
    DESCRIPTION(2),
    FULL_TEXT(3),
    PARRENT_ENTITY_ID(4),
    OWNER_ID(5),
    PERMISSION_TYPE_ID(6),
    CREATED_TIME(7),
    UPDATED_TIME(8),
    ENTITY_TYPE_ID(9),
    SHARED_COUNT(10);

    private final int number;

    EntitySearchField(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static EntitySearchField forNumber(int number) {
        for (EntitySearchField value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
