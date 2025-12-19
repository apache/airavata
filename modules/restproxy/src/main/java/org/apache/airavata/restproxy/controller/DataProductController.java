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
package org.apache.airavata.restproxy.controller;

import java.util.List;
import java.util.Map;
import org.apache.airavata.common.model.DataProductModel;
import org.apache.airavata.registry.exception.ReplicaCatalogException;
import org.apache.airavata.registry.services.DataProductService;
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
public class DataProductController {
    private final DataProductService dataProductService;

    public DataProductController(DataProductService dataProductService) {
        this.dataProductService = dataProductService;
    }

    @GetMapping("/{productUri}")
    public ResponseEntity<?> getDataProduct(@PathVariable String productUri) {
        try {
            DataProductModel product = dataProductService.getDataProduct(productUri);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{productUri}/parent")
    public ResponseEntity<?> getParentDataProduct(@PathVariable String productUri) {
        try {
            DataProductModel product = dataProductService.getParentDataProduct(productUri);
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
            String productUri = dataProductService.registerDataProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("productUri", productUri));
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{productUri}")
    public ResponseEntity<?> updateDataProduct(@PathVariable String productUri, @RequestBody DataProductModel product) {
        try {
            product.setProductUri(productUri);
            boolean updated = dataProductService.updateDataProduct(product);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{productUri}")
    public ResponseEntity<?> deleteDataProduct(@PathVariable String productUri) {
        try {
            dataProductService.removeDataProduct(productUri);
            return ResponseEntity.ok().build();
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{productUri}/children")
    public ResponseEntity<?> getChildDataProducts(@PathVariable String productUri) {
        try {
            List<DataProductModel> children = dataProductService.getChildDataProducts(productUri);
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
            List<DataProductModel> products =
                    dataProductService.searchDataProductsByName(gatewayId, userId, productName, limit, offset);
            return ResponseEntity.ok(products);
        } catch (ReplicaCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
