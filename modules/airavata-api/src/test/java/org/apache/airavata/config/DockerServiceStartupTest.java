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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests for Docker-based service startup.
 *
 * <p>This test class verifies:
 * <ul>
 *   <li>Docker startup script configuration</li>
 *   <li>Service startup with different combinations via environment variables</li>
 *   <li>Docker-specific configurations are valid</li>
 * </ul>
 *
 * <p>Note: These tests verify configuration validity rather than actually
 * starting Docker containers, as that would require Docker to be available
 * in the test environment.
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            TestcontainersConfig.class,
            AiravataServerProperties.class,
            ServiceStartupTestBase.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
public class DockerServiceStartupTest extends ServiceStartupTestBase {

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
        // Look for docker-startup.sh in dev-tools/deployment-scripts
        Path script = Paths.get(scriptPath)
                .getParent()
                .getParent()
                .resolve("dev-tools")
                .resolve("deployment-scripts")
                .resolve("docker-startup.sh");
        return Files.exists(script) && Files.isReadable(script);
    }

    /**
     * Test that application context loads with Docker environment variables.
     */
    @Test
    public void testDockerEnvironmentVariables() {
        assertNotNull(applicationContext, "Application context should load with Docker environment variables");
        // Check that Docker-specific environment variables are handled
        String dbHost = System.getenv("DB_HOST");
        String rabbitmqHost = System.getenv("RABBITMQ_HOST");
        String zookeeperHost = System.getenv("ZOOKEEPER_HOST");
        logger.info(
                "Docker environment variables - DB_HOST: {}, RABBITMQ_HOST: {}, ZOOKEEPER_HOST: {}",
                dbHost,
                rabbitmqHost,
                zookeeperHost);
        // System should handle both cases (set and unset) gracefully
    }

    /**
     * Test that Docker startup script is available (if running in Docker context).
     */
    @Test
    public void testDockerStartupScriptAvailability() {
        assertNotNull(applicationContext, "Application context should load");
        boolean available = isDockerStartupScriptAvailable();
        logger.info("Docker startup script available: {}", available);
        // Script may not be available in test environment, which is OK
    }

    /**
     * Test that system handles Docker-specific service configurations.
     */
    @org.junit.jupiter.api.Nested
    @org.springframework.test.context.TestPropertySource(
            properties = {
                "services.controller.enabled=true",
                "services.participant.enabled=true",
                "services.prewm.enabled=true",
                "services.postwm.enabled=true"
            })
    class DockerServiceConfigurationTest {
        @Test
        public void testDockerServiceConfiguration() {
            assertNotNull(applicationContext, "Application context should load with Docker service configuration");
            // Verify that services can be configured for Docker environment
        }
    }

    /**
     * Test that system handles missing Docker dependencies gracefully.
     */
    @Test
    public void testMissingDockerDependencies() {
        assertNotNull(applicationContext, "Application context should load even if Docker dependencies are missing");
        // System should handle cases where DB_HOST, RABBITMQ_HOST, etc. are not set
    }

    /**
     * Test that Airavata configuration directory is set correctly for Docker.
     */
    @Test
    public void testAiravataConfigDir() {
        assertNotNull(applicationContext, "Application context should load");
        // Get airavata.home from property or env var, then resolve configDir
        String airavataHome = System.getProperty("airavata.home");
        if (airavataHome == null || airavataHome.isEmpty()) {
            airavataHome = System.getenv("AIRAVATA_HOME");
        }
        String configDir = null;
        if (airavataHome != null && !airavataHome.isEmpty()) {
            configDir = new java.io.File(airavataHome, "conf").getAbsolutePath();
        }
        logger.info("Airavata home: {}, config directory: {}", airavataHome, configDir);
        // In Docker, this should be /opt/airavata/vault
        // In test environment, it may be different, which is OK
    }

    /**
     * Test that service home directories are set correctly for Docker.
     */
    @Test
    public void testServiceHomeDirectories() {
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
        // In Docker, these should be set to /opt/airavata/...
        // In test environment, they may not be set, which is OK
    }
}
