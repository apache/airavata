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
package org.apache.airavata.research.experiment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.config.TestBase;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.research.experiment.model.Notification;
import org.apache.airavata.research.experiment.model.NotificationPriority;
import org.apache.airavata.research.experiment.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;

/**
 * Integration tests for NotificationRepository.
 */
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class NotificationRepositoryTest extends TestBase {

    private final NotificationService notificationService;
    private String testGatewayId;

    public NotificationRepositoryTest(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @BeforeEach
    public void setUp() {
        testGatewayId = "testGateway-" + java.util.UUID.randomUUID().toString();
    }

    @Test
    public void testNotificationRepository_Create_WithAllFields() throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId-1");
        notification.setGatewayId(testGatewayId);
        notification.setTitle("Test Notification Title");
        notification.setNotificationMessage("This is a comprehensive test notification message");
        notification.setPriority(NotificationPriority.HIGH);
        long currentTime = IdGenerator.getUniqueTimestamp().toEpochMilli();
        notification.setCreatedAt(currentTime);
        notification.setPublishedAt(currentTime);
        notification.setExpiresAt(currentTime + 86400000L);

        String notificationId = notificationService.createNotification(notification);
        assertNotNull(notificationId);
        assertEquals(notification.getNotificationId(), notificationId);

        Notification retrieved = notificationService.getNotification(notificationId);
        assertNotNull(retrieved);
        assertEquals(testGatewayId, retrieved.getGatewayId());
        assertEquals("Test Notification Title", retrieved.getTitle());
        assertEquals(NotificationPriority.HIGH, retrieved.getPriority());

        notificationService.deleteNotification(notificationId);
        assertNull(notificationService.getNotification(notificationId));
    }

    @Test
    public void testNotificationRepository_Create_MultipleNotificationsPerGateway() throws Exception {
        String notificationId1 = createTestNotification("notif-1", "Title 1", NotificationPriority.LOW);
        String notificationId2 = createTestNotification("notif-2", "Title 2", NotificationPriority.NORMAL);
        String notificationId3 = createTestNotification("notif-3", "Title 3", NotificationPriority.HIGH);

        List<Notification> allNotifications = notificationService.getAllGatewayNotifications(testGatewayId);
        assertEquals(3, allNotifications.size());

        notificationService.deleteNotification(notificationId1);
        notificationService.deleteNotification(notificationId2);
        notificationService.deleteNotification(notificationId3);
    }

    @Test
    public void testNotificationRepository_Get_NonExistentId() throws Exception {
        Notification retrieved = notificationService.getNotification(
                "non-existent-id-" + java.util.UUID.randomUUID().toString());
        assertNull(retrieved);
    }

    @Test
    public void testNotificationRepository_Update_PreservesCreationTime() throws Exception {
        String notificationId =
                createTestNotification("notif-update-preserve", "Original Title", NotificationPriority.LOW);

        Notification original = notificationService.getNotification(notificationId);
        assertNotNull(original);
        long originalCreationTime = original.getCreatedAt();
        assertTrue(originalCreationTime > 0);

        original.setTitle("Updated Title");
        original.setNotificationMessage("Updated message content");
        original.setPriority(NotificationPriority.HIGH);
        notificationService.updateNotification(original);

        Notification updated = notificationService.getNotification(notificationId);
        assertNotNull(updated);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(originalCreationTime, updated.getCreatedAt());

        notificationService.deleteNotification(notificationId);
    }

    @Test
    public void testNotificationRepository_Delete_VerifiesDeletion() throws Exception {
        String notificationId = createTestNotification("notif-delete", "To Be Deleted", NotificationPriority.NORMAL);

        Notification beforeDelete = notificationService.getNotification(notificationId);
        assertNotNull(beforeDelete);

        notificationService.deleteNotification(notificationId);

        Notification afterDelete = notificationService.getNotification(notificationId);
        assertNull(afterDelete);
    }

    @Test
    public void testNotificationRepository_PriorityHandling() throws Exception {
        String lowId = createTestNotification("notif-low", "Low Priority", NotificationPriority.LOW);
        String normalId = createTestNotification("notif-normal", "Normal Priority", NotificationPriority.NORMAL);
        String highId = createTestNotification("notif-high", "High Priority", NotificationPriority.HIGH);

        Notification low = notificationService.getNotification(lowId);
        Notification normal = notificationService.getNotification(normalId);
        Notification high = notificationService.getNotification(highId);

        assertEquals(NotificationPriority.LOW, low.getPriority());
        assertEquals(NotificationPriority.NORMAL, normal.getPriority());
        assertEquals(NotificationPriority.HIGH, high.getPriority());

        notificationService.deleteNotification(lowId);
        notificationService.deleteNotification(normalId);
        notificationService.deleteNotification(highId);
    }

    private String createTestNotification(String id, String title, NotificationPriority priority) throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId(id);
        notification.setGatewayId(testGatewayId);
        notification.setTitle(title);
        notification.setNotificationMessage("Test message for " + title);
        notification.setPriority(priority);
        return notificationService.createNotification(notification);
    }
}
