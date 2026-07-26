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
 * Type of db-entity being replicated.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.dbevent.proto.EntityType}.
 * Distinct from {@link org.apache.airavata.models.sharing.registry.EntityType}, which is an
 * unrelated, client-defined entity-type concept from a different proto namespace.
 */
public enum EntityType {
    ENTITY_TYPE_UNKNOWN(0),
    USER_PROFILE(1),
    TENANT(2),
    GROUP(3),
    PROJECT(4),
    EXPERIMENT(5),
    APPLICATION(6),
    SHARING(7),
    REGISTRY(8);

    private final int number;

    EntityType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static EntityType forNumber(int number) {
        for (EntityType value : values()) {
            if (value.number == number) {
                return value;
            }
        }
        return null;
    }
}
