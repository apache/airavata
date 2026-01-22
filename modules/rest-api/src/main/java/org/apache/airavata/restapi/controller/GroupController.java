/**
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
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.airavata.common.model.GroupModel;
import org.apache.airavata.restapi.security.AuthorizationService;
import org.apache.airavata.security.model.AuthzToken;
import org.apache.airavata.service.security.GroupManagerService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@ConditionalOnProperty(name = "services.rest.enabled", havingValue = "true", matchIfMissing = false)
public class GroupController {
    private final GroupManagerService groupManagerService;
    private final AuthorizationService authorizationService;

    public GroupController(GroupManagerService groupManagerService, AuthorizationService authorizationService) {
        this.groupManagerService = groupManagerService;
        this.authorizationService = authorizationService;
    }

    private AuthzToken getAuthzToken(HttpServletRequest request) {
        return (AuthzToken) request.getAttribute("authzToken");
    }

    @GetMapping
    public ResponseEntity<?> getGroups(
            @RequestParam(required = false) String gatewayId,
            HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate and scope gateway if provided
            if (gatewayId != null && !gatewayId.isEmpty()) {
                authorizationService.requireGatewayAccess(authzToken, gatewayId);
            }
            List<GroupModel> groups = groupManagerService.getGroups(authzToken);
            
            // Transform to frontend format
            var result = groups.stream().map(g -> {
                var groupMap = new java.util.HashMap<String, Object>();
                groupMap.put("id", g.getId());
                groupMap.put("name", g.getName());
                groupMap.put("description", g.getDescription());
                groupMap.put("ownerId", g.getOwnerId());
                groupMap.put("members", g.getMembers() != null ? 
                    g.getMembers().stream().map(m -> {
                        var memberMap = new java.util.HashMap<String, String>();
                        memberMap.put("userId", m);
                        memberMap.put("username", m);
                        return memberMap;
                    }).collect(Collectors.toList()) : 
                    java.util.Collections.emptyList());
                groupMap.put("admins", g.getAdmins() != null ? g.getAdmins() : java.util.Collections.emptyList());
                return groupMap;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable String groupId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            GroupModel group = groupManagerService.getGroup(authzToken, groupId);
            if (group == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Transform to frontend format
            var groupMap = new java.util.HashMap<String, Object>();
            groupMap.put("id", group.getId());
            groupMap.put("name", group.getName());
            groupMap.put("description", group.getDescription());
            groupMap.put("ownerId", group.getOwnerId());
            groupMap.put("members", group.getMembers() != null ? 
                group.getMembers().stream().map(m -> {
                    var memberMap = new java.util.HashMap<String, String>();
                    memberMap.put("userId", m);
                    memberMap.put("username", m);
                    return memberMap;
                }).collect(Collectors.toList()) : 
                java.util.Collections.emptyList());
            groupMap.put("admins", group.getAdmins() != null ? group.getAdmins() : java.util.Collections.emptyList());
            
            return ResponseEntity.ok(groupMap);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> groupData, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            
            // Validate gateway access if gatewayId is provided
            if (groupData.containsKey("gatewayId")) {
                String gatewayId = (String) groupData.get("gatewayId");
                authorizationService.requireGatewayAccess(authzToken, gatewayId);
            }
            var group = new GroupModel();
            if (groupData.containsKey("name")) {
                group.setName((String) groupData.get("name"));
            }
            if (groupData.containsKey("description")) {
                group.setDescription((String) groupData.get("description"));
            }
            if (groupData.containsKey("ownerId")) {
                group.setOwnerId((String) groupData.get("ownerId"));
            }
            
            String groupId = groupManagerService.createGroup(authzToken, group);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("groupId", groupId));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable String groupId, @RequestBody Map<String, Object> groupData, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            GroupModel group = groupManagerService.getGroup(authzToken, groupId);
            if (group == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (groupData.containsKey("name")) {
                group.setName((String) groupData.get("name"));
            }
            if (groupData.containsKey("description")) {
                group.setDescription((String) groupData.get("description"));
            }
            
            boolean updated = groupManagerService.updateGroup(authzToken, group);
            if (!updated) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable String groupId, @RequestParam(required = false) String ownerId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            GroupModel group = groupManagerService.getGroup(authzToken, groupId);
            if (group == null) {
                return ResponseEntity.notFound().build();
            }
            
            String actualOwnerId = ownerId != null ? ownerId : group.getOwnerId();
            boolean deleted = groupManagerService.deleteGroup(authzToken, groupId, actualOwnerId);
            if (!deleted) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMember(@PathVariable String groupId, @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            String userId = requestBody.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body("userId is required");
            }
            boolean added = groupManagerService.addUsersToGroup(authzToken, List.of(userId), groupId);
            if (!added) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable String groupId, @PathVariable String userId, HttpServletRequest request) {
        try {
            var authzToken = getAuthzToken(request);
            boolean removed = groupManagerService.removeUsersFromGroup(authzToken, List.of(userId), groupId);
            if (!removed) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (org.apache.airavata.profile.exception.GroupManagerServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.sharing.model.SharingRegistryException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (org.apache.airavata.common.exception.AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
