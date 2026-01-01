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

import org.apache.airavata.helix.impl.controller.HelixController;
import org.apache.airavata.helix.impl.participant.GlobalParticipant;
import org.apache.airavata.helix.impl.workflow.ParserWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PostWorkflowManager;
import org.apache.airavata.helix.impl.workflow.PreWorkflowManager;
import org.apache.airavata.monitor.email.EmailBasedMonitor;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify UnifiedApplication startup with background services enabled.
 *
 * Background services include:
 * - Helix Controller
 * - Global Participant
 * - Pre/Post/Parser Workflow Managers
 * - Realtime Monitor
 * - Email Monitor
 */
@SpringBootTest(
        classes = {
            JpaConfig.class,
            AiravataPropertiesConfiguration.class,
            BackgroundServicesLauncher.class,
            BackgroundServicesStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.allow-circular-references=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            // Background/infrastructure services - keep property flags (truly optional)
            "services.thrift.enabled=false",
            "services.background.enabled=true",
            "services.orchestrator.enabled=false",
            // Core services (RegistryService, CredentialStoreService) are always available via DI - no flags needed
            "helix.controller.enabled=true",
            "helix.participant.enabled=true",
            "services.prewm.enabled=true",
            "services.postwm.enabled=true",
            "services.parser.enabled=true",
            "services.monitor.realtime.monitorEnabled=true",
            "services.monitor.email.monitorEnabled=true"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
public class BackgroundServicesStartupTest {

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
                "org.apache.airavata.monitor",
                "org.apache.airavata.manager.dbevent"
            },
            useDefaultFilters = false,
            includeFilters = {
                @ComponentScan.Filter(
                        type = org.springframework.context.annotation.FilterType.ANNOTATION,
                        classes = {
                            org.springframework.stereotype.Component.class,
                            org.springframework.stereotype.Service.class,
                            org.springframework.stereotype.Repository.class,
                            org.springframework.context.annotation.Configuration.class
                        })
            })
    @Import({AiravataPropertiesConfiguration.class})
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testBackgroundServicesLauncherIsConfigured() {
        // BackgroundServicesLauncher should be available when enabled
        assertTrue(
                applicationContext
                                .getBeansOfType(BackgroundServicesLauncher.class)
                                .size()
                        > 0,
                "BackgroundServicesLauncher should be configured when background services are enabled");
    }

    @Test
    public void testHelixComponentsAreAvailable() {
        // Helix components should be available as beans
        assertTrue(
                applicationContext.getBeansOfType(HelixController.class).size() > 0,
                "HelixController should be available");
        assertTrue(
                applicationContext.getBeansOfType(GlobalParticipant.class).size() > 0,
                "GlobalParticipant should be available");
    }

    @Test
    public void testWorkflowManagersAreAvailable() {
        // Workflow managers should be available
        assertTrue(
                applicationContext.getBeansOfType(PreWorkflowManager.class).size() > 0,
                "PreWorkflowManager should be available");
        assertTrue(
                applicationContext.getBeansOfType(PostWorkflowManager.class).size() > 0,
                "PostWorkflowManager should be available");
        assertTrue(
                applicationContext.getBeansOfType(ParserWorkflowManager.class).size() > 0,
                "ParserWorkflowManager should be available");
    }

    @Test
    public void testMonitorsAreAvailable() {
        // Monitors should be available
        assertTrue(
                applicationContext.getBeansOfType(RealtimeMonitor.class).size() > 0,
                "RealtimeMonitor should be available");
        assertTrue(
                applicationContext.getBeansOfType(EmailBasedMonitor.class).size() > 0,
                "EmailBasedMonitor should be available");
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded with background services enabled");
    }
}
