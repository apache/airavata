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
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.registry.exception.RegistryExceptions.AppCatalogException;
import org.apache.airavata.registry.services.ApplicationInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/application-interfaces")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class ApplicationInterfaceController {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInterfaceController.class);
    private final ApplicationInterfaceService applicationInterfaceService;

    public ApplicationInterfaceController(ApplicationInterfaceService applicationInterfaceService) {
        this.applicationInterfaceService = applicationInterfaceService;
    }

    @GetMapping("/{interfaceId}")
    public ResponseEntity<?> getApplicationInterface(@PathVariable String interfaceId) {
        try {
            var appInterface = applicationInterfaceService.getApplicationInterface(interfaceId);
            if (appInterface == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(appInterface);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplicationInterface(
            @RequestParam String gatewayId, @RequestBody ApplicationInterfaceDescription appInterface) {
        try {
            logger.debug("Creating application interface: gatewayId={}, applicationName={}", 
                    gatewayId, appInterface.getApplicationName());
            
            // Validate required fields
            if (appInterface.getApplicationName() == null || appInterface.getApplicationName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Application name is required"));
            }
            
            if (appInterface.getApplicationModules() == null || appInterface.getApplicationModules().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "At least one application module is required"));
            }
            
            var interfaceId = applicationInterfaceService.addApplicationInterface(appInterface, gatewayId);
            logger.info("Successfully created application interface: interfaceId={}, gatewayId={}", 
                    interfaceId, gatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("applicationInterfaceId", interfaceId, "interfaceId", interfaceId));
        } catch (AppCatalogException e) {
            logger.error("AppCatalogException while creating application interface: gatewayId={}, error={}", 
                    gatewayId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while creating application interface: gatewayId={}, error={}", 
                    gatewayId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create application interface: " + e.getMessage()));
        }
    }

    @PutMapping("/{interfaceId}")
    public ResponseEntity<?> updateApplicationInterface(
            @PathVariable String interfaceId, @RequestBody ApplicationInterfaceDescription appInterface) {
        try {
            appInterface.setApplicationInterfaceId(interfaceId);
            applicationInterfaceService.updateApplicationInterface(interfaceId, appInterface);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{interfaceId}")
    public ResponseEntity<?> deleteApplicationInterface(@PathVariable String interfaceId) {
        try {
            boolean deleted = applicationInterfaceService.removeApplicationInterface(interfaceId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllApplicationInterfaces(@RequestParam(required = false) String gatewayId) {
        try {
            if (gatewayId != null) {
                var interfaces = applicationInterfaceService.getAllApplicationInterfaces(gatewayId);
                return ResponseEntity.ok(interfaces);
            } else {
                var interfaceIds = applicationInterfaceService.getAllApplicationInterfaceIds();
                return ResponseEntity.ok(interfaceIds);
            }
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{interfaceId}/inputs")
    public ResponseEntity<?> getApplicationInputs(@PathVariable String interfaceId) {
        try {
            var inputs = applicationInterfaceService.getApplicationInputs(interfaceId);
            return ResponseEntity.ok(inputs);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{interfaceId}/outputs")
    public ResponseEntity<?> getApplicationOutputs(@PathVariable String interfaceId) {
        try {
            var outputs = applicationInterfaceService.getApplicationOutputs(interfaceId);
            return ResponseEntity.ok(outputs);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

@RestController
@RequestMapping("/api/v1/application-modules")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
class ApplicationModuleController {
    private final ApplicationInterfaceService applicationInterfaceService;

    public ApplicationModuleController(ApplicationInterfaceService applicationInterfaceService) {
        this.applicationInterfaceService = applicationInterfaceService;
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<?> getApplicationModule(@PathVariable String moduleId) {
        try {
            var module = applicationInterfaceService.getApplicationModule(moduleId);
            if (module == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(module);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplicationModule(
            @RequestParam String gatewayId, @RequestBody ApplicationModule module) {
        try {
            var moduleId = applicationInterfaceService.addApplicationModule(module, gatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("moduleId", moduleId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{moduleId}")
    public ResponseEntity<?> updateApplicationModule(
            @PathVariable String moduleId, @RequestBody ApplicationModule module) {
        try {
            module.setAppModuleId(moduleId);
            applicationInterfaceService.updateApplicationModule(moduleId, module);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{moduleId}")
    public ResponseEntity<?> deleteApplicationModule(@PathVariable String moduleId) {
        try {
            boolean deleted = applicationInterfaceService.removeApplicationModule(moduleId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllApplicationModules(@RequestParam String gatewayId) {
        try {
            var modules = applicationInterfaceService.getAllApplicationModules(gatewayId);
            return ResponseEntity.ok(modules);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
