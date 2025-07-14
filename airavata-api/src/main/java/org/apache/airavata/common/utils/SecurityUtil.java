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
package org.apache.airavata.common.utils;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which includes security utilities.
 */
public class SecurityUtil {

    public static final String PASSWORD_HASH_METHOD_PLAINTEXT = "PLAINTEXT";
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String PADDING_MECHANISM = "AES/CBC/PKCS5Padding";

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public static byte[] encryptString(
            String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback, String value)
            throws GeneralSecurityException, IOException {
        return encrypt(keyStorePath, keyAlias, passwordCallback, value.getBytes(CHARSET_ENCODING));
    }

    public static byte[] encrypt(
            String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback, byte[] value)
            throws GeneralSecurityException, IOException {

        Key secretKey = getSymmetricKey(keyStorePath, keyAlias, passwordCallback);

        Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));
        return cipher.doFinal(value);
    }

    private static Key getSymmetricKey(String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
                    UnrecoverableKeyException {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, passwordCallback);
        return ks.getKey(keyAlias, passwordCallback.getSecretKeyPassPhrase(keyAlias));
    }

    public static byte[] decrypt(
            String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback, byte[] encrypted)
            throws GeneralSecurityException, IOException {

        Key secretKey = getSymmetricKey(keyStorePath, keyAlias, passwordCallback);

        Cipher cipher = Cipher.getInstance(PADDING_MECHANISM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16]));

        return cipher.doFinal(encrypted);
    }

    public static String decryptString(
            String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback, byte[] encrypted)
            throws GeneralSecurityException, IOException {

        byte[] decrypted = decrypt(keyStorePath, keyAlias, passwordCallback, encrypted);
        return new String(decrypted, CHARSET_ENCODING);
    }

    public static KeyStore loadKeyStore(String keyStoreFilePath, KeyStorePasswordCallback passwordCallback)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        File keystoreFile = new File(keyStoreFilePath);
        if (keystoreFile.exists() && keystoreFile.isFile()) {
            logger.debug("Found keystore: {}", keyStoreFilePath);
        } else {
            throw new FileNotFoundException("Keystore file not found: " + keyStoreFilePath);
        }
        return KeyStore.getInstance(keystoreFile, passwordCallback.getStorePassword());
    }
}
