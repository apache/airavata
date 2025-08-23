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
package org.apache.airavata.credential.store.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ApplicationSettings;
import org.apache.airavata.common.utils.DefaultKeyStorePasswordCallback;
import org.apache.airavata.credential.store.credential.Credential;
import org.apache.airavata.credential.store.store.CredentialStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for credential serialization and encryption/decryption operations.
 */
public class CredentialSerializationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(CredentialSerializationUtils.class);
    
    private static final String KEYSTORE_PATH;
    private static final String KEY_ALIAS;
    private static final DefaultKeyStorePasswordCallback KEYSTORE_PASSWORD_CALLBACK = new DefaultKeyStorePasswordCallback();
    
    static {
        try {
            KEYSTORE_PATH = ApplicationSettings.getCredentialStoreKeyStorePath();
            KEY_ALIAS = ApplicationSettings.getCredentialStoreKeyAlias();
        } catch (ApplicationSettingsException e) {
            throw new RuntimeException("Failed to initialize credential serialization utils", e);
        }
    }
    
    /**
     * Serialize a credential object to byte array without encryption.
     * @param credential the credential to serialize
     * @return serialized byte array
     * @throws CredentialStoreException if serialization fails
     */
    public static byte[] serializeCredential(Credential credential) throws CredentialStoreException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(credential);
            oos.flush();
            return baos.toByteArray();
            
        } catch (IOException e) {
            logger.error("Error serializing credential", e);
            throw new CredentialStoreException("Error serializing credential", e);
        }
    }
    
    /**
     * Deserialize a credential object from byte array without decryption.
     * @param serializedCredential the serialized credential byte array
     * @return deserialized credential object
     * @throws CredentialStoreException if deserialization fails
     */
    public static Credential deserializeCredential(byte[] serializedCredential) throws CredentialStoreException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedCredential);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            
            return (Credential) ois.readObject();
            
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error deserializing credential", e);
            throw new CredentialStoreException("Error deserializing credential", e);
        }
    }
    
    /**
     * Serialize and encrypt a credential object.
     * @param credential the credential to serialize and encrypt
     * @return encrypted byte array
     * @throws CredentialStoreException if serialization or encryption fails
     */
    public static byte[] serializeCredentialWithEncryption(Credential credential) throws CredentialStoreException {
        try {
            // First serialize the credential
            byte[] serializedCredential = serializeCredential(credential);

            // Get the secret key from keystore
            SecretKey secretKey = getSecretKeyFromKeyStore();

            // Use AES/CBC/PKCS5Padding to ensure IV is used and generated
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(serializedCredential);

            // Get the IV (should not be null)
            byte[] iv = cipher.getIV();
            if (iv == null) {
                throw new CredentialStoreException("IV is null after cipher initialization");
            }

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return combined;

        } catch (Exception e) {
            logger.error("Error encrypting credential", e);
            throw new CredentialStoreException("Error encrypting credential", e);
        }
    }
    
    /**
     * Decrypt and deserialize a credential object.
     * @param encryptedCredential the encrypted credential byte array
     * @return deserialized credential object
     * @throws CredentialStoreException if decryption or deserialization fails
     */
    public static Credential deserializeCredentialWithDecryption(byte[] encryptedCredential) throws CredentialStoreException {
        try {
            // Get the secret key from keystore
            SecretKey secretKey = getSecretKeyFromKeyStore();
            
            // Extract IV and encrypted data
            byte[] iv = new byte[16]; // AES IV size
            byte[] encryptedData = new byte[encryptedCredential.length - 16];
            System.arraycopy(encryptedCredential, 0, iv, 0, 16);
            System.arraycopy(encryptedCredential, 16, encryptedData, 0, encryptedData.length);
            
            // Decrypt the data
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            // Deserialize the decrypted data
            return deserializeCredential(decryptedData);
            
        } catch (Exception e) {
            logger.error("Error decrypting credential", e);
            throw new CredentialStoreException("Error decrypting credential", e);
        }
    }
    
    /**
     * Serialize and encrypt a credential object with custom keystore settings.
     * @param credential the credential to serialize and encrypt
     * @param keystorePath the path to the keystore
     * @param keyAlias the alias of the key to use
     * @param passwordCallback the password callback for keystore access
     * @return encrypted byte array
     * @throws CredentialStoreException if serialization or encryption fails
     */
    public static byte[] serializeCredentialWithEncryption(Credential credential, String keystorePath, 
                                                          String keyAlias, KeyStorePasswordCallback passwordCallback) 
            throws CredentialStoreException {
        try {
            // First serialize the credential
            byte[] serializedCredential = serializeCredential(credential);
            
            // Get the secret key from custom keystore
            SecretKey secretKey = getSecretKeyFromCustomKeyStore(keystorePath, keyAlias, passwordCallback);
            
            // Encrypt the serialized data
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(serializedCredential);
            
            // Combine IV and encrypted data
            byte[] iv = cipher.getIV();
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return combined;
            
        } catch (Exception e) {
            logger.error("Error encrypting credential with custom keystore", e);
            throw new CredentialStoreException("Error encrypting credential with custom keystore", e);
        }
    }
    
    /**
     * Decrypt and deserialize a credential object with custom keystore settings.
     * @param encryptedCredential the encrypted credential byte array
     * @param keystorePath the path to the keystore
     * @param keyAlias the alias of the key to use
     * @param passwordCallback the password callback for keystore access
     * @return deserialized credential object
     * @throws CredentialStoreException if decryption or deserialization fails
     */
    public static Credential deserializeCredentialWithDecryption(byte[] encryptedCredential, String keystorePath, 
                                                                String keyAlias, KeyStorePasswordCallback passwordCallback) 
            throws CredentialStoreException {
        try {
            // Get the secret key from custom keystore
            SecretKey secretKey = getSecretKeyFromCustomKeyStore(keystorePath, keyAlias, passwordCallback);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[16]; // AES IV size
            byte[] encryptedData = new byte[encryptedCredential.length - 16];
            System.arraycopy(encryptedCredential, 0, iv, 0, 16);
            System.arraycopy(encryptedCredential, 16, encryptedData, 0, encryptedData.length);
            
            // Decrypt the data
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.IvParameterSpec(iv));
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            // Deserialize the decrypted data
            return deserializeCredential(decryptedData);
            
        } catch (Exception e) {
            logger.error("Error decrypting credential with custom keystore", e);
            throw new CredentialStoreException("Error decrypting credential with custom keystore", e);
        }
    }
    
    /**
     * Get the secret key from the default keystore.
     * @return the secret key
     * @throws Exception if key retrieval fails
     */
    private static SecretKey getSecretKeyFromKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD_CALLBACK.getStorePassword());
        }
        
        return (SecretKey) keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD_CALLBACK.getSecretKeyPassPhrase(KEY_ALIAS));
    }
    
    /**
     * Get the secret key from a custom keystore.
     * @param keystorePath the path to the keystore
     * @param keyAlias the alias of the key to use
     * @param passwordCallback the password callback for keystore access
     * @return the secret key
     * @throws Exception if key retrieval fails
     */
    private static SecretKey getSecretKeyFromCustomKeyStore(String keystorePath, String keyAlias, 
                                                           KeyStorePasswordCallback passwordCallback) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, passwordCallback.getStorePassword());
        }
        
        return (SecretKey) keyStore.getKey(keyAlias, passwordCallback.getSecretKeyPassPhrase(keyAlias));
    }
    
    /**
     * Generate a new secret key for encryption.
     * @return the generated secret key
     * @throws Exception if key generation fails
     */
    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }
    
    /**
     * Convert a secret key to a string representation.
     * @param secretKey the secret key to convert
     * @return the string representation
     */
    public static String secretKeyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    
    /**
     * Convert a string representation back to a secret key.
     * @param keyString the string representation
     * @return the secret key
     */
    public static SecretKey stringToSecretKey(String keyString) {
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, "AES");
    }
    
    /**
     * Interface for keystore password callbacks.
     */
    public interface KeyStorePasswordCallback {
        char[] getStorePassword();
        char[] getSecretKeyPassPhrase(String keyAlias);
    }
}
