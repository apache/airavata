package org.apache.airavata.service.notification;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock RegistryServerHandler registryHandler;

    NotificationService notificationService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(registryHandler);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createNotification_delegatesToRegistry() throws Exception {
        Notification notification = new Notification();
        notification.setGatewayId("testGateway");

        when(registryHandler.createNotification(notification)).thenReturn("notif-1");

        String result = notificationService.createNotification(ctx, notification);

        assertEquals("notif-1", result);
        verify(registryHandler).createNotification(notification);
    }

    @Test
    void getNotification_delegatesToRegistry() throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId("notif-1");
        notification.setGatewayId("testGateway");

        when(registryHandler.getNotification("testGateway", "notif-1")).thenReturn(notification);

        Notification result = notificationService.getNotification(ctx, "testGateway", "notif-1");

        assertNotNull(result);
        assertEquals("notif-1", result.getNotificationId());
        verify(registryHandler).getNotification("testGateway", "notif-1");
    }

    @Test
    void deleteNotification_delegatesToRegistry() throws Exception {
        when(registryHandler.deleteNotification("testGateway", "notif-1")).thenReturn(true);

        boolean result = notificationService.deleteNotification(ctx, "testGateway", "notif-1");

        assertTrue(result);
        verify(registryHandler).deleteNotification("testGateway", "notif-1");
    }

    @Test
    void updateNotification_delegatesToRegistry() throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId("notif-1");

        when(registryHandler.updateNotification(notification)).thenReturn(true);

        boolean result = notificationService.updateNotification(ctx, notification);

        assertTrue(result);
        verify(registryHandler).updateNotification(notification);
    }

    @Test
    void getAllNotifications_delegatesToRegistry() throws Exception {
        Notification n1 = new Notification();
        n1.setNotificationId("notif-1");
        Notification n2 = new Notification();
        n2.setNotificationId("notif-2");

        when(registryHandler.getAllNotifications("testGateway")).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getAllNotifications(ctx, "testGateway");

        assertEquals(2, result.size());
        verify(registryHandler).getAllNotifications("testGateway");
    }
}
