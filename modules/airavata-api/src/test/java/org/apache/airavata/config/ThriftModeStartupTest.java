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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify AiravataApplication startup in Thrift mode (default).
 *
 * In Thrift mode:
 * - All Thrift API servers should be configured
 * - DB Event Manager should be enabled
 * - Background services can be enabled/disabled
 */
@SpringBootTest(
        classes = {JpaConfig.class, TestcontainersConfig.class, ThriftModeStartupTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "services.thrift.enabled=true",
            "flyway.enabled=false",
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        org.apache.airavata.config.AiravataServerProperties.class)
@EnabledIfSystemProperty(
        named = "test.startup.enabled",
        matches = "true",
        disabledReason = "Startup tests require full infrastructure - run with -Dtest.startup.enabled=true")
public class ThriftModeStartupTest {

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
                "org.apache.airavata.accountprovisioning",
                "org.apache.airavata.helix",
                "org.apache.airavata.manager.dbevent"
            })
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testDBEventDispatcherIsEnabled() {

        String[] beanNames = applicationContext.getBeanNamesForType(org.apache.airavata.messaging.Dispatcher.class);
        int dispatcherCount = beanNames.length;

        assertTrue(dispatcherCount > 0, "Dispatcher should be available (replaces DBEventManagerRunner)");
    }

    @Test
    public void testCoreServicesAreAvailable() {

        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.service.registry.RegistryService.class)
                                .size()
                        > 0,
                "RegistryService should be available in thrift mode");
        assertTrue(
                applicationContext
                                .getBeansOfType(org.apache.airavata.service.security.CredentialStoreService.class)
                                .size()
                        > 0,
                "CredentialStoreService should be available in thrift mode");
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded in thrift mode");
    }
}
