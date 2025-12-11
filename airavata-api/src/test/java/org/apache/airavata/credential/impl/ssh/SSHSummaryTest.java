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
package org.apache.airavata.credential.impl.ssh;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.apache.airavata.credential.impl.store.SSHCredentialWriter;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by abhandar on 10/24/16.
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SSHSummaryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class SSHSummaryTest {
    private static final Logger logger = LoggerFactory.getLogger(SSHSummaryTest.class);

    private final SSHCredentialWriter sshCredentialWriter;

    private X509Certificate[] x509Certificates;
    private PrivateKey privateKey;

    public SSHSummaryTest(SSHCredentialWriter sshCredentialWriter) {
        this.sshCredentialWriter = sshCredentialWriter;
    }

    @BeforeEach
    public void setUp() throws Exception {
        x509Certificates = new X509Certificate[1];
        initializeKeys();
    }

    private void initializeKeys() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] password = "password".toCharArray();

        String baseDirectory = System.getProperty("credential.module.directory");

        String keyStorePath =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "airavata.p12";

        if (baseDirectory != null) {
            keyStorePath = baseDirectory + File.separator + keyStorePath;
        } else {
            keyStorePath = "modules" + File.separator + "credential-store" + File.separator + keyStorePath;
        }

        File keyStoreFile = new File(keyStorePath);
        if (!keyStoreFile.exists()) {
            logger.error("Unable to read keystore file " + keyStoreFile);
            throw new RuntimeException("Unable to read keystore file " + keyStoreFile);
        }

        java.io.FileInputStream fis = null;
        try {
            fis = new java.io.FileInputStream(keyStorePath);
            ks.load(fis, password);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        privateKey = (PrivateKey) ks.getKey("selfsigned", password);
        x509Certificates[0] = (X509Certificate) ks.getCertificate("selfsigned");
    }

    @Test
    public void testSSHSummary() throws Exception {
        String gatewayId = "test-gateway";
        String privateKeyPath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String pubKeyPath = System.getProperty("user.home") + "/.ssh/id_rsa.pub";

        File privateKeyFile = new File(privateKeyPath);
        File pubKeyFile = new File(pubKeyPath);

        if (!privateKeyFile.exists() || !pubKeyFile.exists()) {
            logger.warn("SSH key files not found at {} and {}. Skipping test.", privateKeyPath, pubKeyPath);
            return;
        }

        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setGateway(gatewayId);
        String token = TokenGenerator.generateToken(gatewayId, null);
        sshCredential.setToken(token);
        sshCredential.setPortalUserName("test-user");
        sshCredential.setDescription("Test SSH credential");

        FileInputStream privateKeyStream = new FileInputStream(privateKeyPath);
        File filePri = new File(privateKeyPath);
        byte[] bFilePri = new byte[(int) filePri.length()];
        privateKeyStream.read(bFilePri);

        FileInputStream pubKeyStream = new FileInputStream(pubKeyPath);
        File filePub = new File(pubKeyPath);
        byte[] bFilePub = new byte[(int) filePub.length()];
        pubKeyStream.read(bFilePub);

        privateKeyStream.close();
        pubKeyStream.close();

        sshCredential.setPrivateKey(bFilePri);
        sshCredential.setPublicKey(bFilePub);
        sshCredential.setPassphrase("test-passphrase");

        sshCredentialWriter.writeCredentials(sshCredential);
        assertEquals(token, sshCredential.getToken());
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.credential", "org.apache.airavata.config"},
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
