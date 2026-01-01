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
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.registry.entities.expcatalog.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between NotificationEntity and Notification.
 */
@Mapper(componentModel = "spring", config = EntityMapperConfig.class)
public interface NotificationMapper {

    @Mapping(
            target = "creationTime",
            expression = "java(entity.getCreationTime() != null ? entity.getCreationTime().getTime() : 0L)")
    @Mapping(
            target = "publishedTime",
            expression = "java(entity.getPublishedTime() != null ? entity.getPublishedTime().getTime() : 0L)")
    @Mapping(
            target = "expirationTime",
            expression = "java(entity.getExpirationTime() != null ? entity.getExpirationTime().getTime() : 0L)")
    Notification toModel(NotificationEntity entity);

    @Mapping(
            target = "creationTime",
            expression = "java(model.getCreationTime() > 0 ? new java.sql.Timestamp(model.getCreationTime()) : null)")
    @Mapping(
            target = "publishedTime",
            expression = "java(model.getPublishedTime() > 0 ? new java.sql.Timestamp(model.getPublishedTime()) : null)")
    @Mapping(
            target = "expirationTime",
            expression =
                    "java(model.getExpirationTime() > 0 ? new java.sql.Timestamp(model.getExpirationTime()) : null)")
    NotificationEntity toEntity(Notification model);

    List<Notification> toModelList(List<NotificationEntity> entities);

    List<NotificationEntity> toEntityList(List<Notification> models);
}
