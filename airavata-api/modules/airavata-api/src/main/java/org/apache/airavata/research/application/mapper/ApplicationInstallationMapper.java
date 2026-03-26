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
package org.apache.airavata.research.application.mapper;

import org.apache.airavata.config.EntityMapperConfiguration;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.research.application.entity.ApplicationInstallationEntity;
import org.apache.airavata.research.application.model.ApplicationInstallation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = EntityMapperConfiguration.class)
public interface ApplicationInstallationMapper
        extends EntityMapper<ApplicationInstallationEntity, ApplicationInstallation> {

    @Override
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "resource", ignore = true)
    ApplicationInstallationEntity toEntity(ApplicationInstallation model);
}
