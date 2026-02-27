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
        classes = {JpaConfiguration.class, TestcontainersConfig.class, ServiceStartupTestBase.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",
        })
@ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(ServerProperties.class)
public abstract class ServiceStartupTestBase {

    protected static final Logger logger = LoggerFactory.getLogger(ServiceStartupTestBase.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected ServerProperties properties;

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
                case "rest-api":
                    return properties.services().rest().enabled();
                case "workflow-manager":
                    return properties.services().controller().enabled();
                case "realtime-monitor":
                    return properties.services().monitor().realtime().enabled();
                case "email-monitor":
                    return properties.services().monitor().email().enabled();
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
                "org.apache.airavata.registry",
                "org.apache.airavata.iam",
                "org.apache.airavata.util",
                "org.apache.airavata.exception",
                "org.apache.airavata.status.model",
                "org.apache.airavata.status.entity",
                "org.apache.airavata.experiment",
                "org.apache.airavata.compute",
                "org.apache.airavata.accounting",
                "org.apache.airavata.workflow",
                "org.apache.airavata.execution",
                "org.apache.airavata.research",
                "org.apache.airavata.sharing",
                "org.apache.airavata.gateway",
                "org.apache.airavata.messaging",
                "org.apache.airavata.config",
                "org.apache.airavata.accountprovisioning",
                "org.apache.airavata.credential",
                "org.apache.airavata.job",
                "org.apache.airavata.process",
                "org.apache.airavata.user"
            },
            excludeFilters = {
                @org.springframework.context.annotation.ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                        classes = IntegrationTestConfiguration.class)
            })
    // Spring Boot automatically loads application.properties from test resources
    static class TestConfiguration {}
}
