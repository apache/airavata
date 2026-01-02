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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify UnifiedApplication startup in REST mode.
 *
 * In REST mode:
 * - Thrift servers should be disabled
 * - REST Proxy should be configured
 * - Background services should still work
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            TestcontainersConfig.class,
            AiravataPropertiesConfiguration.class,
            RestModeStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            // REST mode configuration
            "services.rest.enabled=true",
            // Disable thrift to prevent DBEventManager from being created via @ConditionalOnProperty
            "services.thrift.enabled=false",
            "flyway.enabled=false",
            "services.airavata.enabled=true"
            // Infrastructure components excluded via @ComponentScan excludeFilters - no property flags needed
            // Core services (RegistryService, CredentialStoreService) are always available via DI
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
public class RestModeStartupTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(
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

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testDBEventManagerIsDisabled() {
        // In REST mode, DB Event Manager should not be created (it's thrift-specific)
        assertFalse(
                applicationContext
                                .getBeansOfType(org.apache.airavata.manager.dbevent.DBEventManagerRunner.class)
                                .size()
                        > 0,
                "DBEventManagerRunner should not be configured in REST mode");
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded in REST mode");
    }

    @Test
    public void testCoreServicesStillAvailable() {
        // Core services should still be available in REST mode
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.service.registry.RegistryService.class)
                                .size()
                        > 0,
                "RegistryService should be available in REST mode");
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.service.security.CredentialStoreService.class)
                                .size()
                        > 0,
                "CredentialStoreService should be available in REST mode");
    }
}
