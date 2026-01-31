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
import java.util.UUID;
import org.apache.airavata.common.model.ParsingTemplate;
import org.apache.airavata.registry.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.registry.services.ParsingTemplateService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parsing-templates")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ParsingTemplateController {
    private final ParsingTemplateService parsingTemplateService;

    public ParsingTemplateController(ParsingTemplateService parsingTemplateService) {
        this.parsingTemplateService = parsingTemplateService;
    }

    @GetMapping
    public ResponseEntity<?> getParsingTemplates(
            @RequestParam(required = false) String gatewayId,
            @RequestParam(required = false) String applicationInterfaceId) {
        try {
            List<ParsingTemplate> templates;
            if (applicationInterfaceId != null && !applicationInterfaceId.isEmpty()) {
                templates = parsingTemplateService.getParsingTemplatesForApplication(applicationInterfaceId);
            } else if (gatewayId != null && !gatewayId.isEmpty()) {
                templates = parsingTemplateService.getAllParsingTemplates(gatewayId);
            } else {
                // Return empty list if no filter provided
                templates = List.of();
            }
            return ResponseEntity.ok(templates);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<?> getParsingTemplate(@PathVariable String templateId) {
        try {
            ParsingTemplate template = parsingTemplateService.get(templateId);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(template);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createParsingTemplate(
            @RequestBody ParsingTemplate template,
            @RequestParam(required = false) String gatewayId) {
        try {
            // Generate ID if not provided
            if (template.getId() == null || template.getId().isEmpty()) {
                template.setId(UUID.randomUUID().toString());
            }
            
            // Set gateway ID from request param if not in body
            if (template.getGatewayId() == null || template.getGatewayId().isEmpty()) {
                template.setGatewayId(gatewayId != null ? gatewayId : "default");
            }
            
            ParsingTemplate created = parsingTemplateService.create(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("templateId", created.getId()));
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<?> updateParsingTemplate(
            @PathVariable String templateId,
            @RequestBody ParsingTemplate template) {
        try {
            // Check if template exists
            if (!parsingTemplateService.isExists(templateId)) {
                return ResponseEntity.notFound().build();
            }
            
            // Ensure ID is set correctly
            template.setId(templateId);
            
            ParsingTemplate updated = parsingTemplateService.create(template);
            return ResponseEntity.ok(updated);
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<?> deleteParsingTemplate(@PathVariable String templateId) {
        try {
            // Check if template exists
            if (!parsingTemplateService.isExists(templateId)) {
                return ResponseEntity.notFound().build();
            }
            
            parsingTemplateService.delete(templateId);
            return ResponseEntity.ok().build();
        } catch (RegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
