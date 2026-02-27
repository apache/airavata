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
package org.apache.airavata.core.mapper;

import java.util.List;

/**
 * Generic contract for bidirectional entity-model mapping.
 *
 * <p>All domain mappers (whether MapStruct-generated or hand-written) should implement
 * this interface so that services can depend on a uniform mapping contract.
 *
 * @param <E> the JPA entity type
 * @param <M> the domain model type
 */
public interface EntityMapper<E, M> {

    M toModel(E entity);

    E toEntity(M model);

    default List<M> toModelList(List<E> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toModel).toList();
    }

    default List<E> toEntityList(List<M> models) {
        if (models == null) return List.of();
        return models.stream().map(this::toEntity).toList();
    }
}
