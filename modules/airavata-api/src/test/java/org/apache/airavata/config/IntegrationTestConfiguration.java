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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.airavata.iam.service.CredentialStoreService;
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
 *   <li>Provides a minimal {@link ServerProperties} for testing</li>
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
 * @see JpaConfiguration
 * @see TestcontainersConfig
 */
@Configuration
// application.properties is auto-loaded by Spring Boot
@Import({JpaConfiguration.class, TestcontainersConfig.class})
// Use CGLIB proxies to allow tests to inject concrete class types instead of interfaces
@EnableAspectJAutoProxy(proxyTargetClass = true)
// Enable configuration properties binding for ServerProperties
@EnableConfigurationProperties(ServerProperties.class)
@ComponentScan(
        basePackages = {
            "org.apache.airavata.status.service",
            "org.apache.airavata.status.mapper",
            "org.apache.airavata.research.experiment.mapper",
            "org.apache.airavata.research.experiment.service",
            "org.apache.airavata.research.application.mapper",
            "org.apache.airavata.research.application.service",
            "org.apache.airavata.research.artifact",
            "org.apache.airavata.execution.mapper",
            "org.apache.airavata.compute.resource.adapter",
            "org.apache.airavata.gateway.mapper",
            "org.apache.airavata.iam.mapper",
            "org.apache.airavata.gateway.service",
            "org.apache.airavata.iam.service",
            "org.apache.airavata.compute.resource.service",
            "org.apache.airavata.execution.service",
            "org.apache.airavata.workflow.service"
        },
        // Exclude components that require external dependencies
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Credential.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Monitor.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Kafka.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Rabbit.*"),
            // Services that need external beans not available in minimal test context
            // (ResourceProfileAdapter, IamAdminService, GatewayGroupsInitializer, etc.)
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*DefaultUserService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*ArtifactService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*AllocationProjectService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*DefaultResourceService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*DefaultSharingService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*DefaultGroupService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*IamAdminService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Keycloak.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*GatewayGroupsInitializer.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*AuthorizationService.*"),
            // High-level orchestration services with complex dependency trees
            // DefaultExperimentService now includes lifecycle/sharing logic; exclude it in minimal test context
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*DefaultExperimentService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*ExperimentSearchService.*"),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*WorkflowService.*")
        })
public class IntegrationTestConfiguration {

    // Note: ServerProperties is provided by @EnableConfigurationProperties
    // in test classes. This allows @DynamicPropertySource to inject container URLs.

    /**
     * Mock CredentialStoreService so registry services that depend on it (e.g. PreferenceResolutionService)
     * can be instantiated in tests. Tests that need real credential behavior should @Import their own config.
     */
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    CredentialStoreService credentialStoreService() {
        CredentialStoreService service = mock(CredentialStoreService.class);
        when(service.credentialExists(anyString(), anyString())).thenReturn(true);
        return service;
    }

    /** Mock SharingService so DefaultGatewayService can be created in tests. */
    @Bean
    org.apache.airavata.iam.service.SharingService sharingService() {
        return mock(org.apache.airavata.iam.service.SharingService.class);
    }
}
