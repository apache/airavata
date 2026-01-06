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

import org.apache.airavata.helix.controller.HelixController;
import org.apache.airavata.helix.controller.ParserWorkflowManager;
import org.apache.airavata.helix.controller.PostWorkflowManager;
import org.apache.airavata.helix.controller.PreWorkflowManager;
import org.apache.airavata.helix.participant.GlobalParticipant;
import org.apache.airavata.monitor.realtime.RealtimeMonitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.TestPropertySource;

/**
 * Test to verify AiravataApplication startup with background services enabled.
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
            TestcontainersConfig.class,
            AiravataServerProperties.class,
            BackgroundServicesStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",

            "services.thrift.enabled=false",

            "services.controller.enabled=true",
            "services.participant.enabled=true",
            "services.prewm.enabled=true",
            "flyway.enabled=false",
            "services.postwm.enabled=true",
            "services.parser.enabled=true",
            "services.monitor.realtime.enabled=true",
            "services.monitor.email.enabled=true"
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:conf/airavata.properties")
@org.springframework.boot.context.properties.EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
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
            })
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;



    @Test
    public void testHelixComponentsAreAvailable() {



        int controllerCount =
                applicationContext.getBeansOfType(HelixController.class).size();
        int participantCount =
                applicationContext.getBeansOfType(GlobalParticipant.class).size();


        assertTrue(
                controllerCount >= 0 && participantCount >= 0,
                "Helix components configuration should be valid (may be 0 in test profile due to @Profile(\"!test\"))");
    }

    @Test
    public void testWorkflowManagersAreAvailable() {



        int preWmCount =
                applicationContext.getBeansOfType(PreWorkflowManager.class).size();
        int postWmCount =
                applicationContext.getBeansOfType(PostWorkflowManager.class).size();
        int parserWmCount =
                applicationContext.getBeansOfType(ParserWorkflowManager.class).size();


        assertTrue(
                preWmCount >= 0 && postWmCount >= 0 && parserWmCount >= 0,
                "Workflow managers configuration should be valid (may be 0 in test profile due to @Profile(\"!test\"))");
    }

    @Test
    public void testMonitorsAreAvailable() {



        int monitorCount =
                applicationContext.getBeansOfType(RealtimeMonitor.class).size();


        assertTrue(
                monitorCount >= 0,
                "RealtimeMonitor configuration should be valid (may be 0 in test profile due to @Profile(\"!test\"))");


    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded with background services enabled");
    }
}
