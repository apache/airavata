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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for service startup integration tests.
 *
 * <p>Provides common setup including:
 * <ul>
 *   <li>Testcontainers configuration for infrastructure services</li>
 *   <li>Utility methods for service status checking</li>
 *   <li>Configuration property management</li>
 *   <li>Service verification helpers</li>
 * </ul>
 *
 * <p>Subclasses should use {@code @SpringBootTest} with appropriate configuration
 * and override {@link #getTestProperties()} to provide service-specific properties.
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
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
public abstract class ServiceStartupTestBase {

    protected static final Logger logger = LoggerFactory.getLogger(ServiceStartupTestBase.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected AiravataServerProperties properties;

    /**
     * Get test-specific properties to override defaults.
     * Subclasses should override this method to provide service-specific configurations.
     *
     * @return Map of property keys to values
     */
    protected Map<String, String> getTestProperties() {
        return Map.of();
    }

    /**
     * Check if a service is enabled in configuration by checking properties.
     *
     * @param serviceName Name of the service
     * @return true if enabled based on properties, false otherwise
     */
    protected boolean isServiceEnabled(String serviceName) {
        if (properties == null) {
            return false;
        }
        try {
            // Check properties directly
            switch (serviceName) {
                case "thrift-api":
                    return properties.services.thrift.enabled;
                case "rest-api":
                    return properties.services.rest.enabled;
                case "helix-controller":
                    return properties.services.controller.enabled;
                case "helix-participant":
                    return properties.services.participant.enabled;
                case "pre-workflow-manager":
                    return properties.services.prewm.enabled;
                case "post-workflow-manager":
                    return properties.services.postwm.enabled;
                case "parser-workflow-manager":
                    return properties.services.parser.enabled;
                case "realtime-monitor":
                    return properties.services.monitor.realtime.enabled;
                case "email-monitor":
                    return properties.services.monitor.email.enabled;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.debug("Error checking if service {} is enabled: {}", serviceName, e.getMessage());
            return false;
        }
    }

    /**
     * Test configuration for service startup tests.
     */
    @org.springframework.context.annotation.Configuration
    @org.springframework.context.annotation.EnableAspectJAutoProxy(proxyTargetClass = true)
    @org.springframework.context.annotation.ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.service",
                "org.apache.airavata.profile.repositories",
                "org.apache.airavata.profile.mappers",
                "org.apache.airavata.profile.utils",
                "org.apache.airavata.sharing.services",
                "org.apache.airavata.sharing.repositories",
                "org.apache.airavata.sharing.mappers",
                "org.apache.airavata.sharing.utils",
                "org.apache.airavata.credential.repositories",
                "org.apache.airavata.credential.services",
                "org.apache.airavata.credential.utils",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils",
                "org.apache.airavata.security",
                "org.apache.airavata.accountprovisioning"
            })
    static class TestConfiguration {}
}
