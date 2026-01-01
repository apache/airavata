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
package org.apache.airavata.registry.services;

import java.util.List;
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.registry.entities.expcatalog.NotificationEntity;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.mappers.NotificationMapper;
import org.apache.airavata.registry.repositories.expcatalog.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public void deleteNotification(String notificationId) throws RegistryException {
        notificationRepository.deleteById(notificationId);
    }

    public Notification getNotification(String notificationId) throws RegistryException {
        NotificationEntity entity =
                notificationRepository.findById(notificationId).orElse(null);
        if (entity == null) return null;
        return notificationMapper.toModel(entity);
    }

    public List<Notification> getAllGatewayNotifications(String gatewayId) throws RegistryException {
        List<NotificationEntity> entities = notificationRepository.findByGatewayId(gatewayId);
        return notificationMapper.toModelList(entities);
    }

    public String createNotification(Notification notification) throws RegistryException {
        NotificationEntity entity = notificationMapper.toEntity(notification);
        // Ensure required timestamps are set if mapper didn't set them
        if (entity.getCreationTime() == null) {
            entity.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        if (entity.getPublishedTime() == null) {
            entity.setPublishedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        if (entity.getExpirationTime() == null) {
            // Set expiration to 1 year from now if not set
            entity.setExpirationTime(new java.sql.Timestamp(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000));
        }
        NotificationEntity saved = notificationRepository.save(entity);
        return saved.getNotificationId();
    }

    public void updateNotification(Notification notification) throws RegistryException {
        NotificationEntity entity = notificationMapper.toEntity(notification);
        // Ensure required timestamps are set if mapper didn't set them (mapper returns null if model time is 0)
        if (entity.getCreationTime() == null) {
            // Try to preserve existing creation time, or use current time
            if (notification.getNotificationId() != null) {
                NotificationEntity existing = notificationRepository
                        .findById(notification.getNotificationId())
                        .orElse(null);
                if (existing != null && existing.getCreationTime() != null) {
                    entity.setCreationTime(existing.getCreationTime());
                } else {
                    entity.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
                }
            } else {
                entity.setCreationTime(new java.sql.Timestamp(System.currentTimeMillis()));
            }
        }
        if (entity.getPublishedTime() == null) {
            entity.setPublishedTime(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        if (entity.getExpirationTime() == null) {
            // Set expiration to 1 year from now if not set
            entity.setExpirationTime(new java.sql.Timestamp(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000));
        }
        notificationRepository.save(entity);
    }
}
