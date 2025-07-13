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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import org.junit.jupiter.api.Test;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/11/13
 * Time: 10:42 AM
 */
public class SecurityUtilTest {

    private final String keyStorePath = "airavata.p12";

    @Test
    public void testEncryptString() throws Exception {

        String stringToEncrypt = "Test string to encrypt";
        byte[] encrypted =
                SecurityUtil.encryptString(keyStorePath, "mykey", new TestKeyStoreCallback(), stringToEncrypt);

        String decrypted = SecurityUtil.decryptString(keyStorePath, "mykey", new TestKeyStoreCallback(), encrypted);
        assertEquals(stringToEncrypt, decrypted);
    }

    @Test
    public void testEncryptBytes() throws Exception {
        String stringToEncrypt = "Test string to encrypt";
        byte[] plaintext = stringToEncrypt.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = SecurityUtil.encrypt(keyStorePath, "mykey", new TestKeyStoreCallback(), plaintext);
        byte[] decrypted = SecurityUtil.decrypt(keyStorePath, "mykey", new TestKeyStoreCallback(), encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testLoadKeyStoreFromFile() throws Exception {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, new TestKeyStoreCallback());
        assertNotNull(ks);
    }

    private static class TestKeyStoreCallback implements KeyStorePasswordCallback {

        @Override
        public char[] getStorePassword() {
            return "airavata".toCharArray();
        }

        @Override
        public char[] getSecretKeyPassPhrase(String keyAlias) {
            if (keyAlias.equals("mykey")) {
                return "airavatasecretkey".toCharArray();
            }
            return null;
        }
    }
}
