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
package org.apache.airavata.research.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.research.service.enums.StatusEnum;
import org.apache.airavata.research.service.handlers.AdminHandler;
import org.apache.airavata.research.service.handlers.ResourceHandler;
import org.apache.airavata.research.service.model.entity.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rf/admin")
@Tag(name = "Admin Controls ", description = "Operations performable by Cybershuttle admins")
public class AdminController {

    private final ResourceHandler resourceHandler;
    private final AdminHandler adminHandler;

    public AdminController(ResourceHandler resourceHandler, AdminHandler adminHandler) {
        this.resourceHandler = resourceHandler;
        this.adminHandler = adminHandler;
    }

    @PostMapping("/resources/{id}/verify")
    @Operation(summary = "Verify a resource")
    public ResponseEntity<Resource> verifyResource(@PathVariable(value = "id") String id) {
        Resource resource = resourceHandler.getResourceById(id);
        return ResponseEntity.ok(adminHandler.verifyResource(resource));
    }

    @PostMapping("/resources/{id}/reject")
    @Operation(summary = "Verify a resource")
    public ResponseEntity<Resource> rejectResource(
            @PathVariable(value = "id") String id, @RequestBody String rejectionMessage) {
        Resource resource = resourceHandler.getResourceById(id);
        return ResponseEntity.ok(adminHandler.rejectResource(resource, rejectionMessage));
    }

    @GetMapping("/resources/pending")
    @Operation(summary = "Get all pending verification resources")
    public ResponseEntity<List<Resource>> getPendingResources() {
        return ResponseEntity.ok(resourceHandler.getAllResourcesWithStatus(List.of(StatusEnum.PENDING)));
    }
}
