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
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which includes security utilities.
 */
public class SecurityUtil {

    public static final String PASSWORD_HASH_METHOD_PLAINTEXT = "PLAINTEXT";
    public static final String CHARSET_ENCODING = "UTF-8";
    public static final String CIPHER_NAME = "AES/GCM/NoPadding";
    public static final int GCM_IV_BYTES = 12; // 96 bits
    public static final int GCM_TAG_BITS = 128;

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public static Key getSymmetricKey(String keyStorePath, String keyAlias, KeyStorePasswordCallback passwordCallback)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
                    UnrecoverableKeyException {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, passwordCallback);
        return ks.getKey(keyAlias, passwordCallback.getSecretKeyPassPhrase(keyAlias));
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

    public static byte[] encrypt(byte[] data, Key key) throws GeneralSecurityException {
        // Initialize the cipher
        var cipher = Cipher.getInstance(SecurityUtil.CIPHER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        var iv = cipher.getIV();
        // Encrypt the data
        var encryptedData = cipher.doFinal(data);
        // Construct tag [iv,encryptedData]
        var tag = ByteBuffer.allocate(iv.length + encryptedData.length)
                .put(iv)
                .put(encryptedData)
                .array();
        return tag;
    }

    public static byte[] decrypt(byte[] tag, Key key) throws GeneralSecurityException {
        // Deconstruct tag [iv,encryptedData]
        var iv = Arrays.copyOfRange(tag, 0, GCM_IV_BYTES);
        var encryptedData = Arrays.copyOfRange(tag, GCM_IV_BYTES, tag.length);
        // Initialize the cipher
        var cipher = Cipher.getInstance(SecurityUtil.CIPHER_NAME);
        var spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        // Decrypt the data
        var data = cipher.doFinal(encryptedData);
        return data;
    }
}
