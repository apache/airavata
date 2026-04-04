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
package org.apache.airavata.research.service;

import java.util.List;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceException;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.model.workspace.proto.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final ProjectRegistry projectRegistry;

    public NotificationService(ProjectRegistry projectRegistry) {
        this.projectRegistry = projectRegistry;
    }

    public String createNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return projectRegistry.createNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while creating notification: " + e.getMessage(), e);
        }
    }

    public boolean updateNotification(RequestContext ctx, Notification notification) throws ServiceException {
        try {
            return projectRegistry.updateNotification(notification);
        } catch (Exception e) {
            throw new ServiceException("Error while updating notification: " + e.getMessage(), e);
        }
    }

    public boolean deleteNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return projectRegistry.deleteNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while deleting notification: " + e.getMessage(), e);
        }
    }

    public Notification getNotification(RequestContext ctx, String gatewayId, String notificationId)
            throws ServiceException {
        try {
            return projectRegistry.getNotification(gatewayId, notificationId);
        } catch (Exception e) {
            throw new ServiceException("Error while retrieving notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> getAllNotifications(RequestContext ctx, String gatewayId) throws ServiceException {
        try {
            return projectRegistry.getAllNotifications(gatewayId);
        } catch (Exception e) {
            throw new ServiceException("Error while getting all notifications: " + e.getMessage(), e);
        }
    }
}
