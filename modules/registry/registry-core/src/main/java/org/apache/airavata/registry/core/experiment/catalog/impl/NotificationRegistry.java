/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.impl;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.resources.NotificationResource;
import org.apache.airavata.registry.core.experiment.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.RegistryException;

import java.sql.Timestamp;
import java.util.*;

public class NotificationRegistry {

    public String createNotification(Notification notification) throws RegistryException {
        notification.setNotificationId(getNotificationId());
        updateNotification(notification);
        return notification.getNotificationId();
    }

    public void updateNotification(Notification notification) throws RegistryException {
        NotificationResource notificationResource = new NotificationResource();
        notificationResource.setNotificationId(notification.getNotificationId());
        notificationResource.setGatewayId(notification.getGatewayId());
        notificationResource.setTitle(notification.getTitle());
        notificationResource.setNotificationMessage(notification.getNotificationMessage());
        notificationResource.setPriority(notification.getPriority().toString());
        if(notification.getPublishedTime() != 0)
            notificationResource.setPublishedTime(new Timestamp(notification.getPublishedTime()));
        if(notification.getExpirationTime() != 0)
            notificationResource.setExpirationTime(new Timestamp(notification.getExpirationTime()));
        if(notification.getCreationTime() != 0)
            notificationResource.setCreationTime(new Timestamp(notification.getCreationTime()));
        else
            notificationResource.setCreationTime(new Timestamp(System.currentTimeMillis()));
        notificationResource.save();
    }

    public Notification getNotification(String notificationId) throws RegistryException{
        NotificationResource notificationResource = new NotificationResource();
        NotificationResource resource = (NotificationResource)notificationResource.get(ResourceType.NOTIFICATION, notificationId);
        if(resource != null){
            return ThriftDataModelConversion.getNotification(resource);
        }
        return null;
    }

    public void deleteNotification(String notificationId) throws RegistryException {
        NotificationResource notificationResource = new NotificationResource();
        notificationResource.remove(ResourceType.NOTIFICATION, notificationId);
    }

    public List<Notification> getAllGatewayNotifications(String gatewayId) throws RegistryException {
        List<Notification> notifications = new ArrayList<>();
        NotificationResource notificationResource = new NotificationResource();
        List<ExperimentCatResource> resources = notificationResource.getAllNotifications(gatewayId);
        if(resources != null && !resources.isEmpty()){
            for(ExperimentCatResource e : resources){
                notifications.add(ThriftDataModelConversion.getNotification((NotificationResource) e));
            }
        }
        Collections.sort(notifications, (o1, o2) -> (o2.getCreationTime() - o1.getCreationTime()) > 0 ? 1 : -1);
        return notifications;
    }

    private String getNotificationId (){
        return UUID.randomUUID().toString();
    }

}
