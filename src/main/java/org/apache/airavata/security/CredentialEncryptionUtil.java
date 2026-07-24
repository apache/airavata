package org.apache.airavata.security;

import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.airavata.common.ApplicationSettings;
import org.apache.airavata.model.credential.store.proto.StoredCredential;
import org.springframework.stereotype.Component;

/**
 * Utility for encrypting/decrypting credential blobs (protobuf-serialized
 * StoredCredential).
 */
@Component
public class CredentialEncryptionUtil {

    private final String keyStorePath;
    private final String secretKeyAlias;

    public CredentialEncryptionUtil() throws Exception {
        this.keyStorePath = ApplicationSettings.getCredentialStoreKeyStorePath();
        this.secretKeyAlias = ApplicationSettings.getCredentialStoreKeyAlias();
    }

    /**
     * Says whether to encrypt data or not. If keystore path is set we treat
     * encryption as true.
     */
    private boolean encrypt() {
        return this.keyStorePath != null;
    }

    public StoredCredential convertByteArrayToCredential(byte[] data) throws Exception {
        try {
            if (encrypt()) {
                var key = SecurityUtil.getSymmetricKey(
                        this.keyStorePath, this.secretKeyAlias);
                data = SecurityUtil.decrypt(data, key);
            }
            return StoredCredential.parseFrom(data);
        } catch (Exception e) {
            throw new Exception("Failed to convert byte array to credential.", e);
        }
    }

    public byte[] convertCredentialToByteArray(StoredCredential credential) throws Exception {
        byte[] data = credential.toByteArray();

        if (encrypt()) {
            try {
                var key = SecurityUtil.getSymmetricKey(
                        this.keyStorePath, this.secretKeyAlias);
                return SecurityUtil.encrypt(data, key);
            } catch (Exception e) {
                throw new Exception("Failed to convert credential to byte array for credential type "
                        + credential.getCredentialCase(), e);
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

    /**
     * Overlay DB-managed fields (portalUserName, persistedTime, description, token)
     * onto the stored credential.
     */
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
