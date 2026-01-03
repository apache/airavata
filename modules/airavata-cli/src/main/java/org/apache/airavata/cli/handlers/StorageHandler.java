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
import org.apache.airavata.common.model.SCPDataMovement;
import org.apache.airavata.common.model.SecurityProtocol;
import org.apache.airavata.common.model.StorageResourceDescription;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.registry.exception.AppCatalogException;
import org.apache.airavata.registry.services.StorageResourceService;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StorageHandler {
    private static final Logger logger = LoggerFactory.getLogger(StorageHandler.class);

    private final StorageResourceService storageResourceService;
    private final RegistryService registryService;
    private final CredentialStoreService credentialStoreService;

    public StorageHandler(
            StorageResourceService storageResourceService,
            RegistryService registryService,
            CredentialStoreService credentialStoreService) {
        this.storageResourceService = storageResourceService;
        this.registryService = registryService;
        this.credentialStoreService = credentialStoreService;
    }

    public String registerStorageResource(
            String gatewayId,
            String name,
            String hostname,
            int port,
            String loginUsername,
            String sshKeyPath,
            String passphrase) {
        try {
            // Create storage resource
            StorageResourceDescription storageResource = new StorageResourceDescription();
            storageResource.setHostName(hostname);
            storageResource.setStorageResourceDescription("Storage resource: " + name);
            storageResource.setEnabled(true);
            storageResource.setDataMovementInterfaces(new ArrayList<>());

            try {
                String storageResourceId = storageResourceService.addStorageResource(storageResource);
                System.out.println("✓ Storage resource registered: " + storageResourceId);

                // Add SCP data movement interface
                try {
                    SCPDataMovement scpDataMovement = new SCPDataMovement();
                    scpDataMovement.setSecurityProtocol(SecurityProtocol.SSH_KEYS);
                    scpDataMovement.setSshPort(port);
                    registryService.addSCPDataMovementDetails(
                            storageResourceId,
                            org.apache.airavata.common.model.DMType.STORAGE_RESOURCE,
                            0,
                            scpDataMovement);
                    System.out.println("✓ SCP data movement interface added");
                } catch (Exception e) {
                    logger.warn("Failed to add SCP data movement interface: " + e.getMessage());
                }
            } catch (AppCatalogException e) {
                throw new RuntimeException("Failed to register storage resource: " + e.getMessage(), e);
            }

            // Create SSH credentials
            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setGatewayId(gatewayId);
            sshCredential.setUsername(loginUsername);
            sshCredential.setDescription("Storage resource credentials for " + name);

            if (sshKeyPath != null && !sshKeyPath.trim().isEmpty()) {
                // Load existing key
                try {
                    String privateKey = new String(Files.readAllBytes(Paths.get(sshKeyPath)));
                    sshCredential.setPrivateKey(privateKey);
                    // Try to load public key
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
            System.out.println("Validating storage resource connection...");
            SSHCredential storedCredential = credentialStoreService.getSSHCredential(credentialToken, gatewayId);
            boolean isValid = SSHUtil.validate(hostname, port, loginUsername, storedCredential);
            if (isValid) {
                System.out.println("✓ Storage resource connection validated successfully");
            } else {
                System.out.println("⚠ Warning: Storage resource connection validation failed");
            }
            return credentialToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to register storage resource: " + e.getMessage(), e);
        }
    }

    public void updateStorageResource(String storageId, String hostname, String description, Boolean enabled) {
        try {
            StorageResourceDescription storage = storageResourceService.getStorageResource(storageId);
            if (storage == null) {
                throw new RuntimeException("Storage resource not found: " + storageId);
            }

            if (hostname != null) {
                storage.setHostName(hostname);
            }
            if (description != null) {
                storage.setStorageResourceDescription(description);
            }
            if (enabled != null) {
                storage.setEnabled(enabled);
            }

            storageResourceService.updateStorageResource(storageId, storage);
            System.out.println("✓ Storage resource updated: " + storageId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update storage resource: " + e.getMessage(), e);
        }
    }

    public void deleteStorageResource(String storageId) {
        try {
            storageResourceService.removeStorageResource(storageId);
            System.out.println("✓ Storage resource deleted: " + storageId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete storage resource: " + e.getMessage(), e);
        }
    }

    public Map<String, String> listStorageResources() {
        try {
            Map<String, String> storageResources = storageResourceService.getAllStorageResourceIdList();
            if (storageResources.isEmpty()) {
                System.out.println("No storage resources registered.");
            } else {
                System.out.println("Registered Storage Resources:");
                storageResources.forEach((id, hostname) -> {
                    System.out.println("  " + id + " -> " + hostname);
                });
            }
            return storageResources;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list storage resources: " + e.getMessage(), e);
        }
    }

    public void validateStorageResource(String storageId, String gatewayId) {
        try {
            StorageResourceDescription storage = storageResourceService.getStorageResource(storageId);
            if (storage == null) {
                throw new RuntimeException("Storage resource not found: " + storageId);
            }

            System.out.println("✓ Storage resource found: " + storageId);
            System.out.println("  Hostname: " + storage.getHostName());
            System.out.println("  Enabled: " + storage.getEnabled());
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate storage resource: " + e.getMessage(), e);
        }
    }
}
