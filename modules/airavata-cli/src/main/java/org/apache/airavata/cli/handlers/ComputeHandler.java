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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import org.apache.airavata.accountprovisioning.SSHUtil;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.ResourceJobManager;
import org.apache.airavata.common.model.ResourceJobManagerType;
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SSHJobSubmission;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.ComputeResourceService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComputeHandler {
    private static final Logger logger = LoggerFactory.getLogger(ComputeHandler.class);

    private final ComputeResourceService computeResourceService;
    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public ComputeHandler(
            ComputeResourceService computeResourceService,
            RegistryService registryService,
            CredentialStoreService credentialStoreService) {
        this.computeResourceService = computeResourceService;
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
    }

    public String registerComputeResource(
            String gatewayId,
            String name,
            String hostname,
            int port,
            ResourceJobManagerType jobManagerType,
            String loginUsername,
            String sshKeyPath,
            String passphrase) {
        try {
            // Create compute resource
            ComputeResourceDescription computeResource = new ComputeResourceDescription();
            computeResource.setHostName(hostname);
            computeResource.setResourceDescription("Compute resource: " + name);
            computeResource.setJobSubmissionInterfaces(new ArrayList<>());
            computeResource.setDataMovementInterfaces(new ArrayList<>());

            try {
                String computeResourceId = computeResourceService.addComputeResource(computeResource);
                System.out.println("✓ Compute resource registered: " + computeResourceId);

                // Add SSH job submission interface
                try {
                    SSHJobSubmission sshJobSubmission = new SSHJobSubmission();
                    sshJobSubmission.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
                    sshJobSubmission.setSshPort(port);

                    // Add resource job manager
                    ResourceJobManager jobManager = new ResourceJobManager();
                    jobManager.setResourceJobManagerType(jobManagerType);
                    sshJobSubmission.setResourceJobManager(jobManager);

                    registryService.addSSHJobSubmissionDetails(computeResourceId, 0, sshJobSubmission);
                    System.out.println("✓ SSH job submission interface added");

                    // Add SCP data movement interface
                    SCPDataMovement scpDataMovement = new SCPDataMovement();
                    scpDataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
                    scpDataMovement.setSshPort(port);
                    registryService.addSCPDataMovementDetails(
                            computeResourceId,
                            org.apache.airavata.common.model.DMType.COMPUTE_RESOURCE,
                            0,
                            scpDataMovement);
                    System.out.println("✓ SCP data movement interface added");
                } catch (Exception e) {
                    logger.warn("Failed to add interfaces: " + e.getMessage());
                }
            } catch (AppCatalogException e) {
                throw new RuntimeException("Failed to register compute resource: " + e.getMessage(), e);
            }

            // Create SSH credentials
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setUsername(loginUsername);
            sshCredential.setDescription("Compute resource credentials for " + name);

            if (sshKeyPath != null && !sshKeyPath.trim().isEmpty()) {
                try {
                    String privateKey = new String(Files.readAllBytes(Paths.get(sshKeyPath)));
                    sshCredential.setPrivateKey(privateKey);
                    String publicKeyPath = sshKeyPath + ".pub";
                    if (new File(publicKeyPath).exists()) {
                        sshCredential.setPublicKey(new String(Files.readAllBytes(Paths.get(publicKeyPath))));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to read SSH key file: " + e.getMessage(), e);
                }
            }
            if (passphrase != null) {
                sshCredential.setPassphrase(passphrase);
            }

            String credentialToken = credentialStoreService.addSSHCredential(sshCredential);
            System.out.println("✓ SSH credentials stored (token: " + credentialToken + ")");

            // Validate connection
            System.out.println("Validating compute resource connection...");
            SSHCredential storedCredential = credentialStoreService.getSSHCredential(credentialToken, gatewayId);
            boolean isValid = SSHUtil.validate(hostname, port, loginUsername, storedCredential);
            if (isValid) {
                System.out.println("✓ Compute resource connection validated successfully");
            } else {
                System.out.println("⚠ Warning: Compute resource connection validation failed");
            }
            return credentialToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register compute resource: " + e.getMessage(), e);
        }
    }

    public void updateComputeResource(String computeId, String hostname, String description, Boolean enabled) {
        try {
            ComputeResourceDescription compute = computeResourceService.getComputeResource(computeId);
            if (compute == null) {
                throw new RuntimeException("Compute resource not found: " + computeId);
            }

            if (hostname != null) {
                compute.setHostName(hostname);
            }
            if (description != null) {
                compute.setResourceDescription(description);
            }
            if (enabled != null) {
                compute.setEnabled(enabled);
            }

            computeResourceService.updateComputeResource(computeId, compute);
            System.out.println("✓ Compute resource updated: " + computeId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update compute resource: " + e.getMessage(), e);
        }
    }

    public void deleteComputeResource(String computeId) {
        try {
            computeResourceService.removeComputeResource(computeId);
            System.out.println("✓ Compute resource deleted: " + computeId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete compute resource: " + e.getMessage(), e);
        }
    }

    public Map<String, String> listComputeResources() {
        try {
            Map<String, String> computeResources = computeResourceService.getAllComputeResourceIdList();
            if (computeResources.isEmpty()) {
                System.out.println("No compute resources registered.");
            } else {
                System.out.println("Registered Compute Resources:");
                computeResources.forEach((id, hostname) -> {
                    System.out.println("  " + id + " -> " + hostname);
                });
            }
            return computeResources;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list compute resources: " + e.getMessage(), e);
        }
    }

    public void validateComputeResource(String computeId, String gatewayId) {
        try {
            ComputeResourceDescription compute = computeResourceService.getComputeResource(computeId);
            if (compute == null) {
                throw new RuntimeException("Compute resource not found: " + computeId);
            }

            System.out.println("✓ Compute resource found: " + computeId);
            System.out.println("  Hostname: " + compute.getHostName());
            System.out.println("  Enabled: " + compute.getEnabled());
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate compute resource: " + e.getMessage(), e);
        }
    }
}
