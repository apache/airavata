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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NotificationRepositoryTest extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRepositoryTest.class);

    private String testGateway = "testGateway";
    NotificationRepository notificationRepository;

    public NotificationRepositoryTest() {
        super(Database.EXP_CATALOG);
        notificationRepository = new NotificationRepository();
    }

    @Test
    public void NotificationRepositoryTest() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationRepository.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notification.setPriority(NotificationPriority.NORMAL);
        notificationRepository.updateNotification(notification);

        Notification retrievedNotification = notificationRepository.getNotification(notificationId);
        assertEquals(NotificationPriority.NORMAL, retrievedNotification.getPriority());

        assertTrue(notificationRepository.getAllGatewayNotifications(testGateway).size() == 1);

        notificationRepository.deleteNotification(notificationId);
    }

}
