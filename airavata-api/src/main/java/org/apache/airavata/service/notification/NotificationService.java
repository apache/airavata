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
package org.apache.airavata.service.notification;

import java.util.List;
import org.apache.airavata.common.exception.AiravataErrorType;
import org.apache.airavata.common.exception.AiravataSystemException;
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.registry.exception.RegistryServiceException;
import org.apache.airavata.service.registry.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for notification management operations.
 */
@Service("notificationServiceFacade")
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final RegistryService registryService;

    public NotificationService(RegistryService registryService) {
        this.registryService = registryService;
    }

    private AiravataSystemException airavataSystemException(
            AiravataErrorType errorType, String message, Throwable cause) {
        return org.apache.airavata.common.exception.ExceptionHandlerUtil.wrapAsAiravataException(
                errorType, message, cause);
    }

    public String createNotification(Notification notification) throws AiravataSystemException {
        try {
            return registryService.createNotification(notification);
        } catch (RegistryServiceException e) {
            String msg = "Error while creating notification: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean updateNotification(Notification notification) throws AiravataSystemException {
        try {
            return registryService.updateNotification(notification);
        } catch (RegistryServiceException e) {
            String msg = "Error while updating notification: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public boolean deleteNotification(String gatewayId, String notificationId) throws AiravataSystemException {
        try {
            return registryService.deleteNotification(gatewayId, notificationId);
        } catch (RegistryServiceException e) {
            String msg = "Error while deleting notification: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public Notification getNotification(String gatewayId, String notificationId) throws AiravataSystemException {
        try {
            return registryService.getNotification(gatewayId, notificationId);
        } catch (RegistryServiceException e) {
            String msg = "Error while retrieving notification: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }

    public List<Notification> getAllNotifications(String gatewayId) throws AiravataSystemException {
        try {
            return registryService.getAllNotifications(gatewayId);
        } catch (RegistryServiceException e) {
            String msg = "Error while getting all notifications: " + e.getMessage();
            logger.error(msg, e);
            throw airavataSystemException(AiravataErrorType.INTERNAL_ERROR, msg, e);
        }
    }
}
