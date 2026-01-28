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
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.services.DataProductService;
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
@RequestMapping("/api/v1/data-products")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class DataProductController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataProductController.class);
    
    private final DataProductService dataProductService;

    public DataProductController(DataProductService dataProductService) {
        this.dataProductService = dataProductService;
    }

    @GetMapping("/{productUri}")
    public ResponseEntity<?> getDataProduct(@PathVariable String productUri) {
        try {
            logger.debug("Getting data product: {}", productUri);
            var product = dataProductService.getDataProduct(productUri);
            if (product == null) {
                logger.warn("Data product not found: {}", productUri);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (ReplicaCatalogException e) {
            logger.error("Error getting data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{productUri}/parent")
    public ResponseEntity<?> getParentDataProduct(@PathVariable String productUri) {
        try {
            var product = dataProductService.getParentDataProduct(productUri);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createDataProduct(@RequestBody DataProductModel product) {
        try {
            logger.info("Creating data product: {} for gateway: {}, owner: {}", 
                product.getProductName(), product.getGatewayId(), product.getOwnerName());
            
            // Validate required fields
            if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Product name is required"));
            }
            if (product.getGatewayId() == null || product.getGatewayId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Gateway ID is required"));
            }
            if (product.getOwnerName() == null || product.getOwnerName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Owner name is required"));
            }
            
            var productUri = dataProductService.registerDataProduct(product);
            logger.info("Data product created successfully: {}", productUri);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("productUri", productUri));
        } catch (ReplicaCatalogException e) {
            logger.error("Error creating data product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating data product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/{productUri}")
    public ResponseEntity<?> updateDataProduct(@PathVariable String productUri, @RequestBody DataProductModel product) {
        try {
            logger.info("Updating data product: {}", productUri);
            product.setProductUri(productUri);
            boolean updated = dataProductService.updateDataProduct(product);
            if (!updated) {
                logger.warn("Data product not found for update: {}", productUri);
                return ResponseEntity.notFound().build();
            }
            logger.info("Data product updated successfully: {}", productUri);
            return ResponseEntity.ok().build();
        } catch (ReplicaCatalogException e) {
            logger.error("Error updating data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @DeleteMapping("/{productUri}")
    public ResponseEntity<?> deleteDataProduct(@PathVariable String productUri) {
        try {
            logger.info("Deleting data product: {}", productUri);
            dataProductService.removeDataProduct(productUri);
            logger.info("Data product deleted successfully: {}", productUri);
            return ResponseEntity.ok().build();
        } catch (ReplicaCatalogException e) {
            logger.error("Error deleting data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting data product {}: {}", productUri, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{productUri}/children")
    public ResponseEntity<?> getChildDataProducts(@PathVariable String productUri) {
        try {
            var children = dataProductService.getChildDataProducts(productUri);
            return ResponseEntity.ok(children);
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDataProducts(
            @RequestParam String gatewayId,
            @RequestParam String userId,
            @RequestParam String productName,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        try {
            logger.debug("Searching data products: gatewayId={}, userId={}, productName={}, limit={}, offset={}", 
                gatewayId, userId, productName, limit, offset);
            
            // Allow empty productName to list all products for a user
            var products = dataProductService.searchDataProductsByName(
                gatewayId, 
                userId, 
                productName != null ? productName : "", 
                limit, 
                offset);
            
            logger.debug("Found {} data products", products != null ? products.size() : 0);
            return ResponseEntity.ok(products != null ? products : java.util.Collections.emptyList());
        } catch (ReplicaCatalogException e) {
            logger.error("Error searching data products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error searching data products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
        }
    }
}
