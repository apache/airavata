package org.apache.airavata.security;

import java.nio.ByteBuffer;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import net.schmizz.sshj.common.Buffer;
import net.schmizz.sshj.common.KeyType;

import org.apache.airavata.common.ApplicationSettings;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.util.Arrays;
import javax.crypto.Cipher;

/**
 * Contains some utility methods.
 */
public class SecurityUtil {

    protected static Logger log = LoggerFactory.getLogger(SecurityUtil.class);
    public static final String PASSWORD_HASH_METHOD_PLAINTEXT = "PLAINTEXT";
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String CIPHER_NAME = "AES/GCM/NoPadding";
    public static final int GCM_IV_BYTES = 12; // 96 bits
    public static final int GCM_TAG_BITS = 128;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String convertDateToString(Date date) {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(date);
    }

    public static Date convertStringToDate(String date) throws ParseException {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.parse(date);
    }

    /**
     * Generates an SSH key pair and returns a new SSHCredential proto with the keys
     * set.
     */
    public static SSHCredential generateKeyPair(SSHCredential credential) throws Exception {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            // Encode private key in PEM format (unencrypted — credential store handles
            // encryption at rest)
            StringWriter privateKeyWriter = new StringWriter();
            try (JcaPEMWriter pemWriter = new JcaPEMWriter(privateKeyWriter)) {
                pemWriter.writeObject(new JcaMiscPEMGenerator(kp.getPrivate()));
            }
            String privateKeyPem = privateKeyWriter.toString();

            // Encode public key in OpenSSH format
            String publicKeyOpenSSH = encodePublicKeyOpenSSH((RSAPublicKey) kp.getPublic(), "");

            return credential.toBuilder()
                    .setPrivateKey(privateKeyPem)
                    .setPublicKey(publicKeyOpenSSH)
                    .build();
        } catch (Exception e) {
            log.error("Error while creating key pair", e);
            throw new Exception("Error while creating key pair", e);
        }
    }

    /**
     * Encodes an RSA public key in OpenSSH authorized_keys format.
     */
    private static String encodePublicKeyOpenSSH(RSAPublicKey publicKey, String comment) {
        Buffer.PlainBuffer buf = new Buffer.PlainBuffer();
        buf.putString(KeyType.RSA.toString());
        buf.putMPInt(publicKey.getPublicExponent());
        buf.putMPInt(publicKey.getModulus());
        String encoded = KeyType.RSA.toString() + " " + Base64.getEncoder().encodeToString(buf.getCompactData());
        if (comment != null && !comment.isEmpty()) {
            encoded += " " + comment;
        }
        return encoded + "\n";
    }

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public static Key getSymmetricKey(String keyStorePath, String keyAlias)
            throws Exception {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath);
        return ks.getKey(keyAlias, getSecretKeyPassPhrase(keyAlias));
    }

    public static byte[] encrypt(byte[] data, Key key) throws GeneralSecurityException {
        var cipher = Cipher.getInstance(CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        var iv = cipher.getIV();
        var encryptedData = cipher.doFinal(data);
        return ByteBuffer.allocate(iv.length + encryptedData.length)
                .put(iv)
                .put(encryptedData)
                .array();
    }

    public static byte[] decrypt(byte[] tag, Key key) throws GeneralSecurityException {
        var iv = Arrays.copyOfRange(tag, 0, GCM_IV_BYTES);
        var encryptedData = Arrays.copyOfRange(tag, GCM_IV_BYTES, tag.length);
        var cipher = Cipher.getInstance(CIPHER_NAME);
        var spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(encryptedData);
    }

    /**
     * Decrypt using the legacy AES/CBC/PKCS5Padding scheme with a static zero IV.
     * <p>
     * WARNING: This method is intentionally limited to migration of credentials
     * that were
     * encrypted in the past using AES/CBC with a fixed zero IV. Do NOT use this
     * method
     * for new code or for decrypting data encrypted with any modern scheme. All new
     * encryption and decryption should use the AES/GCM helpers in this class
     * instead.
     * </p>
     * Used only by the migration script to read old credentials, which should then
     * be
     * re-encrypted using AES/GCM.
     */
    @Deprecated
    public static byte[] decryptLegacy(byte[] encrypted, Key key) throws GeneralSecurityException {
        var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
        return cipher.doFinal(encrypted);
    }

    public static KeyStore loadKeyStore(String keyStoreFilePath) throws Exception {

        File keystoreFile = new File(keyStoreFilePath);
        if (keystoreFile.exists() && keystoreFile.isFile()) {
            logger.debug("Found keystore: {}", keyStoreFilePath);
        } else {
            throw new FileNotFoundException("Keystore file not found: " + keyStoreFilePath);
        }
        return KeyStore.getInstance(keystoreFile, getStorePassword());
    }


    private static char[] getStorePassword() throws Exception {
        return ApplicationSettings.getCredentialStoreKeyStorePassword().toCharArray();
    }

    private static char[] getSecretKeyPassPhrase(String keyAlias) throws Exception {
        return ApplicationSettings.getCredentialStoreKeyStorePassword().toCharArray();
    }
}
