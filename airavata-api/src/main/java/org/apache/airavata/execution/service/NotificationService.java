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
package org.apache.airavata.execution.service;

import java.util.List;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.apache.airavata.execution.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final RegistryServerHandler registryHandler;

    public NotificationService(RegistryServerHandler registryHandler) {
        this.registryHandler = registryHandler;
    }

    public String createNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return registryHandler.createNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while creating notification: " + e.getMessage(), e);
        }
    }

    public boolean updateNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return registryHandler.updateNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while updating notification: " + e.getMessage(), e);
        }
    }

    public boolean deleteNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return registryHandler.deleteNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting notification: " + e.getMessage(), e);
        }
    }

    public Notification getNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return registryHandler.getNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> getAllNotifications(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return registryHandler.getAllNotifications(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting all notifications: " + e.getMessage(), e);
        }
    }
}
