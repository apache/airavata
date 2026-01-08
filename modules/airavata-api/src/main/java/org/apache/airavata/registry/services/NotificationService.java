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

import java.sql.Timestamp;
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
            entity.setCreationTime(org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp());
        }
        if (entity.getPublishedTime() == null) {
            entity.setPublishedTime(org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp());
        }
        if (entity.getExpirationTime() == null) {
            // Set expiration to 1 year from now if not set
            Timestamp now = org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp();
            entity.setExpirationTime(new java.sql.Timestamp(now.getTime() + 365L * 24 * 60 * 60 * 1000));
        }
        NotificationEntity saved = notificationRepository.save(entity);
        return saved.getNotificationId();
    }

    public void updateNotification(Notification notification) throws RegistryException {
        // Load existing entity first to preserve creation time and other fields
        NotificationEntity existing = null;
        if (notification.getNotificationId() != null) {
            existing = notificationRepository
                    .findById(notification.getNotificationId())
                    .orElse(null);
        }
        
        // Map the model to entity - this will set all fields from the model
        NotificationEntity entity = notificationMapper.toEntity(notification);
        
        // Preserve creation time from existing entity (business rule)
        if (existing != null && existing.getCreationTime() != null) {
            entity.setCreationTime(existing.getCreationTime());
        } else if (entity.getCreationTime() == null) {
            entity.setCreationTime(org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp());
        }
        
        // Preserve published time if model doesn't have it set
        if (entity.getPublishedTime() == null) {
            if (existing != null && existing.getPublishedTime() != null) {
                entity.setPublishedTime(existing.getPublishedTime());
            } else {
                entity.setPublishedTime(org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp());
            }
        }
        
        // Set expirationTime from model if provided, otherwise preserve existing or use default
        if (notification.getExpirationTime() > 0) {
            // Model explicitly sets expirationTime - always use it (override any existing value)
            entity.setExpirationTime(new java.sql.Timestamp(notification.getExpirationTime()));
        } else if (entity.getExpirationTime() == null) {
            // No expirationTime in model - preserve existing or set default
            if (existing != null && existing.getExpirationTime() != null) {
                entity.setExpirationTime(existing.getExpirationTime());
            } else {
                // Set expiration to 1 year from now if not set
                Timestamp now = org.apache.airavata.common.utils.AiravataUtils.getUniqueTimestamp();
                entity.setExpirationTime(new java.sql.Timestamp(now.getTime() + 365L * 24 * 60 * 60 * 1000));
            }
        }
        
        notificationRepository.save(entity);
        notificationRepository.flush(); // Ensure changes are persisted immediately
    }
}
