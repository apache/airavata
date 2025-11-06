/**
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
package org.apache.airavata.rest.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.rest.api.service.StorageResourceService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

@RestController
@RequestMapping("/api/v1/storage")
@Tag(name = "Storage Resources", description = "API for managing storage resources")
public class StorageResourceController {

    private static final Logger logger = LoggerFactory.getLogger(StorageResourceController.class);
    private final StorageResourceService storageResourceService;

    public StorageResourceController(StorageResourceService storageResourceService) {
        this.storageResourceService = storageResourceService;
    }

    @PostMapping
    @Operation(summary = "Register a storage resource", description = "Creates a new storage resource and returns its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Storage resource created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> registerStorageResource(
            @Parameter(description = "Storage resource description", required = true)
            @RequestBody StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        String storageResourceId = storageResourceService.registerStorageResource(storageResourceDescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(storageResourceId);
    }

    @GetMapping
    @Operation(summary = "Get all storage resource names", description = "Returns a map of all storage resource IDs and their hostnames")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved storage resources"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getAllStorageResourceNames()
            throws RegistryServiceException, TException {
        Map<String, String> storageResources = storageResourceService.getAllStorageResourceNames();
        return ResponseEntity.ok(storageResources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get storage resource by ID", description = "Retrieves detailed information about a specific storage resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved storage resource"),
            @ApiResponse(responseCode = "404", description = "Storage resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StorageResourceDescription> getStorageResource(
            @Parameter(description = "Storage resource ID", required = true)
            @PathVariable("id") String storageResourceId)
            throws RegistryServiceException, TException {
        StorageResourceDescription storageResource = storageResourceService.getStorageResource(storageResourceId);
        return ResponseEntity.ok(storageResource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update storage resource", description = "Updates an existing storage resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Storage resource updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Storage resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> updateStorageResource(
            @Parameter(description = "Storage resource ID", required = true)
            @PathVariable("id") String storageResourceId,
            @Parameter(description = "Updated storage resource description", required = true)
            @RequestBody StorageResourceDescription storageResourceDescription)
            throws RegistryServiceException, TException {
        storageResourceService.updateStorageResource(storageResourceId, storageResourceDescription);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete storage resource", description = "Deletes a storage resource by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Storage resource deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Storage resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteStorageResource(
            @Parameter(description = "Storage resource ID", required = true)
            @PathVariable("id") String storageResourceId)
            throws RegistryServiceException, TException {
        storageResourceService.deleteStorageResource(storageResourceId);
        return ResponseEntity.noContent().build();
    }
}

