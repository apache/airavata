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

import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.credential.Credential;
import org.apache.airavata.credential.CredentialOwnerType;
import org.apache.airavata.credential.entities.CredentialEntity;
import org.apache.airavata.credential.exceptions.CredentialStoreException;
import org.apache.airavata.credential.repositories.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private String keyStorePath;
    private String secretKeyAlias;

    public CredentialEntityService(
            CredentialRepository credentialRepository,
            AiravataServerProperties properties,
            DefaultKeyStorePasswordCallback keyStorePasswordCallback) {
        this.credentialRepository = credentialRepository;
        this.properties = properties;
        this.keyStorePasswordCallback = keyStorePasswordCallback;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            String airavataConfigDir = properties.airavataConfigDir;
            String credentialStoreKeyStorePath = properties.services.vault.keystore.url;
            if (airavataConfigDir == null || credentialStoreKeyStorePath == null) {
                logger.warn("Keystore configuration is missing (airavataConfigDir or keystore.url is null), encryption will be disabled");
                this.keyStorePath = null;
                return;
            }
            this.keyStorePath = new java.io.File(airavataConfigDir, credentialStoreKeyStorePath).getAbsolutePath();
            this.secretKeyAlias = properties.services.vault.keystore.alias;
        } catch (Exception e) {
            logger.warn("Failed to initialize keystore settings, encryption will be disabled", e);
            this.keyStorePath = null;
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
            entity.setTimePersisted(new Timestamp(System.currentTimeMillis()));
            entity.setDescription(credential.getDescription());
            if (credential.getCredentialOwnerType() != null) {
                entity.setCredentialOwnerType(
                        credential.getCredentialOwnerType().toString());
            }

            credentialRepository.save(entity);
        } catch (Exception e) {
            logger.error("Error saving credential for gateway: {}, token: {}", gatewayId, credential.getToken(), e);
            throw new CredentialStoreException("Error saving credential", e);
        }
    }

    /**
     * Delete credentials.
     */
    public void deleteCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        try {
            credentialRepository.deleteByGatewayIdAndTokenId(gatewayId, tokenId);
        } catch (Exception e) {
            logger.error("Error deleting credential for gateway: {}, token: {}", gatewayId, tokenId, e);
            throw new CredentialStoreException("Error deleting credential", e);
        }
    }

    /**
     * Get credential by gateway ID and token ID.
     */
    public Credential getCredential(String gatewayId, String tokenId) throws CredentialStoreException {
        Optional<CredentialEntity> entityOpt = credentialRepository.findByGatewayIdAndTokenId(gatewayId, tokenId);
        if (entityOpt.isEmpty()) {
            return null;
        }

        CredentialEntity entity = entityOpt.get();
        try {
            Credential credential = (Credential) convertByteArrayToObject(entity.getCredential());
            credential.setToken(entity.getTokenId());
            credential.setPortalUserName(entity.getPortalUserId());
            credential.setCertificateRequestedTime(entity.getTimePersisted());
            credential.setDescription(entity.getDescription());
            if (entity.getCredentialOwnerType() != null) {
                credential.setCredentialOwnerType(CredentialOwnerType.valueOf(entity.getCredentialOwnerType()));
            }
            return credential;
        } catch (Exception e) {
            logger.error("Error retrieving credential for gateway: {}, token: {}", gatewayId, tokenId, e);
            throw new CredentialStoreException("Error retrieving credential", e);
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
                if (entity.getCredentialOwnerType() != null) {
                    credential.setCredentialOwnerType(CredentialOwnerType.valueOf(entity.getCredentialOwnerType()));
                }
                credentials.add(credential);
            } catch (Exception e) {
                logger.error(
                        "Error converting entity to credential for gateway: {}, token: {}",
                        gatewayId,
                        entity.getTokenId(),
                        e);
                throw new CredentialStoreException("Error converting entity to credential", e);
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
                if (entity.getCredentialOwnerType() != null) {
                    credential.setCredentialOwnerType(CredentialOwnerType.valueOf(entity.getCredentialOwnerType()));
                }
                credentials.add(credential);
            } catch (Exception e) {
                logger.error(
                        "Error converting entity to credential for gateway: {}, token: {}",
                        entity.getGatewayId(),
                        entity.getTokenId(),
                        e);
                throw new CredentialStoreException("Error converting entity to credential", e);
            }
        }
        return credentials;
    }

    /**
     * Convert byte array to object (with decryption if enabled).
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
            throw new CredentialStoreException("Error de-serializing object", e);
        } catch (ClassNotFoundException e) {
            throw new CredentialStoreException("Error de-serializing object", e);
        } catch (GeneralSecurityException e) {
            throw new CredentialStoreException("Error decrypting data", e);
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
            throw new CredentialStoreException("Error serializing object", e);
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
                throw new CredentialStoreException("Error encrypting data", e);
            } catch (IOException e) {
                throw new CredentialStoreException("Error encrypting data", e);
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
