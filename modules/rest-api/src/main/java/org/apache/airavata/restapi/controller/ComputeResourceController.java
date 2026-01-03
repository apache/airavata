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
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
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
@RequestMapping("/api/v1/compute-resources")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ComputeResourceController {
    private final ComputeResourceService computeResourceService;

    public ComputeResourceController(ComputeResourceService computeResourceService) {
        this.computeResourceService = computeResourceService;
    }

    @GetMapping
    public ResponseEntity<?> getAllComputeResources() {
        try {
            Map<String, String> computeResources = computeResourceService.getAllComputeResourceIdList();
            return ResponseEntity.ok(computeResources);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{computeResourceId}")
    public ResponseEntity<?> getComputeResource(@PathVariable String computeResourceId) {
        try {
            ComputeResourceDescription computeResource = computeResourceService.getComputeResource(computeResourceId);
            if (computeResource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(computeResource);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createComputeResource(@RequestBody ComputeResourceDescription computeResource) {
        try {
            String computeResourceId = computeResourceService.addComputeResource(computeResource);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("computeResourceId", computeResourceId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{computeResourceId}")
    public ResponseEntity<?> updateComputeResource(
            @PathVariable String computeResourceId, @RequestBody ComputeResourceDescription computeResource) {
        try {
            computeResource.setComputeResourceId(computeResourceId);
            computeResourceService.updateComputeResource(computeResourceId, computeResource);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{computeResourceId}")
    public ResponseEntity<?> deleteComputeResource(@PathVariable String computeResourceId) {
        try {
            computeResourceService.removeComputeResource(computeResourceId);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
