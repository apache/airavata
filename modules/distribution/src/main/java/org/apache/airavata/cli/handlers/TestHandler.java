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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.common.exception.LaunchValidationException;
import org.apache.airavata.common.exception.ValidationResults;
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ExperimentModel;
import org.apache.airavata.common.model.ProcessModel;
import org.apache.airavata.orchestrator.ValidationService;
import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.apache.airavata.service.application.ApplicationService;
import org.springframework.stereotype.Service;

@Service
public class TestHandler {
    private final ApplicationService applicationService;
    private final ValidationService validationService;

    public TestHandler(ApplicationService applicationService, ValidationService validationService) {
        this.applicationService = applicationService;
        this.validationService = validationService;
    }

    public void testApplicationSubmission(
            String gatewayId, String userId, String applicationId, String computeId, String storageId, String groupId) {
        System.out.println("Validating experiment submission readiness...");

        try {
            // Get application deployment
            ApplicationInterfaceDescription appInterface = applicationService.getApplicationInterface(applicationId);
            if (appInterface == null) {
                throw new RuntimeException("Application interface not found: " + applicationId);
            }

            List<ApplicationDeploymentDescription> deployments = applicationService.getApplicationDeployments(
                    appInterface.getApplicationModules().get(0));
            if (deployments.isEmpty()) {
                throw new RuntimeException("No deployments found for application: " + applicationId);
            }

            ApplicationDeploymentDescription deployment = deployments.stream()
                    .filter(d -> computeId.equals(d.getComputeHostId()))
                    .findFirst()
                    .orElse(deployments.get(0));

            // Create a test experiment model
            var experiment = new ExperimentModel();
            experiment.setExperimentId(UUID.randomUUID().toString());
            experiment.setGatewayId(gatewayId);
            experiment.setUserName(userId);
            experiment.setProjectId("test-project");

            // Create process model
            var process = new ProcessModel();
            process.setProcessId(UUID.randomUUID().toString());
            process.setApplicationInterfaceId(applicationId);
            process.setApplicationDeploymentId(deployment.getAppDeploymentId());
            process.setComputeResourceId(computeId);
            if (groupId != null) {
                process.setGroupResourceProfileId(groupId);
            }

            experiment.setProcesses(Arrays.asList(process));

            // Validate experiment
            ValidationResults validationResults = validationService.validateExperiment(experiment);

            if (validationResults.getValidationState()) {
                System.out.println("✓ Validation passed! All required information is present.");
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
            } else {
                System.out.println("⚠ Validation completed with warnings:");
                if (validationResults.getValidationResultList() != null) {
                    for (var result : validationResults.getValidationResultList()) {
                        if (!result.getResult()) {
                            System.out.println("  - " + result.getErrorDetails());
                        }
                    }
                }
            }

        } catch (LaunchValidationException e) {
            System.out.println("⚠ Validation found issues:");
            if (e.getValidationResult() != null && e.getValidationResult().getValidationResultList() != null) {
                for (var result : e.getValidationResult().getValidationResultList()) {
                    if (!result.getResult()) {
                        System.out.println("  - " + result.getErrorDetails());
                    }
                }
            }
            System.out.println("\nNote: Some validations may require actual resource access. Please verify manually.");
        } catch (OrchestratorException e) {
            System.out.println("⚠ Validation service error: " + e.getMessage());
            System.out.println("Note: This may be expected if orchestrator services are not fully initialized.");
        } catch (Exception e) {
            System.out.println("⚠ Validation error: " + e.getMessage());
            System.out.println("Note: Continuing despite validation error. Please verify configuration manually.");
        }
    }
}
