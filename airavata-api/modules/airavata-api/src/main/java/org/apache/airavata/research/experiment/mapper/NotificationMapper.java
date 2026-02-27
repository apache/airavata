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
package org.apache.airavata.research.experiment.mapper;

import java.time.Instant;
import org.apache.airavata.core.mapper.EntityMapper;
import org.apache.airavata.research.experiment.entity.NotificationEntity;
import org.apache.airavata.research.experiment.model.Notification;
import org.springframework.stereotype.Component;

/**
 * Hand-written mapper for Notification: the model uses {@code long} epoch millis
 * while the entity uses {@link Instant}, requiring explicit conversion.
 */
@Component
public class NotificationMapper implements EntityMapper<NotificationEntity, Notification> {

    @Override
    public Notification toModel(NotificationEntity entity) {
        var model = new Notification();
        model.setNotificationId(entity.getNotificationId());
        model.setGatewayId(entity.getGatewayId());
        model.setTitle(entity.getTitle());
        model.setNotificationMessage(entity.getNotificationMessage());
        model.setCreatedAt(toEpochMilli(entity.getCreatedAt()));
        model.setPublishedAt(toEpochMilli(entity.getPublishedAt()));
        model.setExpiresAt(toEpochMilli(entity.getExpiresAt()));
        model.setPriority(entity.getPriority());
        return model;
    }

    @Override
    public NotificationEntity toEntity(Notification model) {
        var entity = new NotificationEntity();
        entity.setNotificationId(model.getNotificationId());
        entity.setGatewayId(model.getGatewayId());
        entity.setTitle(model.getTitle());
        entity.setNotificationMessage(model.getNotificationMessage());
        entity.setCreatedAt(toInstant(model.getCreatedAt()));
        entity.setPublishedAt(toInstant(model.getPublishedAt()));
        entity.setExpiresAt(toInstant(model.getExpiresAt()));
        entity.setPriority(model.getPriority());
        return entity;
    }

    private static long toEpochMilli(Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
    }

    private static Instant toInstant(long epochMilli) {
        return epochMilli > 0 ? Instant.ofEpochMilli(epochMilli) : null;
    }
}
