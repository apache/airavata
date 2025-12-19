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
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.ApplicationInterfaceService;
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
public class ApplicationInterfaceController {
    private final ApplicationInterfaceService applicationInterfaceService;

    public ApplicationInterfaceController(ApplicationInterfaceService applicationInterfaceService) {
        this.applicationInterfaceService = applicationInterfaceService;
    }

    @GetMapping("/{interfaceId}")
    public ResponseEntity<?> getApplicationInterface(@PathVariable String interfaceId) {
        try {
            ApplicationInterfaceDescription appInterface =
                    applicationInterfaceService.getApplicationInterface(interfaceId);
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
            String interfaceId = applicationInterfaceService.addApplicationInterface(appInterface, gatewayId);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("interfaceId", interfaceId));
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
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
            List<ApplicationInterfaceDescription> interfaces;
            if (gatewayId != null) {
                interfaces = applicationInterfaceService.getAllApplicationInterfaces(gatewayId);
            } else {
                List<String> interfaceIds = applicationInterfaceService.getAllApplicationInterfaceIds();
                return ResponseEntity.ok(interfaceIds);
            }
            return ResponseEntity.ok(interfaces);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{interfaceId}/inputs")
    public ResponseEntity<?> getApplicationInputs(@PathVariable String interfaceId) {
        try {
            List<InputDataObjectType> inputs = applicationInterfaceService.getApplicationInputs(interfaceId);
            return ResponseEntity.ok(inputs);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{interfaceId}/outputs")
    public ResponseEntity<?> getApplicationOutputs(@PathVariable String interfaceId) {
        try {
            List<OutputDataObjectType> outputs = applicationInterfaceService.getApplicationOutputs(interfaceId);
            return ResponseEntity.ok(outputs);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

@RestController
@RequestMapping("/api/v1/application-modules")
class ApplicationModuleController {
    private final ApplicationInterfaceService applicationInterfaceService;

    public ApplicationModuleController(ApplicationInterfaceService applicationInterfaceService) {
        this.applicationInterfaceService = applicationInterfaceService;
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<?> getApplicationModule(@PathVariable String moduleId) {
        try {
            ApplicationModule module = applicationInterfaceService.getApplicationModule(moduleId);
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
            String moduleId = applicationInterfaceService.addApplicationModule(module, gatewayId);
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
            List<ApplicationModule> modules = applicationInterfaceService.getAllApplicationModules(gatewayId);
            return ResponseEntity.ok(modules);
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
