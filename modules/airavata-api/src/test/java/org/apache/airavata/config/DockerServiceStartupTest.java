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
package org.apache.airavata.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for Docker-based service startup.
 * Uses minimal Spring context to verify configuration loading works in Docker contexts.
 */
@SpringBootTest(classes = DockerServiceStartupTest.MinimalConfig.class)
@ActiveProfiles("test")
public class DockerServiceStartupTest {

    private static final Logger logger = LoggerFactory.getLogger(DockerServiceStartupTest.class);

    @Configuration
    @PropertySource(
        value = "classpath:conf/airavata.properties",
        factory = AiravataPropertySourceFactory.class)
    @EnableConfigurationProperties(AiravataServerProperties.class)
    static class MinimalConfig {
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AiravataServerProperties properties;

    /**
     * Check if docker-startup.sh script exists.
     */
    private boolean isDockerStartupScriptAvailable() {
        String scriptPath = System.getProperty("user.dir");
        if (scriptPath == null) {
            scriptPath = System.getenv("PWD");
        }
        if (scriptPath == null) {
            return false;
        }

        Path script = Paths.get(scriptPath)
                .getParent()
                .getParent()
                .resolve("dev-tools")
                .resolve("deployment-scripts")
                .resolve("docker-startup.sh");
        return Files.exists(script) && Files.isReadable(script);
    }

    @Test
    void testDockerEnvironmentVariables() {
        assertNotNull(applicationContext, "Application context should load");

        String dbHost = System.getenv("DB_HOST");
        String rabbitmqHost = System.getenv("RABBITMQ_HOST");
        String zookeeperHost = System.getenv("ZOOKEEPER_HOST");
        logger.info(
                "Docker environment variables - DB_HOST: {}, RABBITMQ_HOST: {}, ZOOKEEPER_HOST: {}",
                dbHost,
                rabbitmqHost,
                zookeeperHost);
    }

    @Test
    void testDockerStartupScriptAvailability() {
        assertNotNull(applicationContext, "Application context should load");
        boolean available = isDockerStartupScriptAvailable();
        logger.info("Docker startup script available: {}", available);
    }

    @Test
    void testDockerServiceConfiguration() {
        assertNotNull(applicationContext, "Application context should load with Docker service configuration");
        assertNotNull(properties, "Properties should be loaded");
        assertNotNull(properties.services(), "Services should be configured");
    }

    @Test
    void testMissingDockerDependencies() {
        assertNotNull(applicationContext, "Application context should load even if Docker dependencies are missing");
    }

    @Test
    void testAiravataConfigDir() {
        assertNotNull(applicationContext, "Application context should load");

        String airavataHome = System.getProperty("airavata.home");
        if (airavataHome == null || airavataHome.isEmpty()) {
            airavataHome = System.getenv("AIRAVATA_HOME");
        }
        String configDir = null;
        if (airavataHome != null && !airavataHome.isEmpty()) {
            configDir = new java.io.File(airavataHome, "conf").getAbsolutePath();
        }
        logger.info("Airavata home: {}, config directory: {}", airavataHome, configDir);
    }

    @Test
    void testServiceHomeDirectories() {
        assertNotNull(applicationContext, "Application context should load");
        String airavataHome = System.getenv("AIRAVATA_HOME");
        String agentHome = System.getenv("AIRAVATA_AGENT_HOME");
        String researchHome = System.getenv("AIRAVATA_RESEARCH_HOME");
        String fileHome = System.getenv("AIRAVATA_FILE_HOME");
        logger.info(
                "Service homes - Airavata: {}, Agent: {}, Research: {}, File: {}",
                airavataHome,
                agentHome,
                researchHome,
                fileHome);
    }
}
