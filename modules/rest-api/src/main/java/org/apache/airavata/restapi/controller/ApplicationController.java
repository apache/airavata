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
import java.util.Map;
import org.apache.airavata.research.application.model.Application;
import org.apache.airavata.research.application.model.ApplicationInstallation;
import org.apache.airavata.research.application.service.ApplicationInstallationService;
import org.apache.airavata.research.application.service.ApplicationService;
import org.apache.airavata.restapi.exception.ResourceNotFoundException;
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
@RequestMapping("/api/v1/applications")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationInstallationService applicationInstallationService;

    public ApplicationController(
            ApplicationService applicationService, ApplicationInstallationService applicationInstallationService) {
        this.applicationService = applicationService;
        this.applicationInstallationService = applicationInstallationService;
    }

    @GetMapping
    public List<Application> getApplications(@RequestParam String gatewayId) {
        return applicationService.getApplications(gatewayId);
    }

    @GetMapping("/{applicationId}")
    public Application getApplication(@PathVariable("applicationId") String applicationId) {
        var application = applicationService.getApplication(applicationId);
        if (application == null) {
            throw new ResourceNotFoundException("Application", applicationId);
        }
        return application;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createApplication(@RequestBody Application application) {
        var applicationId = applicationService.createApplication(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("applicationId", applicationId));
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<Void> updateApplication(
            @PathVariable("applicationId") String applicationId, @RequestBody Application application) {
        applicationService.updateApplication(applicationId, application);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@PathVariable("applicationId") String applicationId) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{applicationId}/installations")
    public List<ApplicationInstallation> getInstallations(@PathVariable("applicationId") String applicationId) {
        return applicationInstallationService.getInstallationsByApplication(applicationId);
    }
}
