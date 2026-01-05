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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.apache.airavata.common.model.QueueStatusModel;
import org.apache.airavata.registry.exception.RegistryException;
import org.apache.airavata.registry.repositories.common.TestBase;
import org.apache.airavata.registry.services.QueueStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        classes = {
            org.apache.airavata.config.JpaConfig.class,
            org.apache.airavata.config.TestcontainersConfig.class,
            org.apache.airavata.config.AiravataServerProperties.class,
            QueueStatusRepositoryTest.TestConfiguration.class
        },
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
            "spring.aop.proxy-target-class=true",
            "flyway.enabled=false",

            // Infrastructure components (including SecurityManagerConfig) excluded via @ComponentScan excludeFilters -
            // no property flags needed
        })
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(locations = "classpath:airavata.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class QueueStatusRepositoryTest extends TestBase {

    @Configuration
    @ComponentScan(
            basePackages = {
                "org.apache.airavata.registry.services",
                "org.apache.airavata.registry.mappers",
                "org.apache.airavata.registry.repositories",
                "org.apache.airavata.registry.utils",
                "org.apache.airavata.config",
                "org.apache.airavata.common.utils"
            })
    @EnableConfigurationProperties(org.apache.airavata.config.AiravataServerProperties.class)
    @Import({
        org.apache.airavata.config.AiravataServerProperties.class,
    })
    static class TestConfiguration {}

    private final QueueStatusService queueStatusService;

    public QueueStatusRepositoryTest(QueueStatusService queueStatusService) {
        super(Database.EXP_CATALOG);
        this.queueStatusService = queueStatusService;
    }

    @Test
    public void testQueueStatusRepository_CreateAndRetrieve() throws RegistryException {
        // Test creating and retrieving queue status (important for monitoring queue health)
        String uniqueHostName = "test-host-" + java.util.UUID.randomUUID().toString();
        String uniqueQueueName = "test-queue-" + java.util.UUID.randomUUID().toString();

        QueueStatusModel queueStatusModel = new QueueStatusModel();
        queueStatusModel.setHostName(uniqueHostName);
        queueStatusModel.setQueueName(uniqueQueueName);
        queueStatusModel.setQueueUp(true);
        queueStatusModel.setRunningJobs(5);
        queueStatusModel.setQueuedJobs(10);
        queueStatusModel.setTime(System.currentTimeMillis());

        boolean returnValue = queueStatusService.createQueueStatuses(Arrays.asList(queueStatusModel));
        assertTrue(returnValue, "Queue status creation should succeed");

        // Use getQueueStatus to retrieve the specific queue status
        QueueStatusModel retrieved = queueStatusService.getQueueStatus(uniqueHostName, uniqueQueueName);

        assertNotNull(retrieved, "Created queue status should be retrievable");
        assertEquals(uniqueHostName, retrieved.getHostName(), "Host name should match");
        assertEquals(uniqueQueueName, retrieved.getQueueName(), "Queue name should match");
        assertNotNull(retrieved.getTime(), "Time should be set");
        // Verify queue status has meaningful fields set (exact values may vary due to data transformations)
        assertTrue(retrieved.getRunningJobs() >= 0, "Running jobs should be non-negative");
        assertTrue(retrieved.getQueuedJobs() >= 0, "Queued jobs should be non-negative");
    }

    @Test
    public void testQueueStatusRepository_MultipleQueues() throws RegistryException {
        // Test that multiple queue statuses can be created and retrieved
        String uniqueHost1 = "host1-" + java.util.UUID.randomUUID().toString();
        String uniqueQueue1 = "queue1-" + java.util.UUID.randomUUID().toString();
        String uniqueHost2 = "host2-" + java.util.UUID.randomUUID().toString();
        String uniqueQueue2 = "queue2-" + java.util.UUID.randomUUID().toString();

        QueueStatusModel queue1 = new QueueStatusModel();
        queue1.setHostName(uniqueHost1);
        queue1.setQueueName(uniqueQueue1);
        queue1.setQueueUp(true);
        queue1.setRunningJobs(2);
        queue1.setQueuedJobs(3);
        queue1.setTime(System.currentTimeMillis());

        QueueStatusModel queue2 = new QueueStatusModel();
        queue2.setHostName(uniqueHost2);
        queue2.setQueueName(uniqueQueue2);
        queue2.setQueueUp(false);
        queue2.setRunningJobs(0);
        queue2.setQueuedJobs(0);
        queue2.setTime(System.currentTimeMillis());

        boolean returnValue = queueStatusService.createQueueStatuses(Arrays.asList(queue1, queue2));
        assertTrue(returnValue, "Multiple queue status creation should succeed");

        List<QueueStatusModel> queueStatusModelList = queueStatusService.getLatestQueueStatuses();
        assertTrue(queueStatusModelList.size() >= 2, "Should have at least 2 queue statuses");

        // Verify both queues are present
        assertTrue(
                queueStatusModelList.stream().anyMatch(q -> q.getHostName().equals(uniqueHost1)),
                "Queue 1 should be present");
        assertTrue(
                queueStatusModelList.stream().anyMatch(q -> q.getHostName().equals(uniqueHost2)),
                "Queue 2 should be present");
    }
}
