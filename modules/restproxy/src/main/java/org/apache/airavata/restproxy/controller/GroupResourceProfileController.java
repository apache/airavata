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
import org.apache.airavata.common.model.BatchQueueResourcePolicy;
import org.apache.airavata.common.model.ComputeResourcePolicy;
import org.apache.airavata.common.model.GroupComputeResourcePreference;
import org.apache.airavata.common.model.GroupResourceProfile;
import org.apache.airavata.registry.services.GroupResourceProfileService;
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
@RequestMapping("/api/v1/group-resource-profiles")
public class GroupResourceProfileController {
    private final GroupResourceProfileService groupResourceProfileService;

    public GroupResourceProfileController(GroupResourceProfileService groupResourceProfileService) {
        this.groupResourceProfileService = groupResourceProfileService;
    }

    @GetMapping("/{groupResourceProfileId}")
    public ResponseEntity<?> getGroupResourceProfile(@PathVariable String groupResourceProfileId) {
        try {
            GroupResourceProfile profile = groupResourceProfileService.getGroupResourceProfile(groupResourceProfileId);
            if (profile == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createGroupResourceProfile(@RequestBody GroupResourceProfile profile) {
        try {
            String profileId = groupResourceProfileService.addGroupResourceProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("groupResourceProfileId", profileId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{groupResourceProfileId}")
    public ResponseEntity<?> updateGroupResourceProfile(
            @PathVariable String groupResourceProfileId, @RequestBody GroupResourceProfile profile) {
        try {
            profile.setGroupResourceProfileId(groupResourceProfileId);
            String result = groupResourceProfileService.updateGroupResourceProfile(profile);
            return ResponseEntity.ok(Map.of("groupResourceProfileId", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{groupResourceProfileId}")
    public ResponseEntity<?> deleteGroupResourceProfile(@PathVariable String groupResourceProfileId) {
        try {
            boolean deleted = groupResourceProfileService.removeGroupResourceProfile(groupResourceProfileId);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllGroupResourceProfiles(@RequestParam(required = false) String gatewayId) {
        try {
            List<GroupResourceProfile> profiles = groupResourceProfileService.getAllGroupResourceProfiles(gatewayId);
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{groupResourceProfileId}/compute-preferences")
    public ResponseEntity<?> getGroupComputeResourcePreferences(@PathVariable String groupResourceProfileId) {
        try {
            List<GroupComputeResourcePreference> preferences =
                    groupResourceProfileService.getAllGroupComputeResourcePreferences(groupResourceProfileId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{groupResourceProfileId}/compute-policies")
    public ResponseEntity<?> getGroupComputeResourcePolicies(@PathVariable String groupResourceProfileId) {
        try {
            List<ComputeResourcePolicy> policies =
                    groupResourceProfileService.getAllGroupComputeResourcePolicies(groupResourceProfileId);
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{groupResourceProfileId}/batch-queue-policies")
    public ResponseEntity<?> getGroupBatchQueueResourcePolicies(@PathVariable String groupResourceProfileId) {
        try {
            List<BatchQueueResourcePolicy> policies =
                    groupResourceProfileService.getAllGroupBatchQueueResourcePolicies(groupResourceProfileId);
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
