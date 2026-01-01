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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.ComputeResourcePreference;
import org.apache.airavata.registry.entities.appcatalog.ComputeResourcePreferenceEntity;
import org.apache.airavata.registry.entities.appcatalog.SSHAccountProvisionerConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between ComputeResourcePreferenceEntity and ComputeResourcePreference.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface ComputeResourcePreferenceMapper {

    @Mapping(target = "overridebyAiravata", expression = "java(entity.isOverridebyAiravata())")
    @Mapping(
            target = "reservationStartTime",
            expression =
                    "java(entity.getReservationStartTime() != null ? entity.getReservationStartTime().getTime() : 0L)")
    @Mapping(
            target = "reservationEndTime",
            expression = "java(entity.getReservationEndTime() != null ? entity.getReservationEndTime().getTime() : 0L)")
    @Mapping(target = "sshAccountProvisionerConfig", ignore = true) // Handled manually in service layer
    ComputeResourcePreference toModel(ComputeResourcePreferenceEntity entity);

    @Mapping(target = "overridebyAiravata", expression = "java(model.getOverridebyAiravata())")
    @Mapping(
            target = "reservationStartTime",
            expression =
                    "java(model.getReservationStartTime() > 0 ? new java.sql.Timestamp(model.getReservationStartTime()) : null)")
    @Mapping(
            target = "reservationEndTime",
            expression =
                    "java(model.getReservationEndTime() > 0 ? new java.sql.Timestamp(model.getReservationEndTime()) : null)")
    @Mapping(target = "sshAccountProvisionerConfigurations", ignore = true) // Handled manually in service layer
    @Mapping(target = "gatewayProfileResource", ignore = true) // Set by parent entity
    @Mapping(target = "gatewayId", ignore = true) // Set by parent entity
    ComputeResourcePreferenceEntity toEntity(ComputeResourcePreference model);

    List<ComputeResourcePreference> toModelList(List<ComputeResourcePreferenceEntity> entities);

    List<ComputeResourcePreferenceEntity> toEntityList(List<ComputeResourcePreference> models);

    default Map<String, String> mapSshConfig(List<SSHAccountProvisionerConfiguration> configs) {
        if (configs == null) return null;
        return configs.stream()
                .collect(Collectors.toMap(
                        SSHAccountProvisionerConfiguration::getConfigName,
                        SSHAccountProvisionerConfiguration::getConfigValue,
                        (v1, v2) -> v1,
                        HashMap::new));
    }
}
