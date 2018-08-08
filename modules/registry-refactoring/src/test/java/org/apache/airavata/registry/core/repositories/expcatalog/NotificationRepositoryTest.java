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
import org.apache.airavata.model.workspace.NotificationPriority;
import org.apache.airavata.registry.core.repositories.common.TestBase;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRepositoryTest.class);

    private String testGateway = "testGateway";
    NotificationRepository notificationRepository;

    public NotificationRepositoryTest() {
        super(Database.EXP_CATALOG);
        notificationRepository = new NotificationRepository();
    }

    @Test
    public void createNotificationRepositoryTest() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationRepository.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        Notification savedNotification = notificationRepository.getNotification(notificationId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(notification, savedNotification, "__isset_bitfield", "creationTime"));
    }

    @Test
    public void updateNotificationRepositoryTest() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationRepository.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notification.setPriority(NotificationPriority.NORMAL);
        notificationRepository.updateNotification(notification);

        Notification savedNotification = notificationRepository.getNotification(notificationId);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(notification, savedNotification, "__isset_bitfield", "creationTime"));
    }

    @Test
    public void retrieveSingleNotificationRepositoryTest() throws RegistryException {
        List<Notification> actualNotificationList = new ArrayList<>();
        List<String> notificationIdList = new ArrayList<>();

        for (int i = 0 ; i < 5; i++) {
            Notification notification = new Notification();
            notification.setNotificationId("notificationId");
            notification.setGatewayId(testGateway);
            notification.setTitle("notificationTitle");
            notification.setNotificationMessage("notificationMessage");

            String notificationId = notificationRepository.createNotification(notification);
            assertEquals(notification.getNotificationId(), notificationId);

            notification.setPriority(NotificationPriority.NORMAL);
            notificationRepository.updateNotification(notification);

            notificationIdList.add(notificationId);
            actualNotificationList.add(notification);
        }

        for (int j = 0 ; j < 5; j++) {
            Notification savedNotification = notificationRepository.getNotification(notificationIdList.get(j));

            Notification actualNotification = actualNotificationList.get(j);
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualNotification, savedNotification, "__isset_bitfield", "creationTime"));
        }
    }

    @Test
    public void retrieveMultipleNotificationRepositoryTest() throws RegistryException {
        List<String> notificationIdList  = new ArrayList<>();
        HashMap<String, Notification> actualNotificationMap = new HashMap<>();

        for (int i = 0 ; i < 5; i++) {
            Notification notification = new Notification();
            notification.setNotificationId("notificationId");
            notification.setGatewayId(testGateway);
            notification.setTitle("notificationTitle");
            notification.setNotificationMessage("notificationMessage");

            String notificationId = notificationRepository.createNotification(notification);
            assertEquals(notification.getNotificationId(), notificationId);

            notification.setPriority(NotificationPriority.NORMAL);
            notificationRepository.updateNotification(notification);

            notificationIdList.add(notificationId);
            actualNotificationMap.put(notificationId, notification);
        }

        for (int j = 0 ; j < 5; j++) {
            Notification savedNotification = notificationRepository.getNotification(notificationIdList.get(j));
            Notification actualNotification = actualNotificationMap.get(notificationIdList.get(j));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(actualNotification, savedNotification, "__isset_bitfield", "creationTime"));
        }
    }

    @Test
    public void deleteNotificationRepositoryTest() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationRepository.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notificationRepository.deleteNotification(notificationId);
        assertNull(notificationRepository.getNotification(notificationId));
    }

}
