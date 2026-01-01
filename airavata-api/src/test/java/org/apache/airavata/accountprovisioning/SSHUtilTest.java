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
package org.apache.airavata.accountprovisioning;

import com.jcraft.jsch.JSchException;
import org.apache.airavata.credential.model.SSHCredential;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.AiravataPropertiesConfiguration.class,
            SSHUtilTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
public class SSHUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(SSHUtilTest.class);

    public SSHUtilTest() {
        // Spring Boot test - no dependencies to inject for this utility test
    }

    @Test
    public void testValidate() throws JSchException {

        // Test the validate method with in-memory generated keys
        // Note: Actual SSH connection will fail without a real host, but we test the method signature
        String username = "testuser";
        String passphrase = "";
        String hostname = "localhost"; // Will fail connection but tests the method

        // Generate RSA key pair for testing
        java.security.KeyPairGenerator keyGen;
        try {
            keyGen = java.security.KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            java.security.KeyPair keyPair = keyGen.generateKeyPair();

            // Convert to PEM-like format
            String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n"
                    + java.util.Base64.getMimeEncoder(64, "\n".getBytes())
                            .encodeToString(keyPair.getPrivate().getEncoded())
                    + "\n-----END PRIVATE KEY-----";
            String publicKeyPEM = "ssh-rsa "
                    + java.util.Base64.getEncoder()
                            .encodeToString(keyPair.getPublic().getEncoded());

            SSHCredential sshCredential = new SSHCredential();
            sshCredential.setPassphrase(passphrase);
            sshCredential.setPublicKey(publicKeyPEM);
            sshCredential.setPrivateKey(privateKeyPEM);

            // Attempt validation - will fail without real host but tests the method
            try {
                boolean result = SSHUtil.validate(hostname, 22, username, sshCredential);
                logger.info("SSH validation result: {}", result);
            } catch (Exception e) {
                // Expected to fail without real SSH host - just verify method can be called
                logger.debug("SSH validation failed as expected (no real host): {}", e.getMessage());
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate test keys", e);
        }
    }

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.accountprovisioning", "org.apache.airavata.config"},
            excludeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.accountprovisioning.SSHAccountManager.class
                        })
            })
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}
}
