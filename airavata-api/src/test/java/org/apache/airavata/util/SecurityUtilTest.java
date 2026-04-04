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
package org.apache.airavata.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import org.apache.airavata.server.KeyStorePasswordCallback;
import org.junit.jupiter.api.Test;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/11/13
 * Time: 10:42 AM
 */
public class SecurityUtilTest {

    private final String keyStorePath = "airavata.sym.p12";

    @Test
    public void testEncryptString() throws Exception {
        String stringToEncrypt = "Test string to encrypt";
        var key = SecurityUtil.getSymmetricKey(keyStorePath, "mykey", new TestKeyStoreCallback());
        byte[] encrypted = SecurityUtil.encrypt(stringToEncrypt.getBytes(StandardCharsets.UTF_8), key);
        String decrypted = new String(SecurityUtil.decrypt(encrypted, key), StandardCharsets.UTF_8);
        assertEquals(stringToEncrypt, decrypted);
    }

    @Test
    public void testEncryptBytes() throws Exception {
        String stringToEncrypt = "Test string to encrypt";
        byte[] plaintext = stringToEncrypt.getBytes(StandardCharsets.UTF_8);
        var key = SecurityUtil.getSymmetricKey(keyStorePath, "mykey", new TestKeyStoreCallback());
        byte[] encrypted = SecurityUtil.encrypt(plaintext, key);
        byte[] decrypted = SecurityUtil.decrypt(encrypted, key);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testLoadKeyStoreFromFile() throws Exception {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, new TestKeyStoreCallback());
        assertNotNull(ks);
    }

    @Test
    public void testLegacyDecryptRoundTrip() throws Exception {
        byte[] plaintext = "legacy data".getBytes(StandardCharsets.UTF_8);
        Key key = SecurityUtil.getSymmetricKey(keyStorePath, "mykey", new TestKeyStoreCallback());
        byte[] legacyEncrypted = legacyEncrypt(plaintext, key);
        byte[] decrypted = SecurityUtil.decryptLegacy(legacyEncrypted, key);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testMigrationLegacyToGcm() throws Exception {
        byte[] plaintext = "migrating credential".getBytes(StandardCharsets.UTF_8);
        Key key = SecurityUtil.getSymmetricKey(keyStorePath, "mykey", new TestKeyStoreCallback());
        // decrypt old format, re-encrypt as GCM, verify
        byte[] legacyEncrypted = legacyEncrypt(plaintext, key);
        byte[] decrypted = SecurityUtil.decryptLegacy(legacyEncrypted, key);
        byte[] gcmEncrypted = SecurityUtil.encrypt(decrypted, key);
        byte[] result = SecurityUtil.decrypt(gcmEncrypted, key);
        assertArrayEquals(plaintext, result);
    }

    /** Simulate old AES/CBC encryption with static zero IV. */
    private static byte[] legacyEncrypt(byte[] data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
        return cipher.doFinal(data);
    }

    private static class TestKeyStoreCallback implements KeyStorePasswordCallback {

        @Override
        public char[] getStorePassword() {
            return "airavata".toCharArray();
        }

        @Override
        public char[] getSecretKeyPassPhrase(String keyAlias) {
            if (keyAlias.equals("mykey")) {
                // PKCS12 requires key password to match store password
                return "airavata".toCharArray();
            }
            return null;
        }
    }
}
