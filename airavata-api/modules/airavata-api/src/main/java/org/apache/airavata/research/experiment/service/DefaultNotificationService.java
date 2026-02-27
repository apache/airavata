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
package org.apache.airavata.research.experiment.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.research.experiment.entity.NotificationEntity;
import org.apache.airavata.research.experiment.mapper.NotificationMapper;
import org.apache.airavata.research.experiment.model.Notification;
import org.apache.airavata.research.experiment.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DefaultNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public DefaultNotificationService(NotificationRepository notificationRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Override
    public void deleteNotification(String notificationId) throws RegistryException {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public Notification getNotification(String notificationId) throws RegistryException {
        return notificationRepository
                .findById(notificationId)
                .map(mapper::toModel)
                .orElse(null);
    }

    @Override
    public List<Notification> getAllGatewayNotifications(String gatewayId) throws RegistryException {
        return mapper.toModelList(notificationRepository.findByGatewayId(gatewayId));
    }

    @Override
    public String createNotification(Notification notification) throws RegistryException {
        NotificationEntity entity = mapper.toEntity(notification);
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getPublishedAt() == null) {
            entity.setPublishedAt(now);
        }
        if (entity.getExpiresAt() == null) {
            entity.setExpiresAt(now.plus(365, ChronoUnit.DAYS));
        }
        NotificationEntity saved = notificationRepository.save(entity);
        return saved.getNotificationId();
    }

    @Override
    public void updateNotification(Notification notification) throws RegistryException {
        NotificationEntity existing = null;
        if (notification.getNotificationId() != null) {
            existing = notificationRepository
                    .findById(notification.getNotificationId())
                    .orElse(null);
        }

        NotificationEntity entity = mapper.toEntity(notification);

        // Preserve creation time from existing entity
        if (existing != null && existing.getCreatedAt() != null) {
            entity.setCreatedAt(existing.getCreatedAt());
        } else if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }

        // Preserve published time if not set
        if (entity.getPublishedAt() == null) {
            if (existing != null && existing.getPublishedAt() != null) {
                entity.setPublishedAt(existing.getPublishedAt());
            } else {
                entity.setPublishedAt(Instant.now());
            }
        }

        // Set expirationTime from model if provided, otherwise preserve existing or use default
        if (notification.getExpiresAt() > 0) {
            entity.setExpiresAt(Instant.ofEpochMilli(notification.getExpiresAt()));
        } else if (entity.getExpiresAt() == null) {
            if (existing != null && existing.getExpiresAt() != null) {
                entity.setExpiresAt(existing.getExpiresAt());
            } else {
                entity.setExpiresAt(Instant.now().plus(365, ChronoUnit.DAYS));
            }
        }

        notificationRepository.save(entity);
        notificationRepository.flush();
    }
}
