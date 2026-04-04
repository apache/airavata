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
package org.apache.airavata.iam.mapper;

import org.apache.airavata.iam.model.GatewayGroupsEntity;
import org.apache.airavata.iam.model.TenantGatewayEntity;
import org.apache.airavata.iam.model.UserProfileEntity;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper extends CommonMapperConversions {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    // --- UserProfile ---
    UserProfile userProfileToModel(UserProfileEntity entity);

    UserProfileEntity userProfileToEntity(UserProfile model);

    // --- Gateway (tenant profile) ---
    Gateway gatewayToModel(TenantGatewayEntity entity);

    TenantGatewayEntity gatewayToEntity(Gateway model);

    // --- GatewayGroups ---
    GatewayGroups gatewayGroupsToModel(GatewayGroupsEntity entity);

    GatewayGroupsEntity gatewayGroupsToEntity(GatewayGroups model);
}
