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

import org.apache.airavata.restapi.dto.StorageResourceDTO;
import org.apache.airavata.restapi.service.ResourceService;
import org.apache.airavata.service.exception.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage-resources")
public class StorageResourceController {

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceController.class);
    private final ResourceService resourceService;

    public StorageResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<?> registerStorageResource(@RequestBody StorageResourceDTO dto) {
        try {
            String resourceId = resourceService.registerStorageResource(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("storageResourceId", resourceId));
        } catch (AppCatalogException e) {
            logger.error("Error registering storage resource", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error registering storage resource", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllStorageResourceNames() {
        try {
            List<Map<String, String>> resources = resourceService.getAllStorageResourceNames();
            return ResponseEntity.ok(resources);
        } catch (AppCatalogException e) {
            logger.error("Error getting storage resources", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting storage resources", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStorageResource(@PathVariable String id) {
        try {
            StorageResourceDTO resource = resourceService.getStorageResource(id);
            return ResponseEntity.ok(resource);
        } catch (AppCatalogException e) {
            logger.error("Error getting storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStorageResource(@PathVariable String id, @RequestBody StorageResourceDTO dto) {
        try {
            boolean updated = resourceService.updateStorageResource(id, dto);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Storage resource updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Storage resource not found"));
            }
        } catch (AppCatalogException e) {
            logger.error("Error updating storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStorageResource(@PathVariable String id) {
        try {
            boolean deleted = resourceService.deleteStorageResource(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Storage resource deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Storage resource not found"));
            }
        } catch (AppCatalogException e) {
            logger.error("Error deleting storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting storage resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}



