/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Class which includes security utilities.
 */
public class SecurityUtil {

    public static final String PASSWORD_HASH_METHOD_PLAINTEXT = "PLAINTEXT";

    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String ENCRYPTION_ALGORITHM = "AES";
    public static final String PADDING_MECHANISM = "AES/CBC/PKCS5Padding";

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    /**
     * Creates a hash of given string with the given hash algorithm.
     * 
     * @param stringToDigest
     *            The string to digest.
     * @param digestingAlgorithm
     *            Hash algorithm.
     * @return The digested string.
     * @throws NoSuchAlgorithmException
     *             If given hash algorithm doesnt exists.
     */
    public static String digestString(String stringToDigest, String digestingAlgorithm) throws NoSuchAlgorithmException {

        if (digestingAlgorithm == null || digestingAlgorithm.equals(PASSWORD_HASH_METHOD_PLAINTEXT)) {
            return stringToDigest;
        }

        MessageDigest messageDigest = MessageDigest.getInstance(digestingAlgorithm);
        try {
            return new String(messageDigest.digest(stringToDigest.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding password string when creating digest", e);
            throw new RuntimeException("Error encoding password string when creating digest", e);
        }
    }

    /**
     * Sets the truststore for application. Useful when communicating over HTTPS.
     * 
     * @param trustStoreFilePath
     *            Where trust store is located.
     * @param trustStorePassword
     *            The trust store password.
     */
    public static void setTrustStoreParameters(String trustStoreFilePath, String trustStorePassword) {

        if (System.getProperty("javax.net.ssl.trustStrore") == null) {
            logger.info("Setting Java trust store to " + trustStoreFilePath);
            System.setProperty("javax.net.ssl.trustStrore", trustStoreFilePath);
        }

        if (System.getProperty("javax.net.ssl.trustStorePassword") == null) {
            System.setProperty("javax.net.ssl.trustStorePassword", trustStoreFilePath);
        }

    }

    public static byte[] encryptString(String keyStorePath, String keyAlias,
                                 KeyStorePasswordCallback passwordCallback, String value)
            throws GeneralSecurityException, IOException {
        return encrypt(keyStorePath, keyAlias, passwordCallback, value.getBytes(CHARSET_ENCODING));
    }

    public static byte[] encrypt(String keyStorePath, String keyAlias,
                                 KeyStorePasswordCallback passwordCallback, byte[] value)
            throws GeneralSecurityException, IOException {

        Key secretKey = getSymmetricKey(keyStorePath, keyAlias, passwordCallback);

        Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                new IvParameterSpec(new byte[16]));
        return cipher.doFinal(value);
    }

    private static Key getSymmetricKey(String keyStorePath, String keyAlias,
                                       KeyStorePasswordCallback passwordCallback)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableKeyException {

        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, "jceks", passwordCallback);

        if (ks == null) {
            throw new IOException("Unable to load Java keystore " + keyStorePath);
        }

        return ks.getKey(keyAlias, passwordCallback.getSecretKeyPassPhrase(keyAlias));

    }

    public static byte[] decrypt(String keyStorePath, String keyAlias,
                                 KeyStorePasswordCallback passwordCallback, byte[] encrypted)
            throws GeneralSecurityException, IOException {

        Key secretKey = getSymmetricKey(keyStorePath, keyAlias, passwordCallback);

        Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey,
                new IvParameterSpec(new byte[16]));

        return cipher.doFinal(encrypted);
    }

    public static String decryptString(String keyStorePath, String keyAlias,
                                       KeyStorePasswordCallback passwordCallback, byte[] encrypted)
            throws GeneralSecurityException, IOException {

        byte[] decrypted = decrypt(keyStorePath, keyAlias, passwordCallback, encrypted);
        return new String(decrypted, CHARSET_ENCODING);
    }

    public static KeyStore loadKeyStore(String keyStoreFilePath, String keyStoreType,
                                        KeyStorePasswordCallback passwordCallback)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        File keystoreFile = new File(keyStoreFilePath);

        InputStream is;
        if (keystoreFile.exists()) {
            logger.debug("Loading keystore file from path " + keyStoreFilePath);
            is = new FileInputStream(keyStoreFilePath);
        } else {
            logger.debug("Trying to load keystore file form class path " + keyStoreFilePath);
            is = SecurityUtil.class.getClassLoader().getResourceAsStream(keyStoreFilePath);
            if (is != null) {
                logger.debug("Trust store file was loaded form class path " + keyStoreFilePath);
            }
        }

        if (is == null) {
            throw new KeyStoreException("Could not find a keystore file in path " + keyStoreFilePath);
        }

        try {
            return loadKeyStore(is, keyStoreType, passwordCallback);
        } finally {
            is.close();
        }
    }

    public static KeyStore loadKeyStore(InputStream inputStream, String keyStoreType,
                                        KeyStorePasswordCallback passwordCallback)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        if (keyStoreType == null) {
            keyStoreType = KeyStore.getDefaultType();
        }

        KeyStore ks = KeyStore.getInstance(keyStoreType);
        ks.load(inputStream, passwordCallback.getStorePassword());

        return ks;
    }





}
