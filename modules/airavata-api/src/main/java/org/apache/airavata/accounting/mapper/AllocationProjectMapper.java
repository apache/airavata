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
package org.apache.airavata.accounting.mapper;

import org.apache.airavata.accounting.entity.AllocationProjectEntity;
import org.apache.airavata.accounting.model.AllocationProject;
import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.core.mapper.EntityMapper;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting between {@link AllocationProjectEntity} and {@link AllocationProject}.
 *
 * <p>All fields map 1:1 with no lazy associations. No explicit {@code @Mapping}
 * annotations are required. MapStruct generates a structurally complete implementation
 * using the shared {@link EntityMapperConfiguration}.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface AllocationProjectMapper extends EntityMapper<AllocationProjectEntity, AllocationProject> {}
