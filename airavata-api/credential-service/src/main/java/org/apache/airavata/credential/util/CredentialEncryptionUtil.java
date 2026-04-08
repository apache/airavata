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
package org.apache.airavata.credential.util;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.apache.airavata.config.ApplicationSettings;
import org.apache.airavata.credential.repository.CredentialStoreException;
import org.apache.airavata.exception.ApplicationSettingsException;
import org.apache.airavata.model.credential.store.proto.StoredCredential;
import org.apache.airavata.server.DefaultKeyStorePasswordCallback;
import org.apache.airavata.server.KeyStorePasswordCallback;
import org.apache.airavata.util.SecurityUtil;
import org.springframework.stereotype.Component;

/**
 * Utility for encrypting/decrypting credential blobs (protobuf-serialized StoredCredential).
 */
@Component
public class CredentialEncryptionUtil {

    private final String keyStorePath;
    private final String secretKeyAlias;
    private final KeyStorePasswordCallback keyStorePasswordCallback;

    public CredentialEncryptionUtil() throws ApplicationSettingsException {
        this.keyStorePath = ApplicationSettings.getCredentialStoreKeyStorePath();
        this.secretKeyAlias = ApplicationSettings.getCredentialStoreKeyAlias();
        this.keyStorePasswordCallback = new DefaultKeyStorePasswordCallback();
    }

    /** Says whether to encrypt data or not. If keystore path is set we treat encryption as true. */
    private boolean encrypt() {
        return this.keyStorePath != null;
    }

    public StoredCredential convertByteArrayToCredential(byte[] data) throws CredentialStoreException {
        try {
            if (encrypt()) {
                var key = SecurityUtil.getSymmetricKey(
                        this.keyStorePath, this.secretKeyAlias, this.keyStorePasswordCallback);
                data = SecurityUtil.decrypt(data, key);
            }
            return StoredCredential.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new CredentialStoreException("Error de-serializing credential.", e);
        } catch (GeneralSecurityException e) {
            throw new CredentialStoreException("Error decrypting data.", e);
        } catch (IOException e) {
            throw new CredentialStoreException("Error decrypting data. IO exception.", e);
        }
    }

    public byte[] convertCredentialToByteArray(StoredCredential credential) throws CredentialStoreException {
        byte[] data = credential.toByteArray();

        if (encrypt()) {
            try {
                var key = SecurityUtil.getSymmetricKey(
                        this.keyStorePath, this.secretKeyAlias, this.keyStorePasswordCallback);
                return SecurityUtil.encrypt(data, key);
            } catch (GeneralSecurityException e) {
                throw new CredentialStoreException("Error encrypting data", e);
            } catch (IOException e) {
                throw new CredentialStoreException("Error encrypting data. IO exception.", e);
            }
        } else {
            return data;
        }
    }

    /** Extract the token from a StoredCredential. */
    public static String getToken(StoredCredential credential) {
        switch (credential.getCredentialCase()) {
            case SSH_CREDENTIAL:
                return credential.getSshCredential().getToken();
            case PASSWORD_CREDENTIAL:
                return credential.getPasswordCredential().getToken();
            case CERTIFICATE_CREDENTIAL:
                return credential.getCertificateCredential().getToken();
            default:
                return "";
        }
    }

    /** Extract the portal user name from a StoredCredential. */
    public static String getPortalUserName(StoredCredential credential) {
        switch (credential.getCredentialCase()) {
            case SSH_CREDENTIAL:
                return credential.getSshCredential().getUsername();
            case PASSWORD_CREDENTIAL:
                return credential.getPasswordCredential().getPortalUserName();
            case CERTIFICATE_CREDENTIAL:
                return credential.getCertificateCredential().getCommunityUser().getUsername();
            default:
                return "";
        }
    }

    /** Extract the description from a StoredCredential. */
    public static String getDescription(StoredCredential credential) {
        switch (credential.getCredentialCase()) {
            case SSH_CREDENTIAL:
                return credential.getSshCredential().getDescription();
            case PASSWORD_CREDENTIAL:
                return credential.getPasswordCredential().getDescription();
            default:
                return "";
        }
    }

    /** Extract the gateway id from a StoredCredential. */
    public static String getGatewayId(StoredCredential credential) {
        switch (credential.getCredentialCase()) {
            case SSH_CREDENTIAL:
                return credential.getSshCredential().getGatewayId();
            case PASSWORD_CREDENTIAL:
                return credential.getPasswordCredential().getGatewayId();
            case CERTIFICATE_CREDENTIAL:
                return credential.getCertificateCredential().getCommunityUser().getGatewayName();
            default:
                return "";
        }
    }

    /** Overlay DB-managed fields (portalUserName, persistedTime, description, token) onto the stored credential. */
    public static StoredCredential overlayDbFields(
            StoredCredential stored, String portalUserId, long persistedTime, String description, String token) {
        switch (stored.getCredentialCase()) {
            case SSH_CREDENTIAL: {
                var builder = stored.getSshCredential().toBuilder()
                        .setUsername(portalUserId)
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                if (description != null) {
                    builder.setDescription(description);
                }
                return StoredCredential.newBuilder().setSshCredential(builder).build();
            }
            case PASSWORD_CREDENTIAL: {
                var builder = stored.getPasswordCredential().toBuilder()
                        .setPortalUserName(portalUserId)
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                if (description != null) {
                    builder.setDescription(description);
                }
                return StoredCredential.newBuilder()
                        .setPasswordCredential(builder)
                        .build();
            }
            case CERTIFICATE_CREDENTIAL: {
                var builder = stored.getCertificateCredential().toBuilder()
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                return StoredCredential.newBuilder()
                        .setCertificateCredential(builder)
                        .build();
            }
            default:
                return stored;
        }
    }
}
