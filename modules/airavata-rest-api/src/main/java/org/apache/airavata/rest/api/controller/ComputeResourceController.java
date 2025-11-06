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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManager;
import org.apache.airavata.rest.api.service.ComputeResourceService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

@RestController
@RequestMapping("/api/v1/compute")
@Tag(name = "Compute Resources", description = "API for managing compute resources, resource job managers, and batch queues")
public class ComputeResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceController.class);
    private final ComputeResourceService computeResourceService;

    public ComputeResourceController(ComputeResourceService computeResourceService) {
        this.computeResourceService = computeResourceService;
    }

    @PostMapping
    @Operation(summary = "Register a compute resource", description = "Creates a new compute resource and returns its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compute resource created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> registerComputeResource(
            @Parameter(description = "Compute resource description", required = true)
            @RequestBody ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        String computeResourceId = computeResourceService.registerComputeResource(computeResourceDescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(computeResourceId);
    }

    @GetMapping
    @Operation(summary = "Get all compute resource names", description = "Returns a map of all compute resource IDs and their hostnames")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved compute resources"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, String>> getAllComputeResourceNames()
            throws RegistryServiceException, TException {
        Map<String, String> computeResources = computeResourceService.getAllComputeResourceNames();
        return ResponseEntity.ok(computeResources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get compute resource by ID", description = "Retrieves detailed information about a specific compute resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved compute resource"),
            @ApiResponse(responseCode = "404", description = "Compute resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ComputeResourceDescription> getComputeResource(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId)
            throws RegistryServiceException, TException {
        ComputeResourceDescription computeResource = computeResourceService.getComputeResource(computeResourceId);
        return ResponseEntity.ok(computeResource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update compute resource", description = "Updates an existing compute resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compute resource updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Compute resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> updateComputeResource(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Updated compute resource description", required = true)
            @RequestBody ComputeResourceDescription computeResourceDescription)
            throws RegistryServiceException, TException {
        computeResourceService.updateComputeResource(computeResourceId, computeResourceDescription);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete compute resource", description = "Deletes a compute resource by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compute resource deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Compute resource not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteComputeResource(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId)
            throws RegistryServiceException, TException {
        computeResourceService.deleteComputeResource(computeResourceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rjm")
    @Operation(summary = "Register resource job manager", description = "Creates a new resource job manager for a compute resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resource job manager created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> registerResourceJobManager(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Resource job manager description", required = true)
            @RequestBody ResourceJobManager resourceJobManager)
            throws RegistryServiceException, TException {
        String rjmId = computeResourceService.registerResourceJobManager(resourceJobManager);
        return ResponseEntity.status(HttpStatus.CREATED).body(rjmId);
    }

    @GetMapping("/{id}/rjm/{rjmId}")
    @Operation(summary = "Get resource job manager", description = "Retrieves a resource job manager by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved resource job manager"),
            @ApiResponse(responseCode = "404", description = "Resource job manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResourceJobManager> getResourceJobManager(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Resource job manager ID", required = true)
            @PathVariable("rjmId") String resourceJobManagerId)
            throws RegistryServiceException, TException {
        ResourceJobManager rjm = computeResourceService.getResourceJobManager(resourceJobManagerId);
        return ResponseEntity.ok(rjm);
    }

    @PutMapping("/{id}/rjm/{rjmId}")
    @Operation(summary = "Update resource job manager", description = "Updates an existing resource job manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource job manager updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Resource job manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> updateResourceJobManager(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Resource job manager ID", required = true)
            @PathVariable("rjmId") String resourceJobManagerId,
            @Parameter(description = "Updated resource job manager description", required = true)
            @RequestBody ResourceJobManager updatedResourceJobManager)
            throws RegistryServiceException, TException {
        computeResourceService.updateResourceJobManager(resourceJobManagerId, updatedResourceJobManager);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/rjm/{rjmId}")
    @Operation(summary = "Delete resource job manager", description = "Deletes a resource job manager by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource job manager deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Resource job manager not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteResourceJobManager(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Resource job manager ID", required = true)
            @PathVariable("rjmId") String resourceJobManagerId)
            throws RegistryServiceException, TException {
        computeResourceService.deleteResourceJobManager(resourceJobManagerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/queue/{queueName}")
    @Operation(summary = "Delete batch queue", description = "Deletes a batch queue from a compute resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Batch queue deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Batch queue not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteBatchQueue(
            @Parameter(description = "Compute resource ID", required = true)
            @PathVariable("id") String computeResourceId,
            @Parameter(description = "Queue name", required = true)
            @PathVariable("queueName") String queueName)
            throws RegistryServiceException, TException {
        computeResourceService.deleteBatchQueue(computeResourceId, queueName);
        return ResponseEntity.noContent().build();
    }
}

