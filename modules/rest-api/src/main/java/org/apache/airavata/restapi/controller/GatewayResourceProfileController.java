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
import org.apache.airavata.common.model.GatewayResourceProfile;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.GwyResourceProfileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gateway-resource-profile")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class GatewayResourceProfileController {
    private final GwyResourceProfileService gwyResourceProfileService;

    public GatewayResourceProfileController(GwyResourceProfileService gwyResourceProfileService) {
        this.gwyResourceProfileService = gwyResourceProfileService;
    }

    @GetMapping("/{gatewayId}")
    public ResponseEntity<?> getGatewayResourceProfile(@PathVariable String gatewayId) {
        try {
            var profile = gwyResourceProfileService.getGatewayProfile(gatewayId);
            if (profile == null) {
                // Return an empty profile with the gatewayId so the UI can work with it
                GatewayResourceProfile emptyProfile = new GatewayResourceProfile();
                emptyProfile.setGatewayID(gatewayId);
                return ResponseEntity.ok(emptyProfile);
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createGatewayResourceProfile(@RequestBody GatewayResourceProfile profile) {
        try {
            var gatewayId = gwyResourceProfileService.addGatewayResourceProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("gatewayId", gatewayId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{gatewayId}")
    public ResponseEntity<?> updateGatewayResourceProfile(
            @PathVariable String gatewayId, @RequestBody GatewayResourceProfile profile) {
        try {
            profile.setGatewayID(gatewayId);
            gwyResourceProfileService.updateGatewayResourceProfile(gatewayId, profile);
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{gatewayId}")
    public ResponseEntity<?> deleteGatewayResourceProfile(@PathVariable String gatewayId) {
        try {
            boolean deleted = gwyResourceProfileService.removeGatewayResourceProfile(gatewayId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (AppCatalogException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllGatewayResourceProfiles() {
        try {
            var profiles = gwyResourceProfileService.getAllGatewayProfiles();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}/compute-preferences")
    public ResponseEntity<?> getComputeResourcePreferences(@PathVariable String gatewayId) {
        try {
            var preferences = gwyResourceProfileService.getAllComputeResourcePreferences(gatewayId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}/compute-preferences/{computeResourceId}")
    public ResponseEntity<?> getComputeResourcePreference(
            @PathVariable String gatewayId, @PathVariable String computeResourceId) {
        try {
            var preference = gwyResourceProfileService.getComputeResourcePreference(gatewayId, computeResourceId);
            if (preference == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{gatewayId}/compute-preferences/{computeResourceId}")
    public ResponseEntity<?> removeComputeResourcePreference(
            @PathVariable String gatewayId, @PathVariable String computeResourceId) {
        try {
            boolean removed =
                    gwyResourceProfileService.removeComputeResourcePreferenceFromGateway(gatewayId, computeResourceId);
            return ResponseEntity.ok(Map.of("removed", removed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}/storage-preferences")
    public ResponseEntity<?> getStoragePreferences(@PathVariable String gatewayId) {
        try {
            var preferences = gwyResourceProfileService.getAllStoragePreferences(gatewayId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{gatewayId}/storage-preferences/{storageResourceId}")
    public ResponseEntity<?> getStoragePreference(
            @PathVariable String gatewayId, @PathVariable String storageResourceId) {
        try {
            var preference = gwyResourceProfileService.getStoragePreference(gatewayId, storageResourceId);
            if (preference == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{gatewayId}/storage-preferences/{storageResourceId}")
    public ResponseEntity<?> removeStoragePreference(
            @PathVariable String gatewayId, @PathVariable String storageResourceId) {
        try {
            boolean removed =
                    gwyResourceProfileService.removeDataStoragePreferenceFromGateway(gatewayId, storageResourceId);
            return ResponseEntity.ok(Map.of("removed", removed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
