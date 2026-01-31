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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.entities.replicacatalog.DataProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between DataProductEntity and DataProductModel.
 * Handles catalog metadata fields, primary storage, authors, and tags.
 */
@Mapper(
        componentModel = "spring",
        config = EntityMapperConfig.class,
        uses = {DataReplicaLocationMapper.class})
public interface DataProductMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "lastModifiedTime",
            expression = "java(entity.getLastModifiedTime() != null ? entity.getLastModifiedTime().getTime() : 0L)")
    @Mapping(
            target = "updatedAt",
            expression = "java(entity.getUpdatedAt() != null ? entity.getUpdatedAt().getTime() : 0L)")
    @Mapping(
            target = "scope",
            expression = "java(entity.getResourceScope() != null ? entity.getResourceScope().name() : null)")
    @Mapping(
            target = "status",
            expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(
            target = "privacy",
            expression = "java(entity.getPrivacy() != null ? entity.getPrivacy().name() : null)")
    @Mapping(
            target = "tags",
            expression = "java(org.apache.airavata.registry.mappers.DataProductMapper.tagStringsToTags(entity.getTags()))")
    DataProductModel toModel(DataProductEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "lastModifiedTime",
            expression =
                    "java(model.getLastModifiedTime() > 0 ? new java.sql.Timestamp(model.getLastModifiedTime()) : null)")
    @Mapping(
            target = "updatedAt",
            expression = "java(model.getUpdatedAt() > 0 ? new java.sql.Timestamp(model.getUpdatedAt()) : null)")
    @Mapping(
            target = "resourceScope",
            expression = "java(org.apache.airavata.registry.mappers.DataProductMapper.scopeToResourceScope(model.getScope()))")
    @Mapping(
            target = "status",
            expression = "java(org.apache.airavata.registry.mappers.DataProductMapper.statusStringToEnum(model.getStatus()))")
    @Mapping(
            target = "privacy",
            expression = "java(org.apache.airavata.registry.mappers.DataProductMapper.privacyStringToEnum(model.getPrivacy()))")
    @Mapping(
            target = "tags",
            expression = "java(org.apache.airavata.registry.mappers.DataProductMapper.tagsToTagStrings(model.getTags()))")
    DataProductEntity toEntity(DataProductModel model);

    List<DataProductModel> toModelList(List<DataProductEntity> entities);

    List<DataProductEntity> toEntityList(List<DataProductModel> models);

    static List<DataProductModel.Tag> tagStringsToTags(List<String> tagStrings) {
        if (tagStrings == null || tagStrings.isEmpty()) {
            return new ArrayList<>();
        }
        return tagStrings.stream()
                .map(s -> {
                    var t = new DataProductModel.Tag();
                    t.setId(s);
                    t.setName(s);
                    return t;
                })
                .collect(Collectors.toList());
    }

    static List<String> tagsToTagStrings(List<DataProductModel.Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }
        return tags.stream()
                .map(t -> t.getId() != null ? t.getId() : t.getName())
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());
    }

    static DataProductEntity.ResourceScope scopeToResourceScope(String scope) {
        if (scope == null || scope.isEmpty()) {
            return DataProductEntity.ResourceScope.USER;
        }
        try {
            return DataProductEntity.ResourceScope.valueOf(scope);
        } catch (IllegalArgumentException e) {
            return DataProductEntity.ResourceScope.USER;
        }
    }

    static DataProductEntity.ResourceStatus statusStringToEnum(String status) {
        if (status == null || status.isEmpty()) {
            return DataProductEntity.ResourceStatus.NONE;
        }
        try {
            return DataProductEntity.ResourceStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return DataProductEntity.ResourceStatus.NONE;
        }
    }

    static DataProductEntity.Privacy privacyStringToEnum(String privacy) {
        if (privacy == null || privacy.isEmpty()) {
            return DataProductEntity.Privacy.PRIVATE;
        }
        try {
            return DataProductEntity.Privacy.valueOf(privacy);
        } catch (IllegalArgumentException e) {
            return DataProductEntity.Privacy.PRIVATE;
        }
    }
}
