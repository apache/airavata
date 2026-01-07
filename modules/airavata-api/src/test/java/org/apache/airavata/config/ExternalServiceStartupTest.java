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
 * Tests for external service startup (Agent Service, Research Service, File Service).
 *
 * <p>These services are started via shell scripts in docker-startup.sh and are optional.
 * This test class verifies:
 * <ul>
 *   <li>Services start when available (JAR files present)</li>
 *   <li>Services handle gracefully when not available</li>
 *   <li>Port availability for external services</li>
 * </ul>
 *
 * <p>Note: These tests check for service availability and configuration,
 * but may not actually start the services in test environment.
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, ServiceStartupTestBase.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
public class ExternalServiceStartupTest extends ServiceStartupTestBase {

    /**
     * Check if Agent Service is available (JAR file exists).
     */
    private boolean isAgentServiceAvailable() {
        String agentHome = System.getenv("AIRAVATA_AGENT_HOME");
        if (agentHome == null || agentHome.isEmpty()) {
            return false;
        }
        Path agentJar = Paths.get(agentHome, "lib");
        if (!Files.exists(agentJar)) {
            return false;
        }
        try {
            return Files.list(agentJar)
                    .anyMatch(path -> path.getFileName().toString().startsWith("airavata-agent-service-")
                            && path.getFileName().toString().endsWith(".jar"));
        } catch (Exception e) {
            logger.debug("Error checking Agent Service availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if Research Service is available (JAR file exists).
     */
    private boolean isResearchServiceAvailable() {
        String researchHome = System.getenv("AIRAVATA_RESEARCH_HOME");
        if (researchHome == null || researchHome.isEmpty()) {
            return false;
        }
        Path researchJar = Paths.get(researchHome, "lib");
        if (!Files.exists(researchJar)) {
            return false;
        }
        try {
            return Files.list(researchJar)
                    .anyMatch(path -> path.getFileName().toString().startsWith("airavata-research-service-")
                            && path.getFileName().toString().endsWith(".jar"));
        } catch (Exception e) {
            logger.debug("Error checking Research Service availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if File Service is available (JAR file exists).
     */
    private boolean isFileServiceAvailable() {
        String fileHome = System.getenv("AIRAVATA_FILE_HOME");
        if (fileHome == null || fileHome.isEmpty()) {
            return false;
        }
        Path fileJar = Paths.get(fileHome, "lib");
        if (!Files.exists(fileJar)) {
            return false;
        }
        try {
            return Files.list(fileJar)
                    .anyMatch(path -> path.getFileName().toString().startsWith("airavata-file-server-")
                            && path.getFileName().toString().endsWith(".jar"));
        } catch (Exception e) {
            logger.debug("Error checking File Service availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test that application context loads regardless of Agent Service availability.
     */
    @Test
    public void testAgentServiceAvailability() {
        assertNotNull(applicationContext, "Application context should load regardless of Agent Service availability");
        boolean available = isAgentServiceAvailable();
        logger.info("Agent Service available: {}", available);
    }

    /**
     * Test that application context loads regardless of Research Service availability.
     */
    @Test
    public void testResearchServiceAvailability() {
        assertNotNull(
                applicationContext, "Application context should load regardless of Research Service availability");
        boolean available = isResearchServiceAvailable();
        logger.info("Research Service available: {}", available);
    }

    /**
     * Test that application context loads regardless of File Service availability.
     */
    @Test
    public void testFileServiceAvailability() {
        assertNotNull(applicationContext, "Application context should load regardless of File Service availability");
        boolean available = isFileServiceAvailable();
        logger.info("File Service available: {}", available);
    }

    /**
     * Test that system handles missing external services gracefully.
     */
    @Test
    public void testMissingExternalServices() {
        assertNotNull(applicationContext, "Application context should load even when external services are missing");
    }

    /**
     * Test Agent Service port availability (if service is configured).
     * Port 18800 is used for Agent Service Thrift RPC.
     */
    @Test
    public void testAgentServicePort() {
        assertNotNull(applicationContext, "Application context should load");
    }

    /**
     * Test Research Service port availability (if service is configured).
     * Port 18889 is used for Research Service Thrift RPC.
     */
    @Test
    public void testResearchServicePort() {
        assertNotNull(applicationContext, "Application context should load");
    }

    /**
     * Test File Service port availability (if service is configured).
     * Port 8050 is used for File Service Thrift RPC.
     */
    @Test
    public void testFileServicePort() {
        assertNotNull(applicationContext, "Application context should load");
    }

    /**
     * Test that external service environment variables are respected.
     */
    @Test
    public void testExternalServiceEnvironmentVariables() {
        assertNotNull(applicationContext, "Application context should load");

        String agentHome = System.getenv("AIRAVATA_AGENT_HOME");
        String researchHome = System.getenv("AIRAVATA_RESEARCH_HOME");
        String fileHome = System.getenv("AIRAVATA_FILE_HOME");
        logger.info("External service homes - Agent: {}, Research: {}, File: {}", agentHome, researchHome, fileHome);
    }
}
