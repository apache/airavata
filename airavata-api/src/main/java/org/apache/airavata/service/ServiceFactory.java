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
package org.apache.airavata.service;

import org.apache.airavata.orchestrator.exception.OrchestratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread-safe singleton factory for creating and managing service instances.
 * Ensures only one instance of each service exists across all processes and threads.
 */
public class ServiceFactory {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

    private static volatile ServiceFactory instance;
    private static final Object lock = new Object();

    // Service instances - initialized eagerly
    private volatile RegistryService registryService;
    private volatile CredentialStoreService credentialStoreService;
    private volatile SharingRegistryService sharingRegistryService;
    private volatile OrchestratorService orchestratorService;
    private volatile TenantProfileService tenantProfileService;
    private volatile UserProfileService userProfileService;
    private volatile IamAdminService iamAdminService;
    private volatile GroupManagerService groupManagerService;

    // Initialization flag
    private volatile boolean initialized = false;

    private ServiceFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the singleton instance of ServiceFactory.
     * Uses double-checked locking for thread safety.
     */
    public static ServiceFactory getInstance() throws ServiceFactoryException {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ServiceFactory();
                    instance.initializeServices();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize all service instances eagerly.
     * This method is called once during singleton initialization.
     */
    private void initializeServices() throws ServiceFactoryException {
        if (initialized) {
            return;
        }

        synchronized (lock) {
            if (initialized) {
                return;
            }

            try {
                logger.info("Initializing ServiceFactory and all service instances...");

                // Initialize services that don't have dependencies first
                registryService = new RegistryService();
                logger.info("Initialized RegistryService");

                sharingRegistryService = new SharingRegistryService();
                logger.info("Initialized SharingRegistryService");

                userProfileService = new UserProfileService();
                logger.info("Initialized UserProfileService");

                groupManagerService = new GroupManagerService();
                logger.info("Initialized GroupManagerService");

                // Initialize CredentialStoreService (may throw ApplicationSettingsException)
                try {
                    credentialStoreService = new CredentialStoreService();
                    logger.info("Initialized CredentialStoreService");
                } catch (Exception e) {
                    logger.error("Failed to initialize CredentialStoreService", e);
                    throw new ServiceFactoryException("Failed to initialize CredentialStoreService", e);
                }

                // Initialize TenantProfileService (depends on CredentialStoreService)
                try {
                    tenantProfileService = new TenantProfileService();
                    logger.info("Initialized TenantProfileService");
                } catch (Exception e) {
                    logger.error("Failed to initialize TenantProfileService", e);
                    throw new ServiceFactoryException("Failed to initialize TenantProfileService", e);
                }

                // Initialize IamAdminService (depends on CredentialStoreService and RegistryService)
                try {
                    iamAdminService = new IamAdminService(credentialStoreService, registryService);
                    logger.info("Initialized IamAdminService");
                } catch (Exception e) {
                    logger.error("Failed to initialize IamAdminService", e);
                    throw new ServiceFactoryException("Failed to initialize IamAdminService", e);
                }

                // Initialize OrchestratorService (may throw OrchestratorException)
                try {
                    orchestratorService = new OrchestratorService();
                    logger.info("Initialized OrchestratorService");
                } catch (OrchestratorException e) {
                    logger.error("Failed to initialize OrchestratorService", e);
                    throw new ServiceFactoryException("Failed to initialize OrchestratorService", e);
                }

                initialized = true;
                logger.info("ServiceFactory initialization completed successfully");

            } catch (ServiceFactoryException e) {
                logger.error("Critical error during ServiceFactory initialization", e);
                throw e;
            }
        }
    }

    /**
     * Get the RegistryService instance.
     */
    public RegistryService getRegistryService() {
        ensureInitialized();
        return registryService;
    }

    /**
     * Get the CredentialStoreService instance.
     */
    public CredentialStoreService getCredentialStoreService() {
        ensureInitialized();
        return credentialStoreService;
    }

    /**
     * Get the SharingRegistryService instance.
     */
    public SharingRegistryService getSharingRegistryService() {
        ensureInitialized();
        return sharingRegistryService;
    }

    /**
     * Get the OrchestratorService instance.
     */
    public OrchestratorService getOrchestratorService() {
        ensureInitialized();
        return orchestratorService;
    }

    /**
     * Get the TenantProfileService instance.
     */
    public TenantProfileService getTenantProfileService() {
        ensureInitialized();
        return tenantProfileService;
    }

    /**
     * Get the UserProfileService instance.
     */
    public UserProfileService getUserProfileService() {
        ensureInitialized();
        return userProfileService;
    }

    /**
     * Get the IamAdminService instance.
     */
    public IamAdminService getIamAdminService() {
        ensureInitialized();
        return iamAdminService;
    }

    /**
     * Get the GroupManagerService instance.
     */
    public GroupManagerService getGroupManagerService() {
        ensureInitialized();
        return groupManagerService;
    }

    /**
     * Ensure services are initialized before access.
     */
    private void ensureInitialized() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    try {
                        initializeServices();
                    } catch (ServiceFactoryException e) {
                        throw new IllegalStateException("ServiceFactory initialization failed", e);
                    }
                }
            }
        }
    }
}
