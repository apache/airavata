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
package org.apache.airavata.registry.mappers;

import java.util.List;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.registry.entities.appcatalog.GroupComputeResourcePrefEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between GroupComputeResourcePrefEntity and GroupComputeResourcePreference model.
 * Handles polymorphic mapping for AWS and SLURM subtypes.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {SlurmComputeResourcePreferenceMapper.class, AwsComputeResourcePreferenceMapper.class})
public interface GroupComputeResourcePreferenceMapper {

    @Mapping(target = "overridebyAiravata", expression = "java(entity.getOverridebyAiravata() != 0)")
    @Mapping(target = "resourceType", ignore = true) // Set in @AfterMapping
    @Mapping(target = "specificPreferences", ignore = true) // Set in @AfterMapping
    GroupComputeResourcePreference toModel(GroupComputeResourcePrefEntity entity);

    // Note: toEntity is not provided here because GroupComputeResourcePrefEntity is abstract.
    // The service layer creates the appropriate concrete entity type (AWSGroupComputeResourcePrefEntity
    // or SlurmGroupComputeResourcePrefEntity) based on the resourceType, then uses the specific mappers
    // (AwsComputeResourcePreferenceMapper or SlurmComputeResourcePreferenceMapper) to map the fields.

    // Note: Polymorphic mapping (AWS vs SLURM) is handled in the service layer
    // because MapStruct cannot easily create different entity types based on discriminator values.
    // The service layer creates the appropriate entity type (AWSGroupComputeResourcePrefEntity or
    // SlurmGroupComputeResourcePrefEntity) and then maps the base fields.

    List<GroupComputeResourcePreference> toModelList(List<GroupComputeResourcePrefEntity> entities);

    // Note: toEntityList is not provided here because GroupComputeResourcePrefEntity is abstract.
    // The service layer creates the appropriate concrete entity types and maps them individually.
}
