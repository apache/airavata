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

import com.github.dozermapper.core.Mapper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.registry.entities.expcatalog.NotificationEntity;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.expcatalog.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final Mapper mapper;

    public NotificationService(NotificationRepository notificationRepository, Mapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    public void deleteNotification(String notificationId) throws RegistryException {
        notificationRepository.deleteById(notificationId);
    }

    public Notification getNotification(String notificationId) throws RegistryException {
        NotificationEntity entity =
                notificationRepository.findById(notificationId).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, Notification.class);
    }

    public List<Notification> getAllGatewayNotifications(String gatewayId) throws RegistryException {
        List<NotificationEntity> entities = notificationRepository.findByGatewayId(gatewayId);
        return entities.stream().map(e -> mapper.map(e, Notification.class)).collect(Collectors.toList());
    }

    public String createNotification(Notification notification) throws RegistryException {
        NotificationEntity entity = mapper.map(notification, NotificationEntity.class);
        NotificationEntity saved = notificationRepository.save(entity);
        return saved.getNotificationId();
    }

    public void updateNotification(Notification notification) throws RegistryException {
        NotificationEntity entity = mapper.map(notification, NotificationEntity.class);
        notificationRepository.save(entity);
    }
}
