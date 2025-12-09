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
package org.apache.airavata.credential.impl.store;

import java.io.File;
import java.io.FileInputStream;
import org.apache.airavata.credential.impl.ssh.SSHCredential;
import org.apache.airavata.credential.impl.store.SSHCredentialWriter;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SSHCredentialTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class SSHCredentialTest {

    private static final Logger logger = LoggerFactory.getLogger(SSHCredentialTest.class);

    private final SSHCredentialWriter sshCredentialWriter;

    public SSHCredentialTest(SSHCredentialWriter sshCredentialWriter) {
        this.sshCredentialWriter = sshCredentialWriter;
    }

    @Test
    public void testWriteSSHCredential() throws Exception {
        String gatewayId = "test-gateway";
        String privateKeyPath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String pubKeyPath = System.getProperty("user.home") + "/.ssh/id_rsa.pub";

        try {
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
            logger.info("SSH Token: {}", token);
        } catch (Exception e) {
            logger.error("Error writing SSH credential", e);
            throw e;
        }
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.credential",
                "org.apache.airavata.config"
            },
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
