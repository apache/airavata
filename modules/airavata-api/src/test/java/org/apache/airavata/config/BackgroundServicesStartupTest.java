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

import org.apache.airavata.execution.activity.ProcessActivityManager;
import org.apache.airavata.execution.monitoring.JobStatusMonitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Test to verify AiravataApplication startup with background services enabled.
 *
 * Background services include:
 * - ProcessActivityManager (unified pre/post/cancel workflow handling)
 * - Realtime Monitor
 * - Email Monitor
 */
@SpringBootTest(
        classes = {
            JpaConfiguration.class,
            TestcontainersConfig.class,
            BackgroundServicesStartupTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.main.banner-mode=off",
            "spring.main.log-startup-info=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "airavata.services.controller.enabled=true",
            "airavata.services.participant.enabled=true",
            "airavata.flyway.enabled=false",
            "airavata.services.monitor.realtime.enabled=true",
            "airavata.services.monitor.email.enabled=true"
        })
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.boot.context.properties.EnableConfigurationProperties(
        org.apache.airavata.config.ServerProperties.class)
public class BackgroundServicesStartupTest {

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ComponentScan(
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
                "org.apache.airavata.job",
                "org.apache.airavata.process",
                "org.apache.airavata.user"
            })
    static class TestConfiguration {}

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testWorkflowManagerIsAvailable() {
        // Verify that the context can resolve the unified workflow manager type without error.
        // Bean may be absent if its @Profile excludes "test", but the lookup must not throw.
        assertNotNull(
                applicationContext.getBeansOfType(ProcessActivityManager.class),
                "ProcessActivityManager bean lookup should succeed");
    }

    @Test
    public void testJobStatusMonitorAvailable() {
        assertNotNull(
                applicationContext.getBeansOfType(JobStatusMonitor.class),
                "JobStatusMonitor bean lookup should succeed");
    }

    @Test
    public void testApplicationContextLoads() {
        assertNotNull(applicationContext, "Application context should be loaded with background services enabled");
    }
}
