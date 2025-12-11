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
package org.apache.airavata.registry.repositories.expcatalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.airavata.model.workspace.Notification;
import org.apache.airavata.model.workspace.NotificationPriority;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {org.apache.airavata.config.JpaConfig.class, NotificationRepositoryTest.TestConfiguration.class},
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "services.background.enabled=false",
            "services.thrift.enabled=false",
            "services.helix.enabled=false"
        })
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class NotificationRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {"org.apache.airavata.service", "org.apache.airavata.registry", "org.apache.airavata.config"
            },
            excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                            org.apache.airavata.config.BackgroundServicesLauncher.class,
                            org.apache.airavata.config.ThriftServerLauncher.class,
                            org.apache.airavata.monitor.realtime.RealtimeMonitor.class,
                            org.apache.airavata.monitor.email.EmailBasedMonitor.class,
                            org.apache.airavata.monitor.cluster.ClusterStatusMonitorJob.class,
                            org.apache.airavata.monitor.AbstractMonitor.class,
                            org.apache.airavata.helix.impl.controller.HelixController.class,
                            org.apache.airavata.helix.impl.participant.GlobalParticipant.class,
                            org.apache.airavata.helix.impl.workflow.PreWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.PostWorkflowManager.class,
                            org.apache.airavata.helix.impl.workflow.ParserWorkflowManager.class
                        }),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.monitor\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.apache\\.airavata\\.helix\\..*")
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import(org.apache.airavata.config.AiravataPropertiesConfiguration.class)
    static class TestConfiguration {}

    private String testGateway = "testGateway";

    private final NotificationService notificationService;

    public NotificationRepositoryTest(NotificationService notificationService) {
        super(Database.EXP_CATALOG);
        this.notificationService = notificationService;
    }

    @Test
    public void testNotificationRepository() throws RegistryException {
        Notification notification = new Notification();
        notification.setNotificationId("notificationId");
        notification.setGatewayId(testGateway);
        notification.setTitle("notificationTitle");
        notification.setNotificationMessage("notificationMessage");

        String notificationId = notificationService.createNotification(notification);
        assertEquals(notification.getNotificationId(), notificationId);

        notification.setPriority(NotificationPriority.NORMAL);
        notificationService.updateNotification(notification);

        Notification retrievedNotification = notificationService.getNotification(notificationId);
        assertEquals(NotificationPriority.NORMAL, retrievedNotification.getPriority());

        assertTrue(notificationService.getAllGatewayNotifications(testGateway).size() == 1);

        notificationService.deleteNotification(notificationId);
    }
}
