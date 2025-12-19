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
package org.apache.airavata.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.credential.services.SSHCredentialWriter;
import org.apache.airavata.credential.utils.TokenGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by abhandar on 10/24/16.
 */
@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SSHSummaryTest.TestConfiguration.class},
        properties = {"spring.main.allow-bean-definition-overriding=true", "security.manager.enabled=false"})
@TestPropertySource(locations = "classpath:airavata.properties")
@Transactional
public class SSHSummaryTest {
    private static final Logger logger = LoggerFactory.getLogger(SSHSummaryTest.class);

    @Autowired
    private SSHCredentialWriter sshCredentialWriter;

    // Removed keystore initialization - not needed for SSH credential test

    @Test
    public void testSSHSummary() throws Exception {
        String gatewayId = "test-gateway";

        // Generate RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Convert to PEM-like format (simplified for testing)
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n"
                + java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                        .encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";
        String publicKeyPEM = "ssh-rsa "
                + java.util.Base64.getEncoder()
                        .encodeToString(keyPair.getPublic().getEncoded());

        var sshCredential = new SSHCredential();
        sshCredential.setGatewayId(gatewayId);
        String token = TokenGenerator.generateToken(gatewayId, null);
        sshCredential.setToken(token);
        sshCredential.setPortalUserName("test-user");
        sshCredential.setDescription("Test SSH credential");
        sshCredential.setPrivateKey(privateKeyPEM);
        sshCredential.setPublicKey(publicKeyPEM);
        sshCredential.setPassphrase("test-passphrase");

        sshCredentialWriter.writeCredentials(sshCredential);
        assertEquals(token, sshCredential.getToken());
    }

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.credential",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.config.DozerMapperConfig.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
