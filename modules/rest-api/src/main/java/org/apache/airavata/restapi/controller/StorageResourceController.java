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
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.StorageResourceService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage-resources")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class StorageResourceController {
    private final StorageResourceService storageResourceService;

    public StorageResourceController(StorageResourceService storageResourceService) {
        this.storageResourceService = storageResourceService;
    }

    @GetMapping
    public ResponseEntity<?> getAllStorageResources() {
        try {
            var storageResources = storageResourceService.getAllStorageResourceIdList();
            return ResponseEntity.ok(storageResources);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{storageResourceId}")
    public ResponseEntity<?> getStorageResource(@PathVariable String storageResourceId) {
        try {
            var storageResource = storageResourceService.getStorageResource(storageResourceId);
            if (storageResource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(storageResource);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createStorageResource(@RequestBody StorageResourceDescription storageResource) {
        try {
            var storageResourceId = storageResourceService.addStorageResource(storageResource);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("storageResourceId", storageResourceId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{storageResourceId}")
    public ResponseEntity<?> updateStorageResource(
            @PathVariable String storageResourceId, @RequestBody StorageResourceDescription storageResource) {
        try {
            storageResource.setStorageResourceId(storageResourceId);
            storageResourceService.updateStorageResource(storageResourceId, storageResource);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{storageResourceId}")
    public ResponseEntity<?> deleteStorageResource(@PathVariable String storageResourceId) {
        try {
            storageResourceService.removeStorageResource(storageResourceId);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
