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
package org.apache.airavata.gateway.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.core.exception.CoreExceptions.AiravataException;
import org.apache.airavata.core.exception.DuplicateEntryException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.gateway.entity.GatewayEntity;
import org.apache.airavata.gateway.mapper.GatewayMapper;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.model.GatewayGroups;
import org.apache.airavata.gateway.repository.GatewayRepository;
import org.apache.airavata.iam.exception.SharingRegistryException;
import org.apache.airavata.iam.model.Domain;
import org.apache.airavata.iam.model.EntityType;
import org.apache.airavata.iam.model.PermissionType;
import org.apache.airavata.iam.model.SharingResourceType;
import org.apache.airavata.iam.model.User;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.iam.service.SharingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link GatewayService}.
 *
 * <p>Covers three concerns that all operate on the gateway entity:
 * <ol>
 *   <li>Core gateway CRUD (PK = gatewayId UUID, slug = gatewayName).</li>
 *   <li>Gateway-groups CRUD — group IDs are stored as columns on the gateway entity row.</li>
 *   <li>One-time gateway / sharing-registry initialization (run at startup or via InitCommand).</li>
 * </ol>
 */
@Service
@Lazy(false)
@Transactional
public class DefaultGatewayService implements GatewayService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGatewayService.class);

    private final GatewayRepository gatewayRepository;
    private final GatewayMapper gatewayMapper;
    private final ServerProperties properties;
    private final SharingService sharingService;
    private final CredentialStoreService credentialStoreService;
    private final Environment environment;

    public DefaultGatewayService(
            GatewayRepository gatewayRepository,
            GatewayMapper gatewayMapper,
            ServerProperties properties,
            SharingService sharingService,
            CredentialStoreService credentialStoreService,
            Environment environment) {
        this.gatewayRepository = gatewayRepository;
        this.gatewayMapper = gatewayMapper;
        this.properties = properties;
        this.sharingService = sharingService;
        this.credentialStoreService = credentialStoreService;
        this.environment = environment;
    }

    // =========================================================================
    // Gateway CRUD
    // =========================================================================

    /**
     * Check if a gateway exists by name (slug) or by primary key (UUID).
     * Used by RegistryService, SharingService when caller may pass either.
     */
    public boolean isGatewayExist(String nameOrId) throws RegistryException {
        return gatewayRepository.existsByGatewayName(nameOrId) || gatewayRepository.existsById(nameOrId);
    }

    /**
     * Get a gateway by its name (slug) or by primary key (UUID). Tries slug first, then PK.
     *
     * @param gatewayNameOrId the gateway name (slug) or gateway ID (UUID)
     * @return the Gateway model or null if not found
     */
    public Gateway getGateway(String gatewayNameOrId) throws RegistryException {
        var entity = gatewayRepository.findByGatewayName(gatewayNameOrId).orElse(null);
        if (entity == null) {
            entity = gatewayRepository.findById(gatewayNameOrId).orElse(null);
        }
        if (entity == null) return null;
        return gatewayMapper.toModel(entity);
    }

    /**
     * Get all gateways.
     *
     * @return list of all Gateway models
     */
    public List<Gateway> getAllGateways() throws RegistryException {
        var entities = gatewayRepository.findAllGateways();
        return gatewayMapper.toModelList(entities);
    }

    /**
     * Delete a gateway by its human-readable gateway name (slug).
     *
     * @param gatewayName the gateway name (slug)
     */
    public void deleteGateway(String gatewayName) throws RegistryException {
        var entity = gatewayRepository.findByGatewayName(gatewayName).orElse(null);
        if (entity != null) {
            gatewayRepository.delete(entity);
        }
    }

    /**
     * Create a new gateway.
     *
     * @param gateway the Gateway model to create
     * @return the gatewayId of the created gateway
     */
    public String createGateway(Gateway gateway) throws RegistryException {
        var entity = gatewayMapper.toEntity(gateway);
        if (entity.getGatewayId() == null || entity.getGatewayId().isEmpty()) {
            entity.setGatewayId(UUID.randomUUID().toString());
        }
        var saved = gatewayRepository.save(entity);
        return saved.getGatewayId();
    }

    /**
     * Update an existing gateway by its name (slug).
     *
     * @param gatewayName the gateway name (slug)
     * @param gateway the updated Gateway model
     */
    public void updateGateway(String gatewayName, Gateway gateway) throws RegistryException {
        var existingEntity = gatewayRepository.findByGatewayName(gatewayName).orElse(null);
        if (existingEntity == null) {
            throw new RegistryException("Gateway not found with name: " + gatewayName);
        }
        var entity = gatewayMapper.toEntity(gateway);
        entity.setGatewayId(existingEntity.getGatewayId());
        entity.setGatewayName(gatewayName);
        gatewayRepository.save(entity);
    }

    // =========================================================================
    // Gateway groups
    // =========================================================================

    /**
     * Returns true if the gateway has gateway groups configured (non-null adminsGroupId).
     */
    @Override
    public boolean isGatewayGroupsExists(String gatewayId) throws RegistryException {
        return gatewayRepository
                .findByGatewayNameOrId(gatewayId)
                .map(entity -> entity.getAdminsGroupId() != null)
                .orElse(false);
    }

    /**
     * Returns the GatewayGroups for the given gateway, or null if the gateway does not exist.
     */
    @Override
    public GatewayGroups getGatewayGroups(String gatewayId) throws RegistryException {
        GatewayEntity entity =
                gatewayRepository.findByGatewayNameOrId(gatewayId).orElse(null);
        if (entity == null) return null;
        return toGatewayGroupsModel(entity);
    }

    /**
     * Creates (or replaces) the gateway groups for the given gateway.
     * Delegates to {@link #updateGatewayGroups(GatewayGroups)}.
     */
    @Override
    public GatewayGroups createGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        return updateGatewayGroups(gatewayGroups);
    }

    /**
     * Updates the gateway groups for the given gateway by writing the group IDs onto the entity.
     */
    @Override
    public GatewayGroups updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException {
        GatewayEntity entity = gatewayRepository
                .findByGatewayNameOrId(gatewayGroups.getGatewayId())
                .orElse(null);
        if (entity == null) {
            throw new RegistryException("Gateway not found: " + gatewayGroups.getGatewayId());
        }
        entity.setAdminsGroupId(gatewayGroups.getAdminsGroupId());
        entity.setReadOnlyAdminsGroupId(gatewayGroups.getReadOnlyAdminsGroupId());
        entity.setDefaultGatewayUsersGroupId(gatewayGroups.getDefaultGatewayUsersGroupId());
        GatewayEntity saved = gatewayRepository.save(entity);
        return toGatewayGroupsModel(saved);
    }

    /**
     * Clears all gateway group IDs for the given gateway (sets them to null).
     */
    @Override
    public void deleteGatewayGroups(String gatewayId) throws RegistryException {
        GatewayEntity entity =
                gatewayRepository.findByGatewayNameOrId(gatewayId).orElse(null);
        if (entity != null) {
            entity.setAdminsGroupId(null);
            entity.setReadOnlyAdminsGroupId(null);
            entity.setDefaultGatewayUsersGroupId(null);
            gatewayRepository.save(entity);
        }
    }

    private GatewayGroups toGatewayGroupsModel(GatewayEntity entity) {
        var model = new GatewayGroups();
        model.setGatewayId(entity.getGatewayId());
        model.setAdminsGroupId(entity.getAdminsGroupId());
        model.setReadOnlyAdminsGroupId(entity.getReadOnlyAdminsGroupId());
        model.setDefaultGatewayUsersGroupId(entity.getDefaultGatewayUsersGroupId());
        return model;
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Called automatically at application startup.
     * Skipped when the "init" Spring profile is active (InitCommand handles that path).
     */
    @Override
    @jakarta.annotation.PostConstruct
    public void init() throws AiravataException {
        if (Arrays.asList(environment.getActiveProfiles()).contains("init")) {
            logger.info("[BEAN-INIT] DefaultGatewayService.init() skipped (init profile active)");
            return;
        }
        logger.info("[BEAN-INIT] DefaultGatewayService.init() called");
        doInit();
    }

    /**
     * Called explicitly after database migration via InitCommand (--init profile).
     */
    @Override
    public void initializeAfterMigration() throws AiravataException {
        logger.info("DefaultGatewayService.initializeAfterMigration() called");
        doInit();
    }

    private void doInit() throws AiravataException {
        try {
            initSharingRegistry();
        } catch (SharingRegistryException | DuplicateEntryException e) {
            String msg = String.format("Error while initializing sharing registry: %s", e.getMessage());
            if (isConnectionError(e)) {
                logger.warn(
                        "Database not available during sharing registry initialization."
                                + " Will retry when database is available: {}",
                        e.getMessage());
            } else {
                logger.error(msg, e);
                throw new AiravataException(msg, e);
            }
        } catch (Exception e) {
            if (isConnectionError(e)) {
                logger.warn(
                        "Database not available during sharing registry initialization."
                                + " Will retry when database is available: {}",
                        e.getMessage());
            } else {
                String msg = String.format("Error while initializing sharing registry: %s", e.getMessage());
                logger.error(msg, e);
                throw new AiravataException(msg, e);
            }
        }

        try {
            postInitDefaultGateway();
        } catch (CredentialStoreException e) {
            logger.warn(
                    "Error while post-initializing default gateway: {}. Gateway initialization will be skipped.",
                    e.getMessage(),
                    e);
        } catch (Exception e) {
            if (isConnectionError(e)) {
                logger.warn(
                        "Database not available during gateway initialization."
                                + " Will retry when database is available: {}",
                        e.getMessage());
            } else {
                logger.warn(
                        "Error while post-initializing default gateway: {}. Gateway initialization will be skipped.",
                        e.getMessage(),
                        e);
            }
        }
    }

    private void postInitDefaultGateway() throws CredentialStoreException {
        String defaultGateway = properties.defaultGateway();
        if (defaultGateway == null || defaultGateway.isEmpty()) {
            logger.debug("No default gateway configured. Skipping gateway initialization.");
            return;
        }
        try {
            if (!isGatewayExist(defaultGateway)) {
                logger.debug(
                        "Default gateway '{}' does not exist in database. Skipping credential initialization.",
                        defaultGateway);
                return;
            }
        } catch (Exception e) {
            logger.warn("Could not verify default gateway existence: {}", e.getMessage());
            return;
        }
        logger.debug("Starting to add password credential to default gateway={}", defaultGateway);
        var passwordCredential = new PasswordCredential();
        passwordCredential.setUserId(properties.security().iam().superAdmin().username());
        passwordCredential.setGatewayId(defaultGateway);
        passwordCredential.setLoginUserName(
                properties.security().iam().superAdmin().username());
        passwordCredential.setPassword(properties.security().iam().superAdmin().password());
        passwordCredential.setDescription("Credentials for default gateway=" + defaultGateway);
        logger.info("Creating password credential for default gateway={}", defaultGateway);
        String token = credentialStoreService.addPasswordCredential(passwordCredential);
        if (token != null) {
            logger.debug("Added password credential token={} to the default gateway={}", token, defaultGateway);
        }
    }

    private void initSharingRegistry() throws SharingRegistryException, DuplicateEntryException {
        String defaultGateway = properties.defaultGateway();
        if (defaultGateway == null || defaultGateway.isEmpty()) {
            logger.debug("No default gateway configured. Skipping sharing registry initialization.");
            return;
        }

        try {
            if (!isGatewayExist(defaultGateway)) {
                var gateway = new Gateway();
                gateway.setGatewayId(defaultGateway);
                gateway.setGatewayName(defaultGateway);
                gateway.setDomain(defaultGateway);
                createGateway(gateway);
                logger.info("Created default gateway: {}", defaultGateway);
            }
        } catch (Exception e) {
            logger.warn("Could not create or verify default gateway: {}", e.getMessage());
        }

        if (!sharingService.isDomainExists(defaultGateway)) {
            var domain = new Domain();
            domain.setDomainId(defaultGateway);
            domain.setName(defaultGateway);
            domain.setDescription("Domain entry for " + domain.getName());
            sharingService.createDomain(domain);

            var user = new User();
            user.setDomainId(domain.getDomainId());
            user.setUserId(properties.security().iam().superAdmin().username() + "@" + defaultGateway);
            user.setUserName(properties.security().iam().superAdmin().username());
            sharingService.createUser(user);

            createEntityType(domain.getDomainId(), "PROJECT", "Project entity type");
            createEntityType(domain.getDomainId(), "EXPERIMENT", "Experiment entity type");
            createEntityType(domain.getDomainId(), "FILE", "File entity type");
            createEntityType(
                    domain.getDomainId(),
                    SharingResourceType.APPLICATION_DEPLOYMENT.name(),
                    "Application Deployment entity type");
            createEntityType(
                    domain.getDomainId(),
                    SharingResourceType.GROUP_RESOURCE_PROFILE.name(),
                    "Group Resource Profile entity type");
            createEntityType(
                    domain.getDomainId(),
                    SharingResourceType.CREDENTIAL_TOKEN.name(),
                    "Credential Store Token entity type");
            createEntityType(
                    domain.getDomainId(),
                    SharingResourceType.APPLICATION_INTERFACE.name(),
                    "Application Interface entity type");
            createEntityType(
                    domain.getDomainId(), SharingResourceType.COMPUTE_RESOURCE.name(), "Compute Resource entity type");
            createEntityType(
                    domain.getDomainId(), SharingResourceType.STORAGE_RESOURCE.name(), "Storage Resource entity type");

            createPermissionType(domain.getDomainId(), "READ", "Read permission type");
            createPermissionType(domain.getDomainId(), "WRITE", "Write permission type");
            createPermissionType(domain.getDomainId(), "MANAGE_SHARING", "Sharing permission type");
        }
    }

    private void createEntityType(String domainId, String typeName, String description)
            throws SharingRegistryException, DuplicateEntryException {
        var entityType = new EntityType();
        entityType.setEntityTypeId(domainId + ":" + typeName);
        entityType.setDomainId(domainId);
        entityType.setName(typeName);
        entityType.setDescription(description);
        sharingService.createEntityType(entityType);
    }

    private void createPermissionType(String domainId, String typeName, String description)
            throws SharingRegistryException, DuplicateEntryException {
        var permissionType = new PermissionType();
        permissionType.setPermissionTypeId(domainId + ":" + typeName);
        permissionType.setDomainId(domainId);
        permissionType.setName(typeName);
        permissionType.setDescription(description);
        sharingService.createPermissionType(permissionType);
    }

    /**
     * Returns true if the exception (or its cause) represents a transient database connection error.
     */
    private static boolean isConnectionError(Exception e) {
        Throwable t = e.getCause() != null ? e.getCause() : e;
        String msg = t.getMessage();
        return msg != null
                && (msg.contains("Connection refused")
                        || msg.contains("Connection is not available")
                        || msg.contains("Unable to acquire JDBC Connection"));
    }
}
