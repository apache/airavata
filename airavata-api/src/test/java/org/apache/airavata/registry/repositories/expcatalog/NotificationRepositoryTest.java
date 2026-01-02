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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.common.model.NotificationPriority;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            NotificationRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class NotificationRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataPropertiesConfiguration.class,
    })
    static class TestConfiguration {}

    private final NotificationService notificationService;
    private String testGatewayId;

    public NotificationRepositoryTest(NotificationService notificationService) {
        super(Database.EXP_CATALOG);
        this.notificationService = notificationService;
    }

    @BeforeEach
    public void setUp() {
        testGatewayId = "testGateway-" + java.util.UUID.randomUUID().toString();
    }

    @Test
    public void testNotificationRepository_Create_WithAllFields() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId-1");
        notification.setGatewayId(testGatewayId);
        notification.setTitle("Test Notification Title");
        notification.setNotificationMessage("This is a comprehensive test notification message");
        notification.setPriority(NotificationPriority.HIGH);
        long currentTime = System.currentTimeMillis();
        notification.setCreationTime(currentTime);
        notification.setPublishedTime(currentTime);
        notification.setExpirationTime(currentTime + 86400000L); // 1 day later

        String notificationId = notificationService.createNotification(notification);
        assertNotNull(notificationId, "Notification ID should not be null");
        assertEquals(notification.getNotificationId(), notificationId, "Returned ID should match set ID");

        Notification retrieved = notificationService.getNotification(notificationId);
        assertNotNull(retrieved, "Retrieved notification should not be null");
        assertEquals(testGatewayId, retrieved.getGatewayId(), "Gateway ID should match");
        assertEquals("Test Notification Title", retrieved.getTitle(), "Title should match");
        assertEquals(
                "This is a comprehensive test notification message",
                retrieved.getNotificationMessage(),
                "Message should match");
        assertEquals(NotificationPriority.HIGH, retrieved.getPriority(), "Priority should match");
        assertTrue(retrieved.getCreationTime() > 0, "Creation time should be set");
        assertTrue(retrieved.getPublishedTime() > 0, "Published time should be set");
        assertTrue(retrieved.getExpirationTime() > 0, "Expiration time should be set");

        notificationService.deleteNotification(notificationId);
        assertNull(notificationService.getNotification(notificationId), "Notification should be deleted");
    }

    @Test
    public void testNotificationRepository_Create_MultipleNotificationsPerGateway() throws RegistryException {
        // Create multiple notifications for the same gateway
        String notificationId1 = createTestNotification("notif-1", "Title 1", NotificationPriority.LOW);
        String notificationId2 = createTestNotification("notif-2", "Title 2", NotificationPriority.NORMAL);
        String notificationId3 = createTestNotification("notif-3", "Title 3", NotificationPriority.HIGH);

        List<Notification> allNotifications = notificationService.getAllGatewayNotifications(testGatewayId);
        assertEquals(3, allNotifications.size(), "Should have 3 notifications for the gateway");

        // Verify all notifications are present
        assertTrue(
                allNotifications.stream().anyMatch(n -> n.getNotificationId().equals(notificationId1)),
                "Notification 1 should be present");
        assertTrue(
                allNotifications.stream().anyMatch(n -> n.getNotificationId().equals(notificationId2)),
                "Notification 2 should be present");
        assertTrue(
                allNotifications.stream().anyMatch(n -> n.getNotificationId().equals(notificationId3)),
                "Notification 3 should be present");

        // Cleanup
        notificationService.deleteNotification(notificationId1);
        notificationService.deleteNotification(notificationId2);
        notificationService.deleteNotification(notificationId3);
    }

    @Test
    public void testNotificationRepository_Get_NonExistentId() throws RegistryException {
        // Test that getting non-existent notification returns null without throwing exception
        Notification retrieved = notificationService.getNotification(
                "non-existent-id-" + java.util.UUID.randomUUID().toString());
        assertNull(retrieved, "Non-existent notification should return null");

        // Verify this doesn't affect other operations
        String existingId = createTestNotification("notif-exists", "Existing", NotificationPriority.NORMAL);
        Notification existing = notificationService.getNotification(existingId);
        assertNotNull(existing, "Existing notification should be retrievable");
        notificationService.deleteNotification(existingId);
    }

    @Test
    public void testNotificationRepository_Update_PreservesCreationTime() throws RegistryException {
        // Test that update preserves creation time (important business rule)
        String notificationId =
                createTestNotification("notif-update-preserve", "Original Title", NotificationPriority.LOW);

        Notification original = notificationService.getNotification(notificationId);
        assertNotNull(original, "Notification should exist");
        long originalCreationTime = original.getCreationTime();
        assertTrue(originalCreationTime > 0, "Original creation time should be set");

        // Update notification
        original.setTitle("Updated Title");
        original.setNotificationMessage("Updated message content");
        original.setPriority(NotificationPriority.HIGH);
        long newExpirationTime = System.currentTimeMillis() + 172800000L; // 2 days later
        original.setExpirationTime(newExpirationTime);

        notificationService.updateNotification(original);

        Notification updated = notificationService.getNotification(notificationId);
        assertNotNull(updated, "Updated notification should exist");
        assertEquals("Updated Title", updated.getTitle(), "Title should be updated");
        assertEquals("Updated message content", updated.getNotificationMessage(), "Message should be updated");
        assertEquals(NotificationPriority.HIGH, updated.getPriority(), "Priority should be updated");
        assertTrue(updated.getExpirationTime() >= newExpirationTime, "Expiration time should be updated");

        // Verify creation time is preserved (business rule)
        assertEquals(
                originalCreationTime, updated.getCreationTime(), "Creation time should be preserved during update");

        notificationService.deleteNotification(notificationId);
    }

    @Test
    public void testNotificationRepository_Delete_VerifiesDeletion() throws RegistryException {
        // Create and then delete a notification, verify it's actually deleted
        String notificationId = createTestNotification("notif-delete", "To Be Deleted", NotificationPriority.NORMAL);

        Notification beforeDelete = notificationService.getNotification(notificationId);
        assertNotNull(beforeDelete, "Notification should exist before deletion");

        notificationService.deleteNotification(notificationId);

        Notification afterDelete = notificationService.getNotification(notificationId);
        assertNull(afterDelete, "Notification should be null after deletion");

        // Verify it's removed from gateway notifications list
        List<Notification> gatewayNotifications = notificationService.getAllGatewayNotifications(testGatewayId);
        assertTrue(
                gatewayNotifications.stream()
                        .noneMatch(n -> n.getNotificationId().equals(notificationId)),
                "Deleted notification should not appear in gateway notifications list");
    }

    @Test
    public void testNotificationRepository_GetAllGatewayNotifications_GatewayIsolation() throws RegistryException {
        // Test that notifications are properly isolated by gateway
        String gateway1 = "gateway-1-" + java.util.UUID.randomUUID().toString();
        String gateway2 = "gateway-2-" + java.util.UUID.randomUUID().toString();

        // Create notifications for gateway1
        Notification notif1 = new Notification();
        notif1.setNotificationId("notif-g1-1");
        notif1.setGatewayId(gateway1);
        notif1.setTitle("Gateway 1 Notification");
        notif1.setNotificationMessage("Message for gateway 1");
        String id1 = notificationService.createNotification(notif1);

        Notification notif2 = new Notification();
        notif2.setNotificationId("notif-g1-2");
        notif2.setGatewayId(gateway1);
        notif2.setTitle("Gateway 1 Notification 2");
        notif2.setNotificationMessage("Another message for gateway 1");
        String id2 = notificationService.createNotification(notif2);

        // Create notification for gateway2
        Notification notif3 = new Notification();
        notif3.setNotificationId("notif-g2-1");
        notif3.setGatewayId(gateway2);
        notif3.setTitle("Gateway 2 Notification");
        notif3.setNotificationMessage("Message for gateway 2");
        String id3 = notificationService.createNotification(notif3);

        // Verify gateway isolation
        List<Notification> gateway1Notifications = notificationService.getAllGatewayNotifications(gateway1);
        assertEquals(2, gateway1Notifications.size(), "Gateway 1 should have 2 notifications");
        assertTrue(
                gateway1Notifications.stream().allMatch(n -> n.getGatewayId().equals(gateway1)),
                "All notifications should belong to gateway 1");

        List<Notification> gateway2Notifications = notificationService.getAllGatewayNotifications(gateway2);
        assertEquals(1, gateway2Notifications.size(), "Gateway 2 should have 1 notification");
        assertTrue(
                gateway2Notifications.stream().allMatch(n -> n.getGatewayId().equals(gateway2)),
                "All notifications should belong to gateway 2");

        // Empty gateway should return empty list
        String emptyGateway = "empty-gateway-" + java.util.UUID.randomUUID().toString();
        List<Notification> emptyList = notificationService.getAllGatewayNotifications(emptyGateway);
        assertNotNull(emptyList, "Should return non-null list");
        assertTrue(emptyList.isEmpty(), "Empty gateway should return empty list");

        // Cleanup
        notificationService.deleteNotification(id1);
        notificationService.deleteNotification(id2);
        notificationService.deleteNotification(id3);
    }

    @Test
    public void testNotificationRepository_PriorityHandling() throws RegistryException {
        // Test all priority levels
        String lowId = createTestNotification("notif-low", "Low Priority", NotificationPriority.LOW);
        String normalId = createTestNotification("notif-normal", "Normal Priority", NotificationPriority.NORMAL);
        String highId = createTestNotification("notif-high", "High Priority", NotificationPriority.HIGH);

        Notification low = notificationService.getNotification(lowId);
        Notification normal = notificationService.getNotification(normalId);
        Notification high = notificationService.getNotification(highId);

        assertEquals(NotificationPriority.LOW, low.getPriority(), "Low priority should be set correctly");
        assertEquals(NotificationPriority.NORMAL, normal.getPriority(), "Normal priority should be set correctly");
        assertEquals(NotificationPriority.HIGH, high.getPriority(), "High priority should be set correctly");

        // Cleanup
        notificationService.deleteNotification(lowId);
        notificationService.deleteNotification(normalId);
        notificationService.deleteNotification(highId);
    }

    @Test
    public void testNotificationRepository_TimestampHandling() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notif-timestamp");
        notification.setGatewayId(testGatewayId);
        notification.setTitle("Timestamp Test");
        notification.setNotificationMessage("Testing timestamp handling");
        // Don't set timestamps - service should set them automatically

        String notificationId = notificationService.createNotification(notification);
        Notification retrieved = notificationService.getNotification(notificationId);

        assertNotNull(retrieved, "Notification should be created");
        assertTrue(retrieved.getCreationTime() > 0, "Creation time should be automatically set");
        assertTrue(retrieved.getPublishedTime() > 0, "Published time should be automatically set");
        assertTrue(retrieved.getExpirationTime() > 0, "Expiration time should be automatically set");
        assertTrue(retrieved.getExpirationTime() > retrieved.getCreationTime(), "Expiration should be after creation");

        notificationService.deleteNotification(notificationId);
    }

    @Test
    public void testNotificationRepository_AutomaticExpirationDefault() throws RegistryException {
        // Test that expiration time is automatically set to 1 year from now if not provided
        Notification notification = new Notification();
        notification.setNotificationId("notif-auto-expire");
        notification.setGatewayId(testGatewayId);
        notification.setTitle("Auto Expiration Test");
        notification.setNotificationMessage("Testing automatic expiration time setting");
        // Don't set expiration time - service should set it automatically

        long beforeCreation = System.currentTimeMillis();
        String notificationId = notificationService.createNotification(notification);
        long afterCreation = System.currentTimeMillis();

        Notification retrieved = notificationService.getNotification(notificationId);
        assertNotNull(retrieved, "Notification should be created");
        assertTrue(retrieved.getExpirationTime() > 0, "Expiration time should be automatically set");

        // Verify expiration is approximately 1 year from creation (within reasonable margin)
        long expectedExpiration = beforeCreation + (365L * 24 * 60 * 60 * 1000);
        long actualExpiration = retrieved.getExpirationTime();
        long oneDayInMs = 24L * 60 * 60 * 1000;
        assertTrue(
                Math.abs(actualExpiration - expectedExpiration) < oneDayInMs,
                "Expiration should be approximately 1 year from creation");

        notificationService.deleteNotification(notificationId);
    }

    @Test
    public void testNotificationRepository_MessageLengthLimit() throws RegistryException {
        // Test that messages at the database limit (4096 chars) are handled correctly
        // This tests an important boundary condition for the NOTIFICATION_MESSAGE column
        StringBuilder maxLengthMessage = new StringBuilder();
        // Create a message close to the 4096 char limit
        String baseMessage = "A"; // Single char to maximize length
        for (int i = 0; i < 4090; i++) {
            maxLengthMessage.append(baseMessage);
        }

        Notification notification = new Notification();
        notification.setNotificationId("notif-max-length");
        notification.setGatewayId(testGatewayId);
        notification.setTitle("Max Length Test");
        notification.setNotificationMessage(maxLengthMessage.toString());

        String notificationId = notificationService.createNotification(notification);
        Notification retrieved = notificationService.getNotification(notificationId);

        assertNotNull(retrieved, "Notification with max length message should be created");
        assertEquals(
                maxLengthMessage.toString(),
                retrieved.getNotificationMessage(),
                "Max length message should be preserved exactly");
        assertEquals(4090, retrieved.getNotificationMessage().length(), "Message length should match expected length");

        notificationService.deleteNotification(notificationId);
    }

    // Helper method to create test notifications
    private String createTestNotification(String id, String title, NotificationPriority priority)
            throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId(id);
        notification.setGatewayId(testGatewayId);
        notification.setTitle(title);
        notification.setNotificationMessage("Test message for " + title);
        notification.setPriority(priority);
        return notificationService.createNotification(notification);
    }
}
