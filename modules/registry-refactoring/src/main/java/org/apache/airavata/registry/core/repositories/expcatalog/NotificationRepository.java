/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.registry.core.entities.expcatalog.NotificationEntity;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.apache.airavata.registry.core.utils.QueryConstants;
import org.apache.airavata.registry.cpi.RegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationRepository extends ExpCatAbstractRepository<Notification, NotificationEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(NotificationRepository.class);

    public NotificationRepository() { super(Notification.class, NotificationEntity.class); }

    protected String saveNotificationData(Notification notification) throws RegistryException {
        NotificationEntity notificationEntity = saveNotification(notification);
        return notificationEntity.getNotificationId();
    }

    protected NotificationEntity saveNotification(Notification notification) throws RegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        NotificationEntity notificationEntity = mapper.map(notification, NotificationEntity.class);

        if (notificationEntity.getCreationTime() != null) {
            logger.debug("Setting the Notification's creation time");
            notificationEntity.setCreationTime(new Timestamp(notification.getCreationTime()));
        }

        else {
            logger.debug("Setting the Notification's creation time to current time");
            notificationEntity.setCreationTime(new Timestamp(System.currentTimeMillis()));
        }

        if (notificationEntity.getPublishedTime() != null) {
            logger.debug("Setting the Notification's published time");
            notificationEntity.setPublishedTime(new Timestamp(notification.getPublishedTime()));
        }

        if (notificationEntity.getExpirationTime() != null) {
            logger.debug("Setting the Notification's expiration time");
            notificationEntity.setExpirationTime(new Timestamp(notification.getExpirationTime()));
        }

        return execute(entityManager -> entityManager.merge(notificationEntity));
    }

    public String createNotification(Notification notification) throws RegistryException {
        notification.setNotificationId(getNotificationId());
        return saveNotificationData(notification);
    }

    public void updateNotification(Notification notification) throws RegistryException {
        saveNotificationData(notification);
    }

    public Notification getNotification(String notificationId) throws RegistryException{
        return get(notificationId);
    }

    public List<Notification> getAllGatewayNotifications(String gatewayId) throws RegistryException {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put(DBConstants.Notification.GATEWAY_ID, gatewayId);
        List<Notification> notificationList = select(QueryConstants.GET_ALL_GATEWAY_NOTIFICATIONS, -1, 0, queryParameters);
        return notificationList;
    }

    private String getNotificationId() {
        return UUID.randomUUID().toString();
    }

    public void deleteNotification(String notificationId) throws RegistryException {
        delete(notificationId);
    }

}