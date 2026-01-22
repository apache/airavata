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
import org.apache.airavata.common.model.ApplicationDeploymentDescription;
import org.apache.airavata.common.model.ApplicationInterfaceDescription;
import org.apache.airavata.common.model.ApplicationModule;
import org.apache.airavata.common.model.ApplicationParallelismType;
import org.apache.airavata.common.model.DataType;
import org.apache.airavata.common.model.InputDataObjectType;
import org.apache.airavata.common.model.OutputDataObjectType;
import org.apache.airavata.service.application.ApplicationService;
import org.apache.airavata.service.registry.RegistryService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationHandler {
    private final ApplicationService applicationService;
    private final RegistryService registryService;

    public ApplicationHandler(ApplicationService applicationService, RegistryService registryService) {
        this.applicationService = applicationService;
        this.registryService = registryService;
    }

    public String createApplication(
            String gatewayId, String name, String moduleName, String executable, String computeId, String description) {
        try {
            // Create application module
            var appModule = new ApplicationModule();
            appModule.setAppModuleName(moduleName);
            appModule.setAppModuleVersion("1.0");
            appModule.setAppModuleDescription(description);

            String applicationModuleId = registryService.registerApplicationModule(gatewayId, appModule);
            System.out.println("✓ Application module created: " + applicationModuleId);

            // Create application interface
            var appInterface = new ApplicationInterfaceDescription();
            appInterface.setApplicationName(name);
            appInterface.setApplicationDescription(description);
            appInterface.setApplicationModules(Arrays.asList(applicationModuleId));

            // Add input
            var input = new InputDataObjectType();
            input.setName("input");
            input.setType(DataType.STRING);
            input.setInputOrder(1);
            input.setIsRequired(false);
            input.setUserFriendlyDescription("Input text");
            appInterface.setApplicationInputs(Arrays.asList(input));

            // Add outputs
            var stdout = new OutputDataObjectType();
            stdout.setName("STDOUT");
            stdout.setType(DataType.STDOUT);
            stdout.setIsRequired(false);

            var stderr = new OutputDataObjectType();
            stderr.setName("STDERR");
            stderr.setType(DataType.STDERR);
            stderr.setIsRequired(false);

            appInterface.setApplicationOutputs(Arrays.asList(stdout, stderr));

            String applicationInterfaceId = applicationService.registerApplicationInterface(gatewayId, appInterface);
            System.out.println("✓ Application interface created: " + applicationInterfaceId);

            // Create application deployment
            var appDeployment = new ApplicationDeploymentDescription();
            appDeployment.setAppModuleId(applicationModuleId);
            appDeployment.setComputeHostId(computeId);
            appDeployment.setExecutablePath(executable);
            appDeployment.setParallelism(ApplicationParallelismType.SERIAL);
            appDeployment.setAppDeploymentDescription(description + " on " + computeId);

            String applicationDeploymentId = applicationService.registerApplicationDeployment(gatewayId, appDeployment);
            System.out.println("✓ Application deployment created: " + applicationDeploymentId);
            System.out.println("✓ Application created successfully");
            return applicationInterfaceId;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create application: " + e.getMessage(), e);
        }
    }

    public void updateApplication(String appId, String gatewayId, String name, String description) {
        try {
            ApplicationInterfaceDescription app = applicationService.getApplicationInterface(appId);
            if (app == null) {
                throw new RuntimeException("Application not found: " + appId);
            }

            if (name != null) {
                app.setApplicationName(name);
            }
            if (description != null) {
                app.setApplicationDescription(description);
            }

            boolean updated = registryService.updateApplicationInterface(appId, app);
            if (updated) {
                System.out.println("✓ Application updated: " + appId);
            } else {
                System.out.println("⚠ Application update failed: " + appId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update application: " + e.getMessage(), e);
        }
    }

    public void deleteApplication(String appId) {
        try {
            boolean deleted = registryService.deleteApplicationInterface(appId);
            if (deleted) {
                System.out.println("✓ Application deleted: " + appId);
            } else {
                System.out.println("⚠ Application not found or could not be deleted: " + appId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete application: " + e.getMessage(), e);
        }
    }

    public ApplicationInterfaceDescription getApplication(String appId) {
        try {
            ApplicationInterfaceDescription app = applicationService.getApplicationInterface(appId);
            if (app == null) {
                throw new RuntimeException("Application not found: " + appId);
            }

            System.out.println("Application Details:");
            System.out.println("  ID: " + app.getApplicationInterfaceId());
            System.out.println("  Name: " + app.getApplicationName());
            System.out.println("  Description: "
                    + (app.getApplicationDescription() != null ? app.getApplicationDescription() : ""));
            if (app.getApplicationModules() != null
                    && !app.getApplicationModules().isEmpty()) {
                System.out.println("  Modules: " + app.getApplicationModules().size());
            }
            if (app.getApplicationInputs() != null
                    && !app.getApplicationInputs().isEmpty()) {
                System.out.println("  Inputs: " + app.getApplicationInputs().size());
            }
            if (app.getApplicationOutputs() != null
                    && !app.getApplicationOutputs().isEmpty()) {
                System.out.println("  Outputs: " + app.getApplicationOutputs().size());
            }
            return app;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get application: " + e.getMessage(), e);
        }
    }

    public List<ApplicationInterfaceDescription> listApplications(String gatewayId) {
        try {
            List<ApplicationInterfaceDescription> apps = applicationService.getAllApplicationInterfaces(gatewayId);
            if (apps.isEmpty()) {
                System.out.println("No applications registered.");
            } else {
                System.out.println("Registered Applications:");
                for (ApplicationInterfaceDescription app : apps) {
                    System.out.println("  " + app.getApplicationInterfaceId() + " -> " + app.getApplicationName());
                }
            }
            return apps;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list applications: " + e.getMessage(), e);
        }
    }

    public void enableApplicationForGroup(String appId, String groupId) {
        System.out.println("Note: Applications are automatically shared with admin groups if sharing is enabled.");
        System.out.println("Application " + appId + " is available for group " + groupId);
    }

    public void disableApplicationForGroup(String appId, String groupId) {
        System.out.println("Note: Application disabling functionality is not yet implemented.");
        System.out.println("Application " + appId + " disable request for group " + groupId);
    }
}
