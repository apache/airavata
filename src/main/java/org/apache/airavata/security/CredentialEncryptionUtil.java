package org.apache.airavata.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.airavata.common.ApplicationSettings;
import org.apache.airavata.models.credential.store.StoredCredential;
import org.springframework.stereotype.Component;

/**
 * Utility for encrypting/decrypting credential blobs (Java-serialized
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
            try (var in = new ObjectInputStream(new ByteArrayInputStream(data))) {
                return (StoredCredential) in.readObject();
            }
        } catch (Exception e) {
            throw new Exception("Failed to convert byte array to credential.", e);
        }
    }

    public byte[] convertCredentialToByteArray(StoredCredential credential) throws Exception {
        byte[] data;
        try (var byteOut = new ByteArrayOutputStream();
                var out = new ObjectOutputStream(byteOut)) {
            out.writeObject(credential);
            out.flush();
            data = byteOut.toByteArray();
        }

        if (encrypt()) {
            try {
                var key = SecurityUtil.getSymmetricKey(
                        this.keyStorePath, this.secretKeyAlias);
                return SecurityUtil.encrypt(data, key);
            } catch (Exception e) {
                throw new Exception("Failed to convert credential to byte array for credential type "
                        + credential.getClass().getSimpleName(), e);
            }
        } else {
            return data;
        }
    }

    /** Extract the token from a StoredCredential. */
    public static String getToken(StoredCredential credential) {
        return switch (credential) {
            case StoredCredential.Ssh ssh -> ssh.sshCredential().token();
            case StoredCredential.Password password -> password.passwordCredential().token();
            case StoredCredential.Certificate certificate -> certificate.certificateCredential().token();
        };
    }

    /** Extract the portal user name from a StoredCredential. */
    public static String getPortalUserName(StoredCredential credential) {
        return switch (credential) {
            case StoredCredential.Ssh ssh -> ssh.sshCredential().username();
            case StoredCredential.Password password -> password.passwordCredential().portalUserName();
            case StoredCredential.Certificate certificate ->
                certificate.certificateCredential().communityUser().username();
        };
    }

    /** Extract the description from a StoredCredential. */
    public static String getDescription(StoredCredential credential) {
        return switch (credential) {
            case StoredCredential.Ssh ssh -> ssh.sshCredential().description();
            case StoredCredential.Password password -> password.passwordCredential().description();
            case StoredCredential.Certificate ignored -> "";
        };
    }

    /** Extract the gateway id from a StoredCredential. */
    public static String getGatewayId(StoredCredential credential) {
        return switch (credential) {
            case StoredCredential.Ssh ssh -> ssh.sshCredential().gatewayId();
            case StoredCredential.Password password -> password.passwordCredential().gatewayId();
            case StoredCredential.Certificate certificate ->
                certificate.certificateCredential().communityUser().gatewayName();
        };
    }

    /**
     * Overlay DB-managed fields (portalUserName, persistedTime, description, token)
     * onto the stored credential.
     */
    public static StoredCredential overlayDbFields(
            StoredCredential stored, String portalUserId, long persistedTime, String description, String token) {
        return switch (stored) {
            case StoredCredential.Ssh ssh -> {
                var builder = ssh.sshCredential().toBuilder()
                        .setUsername(portalUserId)
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                if (description != null) {
                    builder.setDescription(description);
                }
                yield new StoredCredential.Ssh(builder.build());
            }
            case StoredCredential.Password password -> {
                var builder = password.passwordCredential().toBuilder()
                        .setPortalUserName(portalUserId)
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                if (description != null) {
                    builder.setDescription(description);
                }
                yield new StoredCredential.Password(builder.build());
            }
            case StoredCredential.Certificate certificate -> {
                var builder = certificate.certificateCredential().toBuilder()
                        .setPersistedTime(persistedTime)
                        .setToken(token);
                yield new StoredCredential.Certificate(builder.build());
            }
        };
    }
}
