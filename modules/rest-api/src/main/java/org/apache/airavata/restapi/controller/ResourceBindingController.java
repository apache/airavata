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

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.apache.airavata.compute.resource.model.ResourceBinding;
import org.apache.airavata.compute.resource.service.ResourceService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bindings")
@Tag(name = "Resource Bindings")
public class ResourceBindingController {

    private final ResourceService resourceService;

    public ResourceBindingController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<ResourceBinding> getBindings(
            @RequestParam String gatewayId,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String credentialId) {
        if (resourceId != null) {
            return resourceService.getBindingsByResource(resourceId);
        }
        if (credentialId != null) {
            return resourceService.getBindingsByCredential(credentialId);
        }
        return resourceService.getBindings(gatewayId);
    }

    @GetMapping("/{bindingId}")
    public ResourceBinding getBinding(@PathVariable("bindingId") String bindingId) {
        ResourceBinding binding = resourceService.getBinding(bindingId);
        if (binding == null) {
            throw new ResourceNotFoundException("ResourceBinding", bindingId);
        }
        return binding;
    }

    @PostMapping
    public ResponseEntity<ResourceBinding> createBinding(@RequestBody ResourceBinding binding) {
        String bindingId = resourceService.createBinding(binding);
        ResourceBinding created = resourceService.getBinding(bindingId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{bindingId}")
    public ResponseEntity<Void> updateBinding(
            @PathVariable("bindingId") String bindingId, @RequestBody ResourceBinding binding) {
        resourceService.updateBinding(bindingId, binding);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bindingId}")
    public ResponseEntity<Void> deleteBinding(@PathVariable("bindingId") String bindingId) {
        resourceService.deleteBinding(bindingId);
        return ResponseEntity.ok().build();
    }
}
