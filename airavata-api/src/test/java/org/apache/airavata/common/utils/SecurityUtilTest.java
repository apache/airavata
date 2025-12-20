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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 10/11/13
 * Time: 10:42 AM
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SecurityUtilTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "security.manager.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class SecurityUtilTest {

    private static final char[] STORE_PASSWORD = "airavata".toCharArray();
    private static final char[] SECRET_PASSWORD = "airavatasecretkey".toCharArray();
    private static String keyStorePath;

    @BeforeAll
    static void setUpKeystore() throws Exception {
        Path tempKeystore = Files.createTempFile("airavata-test-keystore", ".p12");
        tempKeystore.toFile().deleteOnExit();

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, STORE_PASSWORD);

        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey secretKey = kg.generateKey();
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(SECRET_PASSWORD);
        ks.setEntry("mykey", entry, protection);

        try (var out = Files.newOutputStream(tempKeystore)) {
            ks.store(out, STORE_PASSWORD);
        }

        keyStorePath = tempKeystore.toString();
    }

    public SecurityUtilTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

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
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testLoadKeyStoreFromFile() throws Exception {
        KeyStore ks = SecurityUtil.loadKeyStore(keyStorePath, new TestKeyStoreCallback());
        assertNotNull(ks);
    }

    private static class TestKeyStoreCallback implements KeyStorePasswordCallback {

        @Override
        public char[] getStorePassword() {
            return STORE_PASSWORD;
        }

        @Override
        public char[] getSecretKeyPassPhrase(String keyAlias) {
            if (keyAlias.equals("mykey")) {
                return SECRET_PASSWORD;
            }
            return null;
        }
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.common",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.DozerMapperConfig.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
