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

import org.apache.airavata.iam.model.GatewayEntity;
import org.apache.airavata.iam.model.GatewayGroupsEntity;
import org.apache.airavata.iam.model.UserProfileEntity;
import org.apache.airavata.mapper.CommonMapperConversions;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper extends CommonMapperConversions {

    ProfileMapper INSTANCE = Mappers.getMapper(ProfileMapper.class);

    // --- UserProfile ---
    UserProfile userProfileToModel(UserProfileEntity entity);

    UserProfileEntity userProfileToEntity(UserProfile model);

    // MapStruct does not match protobuf's repeated accessors (getEmailsList()/addAllEmails())
    // to the entity's `emails` property, so it silently drops every repeated-string field on
    // UserProfile in both directions. Map them explicitly here (List<String> -> repeated string
    // is an identity mapping, so no element converter is needed).
    @AfterMapping
    default void userProfileRepeatedToModel(UserProfileEntity entity, @MappingTarget UserProfile.Builder builder) {
        if (entity.getEmails() != null) builder.addAllEmails(entity.getEmails());
        if (entity.getPhones() != null) builder.addAllPhones(entity.getPhones());
        if (entity.getNationality() != null) builder.addAllNationality(entity.getNationality());
        if (entity.getLabeledURI() != null) builder.addAllLabeledUri(entity.getLabeledURI());
    }

    @AfterMapping
    default void userProfileRepeatedToEntity(UserProfile model, @MappingTarget UserProfileEntity entity) {
        if (!model.getEmailsList().isEmpty()) entity.setEmails(new java.util.ArrayList<>(model.getEmailsList()));
        if (!model.getPhonesList().isEmpty()) entity.setPhones(new java.util.ArrayList<>(model.getPhonesList()));
        if (!model.getNationalityList().isEmpty())
            entity.setNationality(new java.util.ArrayList<>(model.getNationalityList()));
        if (!model.getLabeledUriList().isEmpty())
            entity.setLabeledURI(new java.util.ArrayList<>(model.getLabeledUriList()));
    }

    // --- Gateway (tenant profile) ---
    Gateway gatewayToModel(GatewayEntity entity);

    GatewayEntity gatewayToEntity(Gateway model);

    // --- GatewayGroups ---
    GatewayGroups gatewayGroupsToModel(GatewayGroupsEntity entity);

    GatewayGroupsEntity gatewayGroupsToEntity(GatewayGroups model);
}
