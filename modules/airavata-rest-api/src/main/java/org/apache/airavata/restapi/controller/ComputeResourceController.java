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

import java.util.List;
import java.util.Map;

import org.apache.airavata.registry.cpi.AppCatalogException;
import org.apache.airavata.restapi.dto.BatchQueueDTO;
import org.apache.airavata.restapi.dto.ComputeResourceDTO;
import org.apache.airavata.restapi.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ComputeResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ComputeResourceController.class);
    private final ResourceService resourceService;

    public ComputeResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<?> registerComputeResource(@RequestBody ComputeResourceDTO dto) {
        try {
            String resourceId = resourceService.registerComputeResource(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("computeResourceId", resourceId));
        } catch (AppCatalogException e) {
            logger.error("Error registering compute resource", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error registering compute resource", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllComputeResourceNames() {
        try {
            List<Map<String, String>> resources = resourceService.getAllComputeResourceNames();
            return ResponseEntity.ok(resources);
        } catch (AppCatalogException e) {
            logger.error("Error getting compute resources", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting compute resources", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComputeResource(@PathVariable String id) {
        try {
            ComputeResourceDTO resource = resourceService.getComputeResource(id);
            return ResponseEntity.ok(resource);
        } catch (AppCatalogException e) {
            logger.error("Error getting compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComputeResource(@PathVariable String id, @RequestBody ComputeResourceDTO dto) {
        try {
            boolean updated = resourceService.updateComputeResource(id, dto);
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Compute resource updated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Compute resource not found"));
            }
        } catch (AppCatalogException e) {
            logger.error("Error updating compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComputeResource(@PathVariable String id) {
        try {
            boolean deleted = resourceService.deleteComputeResource(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Compute resource deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Compute resource not found"));
            }
        } catch (AppCatalogException e) {
            logger.error("Error deleting compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{id}/queues")
    public ResponseEntity<?> getBatchQueues(@PathVariable String id) {
        try {
            ComputeResourceDTO resource = resourceService.getComputeResource(id);
            return ResponseEntity.ok(resource.getBatchQueues());
        } catch (AppCatalogException e) {
            logger.error("Error getting batch queues for compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting batch queues for compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/{id}/queues")
    public ResponseEntity<?> addBatchQueue(@PathVariable String id, @RequestBody BatchQueueDTO queueDTO) {
        try {
            ComputeResourceDTO resource = resourceService.getComputeResource(id);
            if (resource.getBatchQueues() == null) {
                resource.setBatchQueues(new java.util.ArrayList<>());
            }
            resource.getBatchQueues().add(queueDTO);
            boolean updated = resourceService.updateComputeResource(id, resource);
            if (updated) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "Batch queue added successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Compute resource not found"));
            }
        } catch (AppCatalogException e) {
            logger.error("Error adding batch queue to compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error adding batch queue to compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{id}/queues/{queueName}")
    public ResponseEntity<?> updateBatchQueue(@PathVariable String id, @PathVariable String queueName,
            @RequestBody BatchQueueDTO queueDTO) {
        try {
            ComputeResourceDTO resource = resourceService.getComputeResource(id);
            if (resource.getBatchQueues() != null) {
                for (int i = 0; i < resource.getBatchQueues().size(); i++) {
                    if (resource.getBatchQueues().get(i).getQueueName().equals(queueName)) {
                        resource.getBatchQueues().set(i, queueDTO);
                        boolean updated = resourceService.updateComputeResource(id, resource);
                        if (updated) {
                            return ResponseEntity.ok(Map.of("message", "Batch queue updated successfully"));
                        } else {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                    .body(Map.of("error", "Compute resource not found"));
                        }
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Batch queue not found"));
        } catch (AppCatalogException e) {
            logger.error("Error updating batch queue: " + queueName + " for compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating batch queue: " + queueName + " for compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{id}/queues/{queueName}")
    public ResponseEntity<?> deleteBatchQueue(@PathVariable String id, @PathVariable String queueName) {
        try {
            ComputeResourceDTO resource = resourceService.getComputeResource(id);
            if (resource.getBatchQueues() != null) {
                boolean removed = resource.getBatchQueues().removeIf(q -> q.getQueueName().equals(queueName));
                if (removed) {
                    boolean updated = resourceService.updateComputeResource(id, resource);
                    if (updated) {
                        return ResponseEntity.ok(Map.of("message", "Batch queue deleted successfully"));
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "Compute resource not found"));
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Batch queue not found"));
        } catch (AppCatalogException e) {
            logger.error("Error deleting batch queue: " + queueName + " from compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting batch queue: " + queueName + " from compute resource: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}
