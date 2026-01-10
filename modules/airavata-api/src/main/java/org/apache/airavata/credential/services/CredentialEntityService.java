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
package org.apache.airavata.credential.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.entities.CredentialEntity;
import org.apache.airavata.credential.exception.CredentialStoreException;
import org.apache.airavata.credential.repositories.CredentialRepository;
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

    private final CredentialRepository credentialRepository;
    private final AiravataServerProperties properties;
    private final DefaultKeyStorePasswordCallback keyStorePasswordCallback;
    private final Environment environment;

    private String keyStorePath;
    private String secretKeyAlias;

    public CredentialEntityService(
            CredentialRepository credentialRepository,
            AiravataServerProperties properties,
            DefaultKeyStorePasswordCallback keyStorePasswordCallback,
            Environment environment) {
        this.credentialRepository = credentialRepository;
        this.properties = properties;
        this.keyStorePasswordCallback = keyStorePasswordCallback;
        this.environment = environment;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        String configDir = org.apache.airavata.config.AiravataConfigUtils.getConfigDir(); // Will throw if not found
        // Use Environment instead of properties object as it's populated earlier in Spring lifecycle
        String credentialStoreKeyStorePath = environment.getProperty("security.vault.keystore.url");
        if (credentialStoreKeyStorePath == null || credentialStoreKeyStorePath.isEmpty()) {
            // In test profile, use default keystore location
            boolean isTestProfile = environment != null
                    && java.util.Arrays.asList(environment.getActiveProfiles()).contains("test");
            if (isTestProfile) {
                credentialStoreKeyStorePath = "keystores/airavata.sym.p12";
                logger.debug("Test profile detected, using default keystore path: {}", credentialStoreKeyStorePath);
            } else {
                throw new IllegalStateException(
                        "Keystore configuration is missing: security.vault.keystore.url is not set in airavata.properties");
            }
        }
        // Keystore path is relative to configDir (e.g., "keystores/airavata.sym.p12")
        this.keyStorePath = new java.io.File(configDir, credentialStoreKeyStorePath).getAbsolutePath();
        String aliasFromEnv = environment.getProperty("security.vault.keystore.alias");
        this.secretKeyAlias = aliasFromEnv != null ? aliasFromEnv : "airavata";

        // Verify keystore password is set (required for encryption/decryption)
        boolean isTestProfile = environment != null
                && java.util.Arrays.asList(environment.getActiveProfiles()).contains("test");
        String keystorePassword = environment.getProperty("security.vault.keystore.password");
        if (keystorePassword == null || keystorePassword.isEmpty()) {
            if (isTestProfile) {
                // In test profile, use default password if not set
                keystorePassword = "airavata";
                logger.debug("Test profile detected, using default keystore password");
            } else {
                throw new IllegalStateException(
                        "Keystore password is missing: security.vault.keystore.password is not set in airavata.properties");
            }
        }

        // Verify keystore file exists
        java.io.File keystoreFile = new java.io.File(this.keyStorePath);
        if (!keystoreFile.exists()) {
            logger.warn(
                    "Keystore file does not exist at: {}. Credential encryption/decryption may fail.",
                    this.keyStorePath);
        }
    }

    /**
     * Add or update credentials.
     */
    public void saveCredential(String gatewayId, Credential credential) throws CredentialStoreException {
        try {
            CredentialEntity entity = new CredentialEntity();
            entity.setGatewayId(gatewayId);
            entity.setTokenId(credential.getToken());
            entity.setCredential(convertObjectToByteArray(credential));
            entity.setPortalUserId(credential.getPortalUserName());
            entity.setTimePersisted(AiravataUtils.getUniqueTimestamp());
            entity.setDescription(credential.getDescription());
            // Set default owner type if not specified
            if (entity.getCredentialOwnerType() == null) {
                entity.setCredentialOwnerType("GATEWAY");
            }
            credentialRepository.save(entity);
        } catch (Exception e) {
            var msg = String.format(
                    "Error saving credential for gateway: %s, token: %s", gatewayId, credential.getToken());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Delete credentials.
     */
    public void deleteCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        try {
            var pkExists = credentialRepository
                    .findByGatewayIdAndTokenId(gatewayId, tokenId)
                    .isPresent();
            if (!pkExists) {
                // Credential doesn't exist - this is fine, make delete idempotent
                logger.debug("Credential not found for gateway: {}, token: {}, skipping delete", gatewayId, tokenId);
                return;
            }
            credentialRepository.deleteByGatewayIdAndTokenId(gatewayId, tokenId);
            credentialRepository.flush();
        } catch (Exception e) {
            var msg = String.format("Error deleting credential for gateway: %s, token: %s", gatewayId, tokenId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Get credential by gateway ID and token ID.
     */
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        var entityOpt = credentialRepository.findByGatewayIdAndTokenId(gatewayId, tokenId);
        if (entityOpt.isEmpty()) {
            var msg = String.format("Credential not found for gateway: %s, token: %s", gatewayId, tokenId);
            logger.error(msg);
            throw new CredentialStoreException(msg);
        }
        var entity = entityOpt.get();
        try {
            var credential = (Credential) convertByteArrayToObject(entity.getCredential());
            credential.setToken(entity.getTokenId());
            credential.setPortalUserName(entity.getPortalUserId());
            credential.setCertificateRequestedTime(entity.getTimePersisted());
            credential.setDescription(entity.getDescription());
            return credential;
        } catch (Exception e) {
            var msg = String.format("Error retrieving credential for gateway: %s, token: %s", gatewayId, tokenId);
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        }
    }

    /**
     * Get gateway ID by token ID.
     */
    public String getGatewayId(String tokenId) throws CredentialStoreException {
        Optional<String> gatewayIdOpt = credentialRepository.findGatewayIdByTokenId(tokenId);
        return gatewayIdOpt.orElse(null);
    }

    /**
     * Get all credentials for a gateway.
     */
    public List<Credential> getCredentials(String gatewayId) throws CredentialStoreException {
        return getCredentialsInternal(gatewayId, null);
    }

    /**
     * Get credentials for a gateway with specific token IDs.
     */
    public List<Credential> getCredentials(String gatewayId, List<String> accessibleTokenIds)
            throws CredentialStoreException {
        if (accessibleTokenIds == null || accessibleTokenIds.isEmpty()) {
            return Collections.emptyList();
        }
        return getCredentialsInternal(gatewayId, accessibleTokenIds);
    }

    private List<Credential> getCredentialsInternal(String gatewayId, List<String> accessibleTokenIds)
            throws CredentialStoreException {
        List<CredentialEntity> entities;
        if (accessibleTokenIds != null && !accessibleTokenIds.isEmpty()) {
            entities = credentialRepository.findByGatewayIdAndTokenIdIn(gatewayId, accessibleTokenIds);
        } else {
            entities = credentialRepository.findByGatewayId(gatewayId);
        }

        List<Credential> credentials = new ArrayList<>();
        for (CredentialEntity entity : entities) {
            try {
                Credential credential = (Credential) convertByteArrayToObject(entity.getCredential());
                credential.setToken(entity.getTokenId());
                credential.setPortalUserName(entity.getPortalUserId());
                credential.setCertificateRequestedTime(entity.getTimePersisted());
                credential.setDescription(entity.getDescription());
                credentials.add(credential);
            } catch (Exception e) {
                var msg = String.format(
                        "Error converting entity to credential for gateway: %s, token: %s",
                        gatewayId, entity.getTokenId());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
        }
        return credentials;
    }

    /**
     * Get all credentials.
     */
    public List<Credential> getAllCredentials() throws CredentialStoreException {
        List<CredentialEntity> entities = credentialRepository.findAll();
        List<Credential> credentials = new ArrayList<>();
        for (CredentialEntity entity : entities) {
            try {
                Credential credential = (Credential) convertByteArrayToObject(entity.getCredential());
                credential.setToken(entity.getTokenId());
                credential.setPortalUserName(entity.getPortalUserId());
                credential.setCertificateRequestedTime(entity.getTimePersisted());
                credential.setDescription(entity.getDescription());
                credentials.add(credential);
            } catch (Exception e) {
                var msg = String.format(
                        "Error converting entity to credential for gateway: %s, token: %s",
                        entity.getGatewayId(), entity.getTokenId());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
        }
        return credentials;
    }

    /**
     * Convert byte array to object (with decryption if enabled).
     * FIXME verify if this works after setting up spring
     */
    private Object convertByteArrayToObject(byte[] data) throws CredentialStoreException {
        ObjectInputStream objectInputStream = null;
        try {
            // Decrypt the data first if encryption is enabled
            if (shouldEncrypt()) {
                data = SecurityUtil.decrypt(keyStorePath, secretKeyAlias, keyStorePasswordCallback, data);
            }

            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            return objectInputStream.readObject();
        } catch (IOException e) {
            var msg = String.format("Error de-serializing object: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        } catch (ClassNotFoundException e) {
            var msg = String.format("Error de-serializing object: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        } catch (GeneralSecurityException e) {
            var msg = String.format("Error decrypting data: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing stream", e);
                }
            }
        }
    }

    /**
     * Convert object to byte array (with encryption if enabled).
     */
    private byte[] convertObjectToByteArray(Serializable o) throws CredentialStoreException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } catch (IOException e) {
            var msg = String.format("Error serializing object: %s", e.getMessage());
            logger.error(msg, e);
            throw new CredentialStoreException(msg, e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    logger.error("Error closing object output stream", e);
                }
            }
        }

        // Encrypt the byte array if encryption is enabled
        if (shouldEncrypt()) {
            byte[] array = byteArrayOutputStream.toByteArray();
            try {
                return SecurityUtil.encrypt(keyStorePath, secretKeyAlias, keyStorePasswordCallback, array);
            } catch (GeneralSecurityException e) {
                var msg = String.format("Error encrypting data: %s", e.getMessage());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            } catch (IOException e) {
                var msg = String.format("Error encrypting data: %s", e.getMessage());
                logger.error(msg, e);
                throw new CredentialStoreException(msg, e);
            }
        } else {
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Check if encryption should be enabled.
     */
    private boolean shouldEncrypt() {
        return keyStorePath != null;
    }
}
