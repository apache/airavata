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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.NotificationPriority;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {org.apache.airavata.config.JpaConfig.class})
@TestPropertySource(locations = "classpath:airavata.properties")
public class NotificationRepositoryTest extends TestBase {

    private String testGateway = "testGateway";

    @Autowired
    NotificationService notificationService;

    public NotificationRepositoryTest() {
        super(Database.EXP_CATALOG);
    }

    @Test
    public void testNotificationRepository() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationService.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notification.setPriority(NotificationPriority.NORMAL);
        notificationService.updateNotification(notification);

        Notification retrievedNotification = notificationService.getNotification(notificationId);
        assertEquals(NotificationPriority.NORMAL, retrievedNotification.getPriority());

        assertTrue(notificationService.getAllGatewayNotifications(testGateway).size() == 1);

        notificationService.deleteNotification(notificationId);
    }
}
