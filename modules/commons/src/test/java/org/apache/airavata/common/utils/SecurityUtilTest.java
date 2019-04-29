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

import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/11/13
 * Time: 10:42 AM
 */

public class SecurityUtilTest {
    @Test
     public void testEncryptString() throws Exception {

        URI uri = this.getClass().getClassLoader().getResource("mykeystore.jks").toURI();

        assert uri != null;

        String stringToEncrypt = "Test string to encrypt";
        byte[] encrypted = SecurityUtil.encryptString(uri.getPath(), "mykey", new TestKeyStoreCallback(), stringToEncrypt);

        String decrypted = SecurityUtil.decryptString(uri.getPath(), "mykey", new TestKeyStoreCallback(), encrypted);
        Assert.assertTrue(stringToEncrypt.equals(decrypted));

    }

    @Test
    public void testEncryptBytes() throws Exception {

        URI uri = this.getClass().getClassLoader().getResource("mykeystore.jks").toURI();

        assert uri != null;

        String stringToEncrypt = "Test string to encrypt";
        byte[] encrypted = SecurityUtil.encrypt(uri.getPath(), "mykey", new TestKeyStoreCallback(),
                stringToEncrypt.getBytes("UTF-8"));

        byte[] decrypted = SecurityUtil.decrypt(uri.getPath(), "mykey", new TestKeyStoreCallback(), encrypted);
        Assert.assertTrue(stringToEncrypt.equals(new String(decrypted, "UTF-8")));

    }

    @Test
    public void testLoadKeyStore() throws Exception{
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("mykeystore.jks");

        KeyStore ks = SecurityUtil.loadKeyStore(inputStream, "jceks", new TestKeyStoreCallback());
        Assert.assertNotNull(ks);

    }

    @Test
    public void testLoadKeyStoreFromFile() throws Exception{
        URI uri = this.getClass().getClassLoader().getResource("mykeystore.jks").toURI();

        assert uri != null;
        KeyStore ks = SecurityUtil.loadKeyStore(uri.getPath(), "jceks", new TestKeyStoreCallback());
        Assert.assertNotNull(ks);

    }

    private class TestKeyStoreCallback implements KeyStorePasswordCallback {

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
