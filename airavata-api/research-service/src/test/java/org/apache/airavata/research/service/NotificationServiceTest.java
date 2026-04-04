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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.apache.airavata.config.RequestContext;
import org.apache.airavata.interfaces.ProjectRegistry;
import org.apache.airavata.model.workspace.proto.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    ProjectRegistry projectRegistry;

    NotificationService notificationService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(projectRegistry);
        ctx = new RequestContext(
                "testUser", "testGateway", "token123", Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createNotification_delegatesToRegistry() throws Exception {
        Notification notification =
                Notification.newBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.createNotification(notification)).thenReturn("notif-1");

        String result = notificationService.createNotification(ctx, notification);

        assertEquals("notif-1", result);
        verify(projectRegistry).createNotification(notification);
    }

    @Test
    void getNotification_delegatesToRegistry() throws Exception {
        Notification notification =
                Notification.newBuilder().setNotificationId("notif-1").build();
        notification = notification.toBuilder().setGatewayId("testGateway").build();

        when(projectRegistry.getNotification("testGateway", "notif-1")).thenReturn(notification);

        Notification result = notificationService.getNotification(ctx, "testGateway", "notif-1");

        assertNotNull(result);
        assertEquals("notif-1", result.getNotificationId());
        verify(projectRegistry).getNotification("testGateway", "notif-1");
    }

    @Test
    void deleteNotification_delegatesToRegistry() throws Exception {
        when(projectRegistry.deleteNotification("testGateway", "notif-1")).thenReturn(true);

        boolean result = notificationService.deleteNotification(ctx, "testGateway", "notif-1");

        assertTrue(result);
        verify(projectRegistry).deleteNotification("testGateway", "notif-1");
    }

    @Test
    void updateNotification_delegatesToRegistry() throws Exception {
        Notification notification =
                Notification.newBuilder().setNotificationId("notif-1").build();

        when(projectRegistry.updateNotification(notification)).thenReturn(true);

        boolean result = notificationService.updateNotification(ctx, notification);

        assertTrue(result);
        verify(projectRegistry).updateNotification(notification);
    }

    @Test
    void getAllNotifications_delegatesToRegistry() throws Exception {
        Notification n1 = Notification.newBuilder().setNotificationId("notif-1").build();
        Notification n2 = Notification.newBuilder().setNotificationId("notif-2").build();

        when(projectRegistry.getAllNotifications("testGateway")).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getAllNotifications(ctx, "testGateway");

        assertEquals(2, result.size());
        verify(projectRegistry).getAllNotifications("testGateway");
    }
}
