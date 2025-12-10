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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, SSHUtilTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
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

        // Test the validate method
        String username = System.getProperty("user.name");
        String privateKeyFilepath = System.getProperty("user.home") + "/.ssh/id_rsa";
        String publicKeyFilepath = privateKeyFilepath + ".pub";
        String passphrase = "changeme";
        String hostname = "changeme";

        Path privateKeyPath = Paths.get(privateKeyFilepath);
        Path publicKeyPath = Paths.get(publicKeyFilepath);

        if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
            logger.warn("SSH key files not found. Skipping test.");
            return;
        }

        SSHCredential sshCredential = new SSHCredential();
        sshCredential.setPassphrase(passphrase);
        try {
            sshCredential.setPublicKey(new String(Files.readAllBytes(publicKeyPath), "UTF-8"));
            sshCredential.setPrivateKey(new String(Files.readAllBytes(privateKeyPath), "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean result = SSHUtil.validate(hostname, 22, username, sshCredential);
        logger.info("SSH validation result: {}", result);
    }

    @org.springframework.context.annotation.Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.accountprovisioning",
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
