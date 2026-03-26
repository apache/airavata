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

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.research.experiment.model.Notification;
import org.apache.airavata.research.experiment.service.NotificationService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
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
@Tag(name = "Notices")
public class NoticeController {
    private final NotificationService notificationService;

    public NoticeController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getNotices(@RequestParam String gatewayId) throws RegistryException {
        return notificationService.getAllGatewayNotifications(gatewayId);
    }

    @GetMapping("/{noticeId}")
    public Notification getNotice(@PathVariable String noticeId) throws RegistryException {
        var notice = notificationService.getNotification(noticeId);
        if (notice == null) {
            throw new ResourceNotFoundException("Notification", noticeId);
        }
        return notice;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createNotice(@RequestBody Notification notice) throws RegistryException {
        // Generate ID if not provided
        if (notice.getNotificationId() == null || notice.getNotificationId().isEmpty()) {
            notice.setNotificationId(UUID.randomUUID().toString());
        }
        String noticeId = notificationService.createNotification(notice);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("noticeId", noticeId));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable String noticeId, @RequestBody Notification notice)
            throws RegistryException {
        notice.setNotificationId(noticeId);
        notificationService.updateNotification(notice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable String noticeId) throws RegistryException {
        notificationService.deleteNotification(noticeId);
        return ResponseEntity.ok().build();
    }
}
