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
import org.apache.airavata.research.application.model.ApplicationInstallation;
import org.apache.airavata.research.application.service.ApplicationInstallationService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/installations")
@Tag(name = "Application Installations")
public class ApplicationInstallationController {

    private final ApplicationInstallationService applicationInstallationService;

    public ApplicationInstallationController(ApplicationInstallationService applicationInstallationService) {
        this.applicationInstallationService = applicationInstallationService;
    }

    @GetMapping
    public List<ApplicationInstallation> getInstallations(
            @RequestParam(required = false) String applicationId, @RequestParam(required = false) String resourceId) {
        if (applicationId != null) {
            var installations = applicationInstallationService.getInstallationsByApplication(applicationId);
            if (resourceId != null) {
                return installations.stream()
                        .filter(i -> resourceId.equals(i.getResourceId()))
                        .toList();
            }
            return installations;
        }
        return List.of();
    }

    @PostMapping
    public ResponseEntity<ApplicationInstallation> createInstallation(
            @RequestBody ApplicationInstallation installation) {
        String installationId = applicationInstallationService.createInstallation(installation);
        ApplicationInstallation created = applicationInstallationService.getInstallation(installationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{installationId}")
    public ApplicationInstallation getInstallation(@PathVariable("installationId") String installationId) {
        var installation = applicationInstallationService.getInstallation(installationId);
        if (installation == null) {
            throw new ResourceNotFoundException("ApplicationInstallation", installationId);
        }
        return installation;
    }

    @PostMapping("/{installationId}/reinstall")
    public ResponseEntity<Void> reinstall(@PathVariable("installationId") String installationId) {
        var installation = applicationInstallationService.getInstallation(installationId);
        if (installation == null) {
            throw new ResourceNotFoundException("ApplicationInstallation", installationId);
        }
        installation.setStatus(org.apache.airavata.research.application.model.InstallationStatus.PENDING);
        applicationInstallationService.updateInstallation(installationId, installation);
        return ResponseEntity.ok().build();
    }
}
