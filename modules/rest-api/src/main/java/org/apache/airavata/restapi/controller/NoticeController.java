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
package org.apache.airavata.restapi.controller;

import java.util.Map;
import java.util.UUID;
import org.apache.airavata.common.model.Notification;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.services.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class NoticeController {
    private final NotificationService notificationService;

    public NoticeController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getNotices(@RequestParam String gatewayId) {
        try {
            var notices = notificationService.getAllGatewayNotifications(gatewayId);
            return ResponseEntity.ok(notices);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNotice(@PathVariable String noticeId) {
        try {
            var notice = notificationService.getNotification(noticeId);
            if (notice == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(notice);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody Notification notice) {
        try {
            // Generate ID if not provided
            if (notice.getNotificationId() == null || notice.getNotificationId().isEmpty()) {
                notice.setNotificationId(UUID.randomUUID().toString());
            }
            String noticeId = notificationService.createNotification(notice);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("noticeId", noticeId));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<?> updateNotice(@PathVariable String noticeId, @RequestBody Notification notice) {
        try {
            notice.setNotificationId(noticeId);
            notificationService.updateNotification(notice);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(@PathVariable String noticeId) {
        try {
            notificationService.deleteNotification(noticeId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
