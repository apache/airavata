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
package org.apache.airavata.credential.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.credential.util.KeyStorePasswordCallback;
import org.apache.airavata.credential.util.SecurityUtil;
import org.apache.airavata.config.ConfigResolver;
import org.apache.airavata.config.ServerProperties;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.model.CertificateCredential;
import org.apache.airavata.credential.model.Credential;
import org.apache.airavata.credential.entity.CredentialEntity;
import org.apache.airavata.credential.repository.CredentialRepository;
import org.apache.airavata.credential.model.PasswordCredential;
import org.apache.airavata.credential.model.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing credential entities with encryption/decryption support.
 */
@Service
@Transactional
public class CredentialEntityService {

    private static final Logger logger = LoggerFactory.getLogger(CredentialEntityService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CredentialRepository credentialRepository;
    private final KeyStorePasswordCallback keyStorePasswordCallback;
    private final Environment environment;

    private String keyStorePath;
    private String secretKeyAlias;

    public CredentialEntityService(
            CredentialRepository credentialRepository,
            ServerProperties properties,
            KeyStorePasswordCallback keyStorePasswordCallback,
            Environment environment) {
        this.credentialRepository = credentialRepository;
        this.keyStorePasswordCallback = keyStorePasswordCallback;
        this.environment = environment;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        var configDir = ConfigResolver.getConfigDir(); // Will throw if not found
        // Use Environment instead of properties object as it's populated earlier in Spring lifecycle
        var credentialStoreKeyStorePath = environment.getProperty("airavata.security.vault.keystore.url");
        if (credentialStoreKeyStorePath == null || credentialStoreKeyStorePath.isEmpty()) {
            // In test profile, use default keystore location
            boolean isTestProfile = environment != null
                    && Arrays.asList(environment.getActiveProfiles()).contains("test");
            if (isTestProfile) {
                credentialStoreKeyStorePath = "conf/keystores/airavata.sym.p12";
                logger.debug("Test profile detected, using default keystore path: {}", credentialStoreKeyStorePath);
            } else {
                throw new IllegalStateException(
                        "Keystore configuration is missing: airavata.security.vault.keystore.url is not set in application.properties");
            }
        }
        // Keystore path is relative to configDir (e.g., "keystores/airavata.sym.p12")
        this.keyStorePath = new File(configDir, credentialStoreKeyStorePath).getAbsolutePath();
        var aliasFromEnv = environment.getProperty("airavata.security.vault.keystore.alias");
        this.secretKeyAlias = aliasFromEnv != null ? aliasFromEnv : "airavata";

        // Verify keystore password is set (required for encryption/decryption)
        boolean isTestProfile = environment != null
                && Arrays.asList(environment.getActiveProfiles()).contains("test");
        var keystorePassword = environment.getProperty("airavata.security.vault.keystore.password");
        if (keystorePassword == null || keystorePassword.isEmpty()) {
            if (isTestProfile) {
                // In test profile, use default password if not set
                keystorePassword = "airavata";
                logger.debug("Test profile detected, using default keystore password");
            } else {
                throw new IllegalStateException(
                        "Keystore password is missing: airavata.security.vault.keystore.password is not set in application.properties");
            }
        }

        // Verify keystore file exists
        var keystoreFile = new File(this.keyStorePath);
        if (!keystoreFile.exists()) {
            logger.warn(
                    "Keystore file does not exist at: {}. Credential encryption/decryption may fail.",
                    this.keyStorePath);
        }
    }

    /**
     * Add or update credentials.
     *
     * @param gatewayId the gateway ID
     * @param credential the credential to save
     * @throws CredentialStoreException if an error occurs
     */
    public void saveCredential(String gatewayId, Credential credential) throws CredentialStoreException {
        try {
            var entity = new CredentialEntity();
            entity.setCredentialId(credential.getToken());
            entity.setGatewayId(gatewayId);
            entity.setType(credentialType(credential));
            entity.setCredentialData(serializeCredential(credential));
            entity.setUserId(credential.getUserId());
            entity.setName(credential.getName());
            entity.setCreatedAt(IdGenerator.getUniqueTimestamp());
            entity.setDescription(credential.getDescription());
            credentialRepository.save(entity);
        } catch (Exception e) {
            var msg = String.format(
                    "Error saving credential for gateway: %s, token: %s", gatewayId, credential.getToken());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Check if this credential is referenced by RESOURCE_BINDING or CREDENTIAL_ALLOCATION_PROJECT.
     */
    public boolean hasReferences(String credentialId) {
        return credentialRepository.countReferences(credentialId) > 0;
    }

    /**
     * Delete credentials.
     * Fails if the credential is still referenced by resource bindings or allocation projects.
     */
    public void deleteCredential(String gatewayId, String credentialId) throws CredentialStoreException {
        try {
            if (!credentialRepository.existsById(credentialId)) {
                logger.debug("Credential not found: {}, skipping delete", credentialId);
                return;
            }
            if (hasReferences(credentialId)) {
                throw new CredentialStoreException(
                        "Cannot delete credential: it is still referenced by resource bindings or allocation projects. Remove those references first.");
            }
            credentialRepository.deleteByGatewayIdAndCredentialId(gatewayId, credentialId);
            credentialRepository.flush();
        } catch (CredentialStoreException e) {
            throw e;
        } catch (Exception e) {
            var msg = String.format("Error deleting credential for gateway: %s, id: %s", gatewayId, credentialId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Check if a credential exists.
     */
    public boolean credentialExists(String gatewayId, String credentialId) {
        return credentialRepository.findByGatewayIdAndCredentialId(gatewayId, credentialId).isPresent();
    }

    /**
     * Get credential by gateway ID and credential ID.
     */
    public Credential getCredential(String gatewayId, String credentialId) throws CredentialStoreException {
        var entityOpt = credentialRepository.findByGatewayIdAndCredentialId(gatewayId, credentialId);
        if (entityOpt.isEmpty()) {
            var msg = String.format("Credential not found for gateway: %s, id: %s", gatewayId, credentialId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
        var entity = entityOpt.get();
        try {
            return entityToCredential(entity);
        } catch (Exception e) {
            var msg = String.format("Error retrieving credential for gateway: %s, id: %s", gatewayId, credentialId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Get gateway ID by credential ID.
     */
    public String getGatewayId(String credentialId) throws CredentialStoreException {
        Optional<String> gatewayIdOpt = credentialRepository.findGatewayIdByCredentialId(credentialId);
        return gatewayIdOpt.orElse(null);
    }

    /**
     * Get all credentials for a gateway.
     */
    public List<Credential> getCredentials(String gatewayId) throws CredentialStoreException {
        return getCredentialsInternal(gatewayId, null);
    }

    /**
     * Get credentials for a gateway with specific credential IDs.
     * If accessibleCredentialIds is null, returns all credentials for the gateway.
     * If accessibleCredentialIds is empty, returns an empty list.
     */
    public List<Credential> getCredentials(String gatewayId, List<String> accessibleCredentialIds)
            throws CredentialStoreException {
        if (accessibleCredentialIds != null && accessibleCredentialIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getCredentialsInternal(gatewayId, accessibleCredentialIds);
    }

    private List<Credential> getCredentialsInternal(String gatewayId, List<String> accessibleCredentialIds)
            throws CredentialStoreException {
        List<CredentialEntity> entities;
        if (accessibleCredentialIds != null && !accessibleCredentialIds.isEmpty()) {
            entities = credentialRepository.findByGatewayIdAndCredentialIdIn(gatewayId, accessibleCredentialIds);
        } else {
            entities = credentialRepository.findByGatewayId(gatewayId);
        }

        var credentials = new ArrayList<Credential>();
        for (var entity : entities) {
            try {
                credentials.add(entityToCredential(entity));
            } catch (Exception e) {
                var msg = String.format(
                        "Error converting entity to credential for gateway: %s, id: %s",
                        gatewayId, entity.getCredentialId());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
        }
        return credentials;
    }

    /**
     * Get credential IDs for a gateway owned by the given user.
     */
    public List<String> getCredentialIdsByGatewayIdAndUserId(String gatewayId, String userId) {
        var entities = credentialRepository.findByGatewayIdAndUserId(gatewayId, userId);
        return entities.stream().map(CredentialEntity::getCredentialId).toList();
    }

    /**
     * Get all credentials.
     */
    public List<Credential> getAllCredentials() throws CredentialStoreException {
        var entities = credentialRepository.findAll();
        var credentials = new ArrayList<Credential>();
        for (var entity : entities) {
            try {
                credentials.add(entityToCredential(entity));
            } catch (Exception e) {
                var msg = String.format(
                        "Error converting entity to credential for gateway: %s, id: %s",
                        entity.getGatewayId(), entity.getCredentialId());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
        }
        return credentials;
    }

    /**
     * Convert entity to Credential model: decrypt + JSON deserialize + populate base fields.
     */
    private Credential entityToCredential(CredentialEntity entity) throws CredentialStoreException {
        var credential = deserializeCredential(entity.getType(), entity.getCredentialData());
        credential.setToken(entity.getCredentialId());
        credential.setUserId(entity.getUserId());
        credential.setName(entity.getName());
        credential.setPersistedTime(entity.getCreatedAt());
        credential.setDescription(entity.getDescription());
        return credential;
    }

    /**
     * Determine the type discriminator string for a credential.
     */
    private String credentialType(Credential credential) {
        return switch (credential) {
            case SSHCredential _ -> "SSH";
            case PasswordCredential _ -> "PASSWORD";
            case CertificateCredential _ -> "CERTIFICATE";
        };
    }

    /**
     * Determine the concrete class for a type discriminator string.
     */
    private Class<? extends Credential> credentialClass(String type) throws CredentialStoreException {
        return switch (type) {
            case "SSH" -> SSHCredential.class;
            case "PASSWORD" -> PasswordCredential.class;
            case "CERTIFICATE" -> CertificateCredential.class;
            default -> throw new CredentialStoreException("Unknown credential type: " + type);
        };
    }

    /**
     * Serialize a credential to JSON bytes, then encrypt if enabled.
     */
    private byte[] serializeCredential(Credential credential) throws CredentialStoreException {
        try {
            byte[] json = objectMapper.writeValueAsBytes(credential);
            if (shouldEncrypt()) {
                return SecurityUtil.encrypt(keyStorePath, secretKeyAlias, keyStorePasswordCallback, json);
            }
            return json;
        } catch (IOException | GeneralSecurityException e) {
            throw new CredentialStoreException("Error serializing credential: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt (if enabled) then deserialize JSON bytes to a typed credential.
     */
    private Credential deserializeCredential(String type, byte[] data) throws CredentialStoreException {
        try {
            if (shouldEncrypt()) {
                data = SecurityUtil.decrypt(keyStorePath, secretKeyAlias, keyStorePasswordCallback, data);
            }
            return objectMapper.readValue(data, credentialClass(type));
        } catch (IOException | GeneralSecurityException e) {
            throw new CredentialStoreException("Error deserializing credential: " + e.getMessage(), e);
        }
    }

    /**
     * Check if encryption should be enabled.
     */
    private boolean shouldEncrypt() {
        return keyStorePath != null;
    }
}
