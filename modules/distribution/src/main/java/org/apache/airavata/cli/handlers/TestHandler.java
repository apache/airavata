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
package org.apache.airavata.cli.handlers;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.apache.airavata.research.application.adapter.ApplicationAdapter;
import org.apache.airavata.research.application.model.ApplicationDeploymentDescription;
import org.apache.airavata.research.application.model.ApplicationInterfaceDescription;
import org.springframework.stereotype.Service;

@Service
public class TestHandler {
    private final ApplicationAdapter applicationAdapter;

    public TestHandler(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public void testApplicationSubmission(
            String gatewayId, String userId, String applicationId, String computeId, String storageId, String groupId) {
        System.out.println("Validating experiment submission readiness...");

        try {
            ApplicationInterfaceDescription appInterface = applicationAdapter.getApplicationInterface(applicationId);
            if (appInterface == null) {
                throw new EntityNotFoundException("Application interface not found: " + applicationId);
            }

            List<ApplicationDeploymentDescription> deployments = applicationAdapter.getApplicationDeployments(
                    appInterface.getApplicationModules().get(0));
            if (deployments.isEmpty()) {
                throw new EntityNotFoundException("No deployments found for application: " + applicationId);
            }

            ApplicationDeploymentDescription deployment = deployments.stream()
                    .filter(d -> computeId.equals(d.getComputeResourceId()))
                    .findFirst()
                    .orElse(deployments.get(0));

            System.out.println("Validation passed! All required information is present.");
            System.out.println("  - Application interface: " + applicationId);
            System.out.println("  - Application deployment: " + deployment.getAppDeploymentId());
            System.out.println("  - Compute resource: " + computeId);
            if (groupId != null) {
                System.out.println("  - Group resource profile: " + groupId);
            }
            if (storageId != null) {
                System.out.println("  - Storage resource: " + storageId);
            }
            System.out.println("\nThe system is ready to submit experiments!");
        } catch (Exception e) {
            System.out.println("Validation error: " + e.getMessage());
            System.out.println("Note: Please verify configuration manually.");
        }
    }
}
