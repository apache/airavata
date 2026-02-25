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
import org.apache.airavata.accounting.model.AllocationProject;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.apache.airavata.accounting.service.AllocationProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/allocation-projects")
@Tag(name = "Allocation Projects")
public class AllocationProjectController {

    private final AllocationProjectService allocationProjectService;

    public AllocationProjectController(AllocationProjectService allocationProjectService) {
        this.allocationProjectService = allocationProjectService;
    }

    @GetMapping
    public List<AllocationProject> getAllocationProjects(
            @RequestParam(required = false) String gatewayId, @RequestParam(required = false) String resourceId) {
        if (resourceId != null) {
            return allocationProjectService.getAllocationProjectsByResource(resourceId);
        }
        if (gatewayId != null) {
            return allocationProjectService.getAllocationProjects(gatewayId);
        }
        return List.of();
    }

    @GetMapping("/{allocationProjectId}")
    public AllocationProject getAllocationProject(@PathVariable("allocationProjectId") String allocationProjectId) {
        var allocationProject = allocationProjectService.getAllocationProject(allocationProjectId);
        if (allocationProject == null) {
            throw new ResourceNotFoundException("AllocationProject", allocationProjectId);
        }
        return allocationProject;
    }

    @GetMapping("/{allocationProjectId}/members")
    public List<String> getProjectMembers(@PathVariable("allocationProjectId") String allocationProjectId) {
        return allocationProjectService.getProjectMembers(allocationProjectId);
    }
}
