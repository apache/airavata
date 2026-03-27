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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.execution.handler.RegistryServerHandler;
import org.apache.airavata.execution.service.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    RegistryServerHandler registryHandler;

    NotificationService notificationService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(registryHandler);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
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
