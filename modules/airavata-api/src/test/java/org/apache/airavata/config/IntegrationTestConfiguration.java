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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.airavata.service.security.CredentialStoreService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

/**
 * Shared Spring configuration for integration tests.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Provides a minimal {@link AiravataServerProperties} for testing</li>
 *   <li>Scans only repository-related packages (not security, monitoring, etc.)</li>
 *   <li>Imports JPA and Testcontainers configurations</li>
 *   <li>Provides a mock {@link CredentialStoreService} so registry services (e.g. PreferenceResolutionService) can be created</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>
 * {@code
 * @SpringBootTest(classes = IntegrationTestConfiguration.class)
 * class MyTest { ... }
 * }
 * </pre>
 *
 * @see JpaConfig
 * @see TestcontainersConfig
 */
@Configuration
// application.properties is auto-loaded by Spring Boot
@Import({JpaConfig.class, TestcontainersConfig.class})
// Use CGLIB proxies to allow tests to inject concrete class types instead of interfaces
@EnableAspectJAutoProxy(proxyTargetClass = true)
// Enable configuration properties binding for AiravataServerProperties
@EnableConfigurationProperties(AiravataServerProperties.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.registry.services",
            "org.apache.airavata.registry.mappers",
            "org.apache.airavata.registry.repositories"
        },
        // Exclude components that require external dependencies
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Credential.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Monitor.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Kafka.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Rabbit.*")
        })
public class IntegrationTestConfiguration {

    // Note: AiravataServerProperties is provided by @EnableConfigurationProperties
    // in test classes. This allows @DynamicPropertySource to inject container URLs.

    /**
     * Mock CredentialStoreService so registry services that depend on it (e.g. PreferenceResolutionService)
     * can be instantiated in tests. Tests that need real credential behavior should @Import their own config.
     */
    @Bean
    CredentialStoreService credentialStoreService() {
        CredentialStoreService service = mock(CredentialStoreService.class);
        when(service.credentialExists(anyString(), anyString())).thenReturn(true);
        return service;
    }
}
